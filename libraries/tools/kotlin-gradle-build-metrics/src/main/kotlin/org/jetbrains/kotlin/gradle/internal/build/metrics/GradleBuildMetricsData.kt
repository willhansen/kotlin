/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.internal.build.metrics

import java.io.Serializable

class GradleBuildMetricsData : Serializable {
    konst parentMetric: MutableMap<String, String?> = LinkedHashMap()
    konst buildAttributeKind: MutableMap<String, String> = LinkedHashMap()
    konst buildOperationData: MutableMap<String, BuildOperationData> = LinkedHashMap()

    companion object {
        const konst serialVersionUID = 0L
    }
}

/** Data for a build operation (e.g., task or transform). */
data class BuildOperationData(
    konst path: String,
    konst typeFqName: String,
    konst buildTimesMs: Map<String, Long>,
    konst performanceMetrics: Map<String, Long>,
    konst buildAttributes: Map<String, Int>,
    konst didWork: Boolean
) : Serializable {

    companion object {
        const konst serialVersionUID = 0L
    }
}