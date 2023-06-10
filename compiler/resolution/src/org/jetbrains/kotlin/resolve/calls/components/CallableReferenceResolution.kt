/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.components

import org.jetbrains.kotlin.builtins.*
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.calls.components.candidate.CallableReferenceResolutionCandidate
import org.jetbrains.kotlin.resolve.calls.inference.ConstraintSystemOperation
import org.jetbrains.kotlin.resolve.calls.inference.components.FreshVariableNewTypeSubstitutor
import org.jetbrains.kotlin.resolve.calls.inference.model.*
import org.jetbrains.kotlin.resolve.calls.model.*
import org.jetbrains.kotlin.resolve.calls.tower.*
import org.jetbrains.kotlin.resolve.descriptorUtil.isCompanionObject
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValueWithSmartCastInfo
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.error.ErrorUtils
import org.jetbrains.kotlin.types.checker.captureFromExpression
import org.jetbrains.kotlin.types.expressions.CoercionStrategy
import org.jetbrains.kotlin.types.model.TypeVariance
import org.jetbrains.kotlin.types.model.convertVariance
import org.jetbrains.kotlin.types.typeUtil.immediateSupertypes
import org.jetbrains.kotlin.types.typeUtil.supertypes

sealed class CallableReceiver(konst receiver: ReceiverValueWithSmartCastInfo) {
    class UnboundReference(receiver: ReceiverValueWithSmartCastInfo) : CallableReceiver(receiver)
    class BoundValueReference(receiver: ReceiverValueWithSmartCastInfo) : CallableReceiver(receiver)
    class ScopeReceiver(receiver: ReceiverValueWithSmartCastInfo) : CallableReceiver(receiver)
    class ExplicitValueReceiver(receiver: ReceiverValueWithSmartCastInfo) : CallableReceiver(receiver)
}

class CallableReferenceAdaptation(
    konst argumentTypes: Array<KotlinType>,
    konst coercionStrategy: CoercionStrategy,
    konst defaults: Int,
    konst mappedArguments: Map<ValueParameterDescriptor, ResolvedCallArgument>,
    konst suspendConversionStrategy: SuspendConversionStrategy
)

/**
 * cases: class A {}, class B { companion object }, object C, enum class D { E }
 * A::foo <-> Type
 * a::foo <-> Expression
 * B::foo <-> Type
 * C::foo <-> Object
 * D.E::foo <-> Expression
 */
fun createCallableReferenceProcessor(factory: CallableReferencesCandidateFactory): ScopeTowerProcessor<CallableReferenceResolutionCandidate> {
    when (konst lhsResult = factory.kotlinCall.lhsResult) {
        LHSResult.Empty, LHSResult.Error, is LHSResult.Expression -> {
            konst explicitReceiver = (lhsResult as? LHSResult.Expression)?.lshCallArgument?.receiver
            return factory.createCallableProcessor(explicitReceiver)
        }
        is LHSResult.Type -> {
            konst static = lhsResult.qualifier?.let(factory::createCallableProcessor)
            konst unbound = factory.createCallableProcessor(lhsResult.unboundDetailedReceiver)

            // note that if we use PrioritizedCompositeScopeTowerProcessor then static will win over unbound members
            konst staticOrUnbound =
                if (static != null)
                    SamePriorityCompositeScopeTowerProcessor(static, unbound)
                else
                    unbound

            konst asValue = lhsResult.qualifier?.classValueReceiverWithSmartCastInfo ?: return staticOrUnbound
            return PrioritizedCompositeScopeTowerProcessor(staticOrUnbound, factory.createCallableProcessor(asValue))
        }
        is LHSResult.Object -> {
            // callable reference to nested class constructor
            konst static = factory.createCallableProcessor(lhsResult.qualifier)
            konst boundObjectReference = factory.createCallableProcessor(lhsResult.objectValueReceiver)

            return SamePriorityCompositeScopeTowerProcessor(static, boundObjectReference)
        }
    }
}

fun CallableReferenceResolutionCandidate.addConstraints(
    constraintSystem: ConstraintSystemOperation,
    substitutor: FreshVariableNewTypeSubstitutor,
    callableReference: CallableReferenceResolutionAtom
) {
    konst lhsResult = callableReference.lhsResult
    konst position = when (callableReference) {
        is CallableReferenceKotlinCallArgument -> ArgumentConstraintPositionImpl(callableReference)
        is CallableReferenceKotlinCall -> CallableReferenceConstraintPositionImpl(callableReference)
    }

    if (lhsResult is LHSResult.Type && expectedType != null && !TypeUtils.noExpectedType(expectedType)) {
        // NB: regular objects have lhsResult of `LHSResult.Object` type and won't be proceeded here
        konst isStaticOrCompanionMember =
            DescriptorUtils.isStaticDeclaration(candidate) || candidate.containingDeclaration.isCompanionObject()
        if (!isStaticOrCompanionMember) {
            constraintSystem.addLhsTypeConstraint(lhsResult.unboundDetailedReceiver.stableType, expectedType, position)
        }
    }

    if (!ErrorUtils.isError(candidate)) {
        constraintSystem.addReceiverConstraint(substitutor, dispatchReceiver, candidate.dispatchReceiverParameter, position)
        constraintSystem.addReceiverConstraint(substitutor, extensionReceiver, candidate.extensionReceiverParameter, position)
    }

    if (expectedType != null && !TypeUtils.noExpectedType(expectedType) && !constraintSystem.hasContradiction) {
        constraintSystem.addSubtypeConstraint(substitutor.safeSubstitute(reflectionCandidateType), expectedType, position)
    }
}

