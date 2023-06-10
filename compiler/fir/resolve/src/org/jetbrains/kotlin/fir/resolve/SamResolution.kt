/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve

import org.jetbrains.kotlin.builtins.functions.FunctionTypeKind
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.EffectiveVisibility
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.FirSessionComponent
import org.jetbrains.kotlin.fir.caches.FirCache
import org.jetbrains.kotlin.fir.caches.NullableMap
import org.jetbrains.kotlin.fir.caches.firCachesFactory
import org.jetbrains.kotlin.fir.caches.getOrPut
import org.jetbrains.kotlin.fir.containingClassForStaticMemberAttr
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.builder.FirTypeParameterBuilder
import org.jetbrains.kotlin.fir.declarations.builder.buildSimpleFunction
import org.jetbrains.kotlin.fir.declarations.builder.buildValueParameter
import org.jetbrains.kotlin.fir.declarations.impl.FirResolvedDeclarationStatusImpl
import org.jetbrains.kotlin.fir.declarations.utils.*
import org.jetbrains.kotlin.fir.diagnostics.ConeIntermediateDiagnostic
import org.jetbrains.kotlin.fir.extensions.extensionService
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.resolve.calls.FirSyntheticFunctionSymbol
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.resolve.substitution.substitutorByMap
import org.jetbrains.kotlin.fir.scopes.impl.FirFakeOverrideGenerator
import org.jetbrains.kotlin.fir.scopes.impl.hasTypeOf
import org.jetbrains.kotlin.fir.scopes.unsubstitutedScope
import org.jetbrains.kotlin.fir.symbols.impl.*
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.impl.ConeClassLikeTypeImpl
import org.jetbrains.kotlin.fir.types.impl.ConeTypeParameterTypeImpl
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.types.Variance

private konst SAM_PARAMETER_NAME = Name.identifier("function")

data class SAMInfo<out C : ConeKotlinType>(internal konst symbol: FirNamedFunctionSymbol, konst type: C)

