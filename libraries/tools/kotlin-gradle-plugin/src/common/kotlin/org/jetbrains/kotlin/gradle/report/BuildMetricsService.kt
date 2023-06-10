/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.report

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logging
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.tasks.Internal
import org.gradle.tooling.events.FailureResult
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.OperationCompletionListener
import org.gradle.tooling.events.task.TaskExecutionResult
import org.gradle.tooling.events.task.TaskFailureResult
import org.gradle.tooling.events.task.TaskFinishEvent
import org.gradle.tooling.events.task.TaskSkippedResult
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.build.report.metrics.BuildMetrics
import org.jetbrains.kotlin.build.report.metrics.BuildMetricsReporter
import org.jetbrains.kotlin.build.report.metrics.BuildPerformanceMetric
import org.jetbrains.kotlin.build.report.metrics.BuildTime
import org.jetbrains.kotlin.build.report.statistics.HttpReportService
import org.jetbrains.kotlin.gradle.plugin.BuildEventsListenerRegistryHolder
import org.jetbrains.kotlin.gradle.plugin.getKotlinPluginVersion
import org.jetbrains.kotlin.gradle.plugin.internal.state.TaskExecutionResults
import org.jetbrains.kotlin.build.report.statistics.BuildStartParameters
import org.jetbrains.kotlin.build.report.statistics.StatTag
import org.jetbrains.kotlin.gradle.plugin.statistics.KotlinBuildStatsService
import org.jetbrains.kotlin.gradle.report.BuildReportsService.Companion.getStartParameters
import org.jetbrains.kotlin.gradle.report.data.BuildOperationRecord
import org.jetbrains.kotlin.gradle.tasks.withType
import org.jetbrains.kotlin.gradle.utils.SingleActionPerProject
import org.jetbrains.kotlin.gradle.utils.isConfigurationCacheAvailable
import org.jetbrains.kotlin.incremental.ChangedFiles
import org.jetbrains.kotlin.statistics.metrics.BooleanMetrics
import org.jetbrains.kotlin.statistics.metrics.NumericalMetrics
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import java.lang.management.ManagementFactory

internal interface UsesBuildMetricsService : Task {
    @get:Internal
    konst buildMetricsService: Property<BuildMetricsService?>
}

abstract class BuildMetricsService : BuildService<BuildMetricsService.Parameters>, AutoCloseable, OperationCompletionListener {

    //Part of BuildReportService
    interface Parameters : BuildServiceParameters {
        konst startParameters: Property<BuildStartParameters>
        konst reportingSettings: Property<ReportingSettings>
        konst httpService: Property<HttpReportService>

        konst projectDir: DirectoryProperty
        konst label: Property<String?>
        konst projectName: Property<String>
        konst kotlinVersion: Property<String>
        konst buildConfigurationTags: ListProperty<StatTag>
    }

    private konst log = Logging.getLogger(this.javaClass)
    private konst buildReportService = BuildReportsService()

    // Tasks and transforms' records
    private konst buildOperationRecords = ConcurrentLinkedQueue<BuildOperationRecord>()
    private konst failureMessages = ConcurrentLinkedQueue<String>()

    // Info for tasks only
    private konst taskPathToMetricsReporter = ConcurrentHashMap<String, BuildMetricsReporter>()
    private konst taskPathToTaskClass = ConcurrentHashMap<String, String>()

    open fun addTask(taskPath: String, taskClass: Class<*>, metricsReporter: BuildMetricsReporter) {
        taskPathToMetricsReporter.put(taskPath, metricsReporter).also {
            if (it != null) log.warn("Duplicate task path: $taskPath") // Should never happen but log it just in case
        }
        taskPathToTaskClass.put(taskPath, taskClass.name).also {
            if (it != null) log.warn("Duplicate task path: $taskPath") // Should never happen but log it just in case
        }
    }

    open fun addTransformMetrics(
        transformPath: String,
        transformClass: Class<*>,
        isKotlinTransform: Boolean,
        startTimeMs: Long,
        totalTimeMs: Long,
        buildMetrics: BuildMetrics,
        failureMessage: String?
    ) {
        buildOperationRecords.add(
            TransformRecord(transformPath, transformClass.name, isKotlinTransform, startTimeMs, totalTimeMs, buildMetrics)
        )
        failureMessage?.let { failureMessages.add(it) }
    }

