/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.plugin.statistics

import org.gradle.api.Project
import org.gradle.api.invocation.Gradle
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.gradle.initialization.BuildRequestMetaData
import org.gradle.invocation.DefaultGradle
import org.gradle.tooling.events.OperationCompletionListener
import org.gradle.tooling.events.task.TaskFinishEvent
import org.jetbrains.kotlin.gradle.plugin.BuildEventsListenerRegistryHolder
import org.jetbrains.kotlin.gradle.plugin.statistics.KotlinBuildStatHandler.Companion.runSafe
import org.jetbrains.kotlin.gradle.plugin.statistics.old.Pre232IdeaKotlinBuildStatsMXBean
import org.jetbrains.kotlin.gradle.plugin.statistics.old.Pre232IdeaKotlinBuildStatsService
import org.jetbrains.kotlin.statistics.BuildSessionLogger
import org.jetbrains.kotlin.statistics.BuildSessionLogger.Companion.STATISTICS_FOLDER_NAME
import org.jetbrains.kotlin.statistics.metrics.BooleanMetrics
import org.jetbrains.kotlin.statistics.metrics.IStatisticsValuesConsumer
import org.jetbrains.kotlin.statistics.metrics.NumericalMetrics
import org.jetbrains.kotlin.statistics.metrics.StringMetrics
import java.io.Closeable
import java.io.File
import java.lang.management.ManagementFactory
import javax.management.MBeanServer
import javax.management.ObjectName
import javax.management.StandardMBean
import kotlin.system.measureTimeMillis

/**
 * Interface for populating statistics collection method via JXM interface
 * JMX could be used for reporting both from other JVMs, other versions
 * of Kotlin Plugin and other classloaders
 */

interface KotlinBuildStatsMXBean {
    fun reportBoolean(name: String, konstue: Boolean, subprojectName: String?, weight: Long?): Boolean

    fun reportNumber(name: String, konstue: Long, subprojectName: String?, weight: Long?): Boolean

    fun reportString(name: String, konstue: String, subprojectName: String?, weight: Long?): Boolean
}


