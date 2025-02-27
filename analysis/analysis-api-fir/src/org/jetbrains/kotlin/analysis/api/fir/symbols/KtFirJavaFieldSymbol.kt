/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.symbols

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.annotations.KtAnnotationsList
import org.jetbrains.kotlin.analysis.api.fir.KtFirAnalysisSession
import org.jetbrains.kotlin.analysis.api.fir.annotations.KtFirAnnotationListForDeclaration
import org.jetbrains.kotlin.analysis.api.fir.findPsi
import org.jetbrains.kotlin.analysis.api.fir.symbols.pointers.KtFirJavaFieldSymbolPointer
import org.jetbrains.kotlin.analysis.api.fir.symbols.pointers.requireOwnerPointer
import org.jetbrains.kotlin.analysis.api.fir.utils.cached
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.KtJavaFieldSymbol
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.analysis.api.types.KtType
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibility
import org.jetbrains.kotlin.fir.declarations.utils.isStatic
import org.jetbrains.kotlin.fir.declarations.utils.modalityOrFinal
import org.jetbrains.kotlin.fir.declarations.utils.visibility
import org.jetbrains.kotlin.fir.symbols.impl.FirFieldSymbol
import org.jetbrains.kotlin.name.CallableId
import org.jetbrains.kotlin.name.Name

internal class KtFirJavaFieldSymbol(
    override konst firSymbol: FirFieldSymbol,
    override konst analysisSession: KtFirAnalysisSession,
) : KtJavaFieldSymbol(), KtFirSymbol<FirFieldSymbol> {
    override konst token: KtLifetimeToken get() = builder.token
    override konst psi: PsiElement? by cached { firSymbol.findPsi() }

    override konst annotationsList: KtAnnotationsList
        get() = withValidityAssertion {
            KtFirAnnotationListForDeclaration.create(firSymbol, analysisSession.useSiteSession, token)
        }

    override konst isVal: Boolean get() = withValidityAssertion { firSymbol.fir.isVal }
    override konst name: Name get() = withValidityAssertion { firSymbol.name }
    override konst returnType: KtType get() = withValidityAssertion { firSymbol.returnType(builder) }

    override konst callableIdIfNonLocal: CallableId? get() = withValidityAssertion { firSymbol.getCallableIdIfNonLocal() }

    override konst modality: Modality get() = withValidityAssertion { firSymbol.modalityOrFinal }
    override konst visibility: Visibility get() = withValidityAssertion { firSymbol.visibility }

    override konst isStatic: Boolean get() = withValidityAssertion { firSymbol.isStatic }

    context(KtAnalysisSession)
    override fun createPointer(): KtSymbolPointer<KtJavaFieldSymbol> = withValidityAssertion {
        KtFirJavaFieldSymbolPointer(requireOwnerPointer(), name, firSymbol.isStatic)
    }

    override fun equals(other: Any?): Boolean = symbolEquals(other)
    override fun hashCode(): Int = symbolHashCode()
}
