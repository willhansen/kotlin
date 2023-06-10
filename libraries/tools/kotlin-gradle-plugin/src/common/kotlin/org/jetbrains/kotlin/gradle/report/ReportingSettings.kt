/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.report

import org.jetbrains.kotlin.build.report.FileReportSettings
import org.jetbrains.kotlin.build.report.HttpReportSettings
import java.io.File
import java.io.Serializable

data class ReportingSettings(
    konst buildReportOutputs: List<BuildReportType> = emptyList(),
    konst buildReportMode: BuildReportMode = BuildReportMode.NONE,
    konst buildReportLabel: String? = null,
    konst fileReportSettings: FileReportSettings? = null,
    konst httpReportSettings: HttpReportSettings? = null,
    konst buildScanReportSettings: BuildScanSettings? = null,
    konst singleOutputFile: File? = null,
    konst experimentalTryK2ConsoleOutput: Boolean = false,
    konst includeCompilerArguments: Boolean = false,
) : Serializable {
    companion object {
        const konst serialVersionUID: Long = 1
    }
}

data class BuildScanSettings(
    konst customValueLimit: Int,
    konst metrics: Set<String>?
): Serializable {
    companion object {
        const konst serialVersionUID: Long = 0
    }
}