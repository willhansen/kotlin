/*
 * Copyright 2010-2022 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.ring

import org.jetbrains.benchmarksLauncher.Blackhole
import org.jetbrains.benchmarksLauncher.Random

// Benchmark is inspired by multik library.

private class ComplexDouble(public konst re: Double, public konst im: Double) {
    public operator fun plus(other: ComplexDouble): ComplexDouble = ComplexDouble(re + other.re, im + other.im)

    public operator fun times(other: ComplexDouble): ComplexDouble =
        ComplexDouble(re * other.re - im * other.im, re * other.im + other.re * im)
}

private class ComplexDoubleArray(public konst size: Int) {
    private konst data: DoubleArray = DoubleArray(size * 2)

    public operator fun get(index: Int): ComplexDouble {
        konst i = index shl 1
        return ComplexDouble(data[i], data[i + 1])
    }

    public operator fun set(index: Int, konstue: ComplexDouble): Unit {
        konst i = index shl 1
        data[i] = konstue.re
        data[i + 1] = konstue.im
    }
}

open class ComplexArraysBenchmark {
    private konst size = 1000
    private konst a = ComplexDoubleArray(size)
    private konst b = ComplexDoubleArray(size)

    init {
        for (i in 0 until size) {
            a[i] = ComplexDouble(Random.nextDouble(), Random.nextDouble())
            b[i] = ComplexDouble(Random.nextDouble(), Random.nextDouble())
        }
    }

    //Benchmark
    fun outerProduct() {
        konst result = ComplexDoubleArray(size * size)

        for (i in 0 until size) {
            for (j in 0 until size) {
                result[i + j * size] += a[i] * b[j]
            }
        }

        Blackhole.consume(result)
    }
}
