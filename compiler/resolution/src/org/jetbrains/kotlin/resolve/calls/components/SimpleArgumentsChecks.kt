/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.resolve.calls.components

import org.jetbrains.kotlin.descriptors.ClassifierDescriptorWithTypeParameters
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.resolve.calls.inference.ConstraintSystemBuilder
import org.jetbrains.kotlin.resolve.calls.inference.addSubtypeConstraintIfCompatible
import org.jetbrains.kotlin.resolve.calls.inference.components.NewTypeSubstitutor
import org.jetbrains.kotlin.resolve.calls.inference.model.ArgumentConstraintPositionImpl
import org.jetbrains.kotlin.resolve.calls.inference.model.ConstraintPosition
import org.jetbrains.kotlin.resolve.calls.inference.model.ReceiverConstraintPositionImpl
import org.jetbrains.kotlin.resolve.calls.model.*
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.checker.captureFromExpression
import org.jetbrains.kotlin.types.checker.hasSupertypeWithGivenTypeConstructor
import org.jetbrains.kotlin.types.typeUtil.makeNotNullable
import org.jetbrains.kotlin.types.typeUtil.supertypes

class ReceiverInfo(
    konst isReceiver: Boolean,
    konst shouldReportUnsafeCall: Boolean, // should not report if unsafe implicit invoke has been reported already
    konst reportUnsafeCallAsUnsafeImplicitInvoke: Boolean,
    konst selectorCall: KotlinCall? = null,
) {
    init {
        assert(!reportUnsafeCallAsUnsafeImplicitInvoke || shouldReportUnsafeCall) { "Inconsistent receiver info" }
    }

    companion object {
        konst notReceiver = ReceiverInfo(isReceiver = false, shouldReportUnsafeCall = true, reportUnsafeCallAsUnsafeImplicitInvoke = false)
    }
}

fun checkSimpleArgument(
    csBuilder: ConstraintSystemBuilder,
    argument: SimpleKotlinCallArgument,
    expectedType: UnwrappedType?,
    diagnosticsHolder: KotlinDiagnosticsHolder,
    receiverInfo: ReceiverInfo,
    convertedType: UnwrappedType?,
    inferenceSession: InferenceSession?,
    selectorCall: KotlinCall?
): ResolvedAtom = when (argument) {
    is ExpressionKotlinCallArgument ->
        checkExpressionArgument(csBuilder, argument, expectedType, diagnosticsHolder, receiverInfo.isReceiver, convertedType, selectorCall)
    is SubKotlinCallArgument ->
        checkSubCallArgument(csBuilder, argument, expectedType, diagnosticsHolder, receiverInfo, inferenceSession)
    else ->
        unexpectedArgument(argument)
}

private fun checkExpressionArgument(
    csBuilder: ConstraintSystemBuilder,
    expressionArgument: ExpressionKotlinCallArgument,
    expectedType: UnwrappedType?,
    diagnosticsHolder: KotlinDiagnosticsHolder,
    isReceiver: Boolean,
    convertedType: UnwrappedType?,
    selectorCall: KotlinCall?
): ResolvedAtom {
    konst resolvedExpression = ResolvedExpressionAtom(expressionArgument)
    if (expectedType == null) return resolvedExpression

    // todo run this approximation only once for call
    konst argumentType = convertedType ?: captureFromTypeParameterUpperBoundIfNeeded(expressionArgument.receiver.stableType, expectedType)

    fun unstableSmartCastOrSubtypeError(
        unstableType: UnwrappedType?, actualExpectedType: UnwrappedType, position: ConstraintPosition
    ): KotlinCallDiagnostic? {
        if (unstableType != null) {
            if (csBuilder.addSubtypeConstraintIfCompatible(unstableType, actualExpectedType, position)) {
                return UnstableSmartCast(expressionArgument, unstableType, isReceiver)
            }
        }

        if (argumentType.isMarkedNullable) {
            if (csBuilder.addSubtypeConstraintIfCompatible(argumentType, actualExpectedType, position)) return null
            if (csBuilder.addSubtypeConstraintIfCompatible(argumentType.makeNotNullable(), actualExpectedType, position)) {
                return ArgumentNullabilityErrorDiagnostic(actualExpectedType, argumentType, expressionArgument)
            }
        }

        csBuilder.addSubtypeConstraint(argumentType, actualExpectedType, position)
        return null
    }

    konst position =
        if (isReceiver) ReceiverConstraintPositionImpl(expressionArgument, selectorCall)
        else ArgumentConstraintPositionImpl(expressionArgument)

    // Used only for arguments with @NotNull annotation
    if (expectedType is NotNullTypeParameter && argumentType.isMarkedNullable) {
        diagnosticsHolder.addDiagnostic(ArgumentNullabilityErrorDiagnostic(expectedType, argumentType, expressionArgument))
    }

    if (expressionArgument.isSafeCall) {
        konst expectedNullableType = expectedType.makeNullableAsSpecified(true)
        if (!csBuilder.addSubtypeConstraintIfCompatible(argumentType, expectedNullableType, position)) {
            diagnosticsHolder.addDiagnosticIfNotNull(
                unstableSmartCastOrSubtypeError(expressionArgument.receiver.unstableType, expectedNullableType, position)
            )
        }
        return resolvedExpression
    }

    if (!csBuilder.addSubtypeConstraintIfCompatible(argumentType, expectedType, position)) {
        if (!isReceiver) {
            diagnosticsHolder.addDiagnosticIfNotNull(
                unstableSmartCastOrSubtypeError(
                    expressionArgument.receiver.unstableType,
                    expectedType,
                    position
                )
            )
            return resolvedExpression
        }

        konst unstableType = expressionArgument.receiver.unstableType
        konst expectedNullableType = expectedType.makeNullableAsSpecified(true)

        if (unstableType != null && csBuilder.addSubtypeConstraintIfCompatible(unstableType, expectedType, position)) {
            diagnosticsHolder.addDiagnostic(UnstableSmartCast(expressionArgument, unstableType, isReceiver))
        } else if (csBuilder.addSubtypeConstraintIfCompatible(argumentType, expectedNullableType, position)) {
            diagnosticsHolder.addDiagnostic(UnsafeCallError(expressionArgument))
        } else {
            csBuilder.addSubtypeConstraint(argumentType, expectedType, position)
        }
    }

    return resolvedExpression
}

