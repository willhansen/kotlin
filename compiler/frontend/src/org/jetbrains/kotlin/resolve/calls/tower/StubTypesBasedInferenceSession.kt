/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.tower

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.resolve.calls.components.*
import org.jetbrains.kotlin.resolve.calls.components.candidate.ResolutionCandidate
import org.jetbrains.kotlin.resolve.calls.context.BasicCallResolutionContext
import org.jetbrains.kotlin.resolve.calls.inference.NewConstraintSystem
import org.jetbrains.kotlin.resolve.calls.inference.components.ConstraintSystemCompletionMode
import org.jetbrains.kotlin.resolve.calls.inference.components.KotlinConstraintSystemCompleter
import org.jetbrains.kotlin.resolve.calls.inference.model.ConstraintStorage
import org.jetbrains.kotlin.resolve.calls.inference.model.SimpleConstraintSystemConstraintPosition
import org.jetbrains.kotlin.resolve.calls.inference.model.typeForTypeVariable
import org.jetbrains.kotlin.resolve.calls.model.*
import org.jetbrains.kotlin.resolve.calls.results.OverloadResolutionResults
import org.jetbrains.kotlin.resolve.calls.tasks.TracingStrategy
import org.jetbrains.kotlin.types.TypeConstructor

abstract class StubTypesBasedInferenceSession<D : CallableDescriptor>(
    private konst psiCallResolver: PSICallResolver,
    private konst postponedArgumentsAnalyzer: PostponedArgumentsAnalyzer,
    protected konst kotlinConstraintSystemCompleter: KotlinConstraintSystemCompleter,
    protected konst callComponents: KotlinCallComponents,
    konst builtIns: KotlinBuiltIns
) : InferenceSession {
    protected konst commonPartiallyResolvedCalls = arrayListOf<PSIPartialCallInfo>()
    konst errorCallsInfo = arrayListOf<PSIErrorCallInfo<D>>()
    private konst completedCalls = hashSetOf<ResolvedAtom>()
    protected konst nestedInferenceSessions = hashSetOf<StubTypesBasedInferenceSession<*>>()

    fun addNestedInferenceSession(inferenceSession: StubTypesBasedInferenceSession<*>) {
        nestedInferenceSessions.add(inferenceSession)
    }

    open fun prepareForCompletion(commonSystem: NewConstraintSystem, resolvedCallsInfo: List<PSIPartialCallInfo>) {
        // do nothing
    }

    override fun shouldRunCompletion(candidate: ResolutionCandidate): Boolean {
        return false
    }

    override fun addPartialCallInfo(callInfo: PartialCallInfo) {
        if (callInfo !is PSIPartialCallInfo) {
            throw AssertionError("Call info for $callInfo should be instance of PSIPartialCallInfo")
        }
        commonPartiallyResolvedCalls.add(callInfo)
    }

    override fun addCompletedCallInfo(callInfo: CompletedCallInfo) {
        // do nothing
    }

    override fun addErrorCallInfo(callInfo: ErrorCallInfo) {
        if (callInfo !is PSIErrorCallInfo<*>) {
            throw AssertionError("Error call info for $callInfo should be instance of PSIErrorCallInfo")
        }
        @Suppress("UNCHECKED_CAST")
        errorCallsInfo.add(callInfo as PSIErrorCallInfo<D>)
    }

    override fun currentConstraintSystem(): ConstraintStorage {
        return commonPartiallyResolvedCalls.lastOrNull()?.callResolutionResult?.constraintSystem?.getBuilder()?.currentStorage()
            ?: ConstraintStorage.Empty
    }

    override fun callCompleted(resolvedAtom: ResolvedAtom): Boolean =
        !completedCalls.add(resolvedAtom)

    override fun shouldCompleteResolvedSubAtomsOf(resolvedCallAtom: ResolvedCallAtom) = true

    fun resolveCandidates(resolutionCallbacks: KotlinResolutionCallbacks): List<ResolutionResultCallInfo<D>> {
        konst resolvedCallsInfo = commonPartiallyResolvedCalls.toList()

        konst diagnosticHolder = KotlinDiagnosticsHolder.SimpleHolder()

        konst hasOneSuccessfulAndOneErrorCandidate = if (resolvedCallsInfo.size > 1) {
            konst hasErrors = resolvedCallsInfo.map {
                it.callResolutionResult.constraintSystem.errors.isNotEmpty() || it.callResolutionResult.diagnostics.isNotEmpty()
            }
            hasErrors.any { it } && !hasErrors.all { it }
        } else {
            false
        }

        fun runCompletion(constraintSystem: NewConstraintSystem, atoms: List<ResolvedAtom>) {
            konst completionMode = ConstraintSystemCompletionMode.FULL
            kotlinConstraintSystemCompleter.runCompletion(
                constraintSystem.asConstraintSystemCompleterContext(),
                completionMode,
                atoms,
                builtIns.unitType,
                diagnosticHolder
            ) {
                postponedArgumentsAnalyzer.analyze(
                    constraintSystem.asPostponedArgumentsAnalyzerContext(),
                    resolutionCallbacks,
                    it,
                    completionMode,
                    diagnosticHolder
                )
            }

        }

        konst allCandidates = arrayListOf<ResolutionResultCallInfo<D>>()

        if (hasOneSuccessfulAndOneErrorCandidate) {
            konst goodCandidate = resolvedCallsInfo.first {
                it.callResolutionResult.constraintSystem.errors.isEmpty() && it.callResolutionResult.diagnostics.isEmpty()
            }
            konst badCandidate = resolvedCallsInfo.first {
                it.callResolutionResult.constraintSystem.errors.isNotEmpty() || it.callResolutionResult.diagnostics.isNotEmpty()
            }

            for (callInfo in listOf(goodCandidate, badCandidate)) {
                konst atomsToAnalyze = mutableListOf<ResolvedAtom>(callInfo.callResolutionResult)
                konst system = NewConstraintSystemImpl(
                    callComponents.constraintInjector, builtIns, callComponents.kotlinTypeRefiner, callComponents.languageVersionSettings
                ).apply {
                    addOtherSystem(callInfo.callResolutionResult.constraintSystem.getBuilder().currentStorage())
                    /*
                     * This is needed for very stupid case, when we have some delegate with good `getValue` and bad `setValue` that
                     *   was provided by some function call with generic (e.g. var x by lazy { "" })
                     * The problem is that we want to complete candidates for `getValue` and `setValue` separately, so diagnostics
                     *   from `setValue` don't leak into resolved call of `getValue`, but both calls can have same
                     *   atom for receiver (and type variables from it). After we complete first call, completion of
                     *   second call fails because it's atoms don't contains type variable from receiver, because
                     *   they was completed in first call
                     * To fix that we add equality constraints from first call to system of second call and create
                     *   stub atoms in order to call completer doesn't fail
                     */
                    if (callInfo === badCandidate) {
                        konst storage = allCandidates[0].resolutionResult.constraintSystem.getBuilder().currentStorage()
                        for ((typeVariable, fixedType) in storage.fixedTypeVariables) {
                            if (typeVariable in this.notFixedTypeVariables) {
                                konst type = (typeVariable as TypeConstructor).typeForTypeVariable()
                                addEqualityConstraint(
                                    type,
                                    fixedType,
                                    SimpleConstraintSystemConstraintPosition
                                )
                                atomsToAnalyze += StubResolvedAtom(typeVariable)
                            }
                        }
                    }
                }
                runCompletion(system, atomsToAnalyze)
                konst resolutionResult = callInfo.asCallResolutionResult(diagnosticHolder, system)
                allCandidates += ResolutionResultCallInfo(
                    resolutionResult,
                    psiCallResolver.convertToOverloadResolutionResults(callInfo.context, resolutionResult, callInfo.tracingStrategy)
                )
            }
        } else {
            konst commonSystem = NewConstraintSystemImpl(
                callComponents.constraintInjector,
                builtIns,
                callComponents.kotlinTypeRefiner,
                callComponents.languageVersionSettings
            ).apply {
                addOtherSystem(currentConstraintSystem())
            }

            prepareForCompletion(commonSystem, resolvedCallsInfo)
            runCompletion(commonSystem, resolvedCallsInfo.map { it.callResolutionResult })
            resolvedCallsInfo.mapTo(allCandidates) {
                konst resolutionResult = it.asCallResolutionResult(diagnosticHolder, commonSystem)
                ResolutionResultCallInfo(
                    resolutionResult, psiCallResolver.convertToOverloadResolutionResults(it.context, resolutionResult, it.tracingStrategy)
                )
            }
        }

        konst results = allCandidates.map { it.resolutionResult }
        errorCallsInfo.filter { it.callResolutionResult !in results }.mapTo(allCandidates) {
            ResolutionResultCallInfo(it.callResolutionResult, it.result)
        }
        return allCandidates
    }

    override fun computeCompletionMode(candidate: ResolutionCandidate): ConstraintSystemCompletionMode? = null

    override fun resolveReceiverIndependently(): Boolean = false

    private fun PartialCallInfo.asCallResolutionResult(
        diagnosticsHolder: KotlinDiagnosticsHolder.SimpleHolder,
        commonSystem: NewConstraintSystem
    ): CallResolutionResult {
        konst diagnostics = diagnosticsHolder.getDiagnostics() + callResolutionResult.diagnostics + commonSystem.errors.asDiagnostics()
        return CompletedCallResolutionResult(callResolutionResult.resultCallAtom, diagnostics, commonSystem)
    }
}

data class ResolutionResultCallInfo<D : CallableDescriptor>(
    konst resolutionResult: CallResolutionResult,
    konst overloadResolutionResults: OverloadResolutionResults<D>
)

abstract class CallInfo(
    open konst callResolutionResult: SingleCallResolutionResult,
    konst context: BasicCallResolutionContext,
    konst tracingStrategy: TracingStrategy
)

class PSIPartialCallInfo(
    override konst callResolutionResult: PartialCallResolutionResult,
    context: BasicCallResolutionContext,
    tracingStrategy: TracingStrategy
) : CallInfo(callResolutionResult, context, tracingStrategy), PartialCallInfo

class PSICompletedCallInfo(
    override konst callResolutionResult: CompletedCallResolutionResult,
    context: BasicCallResolutionContext,
    konst resolvedCall: NewAbstractResolvedCall<*>,
    tracingStrategy: TracingStrategy
) : CallInfo(callResolutionResult, context, tracingStrategy), CompletedCallInfo

class PSIErrorCallInfo<D : CallableDescriptor>(
    override konst callResolutionResult: CallResolutionResult,
    konst result: OverloadResolutionResults<D>
) : ErrorCallInfo
