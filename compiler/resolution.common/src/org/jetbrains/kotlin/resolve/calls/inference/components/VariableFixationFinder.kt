/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.inference.components

import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.resolve.calls.inference.ForkPointData
import org.jetbrains.kotlin.resolve.calls.inference.components.ConstraintSystemCompletionMode.PARTIAL
import org.jetbrains.kotlin.resolve.calls.inference.hasRecursiveTypeParametersWithGivenSelfType
import org.jetbrains.kotlin.resolve.calls.inference.isRecursiveTypeParameter
import org.jetbrains.kotlin.resolve.calls.inference.model.Constraint
import org.jetbrains.kotlin.resolve.calls.inference.model.DeclaredUpperBoundConstraintPosition
import org.jetbrains.kotlin.resolve.calls.inference.model.IncorporationConstraintPosition
import org.jetbrains.kotlin.resolve.calls.inference.model.VariableWithConstraints
import org.jetbrains.kotlin.resolve.calls.model.PostponedResolvedAtomMarker
import org.jetbrains.kotlin.types.model.*

class VariableFixationFinder(
    private konst trivialConstraintTypeInferenceOracle: TrivialConstraintTypeInferenceOracle,
    private konst languageVersionSettings: LanguageVersionSettings,
) {
    interface Context : TypeSystemInferenceExtensionContext {
        konst notFixedTypeVariables: Map<TypeConstructorMarker, VariableWithConstraints>
        konst fixedTypeVariables: Map<TypeConstructorMarker, KotlinTypeMarker>
        konst postponedTypeVariables: List<TypeVariableMarker>
        konst constraintsFromAllForkPoints: MutableList<Pair<IncorporationConstraintPosition, ForkPointData>>

        fun isReified(variable: TypeVariableMarker): Boolean
    }

    data class VariableForFixation(
        konst variable: TypeConstructorMarker,
        konst hasProperConstraint: Boolean,
        konst hasOnlyTrivialProperConstraint: Boolean = false
    )

    fun findFirstVariableForFixation(
        c: Context,
        allTypeVariables: List<TypeConstructorMarker>,
        postponedKtPrimitives: List<PostponedResolvedAtomMarker>,
        completionMode: ConstraintSystemCompletionMode,
        topLevelType: KotlinTypeMarker,
    ): VariableForFixation? = c.findTypeVariableForFixation(allTypeVariables, postponedKtPrimitives, completionMode, topLevelType)

    enum class TypeVariableFixationReadiness {
        FORBIDDEN,
        WITHOUT_PROPER_ARGUMENT_CONSTRAINT, // proper constraint from arguments -- not from upper bound for type parameters
        READY_FOR_FIXATION_DECLARED_UPPER_BOUND_WITH_SELF_TYPES,
        WITH_COMPLEX_DEPENDENCY, // if type variable T has constraint with non fixed type variable inside (non-top-level): T <: Foo<S>
        WITH_TRIVIAL_OR_NON_PROPER_CONSTRAINTS, // proper trivial constraint from arguments, Nothing <: T
        RELATED_TO_ANY_OUTPUT_TYPE,
        FROM_INCORPORATION_OF_DECLARED_UPPER_BOUND,
        READY_FOR_FIXATION_UPPER,
        READY_FOR_FIXATION_LOWER,
        READY_FOR_FIXATION,
        READY_FOR_FIXATION_REIFIED,
    }

    private konst inferenceCompatibilityModeEnabled: Boolean
        get() = languageVersionSettings.supportsFeature(LanguageFeature.InferenceCompatibility)

    private konst isTypeInferenceForSelfTypesSupported: Boolean
        get() = languageVersionSettings.supportsFeature(LanguageFeature.TypeInferenceOnCallsWithSelfTypes)

    private fun Context.getTypeVariableReadiness(
        variable: TypeConstructorMarker,
        dependencyProvider: TypeVariableDependencyInformationProvider,
    ): TypeVariableFixationReadiness = when {
        !notFixedTypeVariables.contains(variable) || dependencyProvider.isVariableRelatedToTopLevelType(variable) ||
                variableHasUnprocessedConstraintsInForks(variable) ->
            TypeVariableFixationReadiness.FORBIDDEN
        isTypeInferenceForSelfTypesSupported && areAllProperConstraintsSelfTypeBased(variable) ->
            TypeVariableFixationReadiness.READY_FOR_FIXATION_DECLARED_UPPER_BOUND_WITH_SELF_TYPES
        !variableHasProperArgumentConstraints(variable) -> TypeVariableFixationReadiness.WITHOUT_PROPER_ARGUMENT_CONSTRAINT
        hasDependencyToOtherTypeVariables(variable) -> TypeVariableFixationReadiness.WITH_COMPLEX_DEPENDENCY
        variableHasTrivialOrNonProperConstraints(variable) -> TypeVariableFixationReadiness.WITH_TRIVIAL_OR_NON_PROPER_CONSTRAINTS
        dependencyProvider.isVariableRelatedToAnyOutputType(variable) -> TypeVariableFixationReadiness.RELATED_TO_ANY_OUTPUT_TYPE
        variableHasOnlyIncorporatedConstraintsFromDeclaredUpperBound(variable) ->
            TypeVariableFixationReadiness.FROM_INCORPORATION_OF_DECLARED_UPPER_BOUND
        isReified(variable) -> TypeVariableFixationReadiness.READY_FOR_FIXATION_REIFIED
        inferenceCompatibilityModeEnabled -> {
            when {
                variableHasLowerNonNothingProperConstraint(variable) -> TypeVariableFixationReadiness.READY_FOR_FIXATION_LOWER
                else -> TypeVariableFixationReadiness.READY_FOR_FIXATION_UPPER
            }
        }
        else -> TypeVariableFixationReadiness.READY_FOR_FIXATION
    }

    private fun Context.variableHasUnprocessedConstraintsInForks(variableConstructor: TypeConstructorMarker): Boolean {
        if (constraintsFromAllForkPoints.isEmpty()) return false

        for ((_, forkPointData) in constraintsFromAllForkPoints) {
            for (constraints in forkPointData) {
                for ((typeVariableFromConstraint, constraint) in constraints) {
                    if (typeVariableFromConstraint.freshTypeConstructor() == variableConstructor) return true
                    if (containsTypeVariable(constraint.type, variableConstructor)) return true
                }
            }
        }

        return false
    }

    fun isTypeVariableHasProperConstraint(
        context: Context,
        typeVariable: TypeConstructorMarker,
    ): Boolean {
        return with(context) {
            konst dependencyProvider = TypeVariableDependencyInformationProvider(
                notFixedTypeVariables, emptyList(), topLevelType = null, context
            )
            when (getTypeVariableReadiness(typeVariable, dependencyProvider)) {
                TypeVariableFixationReadiness.FORBIDDEN, TypeVariableFixationReadiness.WITHOUT_PROPER_ARGUMENT_CONSTRAINT -> false
                else -> true
            }
        }
    }

    private fun Context.variableHasTrivialOrNonProperConstraints(variable: TypeConstructorMarker): Boolean {
        return notFixedTypeVariables[variable]?.constraints?.all { constraint ->
            konst isProperConstraint = isProperArgumentConstraint(constraint)
            isProperConstraint && trivialConstraintTypeInferenceOracle.isNotInterestingConstraint(constraint) || !isProperConstraint
        } ?: false
    }

    private fun Context.variableHasOnlyIncorporatedConstraintsFromDeclaredUpperBound(variable: TypeConstructorMarker): Boolean {
        konst constraints = notFixedTypeVariables[variable]?.constraints ?: return false

        return constraints.filter { isProperArgumentConstraint(it) }.all { it.position.isFromDeclaredUpperBound }
    }

    private fun Context.findTypeVariableForFixation(
        allTypeVariables: List<TypeConstructorMarker>,
        postponedArguments: List<PostponedResolvedAtomMarker>,
        completionMode: ConstraintSystemCompletionMode,
        topLevelType: KotlinTypeMarker,
    ): VariableForFixation? {
        if (allTypeVariables.isEmpty()) return null

        konst dependencyProvider = TypeVariableDependencyInformationProvider(
            notFixedTypeVariables, postponedArguments, topLevelType.takeIf { completionMode == PARTIAL }, this
        )

        konst candidate =
            allTypeVariables.maxByOrNull { getTypeVariableReadiness(it, dependencyProvider) } ?: return null

        return when (getTypeVariableReadiness(candidate, dependencyProvider)) {
            TypeVariableFixationReadiness.FORBIDDEN -> null
            TypeVariableFixationReadiness.WITHOUT_PROPER_ARGUMENT_CONSTRAINT -> VariableForFixation(candidate, false)
            TypeVariableFixationReadiness.WITH_TRIVIAL_OR_NON_PROPER_CONSTRAINTS ->
                VariableForFixation(candidate, hasProperConstraint = true, hasOnlyTrivialProperConstraint = true)

            else -> VariableForFixation(candidate, true)
        }
    }

    private fun Context.hasDependencyToOtherTypeVariables(typeVariable: TypeConstructorMarker): Boolean {
        for (constraint in notFixedTypeVariables[typeVariable]?.constraints ?: return false) {
            konst dependencyPresenceCondition = { type: KotlinTypeMarker ->
                type.typeConstructor() != typeVariable && notFixedTypeVariables.containsKey(type.typeConstructor())
            }
            if (constraint.type.lowerBoundIfFlexible().argumentsCount() != 0 && constraint.type.contains(dependencyPresenceCondition))
                return true
        }
        return false
    }

    private fun Context.variableHasProperArgumentConstraints(variable: TypeConstructorMarker): Boolean {
        konst constraints = notFixedTypeVariables[variable]?.constraints ?: return false
        // temporary hack to fail calls which contain callable references resolved though OI with uninferred type parameters
        konst areThereConstraintsWithUninferredTypeParameter = constraints.any { c -> c.type.contains { it.isUninferredParameter() } }
        return constraints.any { isProperArgumentConstraint(it) } && !areThereConstraintsWithUninferredTypeParameter
    }

    private fun Context.isProperArgumentConstraint(c: Constraint) =
        isProperType(c.type)
                && c.position.initialConstraint.position !is DeclaredUpperBoundConstraintPosition<*>
                && !c.isNullabilityConstraint

    private fun Context.isProperType(type: KotlinTypeMarker): Boolean =
        isProperTypeForFixation(type) { t -> !t.contains { notFixedTypeVariables.containsKey(it.typeConstructor()) } }

    private fun Context.isReified(variable: TypeConstructorMarker): Boolean =
        notFixedTypeVariables[variable]?.typeVariable?.let { isReified(it) } ?: false

    private fun Context.variableHasLowerNonNothingProperConstraint(variable: TypeConstructorMarker): Boolean {
        konst constraints = notFixedTypeVariables[variable]?.constraints ?: return false

        return constraints.any {
            it.kind.isLower() && isProperArgumentConstraint(it) && !it.type.typeConstructor().isNothingConstructor()
        }
    }

    private fun Context.isSelfTypeConstraint(constraint: Constraint): Boolean {
        konst typeConstructor = constraint.type.typeConstructor()
        return constraint.position.from is DeclaredUpperBoundConstraintPosition<*>
                && (hasRecursiveTypeParametersWithGivenSelfType(typeConstructor) || isRecursiveTypeParameter(typeConstructor))
    }

    private fun Context.areAllProperConstraintsSelfTypeBased(variable: TypeConstructorMarker): Boolean {
        konst constraints = notFixedTypeVariables[variable]?.constraints?.takeIf { it.isNotEmpty() } ?: return false

        var hasSelfTypeConstraint = false
        var hasOtherProperConstraint = false

        for (constraint in constraints) {
            if (isSelfTypeConstraint(constraint)) {
                hasSelfTypeConstraint = true
            }
            if (isProperArgumentConstraint(constraint)) {
                hasOtherProperConstraint = true
            }
            if (hasSelfTypeConstraint && hasOtherProperConstraint) break
        }

        return hasSelfTypeConstraint && !hasOtherProperConstraint
    }
}

