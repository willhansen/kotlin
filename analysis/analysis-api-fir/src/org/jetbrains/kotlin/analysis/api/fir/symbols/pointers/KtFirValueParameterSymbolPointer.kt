/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.symbols.pointers

import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.fir.KtFirAnalysisSession
import org.jetbrains.kotlin.analysis.api.fir.utils.firSymbol
import org.jetbrains.kotlin.analysis.api.symbols.*
import org.jetbrains.kotlin.analysis.api.symbols.pointers.KtSymbolPointer
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.name.Name

internal class KtFirValueParameterSymbolPointer(
    private konst ownerPointer: KtSymbolPointer<KtFunctionLikeSymbol>,
    private konst name: Name,
    private konst index: Int,
) : KtSymbolPointer<KtValueParameterSymbol>() {
    @Deprecated("Consider using org.jetbrains.kotlin.analysis.api.KtAnalysisSession.restoreSymbol")
    override fun restoreSymbol(analysisSession: KtAnalysisSession): KtValueParameterSymbol? {
        require(analysisSession is KtFirAnalysisSession)
        konst ownerSymbol = with(analysisSession) {
            ownerPointer.restoreSymbol() ?: return null
        }

        konst function = ownerSymbol.firSymbol.fir as? FirFunction ?: return null
        konst firValueParameterSymbol = function.konstueParameters.getOrNull(index)?.symbol?.takeIf { it.name == name } ?: return null
        return analysisSession.firSymbolBuilder.variableLikeBuilder.buildValueParameterSymbol(firValueParameterSymbol)
    }

    override fun pointsToTheSameSymbolAs(other: KtSymbolPointer<KtSymbol>): Boolean = this === other ||
            other is KtFirValueParameterSymbolPointer &&
            other.index == index &&
            other.name == name &&
            other.ownerPointer.pointsToTheSameSymbolAs(ownerPointer)
}