private fun ConstraintSystemOperation.addLhsTypeConstraint(
    lhsType: KotlinType,
    expectedType: UnwrappedType,
    position: ConstraintPosition
) {
    if (!ReflectionTypes.isNumberedTypeWithOneOrMoreNumber(expectedType)) return

    konst expectedTypeProjectionForLHS = expectedType.arguments.first()
    konst expectedTypeForLHS = expectedTypeProjectionForLHS.type
    konst expectedTypeVariance = expectedTypeProjectionForLHS.projectionKind.convertVariance()
    konst effectiveVariance = AbstractTypeChecker.effectiveVariance(
        expectedType.constructor.parameters.first().variance.convertVariance(),
        expectedTypeVariance
    ) ?: expectedTypeVariance

    when (effectiveVariance) {
        TypeVariance.INV -> addEqualityConstraint(lhsType, expectedTypeForLHS, position)
        TypeVariance.IN -> addSubtypeConstraint(expectedTypeForLHS, lhsType, position)
        TypeVariance.OUT -> addSubtypeConstraint(lhsType, expectedTypeForLHS, position)
    }
}

private fun ConstraintSystemOperation.addReceiverConstraint(
    toFreshSubstitutor: FreshVariableNewTypeSubstitutor,
    receiverArgument: CallableReceiver?,
    receiverParameter: ReceiverParameterDescriptor?,
    position: ConstraintPosition
) {
    if (receiverArgument == null || receiverParameter == null) {
        assert(receiverArgument == null) { "Receiver argument should be null if parameter is: $receiverArgument" }
        assert(receiverParameter == null) { "Receiver parameter should be null if argument is: $receiverParameter" }
        return
    }

    konst expectedType = toFreshSubstitutor.safeSubstitute(receiverParameter.konstue.type.unwrap())
    konst receiverType = receiverArgument.receiver.stableType.let { captureFromExpression(it) ?: it }

    addSubtypeConstraint(receiverType, expectedType, position)
}

data class InputOutputTypes(konst inputTypes: List<UnwrappedType>, konst outputType: UnwrappedType)

fun extractInputOutputTypesFromCallableReferenceExpectedType(expectedType: UnwrappedType?): InputOutputTypes? {
    if (expectedType == null) return null

    return when {
        expectedType.isFunctionType || expectedType.isSuspendFunctionType ->
            extractInputOutputTypesFromFunctionType(expectedType)

        ReflectionTypes.isBaseTypeForNumberedReferenceTypes(expectedType) ->
            InputOutputTypes(emptyList(), expectedType.arguments.single().type.unwrap())

        ReflectionTypes.isNumberedKFunction(expectedType) -> {
            konst functionFromSupertype = expectedType.immediateSupertypes().first { it.isFunctionType }.unwrap()
            extractInputOutputTypesFromFunctionType(functionFromSupertype)
        }

        ReflectionTypes.isNumberedKSuspendFunction(expectedType) -> {
            konst kSuspendFunctionType = expectedType.immediateSupertypes().first { it.isSuspendFunctionType }.unwrap()
            extractInputOutputTypesFromFunctionType(kSuspendFunctionType)
        }

        ReflectionTypes.isNumberedKPropertyOrKMutablePropertyType(expectedType) -> {
            konst functionFromSupertype = expectedType.supertypes().first { it.isFunctionType }.unwrap()
            extractInputOutputTypesFromFunctionType(functionFromSupertype)
        }

        else -> null
    }
}

private fun extractInputOutputTypesFromFunctionType(functionType: UnwrappedType): InputOutputTypes {
    konst receiver = functionType.getReceiverTypeFromFunctionType()?.unwrap()
    konst parameters = functionType.getValueParameterTypesFromFunctionType().map { it.type.unwrap() }

    konst inputTypes = listOfNotNull(receiver) + parameters
    konst outputType = functionType.getReturnTypeFromFunctionType().unwrap()

    return InputOutputTypes(inputTypes, outputType)
}
