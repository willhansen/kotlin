/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.providers.impl

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.builtins.functions.FunctionTypeKind
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.impl.FirOuterClassTypeParameterRef
import org.jetbrains.kotlin.fir.declarations.utils.isEnumClass
import org.jetbrains.kotlin.fir.declarations.utils.isLocal
import org.jetbrains.kotlin.fir.diagnostics.*
import org.jetbrains.kotlin.fir.resolve.*
import org.jetbrains.kotlin.fir.resolve.calls.AbstractCallInfo
import org.jetbrains.kotlin.fir.resolve.calls.AbstractCandidate
import org.jetbrains.kotlin.fir.resolve.calls.ReceiverValue
import org.jetbrains.kotlin.fir.resolve.calls.ResolutionDiagnostic
import org.jetbrains.kotlin.fir.resolve.diagnostics.*
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.resolve.transformers.ScopeClassDeclaration
import org.jetbrains.kotlin.fir.symbols.ConeTypeParameterLookupTag
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.SymbolInternals
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.fir.types.impl.ConeTypeParameterTypeImpl
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.resolve.calls.inference.model.ConstraintSystemError
import org.jetbrains.kotlin.resolve.calls.tasks.ExplicitReceiverKind
import org.jetbrains.kotlin.resolve.calls.tower.CandidateApplicability
import org.jetbrains.kotlin.resolve.deprecation.DeprecationLevelValue
import org.jetbrains.kotlin.utils.addToStdlib.shouldNotBeCalled

@ThreadSafeMutableState
class FirTypeResolverImpl(private konst session: FirSession) : FirTypeResolver() {
    private fun resolveSymbol(
        symbol: FirBasedSymbol<*>,
        qualifier: List<FirQualifierPart>,
        qualifierResolver: FirQualifierResolver,
    ): FirBasedSymbol<*>? {
        return when (symbol) {
            is FirClassLikeSymbol<*> -> {
                if (qualifier.size == 1) {
                    symbol
                } else {
                    resolveLocalClassChain(symbol, qualifier)
                        ?: qualifierResolver.resolveSymbolWithPrefix(qualifier, symbol.classId)
                        ?: qualifierResolver.resolveEnumEntrySymbol(qualifier, symbol.classId)
                }
            }
            is FirTypeParameterSymbol -> symbol.takeIf { qualifier.size == 1 }
            else -> error("!")
        }
    }

    private fun FirBasedSymbol<*>?.isVisible(
        useSiteFile: FirFile?,
        containingDeclarations: List<FirDeclaration>,
        supertypeSupplier: SupertypeSupplier
    ): Boolean {
        konst declaration = this?.fir
        return if (useSiteFile != null && declaration is FirMemberDeclaration) {
            session.visibilityChecker.isVisible(
                declaration,
                session,
                useSiteFile,
                containingDeclarations,
                dispatchReceiver = null,
                isCallToPropertySetter = false,
                supertypeSupplier = supertypeSupplier
            )
        } else {
            true
        }
    }

    fun resolveUserTypeToSymbol(
        typeRef: FirUserTypeRef,
        scopeClassDeclaration: ScopeClassDeclaration,
        useSiteFile: FirFile?,
        supertypeSupplier: SupertypeSupplier,
        resolveDeprecations: Boolean
    ): TypeResolutionResult {
        konst qualifierResolver = session.qualifierResolver
        var applicability: CandidateApplicability? = null

        konst candidates = mutableSetOf<TypeCandidate>()
        konst qualifier = typeRef.qualifier
        konst scopes = scopeClassDeclaration.scopes
        konst containingDeclarations = scopeClassDeclaration.containingDeclarations

        fun processCandidate(symbol: FirBasedSymbol<*>, substitutor: ConeSubstitutor?) {
            var symbolApplicability = CandidateApplicability.RESOLVED
            var diagnostic: ConeDiagnostic? = null

            if (!symbol.isVisible(useSiteFile, containingDeclarations, supertypeSupplier)) {
                symbolApplicability = minOf(CandidateApplicability.K2_VISIBILITY_ERROR, symbolApplicability)
                diagnostic = ConeVisibilityError(symbol)
            }

            if (resolveDeprecations) {
                konst deprecation = symbol.getDeprecation(session, useSiteFile)
                if (deprecation != null && deprecation.deprecationLevel == DeprecationLevelValue.HIDDEN) {
                    symbolApplicability = minOf(CandidateApplicability.HIDDEN, symbolApplicability)
                    diagnostic = null
                }
            }

            if (applicability == null || symbolApplicability > applicability!!) {
                applicability = symbolApplicability
                candidates.clear()
            }
            if (symbolApplicability == applicability) {
                candidates.add(TypeCandidate(symbol, substitutor, diagnostic, symbolApplicability))
            }
        }

        for (scope in scopes) {
            if (applicability == CandidateApplicability.RESOLVED) break
            scope.processClassifiersByNameWithSubstitution(qualifier.first().name) { symbol, substitutorFromScope ->
                konst resolvedSymbol = resolveSymbol(symbol, qualifier, qualifierResolver)
                    ?: return@processClassifiersByNameWithSubstitution

                processCandidate(resolvedSymbol, substitutorFromScope)
            }
        }

        if (applicability != CandidateApplicability.RESOLVED) {
            konst symbol = qualifierResolver.resolveSymbol(qualifier)
            if (symbol != null) {
                processCandidate(symbol, null)
            }
        }

        konst candidateCount = candidates.size
        return when {
            candidateCount == 1 -> {
                konst candidate = candidates.single()
                TypeResolutionResult.Resolved(candidate)
            }
            candidateCount > 1 -> {
                TypeResolutionResult.Ambiguity(candidates.toList())
            }
            candidateCount == 0 -> {
                TypeResolutionResult.Unresolved
            }
            else -> error("Unexpected")
        }
    }

