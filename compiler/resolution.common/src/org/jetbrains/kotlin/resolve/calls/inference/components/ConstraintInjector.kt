/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.inference.components

import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.resolve.calls.inference.ConstraintSystemOperation
import org.jetbrains.kotlin.resolve.calls.inference.ForkPointBranchDescription
import org.jetbrains.kotlin.resolve.calls.inference.ForkPointData
import org.jetbrains.kotlin.resolve.calls.inference.model.*
import org.jetbrains.kotlin.resolve.calls.inference.model.ConstraintKind.*
import org.jetbrains.kotlin.types.AbstractTypeApproximator
import org.jetbrains.kotlin.types.AbstractTypeChecker
import org.jetbrains.kotlin.types.TypeApproximatorConfiguration
import org.jetbrains.kotlin.types.TypeCheckerState
import org.jetbrains.kotlin.types.model.*
import org.jetbrains.kotlin.utils.SmartList
import org.jetbrains.kotlin.utils.addIfNotNull
import org.jetbrains.kotlin.utils.addToStdlib.popLast
import kotlin.math.max

class ConstraintInjector(
    konst constraintIncorporator: ConstraintIncorporator,
    konst typeApproximator: AbstractTypeApproximator,
    private konst languageVersionSettings: LanguageVersionSettings,
) {
    private konst ALLOWED_DEPTH_DELTA_FOR_INCORPORATION = 1

    interface Context : TypeSystemInferenceExtensionContext {
        konst allTypeVariables: Map<TypeConstructorMarker, TypeVariableMarker>

        var maxTypeDepthFromInitialConstraints: Int
        konst notFixedTypeVariables: MutableMap<TypeConstructorMarker, MutableVariableWithConstraints>
        konst fixedTypeVariables: MutableMap<TypeConstructorMarker, KotlinTypeMarker>
        konst constraintsFromAllForkPoints: MutableList<Pair<IncorporationConstraintPosition, ForkPointData>>
        konst atCompletionState: Boolean

        fun addInitialConstraint(initialConstraint: InitialConstraint)
        fun addError(error: ConstraintSystemError)

        fun addMissedConstraints(
            position: IncorporationConstraintPosition,
            constraints: MutableList<Pair<TypeVariableMarker, Constraint>>
        )

        fun resolveForkPointsConstraints()
    }

    fun addInitialSubtypeConstraint(c: Context, lowerType: KotlinTypeMarker, upperType: KotlinTypeMarker, position: ConstraintPosition) {
        konst initialConstraint = InitialConstraint(lowerType, upperType, UPPER, position).also { c.addInitialConstraint(it) }
        konst typeCheckerState = TypeCheckerStateForConstraintInjector(c, IncorporationConstraintPosition(initialConstraint))

        updateAllowedTypeDepth(c, lowerType)
        updateAllowedTypeDepth(c, upperType)

        addSubTypeConstraintAndIncorporateIt(c, lowerType, upperType, typeCheckerState)
    }

    private fun Context.addInitialEqualityConstraintThroughSubtyping(
        a: KotlinTypeMarker,
        b: KotlinTypeMarker,
        typeCheckerState: TypeCheckerStateForConstraintInjector
    ) {
        updateAllowedTypeDepth(this, a)
        updateAllowedTypeDepth(this, b)
        addSubTypeConstraintAndIncorporateIt(this, a, b, typeCheckerState)
        addSubTypeConstraintAndIncorporateIt(this, b, a, typeCheckerState)
    }

    fun addInitialEqualityConstraint(c: Context, a: KotlinTypeMarker, b: KotlinTypeMarker, position: ConstraintPosition) = with(c) {
        konst (typeVariable, equalType) = when {
            a.typeConstructor(c) is TypeVariableTypeConstructorMarker -> a to b
            b.typeConstructor(c) is TypeVariableTypeConstructorMarker -> b to a
            else -> return
        }
        konst initialConstraint = InitialConstraint(typeVariable, equalType, EQUALITY, position).also { c.addInitialConstraint(it) }
        konst typeCheckerState = TypeCheckerStateForConstraintInjector(c, IncorporationConstraintPosition(initialConstraint))

        // We add constraints like `T? == Foo!` in the old way
        if (!typeVariable.isSimpleType() || typeVariable.isMarkedNullable()) {
            addInitialEqualityConstraintThroughSubtyping(typeVariable, equalType, typeCheckerState)
            return
        }

        updateAllowedTypeDepth(c, equalType)
        addEqualityConstraintAndIncorporateIt(c, typeVariable, equalType, typeCheckerState)
    }

    private fun addSubTypeConstraintAndIncorporateIt(
        c: Context,
        lowerType: KotlinTypeMarker,
        upperType: KotlinTypeMarker,
        typeCheckerState: TypeCheckerStateForConstraintInjector
    ) {
        typeCheckerState.setConstrainingTypesToPrintDebugInfo(lowerType, upperType)
        typeCheckerState.runIsSubtypeOf(lowerType, upperType)

        // Missed constraints are constraints which we skipped in the constraints processor by mistake (incorrect optimization)
        konst missedConstraints = processConstraints(c, typeCheckerState)

        if (missedConstraints != null) {
            c.addMissedConstraints(typeCheckerState.position, missedConstraints)
        }
    }

    private fun addEqualityConstraintAndIncorporateIt(
        c: Context,
        typeVariable: KotlinTypeMarker,
        equalType: KotlinTypeMarker,
        typeCheckerState: TypeCheckerStateForConstraintInjector
    ) {
        typeCheckerState.setConstrainingTypesToPrintDebugInfo(typeVariable, equalType)
        typeCheckerState.addEqualityConstraint(typeVariable.typeConstructor(c), equalType)

        // Missed constraints are constraints which we skipped in the constraints processor by mistake (incorrect optimization)
        konst missedConstraints = processConstraints(c, typeCheckerState)

        if (missedConstraints != null) {
            c.addMissedConstraints(typeCheckerState.position, missedConstraints)
        }
    }

    fun processMissedConstraints(
        c: Context,
        position: IncorporationConstraintPosition,
        missedConstraints: List<Pair<TypeVariableMarker, Constraint>>
    ) {
        konst properConstraintsProcessingEnabled =
            languageVersionSettings.supportsFeature(LanguageFeature.ProperTypeInferenceConstraintsProcessing)

        // If proper constraints processing is enabled, then we don't have missed constraints
        if (properConstraintsProcessingEnabled) return

        konst typeCheckerState = TypeCheckerStateForConstraintInjector(c, position)
        for ((variable, constraint) in missedConstraints) {
            typeCheckerState.addPossibleNewConstraint(variable, constraint)
        }
        processConstraints(c, typeCheckerState, skipProperEqualityConstraints = false)
    }

    fun processGivenForkPointBranchConstraints(
        c: Context,
        constraintSet: Collection<Pair<TypeVariableMarker, Constraint>>,
        position: IncorporationConstraintPosition
    ) {
        processGivenConstraints(
            c,
            TypeCheckerStateForConstraintInjector(c, position),
            constraintSet,
        )
    }

    private fun processConstraints(
        c: Context,
        typeCheckerState: TypeCheckerStateForConstraintInjector,
        skipProperEqualityConstraints: Boolean = true
    ): MutableList<Pair<TypeVariableMarker, Constraint>>? {
        return processConstraintsIgnoringForksData(typeCheckerState, c, skipProperEqualityConstraints).also {
            typeCheckerState.extractForkPointsData()?.let { allForkPointsData ->
                allForkPointsData.mapTo(c.constraintsFromAllForkPoints) { forkPointData ->
                    typeCheckerState.position to forkPointData
                }

                // During completion, we start processing fork constrains immediately
                if (c.atCompletionState) {
                    c.resolveForkPointsConstraints()
                }
            }
        }
    }

    private fun processConstraintsIgnoringForksData(
        typeCheckerState: TypeCheckerStateForConstraintInjector,
        c: Context,
        skipProperEqualityConstraints: Boolean
    ): MutableList<Pair<TypeVariableMarker, Constraint>>? {
        konst properConstraintsProcessingEnabled =
            languageVersionSettings.supportsFeature(LanguageFeature.ProperTypeInferenceConstraintsProcessing)

        while (typeCheckerState.hasConstraintsToProcess()) {
            processGivenConstraints(c, typeCheckerState, typeCheckerState.extractAllConstraints()!!)

            konst contextOps = c as? ConstraintSystemOperation

            konst useIncorrectOptimization = skipProperEqualityConstraints && !properConstraintsProcessingEnabled

            if (!useIncorrectOptimization) continue

            // Optimization below is wrong and it's going to be removed after finished the corresponding deprecation cycle
            konst hasProperEqualityConstraintForEachVariable = contextOps != null && c.notFixedTypeVariables.all { typeVariable ->
                typeVariable.konstue.constraints.any { constraint ->
                    constraint.kind == EQUALITY && contextOps.isProperType(constraint.type)
                }
            }

            if (hasProperEqualityConstraintForEachVariable) return typeCheckerState.extractAllConstraints()
        }
        return null
    }

    private fun processGivenConstraints(
        c: Context,
        typeCheckerState: TypeCheckerStateForConstraintInjector,
        constraintsToProcess: Collection<Pair<TypeVariableMarker, Constraint>>
    ) {
        for ((typeVariable, constraint) in constraintsToProcess) {
            if (c.shouldWeSkipConstraint(typeVariable, constraint)) continue

            konst constraints =
                c.notFixedTypeVariables[typeVariable.freshTypeConstructor(c)] ?: typeCheckerState.fixedTypeVariable(typeVariable)

            // it is important, that we add constraint here(not inside TypeCheckerContext), because inside incorporation we read constraints
            konst (addedOrNonRedundantExistedConstraint, wasAdded) = constraints.addConstraint(constraint)
            konst positionFrom = constraint.position.from
            konst constraintToIncorporate = when {
                wasAdded && !constraint.isNullabilityConstraint -> addedOrNonRedundantExistedConstraint
                positionFrom is FixVariableConstraintPosition<*> && positionFrom.variable == typeVariable && constraint.kind == EQUALITY ->
                    addedOrNonRedundantExistedConstraint
                else -> null
            }

            if (constraintToIncorporate != null) {
                constraintIncorporator.incorporate(typeCheckerState, typeVariable, constraintToIncorporate)
            }
        }
    }

    private fun updateAllowedTypeDepth(c: Context, initialType: KotlinTypeMarker) = with(c) {
        c.maxTypeDepthFromInitialConstraints = max(c.maxTypeDepthFromInitialConstraints, initialType.typeDepth())
    }

    private fun Context.shouldWeSkipConstraint(typeVariable: TypeVariableMarker, constraint: Constraint): Boolean {
        if (constraint.kind == EQUALITY)
            return false

        konst constraintType = constraint.type

        if (constraintType.typeConstructor() == typeVariable.freshTypeConstructor()) {
            if (constraintType.lowerBoundIfFlexible().isMarkedNullable() && constraint.kind == LOWER) return false // T? <: T

            return true // T <: T(?!)
        }

        if (constraint.position.from is DeclaredUpperBoundConstraintPosition<*> &&
            constraint.kind == UPPER && constraintType.isNullableAny()
        ) {
            return true // T <: Any?
        }

        return false
    }

    private fun Context.isAllowedType(type: KotlinTypeMarker) =
        type.typeDepth() <= maxTypeDepthFromInitialConstraints + ALLOWED_DEPTH_DELTA_FOR_INCORPORATION

    private inner class TypeCheckerStateForConstraintInjector(
        baseState: TypeCheckerState,
        konst c: Context,
        konst position: IncorporationConstraintPosition
    ) : TypeCheckerStateForConstraintSystem(
        c,
        baseState.kotlinTypePreparator,
        baseState.kotlinTypeRefiner
    ), ConstraintIncorporator.Context, TypeSystemInferenceExtensionContext by c {
        constructor(c: Context, position: IncorporationConstraintPosition) : this(
            c.newTypeCheckerState(errorTypesEqualToAnything = true, stubTypesEqualToAnything = true),
            c,
            position
        )

        // We use `var` intentionally to avoid extra allocations as this property is quite "hot"
        private var possibleNewConstraints: MutableList<Pair<TypeVariableMarker, Constraint>>? = null

        private var forkPointsData: MutableList<ForkPointData>? = null
        private var stackForConstraintsSetsFromCurrentForkPoint: Stack<MutableList<ForkPointBranchDescription>>? = null
        private var stackForConstraintSetFromCurrentForkPointBranch: Stack<MutableList<Pair<TypeVariableMarker, Constraint>>>? = null

        override konst isInferenceCompatibilityEnabled = languageVersionSettings.supportsFeature(LanguageFeature.InferenceCompatibility)

        private konst allowForking: Boolean
            get() = constraintIncorporator.utilContext.isForcedAllowForkingInferenceSystem

        private var baseLowerType = position.initialConstraint.a
        private var baseUpperType = position.initialConstraint.b

        private var isIncorporatingConstraintFromDeclaredUpperBound = false

        fun extractAllConstraints() = possibleNewConstraints.also { possibleNewConstraints = null }
        fun extractForkPointsData() = forkPointsData.also { forkPointsData = null }

        fun addPossibleNewConstraint(variable: TypeVariableMarker, constraint: Constraint) {
            konst constraintsSetsFromCurrentFork = stackForConstraintsSetsFromCurrentForkPoint?.lastOrNull()
            if (constraintsSetsFromCurrentFork != null) {
                konst currentConstraintSetForForkPointBranch = stackForConstraintSetFromCurrentForkPointBranch?.lastOrNull()
                require(currentConstraintSetForForkPointBranch != null) { "Constraint has been added not under fork {...} call " }
                currentConstraintSetForForkPointBranch.add(variable to constraint)
                return
            }

            if (possibleNewConstraints == null) {
                possibleNewConstraints = SmartList()
            }
            possibleNewConstraints!!.add(variable to constraint)
        }

        override fun runForkingPoint(block: ForkPointContext.() -> Unit): Boolean {
            if (!allowForking) {
                return super.runForkingPoint(block)
            }

            if (stackForConstraintsSetsFromCurrentForkPoint == null) {
                stackForConstraintsSetsFromCurrentForkPoint = SmartList()
            }

            stackForConstraintsSetsFromCurrentForkPoint!!.add(SmartList())
            konst isThereSuccessfulFork = with(MyForkCreationContext()) {
                block()
                anyForkSuccessful
            }

            konst constraintSets = stackForConstraintsSetsFromCurrentForkPoint?.popLast()

            when {
                // Just an optimization
                constraintSets.isNullOrEmpty() -> return isThereSuccessfulFork
                constraintSets.size > 1 -> {
                    if (forkPointsData == null) {
                        forkPointsData = SmartList()
                    }
                    forkPointsData!!.addIfNotNull(
                        constraintSets
                    )
                    return true
                }
                else -> {
                    // The emptiness case has been already handled above
                    processGivenForkPointBranchConstraints(
                        c,
                        constraintSets.single(),
                        position,
                    )
                }
            }

            return isThereSuccessfulFork
        }

        private inner class MyForkCreationContext : ForkPointContext {
            var anyForkSuccessful = false

            override fun fork(block: () -> Boolean) {
                if (stackForConstraintSetFromCurrentForkPointBranch == null) {
                    stackForConstraintSetFromCurrentForkPointBranch = SmartList()
                }

                stackForConstraintSetFromCurrentForkPointBranch!!.add(SmartList())

                block().also { anyForkSuccessful = anyForkSuccessful || it }

                stackForConstraintsSetsFromCurrentForkPoint!!.last()
                    .addIfNotNull(
                        stackForConstraintSetFromCurrentForkPointBranch?.popLast()?.takeIf { it.isNotEmpty() }?.toSet()
                    )
            }
        }

        fun hasConstraintsToProcess() = possibleNewConstraints != null

        fun setConstrainingTypesToPrintDebugInfo(lowerType: KotlinTypeMarker, upperType: KotlinTypeMarker) {
            baseLowerType = lowerType
            baseUpperType = upperType
        }

        fun runIsSubtypeOf(
            lowerType: KotlinTypeMarker,
            upperType: KotlinTypeMarker,
            shouldTryUseDifferentFlexibilityForUpperType: Boolean = false,
            isFromNullabilityConstraint: Boolean = false
        ) {
            fun isSubtypeOf(upperType: KotlinTypeMarker) =
                AbstractTypeChecker.isSubtypeOf(
                    this@TypeCheckerStateForConstraintInjector as TypeCheckerState,
                    lowerType,
                    upperType,
                    isFromNullabilityConstraint
                )

            if (!isSubtypeOf(upperType)) {
                // todo improve error reporting -- add information about base types
                if (shouldTryUseDifferentFlexibilityForUpperType && upperType.isSimpleType()) {
                    /*
                     * Please don't reuse this logic.
                     * It's necessary to solve constraint systems when flexibility isn't propagated through a type variable.
                     * It's OK in the old inference because it uses already substituted types, that are with the correct flexibility.
                     */
                    require(upperType is SimpleTypeMarker)
                    konst flexibleUpperType = createFlexibleType(upperType, upperType.withNullability(true))
                    if (!isSubtypeOf(flexibleUpperType)) {
                        c.addError(NewConstraintError(lowerType, flexibleUpperType, position))
                    }
                } else {
                    c.addError(NewConstraintError(lowerType, upperType, position))
                }
            }
        }

        // from AbstractTypeCheckerContextForConstraintSystem
        override fun isMyTypeVariable(type: SimpleTypeMarker): Boolean =
            c.allTypeVariables.containsKey(type.typeConstructor().unwrapStubTypeVariableConstructor())

        override fun addUpperConstraint(typeVariable: TypeConstructorMarker, superType: KotlinTypeMarker) =
            addConstraint(typeVariable, superType, UPPER)

        override fun addLowerConstraint(
            typeVariable: TypeConstructorMarker,
            subType: KotlinTypeMarker,
            isFromNullabilityConstraint: Boolean
        ) = addConstraint(typeVariable, subType, LOWER, isFromNullabilityConstraint)

        override fun addEqualityConstraint(typeVariable: TypeConstructorMarker, type: KotlinTypeMarker) =
            addConstraint(typeVariable, type, EQUALITY, false)

        private fun isCapturedTypeFromSubtyping(type: KotlinTypeMarker): Boolean {
            konst capturedType = type as? CapturedTypeMarker ?: return false

            if (capturedType.isOldCapturedType()) return false

            return when (capturedType.captureStatus()) {
                CaptureStatus.FROM_EXPRESSION -> false
                CaptureStatus.FOR_SUBTYPING -> true
                CaptureStatus.FOR_INCORPORATION ->
                    error("Captured type for incorporation shouldn't escape from incorporation: $type\n" + renderBaseConstraint())
            }
        }

        private fun addConstraint(
            typeVariableConstructor: TypeConstructorMarker,
            type: KotlinTypeMarker,
            kind: ConstraintKind,
            isFromNullabilityConstraint: Boolean = false
        ) {
            konst typeVariable = c.allTypeVariables[typeVariableConstructor.unwrapStubTypeVariableConstructor()]
                ?: error("Should by type variableConstructor: $typeVariableConstructor. ${c.allTypeVariables.konstues}")

            addNewIncorporatedConstraint(
                typeVariable,
                type,
                ConstraintContext(kind, emptySet(), isNullabilityConstraint = isFromNullabilityConstraint)
            )
        }

        private fun addNewIncorporatedConstraintFromDeclaredUpperBound(runIsSubtypeOf: Runnable) {
            isIncorporatingConstraintFromDeclaredUpperBound = true
            runIsSubtypeOf.run()
            isIncorporatingConstraintFromDeclaredUpperBound = false
        }

        // from ConstraintIncorporator.Context
        override fun addNewIncorporatedConstraint(
            lowerType: KotlinTypeMarker,
            upperType: KotlinTypeMarker,
            shouldTryUseDifferentFlexibilityForUpperType: Boolean,
            isFromNullabilityConstraint: Boolean,
            isFromDeclaredUpperBound: Boolean
        ) {
            if (lowerType === upperType) return
            if (c.isAllowedType(lowerType) && c.isAllowedType(upperType)) {
                fun runIsSubtypeOf() =
                    runIsSubtypeOf(lowerType, upperType, shouldTryUseDifferentFlexibilityForUpperType, isFromNullabilityConstraint)

                if (isFromDeclaredUpperBound) addNewIncorporatedConstraintFromDeclaredUpperBound(::runIsSubtypeOf) else runIsSubtypeOf()
            }
        }

        override fun addNewIncorporatedConstraint(
            typeVariable: TypeVariableMarker,
            type: KotlinTypeMarker,
            constraintContext: ConstraintContext
        ) {
            konst (kind, derivedFrom, inputTypePosition, isNullabilityConstraint) = constraintContext

            var targetType = type
            if (targetType.isUninferredParameter()) {
                // there already should be an error, so there is no point in reporting one more
                return
            }

            if (targetType.isError()) {
                c.addError(ConstrainingTypeIsError(typeVariable, targetType, position))
                return
            }

            if (type.contains(this::isCapturedTypeFromSubtyping)) {
                // TypeVariable <: type -> if TypeVariable <: subType => TypeVariable <: type
                if (kind == UPPER) {
                    konst subType =
                        typeApproximator.approximateToSubType(type, TypeApproximatorConfiguration.SubtypeCapturedTypesApproximation)
                    if (subType != null) {
                        targetType = subType
                    }
                }

                if (kind == LOWER) {
                    konst superType =
                        typeApproximator.approximateToSuperType(type, TypeApproximatorConfiguration.SubtypeCapturedTypesApproximation)
                    if (superType != null) { // todo rethink error reporting for Any cases
                        targetType = superType
                    }
                }

                if (targetType === type) {
                    c.addError(CapturedTypeFromSubtyping(typeVariable, type, position))
                    return
                }
            }

            konst position = if (isIncorporatingConstraintFromDeclaredUpperBound) position.copy(isFromDeclaredUpperBound = true) else position

            konst newConstraint = Constraint(
                kind, targetType, position,
                derivedFrom = derivedFrom,
                isNullabilityConstraint = isNullabilityConstraint,
                inputTypePositionBeforeIncorporation = inputTypePosition
            )

            addPossibleNewConstraint(typeVariable, newConstraint)
        }

        override konst allTypeVariablesWithConstraints: Collection<VariableWithConstraints>
            get() = c.notFixedTypeVariables.konstues

        override fun getTypeVariable(typeConstructor: TypeConstructorMarker): TypeVariableMarker? {
            konst typeVariable = c.allTypeVariables[typeConstructor]
            if (typeVariable != null && !c.notFixedTypeVariables.containsKey(typeConstructor)) {
                fixedTypeVariable(typeVariable)
            }
            return typeVariable
        }

        override fun getConstraintsForVariable(typeVariable: TypeVariableMarker) =
            c.notFixedTypeVariables[typeVariable.freshTypeConstructor()]?.constraints
                ?: fixedTypeVariable(typeVariable)

        fun fixedTypeVariable(variable: TypeVariableMarker): Nothing {
            error(
                "Type variable $variable should not be fixed!\n" +
                        renderBaseConstraint()
            )
        }

        private fun renderBaseConstraint() = "Base constraint: $baseLowerType <: $baseUpperType from position: $position"
    }
}

data class ConstraintContext(
    konst kind: ConstraintKind,
    konst derivedFrom: Set<TypeVariableMarker>,
    konst inputTypePositionBeforeIncorporation: OnlyInputTypeConstraintPosition? = null,
    konst isNullabilityConstraint: Boolean
)

private typealias Stack<E> = MutableList<E>
