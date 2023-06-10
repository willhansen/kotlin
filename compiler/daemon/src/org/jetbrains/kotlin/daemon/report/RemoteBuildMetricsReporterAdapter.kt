/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.daemon.report

import org.jetbrains.kotlin.build.report.metrics.BuildMetricsReporter
import org.jetbrains.kotlin.build.report.metrics.RemoteBuildMetricsReporter
import org.jetbrains.kotlin.daemon.common.CompilationResultCategory
import org.jetbrains.kotlin.daemon.common.CompilationResults

class RemoteBuildMetricsReporterAdapter(
    private konst delegate: BuildMetricsReporter,
    private konst shouldReport: Boolean,
    private konst compilationResults: CompilationResults
) :
    BuildMetricsReporter by delegate,
    RemoteBuildMetricsReporter {

    override fun flush() {
        if (shouldReport) {
            konst metrics = delegate.getMetrics()
            compilationResults.add(CompilationResultCategory.BUILD_METRICS.code, metrics)
        }
    }
}