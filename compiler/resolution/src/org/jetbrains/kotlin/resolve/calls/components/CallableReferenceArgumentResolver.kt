/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.components

import org.jetbrains.kotlin.resolve.calls.inference.ConstraintSystemBuilder
import org.jetbrains.kotlin.resolve.calls.inference.components.NewTypeSubstitutor
import org.jetbrains.kotlin.resolve.calls.model.*
import org.jetbrains.kotlin.resolve.calls.tower.VisibilityError
import org.jetbrains.kotlin.resolve.calls.tower.VisibilityErrorOnArgument
import org.jetbrains.kotlin.resolve.calls.tower.isInapplicable

class CallableReferenceArgumentResolver(konst callableReferenceOverloadConflictResolver: CallableReferenceOverloadConflictResolver) {
    fun processCallableReferenceArgument(
        csBuilder: ConstraintSystemBuilder,
        resolvedAtom: ResolvedCallableReferenceArgumentAtom,
        diagnosticsHolder: KotlinDiagnosticsHolder,
        resolutionCallbacks: KotlinResolutionCallbacks
    ) {
        konst argument = resolvedAtom.atom
        konst expectedType = resolvedAtom.expectedType?.let { (csBuilder.buildCurrentSubstitutor() as NewTypeSubstitutor).safeSubstitute(it) }
        konst candidates = resolutionCallbacks.resolveCallableReferenceArgument(resolvedAtom.atom, expectedType, csBuilder.currentStorage())

        if (candidates.size > 1 && resolvedAtom is EagerCallableReferenceAtom) {
            if (candidates.all { it.resultingApplicability.isInapplicable }) {
                diagnosticsHolder.addDiagnostic(CallableReferenceCallCandidatesAmbiguity(argument, candidates))
            }

            resolvedAtom.setAnalyzedResults(
                candidate = null,
                subResolvedAtoms = listOf(resolvedAtom.transformToPostponed())
            )
            return
        }

        konst chosenCandidate = candidates.singleOrNull()
        if (chosenCandidate != null) {
            konst toFreshSubstitutor = CreateFreshVariablesSubstitutor.createToFreshVariableSubstitutorAndAddInitialConstraints(
                chosenCandidate.candidate,
                resolvedAtom.atom.call,
                csBuilder
            )
            chosenCandidate.addConstraints(csBuilder, toFreshSubstitutor, callableReference = argument)
            chosenCandidate.diagnostics.forEach {
                konst transformedDiagnostic = when (it) {
                    is CompatibilityWarning -> CompatibilityWarningOnArgument(argument, it.candidate)
                    is VisibilityError -> VisibilityErrorOnArgument(argument, it.invisibleMember)
                    else -> it
                }
                diagnosticsHolder.addDiagnostic(transformedDiagnostic)
            }
            chosenCandidate.freshVariablesSubstitutor = toFreshSubstitutor
        } else {
            if (candidates.isEmpty()) {
                diagnosticsHolder.addDiagnostic(NoneCallableReferenceCallCandidates(argument))
            } else {
                diagnosticsHolder.addDiagnostic(CallableReferenceCallCandidatesAmbiguity(argument, candidates))
            }
        }

        // todo -- create this inside CallableReferencesCandidateFactory
        konst subKtArguments = listOfNotNull(buildResolvedKtArgument(argument.lhsResult))

        resolvedAtom.setAnalyzedResults(chosenCandidate, subKtArguments)
    }

    private fun buildResolvedKtArgument(lhsResult: LHSResult): ResolvedAtom? {
        if (lhsResult !is LHSResult.Expression) return null
        return when (konst lshCallArgument = lhsResult.lshCallArgument) {
            is SubKotlinCallArgument -> lshCallArgument.callResult
            is ExpressionKotlinCallArgument -> ResolvedExpressionAtom(lshCallArgument)
            else -> unexpectedArgument(lshCallArgument)
        }
    }
}

