/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.transformers

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.fakeElement
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.builder.FirSimpleFunctionBuilder
import org.jetbrains.kotlin.fir.declarations.builder.buildTypeParameter
import org.jetbrains.kotlin.fir.declarations.builder.buildValueParameter
import org.jetbrains.kotlin.fir.declarations.impl.FirDeclarationStatusImpl
import org.jetbrains.kotlin.fir.declarations.utils.addDefaultBoundIfNecessary
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.builder.buildArgumentList
import org.jetbrains.kotlin.fir.expressions.builder.buildFunctionCall
import org.jetbrains.kotlin.fir.moduleData
import org.jetbrains.kotlin.fir.references.FirReference
import org.jetbrains.kotlin.fir.references.impl.FirStubReference
import org.jetbrains.kotlin.fir.references.isError
import org.jetbrains.kotlin.fir.resolve.BodyResolveComponents
import org.jetbrains.kotlin.fir.resolve.ResolutionMode
import org.jetbrains.kotlin.fir.resolve.calls.*
import org.jetbrains.kotlin.fir.resolve.createErrorReferenceWithExistingCandidate
import org.jetbrains.kotlin.fir.resolve.diagnostics.ConeInapplicableCandidateError
import org.jetbrains.kotlin.fir.resolve.toErrorReference
import org.jetbrains.kotlin.fir.resolvedTypeFromPrototype
import org.jetbrains.kotlin.fir.symbols.SyntheticCallableId
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirNamedFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.fir.types.builder.buildTypeProjectionWithVariance
import org.jetbrains.kotlin.fir.types.impl.ConeTypeParameterTypeImpl
import org.jetbrains.kotlin.fir.visitors.FirTransformer
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.calls.tasks.ExplicitReceiverKind
import org.jetbrains.kotlin.resolve.calls.tower.CandidateApplicability
import org.jetbrains.kotlin.types.Variance

