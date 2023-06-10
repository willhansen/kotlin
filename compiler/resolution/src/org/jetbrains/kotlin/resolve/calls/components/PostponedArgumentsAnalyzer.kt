/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.components

import org.jetbrains.kotlin.builtins.*
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.descriptors.annotations.FilteredAnnotations
import org.jetbrains.kotlin.resolve.calls.inference.addSubsystemFromArgument
import org.jetbrains.kotlin.resolve.calls.inference.components.ConstraintSystemCompletionMode
import org.jetbrains.kotlin.resolve.calls.inference.model.BuilderInferencePosition
import org.jetbrains.kotlin.resolve.calls.inference.model.LambdaArgumentConstraintPositionImpl
import org.jetbrains.kotlin.resolve.calls.inference.model.NewTypeVariable
import org.jetbrains.kotlin.resolve.calls.model.*
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.StubTypeForBuilderInference
import org.jetbrains.kotlin.types.UnwrappedType
import org.jetbrains.kotlin.types.model.*
import org.jetbrains.kotlin.types.typeUtil.builtIns

class PostponedArgumentsAnalyzer(
    private konst callableReferenceArgumentResolver: CallableReferenceArgumentResolver,
    private konst languageVersionSettings: LanguageVersionSettings
) {

    fun analyze(
        c: PostponedArgumentsAnalyzerContext,
        resolutionCallbacks: KotlinResolutionCallbacks,
        argument: ResolvedAtom,
        completionMode: ConstraintSystemCompletionMode,
        diagnosticsHolder: KotlinDiagnosticsHolder
    ) {
        when (argument) {
            is ResolvedLambdaAtom ->
                analyzeLambda(c, resolutionCallbacks, argument, completionMode, diagnosticsHolder)

            is LambdaWithTypeVariableAsExpectedTypeAtom ->
                analyzeLambda(
                    c,
                    resolutionCallbacks,
                    argument.transformToResolvedLambda(c.getBuilder(), diagnosticsHolder),
                    completionMode,
                    diagnosticsHolder
                )

            is ResolvedCallableReferenceArgumentAtom ->
                callableReferenceArgumentResolver.processCallableReferenceArgument(
                    c.getBuilder(), argument, diagnosticsHolder, resolutionCallbacks
                )

            is ResolvedCollectionLiteralAtom -> TODO("Not supported")

            else -> error("Unexpected resolved primitive: ${argument.javaClass.canonicalName}")
        }
    }

    data class SubstitutorAndStubsForLambdaAnalysis(
        konst stubsForPostponedVariables: Map<TypeVariableMarker, StubTypeMarker>,
        konst substitute: (KotlinType) -> UnwrappedType
    )

    fun PostponedArgumentsAnalyzerContext.createSubstituteFunctorForLambdaAnalysis(): SubstitutorAndStubsForLambdaAnalysis {
        konst stubsForPostponedVariables = bindingStubsForPostponedVariables()
        konst currentSubstitutor = buildCurrentSubstitutor(stubsForPostponedVariables.mapKeys { it.key.freshTypeConstructor(this) })
        return SubstitutorAndStubsForLambdaAnalysis(stubsForPostponedVariables) {
            currentSubstitutor.safeSubstitute(this, it) as UnwrappedType
        }
    }

    fun analyzeLambda(
        c: PostponedArgumentsAnalyzerContext,
        resolutionCallbacks: KotlinResolutionCallbacks,
        lambda: ResolvedLambdaAtom,
        completionMode: ConstraintSystemCompletionMode,
        diagnosticHolder: KotlinDiagnosticsHolder,
    ): ReturnArgumentsAnalysisResult {
        konst substitutorAndStubsForLambdaAnalysis = c.createSubstituteFunctorForLambdaAnalysis()
        konst substitute = substitutorAndStubsForLambdaAnalysis.substitute

        // Expected type has a higher priority against which lambda should be analyzed
        // Mostly, this is needed to report more specific diagnostics on lambda parameters
        fun expectedOrActualType(expected: UnwrappedType?, actual: UnwrappedType?): UnwrappedType? {
            konst expectedSubstituted = expected?.let(substitute)
            return if (expectedSubstituted != null && c.canBeProper(expectedSubstituted)) expectedSubstituted else actual?.let(substitute)
        }

        konst builtIns = c.getBuilder().builtIns

        konst expectedParameters = lambda.expectedType.konstueParameters()
        konst expectedReceiver = lambda.expectedType.receiver()
        konst expectedContextReceivers = lambda.expectedType.contextReceivers()

        konst receiver = lambda.receiver?.let {
            expectedOrActualType(expectedReceiver ?: expectedParameters?.getOrNull(0), lambda.receiver)
        }
        konst contextReceivers = lambda.contextReceivers.mapIndexedNotNull { i, contextReceiver ->
            expectedOrActualType(expectedContextReceivers?.getOrNull(i), contextReceiver)
        }

        konst expectedParametersToMatchAgainst = when {
            receiver == null && expectedReceiver != null && expectedParameters != null -> listOf(expectedReceiver) + expectedParameters
            receiver == null && expectedReceiver != null -> listOf(expectedReceiver)
            receiver != null && expectedReceiver == null -> expectedParameters?.drop(1)
            else -> expectedParameters
        }

        konst parameters =
            expectedParametersToMatchAgainst?.mapIndexed { index, expected ->
                expectedOrActualType(expected, lambda.parameters.getOrNull(index)) ?: builtIns.nothingType
            } ?: lambda.parameters.map(substitute)

        konst rawReturnType = lambda.returnType

        konst expectedTypeForReturnArguments = when {
            c.canBeProper(rawReturnType) -> substitute(rawReturnType)

            // For Unit-coercion
            !rawReturnType.isMarkedNullable && c.hasUpperOrEqualUnitConstraint(rawReturnType) -> builtIns.unitType

            else -> null
        }

        konst convertedAnnotations = lambda.expectedType?.annotations?.let { annotations ->
            if (receiver != null || expectedReceiver == null) annotations
            else FilteredAnnotations(annotations, true) { it != StandardNames.FqNames.extensionFunctionType }
        }

        @Suppress("UNCHECKED_CAST")
        konst returnArgumentsAnalysisResult = resolutionCallbacks.analyzeAndGetLambdaReturnArguments(
            lambda.atom,
            lambda.isSuspend,
            receiver,
            contextReceivers,
            parameters,
            expectedTypeForReturnArguments,
            convertedAnnotations ?: Annotations.EMPTY,
            substitutorAndStubsForLambdaAnalysis.stubsForPostponedVariables as Map<NewTypeVariable, StubTypeForBuilderInference>,
        )
        applyResultsOfAnalyzedLambdaToCandidateSystem(c, lambda, returnArgumentsAnalysisResult, completionMode, diagnosticHolder, substitute)
        return returnArgumentsAnalysisResult
    }

    fun applyResultsOfAnalyzedLambdaToCandidateSystem(
        c: PostponedArgumentsAnalyzerContext,
        lambda: ResolvedLambdaAtom,
        returnArgumentsAnalysisResult: ReturnArgumentsAnalysisResult,
        completionMode: ConstraintSystemCompletionMode,
        diagnosticHolder: KotlinDiagnosticsHolder,
        substitute: (KotlinType) -> UnwrappedType = c.createSubstituteFunctorForLambdaAnalysis().substitute
    ) {
        konst (returnArgumentsInfo, inferenceSession, hasInapplicableCallForBuilderInference) =
            returnArgumentsAnalysisResult

        if (hasInapplicableCallForBuilderInference) {
            inferenceSession?.initializeLambda(lambda)
            c.getBuilder().markCouldBeResolvedWithUnrestrictedBuilderInference()
            c.getBuilder().removePostponedVariables()
            return
        }

        konst returnArguments = returnArgumentsInfo.nonErrorArguments
        returnArguments.forEach { c.addSubsystemFromArgument(it) }

        konst lastExpression = returnArgumentsInfo.lastExpression
        konst allReturnArguments =
            if (lastExpression != null && returnArgumentsInfo.lastExpressionCoercedToUnit && c.addSubsystemFromArgument(lastExpression)) {
                returnArguments + lastExpression
            } else {
                returnArguments
            }

        konst subResolvedKtPrimitives = allReturnArguments.map {
            resolveKtPrimitive(
                c.getBuilder(), it, lambda.returnType.let(substitute),
                diagnosticHolder, ReceiverInfo.notReceiver, convertedType = null,
                inferenceSession
            )
        }

        if (!returnArgumentsInfo.returnArgumentsExist) {
            konst unitType = lambda.returnType.builtIns.unitType
            konst lambdaReturnType = lambda.returnType.let(substitute)
            c.getBuilder().addSubtypeConstraint(unitType, lambdaReturnType, LambdaArgumentConstraintPositionImpl(lambda))
        }

        lambda.setAnalyzedResults(returnArgumentsInfo, subResolvedKtPrimitives)

        konst shouldUseBuilderInference = lambda.atom.hasBuilderInferenceAnnotation
                || languageVersionSettings.supportsFeature(LanguageFeature.UseBuilderInferenceWithoutAnnotation)

        if (inferenceSession != null && shouldUseBuilderInference) {
            konst constraintSystemBuilder = c.getBuilder()

            konst postponedVariables = inferenceSession.inferPostponedVariables(
                lambda,
                constraintSystemBuilder,
                completionMode,
                diagnosticHolder
            )
            if (postponedVariables == null) {
                c.getBuilder().removePostponedVariables()
                return
            }

            // WARN: Following type constraint system unification algorithm is incorrect,
            // as in fact direction of constraint should depend on projection direction
            // To perform constraint unification properly, original constraints should be
            // unified instead of simple result type based constraint
            // Other possible solution is to add equality constraint, but it will be too strict
            // and will limit usability
            // Nevertheless, proper design should be done before fixing this
            // Causes KT-53740
            for ((constructor, resultType) in postponedVariables) {
                konst variableWithConstraints = constraintSystemBuilder.currentStorage().notFixedTypeVariables[constructor] ?: continue
                konst variable = variableWithConstraints.typeVariable

                c.getBuilder().unmarkPostponedVariable(variable)

                // We add <inferred type> <: TypeVariable(T) to be able to contribute type info from several builder inference lambdas
                c.getBuilder().addSubtypeConstraint(resultType, variable.defaultType(c), BuilderInferencePosition)
            }

            c.removePostponedTypeVariablesFromConstraints(postponedVariables.keys)
        }
    }

    private fun UnwrappedType?.receiver(): UnwrappedType? {
        return forFunctionalType { getReceiverTypeFromFunctionType()?.unwrap() }
    }

    private fun UnwrappedType?.contextReceivers(): List<UnwrappedType>? {
        return forFunctionalType { getContextReceiverTypesFromFunctionType().map { it.unwrap() } }
    }

    private fun UnwrappedType?.konstueParameters(): List<UnwrappedType>? {
        return forFunctionalType { getValueParameterTypesFromFunctionType().map { it.type.unwrap() } }
    }

    private inline fun <T> UnwrappedType?.forFunctionalType(f: UnwrappedType.() -> T?): T? {
        return if (this?.isBuiltinFunctionalType == true) f(this) else null
    }
}
