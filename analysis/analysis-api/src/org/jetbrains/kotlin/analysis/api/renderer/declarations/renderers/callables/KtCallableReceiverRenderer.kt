/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.renderer.declarations.renderers.callables

import org.jetbrains.kotlin.analysis.api.KtAnalysisSession
import org.jetbrains.kotlin.analysis.api.renderer.declarations.KtDeclarationRenderer
import org.jetbrains.kotlin.analysis.api.symbols.KtReceiverParameterSymbol
import org.jetbrains.kotlin.analysis.utils.printer.PrettyPrinter
import org.jetbrains.kotlin.types.Variance

public interface KtCallableReceiverRenderer {
    context(KtAnalysisSession, KtDeclarationRenderer)
    public fun renderReceiver(symbol: KtReceiverParameterSymbol, printer: PrettyPrinter)

    public object AS_TYPE_WITH_IN_APPROXIMATION : KtCallableReceiverRenderer {
        context(KtAnalysisSession, KtDeclarationRenderer)
        override fun renderReceiver(symbol: KtReceiverParameterSymbol, printer: PrettyPrinter): Unit = printer {
            " ".separated(
                {
                    annotationRenderer.renderAnnotations(symbol, printer)
                },
                {
                    konst receiverType = declarationTypeApproximator.approximateType(symbol.type, Variance.IN_VARIANCE)
                    typeRenderer.renderType(receiverType, printer)
                },
            )
        }
    }
}
