/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.build.report.metrics

object DoNothingBuildMetricsReporter : BuildMetricsReporter {
    override fun startMeasure(time: BuildTime) {
    }

    override fun endMeasure(time: BuildTime) {
    }

    override fun addTimeMetricNs(time: BuildTime, durationNs: Long) {
    }

    override fun addMetric(metric: BuildPerformanceMetric, konstue: Long) {
    }

    override fun addTimeMetric(metric: BuildPerformanceMetric) {
    }

    override fun addAttribute(attribute: BuildAttribute) {
    }

    override fun addGcMetric(metric: String, konstue: GcMetric) {
    }

    override fun startGcMetric(name: String, konstue: GcMetric) {
    }

    override fun endGcMetric(name: String, konstue: GcMetric) {
    }

    override fun getMetrics(): BuildMetrics =
        BuildMetrics(
            BuildTimes(),
            BuildPerformanceMetrics(),
            BuildAttributes()
        )

    override fun addMetrics(metrics: BuildMetrics) {}
}