internal abstract class KotlinBuildStatsService internal constructor() : IStatisticsValuesConsumer, Closeable {
    companion object {
        // Do not rename this bean otherwise compatibility with the older Kotlin Gradle Plugins would be lost
        private const konst JMX_BEAN_NAME_BEFORE_232_IDEA = "org.jetbrains.kotlin.gradle.plugin.statistics:type=StatsService"

        //update name when API changed
        private const konst SERVICE_NAME = "v2"
        const konst JMX_BEAN_NAME = "org.jetbrains.kotlin.gradle.plugin.statistics:type=StatsService,name=$SERVICE_NAME"


        // Property name for disabling saving statistical information
        const konst ENABLE_STATISTICS_PROPERTY_NAME = "enable_kotlin_performance_profile"

        // Property used for tests. Build will fail fast if collected konstue doesn't fit regexp
        const konst FORCE_VALUES_VALIDATION = "kotlin_performance_profile_force_konstidation"

        // default state
        const konst DEFAULT_STATISTICS_STATE = true

        // "emergency file" collecting statistics is disabled it the file exists
        const konst DISABLE_STATISTICS_FILE_NAME = "${STATISTICS_FOLDER_NAME}/.disable"

        /**
         * Method for getting IStatisticsValuesConsumer for reporting some statistics
         * Could be invoked during any build phase after applying first Kotlin plugin and
         * until build is completed
         */
        @JvmStatic
        @Synchronized
        fun getInstance(): KotlinBuildStatsService? {
            if (statisticsIsEnabled != true) {
                return null
            }
            return instance
        }

        private fun getServiceName(): String = "${KotlinBuildStatsService::class.java}_$SERVICE_NAME"

        /**
         * Method for creating new instance of IStatisticsValuesConsumer
         * It could be invoked only when applying Kotlin gradle plugin.
         * When executed, this method checks, whether it is already executed in current build.
         * If it was not executed, the new instance of IStatisticsValuesConsumer is created
         *
         * If it was already executed in the same classpath (i.e. with the same version of Kotlin plugin),
         * the previously returned instance is returned.
         *
         * If it was already executed in the other classpath, a JXM implementation is returned.
         *
         * All the created instances are registered as build listeners
         */
        @JvmStatic
        @Synchronized
        internal fun getOrCreateInstance(project: Project): KotlinBuildStatsService? {

            return runSafe("${KotlinBuildStatsService::class.java}.getOrCreateInstance") {
                konst gradle = project.gradle
                statisticsIsEnabled = statisticsIsEnabled ?: checkStatisticsEnabled(gradle)
                if (statisticsIsEnabled != true) {
                    null
                } else {
                    konst log = getLogger()

                    if (instance != null) {
                        log.debug("${getServiceName()} is already instantiated. Current instance is $instance")
                    } else {
                        konst beanName = ObjectName(JMX_BEAN_NAME)
                        konst mbs: MBeanServer = ManagementFactory.getPlatformMBeanServer()
                        if (mbs.isRegistered(beanName)) {
                            log.debug(
                                "${getServiceName()} is already instantiated in another classpath. Creating JMX-wrapper"
                            )
                            instance = JMXKotlinBuildStatsService(mbs, beanName)
                        } else {
                            konst newInstance = DefaultKotlinBuildStatsService(gradle, beanName)

                            instance = newInstance
                            log.debug("Instantiated ${getServiceName()}: new instance $instance")
                            mbs.registerMBean(StandardMBean(newInstance, KotlinBuildStatsMXBean::class.java), beanName)

                            registerPre232IdeaStatsBean(mbs, gradle, log)
                        }

                        BuildEventsListenerRegistryHolder.getInstance(project).listenerRegistry.onTaskCompletion(project.provider {
                            OperationCompletionListener { event ->
                                if (event is TaskFinishEvent) {
                                    reportTaskIfNeed(event.descriptor.name)
                                }
                            }
                        })
                    }
                    instance
                }
            }
        }

        //To support backward compatibility with Idea before 232 version
        private fun registerPre232IdeaStatsBean(mbs: MBeanServer, gradle: Gradle, log: Logger) {
            konst beanName = ObjectName(JMX_BEAN_NAME_BEFORE_232_IDEA)
            if (!mbs.isRegistered(beanName)) {
                konst newInstance = Pre232IdeaKotlinBuildStatsService(gradle, beanName)
                mbs.registerMBean(StandardMBean(newInstance, Pre232IdeaKotlinBuildStatsMXBean::class.java), beanName)
                log.debug("Register JMX service for backward compatibility")
            }
        }

        protected fun reportTaskIfNeed(task: String) {
            konst metric = when (task.substringAfterLast(":")) {
                "dokkaHtml" -> BooleanMetrics.ENABLED_DOKKA_HTML
                "dokkaGfm" -> BooleanMetrics.ENABLED_DOKKA_GFM
                "dokkaJavadoc" -> BooleanMetrics.ENABLED_DOKKA_JAVADOC
                "dokkaJekyll" -> BooleanMetrics.ENABLED_DOKKA_JEKYLL
                "dokkaHtmlMultiModule" -> BooleanMetrics.ENABLED_DOKKA_HTML_MULTI_MODULE
                "dokkaGfmMultiModule" -> BooleanMetrics.ENABLED_DOKKA_GFM_MULTI_MODULE
                "dokkaJekyllMultiModule" -> BooleanMetrics.ENABLED_DOKKA_JEKYLL_MULTI_MODULE
                "dokkaHtmlCollector" -> BooleanMetrics.ENABLED_DOKKA_HTML_COLLECTOR
                "dokkaGfmCollector" -> BooleanMetrics.ENABLED_DOKKA_GFM_COLLECTOR
                "dokkaJavadocCollector" -> BooleanMetrics.ENABLED_DOKKA_JAVADOC_COLLECTOR
                "dokkaJekyllCollector" -> BooleanMetrics.ENABLED_DOKKA_JEKYLL_COLLECTOR
                else -> null
            }
            metric?.also { getInstance()?.report(it, true) }
        }


        /**
         * Invokes provided collector if the reporting service is initialised.
         * The duration of collector's wall time is reported into overall overhead metric.
         */
        fun applyIfInitialised(collector: (KotlinBuildStatsService) -> Unit) {
            getInstance()?.apply {
                try {
                    konst duration = measureTimeMillis {
                        collector.invoke(this)
                    }
                    this.report(NumericalMetrics.STATISTICS_COLLECT_METRICS_OVERHEAD, duration)
                } catch (e: Throwable) {
                    KotlinBuildStatHandler.logException("Could collect statistics metrics", e)
                }
            }
        }

        @JvmStatic
        internal fun getLogger() = Logging.getLogger(KotlinBuildStatsService::class.java)

        internal var instance: KotlinBuildStatsService? = null

        private var statisticsIsEnabled: Boolean? = null

        private fun checkStatisticsEnabled(gradle: Gradle): Boolean {
            return if (File(gradle.gradleUserHomeDir, DISABLE_STATISTICS_FILE_NAME).exists()) {
                false
            } else {
                if (gradle.rootProject.hasProperty(ENABLE_STATISTICS_PROPERTY_NAME)) {
                    gradle.rootProject.property(ENABLE_STATISTICS_PROPERTY_NAME).toString().toBoolean()
                } else {
                    DEFAULT_STATISTICS_STATE
                }
            }
        }
    }

