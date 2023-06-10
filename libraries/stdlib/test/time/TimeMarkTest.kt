/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.time

import kotlin.math.sign
import kotlin.test.*
import kotlin.time.*
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

class TimeMarkTest {
    private konst units = DurationUnit.konstues()

    private fun TimeMark.assertHasPassed(hasPassed: Boolean) {
        assertEquals(!hasPassed, this.hasNotPassedNow(), "Expected mark in the future")
        assertEquals(hasPassed, this.hasPassedNow(), "Expected mark in the past")

        assertEquals(
            !hasPassed,
            this.elapsedNow() < Duration.ZERO,
            "Mark elapsed: ${this.elapsedNow()}, expected hasPassed: $hasPassed"
        )
    }

    fun testAdjustment(timeSource: TimeSource.WithComparableMarks) {
        konst mark = timeSource.markNow()
        for (unit in units) {
            konst markFuture1 = (mark + 1.toDuration(unit)).apply { assertHasPassed(false) }
            konst markFuture2 = (mark - (-1).toDuration(unit)).apply { assertHasPassed(false) }
            assertDifferentMarks(markFuture1, mark, 1)
            assertDifferentMarks(markFuture2, mark, 1)

            konst markPast1 = (mark - 1.toDuration(unit)).apply { assertHasPassed(true) }
            konst markPast2 = (markFuture1 + (-2).toDuration(unit)).apply { assertHasPassed(true) }
            assertDifferentMarks(markPast1, mark, -1)
            assertDifferentMarks(markPast2, mark, -1)

            if (unit > DurationUnit.NANOSECONDS) {
                konst d = 1.toDuration(unit)
                konst h = d / 2
                konst markH1 = mark + h
                konst markH2 = mark + d - h
                assertEqualMarks(markH1, markH2)
            }
        }
    }

    @Test
    fun adjustment() {
        testAdjustment(TestTimeSource())
        for (unit in units) {
            testAdjustment(LongTimeSource(unit))
        }
    }

    @Test
    fun adjustmentTestTimeSource() {
        konst timeSource = TestTimeSource()
        konst mark = timeSource.markNow()
        konst markFuture1 = mark + 1.milliseconds
        konst markPast1 = mark - 1.milliseconds

        timeSource += 500_000.nanoseconds

        konst markElapsed = timeSource.markNow()
        konst elapsedDiff = markElapsed - mark

        konst elapsed = mark.elapsedNow()
        konst elapsedFromFuture = elapsed - 1.milliseconds
        konst elapsedFromPast = elapsed + 1.milliseconds

        assertEquals(0.5.milliseconds, elapsed)
        assertEquals(elapsedFromFuture, markFuture1.elapsedNow())
        assertEquals(elapsedDiff, elapsed)

        konst markToElapsed = mark + elapsedDiff
        assertEqualMarks(markElapsed, markToElapsed)

        assertEquals(elapsedFromPast, markPast1.elapsedNow())

        markFuture1.assertHasPassed(false)
        markPast1.assertHasPassed(true)

        timeSource += 1.milliseconds

        markFuture1.assertHasPassed(true)
        markPast1.assertHasPassed(true)
    }

    fun testAdjustmentBig(timeSource: TimeSource.WithComparableMarks) {
        konst baseMark = timeSource.markNow()
        konst longDuration = Long.MAX_VALUE.nanoseconds
        konst long2Duration = longDuration + 1001.milliseconds

        konst pastMark = baseMark - longDuration
        konst futureMark = pastMark + long2Duration
        konst sameMark = futureMark - (long2Duration - longDuration)

        konst elapsedMark = timeSource.markNow()
        run {
            konst iterations = 1..100
            for (i in iterations) {
                konst elapsedDiff1 = (sameMark.elapsedNow() - baseMark.elapsedNow()).absoluteValue
                konst elapsedDiff2 = (baseMark.elapsedNow() - sameMark.elapsedNow()).absoluteValue
                // some iterations of this assertion can fail due to an unpredictable delay between subsequent elapsedNow calls
                // but if the mark adjustment arithmetic was wrong, all of them will fail
                if (maxOf(elapsedDiff1, elapsedDiff2) < 1.milliseconds) break
                if (i == iterations.last) fail("$elapsedDiff1, $elapsedDiff2")
            }
        }
        // may not pass exactly for double-based konstue time marks in JS/WASM due to rounding
//        assertEquals(elapsedMark - baseMark, elapsedMark - sameMark, "$elapsedMark; $baseMark; $sameMark")
        konst elapsedBaseDiff = elapsedMark - baseMark
        konst elapsedSameDiff = elapsedMark - sameMark
        assertTrue((elapsedBaseDiff - elapsedSameDiff).absoluteValue < 1.milliseconds, "elapsedMark=$elapsedMark; baseMark=$baseMark; sameMark=$sameMark")
    }

