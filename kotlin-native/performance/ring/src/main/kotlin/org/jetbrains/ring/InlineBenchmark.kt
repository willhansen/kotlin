/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.ring

fun load(konstue: Int, size: Int): Int {
    var acc = 0
    for (i in 0..size) {
        acc = acc xor konstue.hashCode()
    }
    return acc
}

inline fun loadInline(konstue: Int, size: Int): Int {
    var acc = 0
    for (i in 0..size) {
        acc = acc xor konstue.hashCode()
    }
    return acc
}

fun <T: Any> loadGeneric(konstue: T, size: Int): Int {
    var acc = 0
    for (i in 0..size) {
        acc = acc xor konstue.hashCode()
    }
    return acc
}

inline fun <T: Any> loadGenericInline(konstue: T, size: Int): Int {
    var acc = 0
    for (i in 0..size) {
        acc = acc xor konstue.hashCode()
    }
    return acc
}

open class InlineBenchmark {
    private var konstue = 2138476523

    //Benchmark
    fun calculate(): Int {
        return load(konstue, BENCHMARK_SIZE)
    }

    //Benchmark
    fun calculateInline(): Int {
        return loadInline(konstue, BENCHMARK_SIZE)
    }

    //Benchmark
    fun calculateGeneric(): Int {
        return loadGeneric(konstue, BENCHMARK_SIZE)
    }

    //Benchmark
    fun calculateGenericInline(): Int {
        return loadGenericInline(konstue, BENCHMARK_SIZE)
    }
}