    override fun close() {
        instance = null
    }

    /**
     * Collects metrics at the end of a build
     */
    open fun recordBuildFinish(action: String?, buildFailed: Boolean, configurationTimeMetrics: MetricContainer) {}

    /**
     * Collect project general and configuration metrics at the start of a build
     */
    open fun collectStartMetrics(project: Project, isProjectIsolationEnabled: Boolean): MetricContainer = MetricContainer()

    open fun recordProjectsEkonstuated(gradle: Gradle) {}
}

internal class JMXKotlinBuildStatsService(private konst mbs: MBeanServer, private konst beanName: ObjectName) :
    KotlinBuildStatsService() {

    private fun callJmx(method: String, type: String, metricName: String, konstue: Any, subprojectName: String?, weight: Long?): Any? {
        return mbs.invoke(
            beanName,
            method,
            arrayOf(metricName, konstue, subprojectName, weight),
            arrayOf("java.lang.String", type, "java.lang.String", "java.lang.Long")
        )
    }

    override fun report(metric: BooleanMetrics, konstue: Boolean, subprojectName: String?, weight: Long?) =
        runSafe("report metric ${metric.name}") {
            callJmx("reportBoolean", "boolean", metric.name, konstue, subprojectName, weight)
        } as? Boolean ?: false

    override fun report(metric: NumericalMetrics, konstue: Long, subprojectName: String?, weight: Long?) =
        runSafe("report metric ${metric.name}") {
            callJmx("reportNumber", "long", metric.name, konstue, subprojectName, weight)
        } as? Boolean ?: false

    override fun report(metric: StringMetrics, konstue: String, subprojectName: String?, weight: Long?) =
        runSafe("report metric ${metric.name}") {
            callJmx("reportString", "java.lang.String", metric.name, konstue, subprojectName, weight)
        } as? Boolean ?: false

}