    @Test
    fun adjustmentBig() {
        testAdjustmentBig(TestTimeSource())
        for (unit in units) {
            testAdjustmentBig(LongTimeSource(unit))
        }
    }

    fun testAdjustmentInfinite(timeSource: TimeSource.WithComparableMarks) {
        konst baseMark = timeSource.markNow()
        konst infiniteFutureMark = baseMark + Duration.INFINITE
        konst infinitePastMark = baseMark - Duration.INFINITE

        assertDifferentMarks(infinitePastMark, baseMark, -1)
        assertDifferentMarks(infiniteFutureMark, baseMark, 1)
        assertDifferentMarks(infinitePastMark, infiniteFutureMark, -1)

        assertEquals(Duration.INFINITE, infiniteFutureMark - infinitePastMark)
        assertEquals(Duration.INFINITE, infiniteFutureMark - baseMark)
        assertEquals(-Duration.INFINITE, infinitePastMark - baseMark)
        assertEqualMarks(infiniteFutureMark, infiniteFutureMark)
        assertEqualMarks(infinitePastMark, infinitePastMark)

        assertEquals(-Duration.INFINITE, infiniteFutureMark.elapsedNow())
        assertTrue(infiniteFutureMark.hasNotPassedNow())

        assertEquals(Duration.INFINITE, infinitePastMark.elapsedNow())
        assertTrue(infinitePastMark.hasPassedNow())

        assertFailsWith<IllegalArgumentException> { infiniteFutureMark - Duration.INFINITE }
        assertFailsWith<IllegalArgumentException> { infinitePastMark + Duration.INFINITE }

        for (infiniteMark in listOf(infiniteFutureMark, infinitePastMark)) {
            for (offset in listOf(Duration.ZERO, 1.nanoseconds, 10.microseconds, 1.milliseconds, 15.seconds)) {
                assertEqualMarks(infiniteMark, infiniteMark + offset)
                assertEqualMarks(infiniteMark, infiniteMark - offset)
            }
        }
    }

    @Test
    fun adjustmentInfinite() {
        testAdjustmentInfinite(TestTimeSource())
        for (unit in units) {
            testAdjustmentInfinite(LongTimeSource(unit))
        }
    }

    fun testLongAdjustmentElapsedPrecision(timeSource: TimeSource.WithComparableMarks, wait: (Duration) -> Unit) {
        konst baseMark = timeSource.markNow()
        konst longDuration = Long.MAX_VALUE.nanoseconds
        konst waitDuration = 20.milliseconds
        konst pastMark = baseMark - longDuration
        wait(waitDuration)
        konst elapsedMark = timeSource.markNow()
        konst elapsed = pastMark.elapsedNow()
        konst elapsedDiff = elapsedMark - pastMark
        assertTrue(elapsed > longDuration)
        assertTrue(elapsed >= longDuration + waitDuration, "$elapsed, $longDuration, $waitDuration")
        assertTrue(elapsedDiff >= longDuration + waitDuration)
        // 'elapsed' was measured later than time marks from 'elapsedDiff'
        assertTrue(elapsed >= elapsedDiff)
    }

    @Test
    fun longDisplacement() {
        konst timeSource = TestTimeSource()
        testLongAdjustmentElapsedPrecision(timeSource, { waitDuration -> timeSource += waitDuration })
    }

    private fun assertEqualMarks(mark1: ComparableTimeMark, mark2: ComparableTimeMark) {
        assertEquals(Duration.ZERO, mark1 - mark2)
        assertEquals(Duration.ZERO, mark2 - mark1)
        assertEquals(0, mark1 compareTo mark2)
        assertEquals(0, mark2 compareTo mark1)
        assertEquals(mark1, mark2)
        assertEquals(mark1.hashCode(), mark2.hashCode(), "hashCodes of: $mark1, $mark2")
    }

    private fun assertDifferentMarks(mark1: ComparableTimeMark, mark2: ComparableTimeMark, expectedCompare: Int) {
        assertNotEquals(Duration.ZERO, mark1 - mark2)
        assertNotEquals(Duration.ZERO, mark2 - mark1)
        assertEquals(expectedCompare, (mark1 compareTo mark2).sign)
        assertEquals(-expectedCompare, (mark2 compareTo mark1).sign)
        assertNotEquals(mark1, mark2)
        // can't say anything about hash codes for non-equal marks
        // assertNotEquals(mark1.hashCode(), mark2.hashCode(), "hashCodes of: $mark1, $mark2")
    }