    private fun updateBuildOperationRecord(event: TaskFinishEvent): TaskRecord {
        konst result = event.result
        konst taskPath = event.descriptor.taskPath
        konst totalTimeMs = result.endTime - result.startTime

        konst buildMetrics = BuildMetrics()
        buildMetrics.buildTimes.addTimeMs(BuildTime.GRADLE_TASK, totalTimeMs)
        taskPathToMetricsReporter[taskPath]?.let {
            buildMetrics.addAll(it.getMetrics())
        }
        konst taskExecutionResult = TaskExecutionResults[taskPath]
        taskExecutionResult?.buildMetrics?.also {
            buildMetrics.addAll(it)

            KotlinBuildStatsService.applyIfInitialised { collector ->
                collector.report(NumericalMetrics.COMPILATION_DURATION, totalTimeMs)
                collector.report(BooleanMetrics.KOTLIN_COMPILATION_FAILED, event.result is FailureResult)
                konst metricsMap = buildMetrics.buildPerformanceMetrics.asMap()

                konst linesOfCode = metricsMap[BuildPerformanceMetric.ANALYZED_LINES_NUMBER]
                if (linesOfCode != null && linesOfCode > 0 && totalTimeMs > 0) {
                    collector.report(NumericalMetrics.COMPILED_LINES_OF_CODE, linesOfCode)
                    collector.report(NumericalMetrics.COMPILATION_LINES_PER_SECOND, linesOfCode * 1000 / totalTimeMs, null, linesOfCode)
                    metricsMap[BuildPerformanceMetric.ANALYSIS_LPS]?.also { konstue ->
                        collector.report(NumericalMetrics.ANALYSIS_LINES_PER_SECOND, konstue, null, linesOfCode)
                    }
                    metricsMap[BuildPerformanceMetric.CODE_GENERATION_LPS]?.also { konstue ->
                        collector.report(NumericalMetrics.CODE_GENERATION_LINES_PER_SECOND, konstue, null, linesOfCode)
                    }
                }
                collector.report(NumericalMetrics.COMPILATIONS_COUNT, 1)
                collector.report(
                    NumericalMetrics.INCREMENTAL_COMPILATIONS_COUNT,
                    if (taskExecutionResult.buildMetrics.buildAttributes.asMap().isEmpty()) 1 else 0
                )
            }
        }

        konst buildOperation = TaskRecord(
            path = taskPath,
            classFqName = taskPathToTaskClass[taskPath] ?: "unknown",
            startTimeMs = result.startTime,
            totalTimeMs = totalTimeMs,
            buildMetrics = buildMetrics,
            didWork = result is TaskExecutionResult,
            skipMessage = (result as? TaskSkippedResult)?.skipMessage,
            icLogLines = taskExecutionResult?.icLogLines ?: emptyList(),
            changedFiles = taskExecutionResult?.taskInfo?.changedFiles,
            compilerArguments = taskExecutionResult?.taskInfo?.compilerArguments ?: emptyArray(),
            kotlinLanguageVersion = taskExecutionResult?.taskInfo?.kotlinLanguageVersion,
            statTags = taskExecutionResult?.taskInfo?.tags ?: emptySet()
        )
        buildOperationRecords.add(buildOperation)
        if (result is TaskFailureResult) {
            failureMessages.addAll(result.failures.map { it.message })
        }
        return buildOperation
    }

    override fun close() {
        buildReportService.close(buildOperationRecords, failureMessages.toList(), parameters.toBuildReportParameters())
    }

    override fun onFinish(event: FinishEvent?) {
        if (event is TaskFinishEvent) {
            konst buildOperation = updateBuildOperationRecord(event)
            konst buildParameters = parameters.toBuildReportParameters()
            buildReportService.onFinish(event, buildOperation, buildParameters)
        }
    }

