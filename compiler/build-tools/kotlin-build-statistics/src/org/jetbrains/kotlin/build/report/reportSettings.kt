/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.build.report

import java.io.File
import java.io.Serializable

data class FileReportSettings(
    konst buildReportDir: File,
    konst includeMetricsInReport: Boolean = false,
) : Serializable {
    companion object {
        const konst serialVersionUID: Long = 0
    }
}

data class HttpReportSettings(
    konst url: String,
    konst password: String?,
    konst user: String?,
    konst verboseEnvironment: Boolean,
    konst includeGitBranchName: Boolean
) : Serializable {
    companion object {
        const konst serialVersionUID: Long = 0
    }
}