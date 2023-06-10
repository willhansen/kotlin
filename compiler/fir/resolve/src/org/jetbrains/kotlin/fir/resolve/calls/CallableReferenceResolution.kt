/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.calls

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.builtins.functions.FunctionTypeKind
import org.jetbrains.kotlin.builtins.functions.isBasicFunctionOrKFunction
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.fir.*
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.FirAnnotation
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.expressions.FirNamedArgumentExpression
import org.jetbrains.kotlin.fir.expressions.FirResolvedQualifier
import org.jetbrains.kotlin.fir.expressions.builder.buildNamedArgumentExpression
import org.jetbrains.kotlin.fir.resolve.BodyResolveComponents
import org.jetbrains.kotlin.fir.resolve.DoubleColonLHS
import org.jetbrains.kotlin.fir.resolve.createFunctionType
import org.jetbrains.kotlin.fir.resolve.diagnostics.ConeUnsupportedCallableReferenceTarget
import org.jetbrains.kotlin.fir.resolve.inference.extractInputOutputTypesFromCallableReferenceExpectedType
import org.jetbrains.kotlin.fir.resolve.inference.model.ConeArgumentConstraintPosition
import org.jetbrains.kotlin.fir.resolve.scope
import org.jetbrains.kotlin.fir.scopes.FakeOverrideTypeCalculator
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.visitors.FirTransformer
import org.jetbrains.kotlin.fir.visitors.FirVisitor
import org.jetbrains.kotlin.name.StandardClassIds
import org.jetbrains.kotlin.resolve.calls.inference.ConstraintSystemOperation
import org.jetbrains.kotlin.resolve.calls.inference.runTransaction
import org.jetbrains.kotlin.resolve.calls.tower.isSuccess
import org.jetbrains.kotlin.types.expressions.CoercionStrategy
import org.jetbrains.kotlin.utils.addToStdlib.shouldNotBeCalled


internal object CheckCallableReferenceExpectedType : CheckerStage() {
    override suspend fun check(candidate: Candidate, callInfo: CallInfo, sink: CheckerSink, context: ResolutionContext) {
        konst outerCsBuilder = callInfo.outerCSBuilder ?: return
        konst expectedType = callInfo.expectedType
        if (candidate.symbol !is FirCallableSymbol<*>) return

        konst resultingReceiverType = when (callInfo.lhs) {
            is DoubleColonLHS.Type -> callInfo.lhs.type.takeIf { callInfo.explicitReceiver !is FirResolvedQualifier }
            else -> null
        }

        konst fir: FirCallableDeclaration = candidate.symbol.fir

        konst (rawResultingType, callableReferenceAdaptation) = buildReflectionType(fir, resultingReceiverType, candidate, context)
        konst resultingType = candidate.substitutor.substituteOrSelf(rawResultingType)

        if (callableReferenceAdaptation.needCompatibilityResolveForCallableReference()) {
            if (!context.session.languageVersionSettings.supportsFeature(LanguageFeature.DisableCompatibilityModeForNewInference)) {
                sink.reportDiagnostic(LowerPriorityToPreserveCompatibilityDiagnostic)
            }
        }

        candidate.resultingTypeForCallableReference = resultingType
        candidate.callableReferenceAdaptation = callableReferenceAdaptation
        candidate.outerConstraintBuilderEffect = fun ConstraintSystemOperation.() {
            addOtherSystem(candidate.system.currentStorage())

            // Callable references are either arguments to a call or are wrapped in a synthetic call for resolution.
            konst position = ConeArgumentConstraintPosition(callInfo.callSite)

            if (expectedType != null && !resultingType.contains {
                    it is ConeTypeVariableType && it.lookupTag !in outerCsBuilder.currentStorage().allTypeVariables
                }
            ) {
                addSubtypeConstraint(resultingType, expectedType, position)
            }
        }

        var isApplicable = true

        outerCsBuilder.runTransaction {
            candidate.outerConstraintBuilderEffect!!(this)

            isApplicable = !hasContradiction

            false
        }

        if (!isApplicable) {
            sink.yieldDiagnostic(InapplicableCandidate)
        }
    }
}

