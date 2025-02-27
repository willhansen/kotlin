/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.components

import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeOwner
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.KtCallableSymbol
import org.jetbrains.kotlin.analysis.api.types.KtSubstitutor
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtSimpleNameExpression

public abstract class KtCompletionCandidateChecker : KtAnalysisSessionComponent() {
    public abstract fun checkExtensionFitsCandidate(
        firSymbolForCandidate: KtCallableSymbol,
        originalFile: KtFile,
        nameExpression: KtSimpleNameExpression,
        possibleExplicitReceiver: KtExpression?,
    ): KtExtensionApplicabilityResult
}


public sealed class KtExtensionApplicabilityResult : KtLifetimeOwner {
    public sealed class Applicable : KtExtensionApplicabilityResult() {
        public abstract konst receiverCastRequired: Boolean
        public abstract konst substitutor: KtSubstitutor
    }

    public class ApplicableAsExtensionCallable(
        private konst _substitutor: KtSubstitutor,
        private konst _receiverCastRequired: Boolean,
        override konst token: KtLifetimeToken
    ) : Applicable() {
        override konst substitutor: KtSubstitutor = withValidityAssertion { _substitutor }
        override konst receiverCastRequired: Boolean get() = withValidityAssertion { _receiverCastRequired }
    }

    public class ApplicableAsFunctionalVariableCall(
        private konst _substitutor: KtSubstitutor,
        private konst _receiverCastRequired: Boolean,
        override konst token: KtLifetimeToken
    ) : Applicable() {
        override konst substitutor: KtSubstitutor get() = withValidityAssertion { _substitutor }
        override konst receiverCastRequired: Boolean get() = withValidityAssertion { _receiverCastRequired }
    }

    public class NonApplicable(
        override konst token: KtLifetimeToken
    ) : KtExtensionApplicabilityResult()
}

public interface KtCompletionCandidateCheckerMixIn : KtAnalysisSessionMixIn {
    public fun KtCallableSymbol.checkExtensionIsSuitable(
        originalPsiFile: KtFile,
        psiFakeCompletionExpression: KtSimpleNameExpression,
        psiReceiverExpression: KtExpression?,
    ): KtExtensionApplicabilityResult = withValidityAssertion {
        analysisSession.completionCandidateChecker.checkExtensionFitsCandidate(
            this,
            originalPsiFile,
            psiFakeCompletionExpression,
            psiReceiverExpression
        )
    }
}