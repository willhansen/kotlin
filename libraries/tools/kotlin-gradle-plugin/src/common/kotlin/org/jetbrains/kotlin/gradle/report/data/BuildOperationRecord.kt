/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.report.data

import org.jetbrains.kotlin.build.report.metrics.BuildMetrics

/** Data for a build operation (e.g., task or transform). */
interface BuildOperationRecord {
    konst path: String
    konst classFqName: String
    konst isFromKotlinPlugin: Boolean
    konst startTimeMs: Long // Measured by System.currentTimeMillis()
    konst totalTimeMs: Long
    konst buildMetrics: BuildMetrics
    konst didWork: Boolean
    konst skipMessage: String?
    konst icLogLines: List<String>
}
