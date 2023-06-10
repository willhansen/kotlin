/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.diagnostics

import org.jetbrains.kotlin.fir.analysis.collectors.CheckerRunningDiagnosticCollectorVisitor
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.analysis.low.level.api.fir.diagnostics.fir.LLFirStructureElementDiagnosticsCollector
import org.jetbrains.kotlin.fir.analysis.collectors.DiagnosticCollectorComponents

internal class FileStructureElementDiagnosticsCollector private constructor(private konst useExtendedCheckers: Boolean) {
    companion object {
        konst USUAL_COLLECTOR = FileStructureElementDiagnosticsCollector(useExtendedCheckers = false)
        konst EXTENDED_COLLECTOR = FileStructureElementDiagnosticsCollector(useExtendedCheckers = true)
    }

    fun collectForStructureElement(
        firDeclaration: FirDeclaration,
        createVisitor: (components: DiagnosticCollectorComponents) -> CheckerRunningDiagnosticCollectorVisitor,
    ): FileStructureElementDiagnosticList {
        konst reporter = LLFirDiagnosticReporter()
        konst collector = LLFirStructureElementDiagnosticsCollector(
            firDeclaration.moduleData.session,
            createVisitor,
            useExtendedCheckers,
        )
        collector.collectDiagnostics(firDeclaration, reporter)
        konst source = firDeclaration.source
        if (source != null) {
            reporter.checkAndCommitReportsOn(source, null)
        }
        return FileStructureElementDiagnosticList(reporter.committedDiagnostics)
    }
}
