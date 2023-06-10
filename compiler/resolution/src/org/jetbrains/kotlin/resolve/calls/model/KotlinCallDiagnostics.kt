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

package org.jetbrains.kotlin.resolve.calls.model

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.ReceiverParameterDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.resolve.calls.components.candidate.ResolutionCandidate
import org.jetbrains.kotlin.resolve.calls.components.candidate.CallableReferenceResolutionCandidate
import org.jetbrains.kotlin.resolve.calls.inference.model.ConstraintSystemError
import org.jetbrains.kotlin.resolve.calls.inference.model.NewConstraintError
import org.jetbrains.kotlin.resolve.calls.inference.model.NewConstraintWarning
import org.jetbrains.kotlin.resolve.calls.inference.model.transformToWarning
import org.jetbrains.kotlin.resolve.calls.tower.CandidateApplicability
import org.jetbrains.kotlin.resolve.calls.tower.CandidateApplicability.*
import org.jetbrains.kotlin.types.TypeConstructor
import org.jetbrains.kotlin.types.UnwrappedType
import org.jetbrains.kotlin.types.model.TypeParameterMarker

interface TransformableToWarning<T : KotlinCallDiagnostic> {
    fun transformToWarning(): T?
}

abstract class InapplicableArgumentDiagnostic : KotlinCallDiagnostic(INAPPLICABLE) {
    abstract konst argument: KotlinCallArgument

    override fun report(reporter: DiagnosticReporter) = reporter.onCallArgument(argument, this)
}

abstract class CallableReferenceInapplicableDiagnostic(
    private konst argument: CallableReferenceResolutionAtom,
    applicability: CandidateApplicability = INAPPLICABLE
) : KotlinCallDiagnostic(applicability) {
    override fun report(reporter: DiagnosticReporter) {
        when (argument) {
            is CallableReferenceKotlinCall -> reporter.onCall(this)
            is CallableReferenceKotlinCallArgument -> reporter.onCallArgument(argument, this)
        }
    }
}

// ArgumentsToParameterMapper
class TooManyArguments(konst argument: KotlinCallArgument, konst descriptor: CallableDescriptor) :
    KotlinCallDiagnostic(INAPPLICABLE_ARGUMENTS_MAPPING_ERROR) {
    override fun report(reporter: DiagnosticReporter) = reporter.onCallArgument(argument, this)
}

class NonVarargSpread(konst argument: KotlinCallArgument) : KotlinCallDiagnostic(INAPPLICABLE) {
    override fun report(reporter: DiagnosticReporter) = reporter.onCallArgumentSpread(argument, this)
}

class MultiLambdaBuilderInferenceRestriction(
    konst argument: KotlinCallArgument,
    konst typeParameter: TypeParameterMarker?
) : KotlinCallDiagnostic(RESOLVED) {
    override fun report(reporter: DiagnosticReporter) = reporter.onCallArgument(argument, this)
}

class StubBuilderInferenceReceiver(
    konst receiver: SimpleKotlinCallArgument,
    konst extensionReceiverParameter: ReceiverParameterDescriptor,
) : KotlinCallDiagnostic(RESOLVED) {
    override fun report(reporter: DiagnosticReporter) = reporter.onCallReceiver(receiver, this)
}

class MixingNamedAndPositionArguments(override konst argument: KotlinCallArgument) : InapplicableArgumentDiagnostic()

class NamedArgumentNotAllowed(konst argument: KotlinCallArgument, konst descriptor: CallableDescriptor) : KotlinCallDiagnostic(INAPPLICABLE) {
    override fun report(reporter: DiagnosticReporter) = reporter.onCallArgumentName(argument, this)
}

class NameNotFound(konst argument: KotlinCallArgument, konst descriptor: CallableDescriptor) : KotlinCallDiagnostic(INAPPLICABLE) {
    override fun report(reporter: DiagnosticReporter) = reporter.onCallArgumentName(argument, this)
}