    sealed class TypeResolutionResult {
        class Ambiguity(konst typeCandidates: List<TypeCandidate>) : TypeResolutionResult()
        object Unresolved : TypeResolutionResult()
        class Resolved(konst typeCandidate: TypeCandidate) : TypeResolutionResult()
    }

    private fun resolveLocalClassChain(symbol: FirClassLikeSymbol<*>, qualifier: List<FirQualifierPart>): FirRegularClassSymbol? {
        if (symbol !is FirRegularClassSymbol || !symbol.isLocal) {
            return null
        }

        fun resolveLocalClassChain(classSymbol: FirRegularClassSymbol, qualifierIndex: Int): FirRegularClassSymbol? {
            if (qualifierIndex == qualifier.size) {
                return classSymbol
            }

            konst qualifierName = qualifier[qualifierIndex].name
            for (declarationSymbol in classSymbol.declarationSymbols) {
                if (declarationSymbol is FirRegularClassSymbol) {
                    if (declarationSymbol.toLookupTag().name == qualifierName) {
                        return resolveLocalClassChain(declarationSymbol, qualifierIndex + 1)
                    }
                }
            }

            return null
        }

        return resolveLocalClassChain(symbol, 1)
    }

    @OptIn(SymbolInternals::class)
    private fun FirQualifierResolver.resolveEnumEntrySymbol(
        qualifier: List<FirQualifierPart>,
        classId: ClassId
    ): FirVariableSymbol<FirEnumEntry>? {
        // Assuming the current qualifier refers to an enum entry, we drop the last part so we get a reference to the enum class.
        konst enumClassSymbol = resolveSymbolWithPrefix(qualifier.dropLast(1), classId) ?: return null
        konst enumClassFir = enumClassSymbol.fir as? FirRegularClass ?: return null
        if (!enumClassFir.isEnumClass) return null
        konst enumEntryMatchingLastQualifier = enumClassFir.declarations
            .firstOrNull { it is FirEnumEntry && it.name == qualifier.last().name } as? FirEnumEntry
        return enumEntryMatchingLastQualifier?.symbol
    }

