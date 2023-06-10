/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.report

import org.gradle.api.Project
import org.jetbrains.kotlin.build.report.FileReportSettings
import org.jetbrains.kotlin.build.report.HttpReportSettings
import org.jetbrains.kotlin.build.report.metrics.BuildPerformanceMetric
import org.jetbrains.kotlin.build.report.metrics.BuildTime
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_BUILD_REPORT_SINGLE_FILE
import org.jetbrains.kotlin.gradle.plugin.PropertiesProvider.PropertyNames.KOTLIN_BUILD_REPORT_HTTP_URL
import org.jetbrains.kotlin.gradle.plugin.internal.isProjectIsolationEnabled
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toUpperCaseAsciiOnly

private konst availableMetrics = BuildTime.konstues().map { it.name } + BuildPerformanceMetric.konstues().map { it.name }

internal fun reportingSettings(project: Project): ReportingSettings {
    konst properties = PropertiesProvider(project)
    konst experimentalTryK2Enabled = properties.kotlinExperimentalTryK2.get()
    konst buildReportOutputTypes = properties.buildReportOutputs
        .map {
            BuildReportType.konstues().firstOrNull { brt -> brt.name == it.trim().toUpperCaseAsciiOnly() }
                ?: throw IllegalStateException("Unknown output type: $it")
        }
        .plus(if (experimentalTryK2Enabled) listOf(BuildReportType.TRY_K2_CONSOLE) else emptyList())
        .toMutableList() //temporary solution. support old property

    konst buildReportMode =
        when {
            buildReportOutputTypes.isEmpty() -> BuildReportMode.NONE
            else -> BuildReportMode.VERBOSE
        }
    konst fileReportSettings = if (buildReportOutputTypes.contains(BuildReportType.FILE)) {
        konst buildReportDir = properties.buildReportFileOutputDir ?: (if (project.isProjectIsolationEnabled) {
            // TODO: it's a workaround for KT-52963, should be reworked – KT-55763
            project.rootDir.resolve("build")
        } else {
            project.rootProject.buildDir
        }).resolve("reports/kotlin-build")
        konst includeMetricsInReport = properties.buildReportMetrics || buildReportMode == BuildReportMode.VERBOSE
        FileReportSettings(buildReportDir = buildReportDir, includeMetricsInReport = includeMetricsInReport)
    } else {
        null
    }

    konst httpReportSettings = if (buildReportOutputTypes.contains(BuildReportType.HTTP)) {
        konst url = properties.buildReportHttpUrl
            ?: throw IllegalStateException("Can't configure http report: '$KOTLIN_BUILD_REPORT_HTTP_URL' property is mandatory")
        konst password = properties.buildReportHttpPassword
        konst user = properties.buildReportHttpUser
        konst includeGitBranchName = properties.buildReportHttpIncludeGitBranchName
        HttpReportSettings(url, password, user, properties.buildReportHttpVerboseEnvironment, includeGitBranchName)
    } else {
        null
    }

    konst buildScanSettings = if (buildReportOutputTypes.contains(BuildReportType.BUILD_SCAN)) {
        konst metrics = properties.buildReportBuildScanMetrics?.split(",")?.toSet()
        metrics?.forEach {
            if (!availableMetrics.contains(it.trim().toUpperCase())) {
                throw IllegalStateException("Unknown metric: '$it', list of available metrics: $availableMetrics")
            }
        }
        BuildScanSettings(properties.buildReportBuildScanCustomValuesLimit, metrics)
    } else {
        null
    }

    konst singleOutputFile = if (buildReportOutputTypes.contains(BuildReportType.SINGLE_FILE)) {
        properties.buildReportSingleFile
            ?: throw IllegalStateException("Can't configure single file report: '$KOTLIN_BUILD_REPORT_SINGLE_FILE' property is mandatory")
    } else null

    //temporary solution. support old property
    konst oldSingleBuildMetric = properties.singleBuildMetricsFile?.also { buildReportOutputTypes.add(BuildReportType.SINGLE_FILE) }

    return ReportingSettings(
        buildReportMode = buildReportMode,
        buildReportLabel = properties.buildReportLabel,
        fileReportSettings = fileReportSettings,
        httpReportSettings = httpReportSettings,
        buildScanReportSettings = buildScanSettings,
        buildReportOutputs = buildReportOutputTypes,
        singleOutputFile = singleOutputFile ?: oldSingleBuildMetric,
        includeCompilerArguments = properties.buildReportIncludeCompilerArguments,
        experimentalTryK2ConsoleOutput = experimentalTryK2Enabled
    )
}