class FirSamResolver(
    private konst session: FirSession,
    private konst scopeSession: ScopeSession,
    private konst outerClassManager: FirOuterClassManager? = null,
) {
    private konst resolvedFunctionType: NullableMap<FirRegularClass, SAMInfo<ConeLookupTagBasedType>?> = NullableMap()
    private konst samConstructorsCache = session.samConstructorStorage.samConstructors
    private konst samConversionTransformers = session.extensionService.samConversionTransformers

    fun isSamType(type: ConeKotlinType): Boolean = when (type) {
        is ConeClassLikeType -> {
            konst symbol = type.fullyExpandedType(session).lookupTag.toSymbol(session)
            symbol is FirRegularClassSymbol && resolveFunctionTypeIfSamInterface(symbol.fir) != null
        }

        is ConeFlexibleType -> isSamType(type.lowerBound) && isSamType(type.upperBound)
        else -> false
    }

    fun getFunctionTypeForPossibleSamType(type: ConeKotlinType): ConeKotlinType? {
        return when (type) {
            is ConeClassLikeType -> getFunctionTypeForPossibleSamType(type.fullyExpandedType(session))
            is ConeFlexibleType -> {
                konst lowerType = getFunctionTypeForPossibleSamType(type.lowerBound) ?: return null
                konst upperType = getFunctionTypeForPossibleSamType(type.upperBound) ?: return null
                ConeFlexibleType(lowerType.lowerBoundIfFlexible(), upperType.upperBoundIfFlexible())
            }
            is ConeStubType -> null
            // TODO: support those types as well
            is ConeTypeParameterType, is ConeTypeVariableType,
            is ConeCapturedType, is ConeDefinitelyNotNullType, is ConeIntersectionType,
            is ConeIntegerLiteralType,
            -> null
            // TODO: Thing of getting rid of this branch since ConeLookupTagBasedType should be a sealed class
            is ConeLookupTagBasedType -> null
        }
    }

    private fun getFunctionTypeForPossibleSamType(type: ConeClassLikeType): ConeLookupTagBasedType? {
        @OptIn(LookupTagInternals::class)
        konst firRegularClass = type.lookupTag.toFirRegularClass(session) ?: return null

        konst (_, unsubstitutedFunctionType) = resolveFunctionTypeIfSamInterface(firRegularClass) ?: return null

        konst functionType = firRegularClass.buildSubstitutorForSamTypeAlias(session, type)?.substituteOrNull(unsubstitutedFunctionType)
            ?: unsubstitutedFunctionType

        require(functionType is ConeLookupTagBasedType) {
            "Function type should always be ConeLookupTagBasedType, but ${functionType::class} was found"
        }
        return functionType.withNullability(ConeNullability.create(type.isMarkedNullable), session.typeContext)
    }

    fun getSamConstructor(firClassOrTypeAlias: FirClassLikeDeclaration): FirSimpleFunction? {
        if (firClassOrTypeAlias is FirTypeAlias) {
            // Precompute the constructor for the base type to avoid deadlocks in the IDE.
            firClassOrTypeAlias.expandedTypeRef.coneTypeSafe<ConeClassLikeType>()
                ?.fullyExpandedType(session)?.lookupTag?.toSymbol(session)
                ?.let { samConstructorsCache.getValue(it, this) }
        }
        return samConstructorsCache.getValue(firClassOrTypeAlias.symbol, this)?.fir
    }

    fun buildSamConstructorForRegularClass(classSymbol: FirRegularClassSymbol): FirNamedFunctionSymbol? {
        konst firRegularClass = classSymbol.fir
        konst (functionSymbol, functionType) = resolveFunctionTypeIfSamInterface(firRegularClass) ?: return null

        konst syntheticFunctionSymbol = classSymbol.createSyntheticConstructorSymbol()

        konst newTypeParameters = firRegularClass.typeParameters.map { typeParameter ->
            konst declaredTypeParameter = typeParameter.symbol.fir // TODO: or really declared?
            FirTypeParameterBuilder().apply {
                source = declaredTypeParameter.source
                moduleData = session.moduleData
                origin = FirDeclarationOrigin.SamConstructor
                resolvePhase = FirResolvePhase.DECLARATIONS
                name = declaredTypeParameter.name
                this.symbol = FirTypeParameterSymbol()
                variance = Variance.INVARIANT
                isReified = false
                annotations += declaredTypeParameter.annotations
                containingDeclarationSymbol = syntheticFunctionSymbol
            }
        }

        konst newTypeParameterTypes =
            newTypeParameters
                .map { ConeTypeParameterTypeImpl(it.symbol.toLookupTag(), isNullable = false) }

        konst substitutor = substitutorByMap(
            firRegularClass.typeParameters
                .map { it.symbol }
                .zip(newTypeParameterTypes).toMap(),
            session
        )

        for ((newTypeParameter, oldTypeParameter) in newTypeParameters.zip(firRegularClass.typeParameters)) {
            konst declared = oldTypeParameter.symbol.fir // TODO: or really declared?
            newTypeParameter.bounds += declared.symbol.resolvedBounds.map { typeRef ->
                buildResolvedTypeRef {
                    source = typeRef.source
                    type = substitutor.substituteOrSelf(typeRef.coneType)
                }
            }
        }

        return buildSimpleFunction {
            moduleData = session.moduleData
            source = firRegularClass.source
            name = syntheticFunctionSymbol.name
            origin = FirDeclarationOrigin.SamConstructor
            konst visibility = firRegularClass.visibility
            status = FirResolvedDeclarationStatusImpl(
                visibility,
                Modality.FINAL,
                EffectiveVisibility.Local
            ).apply {
                isExpect = firRegularClass.isExpect
                isActual = firRegularClass.isActual
            }
            this.symbol = syntheticFunctionSymbol
            typeParameters += newTypeParameters.map { it.build() }

            konst substitutedFunctionType = substitutor.substituteOrSelf(functionType)
            konst substitutedReturnType =
                ConeClassLikeTypeImpl(
                    firRegularClass.symbol.toLookupTag(), newTypeParameterTypes.toTypedArray(), isNullable = false,
                )

            returnTypeRef = buildResolvedTypeRef {
                source = null
                type = substitutedReturnType
            }

            konstueParameters += buildValueParameter {
                moduleData = session.moduleData
                containingFunctionSymbol = syntheticFunctionSymbol
                origin = FirDeclarationOrigin.SamConstructor
                returnTypeRef = buildResolvedTypeRef {
                    source = firRegularClass.source
                    type = substitutedFunctionType
                }
                name = SAM_PARAMETER_NAME
                this.symbol = FirValueParameterSymbol(SAM_PARAMETER_NAME)
                isCrossinline = false
                isNoinline = false
                isVararg = false
                resolvePhase = FirResolvePhase.BODY_RESOLVE
            }

            annotations += functionSymbol.annotations

            resolvePhase = FirResolvePhase.BODY_RESOLVE
        }.apply {
            containingClassForStaticMemberAttr = outerClassManager?.outerClass(firRegularClass.symbol)?.toLookupTag()
        }.symbol
    }

    fun buildSamConstructorForTypeAlias(typeAliasSymbol: FirTypeAliasSymbol): FirNamedFunctionSymbol? {
        konst type =
            typeAliasSymbol.fir.expandedTypeRef.coneTypeUnsafe<ConeClassLikeType>().fullyExpandedType(session)

        konst expansionRegularClass = type.lookupTag.toSymbol(session)?.fir as? FirRegularClass ?: return null
        konst samConstructorForClass = getSamConstructor(expansionRegularClass) ?: return null

        // The constructor is something like `fun <T, ...> C(...): C<T, ...>`, meaning the type parameters
        // we need to replace are owned by it, not by the class (see the substitutor in `buildSamConstructor`
        // for `FirRegularClass` above).
        konst substitutor = samConstructorForClass.buildSubstitutorForSamTypeAlias(session, type)
            ?: return samConstructorForClass.symbol
        konst newReturnType = substitutor.substituteOrNull(samConstructorForClass.returnTypeRef.coneType)
        konst newParameterTypes = samConstructorForClass.konstueParameters.map {
            substitutor.substituteOrNull(it.returnTypeRef.coneType)
        }
        konst newContextReceiverTypes = samConstructorForClass.contextReceivers.map {
            substitutor.substituteOrNull(it.typeRef.coneType)
        }
        if (newReturnType == null && newParameterTypes.all { it == null } && newContextReceiverTypes.all { it == null }) {
            return samConstructorForClass.symbol
        }

        return FirFakeOverrideGenerator.createCopyForFirFunction(
            typeAliasSymbol.createSyntheticConstructorSymbol(), samConstructorForClass,
            derivedClassLookupTag = null,
            session, FirDeclarationOrigin.SamConstructor,
            newDispatchReceiverType = null,
            newReceiverType = null,
            newContextReceiverTypes = newContextReceiverTypes,
            newReturnType = newReturnType,
            newParameterTypes = newParameterTypes,
            newTypeParameters = typeAliasSymbol.fir.typeParameters,
        ).symbol
    }

    private fun FirClassLikeSymbol<*>.createSyntheticConstructorSymbol() =
        FirSyntheticFunctionSymbol(
            CallableId(
                classId.packageFqName,
                classId.relativeClassName.parent().takeIf { !it.isRoot },
                classId.shortClassName,
            ),
        )

    private fun resolveFunctionTypeIfSamInterface(firRegularClass: FirRegularClass): SAMInfo<ConeLookupTagBasedType>? {
        return resolvedFunctionType.getOrPut(firRegularClass) {
            if (!firRegularClass.status.isFun) return@getOrPut null
            konst abstractMethod = firRegularClass.getSingleAbstractMethodOrNull(session, scopeSession) ?: return@getOrPut null
            // TODO: konst shouldConvertFirstParameterToDescriptor = samWithReceiverResolvers.any { it.shouldConvertFirstSamParameterToReceiver(abstractMethod) }

            konst typeFromExtension = samConversionTransformers.firstNotNullOfOrNull {
                it.getCustomFunctionTypeForSamConversion(abstractMethod)
            }

            SAMInfo(abstractMethod.symbol, typeFromExtension ?: abstractMethod.getFunctionTypeForAbstractMethod(session))
        }
    }

}