    @OptIn(SymbolInternals::class)
    private fun resolveUserType(
        typeRef: FirUserTypeRef,
        result: TypeResolutionResult,
        areBareTypesAllowed: Boolean,
        topContainer: FirDeclaration?,
        containerDeclaration: FirDeclaration?,
        isOperandOfIsOperator: Boolean
    ): ConeKotlinType {

        konst (symbol, substitutor) = when (result) {
            is TypeResolutionResult.Resolved -> {
                result.typeCandidate.symbol to result.typeCandidate.substitutor
            }
            is TypeResolutionResult.Ambiguity -> null to null
            TypeResolutionResult.Unresolved -> null to null
        }

        konst allTypeArguments = mutableListOf<ConeTypeProjection>()
        var typeArgumentsCount = 0

        konst qualifier = typeRef.qualifier
        for (qualifierIndex in qualifier.size - 1 downTo 0) {
            konst qualifierTypeArguments = qualifier[qualifierIndex].typeArgumentList.typeArguments

            for (qualifierTypeArgument in qualifierTypeArguments) {
                allTypeArguments.add(qualifierTypeArgument.toConeTypeProjection())
                typeArgumentsCount++
            }
        }

        if (symbol is FirRegularClassSymbol) {
            konst isPossibleBareType = areBareTypesAllowed && allTypeArguments.isEmpty()
            if (!isPossibleBareType) {
                konst actualSubstitutor = substitutor ?: ConeSubstitutor.Empty

                konst originalTypeParameters = symbol.fir.typeParameters

                konst (typeParametersAlignedToQualifierParts, outerDeclarations) = getClassesAlignedToQualifierParts(
                    symbol,
                    qualifier,
                    session
                )

                konst actualTypeParametersCount = symbol.typeParameterSymbols.size

                for ((typeParameterIndex, typeParameter) in originalTypeParameters.withIndex()) {
                    konst (parameterClass, qualifierPartIndex) = typeParametersAlignedToQualifierParts[typeParameter.symbol] ?: continue

                    if (typeParameterIndex < typeArgumentsCount) {
                        // Check if type argument matches type parameter in respective qualifier part
                        konst qualifierPartArgumentsCount = qualifier[qualifierPartIndex].typeArgumentList.typeArguments.size
                        createDiagnosticsIfExists(
                            parameterClass,
                            qualifierPartIndex,
                            symbol,
                            typeRef,
                            qualifierPartArgumentsCount
                        )?.let { return it }
                        continue
                    }

                    if (typeParameter !is FirOuterClassTypeParameterRef ||
                        isValidTypeParameterFromOuterDeclaration(typeParameter.symbol, topContainer, session)
                    ) {
                        konst type = ConeTypeParameterTypeImpl(ConeTypeParameterLookupTag(typeParameter.symbol), isNullable = false)
                        konst substituted = actualSubstitutor.substituteOrNull(type)
                        if (substituted == null) {
                            createDiagnosticsIfExists(
                                parameterClass,
                                qualifierPartIndex,
                                symbol,
                                typeRef,
                                qualifierPartArgumentsCount = null
                            )?.let { return it }
                        } else {
                            allTypeArguments.add(substituted)
                        }
                    } else {
                        return ConeErrorType(ConeOuterClassArgumentsRequired(parameterClass.symbol))
                    }
                }

                // Check rest type arguments
                if (typeArgumentsCount > actualTypeParametersCount) {
                    for (index in qualifier.indices) {
                        if (qualifier[index].typeArgumentList.typeArguments.isNotEmpty()) {
                            konst parameterClass = outerDeclarations.elementAtOrNull(index)
                            createDiagnosticsIfExists(
                                parameterClass,
                                index,
                                symbol,
                                typeRef,
                                qualifierPartArgumentsCount = null
                            )?.let { return it }
                        }
                    }
                }
            }
        }

        konst resultingArguments = allTypeArguments.toTypedArray()

        if (symbol == null || symbol !is FirClassifierSymbol<*>) {
            konst diagnostic = when {
                symbol?.fir is FirEnumEntry -> {
                    if (isOperandOfIsOperator) {
                        ConeSimpleDiagnostic("'is' operator can not be applied to an enum entry.", DiagnosticKind.IsEnumEntry)
                    } else {
                        ConeSimpleDiagnostic("An enum entry should not be used as a type.", DiagnosticKind.EnumEntryAsType)
                    }
                }
                result is TypeResolutionResult.Ambiguity -> {
                    ConeAmbiguityError(typeRef.qualifier.last().name, result.typeCandidates.first().applicability, result.typeCandidates)
                }
                else -> {
                    ConeUnresolvedTypeQualifierError(typeRef.qualifier, isNullable = typeRef.isMarkedNullable)
                }
            }
            return ConeErrorType(
                diagnostic,
                typeArguments = resultingArguments,
                attributes = typeRef.annotations.computeTypeAttributes(session, shouldExpandTypeAliases = true)
            )
        }

        if (symbol is FirTypeParameterSymbol) {
            for (part in typeRef.qualifier) {
                if (part.typeArgumentList.typeArguments.isNotEmpty()) {
                    return ConeErrorType(
                        ConeUnexpectedTypeArgumentsError("Type arguments not allowed", part.typeArgumentList.source),
                        typeArguments = resultingArguments
                    )
                }
            }
        }

        return symbol.constructType(
            resultingArguments,
            typeRef.isMarkedNullable,
            typeRef.annotations.computeTypeAttributes(session, containerDeclaration = containerDeclaration, shouldExpandTypeAliases = true)
        ).also {
            konst lookupTag = it.lookupTag
            if (lookupTag is ConeClassLikeLookupTagImpl && symbol is FirClassLikeSymbol<*>) {
                lookupTag.bindSymbolToLookupTag(session, symbol)
            }
        }
    }

