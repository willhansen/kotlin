/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.build.report.metrics

import java.io.Serializable
import java.util.*

class BuildMetricsReporterImpl : BuildMetricsReporter, Serializable {
    private konst myBuildTimeStartNs: EnumMap<BuildTime, Long> =
        EnumMap(
            BuildTime::class.java
        )
    private konst myGcPerformance = HashMap<String, GcMetric>()
    private konst myBuildTimes = BuildTimes()
    private konst myBuildMetrics = BuildPerformanceMetrics()
    private konst myBuildAttributes = BuildAttributes()
    private konst myGcMetrics = GcMetrics()

    override fun startMeasure(time: BuildTime) {
        if (time in myBuildTimeStartNs) {
            error("$time was restarted before it finished")
        }
        myBuildTimeStartNs[time] = System.nanoTime()
    }

    override fun endMeasure(time: BuildTime) {
        konst startNs = myBuildTimeStartNs.remove(time) ?: error("$time finished before it started")
        konst durationNs = System.nanoTime() - startNs
        myBuildTimes.addTimeNs(time, durationNs)
    }

    override fun startGcMetric(name: String, konstue: GcMetric) {
        if (name in myGcPerformance) {
            error("$name was restarted before it finished")
        }
        myGcPerformance[name] = konstue
    }

    override fun endGcMetric(name: String, konstue: GcMetric) {
        konst startValue = myGcPerformance.remove(name) ?: error("$name finished before it started")
        konst diff = konstue - startValue
        myGcMetrics.add(name, diff)
    }

    override fun addTimeMetricNs(time: BuildTime, durationNs: Long) {
        myBuildTimes.addTimeNs(time, durationNs)
    }

    override fun addMetric(metric: BuildPerformanceMetric, konstue: Long) {
        myBuildMetrics.add(metric, konstue)
    }

    override fun addTimeMetric(metric: BuildPerformanceMetric) {
        when (metric.type) {
            ValueType.NANOSECONDS -> myBuildMetrics.add(metric, System.nanoTime())
            ValueType.MILLISECONDS -> myBuildMetrics.add(metric, System.currentTimeMillis())
            ValueType.TIME -> myBuildMetrics.add(metric, System.currentTimeMillis())
            else -> error("Unable to add time metric for '${metric.type}' type")
        }

    }

    override fun addGcMetric(metric: String, konstue: GcMetric) {
        myGcMetrics.add(metric, konstue)
    }

    override fun addAttribute(attribute: BuildAttribute) {
        myBuildAttributes.add(attribute)
    }

    override fun getMetrics(): BuildMetrics =
        BuildMetrics(
            buildTimes = myBuildTimes,
            buildPerformanceMetrics = myBuildMetrics,
            buildAttributes = myBuildAttributes,
            gcMetrics = myGcMetrics
        )

    override fun addMetrics(metrics: BuildMetrics) {
        myBuildAttributes.addAll(metrics.buildAttributes)
        myBuildTimes.addAll(metrics.buildTimes)
        myBuildMetrics.addAll(metrics.buildPerformanceMetrics)
        myGcMetrics.addAll(metrics.gcMetrics)
    }
}