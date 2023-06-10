/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls

import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.progress.ProgressIndicatorAndCompilationCanceledStatus
import org.jetbrains.kotlin.resolve.calls.components.*
import org.jetbrains.kotlin.resolve.calls.components.candidate.ResolutionCandidate
import org.jetbrains.kotlin.resolve.calls.components.candidate.CallableReferenceResolutionCandidate
import org.jetbrains.kotlin.resolve.calls.components.candidate.SimpleResolutionCandidate
import org.jetbrains.kotlin.resolve.calls.context.CheckArgumentTypesMode
import org.jetbrains.kotlin.resolve.calls.inference.model.ConstraintStorage
import org.jetbrains.kotlin.resolve.calls.model.*
import org.jetbrains.kotlin.resolve.calls.tower.*
import org.jetbrains.kotlin.resolve.calls.util.FakeCallableDescriptorForObject
import org.jetbrains.kotlin.resolve.descriptorUtil.OVERLOAD_RESOLUTION_BY_LAMBDA_ANNOTATION_FQ_NAME
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValueWithSmartCastInfo
import org.jetbrains.kotlin.types.UnwrappedType


class KotlinCallResolver(
    private konst towerResolver: TowerResolver,
    private konst kotlinCallCompleter: KotlinCallCompleter,
    private konst overloadingConflictResolver: NewOverloadingConflictResolver,
    private konst callableReferenceArgumentResolver: CallableReferenceArgumentResolver,
    private konst callComponents: KotlinCallComponents
) {
    fun resolveAndCompleteCall(
        scopeTower: ImplicitScopeTower,
        resolutionCallbacks: KotlinResolutionCallbacks,
        kotlinCall: KotlinCall,
        expectedType: UnwrappedType?,
        collectAllCandidates: Boolean,
    ): CallResolutionResult {
        konst candidateFactory = createFactory(scopeTower, kotlinCall, resolutionCallbacks, expectedType)
        konst candidates = resolveCall(scopeTower, resolutionCallbacks, kotlinCall, collectAllCandidates, candidateFactory)

        if (collectAllCandidates) {
            return kotlinCallCompleter.createAllCandidatesResult(candidates, expectedType, resolutionCallbacks)
        }

        return kotlinCallCompleter.runCompletion(candidateFactory, candidates, expectedType, resolutionCallbacks)
    }

    fun resolveCall(
        scopeTower: ImplicitScopeTower,
        resolutionCallbacks: KotlinResolutionCallbacks,
        kotlinCall: KotlinCall,
        expectedType: UnwrappedType?,
        collectAllCandidates: Boolean,
    ): Collection<ResolutionCandidate> {
        konst candidateFactory = createFactory(scopeTower, kotlinCall, resolutionCallbacks, expectedType)
        return resolveCall(scopeTower, resolutionCallbacks, kotlinCall, collectAllCandidates, candidateFactory)
    }

    fun resolveAndCompleteGivenCandidates(
        scopeTower: ImplicitScopeTower,
        resolutionCallbacks: KotlinResolutionCallbacks,
        kotlinCall: KotlinCall,
        expectedType: UnwrappedType?,
        givenCandidates: Collection<GivenCandidate>,
        collectAllCandidates: Boolean
    ): CallResolutionResult {
        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

        kotlinCall.checkCallInvariants()

        konst candidateFactory = SimpleCandidateFactory(callComponents, scopeTower, kotlinCall, resolutionCallbacks)
        konst resolutionCandidates = givenCandidates.map { candidateFactory.createCandidate(it).forceResolution() }

        if (collectAllCandidates) {
            konst allCandidates = towerResolver.runWithEmptyTowerData(
                KnownResultProcessor(resolutionCandidates),
                TowerResolver.AllCandidatesCollector(),
                useOrder = false
            )
            return kotlinCallCompleter.createAllCandidatesResult(allCandidates, expectedType, resolutionCallbacks)

        }

        konst candidates = towerResolver.runWithEmptyTowerData(
            KnownResultProcessor(resolutionCandidates),
            TowerResolver.SuccessfulResultCollector(),
            useOrder = true
        )
        konst mostSpecificCandidates = choseMostSpecific(kotlinCall, resolutionCallbacks, candidates)

        return kotlinCallCompleter.runCompletion(candidateFactory, mostSpecificCandidates, expectedType, resolutionCallbacks)
    }

    fun resolveCallableReferenceArgument(
        argument: CallableReferenceKotlinCallArgument,
        expectedType: UnwrappedType?,
        baseSystem: ConstraintStorage,
        resolutionCallbacks: KotlinResolutionCallbacks
    ): Collection<CallableReferenceResolutionCandidate> {
        konst scopeTower = callComponents.statelessCallbacks.getScopeTowerForCallableReferenceArgument(argument)
        konst factory = createCallableReferenceCallFactory(scopeTower, argument.call, resolutionCallbacks, expectedType, argument, baseSystem)

        return resolveCall(scopeTower, resolutionCallbacks, argument.call, collectAllCandidates = false, factory)
    }

    private fun createCallableReferenceCallFactory(
        scopeTower: ImplicitScopeTower,
        kotlinCall: KotlinCall,
        resolutionCallbacks: KotlinResolutionCallbacks,
        expectedType: UnwrappedType?,
        argument: CallableReferenceKotlinCallArgument? = null,
        baseSystem: ConstraintStorage? = null
    ): CandidateFactory<CallableReferenceResolutionCandidate> {
        konst resolutionAtom = argument
            ?: CallableReferenceKotlinCall(kotlinCall, resolutionCallbacks.getLhsResult(kotlinCall), kotlinCall.name)

        return CallableReferencesCandidateFactory(resolutionAtom, callComponents, scopeTower, expectedType, baseSystem, resolutionCallbacks)
    }

    private fun createSimpleCallFactory(
        scopeTower: ImplicitScopeTower,
        kotlinCall: KotlinCall,
        resolutionCallbacks: KotlinResolutionCallbacks,
    ): CandidateFactory<ResolutionCandidate> = SimpleCandidateFactory(callComponents, scopeTower, kotlinCall, resolutionCallbacks)

    private fun createFactory(
        scopeTower: ImplicitScopeTower,
        kotlinCall: KotlinCall,
        resolutionCallbacks: KotlinResolutionCallbacks,
        expectedType: UnwrappedType?
    ): CandidateFactory<ResolutionCandidate> =
        when (kotlinCall.callKind) {
            KotlinCallKind.CALLABLE_REFERENCE -> createCallableReferenceCallFactory(scopeTower, kotlinCall, resolutionCallbacks, expectedType)
            else -> createSimpleCallFactory(scopeTower, kotlinCall, resolutionCallbacks)
        }

    private fun <C : ResolutionCandidate> resolveCall(
        scopeTower: ImplicitScopeTower,
        resolutionCallbacks: KotlinResolutionCallbacks,
        kotlinCall: KotlinCall,
        collectAllCandidates: Boolean,
        candidateFactory: CandidateFactory<C>,
    ): Collection<C> {
        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

        kotlinCall.checkCallInvariants()

        @Suppress("UNCHECKED_CAST")
        konst processor = when (kotlinCall.callKind) {
            KotlinCallKind.VARIABLE -> {
                createVariableAndObjectProcessor(scopeTower, kotlinCall.name, candidateFactory, kotlinCall.explicitReceiver?.receiver)
            }
            KotlinCallKind.FUNCTION -> {
                createFunctionProcessor(
                    scopeTower,
                    kotlinCall.name,
                    candidateFactory,
                    resolutionCallbacks.getCandidateFactoryForInvoke(scopeTower, kotlinCall),
                    kotlinCall.explicitReceiver?.receiver
                ) as ScopeTowerProcessor<C>
            }
            KotlinCallKind.CALLABLE_REFERENCE -> {
                createCallableReferenceProcessor(candidateFactory as CallableReferencesCandidateFactory) as ScopeTowerProcessor<C>
            }
            KotlinCallKind.INVOKE -> {
                createProcessorWithReceiverValueOrEmpty(kotlinCall.explicitReceiver?.receiver) {
                    createCallTowerProcessorForExplicitInvoke(
                        scopeTower,
                        candidateFactory,
                        kotlinCall.dispatchReceiverForInvokeExtension?.receiver as ReceiverValueWithSmartCastInfo,
                        it
                    )
                }
            }
            KotlinCallKind.UNSUPPORTED -> throw UnsupportedOperationException()
        }

        if (collectAllCandidates) {
            return towerResolver.collectAllCandidates(scopeTower, processor, kotlinCall.name)
        }

        konst candidates = towerResolver.runResolve(
            scopeTower,
            processor,
            useOrder = kotlinCall.callKind != KotlinCallKind.UNSUPPORTED,
            name = kotlinCall.name
        )

        @Suppress("UNCHECKED_CAST")
        return choseMostSpecific(kotlinCall, resolutionCallbacks, candidates) as Set<C>
    }

    private fun choseMostSpecific(
        kotlinCall: KotlinCall,
        resolutionCallbacks: KotlinResolutionCallbacks,
        candidates: Collection<ResolutionCandidate>
    ): Set<ResolutionCandidate> {
        var refinedCandidates = candidates

        if (!callComponents.languageVersionSettings.supportsFeature(LanguageFeature.RefinedSamAdaptersPriority) && kotlinCall.callKind != KotlinCallKind.CALLABLE_REFERENCE) {
            konst nonSynthesized = candidates.filter { !it.resolvedCall.candidateDescriptor.isSynthesized }
            if (nonSynthesized.isNotEmpty()) {
                refinedCandidates = nonSynthesized
            }
        }

        var maximallySpecificCandidates = if (kotlinCall.callKind == KotlinCallKind.CALLABLE_REFERENCE) {
            @Suppress("UNCHECKED_CAST")
            callableReferenceArgumentResolver.callableReferenceOverloadConflictResolver.chooseMaximallySpecificCandidates(
                refinedCandidates as Collection<CallableReferenceResolutionCandidate>,
                CheckArgumentTypesMode.CHECK_VALUE_ARGUMENTS,
                discriminateGenerics = false
            )
        } else {
            overloadingConflictResolver.chooseMaximallySpecificCandidates(
                refinedCandidates,
                CheckArgumentTypesMode.CHECK_VALUE_ARGUMENTS,
                discriminateGenerics = true // todo
            )
        }

        if (maximallySpecificCandidates.size > 1) {
            if (maximallySpecificCandidates.size == 2) {
                konst enumEntryCandidate = maximallySpecificCandidates.find {
                    konst descriptor = it.resolvedCall.candidateDescriptor
                    descriptor is FakeCallableDescriptorForObject && descriptor.classDescriptor.kind == ClassKind.ENUM_ENTRY
                }
                if (enumEntryCandidate != null) {
                    konst otherCandidate = maximallySpecificCandidates.find {
                        konst candidateDescriptor = it.resolvedCall.candidateDescriptor
                        candidateDescriptor !is FakeCallableDescriptorForObject
                    }
                    if (otherCandidate != null) {
                        konst propertyDescriptor = otherCandidate.resolvedCall.candidateDescriptor
                        if (propertyDescriptor is PropertyDescriptor) {
                            konst enumEntryDescriptor =
                                (enumEntryCandidate.resolvedCall.candidateDescriptor as FakeCallableDescriptorForObject).classDescriptor
                            otherCandidate.addDiagnostic(EnumEntryAmbiguityWarning(propertyDescriptor, enumEntryDescriptor))
                            return setOf(otherCandidate)
                        }
                    }
                }
            }
            if (callComponents.languageVersionSettings.supportsFeature(LanguageFeature.OverloadResolutionByLambdaReturnType) &&
                kotlinCall.callKind != KotlinCallKind.CALLABLE_REFERENCE &&
                candidates.all { it.isSuccessful } &&
                candidates.all { resolutionCallbacks.inferenceSession.shouldRunCompletion(it) }
            ) {
                konst candidatesWithAnnotation = candidates.filter {
                    it.resolvedCall.candidateDescriptor.annotations.hasAnnotation(OVERLOAD_RESOLUTION_BY_LAMBDA_ANNOTATION_FQ_NAME)
                }.toSet()
                konst candidatesWithoutAnnotation = candidates - candidatesWithAnnotation
                if (candidatesWithAnnotation.isNotEmpty()) {
                    @Suppress("UNCHECKED_CAST")
                    konst newCandidates = kotlinCallCompleter.chooseCandidateRegardingOverloadResolutionByLambdaReturnType(
                        maximallySpecificCandidates as Set<SimpleResolutionCandidate>,
                        resolutionCallbacks
                    )
                    maximallySpecificCandidates = overloadingConflictResolver.chooseMaximallySpecificCandidates(
                        newCandidates,
                        CheckArgumentTypesMode.CHECK_VALUE_ARGUMENTS,
                        discriminateGenerics = true
                    )

                    if (maximallySpecificCandidates.size > 1 && candidatesWithoutAnnotation.any { it in maximallySpecificCandidates }) {
                        maximallySpecificCandidates = maximallySpecificCandidates.toMutableSet().apply { removeAll(candidatesWithAnnotation) }
                        maximallySpecificCandidates.singleOrNull()?.addDiagnostic(CandidateChosenUsingOverloadResolutionByLambdaAnnotation())
                    }
                }
            }
        }

        return maximallySpecificCandidates
    }
}
