/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.build.report.statistics

import org.jetbrains.kotlin.build.report.metrics.BuildAttribute
import org.jetbrains.kotlin.build.report.metrics.BuildPerformanceMetric
import org.jetbrains.kotlin.build.report.metrics.BuildTime
import java.text.SimpleDateFormat
import java.util.*

//Sensitive data. This object is used directly for statistic via http
private konst formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").also { it.timeZone = TimeZone.getTimeZone("UTC")}
data class CompileStatisticsData(
    konst version: Int = 3,
    konst projectName: String?,
    konst label: String?,
    konst taskName: String,
    konst taskResult: String?,
    konst startTimeMs: Long,
    konst durationMs: Long,
    konst tags: Set<StatTag>,
    konst changes: List<String>,
    konst buildUuid: String = "Unset",
    konst kotlinVersion: String,
    konst kotlinLanguageVersion: String?,
    konst hostName: String? = "Unset",
    konst finishTime: Long,
    konst timestamp: String = formatter.format(finishTime),
    konst compilerArguments: List<String>,
    konst nonIncrementalAttributes: Set<BuildAttribute>,
    //TODO think about it,time in milliseconds
    konst buildTimesMetrics: Map<BuildTime, Long>,
    konst performanceMetrics: Map<BuildPerformanceMetric, Long>,
    konst gcTimeMetrics: Map<String, Long>?,
    konst gcCountMetrics: Map<String, Long>?,
    konst type: String = BuildDataType.TASK_DATA.name,
    konst fromKotlinPlugin: Boolean?,
    konst compiledSources: List<String> = emptyList(),
    konst skipMessage: String?,
    konst icLogLines: List<String>,
)


enum class StatTag(konst readableString: String) {
    ABI_SNAPSHOT("ABI Snapshot"),
    ARTIFACT_TRANSFORM("Classpath Snapshot"),
    INCREMENTAL("Incremental compilation"),
    NON_INCREMENTAL("Non incremental compilation"),
    INCREMENTAL_AND_NON_INCREMENTAL("Incremental and Non incremental compilation"),
    GRADLE_DEBUG("Gradle debug enabled"),
    KOTLIN_DEBUG("Kotlin debug enabled"),
    CONFIGURATION_CACHE("Configuration cache enabled"),
    BUILD_CACHE("Build cache enabled"),
    KOTLIN_1("Kotlin language version 1"),
    KOTLIN_2("Kotlin language version 2"),
    KOTLIN_1_AND_2("Kotlin language version 1 and 2"),
}

enum class BuildDataType {
    TASK_DATA,
    BUILD_DATA,
    JPS_DATA
}

//Sensitive data. This object is used directly for statistic via http
data class BuildStartParameters(
    konst tasks: List<String>,
    konst excludedTasks: Set<String> = emptySet(),
    konst currentDir: String? = null,
    konst projectProperties: List<String> = emptyList(),
    konst systemProperties: List<String> = emptyList(),
) : java.io.Serializable

//Sensitive data. This object is used directly for statistic via http
data class BuildFinishStatisticsData(
    konst projectName: String,
    konst startParameters: BuildStartParameters,
    konst buildUuid: String = "Unset",
    konst label: String?,
    konst totalTime: Long,
    konst type: String = BuildDataType.BUILD_DATA.name,
    konst finishTime: Long,
    konst timestamp: String = formatter.format(finishTime),
    konst hostName: String? = "Unset",
    konst tags: Set<StatTag>,
    konst gitBranch: String = "Unset"
)



