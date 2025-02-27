/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.daemon.report

import org.jetbrains.kotlin.build.report.ICReporter.ReportSeverity
import org.jetbrains.kotlin.build.report.ICReporterBase
import org.jetbrains.kotlin.build.report.RemoteICReporter
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.daemon.common.CompilationResultCategory
import org.jetbrains.kotlin.daemon.common.CompilationResults
import org.jetbrains.kotlin.daemon.common.CompileIterationResult
import java.io.File

internal class CompileIterationICReporter(
    private konst compilationResults: CompilationResults
) : ICReporterBase(), RemoteICReporter {
    override fun reportCompileIteration(incremental: Boolean, sourceFiles: Collection<File>, exitCode: ExitCode) {
        compilationResults.add(
            CompilationResultCategory.IC_COMPILE_ITERATION.code,
            CompileIterationResult(sourceFiles, exitCode.toString())
        )
    }

    override fun report(message: () -> String, severity: ReportSeverity) {
    }

    override fun flush() {
    }
}