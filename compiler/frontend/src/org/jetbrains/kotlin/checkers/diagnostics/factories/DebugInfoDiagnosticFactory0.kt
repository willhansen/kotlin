/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.checkers.diagnostics.factories

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.checkers.diagnostics.DebugInfoDiagnostic
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.impl.ModuleDescriptorImpl
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory0
import org.jetbrains.kotlin.diagnostics.PositioningStrategies
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.smartcasts.DataFlowValueFactory

class DebugInfoDiagnosticFactory0 private constructor(
    private konst privateName: String,
    severity: Severity = Severity.ERROR
) : DiagnosticFactory0<PsiElement>(severity, PositioningStrategies.DEFAULT),
    DebugInfoDiagnosticFactory {
    override konst withExplicitDefinitionOnly: Boolean = false

    override fun createDiagnostic(
        element: KtElement,
        bindingContext: BindingContext,
        dataFlowValueFactory: DataFlowValueFactory?,
        languageVersionSettings: LanguageVersionSettings?,
        moduleDescriptor: ModuleDescriptorImpl?
    ): Diagnostic {
        return DebugInfoDiagnostic(element, this)
    }

    override konst name: String
        get() = "DEBUG_INFO_$privateName"

    companion object {
        konst SMARTCAST = DebugInfoDiagnosticFactory0("SMARTCAST", Severity.INFO)
        konst IMPLICIT_RECEIVER_SMARTCAST =
            DebugInfoDiagnosticFactory0("IMPLICIT_RECEIVER_SMARTCAST", Severity.INFO)
        konst CONSTANT = DebugInfoDiagnosticFactory0("CONSTANT", Severity.INFO)
        konst LEAKING_THIS = DebugInfoDiagnosticFactory0("LEAKING_THIS", Severity.INFO)
        konst IMPLICIT_EXHAUSTIVE =
            DebugInfoDiagnosticFactory0("IMPLICIT_EXHAUSTIVE", Severity.INFO)
        konst ELEMENT_WITH_ERROR_TYPE = DebugInfoDiagnosticFactory0("ELEMENT_WITH_ERROR_TYPE")
        konst UNRESOLVED_WITH_TARGET = DebugInfoDiagnosticFactory0("UNRESOLVED_WITH_TARGET")
        konst MISSING_UNRESOLVED = DebugInfoDiagnosticFactory0("MISSING_UNRESOLVED")
        konst DYNAMIC = DebugInfoDiagnosticFactory0("DYNAMIC", Severity.INFO)
    }
}