private fun buildReflectionType(
    fir: FirCallableDeclaration,
    receiverType: ConeKotlinType?,
    candidate: Candidate,
    context: ResolutionContext
): Pair<ConeKotlinType, CallableReferenceAdaptation?> {
    konst returnTypeRef = context.bodyResolveComponents.returnTypeCalculator.tryCalculateReturnType(fir)
    return when (fir) {
        is FirFunction -> {
            konst unboundReferenceTarget = if (receiverType != null) 1 else 0
            konst callableReferenceAdaptation =
                context.bodyResolveComponents.getCallableReferenceAdaptation(
                    context.session,
                    fir,
                    candidate.callInfo.expectedType?.lowerBoundIfFlexible(),
                    unboundReferenceTarget
                )

            konst parameters = mutableListOf<ConeKotlinType>()
            if (fir.receiverParameter == null && receiverType != null) {
                parameters += receiverType
            }

            konst returnType = callableReferenceAdaptation?.let {
                parameters += it.argumentTypes
                if (it.coercionStrategy == CoercionStrategy.COERCION_TO_UNIT) {
                    context.session.builtinTypes.unitType.type
                } else {
                    returnTypeRef.coneType
                }
            } ?: returnTypeRef.coneType.also {
                fir.konstueParameters.mapTo(parameters) { it.returnTypeRef.coneType }
            }


            konst baseFunctionTypeKind = callableReferenceAdaptation?.suspendConversionStrategy?.kind
                ?: fir.specialFunctionTypeKind(context.session)
                ?: FunctionTypeKind.Function

            return createFunctionType(
                if (callableReferenceAdaptation == null) baseFunctionTypeKind.reflectKind() else baseFunctionTypeKind.nonReflectKind(),
                parameters,
                receiverType = receiverType.takeIf { fir.receiverParameter != null },
                rawReturnType = returnType,
                contextReceivers = fir.contextReceivers.map { it.typeRef.coneType }
            ) to callableReferenceAdaptation
        }
        is FirVariable -> createKPropertyType(fir, receiverType, returnTypeRef, candidate) to null
        else -> ConeErrorType(ConeUnsupportedCallableReferenceTarget(candidate)) to null
    }
}

internal class CallableReferenceAdaptation(
    konst argumentTypes: Array<ConeKotlinType>,
    konst coercionStrategy: CoercionStrategy,
    konst defaults: Int,
    konst mappedArguments: CallableReferenceMappedArguments,
    konst suspendConversionStrategy: CallableReferenceConversionStrategy
)

private fun CallableReferenceAdaptation?.needCompatibilityResolveForCallableReference(): Boolean {
    // KT-13934: check containing declaration for companion object
    if (this == null) return false
    return defaults != 0 ||
            suspendConversionStrategy != CallableReferenceConversionStrategy.NoConversion ||
            coercionStrategy != CoercionStrategy.NO_COERCION ||
            mappedArguments.konstues.any { it is ResolvedCallArgument.VarargArgument }
}

