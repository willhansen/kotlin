/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.symbols

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.annotations.KtAnnotationsList
import org.jetbrains.kotlin.analysis.api.base.KtContextReceiver
import org.jetbrains.kotlin.analysis.api.fir.KtFirAnalysisSession
import org.jetbrains.kotlin.analysis.api.fir.annotations.KtFirAnnotationListForDeclaration
import org.jetbrains.kotlin.analysis.api.fir.getAllowedPsi
import org.jetbrains.kotlin.analysis.api.fir.utils.cached
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.KtAnonymousFunctionSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtReceiverParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtValueParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.pointers.CanNotCreateSymbolPointerForLocalLibraryDeclarationException
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtPsiBasedSymbolPointer
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.fir.declarations.utils.hasStableParameterNames
import org.jetbrains.kotlin.fir.symbols.impl.FirAnonymousFunctionSymbol
import org.jetbrains.kotlin.fir.symbols.impl.isExtension

internal class KtFirAnonymousFunctionSymbol(
    override konst firSymbol: FirAnonymousFunctionSymbol,
    override konst analysisSession: KtFirAnalysisSession,
) : KtAnonymousFunctionSymbol(), KtFirSymbol<FirAnonymousFunctionSymbol> {

    override konst psi: PsiElement? = withValidityAssertion { firSymbol.fir.getAllowedPsi() }

    override konst annotationsList: KtAnnotationsList
        get() = withValidityAssertion {
            KtFirAnnotationListForDeclaration.create(firSymbol, analysisSession.useSiteSession, token)
        }

    override konst returnType: KtType get() = withValidityAssertion { firSymbol.returnType(analysisSession.firSymbolBuilder) }
    override konst receiverParameter: KtReceiverParameterSymbol? get() = withValidityAssertion { firSymbol.receiver(builder) }

    override konst contextReceivers: List<KtContextReceiver> by cached { firSymbol.createContextReceivers(builder) }

    override konst konstueParameters: List<KtValueParameterSymbol> by cached { firSymbol.createKtValueParameters(builder) }

    override konst hasStableParameterNames: Boolean
        get() = withValidityAssertion {
            firSymbol.fir.hasStableParameterNames
        }
    override konst isExtension: Boolean get() = withValidityAssertion { firSymbol.isExtension }


    context(KtAnalysisSession)
    override fun createPointer(): KtSymbolPointer<KtAnonymousFunctionSymbol> = withValidityAssertion {
        KtPsiBasedSymbolPointer.createForSymbolFromSource(this)
            ?: throw CanNotCreateSymbolPointerForLocalLibraryDeclarationException(this::class)
    }

    override fun equals(other: Any?): Boolean = symbolEquals(other)
    override fun hashCode(): Int = symbolHashCode()
}