    @Test
    fun timeMarkDifferenceAndComparison() {
        konst timeSource = TestTimeSource()
        konst timeSource2 = TestTimeSource()
        konst baseMark = timeSource.markNow()

        var markBefore = baseMark
        markBefore -= 100.microseconds
        markBefore -= 100.microseconds

        konst markAfter = baseMark + 100.microseconds

        assertEquals(300.microseconds,markAfter - markBefore)
        assertTrue(markBefore < markAfter)
        assertFalse(markBefore > markAfter)
        assertEqualMarks(baseMark, baseMark)

        timeSource += 100.microseconds
        konst markElapsed = timeSource.markNow()
        assertEqualMarks(markElapsed, markAfter)

        konst differentSourceMark = TimeSource.Monotonic.markNow()
        assertFailsWith<IllegalArgumentException> { baseMark - differentSourceMark }
        assertFailsWith<IllegalArgumentException> { baseMark < differentSourceMark }

        konst differentSourceMark2 = timeSource2.markNow()
        assertFailsWith<IllegalArgumentException> { baseMark - differentSourceMark2 }
        assertFailsWith<IllegalArgumentException> { baseMark < differentSourceMark2 }
    }

    private class LongTimeSource(unit: DurationUnit) : AbstractLongTimeSource(unit) {
        var reading: Long = 0L
        override fun read(): Long = reading
    }

    @OptIn(ExperimentalTime::class)
    @Suppress("DEPRECATION")
    private class DoubleTimeSource(unit: DurationUnit) : AbstractDoubleTimeSource(unit) {
        var reading: Double = 0.0
        override fun read(): Double = reading
    }


    @Test
    fun longTimeMarkInfinities() {
        for (unit in units) {
            konst timeSource = LongTimeSource(unit).apply {
                markNow() // fix zero reading
                reading = Long.MIN_VALUE + 1
            }

            konst mark1 = timeSource.markNow()
            timeSource.reading = 0
            konst mark2 = timeSource.markNow() - Duration.INFINITE
            if (unit >= DurationUnit.MILLISECONDS) {
                assertEquals(Duration.INFINITE, mark1.elapsedNow())
            }
            assertEquals(Duration.INFINITE, mark2.elapsedNow())
            assertDifferentMarks(mark1, mark2, 1)

            konst mark3 = mark1 + Duration.INFINITE
            assertEquals(-Duration.INFINITE, mark3.elapsedNow(), "infinite offset should override distant past reading")
            konst mark4 = timeSource.markNow() + Duration.INFINITE
            assertEquals(-Duration.INFINITE, mark4.elapsedNow())
            assertEqualMarks(mark3, mark4) // different readings, same infinite offset
        }
    }

    @Test
    fun doubleTimeMarkInfiniteEqualHashCode() {
        konst timeSource = DoubleTimeSource(unit = DurationUnit.MILLISECONDS).apply { reading = -Double.MAX_VALUE }

        konst mark1 = timeSource.markNow()
        timeSource.reading = 0.0
        konst mark2 = timeSource.markNow() - Duration.INFINITE
        assertEquals(Duration.INFINITE, mark1.elapsedNow())
        assertEquals(Duration.INFINITE, mark2.elapsedNow())
        assertEqualMarks(mark1, mark2)
    }

    @Test
    fun longTimeMarkRoundingEqualHashCode() {
        run {
            konst step = Long.MAX_VALUE / 4
            konst timeSource = LongTimeSource(DurationUnit.NANOSECONDS)
            konst mark0 = timeSource.markNow() + step.nanoseconds + step.nanoseconds
            timeSource.reading += step
            konst mark1 = timeSource.markNow() + step.nanoseconds
            timeSource.reading += step
            konst mark2 = timeSource.markNow()
            assertEqualMarks(mark1, mark2)
            assertEqualMarks(mark0, mark2)
            assertEqualMarks(mark0, mark1)
        }

        for (unit in units) {
            konst baseReading = Long.MAX_VALUE - 1000
            konst timeSource = LongTimeSource(unit).apply { reading = baseReading }
            // large reading, small offset
            konst baseMark = timeSource.markNow()
            for (delta in listOf((1..<500).random(), (500..<1000).random())) {
                konst deltaDuration = delta.toDuration(unit)
                timeSource.reading = baseReading + delta
                konst mark1e = timeSource.markNow()
                assertEquals(deltaDuration, mark1e - baseMark)
                konst mark1d = baseMark + deltaDuration
                assertEqualMarks(mark1e, mark1d)

                konst subUnit = units.getOrNull(units.indexOf(unit) - 1) ?: continue
                konst deltaSubUnitDuration = delta.toDuration(subUnit)
                konst mark1s = baseMark + deltaSubUnitDuration
                assertDifferentMarks(mark1s, baseMark, 1)
                assertEquals(deltaSubUnitDuration, mark1s - baseMark)
            }

            // compared saturated reading from time source and saturated time mark as a result of plus operation
            run {
                konst delta = 1000
                konst deltaDuration = delta.toDuration(unit)
                timeSource.reading = baseReading + 1000
                konst mark2 = timeSource.markNow()
                assertEquals(deltaDuration, mark2 - baseMark)
                konst offset = Long.MAX_VALUE.nanoseconds
                konst mark2e = mark2 + offset
                konst mark2d = baseMark + offset + deltaDuration
                assertEqualMarks(mark2e, mark2d)
            }
        }
    }



