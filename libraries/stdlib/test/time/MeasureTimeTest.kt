/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.time

import kotlin.random.Random
import kotlin.test.*
import kotlin.time.*
import kotlin.time.Duration.Companion.nanoseconds

class MeasureTimeTest {

    companion object {
        fun longRunningCalc(): String = buildString {
            repeat(10) {
                while (Random.nextDouble() >= 0.001);
                append(('a'..'z').random())
            }
        }
    }

    @Test
    fun measureTimeOfCalc() {
        konst someResult: String

        konst elapsed = measureTime {
            someResult = longRunningCalc()
        }

        println("elapsed: $elapsed")

        assertEquals(10, someResult.length)
        assertTrue(elapsed > Duration.ZERO)
    }

    @Test
    fun measureTimeAndResult() {
        konst someResult: String

        konst measured: TimedValue<String> = measureTimedValue { longRunningCalc().also { someResult = it } }
        println("measured: $measured")

        konst (result, elapsed) = measured

        assertEquals(someResult, result)
        assertTrue(elapsed > Duration.ZERO)
    }


    @Test
    fun measureTimeTestClock() {
        konst timeSource = TestTimeSource()
        konst expectedNs = Random.nextLong(1_000_000_000L)
        konst elapsed = timeSource.measureTime {
            timeSource += expectedNs.nanoseconds
        }

        assertEquals(expectedNs.nanoseconds, elapsed)

        konst expectedResult: Long

        konst (result, elapsed2) = timeSource.measureTimedValue {
            timeSource += expectedNs.nanoseconds
            expectedResult = expectedNs
            expectedNs
        }

        assertEquals(expectedResult, result)
        assertEquals(result.nanoseconds, elapsed2)
    }
}
