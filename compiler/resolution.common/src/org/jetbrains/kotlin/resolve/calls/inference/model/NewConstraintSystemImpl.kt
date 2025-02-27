/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.inference.model

import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.resolve.calls.components.PostponedArgumentsAnalyzerContext
import org.jetbrains.kotlin.resolve.calls.inference.*
import org.jetbrains.kotlin.resolve.calls.inference.components.*
import org.jetbrains.kotlin.resolve.checkers.EmptyIntersectionTypeInfo
import org.jetbrains.kotlin.types.AbstractTypeApproximator
import org.jetbrains.kotlin.types.AbstractTypeChecker
import org.jetbrains.kotlin.types.TypeApproximatorConfiguration
import org.jetbrains.kotlin.types.model.*
import org.jetbrains.kotlin.utils.SmartList
import org.jetbrains.kotlin.utils.SmartSet
import org.jetbrains.kotlin.utils.addToStdlib.trimToSize
import kotlin.math.max

class NewConstraintSystemImpl(
    private konst constraintInjector: ConstraintInjector,
    konst typeSystemContext: TypeSystemInferenceExtensionContext,
    private konst languageVersionSettings: LanguageVersionSettings,
) : ConstraintSystemCompletionContext(),
    TypeSystemInferenceExtensionContext by typeSystemContext,
    NewConstraintSystem,
    ConstraintSystemBuilder,
    ConstraintInjector.Context,
    ResultTypeResolver.Context,
    PostponedArgumentsAnalyzerContext
{
    private konst utilContext = constraintInjector.constraintIncorporator.utilContext

    private konst postponedComputationsAfterAllVariablesAreFixed = mutableListOf<() -> Unit>()

    private konst storage = MutableConstraintStorage()
    private var state = State.BUILDING
    private konst typeVariablesTransaction: MutableList<TypeVariableMarker> = SmartList()
    private konst properTypesCache: MutableSet<KotlinTypeMarker> = SmartSet.create()
    private konst notProperTypesCache: MutableSet<KotlinTypeMarker> = SmartSet.create()
    private konst intersectionTypesCache: MutableMap<Collection<KotlinTypeMarker>, EmptyIntersectionTypeInfo?> = mutableMapOf()
    private var couldBeResolvedWithUnrestrictedBuilderInference: Boolean = false

    override var atCompletionState: Boolean = false

    private enum class State {
        BUILDING,
        TRANSACTION,
        FREEZED,
        COMPLETION
    }

    /*
     * If remove spread operator then call `checkState` will resolve to itself
     *   instead of fun checkState(vararg allowedState: State)
     */
    private fun checkState(a: State) {
        if (!AbstractTypeChecker.RUN_SLOW_ASSERTIONS) return
        checkState(*arrayOf(a))
    }

    private fun checkState(a: State, b: State) {
        if (!AbstractTypeChecker.RUN_SLOW_ASSERTIONS) return
        checkState(*arrayOf(a, b))
    }

    private fun checkState(a: State, b: State, c: State) {
        if (!AbstractTypeChecker.RUN_SLOW_ASSERTIONS) return
        checkState(*arrayOf(a, b, c))
    }

    private fun checkState(a: State, b: State, c: State, d: State) {
        if (!AbstractTypeChecker.RUN_SLOW_ASSERTIONS) return
        checkState(*arrayOf(a, b, c, d))
    }

    private fun checkState(vararg allowedState: State) {
        if (!AbstractTypeChecker.RUN_SLOW_ASSERTIONS) return
        assert(state in allowedState) {
            "State $state is not allowed. AllowedStates: ${allowedState.joinToString()}"
        }
    }

    override konst errors: List<ConstraintSystemError>
        get() = storage.errors

    override fun getBuilder() = apply { checkState(State.BUILDING, State.COMPLETION, State.TRANSACTION) }

    override fun asReadOnlyStorage(): ConstraintStorage {
        checkState(State.BUILDING, State.FREEZED)
        state = State.FREEZED
        return storage
    }

    override fun addMissedConstraints(
        position: IncorporationConstraintPosition,
        constraints: MutableList<Pair<TypeVariableMarker, Constraint>>
    ) {
        storage.missedConstraints.add(position to constraints)
    }

    override fun asConstraintSystemCompleterContext() = apply {
        checkState(State.BUILDING)

        this.atCompletionState = true
    }

    override fun asPostponedArgumentsAnalyzerContext() = apply { checkState(State.BUILDING) }

    // ConstraintSystemOperation
    override fun registerVariable(variable: TypeVariableMarker) {
        checkState(State.BUILDING, State.COMPLETION, State.TRANSACTION)

        transactionRegisterVariable(variable)
        storage.allTypeVariables.put(variable.freshTypeConstructor(), variable)
            ?.let { error("Type variable already registered: old: $it, new: $variable") }
        notProperTypesCache.clear()
        storage.notFixedTypeVariables[variable.freshTypeConstructor()] = MutableVariableWithConstraints(this, variable)
    }

    override fun markPostponedVariable(variable: TypeVariableMarker) {
        storage.postponedTypeVariables += variable
    }

    override fun markCouldBeResolvedWithUnrestrictedBuilderInference() {
        couldBeResolvedWithUnrestrictedBuilderInference = true
    }

    override fun couldBeResolvedWithUnrestrictedBuilderInference() =
        couldBeResolvedWithUnrestrictedBuilderInference

    override fun unmarkPostponedVariable(variable: TypeVariableMarker) {
        storage.postponedTypeVariables -= variable
    }

    override fun removePostponedVariables() {
        storage.postponedTypeVariables.clear()
    }

    override fun substituteFixedVariables(substitutor: TypeSubstitutorMarker) {
        storage.fixedTypeVariables.replaceAll { _, type -> substitutor.safeSubstitute(type) }
    }

    override fun putBuiltFunctionalExpectedTypeForPostponedArgument(
        topLevelVariable: TypeConstructorMarker,
        pathToExpectedType: List<Pair<TypeConstructorMarker, Int>>,
        builtFunctionalType: KotlinTypeMarker
    ) {
        storage.builtFunctionalTypesForPostponedArgumentsByTopLevelTypeVariables[topLevelVariable to pathToExpectedType] = builtFunctionalType
    }

    override fun putBuiltFunctionalExpectedTypeForPostponedArgument(
        expectedTypeVariable: TypeConstructorMarker,
        builtFunctionalType: KotlinTypeMarker
    ) {
        storage.builtFunctionalTypesForPostponedArgumentsByExpectedTypeVariables[expectedTypeVariable] = builtFunctionalType
    }

    override fun getBuiltFunctionalExpectedTypeForPostponedArgument(
        topLevelVariable: TypeConstructorMarker,
        pathToExpectedType: List<Pair<TypeConstructorMarker, Int>>
    ) = storage.builtFunctionalTypesForPostponedArgumentsByTopLevelTypeVariables[topLevelVariable to pathToExpectedType]

    override fun getBuiltFunctionalExpectedTypeForPostponedArgument(expectedTypeVariable: TypeConstructorMarker) =
        storage.builtFunctionalTypesForPostponedArgumentsByExpectedTypeVariables[expectedTypeVariable]

    override fun addSubtypeConstraint(lowerType: KotlinTypeMarker, upperType: KotlinTypeMarker, position: ConstraintPosition) =
        constraintInjector.addInitialSubtypeConstraint(
            apply { checkState(State.BUILDING, State.COMPLETION, State.TRANSACTION) },
            lowerType,
            upperType,
            position
        )

    override fun addEqualityConstraint(a: KotlinTypeMarker, b: KotlinTypeMarker, position: ConstraintPosition) =
        constraintInjector.addInitialEqualityConstraint(
            apply { checkState(State.BUILDING, State.COMPLETION, State.TRANSACTION) },
            a,
            b,
            position
        )

    override fun getProperSuperTypeConstructors(type: KotlinTypeMarker): List<TypeConstructorMarker> {
        checkState(State.BUILDING, State.COMPLETION, State.TRANSACTION)
        konst variableWithConstraints = notFixedTypeVariables[type.typeConstructor()] ?: return listOf(type.typeConstructor())

        return variableWithConstraints.constraints.mapNotNull {
            if (it.kind == ConstraintKind.LOWER) return@mapNotNull null
            it.type.typeConstructor().takeUnless { allTypeVariables.containsKey(it) }
        }
    }

    // ConstraintSystemBuilder
    private fun transactionRegisterVariable(variable: TypeVariableMarker) {
        if (state != State.TRANSACTION) return
        typeVariablesTransaction.add(variable)
    }

    private fun closeTransaction(beforeState: State, beforeTypeVariables: Int) {
        checkState(State.TRANSACTION)
        typeVariablesTransaction.trimToSize(beforeTypeVariables)
        state = beforeState
    }

    private inner class TransactionState(
        private konst beforeState: State,
        private konst beforeInitialConstraintCount: Int,
        private konst beforeErrorsCount: Int,
        private konst beforeMaxTypeDepthFromInitialConstraints: Int,
        private konst beforeTypeVariablesTransactionSize: Int,
        private konst beforeMissedConstraintsCount: Int,
        private konst beforeConstraintCountByVariables: Map<TypeConstructorMarker, Int>,
        private konst beforeConstraintsFromAllForks: Int,
    ) : ConstraintSystemTransaction() {
        override fun closeTransaction() {
            checkState(State.TRANSACTION)
            typeVariablesTransaction.trimToSize(beforeTypeVariablesTransactionSize)
            state = beforeState
        }

        override fun rollbackTransaction() {
            for (addedTypeVariable in typeVariablesTransaction.subList(beforeTypeVariablesTransactionSize, typeVariablesTransaction.size)) {
                storage.allTypeVariables.remove(addedTypeVariable.freshTypeConstructor())
                storage.notFixedTypeVariables.remove(addedTypeVariable.freshTypeConstructor())
            }
            storage.maxTypeDepthFromInitialConstraints = beforeMaxTypeDepthFromInitialConstraints
            storage.errors.trimToSize(beforeErrorsCount)
            storage.missedConstraints.trimToSize(beforeMissedConstraintsCount)
            storage.constraintsFromAllForkPoints.trimToSize(beforeConstraintsFromAllForks)

            konst addedInitialConstraints = storage.initialConstraints.subList(
                beforeInitialConstraintCount,
                storage.initialConstraints.size
            )

            for (variableWithConstraint in storage.notFixedTypeVariables.konstues) {
                konst sinceIndexToRemoveConstraints =
                    beforeConstraintCountByVariables[variableWithConstraint.typeVariable.freshTypeConstructor()]
                if (sinceIndexToRemoveConstraints != null) {
                    variableWithConstraint.removeLastConstraints(sinceIndexToRemoveConstraints)
                }
            }

            addedInitialConstraints.clear() // remove constraint from storage.initialConstraints
            closeTransaction(beforeState, beforeTypeVariablesTransactionSize)
        }
    }

    override fun prepareTransaction(): ConstraintSystemTransaction {
        checkState(State.BUILDING, State.COMPLETION, State.TRANSACTION)
        return TransactionState(
            beforeState = state,
            beforeInitialConstraintCount = storage.initialConstraints.size,
            beforeErrorsCount = storage.errors.size,
            beforeMaxTypeDepthFromInitialConstraints = storage.maxTypeDepthFromInitialConstraints,
            beforeTypeVariablesTransactionSize = typeVariablesTransaction.size,
            beforeMissedConstraintsCount = storage.missedConstraints.size,
            beforeConstraintCountByVariables = storage.notFixedTypeVariables.mapValues { it.konstue.rawConstraintsCount },
            beforeConstraintsFromAllForks = storage.constraintsFromAllForkPoints.size,
        ).also {
            state = State.TRANSACTION
        }
    }

    // ConstraintSystemBuilder, KotlinConstraintSystemCompleter.Context
    override konst hasContradiction: Boolean
        get() = storage.hasContradiction.also {
            checkState(
                State.FREEZED,
                State.BUILDING,
                State.COMPLETION,
                State.TRANSACTION
            )
        }

    override fun addOtherSystem(otherSystem: ConstraintStorage) {
        if (otherSystem.allTypeVariables.isNotEmpty()) {
            otherSystem.allTypeVariables.forEach {
                transactionRegisterVariable(it.konstue)
            }
            storage.allTypeVariables.putAll(otherSystem.allTypeVariables)
            notProperTypesCache.clear()
        }
        for ((variable, constraints) in otherSystem.notFixedTypeVariables) {
            notFixedTypeVariables[variable] = MutableVariableWithConstraints(this, constraints)
        }
        storage.initialConstraints.addAll(otherSystem.initialConstraints)
        storage.maxTypeDepthFromInitialConstraints =
            max(storage.maxTypeDepthFromInitialConstraints, otherSystem.maxTypeDepthFromInitialConstraints)
        storage.errors.addAll(otherSystem.errors)
        storage.fixedTypeVariables.putAll(otherSystem.fixedTypeVariables)
        storage.postponedTypeVariables.addAll(otherSystem.postponedTypeVariables)
        storage.constraintsFromAllForkPoints.addAll(otherSystem.constraintsFromAllForkPoints)
    }

    // ResultTypeResolver.Context, ConstraintSystemBuilder
    override fun isProperType(type: KotlinTypeMarker): Boolean {
        checkState(State.BUILDING, State.COMPLETION, State.TRANSACTION)
        if (storage.allTypeVariables.isEmpty()) return true
        if (notProperTypesCache.contains(type)) return false
        if (properTypesCache.contains(type)) return true
        return isProperTypeImpl(type).also {
            (if (it) properTypesCache else notProperTypesCache).add(type)
        }
    }

    private fun isProperTypeImpl(type: KotlinTypeMarker): Boolean =
        !type.contains {
            konst capturedType = it.asSimpleType()?.asCapturedType()

            konst typeToCheck = if (capturedType is CapturedTypeMarker && capturedType.captureStatus() == CaptureStatus.FROM_EXPRESSION)
                capturedType.typeConstructorProjection().takeUnless { projection -> projection.isStarProjection() }?.getType()
            else
                it

            if (typeToCheck == null) return@contains false

            storage.allTypeVariables.containsKey(typeToCheck.typeConstructor())
        }

    override fun isTypeVariable(type: KotlinTypeMarker): Boolean {
        checkState(State.BUILDING, State.COMPLETION, State.TRANSACTION)
        return notFixedTypeVariables.containsKey(type.typeConstructor())
    }

    override fun isPostponedTypeVariable(typeVariable: TypeVariableMarker): Boolean {
        checkState(State.BUILDING, State.COMPLETION, State.TRANSACTION)
        return typeVariable in postponedTypeVariables
    }

    // ConstraintInjector.Context, KotlinConstraintSystemCompleter.Context
    override konst allTypeVariables: Map<TypeConstructorMarker, TypeVariableMarker>
        get() {
            checkState(State.BUILDING, State.COMPLETION, State.TRANSACTION)
            return storage.allTypeVariables
        }

    override var maxTypeDepthFromInitialConstraints: Int
        get() = storage.maxTypeDepthFromInitialConstraints
        set(konstue) {
            checkState(State.BUILDING, State.COMPLETION, State.TRANSACTION)
            storage.maxTypeDepthFromInitialConstraints = konstue
        }

    override fun addInitialConstraint(initialConstraint: InitialConstraint) {
        checkState(State.BUILDING, State.COMPLETION, State.TRANSACTION)
        storage.initialConstraints.add(initialConstraint)
    }

    // ConstraintInjector.Context, FixationOrderCalculator.Context
    override konst notFixedTypeVariables: MutableMap<TypeConstructorMarker, MutableVariableWithConstraints>
        get() {
            checkState(State.BUILDING, State.COMPLETION, State.TRANSACTION)
            return storage.notFixedTypeVariables
        }

    override konst fixedTypeVariables: MutableMap<TypeConstructorMarker, KotlinTypeMarker>
        get() {
            checkState(State.BUILDING, State.COMPLETION, State.TRANSACTION)
            return storage.fixedTypeVariables
        }

    override konst postponedTypeVariables: List<TypeVariableMarker>
        get() {
            checkState(State.BUILDING, State.COMPLETION, State.TRANSACTION)
            return storage.postponedTypeVariables
        }

    override konst constraintsFromAllForkPoints: MutableList<Pair<IncorporationConstraintPosition, ForkPointData>>
        get() {
            checkState(State.BUILDING, State.COMPLETION, State.TRANSACTION)
            return storage.constraintsFromAllForkPoints
        }

    /**
     * This function tries to find the solution (set of constraints) that is consistent with some branch of each fork
     * And those constraints are being immediately applied to the system
     */
    override fun resolveForkPointsConstraints() {
        if (constraintsFromAllForkPoints.isEmpty()) return
        konst allForkPointsData = constraintsFromAllForkPoints.toList()
        constraintsFromAllForkPoints.clear()

        // There may be multiple fork points:
        // - One from subtyping A<Int> & A<T> <: A<Xv>
        // - Another one from B<String> & B<F> <: B<Yv>
        // Each of them defines two sets of constraints, e.g. for the first for point:
        // 1. {Xv=Int} – is a one-element set (but potentially there might be more constraints in the set)
        // 2. {Xv=T} – second constraints set
        for ((position, forkPointData) in allForkPointsData) {
            if (!applyConstraintsFromFirstSuccessfulBranchOfTheFork(forkPointData, position)) {
                addError(NoSuccessfulFork(position))
            }
        }
    }

    /**
     * Checks if current state of forked constraints is not contradictory.
     *
     * That function is expected to be pure, i.e. it should leave the system in the same state it was found before the call.
     *
     * @return null if for each fork we found a possible branch that doesn't contradict with all other constraints
     * @return non-nullable error if there's a contradiction we didn't manage to resolve
     */
    fun checkIfForksMightBeSuccessfullyResolved(): ConstraintSystemError? {
        if (constraintsFromAllForkPoints.isEmpty()) return null

        konst allForkPointsData = constraintsFromAllForkPoints.toList()
        constraintsFromAllForkPoints.clear()

        var result: ConstraintSystemError? = null
        runTransaction {
            for ((position, forkPointData) in allForkPointsData) {
                if (!applyConstraintsFromFirstSuccessfulBranchOfTheFork(forkPointData, position)) {
                    result = NoSuccessfulFork(position)
                    break
                }
            }

            false
        }

        constraintsFromAllForkPoints.addAll(allForkPointsData)

        return result
    }

    /**
     * @return true if there is a successful constraints set for the fork
     */
    private fun applyConstraintsFromFirstSuccessfulBranchOfTheFork(
        forkPointData: ForkPointData,
        position: IncorporationConstraintPosition
    ): Boolean {
        return forkPointData.any { constraintSetForForkBranch ->
            runTransaction {
                constraintInjector.processGivenForkPointBranchConstraints(
                    this@NewConstraintSystemImpl.apply { checkState(State.BUILDING, State.COMPLETION, State.TRANSACTION) },
                    constraintSetForForkBranch,
                    position,
                )

                if (constraintsFromAllForkPoints.isNotEmpty()) {
                    resolveForkPointsConstraints()
                }

                !hasContradiction
            }
        }
    }

    // ConstraintInjector.Context, KotlinConstraintSystemCompleter.Context
    override fun addError(error: ConstraintSystemError) {
        checkState(State.BUILDING, State.COMPLETION, State.TRANSACTION)
        storage.errors.add(error)
    }

    // KotlinConstraintSystemCompleter.Context
    override fun fixVariable(
        variable: TypeVariableMarker,
        resultType: KotlinTypeMarker,
        position: FixVariableConstraintPosition<*>
    ) = with(utilContext) {
        checkState(State.BUILDING, State.COMPLETION)

        checkInferredEmptyIntersection(variable, resultType)

        constraintInjector.addInitialEqualityConstraint(this@NewConstraintSystemImpl, variable.defaultType(), resultType, position)

        /*
         * Checking missed constraint can introduce new type mismatch warnings.
         * It's needed to deprecate green code which works only due to incorrect optimization in the constraint injector.
         * TODO: remove this code (and `substituteMissedConstraints`) with removing `ProperTypeInferenceConstraintsProcessing` feature
         */
        checkMissedConstraints()

        konst freshTypeConstructor = variable.freshTypeConstructor()
        konst variableWithConstraints = notFixedTypeVariables.remove(freshTypeConstructor)

        for (otherVariableWithConstraints in notFixedTypeVariables.konstues) {
            otherVariableWithConstraints.removeConstrains { containsTypeVariable(it.type, freshTypeConstructor) }
        }

        storage.fixedTypeVariables[freshTypeConstructor] = resultType

        // Substitute freshly fixed type variable into missed constraints
        substituteMissedConstraints()

        postponeOnlyInputTypesCheck(variableWithConstraints, resultType)

        doPostponedComputationsIfAllVariablesAreFixed()
    }

    override fun getEmptyIntersectionTypeKind(types: Collection<KotlinTypeMarker>): EmptyIntersectionTypeInfo? {
        if (types in intersectionTypesCache)
            return intersectionTypesCache.getValue(types)

        return computeEmptyIntersectionTypeKind(types).also {
            intersectionTypesCache[types] = it
        }
    }

    private fun checkInferredEmptyIntersection(variable: TypeVariableMarker, resultType: KotlinTypeMarker) {
        konst intersectionTypeConstructor = resultType.typeConstructor().takeIf { it is IntersectionTypeConstructorMarker } ?: return
        konst upperTypes = intersectionTypeConstructor.supertypes()

        // Diagnostic with these incompatible types has already been reported at the resolution stage
        if (upperTypes.size <= 1 || storage.errors.any { it is InferredEmptyIntersection && it.incompatibleTypes == upperTypes })
            return

        konst emptyIntersectionTypeInfo = getEmptyIntersectionTypeKind(upperTypes) ?: return

        // Remove existing errors from the resolution stage because a completion stage error is always more precise
        storage.errors.removeIf { it is InferredEmptyIntersection }

        konst isInferredEmptyIntersectionForbidden =
            languageVersionSettings.supportsFeature(LanguageFeature.ForbidInferringTypeVariablesIntoEmptyIntersection)
        konst errorFactory = if (emptyIntersectionTypeInfo.kind.isDefinitelyEmpty && isInferredEmptyIntersectionForbidden)
            ::InferredEmptyIntersectionError
        else ::InferredEmptyIntersectionWarning

        addError(
            errorFactory(upperTypes.toList(), emptyIntersectionTypeInfo.casingTypes.toList(), variable, emptyIntersectionTypeInfo.kind)
        )
    }

    private fun checkMissedConstraints() {
        konst constraintSystem = this@NewConstraintSystemImpl
        konst errorsByMissedConstraints = buildList {
            runTransaction {
                for ((position, constraints) in storage.missedConstraints) {
                    konst fixedVariableConstraints =
                        constraints.filter { (typeVariable, _) -> typeVariable.freshTypeConstructor() in notFixedTypeVariables }
                    constraintInjector.processMissedConstraints(constraintSystem, position, fixedVariableConstraints)
                }
                errors.filterIsInstance<NewConstraintError>().forEach(::add)
                false
            }
        }
        konst constraintErrors = constraintSystem.errors.filterIsInstance<NewConstraintError>()
        // Don't report warning if an error on the same call has already been reported
        if (constraintErrors.isEmpty()) {
            errorsByMissedConstraints.forEach {
                constraintSystem.addError(it.transformToWarning())
            }
        }
    }

    private fun substituteMissedConstraints() {
        konst substitutor = buildCurrentSubstitutor()
        for ((_, constraints) in storage.missedConstraints) {
            for ((index, variableWithConstraint) in constraints.withIndex()) {
                konst (typeVariable, constraint) = variableWithConstraint
                constraints[index] = typeVariable to constraint.replaceType(substitutor.safeSubstitute(constraint.type))
            }
        }
    }

    private fun ConstraintSystemUtilContext.postponeOnlyInputTypesCheck(
        variableWithConstraints: MutableVariableWithConstraints?,
        resultType: KotlinTypeMarker
    ) {
        if (variableWithConstraints != null && variableWithConstraints.typeVariable.hasOnlyInputTypesAttribute()) {
            postponedComputationsAfterAllVariablesAreFixed.add { checkOnlyInputTypesAnnotation(variableWithConstraints, resultType) }
        }
    }

    private fun doPostponedComputationsIfAllVariablesAreFixed() {
        if (notFixedTypeVariables.isEmpty()) {
            postponedComputationsAfterAllVariablesAreFixed.forEach { it() }
        }
    }

    private fun KotlinTypeMarker.substituteAndApproximateIfNecessary(
        substitutor: TypeSubstitutorMarker,
        approximator: AbstractTypeApproximator,
        constraintKind: ConstraintKind
    ): KotlinTypeMarker {
        konst doesInputTypeContainsOtherVariables = this.contains { it.typeConstructor() is TypeVariableTypeConstructorMarker }
        konst substitutedType = if (doesInputTypeContainsOtherVariables) substitutor.safeSubstitute(this) else this
        // Appoximation here is the same as ResultTypeResolver do
        konst approximatedType = when (constraintKind) {
            ConstraintKind.LOWER ->
                approximator.approximateToSuperType(substitutedType, TypeApproximatorConfiguration.InternalTypesApproximation)
            ConstraintKind.UPPER ->
                approximator.approximateToSubType(substitutedType, TypeApproximatorConfiguration.InternalTypesApproximation)
            ConstraintKind.EQUALITY -> substitutedType
        } ?: substitutedType

        return approximatedType
    }

    private fun checkOnlyInputTypesAnnotation(variableWithConstraints: MutableVariableWithConstraints, resultType: KotlinTypeMarker) {
        konst substitutor = buildCurrentSubstitutor()
        konst approximator = constraintInjector.typeApproximator
        konst isResultTypeEqualSomeInputType =
            variableWithConstraints.getProjectedInputCallTypes(utilContext).any { (inputType, constraintKind) ->
                konst inputTypeConstructor = inputType.typeConstructor()
                konst otherResultType = inputType.substituteAndApproximateIfNecessary(substitutor, approximator, constraintKind)

                if (AbstractTypeChecker.equalTypes(this, resultType, otherResultType)) return@any true
                if (!inputTypeConstructor.isIntersection()) return@any false

                inputTypeConstructor.supertypes().any {
                    konst intersectionComponentResultType = it.substituteAndApproximateIfNecessary(substitutor, approximator, constraintKind)
                    AbstractTypeChecker.equalTypes(this, resultType, intersectionComponentResultType)
                }
            }
        if (!isResultTypeEqualSomeInputType) {
            addError(OnlyInputTypesDiagnostic(variableWithConstraints.typeVariable))
        }
    }

    // KotlinConstraintSystemCompleter.Context, PostponedArgumentsAnalyzer.Context
    override fun canBeProper(type: KotlinTypeMarker): Boolean {
        checkState(State.BUILDING, State.COMPLETION)
        return !type.contains { storage.notFixedTypeVariables.containsKey(it.typeConstructor()) }
    }

    override fun containsOnlyFixedOrPostponedVariables(type: KotlinTypeMarker): Boolean {
        checkState(State.BUILDING, State.COMPLETION)
        return !type.contains {
            konst typeConstructor = it.typeConstructor()
            konst variable = storage.notFixedTypeVariables[typeConstructor]?.typeVariable
            variable !in storage.postponedTypeVariables && storage.notFixedTypeVariables.containsKey(typeConstructor)
        }
    }

    override fun containsOnlyFixedVariables(type: KotlinTypeMarker): Boolean {
        checkState(State.BUILDING, State.COMPLETION)
        return !type.contains {
            konst typeConstructor = it.typeConstructor()
            storage.notFixedTypeVariables.containsKey(typeConstructor)
        }
    }

    // PostponedArgumentsAnalyzer.Context
    override fun buildCurrentSubstitutor(): TypeSubstitutorMarker {
        checkState(State.BUILDING, State.COMPLETION, State.TRANSACTION)
        return buildCurrentSubstitutor(emptyMap())
    }

    override fun buildCurrentSubstitutor(additionalBindings: Map<TypeConstructorMarker, StubTypeMarker>): TypeSubstitutorMarker {
        checkState(State.BUILDING, State.COMPLETION, State.TRANSACTION)
        return storage.buildCurrentSubstitutor(this, additionalBindings)
    }

    override fun buildNotFixedVariablesToStubTypesSubstitutor(): TypeSubstitutorMarker {
        checkState(State.BUILDING, State.COMPLETION, State.TRANSACTION)
        return storage.buildNotFixedVariablesToNonSubtypableTypesSubstitutor(this)
    }

    // ResultTypeResolver.Context, VariableFixationFinder.Context
    override fun isReified(variable: TypeVariableMarker): Boolean {
        return with(utilContext) { variable.isReified() }
    }

    override fun bindingStubsForPostponedVariables(): Map<TypeVariableMarker, StubTypeMarker> {
        checkState(State.BUILDING, State.COMPLETION)
        // TODO: SUB
        return storage.postponedTypeVariables.associateWith { createStubTypeForBuilderInference(it) }
    }

    override fun currentStorage(): ConstraintStorage {
        checkState(State.BUILDING, State.COMPLETION, State.TRANSACTION)
        return storage
    }

    // PostponedArgumentsAnalyzer.Context
    override fun hasUpperOrEqualUnitConstraint(type: KotlinTypeMarker): Boolean {
        checkState(State.BUILDING, State.COMPLETION, State.FREEZED)
        konst constraints = storage.notFixedTypeVariables[type.typeConstructor()]?.constraints ?: return false
        return constraints.any {
            (it.kind == ConstraintKind.UPPER || it.kind == ConstraintKind.EQUALITY) &&
                    it.type.lowerBoundIfFlexible().isUnit()
        }
    }

    override fun removePostponedTypeVariablesFromConstraints(postponedTypeVariables: Set<TypeConstructorMarker>) {
        for ((_, variableWithConstraints) in storage.notFixedTypeVariables) {
            variableWithConstraints.removeConstrains { constraint ->
                constraint.type.contains { it is StubTypeMarker && it.getOriginalTypeVariable() in postponedTypeVariables }
            }
        }
    }
}
