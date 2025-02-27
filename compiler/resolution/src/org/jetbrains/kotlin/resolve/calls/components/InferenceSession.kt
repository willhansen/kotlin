/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.components

import org.jetbrains.kotlin.resolve.calls.components.candidate.ResolutionCandidate
import org.jetbrains.kotlin.resolve.calls.inference.ConstraintSystemBuilder
import org.jetbrains.kotlin.resolve.calls.inference.components.ConstraintSystemCompletionMode
import org.jetbrains.kotlin.resolve.calls.inference.model.ConstraintStorage
import org.jetbrains.kotlin.resolve.calls.model.*
import org.jetbrains.kotlin.types.TypeConstructor
import org.jetbrains.kotlin.types.UnwrappedType

interface InferenceSession {
    konst parentSession: InferenceSession?

    companion object {
        konst default = object : InferenceSession {
            override konst parentSession: InferenceSession? = null

            override fun shouldRunCompletion(candidate: ResolutionCandidate): Boolean = true
            override fun addPartialCallInfo(callInfo: PartialCallInfo) {}
            override fun addErrorCallInfo(callInfo: ErrorCallInfo) {}
            override fun addCompletedCallInfo(callInfo: CompletedCallInfo) {}
            override fun currentConstraintSystem(): ConstraintStorage = ConstraintStorage.Empty
            override fun inferPostponedVariables(
                lambda: ResolvedLambdaAtom,
                constraintSystemBuilder: ConstraintSystemBuilder,
                completionMode: ConstraintSystemCompletionMode,
                diagnosticsHolder: KotlinDiagnosticsHolder
            ): Map<TypeConstructor, UnwrappedType> = emptyMap()

            override fun initializeLambda(lambda: ResolvedLambdaAtom) { }

            override fun writeOnlyStubs(callInfo: SingleCallResolutionResult): Boolean = false
            override fun callCompleted(resolvedAtom: ResolvedAtom): Boolean = false
            override fun shouldCompleteResolvedSubAtomsOf(resolvedCallAtom: ResolvedCallAtom) = true
            override fun computeCompletionMode(
                candidate: ResolutionCandidate
            ): ConstraintSystemCompletionMode? = null

            override fun resolveReceiverIndependently(): Boolean = false
        }
    }

    fun shouldRunCompletion(candidate: ResolutionCandidate): Boolean
    fun addPartialCallInfo(callInfo: PartialCallInfo)
    fun addCompletedCallInfo(callInfo: CompletedCallInfo)
    fun addErrorCallInfo(callInfo: ErrorCallInfo)
    fun currentConstraintSystem(): ConstraintStorage
    fun inferPostponedVariables(
        lambda: ResolvedLambdaAtom,
        constraintSystemBuilder: ConstraintSystemBuilder,
        completionMode: ConstraintSystemCompletionMode,
        diagnosticsHolder: KotlinDiagnosticsHolder
    ): Map<TypeConstructor, UnwrappedType>?

    fun initializeLambda(lambda: ResolvedLambdaAtom)

    fun writeOnlyStubs(callInfo: SingleCallResolutionResult): Boolean
    fun callCompleted(resolvedAtom: ResolvedAtom): Boolean
    fun shouldCompleteResolvedSubAtomsOf(resolvedCallAtom: ResolvedCallAtom): Boolean
    fun computeCompletionMode(candidate: ResolutionCandidate): ConstraintSystemCompletionMode?
    fun resolveReceiverIndependently(): Boolean
}

interface PartialCallInfo {
    konst callResolutionResult: PartialCallResolutionResult
}

interface CompletedCallInfo {
    konst callResolutionResult: CompletedCallResolutionResult
}

interface ErrorCallInfo {
    konst callResolutionResult: CallResolutionResult
}
