/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.symbols

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.fir.KtFirAnalysisSession
import org.jetbrains.kotlin.analysis.api.fir.findPsi
import org.jetbrains.kotlin.analysis.api.fir.utils.cached
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.Variance

internal class KtFirTypeParameterSymbol(
    override konst firSymbol: FirTypeParameterSymbol,
    override konst analysisSession: KtFirAnalysisSession,
) : KtFirTypeParameterSymbolBase() {
    override konst token: KtLifetimeToken get() = builder.token
    override konst psi: PsiElement? by cached { firSymbol.findPsi() }

    override konst name: Name get() = withValidityAssertion { firSymbol.name }

    override konst variance: Variance get() = withValidityAssertion { firSymbol.variance }
    override konst isReified: Boolean get() = withValidityAssertion { firSymbol.isReified }
}
