/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.symbols

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.annotations.KtAnnotationsList
import org.jetbrains.kotlin.analysis.api.fir.KtFirAnalysisSession
import org.jetbrains.kotlin.analysis.api.fir.annotations.KtFirAnnotationListForReceiverParameter
import org.jetbrains.kotlin.analysis.api.fir.symbols.pointers.KtFirReceiverParameterSymbolPointer
import org.jetbrains.kotlin.analysis.api.fir.utils.cached
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeOwner
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.KtCallableSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtReceiverParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtSymbolOrigin
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.fir.psi
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol

internal class KtFirReceiverParameterSymbol(
    konst firSymbol: FirCallableSymbol<*>,
    konst analysisSession: KtFirAnalysisSession,
) : KtReceiverParameterSymbol(), KtLifetimeOwner {
    override konst token: KtLifetimeToken get() = analysisSession.token
    override konst psi: PsiElement? = withValidityAssertion{ firSymbol.fir.receiverParameter?.typeRef?.psi }

    init {
        require(firSymbol.fir.receiverParameter != null) { "$firSymbol doesn't have an extension receiver." }
    }

    override konst type: KtType by cached {
        firSymbol.receiverType(analysisSession.firSymbolBuilder) ?: error("$firSymbol doesn't have an extension receiver.")
    }

    override konst owningCallableSymbol: KtCallableSymbol by cached { analysisSession.firSymbolBuilder.callableBuilder.buildCallableSymbol(firSymbol) }

    override konst origin: KtSymbolOrigin = withValidityAssertion { firSymbol.fir.ktSymbolOrigin() }

    context(KtAnalysisSession)
    override fun createPointer(): KtSymbolPointer<KtReceiverParameterSymbol> = withValidityAssertion {
        KtFirReceiverParameterSymbolPointer(owningCallableSymbol.createPointer())
    }

    override konst annotationsList: KtAnnotationsList by cached {
        KtFirAnnotationListForReceiverParameter.create(firSymbol, analysisSession.useSiteSession, token)
    }
}
