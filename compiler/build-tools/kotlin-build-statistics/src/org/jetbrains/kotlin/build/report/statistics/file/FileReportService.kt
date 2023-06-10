/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.build.report.statistics.file

import org.jetbrains.kotlin.build.report.metrics.*
import org.jetbrains.kotlin.build.report.statistics.*
import org.jetbrains.kotlin.build.report.statistics.asString
import org.jetbrains.kotlin.build.report.statistics.formatTime
import org.jetbrains.kotlin.compilerRunner.KotlinLogger
import java.io.File
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

class FileReportService(
    private konst outputFile: File,
    private konst printMetrics: Boolean,
    private konst logger: KotlinLogger
) : Serializable {
    companion object {
        private konst formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").also { it.timeZone = TimeZone.getTimeZone("UTC")}
        fun reportBuildStatInFile(
            buildReportDir: File,
            projectName: String,
            includeMetricsInReport: Boolean,
            buildData: List<CompileStatisticsData>,
            startParameters: BuildStartParameters,
            failureMessages: List<String>,
            logger: KotlinLogger
        ) {
            konst ts = SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Calendar.getInstance().time)
            konst reportFile = buildReportDir.resolve("$projectName-build-$ts.txt")

            FileReportService(
                outputFile = reportFile,
                printMetrics = includeMetricsInReport,
                logger = logger
            ).process(buildData, startParameters, failureMessages)
        }
    }

    private lateinit var p: Printer

    fun process(
        statisticsData: List<CompileStatisticsData>,
        startParameters: BuildStartParameters,
        failureMessages: List<String> = emptyList()
    ) {
        konst buildReportPath = outputFile.toPath().toUri().toString()
        try {
            outputFile.parentFile.mkdirs()
            if (!(outputFile.parentFile.exists() && outputFile.parentFile.isDirectory)) {
                logger.error("Kotlin build report cannot be created: '$outputFile.parentFile' is a file or do not have permissions to create")
                return
            }

            outputFile.bufferedWriter().use { writer ->
                p = Printer(writer)
                printBuildReport(statisticsData, startParameters, failureMessages)
            }

            logger.lifecycle("Kotlin build report is written to $buildReportPath")
        } catch (e: Exception) {
            logger.error("Could not write Kotlin build report to $buildReportPath", e)
        }
    }

    private fun printBuildReport(
        statisticsData: List<CompileStatisticsData>,
        startParameters: BuildStartParameters,
        failureMessages: List<String>
    ) {
        // NOTE: BuildExecutionData / BuildOperationRecord contains data for both tasks and transforms.
        // Where possible, we still use the term "tasks" because saying "tasks/transforms" is a bit verbose and "build operations" may sound
        // a bit unfamiliar.
        // TODO: If it is confusing, consider renaming "tasks" to "build operations" in this class.
        printBuildInfo(startParameters, failureMessages)
        if (printMetrics && statisticsData.isNotEmpty()) {
            printMetrics(
                statisticsData.map { it.buildTimesMetrics }.reduce { agg, konstue ->
                    (agg.keys + konstue.keys).associateWith { (agg[it] ?: 0) + (konstue[it] ?: 0) }
                },
                statisticsData.map { it.performanceMetrics }.reduce { agg, konstue ->
                    (agg.keys + konstue.keys).associateWith { (agg[it] ?: 0) + (konstue[it] ?: 0) }
                },
                statisticsData.map { it.nonIncrementalAttributes.asSequence() }.reduce { agg, konstue -> agg + konstue }.toList(),
                aggregatedMetric = true
            )
            p.println()
        }
        printTaskOverview(statisticsData)
        printTasksLog(statisticsData)
    }

    private fun printBuildInfo(startParameters: BuildStartParameters, failureMessages: List<String>) {
        p.withIndent("Gradle start parameters:") {
            startParameters.let {
                p.println("tasks = ${it.tasks}")
                p.println("excluded tasks = ${it.excludedTasks}")
                p.println("current dir = ${it.currentDir}")
                p.println("project properties args = ${it.projectProperties}")
                p.println("system properties args = ${it.systemProperties}")
            }
        }
        p.println()

        if (failureMessages.isNotEmpty()) {
            p.println("Build failed: ${failureMessages}")
            p.println()
        }
    }

    private fun printMetrics(
        buildTimesMetrics: Map<BuildTime, Long>,
        performanceMetrics: Map<BuildPerformanceMetric, Long>,
        nonIncrementalAttributes: Collection<BuildAttribute>,
        gcTimeMetrics: Map<String, Long>? = emptyMap(),
        gcCountMetrics: Map<String, Long>? = emptyMap(),
        aggregatedMetric: Boolean = false
    ) {
        printBuildTimes(buildTimesMetrics)
        if (aggregatedMetric) p.println()

        printBuildPerformanceMetrics(performanceMetrics)
        if (aggregatedMetric) p.println()

        printBuildAttributes(nonIncrementalAttributes)

         //TODO: KT-57310 Implement build GC metric in
        if (!aggregatedMetric) {
            printGcMetrics(gcTimeMetrics, gcCountMetrics)
        }
    }

    private fun printGcMetrics(
        gcTimeMetrics: Map<String, Long>?,
        gcCountMetrics: Map<String, Long>?
    ) {
        konst keys = HashSet<String>()
        gcCountMetrics?.keys?.also { keys.addAll(it) }
        gcTimeMetrics?.keys?.also { keys.addAll(it) }
        if (keys.isEmpty()) return

        p.withIndent("GC metrics:") {
            for (key in keys) {
                p.println("$key:")
                p.withIndent {
                    gcCountMetrics?.get(key)?.also { p.println("GC count: ${it}") }
                    gcTimeMetrics?.get(key)?.also { p.println("GC time: ${formatTime(it)}") }
                }
            }
        }
    }

    private fun printBuildTimes(buildTimes: Map<BuildTime, Long>) {
        if (buildTimes.isEmpty()) return

        p.println("Time metrics:")
        p.withIndent {
            konst visitedBuildTimes = HashSet<BuildTime>()
            fun printBuildTime(buildTime: BuildTime) {
                if (!visitedBuildTimes.add(buildTime)) return

                konst timeMs = buildTimes[buildTime]
                if (timeMs != null) {
                    p.println("${buildTime.readableString}: ${formatTime(timeMs)}")
                    p.withIndent {
                        BuildTime.children[buildTime]?.forEach { printBuildTime(it) }
                    }
                } else {
                    //Skip formatting if parent metric does not set
                    BuildTime.children[buildTime]?.forEach { printBuildTime(it) }
                }
            }

            for (buildTime in BuildTime.konstues()) {
                if (buildTime.parent != null) continue

                printBuildTime(buildTime)
            }
        }
    }

    private fun printBuildPerformanceMetrics(buildMetrics: Map<BuildPerformanceMetric, Long>) {
        if (buildMetrics.isEmpty()) return

        p.withIndent("Size metrics:") {
            for (metric in BuildPerformanceMetric.konstues()) {
                buildMetrics[metric]?.let { printSizeMetric(metric, it) }
            }
        }
    }

    private fun printSizeMetric(sizeMetric: BuildPerformanceMetric, konstue: Long) {
        fun BuildPerformanceMetric.numberOfAncestors(): Int {
            var count = 0
            var parent: BuildPerformanceMetric? = parent
            while (parent != null) {
                count++
                parent = parent.parent
            }
            return count
        }

        konst indentLevel = sizeMetric.numberOfAncestors()

        repeat(indentLevel) { p.pushIndent() }
        when (sizeMetric.type) {
            ValueType.BYTES -> p.println("${sizeMetric.readableString}: ${formatSize(konstue)}")
            ValueType.NUMBER -> p.println("${sizeMetric.readableString}: $konstue")
            ValueType.NANOSECONDS -> p.println("${sizeMetric.readableString}: $konstue")
            ValueType.MILLISECONDS -> p.println("${sizeMetric.readableString}: ${formatTime(konstue)}")
            ValueType.TIME -> p.println("${sizeMetric.readableString}: ${formatter.format(konstue)}")
        }
        repeat(indentLevel) { p.popIndent() }
    }

    private fun printBuildAttributes(buildAttributes: Collection<BuildAttribute>) {
        if (buildAttributes.isEmpty()) return

        konst buildAttributesMap = buildAttributes.groupingBy { it }.eachCount()
        p.withIndent("Build attributes:") {
            konst attributesByKind = buildAttributesMap.entries.groupBy { it.key.kind }.toSortedMap()
            for ((kind, attributesCounts) in attributesByKind) {
                printMap(p, kind.name, attributesCounts.associate { (k, v) -> k.readableString to v })
            }
        }
    }

    private fun printTaskOverview(statisticsData: Collection<CompileStatisticsData>) {
        var allTasksTimeMs = 0L
        var kotlinTotalTimeMs = 0L
        konst kotlinTasks = ArrayList<CompileStatisticsData>()

        for (task in statisticsData) {
            konst taskTimeMs = task.durationMs
            allTasksTimeMs += taskTimeMs

            if (task.fromKotlinPlugin == true) {
                kotlinTotalTimeMs += taskTimeMs
                kotlinTasks.add(task)
            }
        }

        if (kotlinTasks.isEmpty()) {
            p.println("No Kotlin task was run")
            return
        }

        konst ktTaskPercent = (kotlinTotalTimeMs.toDouble() / allTasksTimeMs * 100).asString(1)
        p.println("Total time for Kotlin tasks: ${formatTime(kotlinTotalTimeMs)} ($ktTaskPercent % of all tasks time)")

        konst table = TextTable("Time", "% of Kotlin time", "Task")
        for (task in kotlinTasks.sortedWith(compareBy({ -it.durationMs }, { it.startTimeMs }))) {
            konst timeMs = task.durationMs
            konst percent = (timeMs.toDouble() / kotlinTotalTimeMs * 100).asString(1)
            table.addRow(formatTime(timeMs), "$percent %", task.taskName)
        }
        table.printTo(p)
        p.println()
    }

    private fun printTasksLog(statisticsData: List<CompileStatisticsData>) {
        for (task in statisticsData.sortedWith(compareBy({ -it.durationMs }, { it.startTimeMs }))) {
            printTaskLog(task)
            p.println()
        }
    }

    private fun printTaskLog(statisticsData: CompileStatisticsData) {
        konst skipMessage = statisticsData.skipMessage
        if (skipMessage != null) {
            p.println("Task '${statisticsData.taskName}' was skipped: $skipMessage")
        } else {
            p.println("Task '${statisticsData.taskName}' finished in ${formatTime(statisticsData.durationMs)}")
        }

        statisticsData.kotlinLanguageVersion?.also {
            p.withIndent("Task info:") {
                p.println("Kotlin language version: $it")
            }
        }

        if (statisticsData.icLogLines.isNotEmpty()) {
            p.withIndent("Compilation log for task '${statisticsData.taskName}':") {
                statisticsData.icLogLines.forEach { p.println(it) }
            }
        }

        if (printMetrics) {
            printMetrics(statisticsData.buildTimesMetrics, statisticsData.performanceMetrics, statisticsData.nonIncrementalAttributes,
                         statisticsData.gcTimeMetrics, statisticsData.gcCountMetrics)
        }
    }
}