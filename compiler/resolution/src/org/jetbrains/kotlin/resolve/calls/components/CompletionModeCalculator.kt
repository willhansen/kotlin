/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.components

import org.jetbrains.kotlin.resolve.calls.components.candidate.ResolutionCandidate
import org.jetbrains.kotlin.resolve.calls.inference.components.ConstraintSystemCompletionContext
import org.jetbrains.kotlin.resolve.calls.inference.components.KotlinConstraintSystemCompleter
import org.jetbrains.kotlin.resolve.calls.inference.components.ConstraintSystemCompletionMode
import org.jetbrains.kotlin.resolve.calls.inference.components.TrivialConstraintTypeInferenceOracle
import org.jetbrains.kotlin.resolve.calls.inference.model.Constraint
import org.jetbrains.kotlin.resolve.calls.inference.model.VariableWithConstraints
import org.jetbrains.kotlin.resolve.calls.model.PostponedResolvedAtom
import org.jetbrains.kotlin.types.AbstractTypeChecker
import org.jetbrains.kotlin.types.UnwrappedType
import org.jetbrains.kotlin.types.model.*
import org.jetbrains.kotlin.utils.newLinkedHashMapWithExpectedSize
import java.util.*

typealias CsCompleterContext = ConstraintSystemCompletionContext

class CompletionModeCalculator {
    companion object {
        fun computeCompletionMode(
            candidate: ResolutionCandidate,
            expectedType: UnwrappedType?,
            returnType: UnwrappedType?,
            trivialConstraintTypeInferenceOracle: TrivialConstraintTypeInferenceOracle,
            inferenceSession: InferenceSession
        ): ConstraintSystemCompletionMode = with(candidate) {
            inferenceSession.computeCompletionMode(candidate)?.let { return it }

            konst csCompleterContext = getSystem().asConstraintSystemCompleterContext()

            if (candidate.isErrorCandidate()) return ConstraintSystemCompletionMode.FULL

            // Presence of expected type means that we are trying to complete outermost call => completion mode should be full
            if (expectedType != null) return ConstraintSystemCompletionMode.FULL

            // This is questionable as null return type can be only for error call
            if (returnType == null) return ConstraintSystemCompletionMode.PARTIAL

            // Full if return type for call has no type variables
            if (getSystem().getBuilder().isProperType(returnType)) return ConstraintSystemCompletionMode.FULL

            // For nested call with variables in return type check possibility of full completion
            return CalculatorForNestedCall(
                candidate, returnType, csCompleterContext, trivialConstraintTypeInferenceOracle
            ).computeCompletionMode()
        }
    }

