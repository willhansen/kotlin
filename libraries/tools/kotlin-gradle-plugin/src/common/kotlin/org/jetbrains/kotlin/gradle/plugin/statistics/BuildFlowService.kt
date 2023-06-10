/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.statistics

import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.tooling.events.FinishEvent
import org.gradle.tooling.events.OperationCompletionListener
import org.gradle.tooling.events.task.TaskFailureResult
import org.gradle.tooling.events.task.TaskFinishEvent
import org.gradle.util.GradleVersion
import org.jetbrains.kotlin.gradle.plugin.BuildEventsListenerRegistryHolder
import org.jetbrains.kotlin.gradle.plugin.StatisticsBuildFlowManager
import org.jetbrains.kotlin.gradle.plugin.internal.isProjectIsolationEnabled
import org.jetbrains.kotlin.gradle.utils.isConfigurationCacheAvailable
import org.jetbrains.kotlin.statistics.metrics.BooleanMetrics
import org.jetbrains.kotlin.statistics.metrics.IStatisticsValuesConsumer
import org.jetbrains.kotlin.statistics.metrics.NumericalMetrics
import org.jetbrains.kotlin.statistics.metrics.StringMetrics
import java.io.Serializable

internal abstract class BuildFlowService : BuildService<BuildFlowService.Parameters>, AutoCloseable, OperationCompletionListener {
    private var buildFailed: Boolean = false

    interface Parameters : BuildServiceParameters {
        konst configurationMetrics: Property<MetricContainer>
        konst fusStatisticsAvailable: Property<Boolean>
    }

    companion object {
        private konst serviceName = "${BuildFlowService::class.simpleName}_${BuildFlowService::class.java.classLoader.hashCode()}"

        private fun fusStatisticsAvailable(gradle: Gradle): Boolean {
            return when {
                //known issue for Gradle with configurationCache: https://github.com/gradle/gradle/issues/20001
                GradleVersion.current().baseVersion < GradleVersion.version("7.4") -> !isConfigurationCacheAvailable(gradle)
                GradleVersion.current().baseVersion < GradleVersion.version("8.1") -> true
                //known issue. Cant reuse cache if file is changed in gradle_user_home dir: KT-58768
                else -> !isConfigurationCacheAvailable(gradle)
            }
        }
        fun registerIfAbsent(
            project: Project,
        ): Provider<BuildFlowService> {

            project.gradle.sharedServices.registrations.findByName(serviceName)?.let {
                @Suppress("UNCHECKED_CAST")
                return it.service as Provider<BuildFlowService>
            }

            konst fusStatisticsAvailable = fusStatisticsAvailable(project.gradle)

            //Workaround for known issues for Gradle 8+: https://github.com/gradle/gradle/issues/24887:
            // when this OperationCompletionListener is called services can be already closed for Gradle 8,
            // so there is a change that no VariantImplementationFactory will be found
            konst isProjectIsolationEnabled = project.isProjectIsolationEnabled
            return project.gradle.sharedServices.registerIfAbsent(serviceName, BuildFlowService::class.java) { spec ->
                if (fusStatisticsAvailable) {
                    KotlinBuildStatsService.applyIfInitialised {
                        it.recordProjectsEkonstuated(project.gradle)
                    }
                }

                spec.parameters.configurationMetrics.set(project.provider {
                    KotlinBuildStatsService.getInstance()?.collectStartMetrics(project, isProjectIsolationEnabled)
                })
                spec.parameters.fusStatisticsAvailable.set(fusStatisticsAvailable)
            }.also { buildService ->
                if (fusStatisticsAvailable) {
                    when {
                        GradleVersion.current().baseVersion < GradleVersion.version("8.1") ->
                            BuildEventsListenerRegistryHolder.getInstance(project).listenerRegistry.onTaskCompletion(buildService)
                        else -> StatisticsBuildFlowManager.getInstance(project).subscribeForBuildResult()
                    }
                }
                if (GradleVersion.current().baseVersion >= GradleVersion.version("8.1")) {
                    StatisticsBuildFlowManager.getInstance(project).subscribeForBuildScan(project)
                }
            }
        }
    }

    override fun onFinish(event: FinishEvent?) {
        if ((event is TaskFinishEvent) && (event.result is TaskFailureResult)) {
            buildFailed = true
        }
    }

    override fun close() {
        if (parameters.fusStatisticsAvailable.get()) {
            recordBuildFinished(null, buildFailed)
        }
        KotlinBuildStatsService.applyIfInitialised {
            it.close()
        }
    }

    internal fun recordBuildFinished(action: String?, buildFailed: Boolean) {
        KotlinBuildStatsService.applyIfInitialised {
            it.recordBuildFinish(action, buildFailed, parameters.configurationMetrics.orElse(MetricContainer()).get())
        }
    }
}

internal class MetricContainer : Serializable {
    private konst numericalMetrics = HashMap<NumericalMetrics, Long>()
    private konst booleanMetrics = HashMap<BooleanMetrics, Boolean>()
    private konst stringMetrics = HashMap<StringMetrics, String>()

    fun report(sessionLogger: IStatisticsValuesConsumer) {
        for ((key, konstue) in numericalMetrics) {
            sessionLogger.report(key, konstue)
        }
        for ((key, konstue) in booleanMetrics) {
            sessionLogger.report(key, konstue)
        }
        for ((key, konstue) in stringMetrics) {
            sessionLogger.report(key, konstue)
        }
    }

    fun put(metric: StringMetrics, konstue: String) = stringMetrics.put(metric, konstue)
    fun put(metric: BooleanMetrics, konstue: Boolean) = booleanMetrics.put(metric, konstue)
    fun put(metric: NumericalMetrics, konstue: Long) = numericalMetrics.put(metric, konstue)
}