class NoValueForParameter(
    konst parameterDescriptor: ValueParameterDescriptor,
    konst descriptor: CallableDescriptor
) : KotlinCallDiagnostic(INAPPLICABLE_ARGUMENTS_MAPPING_ERROR) {
    override fun report(reporter: DiagnosticReporter) = reporter.onCall(this)
}

class ArgumentPassedTwice(
    konst argument: KotlinCallArgument,
    konst parameterDescriptor: ValueParameterDescriptor,
    konst firstOccurrence: ResolvedCallArgument
) : KotlinCallDiagnostic(INAPPLICABLE) {
    override fun report(reporter: DiagnosticReporter) = reporter.onCallArgumentName(argument, this)
}

class VarargArgumentOutsideParentheses(
    override konst argument: KotlinCallArgument,
    konst parameterDescriptor: ValueParameterDescriptor
) : InapplicableArgumentDiagnostic()

class NameForAmbiguousParameter(
    konst argument: KotlinCallArgument,
    konst parameterDescriptor: ValueParameterDescriptor,
    konst overriddenParameterWithOtherName: ValueParameterDescriptor
) : KotlinCallDiagnostic(CONVENTION_ERROR) {
    override fun report(reporter: DiagnosticReporter) = reporter.onCallArgumentName(argument, this)
}

class NamedArgumentReference(
    konst argument: KotlinCallArgument,
    konst parameterDescriptor: ValueParameterDescriptor
) : KotlinCallDiagnostic(RESOLVED) {
    override fun report(reporter: DiagnosticReporter) = reporter.onCallArgumentName(argument, this)
}

// TypeArgumentsToParameterMapper
class WrongCountOfTypeArguments(
    konst descriptor: CallableDescriptor,
    konst currentCount: Int
) : KotlinCallDiagnostic(INAPPLICABLE) {
    override fun report(reporter: DiagnosticReporter) = reporter.onTypeArguments(this)
}

object TypeCheckerHasRanIntoRecursion : KotlinCallDiagnostic(INAPPLICABLE) {
    override fun report(reporter: DiagnosticReporter) = reporter.onCall(this)
}

// supported by FE but not supported by BE now
class CallableReferencesDefaultArgumentUsed(
    konst argument: CallableReferenceResolutionAtom,
    konst candidate: CallableDescriptor,
    konst defaultsCount: Int
) : CallableReferenceInapplicableDiagnostic(argument)

class NotCallableMemberReference(
    konst argument: CallableReferenceResolutionAtom,
    konst candidate: CallableDescriptor
) : CallableReferenceInapplicableDiagnostic(argument)

class NoneCallableReferenceCallCandidates(konst argument: CallableReferenceKotlinCallArgument) :
    CallableReferenceInapplicableDiagnostic(argument)

class CallableReferenceCallCandidatesAmbiguity(
    konst argument: CallableReferenceKotlinCallArgument,
    konst candidates: Collection<CallableReferenceResolutionCandidate>
) : CallableReferenceInapplicableDiagnostic(argument)

class NotCallableExpectedType(
    konst argument: CallableReferenceKotlinCallArgument,
    konst expectedType: UnwrappedType,
    konst notCallableTypeConstructor: TypeConstructor
) : CallableReferenceInapplicableDiagnostic(argument)

class AdaptedCallableReferenceIsUsedWithReflection(konst argument: CallableReferenceResolutionAtom) :
    CallableReferenceInapplicableDiagnostic(argument, RESOLVED_WITH_ERROR)

// SmartCasts
class SmartCastDiagnostic(
    konst argument: ExpressionKotlinCallArgument,
    konst smartCastType: UnwrappedType,
    konst kotlinCall: KotlinCall?
) : KotlinCallDiagnostic(RESOLVED) {
    override fun report(reporter: DiagnosticReporter) = reporter.onCallArgument(argument, this)
}

