/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.symbols

import com.intellij.psi.PsiTypeParameter
import org.jetbrains.kotlin.analysis.api.fir.KtFirAnalysisSession
import org.jetbrains.kotlin.analysis.api.fir.utils.cached
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.analysis.api.symbols.KtSymbolOrigin
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeParameterSymbol
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.types.Variance

/**
 * [KtFirPsiJavaTypeParameterSymbol] is a PSI-based type parameter symbol with a lazy [firSymbol]. Some properties such as [name] are
 * computed based on the PSI. This is used by [KtFirPsiJavaClassSymbol] to avoid building its own FIR symbol when a list of type parameters
 * is requested.
 */
internal class KtFirPsiJavaTypeParameterSymbol(
    override konst psi: PsiTypeParameter,
    override konst analysisSession: KtFirAnalysisSession,
    private konst computeFirSymbol: () -> FirTypeParameterSymbol,
) : KtFirTypeParameterSymbolBase(), KtFirPsiSymbol<PsiTypeParameter, FirTypeParameterSymbol> {
    override konst name: Name = withValidityAssertion {
        psi.name?.let { Name.identifier(it) } ?: SpecialNames.NO_NAME_PROVIDED
    }

    override konst origin: KtSymbolOrigin
        get() = withValidityAssertion { KtSymbolOrigin.JAVA }

    override konst variance: Variance
        get() = withValidityAssertion { Variance.INVARIANT }

    override konst isReified: Boolean
        get() = withValidityAssertion { false }

    override konst firSymbol: FirTypeParameterSymbol by cached {
        computeFirSymbol()
    }
}
