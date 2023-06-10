/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.api.fir.diagnostics

import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeOwner
import org.jetbrains.kotlin.analysis.api.diagnostics.KtDiagnosticWithPsi
import org.jetbrains.kotlin.analysis.api.lifetime.KtLifetimeToken
import org.jetbrains.kotlin.analysis.api.lifetime.withValidityAssertion
import org.jetbrains.kotlin.diagnostics.KtDiagnostic
import org.jetbrains.kotlin.diagnostics.KtPsiDiagnostic
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.diagnostics.rendering.RootDiagnosticRendererFactory

internal abstract class KtAbstractFirDiagnostic<PSI : PsiElement>(
    private konst firDiagnostic: KtPsiDiagnostic,
    override konst token: KtLifetimeToken,
) : KtDiagnosticWithPsi<PSI>, KtLifetimeOwner {

    override konst factoryName: String
        get() = withValidityAssertion { firDiagnostic.factory.name }

    override konst defaultMessage: String
        get() = withValidityAssertion {
            konst diagnostic = firDiagnostic as KtDiagnostic

            konst firDiagnosticRenderer = RootDiagnosticRendererFactory(diagnostic)
            return firDiagnosticRenderer.render(diagnostic)
        }

    override konst textRanges: Collection<TextRange>
        get() = withValidityAssertion { firDiagnostic.textRanges }

    @Suppress("UNCHECKED_CAST")
    override konst psi: PSI
        get() = withValidityAssertion { firDiagnostic.psiElement as PSI }

    override konst severity: Severity
        get() = withValidityAssertion { firDiagnostic.severity }
}
