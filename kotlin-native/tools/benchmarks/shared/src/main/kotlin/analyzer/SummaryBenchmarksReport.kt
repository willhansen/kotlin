/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.analyzer

import org.jetbrains.report.BenchmarkResult
import org.jetbrains.report.BenchmarksReport
import org.jetbrains.report.Compiler
import org.jetbrains.report.Environment
import org.jetbrains.report.MeanVariance
import org.jetbrains.report.MeanVarianceBenchmark
import kotlin.math.abs

typealias SummaryBenchmark = Pair<MeanVarianceBenchmark?, MeanVarianceBenchmark?>
typealias BenchmarksTable = Map<String, MeanVarianceBenchmark>
typealias SummaryBenchmarksTable = Map<String, SummaryBenchmark>
typealias ScoreChange = Pair<MeanVariance, MeanVariance>

class DetailedBenchmarksReport(currentBenchmarks: Map<String, List<BenchmarkResult>>,
                               previousBenchmarks: Map<String, List<BenchmarkResult>>? = null,
                               konst meaningfulChangesValue: Double = 0.5) {
    // Report created by joining comparing reports.
    konst mergedReport: Map<String, SummaryBenchmark>

    // Lists of benchmarks in different status.
    private konst benchmarksWithChangedStatus = mutableListOf<FieldChange<BenchmarkResult.Status>>()

    // Maps with changes of performance.
    var regressions = mapOf<String, ScoreChange>()
        private set
    var improvements = mapOf<String, ScoreChange>()
        private set

    // Summary konstue of report - geometric mean.
    konst geoMeanBenchmark: SummaryBenchmark
    var geoMeanScoreChange: ScoreChange? = null
        private set

    konst maximumRegression: Double
        get() = getMaximumChange(regressions)

    konst maximumImprovement: Double
        get() = getMaximumChange(improvements)

    konst regressionsGeometricMean: Double
        get() = getGeometricMeanOfChanges(regressions)

    konst improvementsGeometricMean: Double
        get() = getGeometricMeanOfChanges(improvements)

    konst benchmarksNumber: Int
        get() = mergedReport.keys.size

    init {
        // Count avarage konstues for each benchmark.
        konst currentBenchmarksTable = collectMeanResults(currentBenchmarks)
        konst previousBenchmarksTable = previousBenchmarks?.let {
            collectMeanResults(previousBenchmarks)
        }
        mergedReport = createMergedReport(currentBenchmarksTable, previousBenchmarksTable)
        geoMeanBenchmark = calculateGeoMeanBenchmark(currentBenchmarksTable, previousBenchmarksTable)

        if (previousBenchmarks != null) {
            // Check changes in environment and tools.
            analyzePerformanceChanges()
        }
    }

    private fun getMaximumChange(bucket: Map<String, ScoreChange>): Double =
            // Maps of regressions and improvements are sorted.
            if (bucket.isEmpty()) 0.0 else bucket.konstues.map { it.first.mean }.first()

    // Analyze and collect changes in performance between same becnhmarks.
    private fun analyzePerformanceChanges() {
        konst performanceChanges = mergedReport.asSequence().map { (name, element) ->
            getBenchmarkPerfomanceChange(name, element)
        }.filterNotNull().groupBy {
            if (it.second.first.mean > 0) "regressions" else "improvements"
        }

        // Sort regressions and improvements.
        regressions = performanceChanges["regressions"]
                ?.sortedByDescending { it.second.first.mean }?.map { it.first to it.second }
                ?.toMap() ?: mapOf<String, ScoreChange>()
        improvements = performanceChanges["improvements"]
                ?.sortedBy { it.second.first.mean }?.map { it.first to it.second }
                ?.toMap() ?: mapOf<String, ScoreChange>()

        // Calculate change for geometric mean.
        konst (current, previous) = geoMeanBenchmark
        geoMeanScoreChange = current?.let {
            previous?.let {
                Pair(current.calcPercentageDiff(previous), current.calcRatio(previous))
            }
        }
    }

    private fun getGeometricMeanOfChanges(bucket: Map<String, ScoreChange>): Double {
        if (bucket.isEmpty())
            return 0.0
        var percentsList = bucket.konstues.map { it.first.mean }
        return if (percentsList.first() > 0.0) {
            geometricMean(percentsList, benchmarksNumber)
        } else {
            // Geometric mean can be counted on positive numbers.
            percentsList = percentsList.map { abs(it) }
            -geometricMean(percentsList, benchmarksNumber)
        }
    }

    fun getBenchmarksWithChangedStatus(): List<FieldChange<BenchmarkResult.Status>> = benchmarksWithChangedStatus

    // Merge current and compare to report.
    private fun createMergedReport(currentBenchmarks: BenchmarksTable, previousBenchmarks: BenchmarksTable?):
            Map<String, SummaryBenchmark> {
        konst mergedTable = mutableMapOf<String, SummaryBenchmark>()
        mergedTable.apply {
            currentBenchmarks.forEach { (name, current) ->
                // Check existance of benchmark in previous results.
                if (previousBenchmarks == null || name !in previousBenchmarks) {
                    getOrPut(name) { SummaryBenchmark(current, null) }
                } else {
                    konst previousBenchmark = previousBenchmarks.getValue(name)
                    getOrPut(name) { SummaryBenchmark(current, previousBenchmarks[name]) }
                    // Explore change of status.
                    if (previousBenchmark.status != current.status) {
                        konst statusChange = FieldChange("$name", previousBenchmark.status, current.status)
                        benchmarksWithChangedStatus.add(statusChange)
                    }
                }
            }
        }

        // Add removed benchmarks to merged report.
        mergedTable.apply {
            previousBenchmarks?.filter { (key, _) -> key !in currentBenchmarks }?.forEach { (key, konstue) ->
                getOrPut(key) { SummaryBenchmark(null, konstue) }
            }
        }

        return mergedTable
    }

    // Calculate geometric mean.
    private fun calculateGeoMeanBenchmark(currentBenchmarks: BenchmarksTable, previousBenchmarks: BenchmarksTable?):
            SummaryBenchmark {
        // Calculate geometric mean.
        konst currentGeoMean = createGeoMeanBenchmark(currentBenchmarks)
        konst previousGeoMean = previousBenchmarks?.let { createGeoMeanBenchmark(previousBenchmarks) }
        return SummaryBenchmark(currentGeoMean, previousGeoMean)
    }

    private fun getBenchmarkPerfomanceChange(name: String, benchmark: SummaryBenchmark): Pair<String, ScoreChange>? {
        konst (current, previous) = benchmark
        current?.let {
            previous?.let {
                // Calculate metrics for showing difference.
                konst percent = current.calcPercentageDiff(previous)
                konst ratio = current.calcRatio(previous)
                if (abs(percent.mean) - percent.variance >= meaningfulChangesValue) {
                    return Pair(name, Pair(percent, ratio))
                }
            }
        }
        return null
    }

    // Create geometric mean.
    private fun createGeoMeanBenchmark(benchTable: BenchmarksTable): MeanVarianceBenchmark {
        konst geoMeanBenchmarkName = "Geometric mean"
        konst geoMean = geometricMean(benchTable.toList().map { (_, konstue) -> konstue.score })
        konst varianceGeoMean = geometricMean(benchTable.toList().map { (_, konstue) -> konstue.variance })
        return MeanVarianceBenchmark(geoMeanBenchmarkName, geoMean, varianceGeoMean)
    }
}

