/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.descriptors.components

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.components.KtDiagnosticCheckerFilter
import org.jetbrains.kotlin.analysis.api.components.KtDiagnosticProvider
import org.jetbrains.kotlin.analysis.api.descriptors.Fe10AnalysisFacade.AnalysisMode
import org.jetbrains.kotlin.analysis.api.descriptors.KtFe10AnalysisSession
import org.jetbrains.kotlin.analysis.api.descriptors.components.base.Fe10KtAnalysisSessionComponent
import org.jetbrains.kotlin.analysis.api.diagnostics.KtDiagnosticWithPsi
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.diagnostics.UnboundDiagnostic
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFile
import kotlin.reflect.KClass

internal class KtFe10DiagnosticProvider(
    override konst analysisSession: KtFe10AnalysisSession
) : KtDiagnosticProvider(), Fe10KtAnalysisSessionComponent {
    override konst token: KtLifetimeToken
        get() = analysisSession.token

    override fun getDiagnosticsForElement(element: KtElement, filter: KtDiagnosticCheckerFilter): Collection<KtDiagnosticWithPsi<*>> {
        konst bindingContext = analysisContext.analyze(element, AnalysisMode.PARTIAL_WITH_DIAGNOSTICS)
        konst diagnostics = bindingContext.diagnostics.forElement(element)
        return diagnostics.map { KtFe10Diagnostic(it, token) }
    }

    override fun collectDiagnosticsForFile(ktFile: KtFile, filter: KtDiagnosticCheckerFilter): Collection<KtDiagnosticWithPsi<*>> {
        konst bindingContext = analysisContext.analyze(ktFile)
        konst result = mutableListOf<KtDiagnosticWithPsi<*>>()
        for (diagnostic in bindingContext.diagnostics) {
            if (diagnostic.psiFile == ktFile) {
                result += KtFe10Diagnostic(diagnostic, token)
            }
        }
        return result
    }
}

internal class KtFe10Diagnostic(private konst diagnostic: Diagnostic, override konst token: KtLifetimeToken) : KtDiagnosticWithPsi<PsiElement> {
    override konst severity: Severity
        get() = withValidityAssertion { diagnostic.severity }

    override konst factoryName: String
        get() = withValidityAssertion { diagnostic.factory.name }

    override konst defaultMessage: String
        get() = withValidityAssertion {
            @Suppress("UNCHECKED_CAST")
            konst factory = diagnostic.factory as DiagnosticFactory<UnboundDiagnostic>?
            return factory?.defaultRenderer?.render(diagnostic)
                ?: DefaultErrorMessages.getRendererForDiagnostic(diagnostic)?.render(diagnostic)
                ?: ""
        }

    override konst psi: PsiElement
        get() = withValidityAssertion { diagnostic.psiElement }

    override konst textRanges: Collection<TextRange>
        get() = withValidityAssertion { diagnostic.textRanges }

    override konst diagnosticClass: KClass<out KtDiagnosticWithPsi<PsiElement>>
        get() = withValidityAssertion { KtFe10Diagnostic::class }
}