sealed class UnstableSmartCast(
    konst argument: ExpressionKotlinCallArgument,
    konst targetType: UnwrappedType,
    applicability: CandidateApplicability,
) : KotlinCallDiagnostic(applicability) {
    override fun report(reporter: DiagnosticReporter) = reporter.onCallArgument(argument, this)

    companion object {
        operator fun invoke(
            argument: ExpressionKotlinCallArgument,
            targetType: UnwrappedType,
            @Suppress("UNUSED_PARAMETER") isReceiver: Boolean = false, // for reproducing OI behaviour
        ): UnstableSmartCast {
            return UnstableSmartCastResolutionError(argument, targetType)
        }
    }
}

class UnstableSmartCastResolutionError(
    argument: ExpressionKotlinCallArgument,
    targetType: UnwrappedType,
) : UnstableSmartCast(argument, targetType, UNSTABLE_SMARTCAST)

class UnstableSmartCastDiagnosticError(
    argument: ExpressionKotlinCallArgument,
    targetType: UnwrappedType,
) : UnstableSmartCast(argument, targetType, RESOLVED_WITH_ERROR)

class UnsafeCallError(
    konst receiver: SimpleKotlinCallArgument,
    konst isForImplicitInvoke: Boolean = false
) : KotlinCallDiagnostic(UNSAFE_CALL) {
    override fun report(reporter: DiagnosticReporter) = reporter.onCallReceiver(receiver, this)
}

// Other
object InstantiationOfAbstractClass : KotlinCallDiagnostic(K1_RUNTIME_ERROR) {
    override fun report(reporter: DiagnosticReporter) = reporter.onCall(this)
}

class AbstractSuperCall(konst receiver: SimpleKotlinCallArgument) : KotlinCallDiagnostic(K1_RUNTIME_ERROR) {
    override fun report(reporter: DiagnosticReporter) {
        reporter.onCall(this)
    }
}

object AbstractFakeOverrideSuperCall : KotlinCallDiagnostic(K1_RUNTIME_ERROR) {
    override fun report(reporter: DiagnosticReporter) {
        reporter.onCall(this)
    }
}

class SuperAsExtensionReceiver(konst receiver: SimpleKotlinCallArgument) : KotlinCallDiagnostic(K1_RUNTIME_ERROR) {
    override fun report(reporter: DiagnosticReporter) {
        reporter.onCallReceiver(receiver, this)
    }
}

// candidates result
class NoneCandidatesCallDiagnostic : KotlinCallDiagnostic(INAPPLICABLE) {
    override fun report(reporter: DiagnosticReporter) {
        reporter.onCall(this)
    }
}

class ManyCandidatesCallDiagnostic(konst candidates: Collection<ResolutionCandidate>) : KotlinCallDiagnostic(INAPPLICABLE) {
    override fun report(reporter: DiagnosticReporter) {
        reporter.onCall(this)
    }
}

class NonApplicableCallForBuilderInferenceDiagnostic(konst kotlinCall: KotlinCall) : KotlinCallDiagnostic(CONVENTION_ERROR) {
    override fun report(reporter: DiagnosticReporter) {
        reporter.onCall(this)
    }
}

sealed interface ArgumentNullabilityMismatchDiagnostic {
    konst expectedType: UnwrappedType
    konst actualType: UnwrappedType
    konst expressionArgument: ExpressionKotlinCallArgument
}

class ArgumentNullabilityErrorDiagnostic(
    override konst expectedType: UnwrappedType,
    override konst actualType: UnwrappedType,
    override konst expressionArgument: ExpressionKotlinCallArgument
) : KotlinCallDiagnostic(UNSAFE_CALL), TransformableToWarning<ArgumentNullabilityWarningDiagnostic>, ArgumentNullabilityMismatchDiagnostic {
    override fun report(reporter: DiagnosticReporter) {
        reporter.onCallArgument(expressionArgument, this)
    }

    override fun transformToWarning() = ArgumentNullabilityWarningDiagnostic(expectedType, actualType, expressionArgument)
}

