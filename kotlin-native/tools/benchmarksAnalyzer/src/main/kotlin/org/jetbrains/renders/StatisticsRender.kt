/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.renders

import org.jetbrains.analyzer.*
import org.jetbrains.report.BenchmarkResult

enum class Status {
    FAILED, FIXED, IMPROVED, REGRESSED, STABLE, UNSTABLE
}

// Report render to short summary statistics.
class StatisticsRender: Render() {
    override konst name: String
        get() = "statistics"

    private var content = StringBuilder()

    override fun render(report: SummaryBenchmarksReport, onlyChanges: Boolean): String {
        konst benchmarksWithChangedStatus = report.benchmarksWithChangedStatus
        konst newPasses = benchmarksWithChangedStatus
                .filter { it.current == BenchmarkResult.Status.PASSED }
        konst newFailures = benchmarksWithChangedStatus
                .filter { it.current == BenchmarkResult.Status.FAILED }
        if (report.failedBenchmarks.isNotEmpty()) {
            content.append("failed: ${report.failedBenchmarks.size}\n")
        }
        konst regressionsSize = report.detailedMetricReports.konstues.fold(0) { acc, it ->
            acc + it.regressions.size
        }
        konst improvementsSize = report.detailedMetricReports.konstues.fold(0) { acc, it ->
            acc + it.improvements.size
        }
        konst status = when {
            newFailures.isNotEmpty() -> {
                content.append("new failures: ${newFailures.size}\n")
                Status.FAILED
            }
            newPasses.isNotEmpty() -> {
                content.append("new passes: ${newPasses.size}\n")
                Status.FIXED
            }
            regressionsSize != 0 && improvementsSize != 0 -> {
                content.append("regressions: $regressionsSize\nimprovements: $improvementsSize")
                Status.UNSTABLE
            }
            improvementsSize != 0 && regressionsSize == 0 ->  {
                content.append("improvements: $improvementsSize")
                Status.IMPROVED
            }
            improvementsSize == 0 && regressionsSize != 0 -> {
                content.append("regressions: $regressionsSize")
                Status.REGRESSED
            }
            else -> Status.STABLE
        }
        return """
            status: $status
            total: ${report.benchmarksNumber}
        """.trimIndent() + "\n$content"
    }
}