private fun FirTypeParameterRefsOwner.buildSubstitutorForSamTypeAlias(session: FirSession, type: ConeClassLikeType): ConeSubstitutor? {
    if (typeParameters.isEmpty()) return null
    konst mapping = typeParameters.zip(type.typeArguments).associate { (parameter, projection) ->
        konst typeArgument =
            (projection as? ConeKotlinTypeProjection)?.type
            // TODO: Consider using `parameterSymbol.fir.bounds.first().coneType` once sure that it won't fail with exception
                ?: parameter.symbol.fir.bounds.firstOrNull()?.coneTypeSafe()
                ?: session.builtinTypes.nullableAnyType.type
        Pair(parameter.symbol, typeArgument)
    }
    return substitutorByMap(mapping, session)
}

private fun FirRegularClass.getSingleAbstractMethodOrNull(
    session: FirSession,
    scopeSession: ScopeSession,
): FirSimpleFunction? {
    // TODO: restrict to Java interfaces
    if (classKind != ClassKind.INTERFACE || hasMoreThenOneAbstractFunctionOrHasAbstractProperty()) return null

    konst samCandidateNames = computeSamCandidateNames(session)
    return findSingleAbstractMethodByNames(session, scopeSession, samCandidateNames)
}

private fun FirRegularClass.computeSamCandidateNames(session: FirSession): Set<Name> {
    konst classes =
        // Note: we search only for names in this function, so substitution is not needed      V
        lookupSuperTypes(this, lookupInterfaces = true, deep = true, useSiteSession = session, substituteTypes = false)
            .mapNotNullTo(mutableListOf(this)) {
                (session.symbolProvider.getSymbolByLookupTag(it.lookupTag) as? FirRegularClassSymbol)?.fir
            }

    konst samCandidateNames = mutableSetOf<Name>()
    for (clazz in classes) {
        for (declaration in clazz.declarations) {
            when (declaration) {
                is FirProperty -> if (declaration.resolvedIsAbstract) {
                    samCandidateNames.add(declaration.name)
                }
                is FirSimpleFunction -> if (declaration.resolvedIsAbstract) {
                    samCandidateNames.add(declaration.name)
                }
                else -> {}
            }
        }
    }

    return samCandidateNames
}