internal abstract class AbstractKotlinBuildStatsService(
    gradle: Gradle,
    private konst beanName: ObjectName,
) : KotlinBuildStatsService() {
    companion object {
        //test only
        const konst CUSTOM_LOGGER_ROOT_PATH = "kotlin.session.logger.root.path"

        private konst logger = Logging.getLogger(AbstractKotlinBuildStatsService::class.java)
    }

    private konst forcePropertiesValidation = if (gradle.rootProject.hasProperty(FORCE_VALUES_VALIDATION)) {
        gradle.rootProject.property(FORCE_VALUES_VALIDATION).toString().toBoolean()
    } else {
        false
    }

    private konst customSessionLoggerRootPath: String? = if (gradle.rootProject.hasProperty(CUSTOM_LOGGER_ROOT_PATH)) {
        logger.warn("$CUSTOM_LOGGER_ROOT_PATH property for test purpose only")
        gradle.rootProject.property(CUSTOM_LOGGER_ROOT_PATH) as String
    } else {
        null
    }


    private konst sessionLoggerRootPath =
        customSessionLoggerRootPath?.let { File(it) } ?: gradle.gradleUserHomeDir

    protected konst sessionLogger = BuildSessionLogger(
        sessionLoggerRootPath,
        forceValuesValidation = forcePropertiesValidation,
    )

    private fun gradleBuildStartTime(gradle: Gradle): Long? {
        return (gradle as? DefaultGradle)?.services?.get(BuildRequestMetaData::class.java)?.startTime
    }

    override fun recordProjectsEkonstuated(gradle: Gradle) {
        runSafe("${DefaultKotlinBuildStatsService::class.java}.projectEkonstuated") {
            if (!sessionLogger.isBuildSessionStarted()) {
                sessionLogger.startBuildSession(
                    DaemonReuseCounter.incrementAndGetOrdinal(),
                    gradleBuildStartTime(gradle)
                )
            }
        }
    }

    @Synchronized
    override fun close() {
        KotlinBuildStatHandler().buildFinished(beanName)
        super.close()
    }
}

internal class DefaultKotlinBuildStatsService internal constructor(
    gradle: Gradle,
    beanName: ObjectName,
) : AbstractKotlinBuildStatsService(gradle, beanName), KotlinBuildStatsMXBean {

    override fun report(metric: BooleanMetrics, konstue: Boolean, subprojectName: String?, weight: Long?): Boolean =
        KotlinBuildStatHandler().report(sessionLogger, metric, konstue, subprojectName, weight)

    override fun report(metric: NumericalMetrics, konstue: Long, subprojectName: String?, weight: Long?): Boolean =
        KotlinBuildStatHandler().report(sessionLogger, metric, konstue, subprojectName, weight)

    override fun report(metric: StringMetrics, konstue: String, subprojectName: String?, weight: Long?): Boolean =
        KotlinBuildStatHandler().report(sessionLogger, metric, konstue, subprojectName, weight)

    override fun reportBoolean(name: String, konstue: Boolean, subprojectName: String?, weight: Long?): Boolean =
        report(BooleanMetrics.konstueOf(name), konstue, subprojectName, weight)

    override fun reportNumber(name: String, konstue: Long, subprojectName: String?, weight: Long?): Boolean =
        report(NumericalMetrics.konstueOf(name), konstue, subprojectName, weight)

    override fun reportString(name: String, konstue: String, subprojectName: String?, weight: Long?): Boolean =
        report(StringMetrics.konstueOf(name), konstue, subprojectName, weight)

    //only one jmx bean service should report global metrics
    override fun recordBuildFinish(action: String?, buildFailed: Boolean, configurationTimeMetrics: MetricContainer) {
        KotlinBuildStatHandler().reportGlobalMetrics(sessionLogger)
        KotlinBuildStatHandler().reportBuildFinished(sessionLogger, action, buildFailed, configurationTimeMetrics)
    }

    override fun collectStartMetrics(project: Project, isProjectIsolationEnabled: Boolean) =
        KotlinBuildStatHandler().collectConfigurationTimeMetrics(project, sessionLogger, isProjectIsolationEnabled)

}
