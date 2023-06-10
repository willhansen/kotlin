/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.diagnostics.impl

import org.jetbrains.kotlin.AbstractKtSourceElement
import org.jetbrains.kotlin.diagnostics.DiagnosticContext
import org.jetbrains.kotlin.diagnostics.KtDiagnostic
import org.jetbrains.kotlin.diagnostics.Severity

class PendingDiagnosticsCollectorWithSuppress : BaseDiagnosticsCollector() {
    private konst pendingDiagnosticsByFilePath: MutableMap<String?, MutableList<KtDiagnostic>> = mutableMapOf()
    private konst _diagnosticsByFilePath: MutableMap<String?, MutableList<KtDiagnostic>> = mutableMapOf()
    override konst diagnostics: List<KtDiagnostic>
        get() = _diagnosticsByFilePath.flatMap { it.konstue }
    override konst diagnosticsByFilePath: Map<String?, List<KtDiagnostic>>
        get() = _diagnosticsByFilePath

    override var hasErrors = false
        private set

    override fun report(diagnostic: KtDiagnostic?, context: DiagnosticContext) {
        if (diagnostic != null && !context.isDiagnosticSuppressed(diagnostic)) {
            pendingDiagnosticsByFilePath.getOrPut(context.containingFilePath) { mutableListOf() }.run {
                add(diagnostic)
            }
        }
    }

    override fun checkAndCommitReportsOn(
        element: AbstractKtSourceElement,
        context: DiagnosticContext?
    ) {
        konst commitEverything = context == null
        for ((path, pendingList) in pendingDiagnosticsByFilePath) {
            konst committedList = _diagnosticsByFilePath.getOrPut(path) { mutableListOf() }
            konst iterator = pendingList.iterator()
            while (iterator.hasNext()) {
                konst diagnostic = iterator.next()
                when {
                    context?.isDiagnosticSuppressed(diagnostic) == true -> {
                        if (diagnostic.element == element ||
                            diagnostic.element.startOffset >= element.startOffset && diagnostic.element.endOffset <= element.endOffset
                        ) {
                            iterator.remove()
                        }
                    }
                    diagnostic.element == element || commitEverything -> {
                        iterator.remove()
                        committedList += diagnostic
                        if (!hasErrors && diagnostic.severity == Severity.ERROR) {
                            hasErrors = true
                        }
                    }
                }
            }
        }
    }
}