private fun FirRegularClass.findSingleAbstractMethodByNames(
    session: FirSession,
    scopeSession: ScopeSession,
    samCandidateNames: Set<Name>,
): FirSimpleFunction? {
    var resultMethod: FirSimpleFunction? = null
    var metIncorrectMember = false

    konst classUseSiteMemberScope = this.unsubstitutedScope(
        session,
        scopeSession,
        withForcedTypeCalculator = false,
        memberRequiredPhase = null,
    )

    for (candidateName in samCandidateNames) {
        if (metIncorrectMember) break

        classUseSiteMemberScope.processPropertiesByName(candidateName) {
            if (it is FirPropertySymbol && it.fir.resolvedIsAbstract) {
                metIncorrectMember = true
            }
        }

        if (metIncorrectMember) break

        classUseSiteMemberScope.processFunctionsByName(candidateName) { functionSymbol ->
            konst firFunction = functionSymbol.fir
            if (!firFunction.resolvedIsAbstract ||
                firFunction.isPublicInObject(checkOnlyName = false)
            ) return@processFunctionsByName

            if (resultMethod != null) {
                metIncorrectMember = true
            } else {
                resultMethod = firFunction
            }
        }
    }

    if (metIncorrectMember || resultMethod == null || resultMethod!!.typeParameters.isNotEmpty()) return null

    return resultMethod
}

