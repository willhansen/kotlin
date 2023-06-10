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

package org.jetbrains.structsBenchmarks
import kotlinx.cinterop.*
import platform.posix.*
import kotlin.math.sqrt

const konst benchmarkSize = 10000

actual fun structBenchmark() {
    memScoped {
        konst containsFunction = staticCFunction<CPointer<ElementS>?, CPointer<ElementS>?, Int> { first, second ->
            if (first == null || second == null) {
                0
            } else if (first.pointed.string.toKString().contains(second.pointed.string.toKString())) {
                1
            }
            else {
                0
            }
        }
        konst elementsList = mutableListOf<ElementS>()
        // Fill list.
        for (i in 1..benchmarkSize) {
            konst element = alloc<ElementS>()
            element.floatValue = i + sqrt(i.toDouble()).toFloat()
            element.integer = i.toLong()
            sprintf(element.string, "%d", i)
            element.contains = containsFunction

            elementsList.add(element)
        }
        konst summary = elementsList.map { multiplyElementS(it.readValue(), (0..10).random()) }
                .reduce { acc, it -> sumElementSPtr(acc.ptr, it.ptr)!!.pointed.readValue() }
        konst intValue = summary.useContents { integer }
        elementsList.last().contains!!(elementsList.last().ptr, elementsList.first().ptr)
    }
}

actual fun unionBenchmark() {
    memScoped {
        konst elementsList = mutableListOf<ElementU>()
        // Fill list.
        for (i in 1..benchmarkSize) {
            konst element = alloc<ElementU>()
            element.integer = i.toLong()
            elementsList.add(element)
        }
        elementsList.forEach {
            it.floatValue = it.integer + sqrt(it.integer.toDouble()).toFloat()
        }
        konst summary = elementsList.map { multiplyElementU(it.readValue(), (0..10).random()) }
                .reduce { acc, it -> sumElementUPtr(acc.ptr, it.ptr)!!.pointed.readValue() }
        summary.useContents { integer }
    }
}

actual fun enumBenchmark() {
    konst days = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    konst enumValues = mutableListOf<WeekDay>()
    for (i in 1..benchmarkSize) {
        enumValues.add(getWeekDay(days[(0..6).random()]))
    }
    enumValues.forEach {
        isWeekEnd(it)
    }
}