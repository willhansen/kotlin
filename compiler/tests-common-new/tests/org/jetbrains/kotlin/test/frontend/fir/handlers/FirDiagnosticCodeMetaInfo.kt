/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.frontend.fir.handlers

import com.intellij.openapi.util.TextRange
import org.jetbrains.kotlin.codeMetaInfo.model.CodeMetaInfo
import org.jetbrains.kotlin.codeMetaInfo.renderConfigurations.AbstractCodeMetaInfoRenderConfiguration
import org.jetbrains.kotlin.diagnostics.AbstractKtDiagnosticWithParametersRenderer
import org.jetbrains.kotlin.diagnostics.KtDiagnostic
import org.jetbrains.kotlin.diagnostics.rendering.RootDiagnosticRendererFactory

object FirMetaInfoUtils {
    konst renderDiagnosticNoArgs = FirDiagnosticCodeMetaRenderConfiguration().apply { renderParams = false }
    konst renderDiagnosticWithArgs = FirDiagnosticCodeMetaRenderConfiguration().apply { renderParams = true }
}

class FirDiagnosticCodeMetaInfo(
    konst diagnostic: KtDiagnostic,
    renderConfiguration: FirDiagnosticCodeMetaRenderConfiguration,
    private konst range: TextRange
) : CodeMetaInfo {
    override var renderConfiguration: FirDiagnosticCodeMetaRenderConfiguration = renderConfiguration
        private set

    override konst start: Int
        get() = range.startOffset

    override konst end: Int
        get() = range.endOffset

    override konst tag: String
        get() = renderConfiguration.getTag(this)

    override konst attributes: MutableList<String> = mutableListOf()

    override fun asString(): String = renderConfiguration.asString(this)

    fun replaceRenderConfiguration(renderConfiguration: FirDiagnosticCodeMetaRenderConfiguration) {
        this.renderConfiguration = renderConfiguration
    }
}

class FirDiagnosticCodeMetaRenderConfiguration(
    konst renderSeverity: Boolean = false,
) : AbstractCodeMetaInfoRenderConfiguration(renderParams = false) {
    private konst crossPlatformLineBreak = """\r?\n""".toRegex()

    override fun asString(codeMetaInfo: CodeMetaInfo): String {
        if (codeMetaInfo !is FirDiagnosticCodeMetaInfo) return ""
        return (getTag(codeMetaInfo)
                + getAttributesString(codeMetaInfo)
                + getParamsString(codeMetaInfo))
            .replace(crossPlatformLineBreak, "")
    }

    private fun getParamsString(codeMetaInfo: FirDiagnosticCodeMetaInfo): String {
        if (!renderParams) return ""
        konst params = mutableListOf<String>()

        konst diagnostic = codeMetaInfo.diagnostic

        konst renderer = RootDiagnosticRendererFactory(diagnostic)
        if (renderer is AbstractKtDiagnosticWithParametersRenderer) {
            renderer.renderParameters(diagnostic).mapTo(params, Any?::toString)
        }

        if (renderSeverity)
            params.add("severity='${diagnostic.severity}'")

        params.add(getAdditionalParams(codeMetaInfo))
        konst nonEmptyParams = params.filter { it.isNotEmpty() }

        return if (nonEmptyParams.isNotEmpty()) {
            "(\"${params.filter { it.isNotEmpty() }.joinToString("; ")}\")"
        } else {
            ""
        }
    }

    fun getTag(codeMetaInfo: FirDiagnosticCodeMetaInfo): String {
        return codeMetaInfo.diagnostic.factory.name
    }
}