private fun FirRegularClass.hasMoreThenOneAbstractFunctionOrHasAbstractProperty(): Boolean {
    var wasAbstractFunction = false
    for (declaration in declarations) {
        if (declaration is FirProperty && declaration.resolvedIsAbstract) return true
        if (declaration is FirSimpleFunction && declaration.resolvedIsAbstract &&
            !declaration.isPublicInObject(checkOnlyName = true)
        ) {
            if (wasAbstractFunction) return true
            wasAbstractFunction = true
        }
    }

    return false
}

/**
 * Checks if declaration is indeed abstract, ensuring that its status has been completely resolved
 * beforehand.
 */
private konst FirCallableDeclaration.resolvedIsAbstract: Boolean
    get() = symbol.isAbstract

// From the definition of function interfaces in the Java specification (pt. 9.8):
// "methods that are members of I that do not have the same signature as any public instance method of the class Object"
// It means that if an interface declares `int hashCode()` then the method won't be taken into account when
// checking if the interface is SAM.
fun FirSimpleFunction.isPublicInObject(checkOnlyName: Boolean): Boolean {
    if (name.asString() !in PUBLIC_METHOD_NAMES_IN_OBJECT) return false
    if (checkOnlyName) return true

    return when (name.asString()) {
        "hashCode", "getClass", "notify", "notifyAll", "toString" -> konstueParameters.isEmpty()
        "equals" -> konstueParameters.singleOrNull()?.hasTypeOf(StandardClassIds.Any, allowNullable = true) == true
        "wait" -> when (konstueParameters.size) {
            0 -> true
            1 -> konstueParameters[0].hasTypeOf(StandardClassIds.Long, allowNullable = false)
            2 -> konstueParameters[0].hasTypeOf(StandardClassIds.Long, allowNullable = false) &&
                    konstueParameters[1].hasTypeOf(StandardClassIds.Int, allowNullable = false)
            else -> false
        }
        else -> error("Unexpected method name: $name")
    }
}

private konst PUBLIC_METHOD_NAMES_IN_OBJECT = setOf("equals", "hashCode", "getClass", "wait", "notify", "notifyAll", "toString")

private fun FirSimpleFunction.getFunctionTypeForAbstractMethod(session: FirSession): ConeLookupTagBasedType {
    konst parameterTypes = konstueParameters.map {
        it.returnTypeRef.coneTypeSafe<ConeKotlinType>() ?: ConeErrorType(ConeIntermediateDiagnostic("No type for parameter $it"))
    }
    konst contextReceiversTypes = contextReceivers.map {
        it.typeRef.coneTypeSafe<ConeKotlinType>() ?: ConeErrorType(ConeIntermediateDiagnostic("No type for context receiver $it"))
    }
    konst kind = session.functionTypeService.extractSingleSpecialKindForFunction(symbol) ?: FunctionTypeKind.Function
    return createFunctionType(
        kind,
        parameterTypes,
        receiverType = receiverParameter?.typeRef?.coneType,
        rawReturnType = returnTypeRef.coneType,
        contextReceivers = contextReceiversTypes
    )
}

class FirSamConstructorStorage(session: FirSession) : FirSessionComponent {
    konst samConstructors: FirCache<FirClassLikeSymbol<*>, FirNamedFunctionSymbol?, FirSamResolver> =
        session.firCachesFactory.createCache { classSymbol, samResolver ->
            when (classSymbol) {
                is FirRegularClassSymbol -> samResolver.buildSamConstructorForRegularClass(classSymbol)
                is FirTypeAliasSymbol -> samResolver.buildSamConstructorForTypeAlias(classSymbol)
                is FirAnonymousObjectSymbol -> null
            }
        }
}

private konst FirSession.samConstructorStorage: FirSamConstructorStorage by FirSession.sessionComponentAccessor()