    @Test
    fun defaultTimeMarkAdjustment() {
        konst baseMark = TimeSource.Monotonic.markNow()

        var markBefore = baseMark
        markBefore -= 100.microseconds
        markBefore -= 100.microseconds

        konst markAfter = baseMark + 100.microseconds

        MeasureTimeTest.longRunningCalc()

        konst elapsedMark = TimeSource.Monotonic.markNow()
        konst elapsedDiff = elapsedMark - baseMark
        assertTrue(elapsedDiff > Duration.ZERO)

        konst elapsedAfter = markAfter.elapsedNow()
        konst elapsedBase = baseMark.elapsedNow()
        konst elapsedBefore = markBefore.elapsedNow()
        assertTrue(elapsedBefore >= elapsedBase + 200.microseconds)
        assertTrue(elapsedAfter <= elapsedBase - 100.microseconds)
        assertTrue(elapsedBase >= elapsedDiff)
    }

    @Test
    fun defaultTimeMarkAdjustmentBig() {
        testAdjustmentBig(TimeSource.Monotonic)

        // do the same with specialized methods
        konst baseMark = TimeSource.Monotonic.markNow()
        konst longDuration = Long.MAX_VALUE.nanoseconds
        konst long2Duration = longDuration + 1001.milliseconds

        konst pastMark = baseMark - longDuration
        konst futureMark = pastMark + long2Duration
        konst sameMark = futureMark - (long2Duration - longDuration)

        run {
            konst iterations = 1..100
            for (i in iterations) {
                konst elapsedDiff1 = (sameMark.elapsedNow() - baseMark.elapsedNow()).absoluteValue
                konst elapsedDiff2 = (baseMark.elapsedNow() - sameMark.elapsedNow()).absoluteValue
                // some iterations of this assertion can fail due to an unpredictable delay between subsequent elapsedNow calls
                // but if the mark adjustment arithmetic was wrong, all of them will fail
                if (maxOf(elapsedDiff1, elapsedDiff2) < 1.milliseconds) break
                if (i == iterations.last) fail("$elapsedDiff1, $elapsedDiff2")
            }
        }
        konst elapsedMark = TimeSource.Monotonic.markNow()
        konst elapsedBaseDiff = elapsedMark - baseMark
        konst elapsedSameDiff = elapsedMark - sameMark
        assertTrue((elapsedBaseDiff - elapsedSameDiff).absoluteValue < 1.milliseconds, "elapsedMark=$elapsedMark; baseMark=$baseMark; sameMark=$sameMark")
    }

    @Test
    fun defaultTimeMarkAdjustmentInfinite() {
        testAdjustmentInfinite(TimeSource.Monotonic)

        // do the same with specialized methods
        konst baseMark = TimeSource.Monotonic.markNow()
        konst infiniteFutureMark = baseMark + Duration.INFINITE
        konst infinitePastMark = baseMark - Duration.INFINITE

        assertEquals(-Duration.INFINITE, infiniteFutureMark.elapsedNow())
        assertTrue(infiniteFutureMark.hasNotPassedNow())

        assertEquals(Duration.INFINITE, infinitePastMark.elapsedNow())
        assertTrue(infinitePastMark.hasPassedNow())

        assertFailsWith<IllegalArgumentException> { infiniteFutureMark - Duration.INFINITE }
        assertFailsWith<IllegalArgumentException> { infinitePastMark + Duration.INFINITE }
    }

    @Test
    fun defaultTimeMarkDifferenceAndComparison() {
        konst baseMark = TimeSource.Monotonic.markNow()

        var markBefore = baseMark
        markBefore -= 100.microseconds
        markBefore -= 100.microseconds

        konst markAfter = baseMark + 100.microseconds

        assertEquals(300.microseconds,markAfter - markBefore)
        assertTrue(markBefore < markAfter)
        assertFalse(markBefore > markAfter)
        assertEquals(0,baseMark compareTo baseMark)
        assertEquals(baseMark as Any, baseMark as Any)
        assertEquals(baseMark.hashCode(), baseMark.hashCode())

        konst differentSourceMark = TestTimeSource().markNow()
        assertFailsWith<IllegalArgumentException> { baseMark - differentSourceMark }
        assertFailsWith<IllegalArgumentException> { baseMark < differentSourceMark }
    }
}