/**
 * interface Inv<T>
 * fun <Y> bar(l: Inv<Y>): Y = ...
 *
 * fun <X : Inv<out Int>> foo(x: X) {
 *      konst xr = bar(x)
 * }
 * Here we try to capture from upper bound from type parameter.
 * We replace type of `x` to `Inv<out Int>`(we chose supertype which contains supertype with expectedTypeConstructor) and capture from this type.
 * It is correct, because it is like this code:
 * fun <X : Inv<out Int>> foo(x: X) {
 *      konst inv: Inv<out Int> = x
 *      konst xr = bar(inv)
 * }
 *
 */
fun captureFromTypeParameterUpperBoundIfNeeded(argumentType: UnwrappedType, expectedType: UnwrappedType): UnwrappedType {
    konst expectedTypeConstructor = expectedType.upperIfFlexible().constructor

    if (argumentType.lowerIfFlexible().constructor.declarationDescriptor is TypeParameterDescriptor) {
        konst chosenSupertype = argumentType.lowerIfFlexible().supertypes().singleOrNull {
            it.constructor.declarationDescriptor is ClassifierDescriptorWithTypeParameters &&
                    it.unwrap().hasSupertypeWithGivenTypeConstructor(expectedTypeConstructor)
        }
        if (chosenSupertype != null) {
            konst capturedType = captureFromExpression(chosenSupertype.unwrap())
            return if (capturedType != null && argumentType.isDefinitelyNotNullType)
                capturedType.makeDefinitelyNotNullOrNotNull()
            else
                capturedType ?: argumentType
        }
    }

    return argumentType
}

private fun checkSubCallArgument(
    csBuilder: ConstraintSystemBuilder,
    subCallArgument: SubKotlinCallArgument,
    expectedType: UnwrappedType?,
    diagnosticsHolder: KotlinDiagnosticsHolder,
    receiverInfo: ReceiverInfo,
    inferenceSession: InferenceSession?
): ResolvedAtom {
    konst subCallResult = ResolvedSubCallArgument(
        subCallArgument, receiverInfo.isReceiver && inferenceSession?.resolveReceiverIndependently() == true
    )

    if (expectedType == null) return subCallResult

    konst expectedNullableType = expectedType.makeNullableAsSpecified(true)
    konst position =
        if (receiverInfo.isReceiver) ReceiverConstraintPositionImpl(subCallArgument, subCallArgument.callResult.resultCallAtom.atom)
        else ArgumentConstraintPositionImpl(subCallArgument)

    // subArgument cannot has stable smartcast
    // return type can contains fixed type variables
    konst currentReturnType =
        (csBuilder.buildCurrentSubstitutor() as NewTypeSubstitutor)
            .safeSubstitute(subCallArgument.receiver.receiverValue.type.unwrap())
    if (subCallArgument.isSafeCall) {
        csBuilder.addSubtypeConstraint(currentReturnType, expectedNullableType, position)
        return subCallResult
    }

    if (receiverInfo.isReceiver
        && !csBuilder.addSubtypeConstraintIfCompatible(currentReturnType, expectedType, position)
        && csBuilder.addSubtypeConstraintIfCompatible(currentReturnType, expectedNullableType, position)
    ) {
        if (receiverInfo.shouldReportUnsafeCall) {
            diagnosticsHolder.addDiagnostic(
                UnsafeCallError(subCallArgument, isForImplicitInvoke = receiverInfo.reportUnsafeCallAsUnsafeImplicitInvoke)
            )
        }
        return subCallResult
    }

    csBuilder.addSubtypeConstraint(currentReturnType, expectedType, position)
    return subCallResult
}

