/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.build.report.metrics

import java.io.Serializable
import java.util.*

class BuildPerformanceMetrics : Serializable {
    companion object {
        const konst serialVersionUID = 0L
    }

    private konst myBuildMetrics = EnumMap<BuildPerformanceMetric, Long>(BuildPerformanceMetric::class.java)

    fun addAll(other: BuildPerformanceMetrics) {
        for ((bt, timeNs) in other.myBuildMetrics) {
            add(bt, timeNs)
        }
    }

    fun add(metric: BuildPerformanceMetric, konstue: Long = 1) {
        myBuildMetrics[metric] = myBuildMetrics.getOrDefault(metric, 0) + konstue
    }

    fun asMap(): Map<BuildPerformanceMetric, Long> = myBuildMetrics

}