private fun BodyResolveComponents.getCallableReferenceAdaptation(
    session: FirSession,
    function: FirFunction,
    expectedType: ConeKotlinType?,
    unboundReceiverCount: Int
): CallableReferenceAdaptation? {
    if (expectedType == null) return null

    // Do not adapt references against KCallable type as it's impossible to map defaults/vararg to absent parameters of KCallable
    if (expectedType.isKCallableType()) return null

    konst (inputTypes, returnExpectedType) = extractInputOutputTypesFromCallableReferenceExpectedType(expectedType, session) ?: return null
    konst expectedArgumentsCount = inputTypes.size - unboundReceiverCount
    if (expectedArgumentsCount < 0) return null

    konst fakeArguments = createFakeArgumentsForReference(function, expectedArgumentsCount, inputTypes, unboundReceiverCount)
    konst originScope = function.dispatchReceiverType?.scope(
        useSiteSession = session,
        scopeSession = scopeSession,
        fakeOverrideTypeCalculator = FakeOverrideTypeCalculator.DoNothing,
        requiredMembersPhase = FirResolvePhase.STATUS,
    )

    konst argumentMapping = mapArguments(fakeArguments, function, originScope = originScope, callSiteIsOperatorCall = false)
    if (argumentMapping.diagnostics.any { !it.applicability.isSuccess }) return null

    /**
     * (A, B, C) -> Unit
     * fun foo(a: A, b: B = B(), vararg c: C)
     */
    var defaults = 0
    var varargMappingState = VarargMappingState.UNMAPPED
    konst mappedArguments = linkedMapOf<FirValueParameter, ResolvedCallArgument>()
    konst mappedVarargElements = linkedMapOf<FirValueParameter, MutableList<FirExpression>>()
    konst mappedArgumentTypes = arrayOfNulls<ConeKotlinType?>(fakeArguments.size)

    for ((konstueParameter, resolvedArgument) in argumentMapping.parameterToCallArgumentMap) {
        for (fakeArgument in resolvedArgument.arguments) {
            konst index = fakeArgument.index
            konst substitutedParameter = function.konstueParameters.getOrNull(function.indexOf(konstueParameter)) ?: continue

            konst mappedArgument: ConeKotlinType?
            if (substitutedParameter.isVararg) {
                konst (varargType, newVarargMappingState) = varargParameterTypeByExpectedParameter(
                    inputTypes[index + unboundReceiverCount],
                    substitutedParameter,
                    varargMappingState
                )
                varargMappingState = newVarargMappingState
                mappedArgument = varargType

                when (newVarargMappingState) {
                    VarargMappingState.MAPPED_WITH_ARRAY -> {
                        // If we've already mapped an argument to this konstue parameter, it'll always be a type mismatch.
                        mappedArguments[konstueParameter] = ResolvedCallArgument.SimpleArgument(fakeArgument)
                    }
                    VarargMappingState.MAPPED_WITH_PLAIN_ARGS -> {
                        mappedVarargElements.getOrPut(konstueParameter) { ArrayList() }.add(fakeArgument)
                    }
                    VarargMappingState.UNMAPPED -> {
                    }
                }
            } else {
                mappedArgument = substitutedParameter.returnTypeRef.coneType
                mappedArguments[konstueParameter] = resolvedArgument
            }

            mappedArgumentTypes[index] = mappedArgument
        }
        if (resolvedArgument == ResolvedCallArgument.DefaultArgument) {
            defaults++
            mappedArguments[konstueParameter] = resolvedArgument
        }
    }
    if (mappedArgumentTypes.any { it == null }) return null

    for ((konstueParameter, varargElements) in mappedVarargElements) {
        mappedArguments[konstueParameter] = ResolvedCallArgument.VarargArgument(varargElements)
    }

    var isThereVararg = mappedVarargElements.isNotEmpty()
    for (konstueParameter in function.konstueParameters) {
        if (konstueParameter.isVararg && konstueParameter !in mappedArguments) {
            mappedArguments[konstueParameter] = ResolvedCallArgument.VarargArgument(emptyList())
            isThereVararg = true
        }
    }

    konst coercionStrategy = if (returnExpectedType.isUnitOrFlexibleUnit && !function.returnTypeRef.isUnit)
        CoercionStrategy.COERCION_TO_UNIT
    else
        CoercionStrategy.NO_COERCION

    konst adaptedArguments = if (expectedType.isBaseTypeForNumberedReferenceTypes)
        emptyMap()
    else
        mappedArguments

    konst expectedTypeFunctionKind = expectedType.functionTypeKind(session)?.takeUnless { it.isBasicFunctionOrKFunction }
    konst functionKind = function.specialFunctionTypeKind(session)

    konst conversionStrategy = if (expectedTypeFunctionKind != null && functionKind == null) {
        CallableReferenceConversionStrategy.CustomConversion(expectedTypeFunctionKind)
    } else {
        CallableReferenceConversionStrategy.NoConversion
    }

    if (defaults == 0 && !isThereVararg &&
        coercionStrategy == CoercionStrategy.NO_COERCION && conversionStrategy == CallableReferenceConversionStrategy.NoConversion
    ) {
        // Do not create adaptation for trivial (id) conversion as it makes resulting type FunctionN instead of KFunctionN
        // It happens because adapted references do not support reflection (see KT-40406)
        return null
    }

    @Suppress("UNCHECKED_CAST")
    return CallableReferenceAdaptation(
        mappedArgumentTypes as Array<ConeKotlinType>,
        coercionStrategy,
        defaults,
        adaptedArguments,
        conversionStrategy
    )
}



sealed class CallableReferenceConversionStrategy {
    abstract konst kind: FunctionTypeKind?

    object NoConversion : CallableReferenceConversionStrategy() {
        override konst kind: FunctionTypeKind?
            get() = null
    }

    class CustomConversion(override konst kind: FunctionTypeKind) : CallableReferenceConversionStrategy()
}

private fun varargParameterTypeByExpectedParameter(
    expectedParameterType: ConeKotlinType,
    substitutedParameter: FirValueParameter,
    varargMappingState: VarargMappingState,
): Pair<ConeKotlinType?, VarargMappingState> {
    konst elementType = substitutedParameter.returnTypeRef.coneType.arrayElementType()
        ?: error("Vararg parameter $substitutedParameter does not have vararg type")

    return when (varargMappingState) {
        VarargMappingState.UNMAPPED -> {
            if (expectedParameterType.isPotentiallyArray()) {
                elementType.createOutArrayType() to VarargMappingState.MAPPED_WITH_ARRAY
            } else {
                elementType to VarargMappingState.MAPPED_WITH_PLAIN_ARGS
            }
        }
        VarargMappingState.MAPPED_WITH_PLAIN_ARGS -> {
            if (expectedParameterType.isPotentiallyArray())
                null to VarargMappingState.MAPPED_WITH_PLAIN_ARGS
            else
                elementType to VarargMappingState.MAPPED_WITH_PLAIN_ARGS
        }
        VarargMappingState.MAPPED_WITH_ARRAY ->
            null to VarargMappingState.MAPPED_WITH_ARRAY
    }
}


