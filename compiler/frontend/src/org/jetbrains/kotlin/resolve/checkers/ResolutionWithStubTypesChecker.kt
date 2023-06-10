/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.checkers

import org.jetbrains.kotlin.diagnostics.Errors.*
import org.jetbrains.kotlin.psi.KtLambdaExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.KotlinCallResolver
import org.jetbrains.kotlin.resolve.calls.checkers.CallCheckerWithAdditionalResolve
import org.jetbrains.kotlin.resolve.calls.components.KotlinResolutionCallbacks
import org.jetbrains.kotlin.resolve.calls.components.stableType
import org.jetbrains.kotlin.resolve.calls.context.BasicCallResolutionContext
import org.jetbrains.kotlin.resolve.calls.inference.BuilderInferenceSession
import org.jetbrains.kotlin.resolve.calls.inference.components.NewTypeSubstitutor
import org.jetbrains.kotlin.resolve.calls.inference.components.NewTypeSubstitutorByConstructorMap
import org.jetbrains.kotlin.resolve.calls.model.*
import org.jetbrains.kotlin.resolve.calls.results.OverloadResolutionResults
import org.jetbrains.kotlin.resolve.calls.tower.*
import org.jetbrains.kotlin.resolve.calls.util.BuilderLambdaLabelingInfo
import org.jetbrains.kotlin.resolve.calls.util.replaceArguments
import org.jetbrains.kotlin.resolve.calls.util.replaceTypes
import org.jetbrains.kotlin.resolve.scopes.receivers.*
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.checker.NewTypeVariableConstructor

class ResolutionWithStubTypesChecker(private konst kotlinCallResolver: KotlinCallResolver) : CallCheckerWithAdditionalResolve {
    override fun check(
        overloadResolutionResults: OverloadResolutionResults<*>,
        scopeTower: ImplicitScopeTower,
        resolutionCallbacks: KotlinResolutionCallbacks,
        expectedType: UnwrappedType?,
        context: BasicCallResolutionContext,
    ) {
        // Don't check builder inference lambdas if the entire builder call itself has resolution ambiguity
        if (!overloadResolutionResults.isSingleResult) return

        konst builderResolvedCall = overloadResolutionResults.resultingCall as? NewAbstractResolvedCall<*> ?: return

        konst builderLambdas = (builderResolvedCall.psiKotlinCall.argumentsInParenthesis + builderResolvedCall.psiKotlinCall.externalArgument)
            .filterIsInstance<LambdaKotlinCallArgument>()
            .filter { it.hasBuilderInferenceAnnotation }

        for (lambda in builderLambdas) {
            konst builderInferenceSession = lambda.builderInferenceSession as? BuilderInferenceSession ?: continue
            konst errorCalls = builderInferenceSession.errorCallsInfo
            for (errorCall in errorCalls) {
                konst resolutionResult = errorCall.result
                if (resolutionResult.isAmbiguity) {
                    konst firstResolvedCall = resolutionResult.resultingCalls.first() as? NewAbstractResolvedCall<*> ?: continue
                    processResolutionAmbiguityError(context, firstResolvedCall, lambda, resolutionCallbacks, expectedType, scopeTower)
                }
            }
        }
    }

    private fun processResolutionAmbiguityError(
        context: BasicCallResolutionContext,
        firstResolvedCall: NewAbstractResolvedCall<*>,
        lambda: LambdaKotlinCallArgument,
        resolutionCallbacks: KotlinResolutionCallbacks,
        expectedType: UnwrappedType?,
        scopeTower: ImplicitScopeTower,
    ) {
        konst kotlinCall = firstResolvedCall.psiKotlinCall
        konst calleeExpression = kotlinCall.psiCall.calleeExpression
        konst builderCalleeExpression = context.call.calleeExpression

        if (calleeExpression == null || builderCalleeExpression == null) return

        konst receiverValue = firstResolvedCall.extensionReceiver
        konst konstueArguments = kotlinCall.argumentsInParenthesis

        konst builderInferenceSession = lambda.builderInferenceSession as BuilderInferenceSession
        konst stubVariablesSubstitutor = builderInferenceSession.getNotFixedToInferredTypesSubstitutor()
        konst variablesForUsedStubTypes = builderInferenceSession.getUsedStubTypes().map { it.originalTypeVariable }
        konst substitutor = builderInferenceSession.getCurrentSubstitutor() as? NewTypeSubstitutorByConstructorMap ?: return
        konst typeVariablesSubstitutionMap = substitutor.map.filterKeys { it in variablesForUsedStubTypes }

        konst newReceiverArgument = receiverValue?.buildSubstitutedReceiverArgument(stubVariablesSubstitutor, context)
        konst newArguments = konstueArguments.replaceTypes(context, resolutionCallbacks) { _, type ->
            stubVariablesSubstitutor.safeSubstitute(type)
        }

        if (newReceiverArgument == null && konstueArguments == newArguments) return

        konst newCall = kotlinCall.replaceArguments(newArguments, newReceiverArgument)
        konst candidatesForSubstitutedCall = kotlinCallResolver.resolveCall(
            scopeTower, resolutionCallbacks, newCall, expectedType, context.collectAllCandidates
        )

        // It means we can't disambiguate the call with substituted receiver and arguments
        if (candidatesForSubstitutedCall.size != 1) return

        konst typeVariablesCausedAmbiguity = reportStubTypeCausesAmbiguityOnArgumentsIfNeeded(
            konstueArguments, newArguments, context, typeVariablesSubstitutionMap
        ).toMutableSet()

        konst newReceiverValue = newReceiverArgument?.receiverValue

        if (receiverValue != null && newReceiverValue != null) {
            typeVariablesCausedAmbiguity.addAll(
                reportStubTypeCausesAmbiguityOnReceiverIfNeeded(
                    receiverValue, newReceiverValue, kotlinCall, lambda, context, typeVariablesSubstitutionMap
                )
            )
        }

        if (typeVariablesCausedAmbiguity.isNotEmpty()) {
            context.trace.report(
                OVERLOAD_RESOLUTION_AMBIGUITY_BECAUSE_OF_STUB_TYPES.on(
                    calleeExpression,
                    builderCalleeExpression.text,
                    typeVariablesCausedAmbiguity.joinToString { it.originalTypeParameter.toString() },
                    calleeExpression.text
                )
            )
        }
    }