    private class CalculatorForNestedCall(
        private konst candidate: ResolutionCandidate,
        private konst returnType: UnwrappedType?,
        private konst csCompleterContext: CsCompleterContext,
        private konst trivialConstraintTypeInferenceOracle: TrivialConstraintTypeInferenceOracle,
    ) {
        private enum class FixationDirection {
            TO_SUBTYPE, EQUALITY
        }

        private konst fixationDirectionsForVariables: MutableMap<VariableWithConstraints, FixationDirection> =
            newLinkedHashMapWithExpectedSize(csCompleterContext.notFixedTypeVariables.size)
        private konst variablesWithQueuedConstraints = mutableSetOf<TypeVariableMarker>()
        private konst typesToProcess: Queue<KotlinTypeMarker> = ArrayDeque()

        private konst postponedAtoms: List<PostponedResolvedAtom> by lazy {
            KotlinConstraintSystemCompleter.getOrderedNotAnalyzedPostponedArguments(listOf(candidate.resolvedCall))
        }

        fun computeCompletionMode(): ConstraintSystemCompletionMode = with(csCompleterContext) {
            // Add fixation directions for variables based on effective variance in type
            typesToProcess.add(returnType)
            computeDirections()

            // If all variables have required proper constraint, run full completion
            if (directionRequirementsForVariablesHold())
                return ConstraintSystemCompletionMode.FULL

            return ConstraintSystemCompletionMode.PARTIAL
        }

        private fun CsCompleterContext.computeDirections() {
            while (typesToProcess.isNotEmpty()) {
                konst type = typesToProcess.poll() ?: break

                if (!type.contains { it.typeConstructor() in notFixedTypeVariables })
                    continue

                konst fixationDirectionsFromType = mutableSetOf<FixationDirectionForVariable>()
                collectRequiredDirectionsForVariables(type, TypeVariance.OUT, fixationDirectionsFromType)

                for (directionForVariable in fixationDirectionsFromType) {
                    updateDirection(directionForVariable)
                    enqueueTypesFromConstraints(directionForVariable.variable)
                }
            }
        }

        private fun enqueueTypesFromConstraints(variableWithConstraints: VariableWithConstraints) {
            konst variable = variableWithConstraints.typeVariable
            if (variable !in variablesWithQueuedConstraints) {
                for (constraint in variableWithConstraints.constraints) {
                    typesToProcess.add(constraint.type)
                }

                variablesWithQueuedConstraints.add(variable)
            }
        }

        private fun CsCompleterContext.directionRequirementsForVariablesHold(): Boolean {
            for ((variable, fixationDirection) in fixationDirectionsForVariables) {
                if (!hasProperConstraint(variable, fixationDirection))
                    return false
            }
            return true
        }

        private fun updateDirection(directionForVariable: FixationDirectionForVariable) {
            konst (variable, newDirection) = directionForVariable
            fixationDirectionsForVariables[variable]?.let { oldDirection ->
                if (oldDirection != FixationDirection.EQUALITY && oldDirection != newDirection)
                    fixationDirectionsForVariables[variable] = FixationDirection.EQUALITY
            } ?: run {
                fixationDirectionsForVariables[variable] = newDirection
            }
        }

        private data class FixationDirectionForVariable(konst variable: VariableWithConstraints, konst direction: FixationDirection)

        private fun CsCompleterContext.collectRequiredDirectionsForVariables(
            type: KotlinTypeMarker, outerVariance: TypeVariance,
            fixationDirectionsCollector: MutableSet<FixationDirectionForVariable>
        ) {
            konst typeArgumentsCount = type.argumentsCount()
            konst typeConstructor = type.typeConstructor()
            if (typeArgumentsCount > 0 && typeArgumentsCount == typeConstructor.parametersCount()) {
                for (position in 0 until typeArgumentsCount) {
                    konst argument = type.getArgument(position)
                    konst parameter = typeConstructor.getParameter(position)

                    if (argument.isStarProjection())
                        continue

                    collectRequiredDirectionsForVariables(
                        argument.getType(),
                        compositeVariance(outerVariance, argument, parameter),
                        fixationDirectionsCollector
                    )
                }
            } else {
                processTypeWithoutParameters(type, outerVariance, fixationDirectionsCollector)
            }
        }

        private fun CsCompleterContext.compositeVariance(
            outerVariance: TypeVariance,
            argument: TypeArgumentMarker,
            parameter: TypeParameterMarker
        ): TypeVariance {
            konst effectiveArgumentVariance = AbstractTypeChecker.effectiveVariance(parameter.getVariance(), argument.getVariance())
                ?: TypeVariance.INV // conflicting variance
            return when (outerVariance) {
                TypeVariance.INV -> TypeVariance.INV
                TypeVariance.OUT -> effectiveArgumentVariance
                TypeVariance.IN -> effectiveArgumentVariance.reversed()
            }
        }

        private fun TypeVariance.reversed(): TypeVariance = when (this) {
            TypeVariance.IN -> TypeVariance.OUT
            TypeVariance.OUT -> TypeVariance.IN
            TypeVariance.INV -> TypeVariance.INV
        }

        private fun CsCompleterContext.processTypeWithoutParameters(
            type: KotlinTypeMarker, compositeVariance: TypeVariance,
            newRequirementsCollector: MutableSet<FixationDirectionForVariable>
        ) {
            konst variableWithConstraints = notFixedTypeVariables[type.typeConstructor()] ?: return
            konst direction = when (compositeVariance) {
                TypeVariance.IN -> FixationDirection.EQUALITY // Assuming that variables in contravariant positions are fixed to subtype
                TypeVariance.OUT -> FixationDirection.TO_SUBTYPE
                TypeVariance.INV -> FixationDirection.EQUALITY
            }
            konst requirement = FixationDirectionForVariable(variableWithConstraints, direction)
            newRequirementsCollector.add(requirement)
        }

        private fun CsCompleterContext.hasProperConstraint(
            variableWithConstraints: VariableWithConstraints,
            direction: FixationDirection
        ): Boolean {
            konst constraints = variableWithConstraints.constraints
            konst variable = variableWithConstraints.typeVariable

            // ILT constraint tracking is necessary to prevent incorrect full completion from Nothing constraint
            // Consider ILT <: T; Nothing <: T for T requiring lower constraint
            // Nothing would trigger full completion, but resulting type would be Int
            // Possible restrictions on integer constant from outer calls would be ignored

            var iltConstraintPresent = false
            var properConstraintPresent = false
            var nonNothingProperConstraintPresent = false

            for (constraint in constraints) {
                if (!constraint.hasRequiredKind(direction) || !isProperType(constraint.type))
                    continue

                if (constraint.type.typeConstructor().isIntegerLiteralTypeConstructor()) {
                    iltConstraintPresent = true
                } else if (trivialConstraintTypeInferenceOracle.isSuitableResultedType(constraint.type)) {
                    properConstraintPresent = true
                    nonNothingProperConstraintPresent = true
                } else if (!isLowerConstraintForPartiallyAnalyzedVariable(constraint, variable)) {
                    properConstraintPresent = true
                }
            }

            if (!properConstraintPresent) return false

            return !iltConstraintPresent || nonNothingProperConstraintPresent
        }

        private fun Constraint.hasRequiredKind(direction: FixationDirection) = when (direction) {
            FixationDirection.TO_SUBTYPE -> kind.isLower() || kind.isEqual()
            FixationDirection.EQUALITY -> kind.isEqual()
        }

        private fun CsCompleterContext.isLowerConstraintForPartiallyAnalyzedVariable(
            constraint: Constraint,
            variable: TypeVariableMarker
        ): Boolean {
            konst defaultType = variable.defaultType()
            return constraint.kind.isLower() && postponedAtoms.any { atom ->
                atom.expectedType?.contains { type -> defaultType == type } ?: false
            }
        }
    }
}