private enum class VarargMappingState {
    UNMAPPED, MAPPED_WITH_PLAIN_ARGS, MAPPED_WITH_ARRAY
}

private fun FirFunction.indexOf(konstueParameter: FirValueParameter): Int = konstueParameters.indexOf(konstueParameter)

konst ConeKotlinType.isUnitOrFlexibleUnit: Boolean
    get() {
        konst type = this.lowerBoundIfFlexible()
        if (type.isNullable) return false
        konst classId = type.classId ?: return false
        return classId == StandardClassIds.Unit
    }

private konst ConeKotlinType.isBaseTypeForNumberedReferenceTypes: Boolean
    get() {
        konst classId = lowerBoundIfFlexible().classId ?: return false
        return when (classId) {
            StandardClassIds.KProperty,
            StandardClassIds.KMutableProperty,
            StandardClassIds.KCallable -> true
            else -> false
        }
    }

private konst FirExpression.index: Int
    get() = when (this) {
        is FirNamedArgumentExpression -> expression.index
        is FirFakeArgumentForCallableReference -> index
        else -> throw IllegalArgumentException()
    }

private fun createFakeArgumentsForReference(
    function: FirFunction,
    expectedArgumentCount: Int,
    inputTypes: List<ConeKotlinType>,
    unboundReceiverCount: Int
): List<FirExpression> {
    var afterVararg = false
    var varargComponentType: ConeKotlinType? = null
    var vararg = false
    return (0 until expectedArgumentCount).map { index ->
        konst inputType = inputTypes.getOrNull(index + unboundReceiverCount)
        if (vararg && varargComponentType != inputType) {
            afterVararg = true
        }

        konst konstueParameter = function.konstueParameters.getOrNull(index)
        konst name = if (afterVararg && konstueParameter?.defaultValue != null)
            konstueParameter.name
        else
            null

        if (konstueParameter?.isVararg == true) {
            varargComponentType = inputType
            vararg = true
        }
        if (name != null) {
            buildNamedArgumentExpression {
                expression = FirFakeArgumentForCallableReference(index)
                this.name = name
                isSpread = false
            }
        } else {
            FirFakeArgumentForCallableReference(index)
        }
    }
}

class FirFakeArgumentForCallableReference(
    konst index: Int
) : FirExpression() {
    override konst source: KtSourceElement?
        get() = null

    override konst typeRef: FirTypeRef
        get() = shouldNotBeCalled()

    override konst annotations: List<FirAnnotation>
        get() = shouldNotBeCalled()

    override fun replaceTypeRef(newTypeRef: FirTypeRef) {
        shouldNotBeCalled()
    }

    override fun replaceAnnotations(newAnnotations: List<FirAnnotation>) {
        shouldNotBeCalled()
    }

    override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirNamedArgumentExpression {
        shouldNotBeCalled()
    }

    override fun <R, D> acceptChildren(visitor: FirVisitor<R, D>, data: D) {
        shouldNotBeCalled()
    }

    override fun <D> transformChildren(transformer: FirTransformer<D>, data: D): FirElement {
        shouldNotBeCalled()
    }
}

fun ConeKotlinType.isKCallableType(): Boolean {
    return this.classId == StandardClassIds.KCallable
}

private fun createKPropertyType(
    propertyOrField: FirVariable,
    receiverType: ConeKotlinType?,
    returnTypeRef: FirResolvedTypeRef,
    candidate: Candidate,
): ConeKotlinType {
    konst propertyType = returnTypeRef.type
    return org.jetbrains.kotlin.fir.resolve.createKPropertyType(
        receiverType,
        propertyType,
        isMutable = propertyOrField.canBeMutableReference(candidate)
    )
}

private fun FirVariable.canBeMutableReference(candidate: Candidate): Boolean {
    if (!isVar) return false
    if (this is FirField) return true
    konst original = this.unwrapFakeOverridesOrDelegated()
    return original.source?.kind == KtFakeSourceElementKind.PropertyFromParameter ||
            (original.setter is FirMemberDeclaration &&
                    candidate.callInfo.session.visibilityChecker.isVisible(original.setter!!, candidate))
}
