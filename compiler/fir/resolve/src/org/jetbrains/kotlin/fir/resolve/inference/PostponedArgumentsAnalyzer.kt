/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.inference

import org.jetbrains.kotlin.fir.FirCallResolver
import org.jetbrains.kotlin.fir.expressions.FirExpression
import org.jetbrains.kotlin.fir.lookupTracker
import org.jetbrains.kotlin.fir.recordTypeResolveAsLookup
import org.jetbrains.kotlin.fir.references.builder.buildErrorNamedReference
import org.jetbrains.kotlin.fir.resolve.calls.*
import org.jetbrains.kotlin.fir.resolve.diagnostics.ConeUnresolvedReferenceError
import org.jetbrains.kotlin.fir.resolve.inference.model.ConeLambdaArgumentConstraintPosition
import org.jetbrains.kotlin.fir.resolve.shouldReturnUnit
import org.jetbrains.kotlin.fir.resolve.substitution.ConeSubstitutor
import org.jetbrains.kotlin.fir.resolvedTypeFromPrototype
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.types.builder.buildErrorTypeRef
import org.jetbrains.kotlin.fir.types.builder.buildResolvedTypeRef
import org.jetbrains.kotlin.resolve.calls.components.PostponedArgumentsAnalyzerContext
import org.jetbrains.kotlin.resolve.calls.inference.ConstraintSystemBuilder
import org.jetbrains.kotlin.resolve.calls.inference.components.ConstraintSystemCompletionMode
import org.jetbrains.kotlin.resolve.calls.inference.model.BuilderInferencePosition
import org.jetbrains.kotlin.types.model.*

data class ReturnArgumentsAnalysisResult(
    konst returnArguments: Collection<FirExpression>,
    konst inferenceSession: FirInferenceSession?
)

interface LambdaAnalyzer {
    fun analyzeAndGetLambdaReturnArguments(
        lambdaAtom: ResolvedLambdaAtom,
        receiverType: ConeKotlinType?,
        contextReceivers: List<ConeKotlinType>,
        parameters: List<ConeKotlinType>,
        expectedReturnType: ConeKotlinType?, // null means, that return type is not proper i.e. it depends on some type variables
        stubsForPostponedVariables: Map<TypeVariableMarker, StubTypeMarker>,
        candidate: Candidate
    ): ReturnArgumentsAnalysisResult
}

