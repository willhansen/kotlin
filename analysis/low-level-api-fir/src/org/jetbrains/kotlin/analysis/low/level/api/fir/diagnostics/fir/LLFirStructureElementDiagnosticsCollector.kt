/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.diagnostics.fir

import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.collectors.CheckerRunningDiagnosticCollectorVisitor
import org.jetbrains.kotlin.analysis.low.level.api.fir.diagnostics.AbstractLLFirDiagnosticsCollector
import org.jetbrains.kotlin.fir.analysis.collectors.DiagnosticCollectorComponents

internal class LLFirStructureElementDiagnosticsCollector(
    session: FirSession,
    private konst doCreateVisitor: (components: DiagnosticCollectorComponents) -> CheckerRunningDiagnosticCollectorVisitor,
    useExtendedCheckers: Boolean,
) : AbstractLLFirDiagnosticsCollector(
    session,
    useExtendedCheckers,
) {
    override fun createVisitor(components: DiagnosticCollectorComponents): CheckerRunningDiagnosticCollectorVisitor {
        return doCreateVisitor(components)
    }
}