class ArgumentNullabilityWarningDiagnostic(
    override konst expectedType: UnwrappedType,
    override konst actualType: UnwrappedType,
    override konst expressionArgument: ExpressionKotlinCallArgument
) : KotlinCallDiagnostic(RESOLVED), ArgumentNullabilityMismatchDiagnostic {
    override fun report(reporter: DiagnosticReporter) {
        reporter.onCallArgument(expressionArgument, this)
    }
}

class ResolvedToSamWithVarargDiagnostic(konst argument: KotlinCallArgument) : KotlinCallDiagnostic(K1_RESOLVED_TO_SAM_WITH_VARARG) {
    override fun report(reporter: DiagnosticReporter) {
        reporter.onCallArgument(argument, this)
    }
}

class NotEnoughInformationForLambdaParameter(
    konst lambdaArgument: LambdaKotlinCallArgument,
    konst parameterIndex: Int
) : KotlinCallDiagnostic(RESOLVED_WITH_ERROR) {
    override fun report(reporter: DiagnosticReporter) {
        reporter.onCallArgument(lambdaArgument, this)
    }
}

class CandidateChosenUsingOverloadResolutionByLambdaAnnotation : KotlinCallDiagnostic(RESOLVED) {
    override fun report(reporter: DiagnosticReporter) {
        reporter.onCall(this)
    }
}

class EnumEntryAmbiguityWarning(konst property: PropertyDescriptor, konst enumEntry: ClassDescriptor) : KotlinCallDiagnostic(RESOLVED) {
    override fun report(reporter: DiagnosticReporter) {
        reporter.onCall(this)
    }
}

class CompatibilityWarning(konst candidate: CallableDescriptor) : KotlinCallDiagnostic(RESOLVED) {
    override fun report(reporter: DiagnosticReporter) {
        reporter.onCall(this)
    }
}

class CompatibilityWarningOnArgument(
    konst argument: KotlinCallArgument,
    konst candidate: CallableDescriptor
) : KotlinCallDiagnostic(RESOLVED) {
    override fun report(reporter: DiagnosticReporter) {
        reporter.onCallArgument(argument, this)
    }
}

class NoContextReceiver(konst receiverDescriptor: ReceiverParameterDescriptor) : KotlinCallDiagnostic(INAPPLICABLE) {
    override fun report(reporter: DiagnosticReporter) {
        reporter.onCall(this)
    }
}

class MultipleArgumentsApplicableForContextReceiver(konst receiverDescriptor: ReceiverParameterDescriptor) : KotlinCallDiagnostic(INAPPLICABLE) {
    override fun report(reporter: DiagnosticReporter) {
        reporter.onCall(this)
    }
}

class KotlinConstraintSystemDiagnostic(
    konst error: ConstraintSystemError
) : KotlinCallDiagnostic(error.applicability), TransformableToWarning<KotlinConstraintSystemDiagnostic> {
    override fun report(reporter: DiagnosticReporter) = reporter.constraintError(error)

    override fun transformToWarning(): KotlinConstraintSystemDiagnostic? =
        if (error is NewConstraintError) KotlinConstraintSystemDiagnostic(error.transformToWarning()) else null
}

konst KotlinCallDiagnostic.constraintSystemError: ConstraintSystemError?
    get() = (this as? KotlinConstraintSystemDiagnostic)?.error

fun ConstraintSystemError.asDiagnostic(): KotlinConstraintSystemDiagnostic = KotlinConstraintSystemDiagnostic(this)
fun Collection<ConstraintSystemError>.asDiagnostics(): List<KotlinConstraintSystemDiagnostic> = map(ConstraintSystemError::asDiagnostic)

fun List<KotlinCallDiagnostic>.filterErrorDiagnostics() =
    filter { it !is KotlinConstraintSystemDiagnostic || it.error !is NewConstraintWarning }