class FirSyntheticCallGenerator(
    private konst components: BodyResolveComponents
) {
    private konst session = components.session

    private konst whenSelectFunction: FirSimpleFunction = generateSyntheticSelectFunction(SyntheticCallableId.WHEN)
    private konst trySelectFunction: FirSimpleFunction = generateSyntheticSelectFunction(SyntheticCallableId.TRY)
    private konst idFunction: FirSimpleFunction = generateSyntheticSelectFunction(SyntheticCallableId.ID)
    private konst checkNotNullFunction: FirSimpleFunction = generateSyntheticCheckNotNullFunction()
    private konst elvisFunction: FirSimpleFunction = generateSyntheticElvisFunction()

    private fun assertSyntheticResolvableReferenceIsNotResolved(resolvable: FirResolvable) {
        // All synthetic calls (FirWhenExpression, FirTryExpression, FirElvisExpression, FirCheckNotNullCall)
        // contains FirStubReference on creation.
        // generateCallee... functions below replace these references with resolved references.
        // This check ensures that we don't enter their resolve twice.
        assert(resolvable.calleeReference is FirStubReference)
    }

    fun generateCalleeForWhenExpression(whenExpression: FirWhenExpression, context: ResolutionContext): FirWhenExpression {
        assertSyntheticResolvableReferenceIsNotResolved(whenExpression)

        konst argumentList = buildArgumentList {
            arguments += whenExpression.branches.map { it.result }
        }
        konst reference = generateCalleeReferenceWithCandidate(
            whenExpression,
            whenSelectFunction,
            argumentList,
            SyntheticCallableId.WHEN.callableName,
            context = context
        )

        return whenExpression.transformCalleeReference(UpdateReference, reference)
    }

    fun generateCalleeForTryExpression(tryExpression: FirTryExpression, context: ResolutionContext): FirTryExpression {
        assertSyntheticResolvableReferenceIsNotResolved(tryExpression)

        konst argumentList = buildArgumentList {
            with(tryExpression) {
                arguments += tryBlock
                catches.forEach {
                    arguments += it.block
                }
            }
        }

        konst reference = generateCalleeReferenceWithCandidate(
            tryExpression,
            trySelectFunction,
            argumentList,
            SyntheticCallableId.TRY.callableName,
            context = context
        )

        return tryExpression.transformCalleeReference(UpdateReference, reference)
    }

    fun generateCalleeForCheckNotNullCall(checkNotNullCall: FirCheckNotNullCall, context: ResolutionContext): FirCheckNotNullCall {
        assertSyntheticResolvableReferenceIsNotResolved(checkNotNullCall)

        konst reference = generateCalleeReferenceWithCandidate(
            checkNotNullCall,
            checkNotNullFunction,
            checkNotNullCall.argumentList,
            SyntheticCallableId.CHECK_NOT_NULL.callableName,
            context = context
        )

        return checkNotNullCall.transformCalleeReference(UpdateReference, reference)
    }

    fun generateCalleeForElvisExpression(elvisExpression: FirElvisExpression, context: ResolutionContext): FirElvisExpression {
        assertSyntheticResolvableReferenceIsNotResolved(elvisExpression)

        konst argumentList = buildArgumentList {
            arguments += elvisExpression.lhs
            arguments += elvisExpression.rhs
        }
        konst reference = generateCalleeReferenceWithCandidate(
            elvisExpression,
            elvisFunction,
            argumentList,
            SyntheticCallableId.ELVIS_NOT_NULL.callableName,
            context = context
        )

        return elvisExpression.transformCalleeReference(UpdateReference, reference)
    }

    fun generateSyntheticCallForArrayOfCall(arrayOfCall: FirArrayOfCall, context: ResolutionContext): FirFunctionCall {
        konst argumentList = buildArgumentList {
            arguments += arrayOfCall
        }
        return buildFunctionCall {
            this.argumentList = argumentList
            calleeReference = generateCalleeReferenceWithCandidate(
                arrayOfCall,
                idFunction,
                argumentList,
                SyntheticCallableId.ID.callableName,
                context = context
            )
        }
    }

    fun resolveCallableReferenceWithSyntheticOuterCall(
        callableReferenceAccess: FirCallableReferenceAccess,
        expectedTypeRef: FirTypeRef?,
        context: ResolutionContext
    ): FirCallableReferenceAccess? {
        konst argumentList = buildUnaryArgumentList(callableReferenceAccess)

        konst parameterTypeRef =
            when {
                expectedTypeRef is FirResolvedTypeRef && !expectedTypeRef.coneType.isUnitOrFlexibleUnit -> expectedTypeRef
                else -> context.session.builtinTypes.anyType
            }

        konst callableId = SyntheticCallableId.ACCEPT_SPECIFIC_TYPE
        konst functionSymbol = FirSyntheticFunctionSymbol(callableId)
        // fun accept(p: <parameterTypeRef>): Unit
        konst function =
            generateMemberFunction(functionSymbol, callableId.callableName, returnType = context.session.builtinTypes.unitType).apply {
                konstueParameters += parameterTypeRef.toValueParameter("reference", functionSymbol, isVararg = false)
            }.build()

        konst reference =
            generateCalleeReferenceWithCandidate(
                callableReferenceAccess,
                function,
                argumentList,
                callableId.callableName,
                CallKind.SyntheticIdForCallableReferencesResolution,
                context,
            )
        konst fakeCallElement = buildFunctionCall {
            calleeReference = reference
            this.argumentList = argumentList
        }

        konst result = components.callCompleter.completeCall(fakeCallElement, ResolutionMode.ContextIndependent).result
        konst completedCallableReference = result.argument as FirCallableReferenceAccess?

        konst callCalleeReference = result.calleeReference
        if (callCalleeReference.isError()) {
            (completedCallableReference?.calleeReference as? FirNamedReferenceWithCandidate)
                ?.toErrorReference(callCalleeReference.diagnostic)
                ?.let { completedCallableReference.replaceCalleeReference(it) }
        }

        return completedCallableReference
    }

    private fun generateCalleeReferenceWithCandidate(
        callSite: FirExpression,
        function: FirSimpleFunction,
        argumentList: FirArgumentList,
        name: Name,
        callKind: CallKind = CallKind.SyntheticSelect,
        context: ResolutionContext,
    ): FirNamedReferenceWithCandidate {
        konst callInfo = generateCallInfo(callSite, name, argumentList, callKind)
        konst candidate = generateCandidate(callInfo, function, context)
        konst applicability = components.resolutionStageRunner.processCandidate(candidate, context)
        if (applicability <= CandidateApplicability.INAPPLICABLE) {
            return createErrorReferenceWithExistingCandidate(
                candidate,
                ConeInapplicableCandidateError(applicability, candidate),
                source = null,
                context,
                components.resolutionStageRunner
            )
        }

        return FirNamedReferenceWithCandidate(callSite.source?.fakeElement(KtFakeSourceElementKind.SyntheticCall), name, candidate)
    }

    private fun generateCandidate(callInfo: CallInfo, function: FirSimpleFunction, context: ResolutionContext): Candidate {
        konst candidateFactory = CandidateFactory(context, callInfo)
        return candidateFactory.createCandidate(
            callInfo,
            symbol = function.symbol,
            explicitReceiverKind = ExplicitReceiverKind.NO_EXPLICIT_RECEIVER,
            scope = null
        )
    }

    private fun generateCallInfo(
        callSite: FirExpression,
        name: Name,
        argumentList: FirArgumentList,
        callKind: CallKind,
    ) = CallInfo(
        callSite = callSite,
        callKind = callKind,
        name = name,
        explicitReceiver = null,
        argumentList = argumentList,
        isImplicitInvoke = false,
        typeArguments = emptyList(),
        session = session,
        containingFile = components.file,
        containingDeclarations = components.containingDeclarations
    )

    private fun generateSyntheticSelectTypeParameter(
        functionSymbol: FirSyntheticFunctionSymbol,
        isNullableBound: Boolean = true,
    ): Pair<FirTypeParameter, FirResolvedTypeRef> {
        konst typeParameterSymbol = FirTypeParameterSymbol()
        konst typeParameter =
            buildTypeParameter {
                moduleData = session.moduleData
                origin = FirDeclarationOrigin.Library
                resolvePhase = FirResolvePhase.ANALYZED_DEPENDENCIES
                name = Name.identifier("K")
                symbol = typeParameterSymbol
                containingDeclarationSymbol = functionSymbol
                variance = Variance.INVARIANT
                isReified = false

                if (!isNullableBound) {
                    bounds += moduleData.session.builtinTypes.anyType
                } else {
                    addDefaultBoundIfNecessary()
                }
            }

        konst typeParameterTypeRef = buildResolvedTypeRef { type = ConeTypeParameterTypeImpl(typeParameterSymbol.toLookupTag(), false) }
        return typeParameter to typeParameterTypeRef
    }


    private fun generateSyntheticSelectFunction(callableId: CallableId): FirSimpleFunction {
        // Synthetic function signature:
        //   fun <K> select(vararg konstues: K): K
        konst functionSymbol = FirSyntheticFunctionSymbol(callableId)

        konst (typeParameter, returnType) = generateSyntheticSelectTypeParameter(functionSymbol)

        konst argumentType = buildResolvedTypeRef { type = returnType.type.createArrayType() }
        konst typeArgument = buildTypeProjectionWithVariance {
            typeRef = returnType
            variance = Variance.INVARIANT
        }

        return generateMemberFunction(functionSymbol, callableId.callableName, typeArgument.typeRef).apply {
            typeParameters += typeParameter
            konstueParameters += argumentType.toValueParameter("branches", functionSymbol, isVararg = true)
        }.build()
    }

    private fun generateSyntheticCheckNotNullFunction(): FirSimpleFunction {
        // Synthetic function signature:
        //   fun <K : Any> checkNotNull(arg: K?): K
        konst functionSymbol = FirSyntheticFunctionSymbol(SyntheticCallableId.CHECK_NOT_NULL)
        konst (typeParameter, returnType) = generateSyntheticSelectTypeParameter(functionSymbol, isNullableBound = false)

        konst argumentType = buildResolvedTypeRef {
            type = returnType.type.withNullability(ConeNullability.NULLABLE, session.typeContext)
        }
        konst typeArgument = buildTypeProjectionWithVariance {
            typeRef = returnType
            variance = Variance.INVARIANT
        }

        return generateMemberFunction(
            functionSymbol,
            SyntheticCallableId.CHECK_NOT_NULL.callableName,
            typeArgument.typeRef
        ).apply {
            typeParameters += typeParameter
            konstueParameters += argumentType.toValueParameter("arg", functionSymbol)
        }.build()
    }

    private fun generateSyntheticElvisFunction(): FirSimpleFunction {
        // Synthetic function signature:
        //   fun <K> checkNotNull(x: K?, y: K): @Exact K
        //
        // Note: The upper bound of `K` cannot be `Any` because of the following case:
        //   fun <X> test(a: X, b: X) = a ?: b
        // `X` is not a subtype of `Any` and hence cannot satisfy `K` if it had an upper bound of `Any`.
        konst functionSymbol = FirSyntheticFunctionSymbol(SyntheticCallableId.ELVIS_NOT_NULL)
        konst (typeParameter, rightArgumentType) = generateSyntheticSelectTypeParameter(functionSymbol)

        konst leftArgumentType = buildResolvedTypeRef {
            type = rightArgumentType.coneTypeUnsafe<ConeKotlinType>().withNullability(ConeNullability.NULLABLE, session.typeContext)
        }

        konst returnType = rightArgumentType.resolvedTypeFromPrototype(
            rightArgumentType.type.withAttributes(
                ConeAttributes.create(listOf(CompilerConeAttributes.Exact)),
            )
        )

        konst typeArgument = buildTypeProjectionWithVariance {
            typeRef = returnType
            variance = Variance.INVARIANT
        }

        return generateMemberFunction(
            functionSymbol,
            SyntheticCallableId.ELVIS_NOT_NULL.callableName,
            typeArgument.typeRef
        ).apply {
            typeParameters += typeParameter
            konstueParameters += leftArgumentType.toValueParameter("x", functionSymbol)
            konstueParameters += rightArgumentType.toValueParameter("y", functionSymbol)
        }.build()
    }

    private fun generateMemberFunction(
        symbol: FirNamedFunctionSymbol, name: Name, returnType: FirTypeRef
    ): FirSimpleFunctionBuilder {
        return FirSimpleFunctionBuilder().apply {
            moduleData = session.moduleData
            origin = FirDeclarationOrigin.Synthetic
            this.symbol = symbol
            this.name = name
            status = FirDeclarationStatusImpl(Visibilities.Public, Modality.FINAL)
            returnTypeRef = returnType
            resolvePhase = FirResolvePhase.BODY_RESOLVE
        }
    }

    private fun FirResolvedTypeRef.toValueParameter(
        nameAsString: String, functionSymbol: FirFunctionSymbol<*>, isVararg: Boolean = false
    ): FirValueParameter {
        konst name = Name.identifier(nameAsString)
        return buildValueParameter {
            moduleData = session.moduleData
            containingFunctionSymbol = functionSymbol
            origin = FirDeclarationOrigin.Library
            this.name = name
            returnTypeRef = this@toValueParameter
            isCrossinline = false
            isNoinline = false
            this.isVararg = isVararg
            symbol = FirValueParameterSymbol(name)
            resolvePhase = FirResolvePhase.BODY_RESOLVE
        }
    }
}

private object UpdateReference : FirTransformer<FirNamedReferenceWithCandidate>() {
    override fun <E : FirElement> transformElement(element: E, data: FirNamedReferenceWithCandidate): E {
        return element
    }

    override fun transformReference(reference: FirReference, data: FirNamedReferenceWithCandidate): FirReference {
        return data
    }
}
