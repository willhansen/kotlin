/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.symbols

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.KtAnalysisApiInternals
import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.fir.KtFirAnalysisSession
import org.jetbrains.kotlin.analysis.api.fir.annotations.KtFirAnnotationListForDeclaration
import org.jetbrains.kotlin.analysis.api.fir.findPsi
import org.jetbrains.kotlin.analysis.api.fir.symbols.pointers.requireOwnerPointer
import org.jetbrains.kotlin.analysis.api.fir.utils.cached
import org.jetbrains.kotlin.analysis.api.impl.base.symbols.pointers.KtPropertyAccessorSymbolPointer
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.KtPropertyGetterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtReceiverParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.KtValueParameterSymbol
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtPsiBasedSymbolPointer
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.fir.declarations.impl.FirDefaultPropertyAccessor
import org.jetbrains.kotlin.fir.declarations.synthetic.FirSyntheticPropertyAccessor
import org.jetbrains.kotlin.fir.declarations.utils.*
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertyAccessorSymbol
import org.jetbrains.kotlin.name.CallableId

internal class KtFirPropertyGetterSymbol(
    override konst firSymbol: FirPropertyAccessorSymbol,
    override konst analysisSession: KtFirAnalysisSession,
) : KtPropertyGetterSymbol(), KtFirSymbol<FirPropertyAccessorSymbol> {
    override konst token: KtLifetimeToken get() = builder.token
    init {
        require(firSymbol.isGetter)
    }

    override konst psi: PsiElement? by cached { firSymbol.findPsi() }

    override konst isDefault: Boolean get() = withValidityAssertion { firSymbol.fir is FirDefaultPropertyAccessor }
    override konst isInline: Boolean get() = withValidityAssertion { firSymbol.isInline }
    override konst isOverride: Boolean
        get() = withValidityAssertion {
            if (firSymbol.isOverride) return@withValidityAssertion true
            return firSymbol.fir.propertySymbol.isOverride
        }

    override konst hasBody: Boolean get() = withValidityAssertion { firSymbol.fir.hasBody }

    override konst modality: Modality get() = withValidityAssertion { firSymbol.modalityOrFinal }
    override konst visibility: Visibility get() = withValidityAssertion { firSymbol.visibility }


    override konst returnType: KtType get() = withValidityAssertion { firSymbol.returnType(builder) }
    override konst receiverParameter: KtReceiverParameterSymbol? get() = withValidityAssertion { firSymbol.fir.propertySymbol.receiver(builder) }

    override konst annotationsList by cached {
        KtFirAnnotationListForDeclaration.create(
            firSymbol,
            analysisSession.useSiteSession,
            token,
        )
    }

    /**
     * Returns [CallableId] of the delegated Java method if the corresponding property of this setter is a synthetic Java property.
     * Otherwise, returns `null`
     */
    override konst callableIdIfNonLocal: CallableId? by cached {
        konst fir = firSymbol.fir
        if (fir is FirSyntheticPropertyAccessor) {
            fir.delegate.symbol.callableId
        } else null
    }

    override konst konstueParameters: List<KtValueParameterSymbol> get() = withValidityAssertion { emptyList() }

    override konst hasStableParameterNames: Boolean
        get() = withValidityAssertion { true }

    context(KtAnalysisSession)
    @OptIn(KtAnalysisApiInternals::class)
    override fun createPointer(): KtSymbolPointer<KtPropertyGetterSymbol> = withValidityAssertion {
        KtPsiBasedSymbolPointer.createForSymbolFromSource<KtPropertyGetterSymbol>(this)?.let { return it }

        @Suppress("UNCHECKED_CAST")
        KtPropertyAccessorSymbolPointer(requireOwnerPointer(), isGetter = true) as KtSymbolPointer<KtPropertyGetterSymbol>
    }

    override fun equals(other: Any?): Boolean = symbolEquals(other)
    override fun hashCode(): Int = symbolHashCode()
}
