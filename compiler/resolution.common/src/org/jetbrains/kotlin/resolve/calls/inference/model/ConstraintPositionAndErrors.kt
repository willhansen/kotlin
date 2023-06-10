/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.inference.model

import org.jetbrains.kotlin.resolve.calls.tower.CandidateApplicability
import org.jetbrains.kotlin.resolve.calls.tower.CandidateApplicability.*
import org.jetbrains.kotlin.types.EmptyIntersectionTypeKind
import org.jetbrains.kotlin.types.model.KotlinTypeMarker
import org.jetbrains.kotlin.types.model.TypeVariableMarker

interface OnlyInputTypeConstraintPosition

sealed class ConstraintPosition

abstract class ExplicitTypeParameterConstraintPosition<T>(konst typeArgument: T) : ConstraintPosition(), OnlyInputTypeConstraintPosition {
    override fun toString(): String = "TypeParameter $typeArgument"
}

abstract class InjectedAnotherStubTypeConstraintPosition<T>(private konst builderInferenceLambdaOfInjectedStubType: T) : ConstraintPosition(),
    OnlyInputTypeConstraintPosition {
    override fun toString(): String = "Injected from $builderInferenceLambdaOfInjectedStubType builder inference call"
}

abstract class BuilderInferenceSubstitutionConstraintPosition<L>(
    private konst builderInferenceLambda: L,
    konst initialConstraint: InitialConstraint,
    konst isFromNotSubstitutedDeclaredUpperBound: Boolean = false
) : ConstraintPosition(), OnlyInputTypeConstraintPosition {
    override fun toString(): String = "Incorporated builder inference constraint $initialConstraint " +
            "into $builderInferenceLambda call"
}

abstract class ExpectedTypeConstraintPosition<T>(konst topLevelCall: T) : ConstraintPosition(), OnlyInputTypeConstraintPosition {
    override fun toString(): String = "ExpectedType for call $topLevelCall"
}

abstract class DeclaredUpperBoundConstraintPosition<T>(konst typeParameter: T) : ConstraintPosition() {
    override fun toString(): String = "DeclaredUpperBound $typeParameter"
}

abstract class ArgumentConstraintPosition<out T>(konst argument: T) : ConstraintPosition(), OnlyInputTypeConstraintPosition {
    override fun toString(): String = "Argument $argument"
}

abstract class CallableReferenceConstraintPosition<out T>(konst call: T) : ConstraintPosition(), OnlyInputTypeConstraintPosition {
    override fun toString(): String = "Callable reference $call"
}

abstract class ReceiverConstraintPosition<T>(konst argument: T) : ConstraintPosition(), OnlyInputTypeConstraintPosition {
    override fun toString(): String = "Receiver $argument"
}

abstract class FixVariableConstraintPosition<T>(konst variable: TypeVariableMarker, konst resolvedAtom: T) : ConstraintPosition() {
    override fun toString(): String = "Fix variable $variable"
}

abstract class KnownTypeParameterConstraintPosition<T : KotlinTypeMarker>(konst typeArgument: T) : ConstraintPosition() {
    override fun toString(): String = "TypeArgument $typeArgument"
}

abstract class LHSArgumentConstraintPosition<T, R>(
    konst argument: T,
    konst receiver: R
) : ConstraintPosition() {
    override fun toString(): String {
        return "LHS receiver $receiver"
    }
}

abstract class LambdaArgumentConstraintPosition<T>(konst lambda: T) : ConstraintPosition() {
    override fun toString(): String {
        return "LambdaArgument $lambda"
    }
}

open class DelegatedPropertyConstraintPosition<T>(konst topLevelCall: T) : ConstraintPosition() {
    override fun toString(): String = "Constraint from call $topLevelCall for delegated property"
}

data class IncorporationConstraintPosition(
    konst initialConstraint: InitialConstraint,
    var isFromDeclaredUpperBound: Boolean = false
) : ConstraintPosition() {
    konst from: ConstraintPosition get() = initialConstraint.position

    override fun toString(): String = "Incorporate $initialConstraint from position $from"
}