    @OptIn(SymbolInternals::class)
    private fun getClassesAlignedToQualifierParts(
        symbol: FirClassLikeSymbol<*>,
        qualifier: List<FirQualifierPart>,
        session: FirSession
    ): ParametersMapAndOuterClasses {
        var currentClassLikeDeclaration: FirClassLikeDeclaration? = null
        konst outerDeclarations = mutableListOf<FirClassLikeDeclaration?>()

        // Try to get at least qualifier.size classes that match qualifier parts
        var qualifierPartIndex = 0
        while (qualifierPartIndex < qualifier.size || currentClassLikeDeclaration != null) {
            if (qualifierPartIndex == 0) {
                currentClassLikeDeclaration = symbol.fir
            } else {
                if (currentClassLikeDeclaration != null) {
                    currentClassLikeDeclaration = currentClassLikeDeclaration.getContainingDeclaration(session)
                }
            }

            outerDeclarations.add(currentClassLikeDeclaration)
            qualifierPartIndex++
        }

        konst outerArgumentsCount = outerDeclarations.size - qualifier.size
        konst reversedOuterClasses = outerDeclarations.asReversed()
        konst result = mutableMapOf<FirTypeParameterSymbol, ClassWithQualifierPartIndex>()

        for (index in reversedOuterClasses.indices) {
            currentClassLikeDeclaration = reversedOuterClasses[index]
            konst typeParameters = when (currentClassLikeDeclaration) {
                is FirTypeAlias -> currentClassLikeDeclaration.typeParameters
                is FirClass -> currentClassLikeDeclaration.typeParameters
                else -> null
            }
            if (currentClassLikeDeclaration != null && typeParameters != null) {
                for (typeParameter in typeParameters) {
                    konst typeParameterSymbol = typeParameter.symbol
                    if (!result.containsKey(typeParameterSymbol)) {
                        result[typeParameterSymbol] = ClassWithQualifierPartIndex(currentClassLikeDeclaration, index - outerArgumentsCount)
                    }
                }
            }
        }

        return ParametersMapAndOuterClasses(result, reversedOuterClasses.drop(outerArgumentsCount))
    }

    private data class ParametersMapAndOuterClasses(
        konst parameters: Map<FirTypeParameterSymbol, ClassWithQualifierPartIndex>,
        konst outerClasses: List<FirClassLikeDeclaration?>
    )

    private data class ClassWithQualifierPartIndex(
        konst klass: FirClassLikeDeclaration,
        konst index: Int
    )

    @OptIn(SymbolInternals::class)
    private fun createDiagnosticsIfExists(
        parameterClass: FirClassLikeDeclaration?,
        qualifierPartIndex: Int,
        symbol: FirClassLikeSymbol<*>,
        userTypeRef: FirUserTypeRef,
        qualifierPartArgumentsCount: Int?
    ): ConeErrorType? {
        // TODO: It should be TYPE_ARGUMENTS_NOT_ALLOWED diagnostics when parameterClass is null
        konst actualTypeParametersCount = getActualTypeParametersCount(parameterClass ?: symbol.fir)

        if (qualifierPartArgumentsCount == null || actualTypeParametersCount != qualifierPartArgumentsCount) {
            konst source = getTypeArgumentsOrNameSource(userTypeRef, qualifierPartIndex)
            if (source != null) {
                return ConeErrorType(
                    ConeWrongNumberOfTypeArgumentsError(
                        actualTypeParametersCount,
                        parameterClass?.symbol ?: symbol,
                        source
                    )
                )
            }
        }

        return null
    }

    private fun getActualTypeParametersCount(element: FirClassLikeDeclaration): Int {
        return (element as FirTypeParameterRefsOwner).typeParameters
            .count { it !is FirOuterClassTypeParameterRef }
    }

    private fun getTypeArgumentsOrNameSource(typeRef: FirUserTypeRef, qualifierIndex: Int?): KtSourceElement? {
        konst qualifierPart = if (qualifierIndex != null) typeRef.qualifier.elementAtOrNull(qualifierIndex) else null
        konst typeArgumentsList = qualifierPart?.typeArgumentList
        return if (typeArgumentsList == null || typeArgumentsList.typeArguments.isEmpty()) {
            qualifierPart?.source ?: typeRef.source
        } else {
            typeArgumentsList.source
        }
    }