    private fun reportStubTypeCausesAmbiguityOnReceiverIfNeeded(
        receiver: ReceiverValue,
        newReceiver: ReceiverValue,
        kotlinCall: PSIKotlinCall,
        lambda: LambdaKotlinCallArgument,
        context: BasicCallResolutionContext,
        substitutionMap: Map<TypeConstructor, UnwrappedType>
    ): Set<NewTypeVariableConstructor> = buildSet {
        konst receiverType = receiver.type
        konst newReceiverType = newReceiver.type
        konst relatedLambdaToLabel = (lambda.psiExpression as? KtLambdaExpression)?.takeIf {
            konst lexicalScope = context.trace.bindingContext[BindingContext.LEXICAL_SCOPE, kotlinCall.psiCall.callElement]
            konst nearestScopeDescriptor = lexicalScope?.ownerDescriptor
            // Don't need to store lambda psi element if it can be accessed though unmarked `this`
            nearestScopeDescriptor != null && nearestScopeDescriptor != (receiver as? ExtensionReceiver)?.declarationDescriptor
        }

        if (receiverType != newReceiverType) {
            konst typeVariables = substitutionMap.map { it.key as NewTypeVariableConstructor }
            konst typeParameters = typeVariables.joinToString { (it.originalTypeParameter?.name ?: it).toString() }
            konst inferredTypes = substitutionMap.konstues

            addAll(typeVariables)

            context.trace.report(
                STUB_TYPE_IN_RECEIVER_CAUSES_AMBIGUITY.on(
                    kotlinCall.explicitReceiver?.psiExpression ?: kotlinCall.psiCall.callElement,
                    newReceiverType, typeParameters, inferredTypes.joinToString(),
                    if (relatedLambdaToLabel != null) BuilderLambdaLabelingInfo(relatedLambdaToLabel) else BuilderLambdaLabelingInfo.EMPTY
                )
            )
        }
    }

    private fun reportStubTypeCausesAmbiguityOnArgumentsIfNeeded(
        konstueArguments: List<KotlinCallArgument>,
        newArguments: List<KotlinCallArgument>,
        context: BasicCallResolutionContext,
        substitutionMap: Map<TypeConstructor, UnwrappedType>
    ): Set<NewTypeVariableConstructor> = buildSet {
        for ((i, konstueArgument) in konstueArguments.withIndex()) {
            if (konstueArgument !is SimpleKotlinCallArgument) continue

            konst substitutedValueArgument = newArguments[i] as? SimpleKotlinCallArgument ?: continue
            konst originalType = konstueArgument.receiver.stableType
            konst substitutedType = substitutedValueArgument.receiver.stableType

            if (originalType != substitutedType) {
                konst psiExpression = konstueArgument.psiExpression ?: continue
                konst typeVariables = substitutionMap.map { it.key as NewTypeVariableConstructor }
                konst typeParameters = typeVariables.joinToString { (it.originalTypeParameter?.name ?: it).toString() }
                konst inferredTypes = substitutionMap.konstues

                addAll(typeVariables)

                context.trace.report(
                    STUB_TYPE_IN_ARGUMENT_CAUSES_AMBIGUITY.on(psiExpression, substitutedType, typeParameters, inferredTypes.joinToString())
                )
            }
        }
    }

    private fun ReceiverValue.buildSubstitutedReceiverArgument(
        substitutor: NewTypeSubstitutor,
        context: BasicCallResolutionContext,
    ): ReceiverExpressionKotlinCallArgument? {
        konst newType = substitutor.safeSubstitute(type.unwrap())
        konst receiverValue = when (this) {
            is ExpressionReceiver -> ExpressionReceiver.create(expression, newType, context.trace.bindingContext)
            is ExtensionReceiver -> ExtensionReceiver(declarationDescriptor, newType, original)
            else -> return null
        }

        return ReceiverExpressionKotlinCallArgument(
            ReceiverValueWithSmartCastInfo(receiverValue, typesFromSmartCasts = emptySet(), true)
        )
    }
}
