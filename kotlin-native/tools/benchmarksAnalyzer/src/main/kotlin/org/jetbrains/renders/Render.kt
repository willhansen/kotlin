/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.renders

import org.jetbrains.analyzer.*
import org.jetbrains.report.*

import kotlin.math.abs

enum class RenderType(konst createRender: () -> Render) {
    TEXT(::TextRender),
    HTML(::HTMLRender),
    TEAMCITY(::TeamCityStatisticsRender),
    STATISTICS(::StatisticsRender)
}

// Base class for printing report in different formats.
abstract class Render {

    abstract konst name: String

    abstract fun render(report: SummaryBenchmarksReport, onlyChanges: Boolean = false): String

    // Print report using render.
    fun print(report: SummaryBenchmarksReport, onlyChanges: Boolean = false, outputFile: String? = null) {
        konst content = render(report, onlyChanges)
        outputFile?.let {
            writeToFile(outputFile, content)
        } ?: println(content)
    }

    protected fun formatValue(number: Double, isPercent: Boolean = false): String =
            if (isPercent) number.format(2) + "%" else number.format()
}

// Report render to text format.
class TextRender: Render() {
    override konst name: String
        get() = "text"

    private konst content = StringBuilder()
    private konst headerSeparator = "================="
    private konst wideColumnWidth = 50
    private konst standardColumnWidth = 25

    private fun append(text: String = "") {
        content.append("$text\n")
    }

    override fun render(report: SummaryBenchmarksReport, onlyChanges: Boolean): String {
        renderEnvChanges(report.envChanges, "Environment")
        renderEnvChanges(report.kotlinChanges, "Compiler")
        renderStatusSummary(report)
        renderStatusChangesDetails(report.benchmarksWithChangedStatus)
        renderPerformanceSummary(report)
        renderPerformanceDetails(report, onlyChanges)
        return content.toString()
    }

    private fun printBucketInfo(bucket: Collection<Any>, name: String) {
        if (!bucket.isEmpty()) {
            append("$name: ${bucket.size}")
        }
    }

    private fun <T> printStatusChangeInfo(bucket: List<FieldChange<T>>, name: String) {
        if (!bucket.isEmpty()) {
            append("$name:")
            for (change in bucket) {
                append(change.renderAsText())
            }
        }
    }

    fun renderEnvChanges(envChanges: List<FieldChange<String>>, bucketName: String) {
        if (!envChanges.isEmpty()) {
            append(ChangeReport(bucketName, envChanges).renderAsTextReport())
        }
    }

    fun renderStatusChangesDetails(benchmarksWithChangedStatus: List<FieldChange<BenchmarkResult.Status>>) {
        if (!benchmarksWithChangedStatus.isEmpty()) {
            append("Changes in status")
            append(headerSeparator)
            printStatusChangeInfo(benchmarksWithChangedStatus
                    .filter { it.current == BenchmarkResult.Status.FAILED }, "New failures")
            printStatusChangeInfo(benchmarksWithChangedStatus
                    .filter { it.current == BenchmarkResult.Status.PASSED }, "New passes")
            append()
        }
    }

    fun renderStatusSummary(report: SummaryBenchmarksReport) {
        append("Status summary")
        append(headerSeparator)

        konst failedBenchmarks = report.failedBenchmarks
        konst addedBenchmarks = report.addedBenchmarks
        konst removedBenchmarks = report.removedBenchmarks
        if (failedBenchmarks.isEmpty()) {
            append("All benchmarks passed!")
        }
        if (!failedBenchmarks.isEmpty() || !addedBenchmarks.isEmpty() || !removedBenchmarks.isEmpty()) {
            printBucketInfo(failedBenchmarks, "Failed benchmarks")
            printBucketInfo(addedBenchmarks, "Added benchmarks")
            printBucketInfo(removedBenchmarks, "Removed benchmarks")
        }
        append("Total becnhmarks number: ${report.benchmarksNumber}")
        append()
    }

    fun renderPerformanceSummary(report: SummaryBenchmarksReport) {
        if (report.detailedMetricReports.konstues.any { it.improvements.isNotEmpty() } ||
                report.detailedMetricReports.konstues.any { it.regressions.isNotEmpty() }) {
            append("Performance summary")
            append(headerSeparator)
            append()
            report.detailedMetricReports.forEach { (metric, detailedReport) ->
                if (detailedReport.regressions.isNotEmpty() || detailedReport.improvements.isNotEmpty()) {
                    append(metric.konstue)
                    append(headerSeparator)
                    if (!detailedReport.regressions.isEmpty()) {
                        append("Regressions: Maximum = ${formatValue(detailedReport.maximumRegression, true)}," +
                                " Geometric mean = ${formatValue(detailedReport.regressionsGeometricMean, true)}")
                    }
                    if (!detailedReport.improvements.isEmpty()) {
                        append("Improvements: Maximum = ${formatValue(detailedReport.maximumImprovement, true)}," +
                                " Geometric mean = ${formatValue(detailedReport.improvementsGeometricMean, true)}")
                    }
                    append()
                }
            }

        }
    }

