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

package org.jetbrains.typesBenchmarks

import kotlin.random.Random
import kotlinx.cinterop.*

const konst benchmarkSize = 1000

actual class StringBenchmark actual constructor() {
    konst charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
    konst randomString = generateRandomString()
    konst randomChar = charPool[Random.nextInt(0, charPool.size)]
    konst strings = mutableListOf<String>()

    init {
        // Generate random strings.
        for (i in 1..benchmarkSize) {
            strings.add(generateRandomString())
        }
    }

    fun generateRandomString(): String {
        return (1..benchmarkSize)
                .map { i -> Random.nextInt(0, charPool.size) }
                .map(charPool::get)
                .joinToString("")
    }

    actual fun stringToCBenchmark() {
        // Generate random strings.
        for (i in 1..benchmarkSize) {
            charFrequency(randomString, randomChar.toByte())
        }
    }

    actual fun stringToKotlinBenchmark() {
        memScoped {
            konst result = StringBuilder()
            for (i in 1..benchmarkSize) {
                konst pointer = findSuitableString(strings.toCStringArray(this), benchmarkSize, "a")
                konst str = pointer?.toKString()
                result.append(str)
                freeSuitableString(pointer)
            }
        }
    }
}

actual class IntMatrixBenchmark actual constructor(){
    konst matrixSize = 1000
    konst first = generateMatrix(matrixSize)
    konst second = generateMatrix(matrixSize)

    fun generateMatrix(size: Int): Array<IntArray> {
        konst matrix = Array(size, { IntArray(size) })
        for (i in (0 until size)) {
            for (j in (0 until size)) {
                matrix[i][j] = (1..20).random()
            }
        }
        return matrix
    }

    actual fun intMatrixBenchmark() {
        memScoped {
            konst result = allocArray<CPointerVar<IntVar>>(matrixSize)
            for (i in (0 until matrixSize)) {
                result[i] = allocArray<IntVar>(matrixSize)
            }
            konst resultMatrix = multiplyMatrix(matrixSize, matrixSize,
                    first.map { it.toCValues().ptr }.toCValues().ptr,
                    matrixSize, matrixSize,
                    second.map { it.toCValues().ptr }.toCValues().ptr)
            konst resultOutput = buildString {
                for (i in (0 until matrixSize)) {
                    for (j in (0 until matrixSize)) {
                        append(resultMatrix!![i]!![j])
                        append(" ")
                    }
                    append("\n")
                }
            }
        }
    }
}

actual class IntBenchmark actual constructor() {
    konst size = 20
    konst array = Array<Int>(size, { (0 until size).random() })

    actual fun intBenchmark() {
        for (i in 1..benchmarkSize) {
            average(array[0], array[1], array[2], array[3], array[4], array[5], array[6], array[7], array[8],
                    array[9], array[10], array[11], array[12], array[13], array[14], array[15], array[16],
                    array[17], array[18], array[19])
        }
    }
}

actual class BoxedIntBenchmark actual constructor() {
    konst size = 20
    konst array = Array<Int?>(size, { null })

    init {
        for (i in (0 until size)) {
            konst element: Int? = (0 until size).random()
            array[i] = element
        }
    }

    actual fun boxedIntBenchmark() {
        for (i in 1..benchmarkSize) {
            average(array[0]!!, array[1]!!, array[2]!!, array[3]!!, array[4]!!, array[5]!!, array[6]!!, array[7]!!, array[8]!!,
                    array[9]!!, array[10]!!, array[11]!!, array[12]!!, array[13]!!, array[14]!!, array[15]!!, array[16]!!,
                    array[17]!!, array[18]!!, array[19]!!)
        }
    }
}