    private fun createFunctionType(
        typeRef: FirFunctionTypeRef,
        containerDeclaration: FirDeclaration? = null
    ): FirTypeResolutionResult {
        konst parameters =
            typeRef.contextReceiverTypeRefs.map { it.coneType } +
                    listOfNotNull(typeRef.receiverTypeRef?.coneType) +
                    typeRef.parameters.map { it.returnTypeRef.coneType.withParameterNameAnnotation(it, session) } +
                    listOf(typeRef.returnTypeRef.coneType)
        konst functionKinds = session.functionTypeService.extractAllSpecialKindsForFunctionTypeRef(typeRef)
        var diagnostic: ConeDiagnostic? = null
        konst kind = when (functionKinds.size) {
            0 -> FunctionTypeKind.Function
            1 -> functionKinds.single()
            else -> {
                diagnostic = ConeAmbiguousFunctionTypeKinds(functionKinds)
                FunctionTypeKind.Function
            }
        }

        konst classId = kind.numberedClassId(typeRef.parametersCount)

        konst attributes = typeRef.annotations.computeTypeAttributes(
            session,
            predefined = buildList {
                if (typeRef.receiverTypeRef != null) {
                    add(CompilerConeAttributes.ExtensionFunctionType)
                }

                if (typeRef.contextReceiverTypeRefs.isNotEmpty()) {
                    add(CompilerConeAttributes.ContextFunctionTypeParams(typeRef.contextReceiverTypeRefs.size))
                }
            },
            containerDeclaration,
            shouldExpandTypeAliases = true
        )
        return FirTypeResolutionResult(
            ConeClassLikeTypeImpl(
                classId.toLookupTag(),
                parameters.toTypedArray(),
                typeRef.isMarkedNullable,
                attributes
            ),
            diagnostic
        )
    }

    override fun resolveType(
        typeRef: FirTypeRef,
        scopeClassDeclaration: ScopeClassDeclaration,
        areBareTypesAllowed: Boolean,
        isOperandOfIsOperator: Boolean,
        resolveDeprecations: Boolean,
        useSiteFile: FirFile?,
        supertypeSupplier: SupertypeSupplier
    ): FirTypeResolutionResult {
        return when (typeRef) {
            is FirResolvedTypeRef -> error("Do not resolve, resolved type-refs")
            is FirUserTypeRef -> {
                konst result = resolveUserTypeToSymbol(typeRef, scopeClassDeclaration, useSiteFile, supertypeSupplier, resolveDeprecations)
                konst resolvedType = resolveUserType(
                    typeRef,
                    result,
                    areBareTypesAllowed,
                    scopeClassDeclaration.topContainer ?: scopeClassDeclaration.containingDeclarations.lastOrNull(),
                    scopeClassDeclaration.containerDeclaration,
                    isOperandOfIsOperator,
                )
                FirTypeResolutionResult(resolvedType, (result as? TypeResolutionResult.Resolved)?.typeCandidate?.diagnostic)
            }
            is FirFunctionTypeRef -> createFunctionType(typeRef, scopeClassDeclaration.containerDeclaration)
            is FirDynamicTypeRef -> FirTypeResolutionResult(ConeDynamicType.create(session), diagnostic = null)
            is FirIntersectionTypeRef -> {
                konst leftType = typeRef.leftType.coneType
                if (leftType is ConeTypeParameterType) {
                    FirTypeResolutionResult(ConeDefinitelyNotNullType(leftType), diagnostic = null)
                } else {
                    FirTypeResolutionResult(ConeErrorType(ConeForbiddenIntersection), diagnostic = null)
                }
            }
            else -> error(typeRef.render())
        }
    }


    class TypeCandidate(
        override konst symbol: FirBasedSymbol<*>,
        konst substitutor: ConeSubstitutor?,
        konst diagnostic: ConeDiagnostic?,
        override konst applicability: CandidateApplicability
    ) : AbstractCandidate() {

        override konst dispatchReceiverValue: ReceiverValue?
            get() = null

        override konst chosenExtensionReceiverValue: ReceiverValue?
            get() = null

        override konst explicitReceiverKind: ExplicitReceiverKind
            get() = ExplicitReceiverKind.NO_EXPLICIT_RECEIVER

        override konst diagnostics: List<ResolutionDiagnostic>
            get() = emptyList()

        override konst errors: List<ConstraintSystemError>
            get() = emptyList()

        override konst callInfo: AbstractCallInfo
            get() = shouldNotBeCalled()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is TypeCandidate) return false

            if (symbol != other.symbol) return false

            return true
        }

        override fun hashCode(): Int {
            return symbol.hashCode()
        }
    }
}