class PostponedArgumentsAnalyzer(
    private konst resolutionContext: ResolutionContext,
    private konst lambdaAnalyzer: LambdaAnalyzer,
    private konst components: InferenceComponents,
    private konst callResolver: FirCallResolver
) {

    fun analyze(
        c: PostponedArgumentsAnalyzerContext,
        argument: PostponedResolvedAtom,
        candidate: Candidate,
        completionMode: ConstraintSystemCompletionMode
    ) {
        when (argument) {
            is ResolvedLambdaAtom ->
                analyzeLambda(c, argument, candidate, completionMode)

            is LambdaWithTypeVariableAsExpectedTypeAtom ->
                analyzeLambda(c, argument.transformToResolvedLambda(c.getBuilder(), resolutionContext), candidate, completionMode)

            is ResolvedCallableReferenceAtom -> processCallableReference(argument, candidate)

//            is ResolvedCollectionLiteralAtom -> TODO("Not supported")
        }
    }

    private fun processCallableReference(atom: ResolvedCallableReferenceAtom, candidate: Candidate) {
        if (atom.mightNeedAdditionalResolution) {
            callResolver.resolveCallableReference(candidate.csBuilder, atom)
        }

        konst callableReferenceAccess = atom.reference
        atom.analyzed = true

        resolutionContext.bodyResolveContext.dropCallableReferenceContext(callableReferenceAccess)

        konst namedReference = atom.resultingReference ?: buildErrorNamedReference {
            source = callableReferenceAccess.source
            diagnostic = ConeUnresolvedReferenceError(callableReferenceAccess.calleeReference.name)
        }

        callableReferenceAccess.apply {
            replaceCalleeReference(namedReference)
            konst typeForCallableReference = atom.resultingTypeForCallableReference
            konst resolvedTypeRef = when {
                typeForCallableReference != null -> buildResolvedTypeRef {
                    type = typeForCallableReference
                }
                namedReference is FirErrorReferenceWithCandidate -> buildErrorTypeRef {
                    diagnostic = namedReference.diagnostic
                }
                else -> buildErrorTypeRef {
                    diagnostic = ConeUnresolvedReferenceError(callableReferenceAccess.calleeReference.name)
                }
            }
            replaceTypeRef(resolvedTypeRef)
            resolutionContext.session.lookupTracker?.recordTypeResolveAsLookup(
                resolvedTypeRef, source, resolutionContext.bodyResolveComponents.file.source
            )
        }
    }

    fun analyzeLambda(
        c: PostponedArgumentsAnalyzerContext,
        lambda: ResolvedLambdaAtom,
        candidate: Candidate,
        completionMode: ConstraintSystemCompletionMode,
        //diagnosticHolder: KotlinDiagnosticsHolder
    ): ReturnArgumentsAnalysisResult {
        // TODO: replace with `require(!lambda.analyzed)` when KT-54767 will be fixed
        if (lambda.analyzed) {
            return ReturnArgumentsAnalysisResult(lambda.returnStatements, inferenceSession = null)
        }

        konst unitType = components.session.builtinTypes.unitType.type
        konst stubsForPostponedVariables = c.bindingStubsForPostponedVariables()
        konst currentSubstitutor = c.buildCurrentSubstitutor(stubsForPostponedVariables.mapKeys { it.key.freshTypeConstructor(c) })

        fun substitute(type: ConeKotlinType) = currentSubstitutor.safeSubstitute(c, type) as ConeKotlinType

        konst receiver = lambda.receiver?.let(::substitute)
        konst contextReceivers = lambda.contextReceivers.map(::substitute)
        konst parameters = lambda.parameters.map(::substitute)
        konst rawReturnType = lambda.returnType

        konst expectedTypeForReturnArguments = when {
            c.canBeProper(rawReturnType) -> substitute(rawReturnType)

            // For Unit-coercion
            !rawReturnType.isMarkedNullable && c.hasUpperOrEqualUnitConstraint(rawReturnType) -> unitType

            else -> null
        }

        konst results = lambdaAnalyzer.analyzeAndGetLambdaReturnArguments(
            lambda,
            receiver,
            contextReceivers,
            parameters,
            expectedTypeForReturnArguments,
            stubsForPostponedVariables,
            candidate
        )
        applyResultsOfAnalyzedLambdaToCandidateSystem(
            c,
            lambda,
            candidate,
            results,
            completionMode,
            ::substitute
        )
        return results
    }

    fun applyResultsOfAnalyzedLambdaToCandidateSystem(
        c: PostponedArgumentsAnalyzerContext,
        lambda: ResolvedLambdaAtom,
        candidate: Candidate,
        results: ReturnArgumentsAnalysisResult,
        completionMode: ConstraintSystemCompletionMode,
        substitute: (ConeKotlinType) -> ConeKotlinType = c.createSubstituteFunctorForLambdaAnalysis()
    ) {
        konst (returnArguments, inferenceSession) = results

        konst checkerSink: CheckerSink = CheckerSinkImpl(candidate)
        konst builder = c.getBuilder()

        konst lastExpression = lambda.atom.body?.statements?.lastOrNull() as? FirExpression
        var hasExpressionInReturnArguments = false
        konst returnTypeRef = lambda.atom.returnTypeRef.let {
            it as? FirResolvedTypeRef ?: it.resolvedTypeFromPrototype(substitute(lambda.returnType))
        }
        konst lambdaExpectedTypeIsUnit = returnTypeRef.type.isUnitOrFlexibleUnit
        returnArguments.forEach {
            // If the lambda returns Unit, the last expression is not returned and should not be constrained.
            konst isLastExpression = it == lastExpression

            // Don't add last call for builder-inference
            // Note, that we don't use the same condition "(returnTypeRef.type.isUnitOrFlexibleUnit || lambda.atom.shouldReturnUnit(returnArguments))"
            // as in the if below, because "lambda.atom.shouldReturnUnit(returnArguments)" might mean that the last statement is not completed
            if (isLastExpression && lambdaExpectedTypeIsUnit && inferenceSession is FirBuilderInferenceSession) return@forEach

            // TODO (KT-55837) questionable moment inherited from FE1.0 (the `haveSubsystem` case):
            //    fun <T> foo(): T
            //    run {
            //      if (p) return@run
            //      foo() // T = Unit, even though there is no implicit return
            //    }
            //  Things get even weirder if T has an upper bound incompatible with Unit.
            // Not calling `addSubsystemFromExpression` for builder-inference is crucial
            konst haveSubsystem = c.addSubsystemFromExpression(it)
            if (isLastExpression && !haveSubsystem &&
                (lambdaExpectedTypeIsUnit || lambda.atom.shouldReturnUnit(returnArguments))
            ) return@forEach

            hasExpressionInReturnArguments = true
            if (!builder.hasContradiction) {
                candidate.resolveArgumentExpression(
                    builder,
                    it,
                    returnTypeRef.type,
                    checkerSink,
                    context = resolutionContext,
                    isReceiver = false,
                    isDispatch = false
                )
            }
        }

        if (!hasExpressionInReturnArguments && !lambdaExpectedTypeIsUnit) {
            builder.addSubtypeConstraint(
                components.session.builtinTypes.unitType.type,
                returnTypeRef.type,
                ConeLambdaArgumentConstraintPosition(lambda.atom)
            )
        }

        lambda.analyzed = true
        lambda.returnStatements = returnArguments
        c.resolveForkPointsConstraints()

        if (inferenceSession != null) {
            konst postponedVariables = inferenceSession.inferPostponedVariables(lambda, builder, completionMode)

            if (postponedVariables == null) {
                builder.removePostponedVariables()
                return
            }

            for ((constructor, resultType) in postponedVariables) {
                konst variableWithConstraints = builder.currentStorage().notFixedTypeVariables[constructor] ?: continue
                konst variable = variableWithConstraints.typeVariable as ConeTypeVariable

                builder.unmarkPostponedVariable(variable)
                builder.addSubtypeConstraint(resultType, variable.defaultType(c), BuilderInferencePosition)
            }

            c.removePostponedTypeVariablesFromConstraints(postponedVariables.keys)
        }
    }

    fun PostponedArgumentsAnalyzerContext.createSubstituteFunctorForLambdaAnalysis(): (ConeKotlinType) -> ConeKotlinType {
        konst stubsForPostponedVariables = bindingStubsForPostponedVariables()
        konst currentSubstitutor = buildCurrentSubstitutor(stubsForPostponedVariables.mapKeys { it.key.freshTypeConstructor(this) })
        return { currentSubstitutor.safeSubstitute(this, it) as ConeKotlinType }
    }
}

fun LambdaWithTypeVariableAsExpectedTypeAtom.transformToResolvedLambda(
    csBuilder: ConstraintSystemBuilder,
    context: ResolutionContext,
    expectedType: ConeKotlinType? = null,
    returnTypeVariable: ConeTypeVariableForLambdaReturnType? = null
): ResolvedLambdaAtom {
    konst fixedExpectedType = (csBuilder.buildCurrentSubstitutor() as ConeSubstitutor)
        .substituteOrSelf(expectedType ?: this.expectedType)
    konst resolvedAtom = candidateOfOuterCall.preprocessLambdaArgument(
        csBuilder,
        atom,
        fixedExpectedType,
        context,
        sink = null,
        duringCompletion = true,
        returnTypeVariable = returnTypeVariable
    ) as ResolvedLambdaAtom
    analyzed = true
    return resolvedAtom
}
