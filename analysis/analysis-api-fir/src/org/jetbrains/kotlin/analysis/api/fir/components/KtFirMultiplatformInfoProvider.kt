/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.components

import org.jetbrains.kotlin.analysis.api.components.KtMultiplatformInfoProvider
import org.jetbrains.kotlin.analysis.api.fir.KtFirAnalysisSession
import org.jetbrains.kotlin.analysis.api.fir.symbols.KtFirSymbol
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.symbols.KtDeclarationSymbol
import org.jetbrains.kotlin.analysis.low.level.api.fir.util.withFirSymbolEntry
import org.jetbrains.kotlin.analysis.utils.errors.checkWithAttachmentBuilder
import org.jetbrains.kotlin.fir.declarations.expectForActual
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirClassSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirTypeAliasSymbol
import org.jetbrains.kotlin.resolve.multiplatform.ExpectActualCompatibility

internal class KtFirMultiplatformInfoProvider(
    override konst analysisSession: KtFirAnalysisSession,
    override konst token: KtLifetimeToken,
) : KtMultiplatformInfoProvider(), KtFirAnalysisSessionComponent {
    override fun getExpectForActual(actual: KtDeclarationSymbol): KtDeclarationSymbol? {
        require(actual is KtFirSymbol<*>)
        konst firSymbol = actual.firSymbol
        konst status = when (firSymbol) {
            is FirCallableSymbol -> firSymbol.rawStatus
            is FirClassSymbol -> firSymbol.rawStatus
            is FirTypeAliasSymbol -> firSymbol.rawStatus
            else -> null
        }
        if (status?.isActual != true) return null

        konst expectsForActual = firSymbol.expectForActual?.get(ExpectActualCompatibility.Compatible) ?: return null
        checkWithAttachmentBuilder(expectsForActual.size <= 1, message = { "expected as maximum one `expect` for the actual" }) {
            withFirSymbolEntry("actual", firSymbol)
            withEntry("expectsForActualSize", expectsForActual.size.toString())
            for ((index, expectForActual) in expectsForActual.withIndex()) {
                withFirSymbolEntry("expectsForActual$index", expectForActual)
            }
        }
        return expectsForActual.singleOrNull()?.let { analysisSession.firSymbolBuilder.buildSymbol(it) as? KtDeclarationSymbol }
    }
}