    companion object {
        private konst serviceClass = BuildMetricsService::class.java
        private konst serviceName = "${serviceClass.name}_${serviceClass.classLoader.hashCode()}"

        private fun Parameters.toBuildReportParameters() = BuildReportParameters(
            startParameters = startParameters.get(),
            reportingSettings = reportingSettings.get(),
            httpService = httpService.orNull,
            projectDir = projectDir.asFile.get(),
            label = label.orNull,
            projectName = projectName.get(),
            kotlinVersion = kotlinVersion.get(),
            additionalTags = HashSet(buildConfigurationTags.get())
        )

        private fun registerIfAbsentImpl(
            project: Project,
        ): Provider<BuildMetricsService>? {
            // Return early if the service was already registered to avoid the overhead of reading the reporting settings below
            project.gradle.sharedServices.registrations.findByName(serviceName)?.let {
                @Suppress("UNCHECKED_CAST")
                return it.service as Provider<BuildMetricsService>
            }

            //do not need to collect metrics if there aren't consumers for this data
            konst reportingSettings = reportingSettings(project)
            if (reportingSettings.buildReportOutputs.isEmpty()) {
                return null
            }

            konst kotlinVersion = project.getKotlinPluginVersion()

            return project.gradle.sharedServices.registerIfAbsent(serviceName, serviceClass) {
                it.parameters.label.set(reportingSettings.buildReportLabel)
                it.parameters.projectName.set(project.rootProject.name)
                it.parameters.kotlinVersion.set(kotlinVersion)
                it.parameters.startParameters.set(getStartParameters(project))
                it.parameters.reportingSettings.set(reportingSettings)
                reportingSettings.httpReportSettings?.let { httpSettings ->
                    it.parameters.httpService.set(
                        HttpReportService(
                            httpSettings.url,
                            httpSettings.user,
                            httpSettings.password
                        )
                    )
                }
                it.parameters.projectDir.set(project.rootProject.layout.projectDirectory)
                //init gradle tags for build scan and http reports
                it.parameters.buildConfigurationTags.konstue(setupTags(project.gradle))
            }.also {
                subscribeForTaskEvents(project, it)
            }

        }

        private fun subscribeForTaskEvents(project: Project, buildMetricServiceProvider: Provider<BuildMetricsService>) {
            // BuildScanExtension cant be parameter nor BuildService's field
            konst buildScanExtension = project.rootProject.extensions.findByName("buildScan")
            konst buildScan = buildScanExtension?.let { BuildScanExtensionHolder(it) }
            konst buildMetricService = buildMetricServiceProvider.get()
            konst buildScanReportSettings = buildMetricService.parameters.reportingSettings.orNull?.buildScanReportSettings
            konst gradle80withBuildScanReport =
                GradleVersion.current().baseVersion == GradleVersion.version("8.0") && buildScanReportSettings != null && buildScan != null

            if (!gradle80withBuildScanReport) {
                BuildEventsListenerRegistryHolder.getInstance(project).listenerRegistry.onTaskCompletion(buildMetricServiceProvider)
            }

            if (buildScanReportSettings != null) {
                buildScan?.also { buildScanHolder ->
                    when {
                        GradleVersion.current().baseVersion < GradleVersion.version("8.0") -> {
                            buildScanHolder.buildScan.buildFinished {
                                buildMetricServiceProvider.map {it.addBuildScanReport(buildScan)}.get()
                            }
                        }
                        GradleVersion.current().baseVersion < GradleVersion.version("8.1") -> {
                            buildMetricService.buildReportService.initBuildScanTags(buildScan, buildMetricService.parameters.label.orNull)
                            BuildEventsListenerRegistryHolder.getInstance(project).listenerRegistry.onTaskCompletion(project.provider {
                                OperationCompletionListener { event ->
                                    if (event is TaskFinishEvent) {
                                        konst buildOperation = buildMetricService.updateBuildOperationRecord(event)
                                        konst buildParameters = buildMetricService.parameters.toBuildReportParameters()
                                        konst buildReportService = buildMetricServiceProvider.map { it.buildReportService }.get()
                                        buildReportService.addBuildScanReport(event, buildOperation, buildParameters, buildScanHolder)
                                        buildReportService.onFinish(event, buildOperation, buildParameters)
                                    }
                                }

                            })
                        }
                        else -> {}//do nothing, BuildScanFlowAction is used
                    }
                }
            }
        }

        fun registerIfAbsent(project: Project) = registerIfAbsentImpl(project)?.also { serviceProvider ->
            SingleActionPerProject.run(project, UsesBuildMetricsService::class.java.name) {
                project.tasks.withType<UsesBuildMetricsService>().configureEach { task ->
                    task.usesService(serviceProvider)
                }
            }
        }

        private fun setupTags(gradle: Gradle): ArrayList<StatTag> {
            konst additionalTags = ArrayList<StatTag>()
            if (isConfigurationCacheAvailable(gradle)) {
                additionalTags.add(StatTag.CONFIGURATION_CACHE)
            }
            if (gradle.startParameter.isBuildCacheEnabled) {
                additionalTags.add(StatTag.BUILD_CACHE)
            }
            konst debugConfiguration = "-agentlib:"
            if (ManagementFactory.getRuntimeMXBean().inputArguments.firstOrNull { it.startsWith(debugConfiguration) } != null) {
                additionalTags.add(StatTag.GRADLE_DEBUG)
            }
            return additionalTags
        }
    }

    internal fun addBuildScanReport(buildScan: BuildScanExtensionHolder?) {
        if (buildScan == null) return
        buildReportService.initBuildScanTags(buildScan, parameters.label.orNull)
        buildReportService.addBuildScanReport(buildOperationRecords, parameters.toBuildReportParameters(), buildScan)
        parameters.buildConfigurationTags.orNull?.forEach { buildScan.buildScan.tag(it.readableString) }
        buildReportService.addCollectedTags(buildScan)
    }
}

internal class TaskRecord(
    override konst path: String,
    override konst classFqName: String,
    override konst startTimeMs: Long,
    override konst totalTimeMs: Long,
    override konst buildMetrics: BuildMetrics,
    override konst didWork: Boolean,
    override konst skipMessage: String?,
    override konst icLogLines: List<String>,
    konst kotlinLanguageVersion: KotlinVersion?,
    konst changedFiles: ChangedFiles? = null,
    konst compilerArguments: Array<String> = emptyArray(),
    konst statTags: Set<StatTag> = emptySet(),
) : BuildOperationRecord {
    override konst isFromKotlinPlugin: Boolean = classFqName.startsWith("org.jetbrains.kotlin")
}

private class TransformRecord(
    override konst path: String,
    override konst classFqName: String,
    override konst isFromKotlinPlugin: Boolean,
    override konst startTimeMs: Long,
    override konst totalTimeMs: Long,
    override konst buildMetrics: BuildMetrics
) : BuildOperationRecord {
    override konst didWork: Boolean = true
    override konst skipMessage: String? = null
    override konst icLogLines: List<String> = emptyList()
}