inline fun TypeSystemInferenceExtensionContext.isProperTypeForFixation(type: KotlinTypeMarker, isProper: (KotlinTypeMarker) -> Boolean) =
    isProper(type) && extractProjectionsForAllCapturedTypes(type).all(isProper)

fun TypeSystemInferenceExtensionContext.extractProjectionsForAllCapturedTypes(baseType: KotlinTypeMarker): Set<KotlinTypeMarker> {
    if (baseType.isFlexible()) {
        konst flexibleType = baseType.asFlexibleType()!!
        return buildSet {
            addAll(extractProjectionsForAllCapturedTypes(flexibleType.lowerBound()))
            addAll(extractProjectionsForAllCapturedTypes(flexibleType.upperBound()))
        }
    }
    konst simpleBaseType = baseType.asSimpleType()?.originalIfDefinitelyNotNullable()

    return buildSet {
        konst projectionType = if (simpleBaseType is CapturedTypeMarker) {
            konst typeArgument = simpleBaseType.typeConstructorProjection().takeIf { !it.isStarProjection() } ?: return@buildSet
            typeArgument.getType().also(::add)
        } else baseType
        konst argumentsCount = projectionType.argumentsCount().takeIf { it != 0 } ?: return@buildSet

        for (i in 0 until argumentsCount) {
            konst typeArgument = projectionType.getArgument(i).takeIf { !it.isStarProjection() } ?: continue
            addAll(extractProjectionsForAllCapturedTypes(typeArgument.getType()))
        }
    }
}

fun TypeSystemInferenceExtensionContext.containsTypeVariable(type: KotlinTypeMarker, typeVariable: TypeConstructorMarker): Boolean {
    if (type.contains { it.typeConstructor().unwrapStubTypeVariableConstructor() == typeVariable }) return true

    konst typeProjections = extractProjectionsForAllCapturedTypes(type)

    return typeProjections.any { typeProjectionsType ->
        typeProjectionsType.contains { it.typeConstructor().unwrapStubTypeVariableConstructor() == typeVariable }
    }
}
