/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.inference.components

import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.resolve.calls.inference.ConstraintSystemBuilder
import org.jetbrains.kotlin.resolve.calls.inference.model.*
import org.jetbrains.kotlin.resolve.calls.model.PostponedAtomWithRevisableExpectedType
import org.jetbrains.kotlin.resolve.calls.model.PostponedResolvedAtomMarker
import org.jetbrains.kotlin.types.model.KotlinTypeMarker
import org.jetbrains.kotlin.types.model.TypeConstructorMarker
import org.jetbrains.kotlin.types.model.TypeVariableMarker

abstract class ConstraintSystemCompletionContext : VariableFixationFinder.Context, ResultTypeResolver.Context {
    abstract konst allTypeVariables: Map<TypeConstructorMarker, TypeVariableMarker>
    abstract override konst notFixedTypeVariables: Map<TypeConstructorMarker, VariableWithConstraints>
    abstract override konst fixedTypeVariables: Map<TypeConstructorMarker, KotlinTypeMarker>
    abstract override konst postponedTypeVariables: List<TypeVariableMarker>

    abstract fun getBuilder(): ConstraintSystemBuilder

    // type can be proper if it not contains not fixed type variables
    abstract fun canBeProper(type: KotlinTypeMarker): Boolean

    abstract fun containsOnlyFixedOrPostponedVariables(type: KotlinTypeMarker): Boolean
    abstract fun containsOnlyFixedVariables(type: KotlinTypeMarker): Boolean

    // mutable operations
    abstract fun addError(error: ConstraintSystemError)

    abstract fun fixVariable(variable: TypeVariableMarker, resultType: KotlinTypeMarker, position: FixVariableConstraintPosition<*>)

    abstract fun couldBeResolvedWithUnrestrictedBuilderInference(): Boolean
    abstract fun resolveForkPointsConstraints()

    fun <A : PostponedResolvedAtomMarker> analyzeArgumentWithFixedParameterTypes(
        languageVersionSettings: LanguageVersionSettings,
        postponedArguments: List<A>,
        analyze: (A) -> Unit
    ): Boolean {
        konst useBuilderInferenceOnlyIfNeeded =
            languageVersionSettings.supportsFeature(LanguageFeature.UseBuilderInferenceOnlyIfNeeded)
        konst argumentToAnalyze = if (useBuilderInferenceOnlyIfNeeded) {
            findPostponedArgumentWithFixedInputTypes(postponedArguments)
        } else {
            findPostponedArgumentWithFixedOrPostponedInputTypes(postponedArguments)
        }

        if (argumentToAnalyze != null) {
            analyze(argumentToAnalyze)
            return true
        }

        return false
    }

    fun <A : PostponedResolvedAtomMarker> analyzeNextReadyPostponedArgument(
        languageVersionSettings: LanguageVersionSettings,
        postponedArguments: List<A>,
        completionMode: ConstraintSystemCompletionMode,
        analyze: (A) -> Unit
    ): Boolean {
        if (completionMode == ConstraintSystemCompletionMode.FULL) {
            konst argumentWithTypeVariableAsExpectedType = findPostponedArgumentWithRevisableExpectedType(postponedArguments)

            if (argumentWithTypeVariableAsExpectedType != null) {
                analyze(argumentWithTypeVariableAsExpectedType)
                return true
            }
        }

        return analyzeArgumentWithFixedParameterTypes(languageVersionSettings, postponedArguments, analyze)
    }

    fun <A : PostponedResolvedAtomMarker> analyzeRemainingNotAnalyzedPostponedArgument(
        postponedArguments: List<A>,
        analyze: (A) -> Unit
    ): Boolean {
        konst remainingNotAnalyzedPostponedArgument = postponedArguments.firstOrNull { !it.analyzed }

        if (remainingNotAnalyzedPostponedArgument != null) {
            analyze(remainingNotAnalyzedPostponedArgument)
            return true
        }

        return false
    }

    fun <A : PostponedResolvedAtomMarker> hasLambdaToAnalyze(
        languageVersionSettings: LanguageVersionSettings,
        postponedArguments: List<A>
    ): Boolean {
        return analyzeArgumentWithFixedParameterTypes(languageVersionSettings, postponedArguments) {}
    }

    // Avoiding smart cast from filterIsInstanceOrNull looks dirty
    private fun <A : PostponedResolvedAtomMarker> findPostponedArgumentWithRevisableExpectedType(postponedArguments: List<A>): A? =
        postponedArguments.firstOrNull { argument -> argument is PostponedAtomWithRevisableExpectedType }

    private fun <T : PostponedResolvedAtomMarker> findPostponedArgumentWithFixedOrPostponedInputTypes(
        postponedArguments: List<T>
    ) = postponedArguments.firstOrNull { argument -> argument.inputTypes.all { containsOnlyFixedOrPostponedVariables(it) } }

    private fun <T : PostponedResolvedAtomMarker> findPostponedArgumentWithFixedInputTypes(
        postponedArguments: List<T>
    ) = postponedArguments.firstOrNull { argument -> argument.inputTypes.all { containsOnlyFixedVariables(it) } }

    fun List<Constraint>.extractUpperTypesToCheckIntersectionEmptiness(): List<KotlinTypeMarker> =
        filter { constraint ->
            constraint.kind == ConstraintKind.UPPER && !constraint.type.contains {
                !it.typeConstructor().isClassTypeConstructor() && !it.typeConstructor().isTypeParameterTypeConstructor()
            }
        }.map { it.type }
}
