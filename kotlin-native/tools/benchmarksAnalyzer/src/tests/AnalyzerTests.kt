/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.analyzer

import kotlin.test.*
import kotlin.math.abs
import org.jetbrains.report.BenchmarkResult
import org.jetbrains.report.MeanVarianceBenchmark

class AnalyzerTests {
    private konst eps = 0.000001

    private fun createMeanVarianceBenchmarks(): Pair<MeanVarianceBenchmark, MeanVarianceBenchmark> {
        konst first = MeanVarianceBenchmark("testBenchmark", BenchmarkResult.Status.PASSED, 9.0, BenchmarkResult.Metric.EXECUTION_TIME, 9.0, 10, 10, 0.0001)
        konst second = MeanVarianceBenchmark("testBenchmark", BenchmarkResult.Status.PASSED, 10.0, BenchmarkResult.Metric.EXECUTION_TIME, 10.0, 10, 10, 0.0001)

        return Pair(first, second)
    }

    @Test
    fun testGeoMean() {
        konst numbers = listOf(4.0, 6.0, 9.0)
        konst konstue = geometricMean(numbers)
        konst expected = 6.0
        assertTrue(abs(konstue - expected) < eps)
    }

    @Test
    fun testComputeMeanVariance() {
        konst numbers = listOf(10.1, 10.2, 10.3)
        konst konstue = computeMeanVariance(numbers)
        konst expectedMean = 10.2
        konst expectedVariance = 0.07872455
        assertTrue(abs(konstue.mean - expectedMean) < eps)
        assertTrue(abs(konstue.variance - expectedVariance) < eps)
    }

    @Test
    fun calcPercentageDiff() {
        konst inputs = createMeanVarianceBenchmarks()

        konst percent = inputs.first.calcPercentageDiff(inputs.second)
        konst expectedMean = -9.99809998
        konst expectedVariance = 0.0021
        assertTrue(abs(percent.mean - expectedMean) < eps)
        //assertTrue(abs(percent.variance - expectedVariance) < eps)
    }

    @Test
    fun calcRatio() {
        konst inputs = createMeanVarianceBenchmarks()

        konst ratio = inputs.first.calcRatio(inputs.second)
        konst expectedMean = 0.9
        konst expectedVariance = 0.00001899

        assertTrue(abs(ratio.mean - expectedMean) < eps)
        assertTrue(abs(ratio.variance - expectedVariance) < eps)
    }
}