object BuilderInferencePosition : ConstraintPosition() {
    override fun toString(): String = "For builder inference call"
}

// TODO: should be used only in SimpleConstraintSystemImpl
object SimpleConstraintSystemConstraintPosition : ConstraintPosition()

// ------------------------------------------------ Errors ------------------------------------------------

sealed class ConstraintSystemError(konst applicability: CandidateApplicability)

sealed interface NewConstraintMismatch {
    konst lowerType: KotlinTypeMarker
    konst upperType: KotlinTypeMarker
    konst position: IncorporationConstraintPosition
}

class NewConstraintError(
    override konst lowerType: KotlinTypeMarker,
    override konst upperType: KotlinTypeMarker,
    override konst position: IncorporationConstraintPosition,
) : ConstraintSystemError(if (position.from is ReceiverConstraintPosition<*>) INAPPLICABLE_WRONG_RECEIVER else INAPPLICABLE),
    NewConstraintMismatch {
    override fun toString(): String {
        return "$lowerType <: $upperType"
    }
}

class NewConstraintWarning(
    override konst lowerType: KotlinTypeMarker,
    override konst upperType: KotlinTypeMarker,
    override konst position: IncorporationConstraintPosition,
) : ConstraintSystemError(RESOLVED), NewConstraintMismatch

class CapturedTypeFromSubtyping(
    konst typeVariable: TypeVariableMarker,
    konst constraintType: KotlinTypeMarker,
    konst position: ConstraintPosition
) : ConstraintSystemError(INAPPLICABLE)

open class NotEnoughInformationForTypeParameter<T>(
    konst typeVariable: TypeVariableMarker,
    konst resolvedAtom: T,
    konst couldBeResolvedWithUnrestrictedBuilderInference: Boolean
) : ConstraintSystemError(INAPPLICABLE)

class InferredIntoDeclaredUpperBounds(konst typeVariable: TypeVariableMarker) : ConstraintSystemError(RESOLVED)

class ConstrainingTypeIsError(
    konst typeVariable: TypeVariableMarker,
    konst constraintType: KotlinTypeMarker,
    konst position: IncorporationConstraintPosition
) : ConstraintSystemError(INAPPLICABLE)

class NoSuccessfulFork(konst position: IncorporationConstraintPosition) : ConstraintSystemError(INAPPLICABLE)

sealed interface InferredEmptyIntersection {
    konst incompatibleTypes: List<KotlinTypeMarker>
    konst causingTypes: List<KotlinTypeMarker>
    konst typeVariable: TypeVariableMarker
    konst kind: EmptyIntersectionTypeKind
}

class InferredEmptyIntersectionWarning(
    override konst incompatibleTypes: List<KotlinTypeMarker>,
    override konst causingTypes: List<KotlinTypeMarker>,
    override konst typeVariable: TypeVariableMarker,
    override konst kind: EmptyIntersectionTypeKind,
) : ConstraintSystemError(RESOLVED), InferredEmptyIntersection

class InferredEmptyIntersectionError(
    override konst incompatibleTypes: List<KotlinTypeMarker>,
    override konst causingTypes: List<KotlinTypeMarker>,
    override konst typeVariable: TypeVariableMarker,
    override konst kind: EmptyIntersectionTypeKind,
) : ConstraintSystemError(INAPPLICABLE), InferredEmptyIntersection

class OnlyInputTypesDiagnostic(konst typeVariable: TypeVariableMarker) : ConstraintSystemError(INAPPLICABLE)

class LowerPriorityToPreserveCompatibility(konst needToReportWarning: Boolean) :
    ConstraintSystemError(RESOLVED_NEED_PRESERVE_COMPATIBILITY)

fun Constraint.isExpectedTypePosition() =
    position.from is ExpectedTypeConstraintPosition<*> || position.from is DelegatedPropertyConstraintPosition<*>

fun NewConstraintError.transformToWarning() = NewConstraintWarning(lowerType, upperType, position)
