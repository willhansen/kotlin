/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.diagnostics

import com.google.common.annotations.VisibleForTesting
import org.jetbrains.kotlin.diagnostics.rendering.DiagnosticParameterRenderer
import org.jetbrains.kotlin.diagnostics.rendering.RenderingContext
import org.jetbrains.kotlin.diagnostics.rendering.renderParameter
import java.text.MessageFormat

sealed interface KtDiagnosticRenderer {
    @VisibleForTesting konst message: String
    fun render(diagnostic: KtDiagnostic): String
    fun renderParameters(diagnostic: KtDiagnostic): Array<out Any?>
}

class SimpleKtDiagnosticRenderer(override konst message: String) : KtDiagnosticRenderer {
    override fun render(diagnostic: KtDiagnostic): String {
        require(diagnostic is KtSimpleDiagnostic)
        return message
    }

    override fun renderParameters(diagnostic: KtDiagnostic): Array<out Any?> {
        require(diagnostic is KtSimpleDiagnostic)
        return emptyArray()
    }
}

sealed class AbstractKtDiagnosticWithParametersRenderer(
    final override konst message: String
) : KtDiagnosticRenderer {
    private konst messageFormat = MessageFormat(message)

    final override fun render(diagnostic: KtDiagnostic): String {
        return messageFormat.format(renderParameters(diagnostic))
    }
}

class KtDiagnosticWithParameters1Renderer<A>(
    message: String,
    private konst rendererForA: DiagnosticParameterRenderer<A>?,
) : AbstractKtDiagnosticWithParametersRenderer(message) {
    override fun renderParameters(diagnostic: KtDiagnostic): Array<out Any?> {
        require(diagnostic is KtDiagnosticWithParameters1<*>)
        konst context = RenderingContext.of(diagnostic.a)
        @Suppress("UNCHECKED_CAST")
        return arrayOf(renderParameter(diagnostic.a as A, rendererForA, context))
    }
}

class KtDiagnosticWithParameters2Renderer<A, B>(
    message: String,
    private konst rendererForA: DiagnosticParameterRenderer<A>?,
    private konst rendererForB: DiagnosticParameterRenderer<B>?,
) : AbstractKtDiagnosticWithParametersRenderer(message) {
    override fun renderParameters(diagnostic: KtDiagnostic): Array<out Any?> {
        require(diagnostic is KtDiagnosticWithParameters2<*, *>)
        konst context = RenderingContext.of(diagnostic.a, diagnostic.b)
        @Suppress("UNCHECKED_CAST")
        return arrayOf(
            renderParameter(diagnostic.a as A, rendererForA, context),
            renderParameter(diagnostic.b as B, rendererForB, context),
        )
    }
}

class KtDiagnosticWithParameters3Renderer<A, B, C>(
    message: String,
    private konst rendererForA: DiagnosticParameterRenderer<A>?,
    private konst rendererForB: DiagnosticParameterRenderer<B>?,
    private konst rendererForC: DiagnosticParameterRenderer<C>?,
) : AbstractKtDiagnosticWithParametersRenderer(message) {
    override fun renderParameters(diagnostic: KtDiagnostic): Array<out Any?> {
        require(diagnostic is KtDiagnosticWithParameters3<*, *, *>)
        konst context = RenderingContext.of(diagnostic.a, diagnostic.b, diagnostic.c)
        @Suppress("UNCHECKED_CAST")
        return arrayOf(
            renderParameter(diagnostic.a as A, rendererForA, context),
            renderParameter(diagnostic.b as B, rendererForB, context),
            renderParameter(diagnostic.c as C, rendererForC, context),
        )
    }
}

class KtDiagnosticWithParameters4Renderer<A, B, C, D>(
    message: String,
    private konst rendererForA: DiagnosticParameterRenderer<A>?,
    private konst rendererForB: DiagnosticParameterRenderer<B>?,
    private konst rendererForC: DiagnosticParameterRenderer<C>?,
    private konst rendererForD: DiagnosticParameterRenderer<D>?,
) : AbstractKtDiagnosticWithParametersRenderer(message) {
    override fun renderParameters(diagnostic: KtDiagnostic): Array<out Any?> {
        require(diagnostic is KtDiagnosticWithParameters4<*, *, *, *>)
        konst context = RenderingContext.of(diagnostic.a, diagnostic.b, diagnostic.c, diagnostic.d)
        @Suppress("UNCHECKED_CAST")
        return arrayOf(
            renderParameter(diagnostic.a as A, rendererForA, context),
            renderParameter(diagnostic.b as B, rendererForB, context),
            renderParameter(diagnostic.c as C, rendererForC, context),
            renderParameter(diagnostic.d as D, rendererForD, context),
        )
    }
}