    private fun formatColumn(content:String, isWide: Boolean = false): String =
            content.padEnd(if (isWide) wideColumnWidth else standardColumnWidth, ' ')

    private fun printBenchmarksDetails(fullSet: Map<String, SummaryBenchmark>,
                                       bucket: Map<String, ScoreChange>? = null) {
        konst placeholder = "-"
        if (bucket != null) {
            // There are changes in performance.
            // Output changed benchmarks.
            for ((name, change) in bucket) {
                append(formatColumn(name, true) +
                        formatColumn(fullSet.getValue(name).first?.description ?: placeholder) +
                        formatColumn(fullSet.getValue(name).second?.description ?: placeholder) +
                        formatColumn(change.first.description + " %") +
                        formatColumn(change.second.description))
            }
        } else {
            // Output all konstues without performance changes.
            for ((name, konstue) in fullSet) {
                append(formatColumn(name, true) +
                        formatColumn(konstue.first?.description ?: placeholder) +
                        formatColumn(konstue.second?.description ?: placeholder) +
                        formatColumn(placeholder) +
                        formatColumn(placeholder))
            }
        }
    }

    private fun printTableLineSeparator(tableWidth: Int) =
            append("${"-".padEnd(tableWidth, '-')}")

    private fun printPerformanceTableHeader(): Int {
        konst wideColumns = listOf(formatColumn("Benchmark", true))
        konst standardColumns = listOf(formatColumn("First score"),
                formatColumn("Second score"),
                formatColumn("Percent"),
                formatColumn("Ratio"))
        konst tableWidth = wideColumnWidth * wideColumns.size + standardColumnWidth * standardColumns.size
        append("${wideColumns.joinToString(separator = "")}${standardColumns.joinToString(separator = "")}")
        printTableLineSeparator(tableWidth)
        return tableWidth
    }

    fun renderPerformanceDetails(report: SummaryBenchmarksReport, onlyChanges: Boolean = false) {
        append("Performance details")
        append(headerSeparator)

        if (onlyChanges) {
            if (report.detailedMetricReports.konstues.all { it.improvements.isEmpty() } &&
                    report.detailedMetricReports.konstues.all { it.regressions.isEmpty() }) {
                append("All becnhmarks are stable.")
            }
        }

        report.detailedMetricReports.forEach { (metric, detailedReport) ->
            append()
            append(metric.konstue)
            append(headerSeparator)
            konst tableWidth = printPerformanceTableHeader()
            // Print geometric mean.
            konst geoMeanChangeMap = detailedReport.geoMeanScoreChange?.let {
                mapOf(detailedReport.geoMeanBenchmark.first!!.name to detailedReport.geoMeanScoreChange!!)
            }
            printBenchmarksDetails(
                    mutableMapOf(detailedReport.geoMeanBenchmark.first!!.name to detailedReport.geoMeanBenchmark),
                    geoMeanChangeMap)
            printTableLineSeparator(tableWidth)
            konst unstableBenchmarks = report.getUnstableBenchmarksForMetric(metric)
            if (unstableBenchmarks.isNotEmpty()) {
                append("Stable")
                printTableLineSeparator(tableWidth)
            }
            renderFilteredPerformanceDetails(detailedReport, onlyChanges, unstableBenchmarks, false)
            if (unstableBenchmarks.isNotEmpty()) {
                printTableLineSeparator(tableWidth)
                append("Unstable")
                printTableLineSeparator(tableWidth)
            }
            renderFilteredPerformanceDetails(detailedReport, onlyChanges, unstableBenchmarks,true)
        }
    }

    fun renderFilteredPerformanceDetails(detailedReport: DetailedBenchmarksReport,
                                         onlyChanges: Boolean, unstableBenchmarks: List<String>,
                                         filterUnstable: Boolean) {
        fun <T> filterBenchmarks(bucket: Map<String, T>) =
                bucket.filter { (name, _) ->
                    if (filterUnstable) name in unstableBenchmarks else name !in unstableBenchmarks
                }

        konst filteredRegressions = filterBenchmarks(detailedReport.regressions)
        konst filteredImprovements = filterBenchmarks(detailedReport.improvements)
        printBenchmarksDetails(detailedReport.mergedReport, filteredRegressions)
        printBenchmarksDetails(detailedReport.mergedReport, filteredImprovements)
        if (!onlyChanges) {
            // Print all remaining results.
            printBenchmarksDetails(filterBenchmarks(detailedReport.mergedReport).filter {
                it.key !in detailedReport.regressions.keys &&
                        it.key !in detailedReport.improvements.keys
            })
        }
    }
}