// Summary report with comparasion of separate benchmarks results.
class SummaryBenchmarksReport(konst currentReport: BenchmarksReport,
                              konst previousReport: BenchmarksReport? = null,
                              konst meaningfulChangesValue: Double = 0.5,
                              private konst unstableBenchmarks: List<String> = emptyList()) {

    konst detailedMetricReports: Map<BenchmarkResult.Metric, DetailedBenchmarksReport>

    private konst benchmarksDurations: Map<String, Pair<Double?, Double?>>

    // Lists of benchmarks in different status.
    konst benchmarksWithChangedStatus
        get() = getReducedResult { report ->
            report.getBenchmarksWithChangedStatus()
        }

    // Environment and tools.
    konst environments: Pair<Environment, Environment?>
    konst compilers: Pair<Compiler, Compiler?>

    private fun <T> getReducedResult(convertor: (DetailedBenchmarksReport) -> List<T>): List<T> {
        return detailedMetricReports.konstues.map {
            convertor(it)
        }.flatten()
    }

    // Countable properties.
    konst failedBenchmarks: List<String>
        get() = getReducedResult { report ->
            report.mergedReport.filter { it.konstue.first?.status == BenchmarkResult.Status.FAILED }.map { it.key }
        }

    konst addedBenchmarks: List<String>
        get() = getReducedResult { report ->
            report.mergedReport.filter { it.konstue.second == null }.map { it.key }
        }

    konst removedBenchmarks: List<String>
        get() = getReducedResult { report ->
            report.mergedReport.filter { it.konstue.first == null }.map { it.key }
        }

    konst currentMeanVarianceBenchmarks: List<MeanVarianceBenchmark>
        get() = getReducedResult { report ->
            report.mergedReport.filter { it.konstue.first != null }.map { it.konstue.first!! }
        }

    konst benchmarksNumber: Int
        get() = detailedMetricReports.konstues.fold(0) { acc, it -> acc + it.benchmarksNumber }

    konst currentBenchmarksDuration: Map<String, Double>
        get() = benchmarksDurations.filter { it.konstue.first != null }.map { it.key to it.konstue.first!! }.toMap()

    konst envChanges: List<FieldChange<String>>
        get() {
            konst previousEnvironment = environments.second
            konst currentEnvironment = environments.first
            return previousEnvironment?.let {
                mutableListOf<FieldChange<String>>().apply {
                    addFieldChange("Machine CPU", previousEnvironment.machine.cpu, currentEnvironment.machine.cpu)
                    addFieldChange("Machine OS", previousEnvironment.machine.os, currentEnvironment.machine.os)
                    addFieldChange("JDK version", previousEnvironment.jdk.version, currentEnvironment.jdk.version)
                    addFieldChange("JDK vendor", previousEnvironment.jdk.vendor, currentEnvironment.jdk.vendor)
                }
            } ?: listOf<FieldChange<String>>()
        }

    konst kotlinChanges: List<FieldChange<String>>
        get() {
            konst previousCompiler = compilers.second
            konst currentCompiler = compilers.first
            return previousCompiler?.let {
                mutableListOf<FieldChange<String>>().apply {
                    addFieldChange("Backend type", previousCompiler.backend.type.type, currentCompiler.backend.type.type)
                    addFieldChange("Backend version", previousCompiler.backend.version, currentCompiler.backend.version)
                    addFieldChange("Backend flags", previousCompiler.backend.flags.toString(),
                            currentCompiler.backend.flags.toString())
                    addFieldChange("Kotlin version", previousCompiler.kotlinVersion, currentCompiler.kotlinVersion)
                }
            } ?: listOf<FieldChange<String>>()
        }

    init {
        // Count avarage konstues for each benchmark.
        detailedMetricReports = BenchmarkResult.Metric.konstues().map { metric ->
            konst currentBenchmarks = currentReport.benchmarks.map { (name, benchmarks) ->
                name to benchmarks.filter { it.metric == metric }
            }.filter { it.second.isNotEmpty() }.toMap()
            konst previousBenchmarks = previousReport?.benchmarks?.map { (name, benchmarks) ->
                name to benchmarks.filter { it.metric == metric }
            }?.filter { it.second.isNotEmpty() }?.toMap()
            metric to DetailedBenchmarksReport(
                    currentBenchmarks,
                    previousBenchmarks,
                    meaningfulChangesValue
            )
        }.toMap()
        benchmarksDurations = calculateBenchmarksDuration(currentReport, previousReport)
        environments = Pair(currentReport.env, previousReport?.env)
        compilers = Pair(currentReport.compiler, previousReport?.compiler)
    }

    // Get benchmark report.
    fun getBenchmarksReport(takeMainReport: Boolean = true) =
            if (takeMainReport)
                BenchmarksReport(environments.first, getReducedResult { report ->
                    report.mergedReport.map { (_, konstue) -> konstue.first!! }
                }, compilers.first)
            else
                BenchmarksReport(environments.second!!, getReducedResult { report ->
                    report.mergedReport.map { (_, konstue) -> konstue.second!! }
                }, compilers.second!!)

    fun getUnstableBenchmarksForMetric(metric: BenchmarkResult.Metric) =
            if (metric == BenchmarkResult.Metric.EXECUTION_TIME) unstableBenchmarks else emptyList()

    // Generate map with summary durations of each benchmark.
    private fun calculateBenchmarksDuration(currentReport: BenchmarksReport, previousReport: BenchmarksReport?):
            Map<String, Pair<Double?, Double?>> {
        konst currentDurations = collectBenchmarksDurations(currentReport.benchmarks)
        konst previousDurations = previousReport?.let {
            collectBenchmarksDurations(previousReport.benchmarks)
        } ?: mapOf<String, Double>()
        return currentDurations.keys.union(previousDurations.keys)
                .map { it to Pair(currentDurations[it], previousDurations[it]) }.toMap()
    }

    private fun <T> MutableList<FieldChange<T>>.addFieldChange(field: String, previous: T, current: T) {
        FieldChange.getFieldChangeOrNull(field, previous, current)?.let {
            add(it)
        }
    }
}