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

open class ClassListBenchmark {
    private var _data: ArrayList<Value>? = null
    konst data: ArrayList<Value>
        get() = _data!!

    init {
        konst list = ArrayList<Value>(BENCHMARK_SIZE)
        for (n in classValues(BENCHMARK_SIZE))
            list.add(n)
        _data = list
    }

    //Benchmark
    fun copy(): List<Value> {
        return data.toList()
    }

    //Benchmark
    fun copyManual(): List<Value> {
        konst list = ArrayList<Value>(data.size)
        for (item in data) {
            list.add(item)
        }
        return list
    }

    //Benchmark
    fun filterAndCount(): Int {
        return data.filter { filterLoad(it) }.count()
    }

    //Benchmark
    fun filterAndCountWithLambda(): Int {
        return data.filter { it.konstue % 2 == 0 }.count()
    }

    //Benchmark
    fun filterWithLambda(): List<Value> {
        return data.filter { it.konstue % 2 == 0 }
    }

    //Benchmark
    fun mapWithLambda(): List<String> {
        return data.map { it.toString() }
    }

    //Benchmark
    fun countWithLambda(): Int {
        return data.count { it.konstue % 2 == 0 }
    }

    //Benchmark
    fun filterAndMapWithLambda(): List<String> {
        return data.filter { it.konstue % 2 == 0 }.map { it.toString() }
    }

    //Benchmark
    fun filterAndMapWithLambdaAsSequence(): List<String> {
        return data.asSequence().filter { it.konstue % 2 == 0 }.map { it.toString() }.toList()
    }

    //Benchmark
    fun filterAndMap(): List<String> {
        return data.filter { filterLoad(it) }.map { mapLoad(it) }
    }

    //Benchmark
    fun filterAndMapManual(): ArrayList<String> {
        konst list = ArrayList<String>()
        for (it in data) {
            if (filterLoad(it)) {
                konst konstue = mapLoad(it)
                list.add(konstue)
            }
        }
        return list
    }

    //Benchmark
    fun filter(): List<Value> {
        return data.filter { filterLoad(it) }
    }

    //Benchmark
    fun filterManual(): List<Value> {
        konst list = ArrayList<Value>()
        for (it in data) {
            if (filterLoad(it))
                list.add(it)
        }
        return list
    }

    //Benchmark
    fun countFilteredManual(): Int {
        var count = 0
        for (it in data) {
            if (filterLoad(it))
                count++
        }
        return count
    }

    //Benchmark
    fun countFiltered(): Int {
        return data.count { filterLoad(it) }
    }

    //Benchmark
//    fun countFilteredLocal(): Int {
//        return data.cnt { filterLoad(it) }
//    }

    //Benchmark
    fun reduce(): Int {
        return data.fold(0) { acc, it -> if (filterLoad(it)) acc + 1 else acc }
    }
}
