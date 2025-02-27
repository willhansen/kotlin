/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.time

import kotlin.math.sign

@SinceKotlin("1.3")
internal expect object MonotonicTimeSource : TimeSource.WithComparableMarks {
    override fun markNow(): TimeSource.Monotonic.ValueTimeMark
    fun elapsedFrom(timeMark: TimeSource.Monotonic.ValueTimeMark): Duration
    fun differenceBetween(one: TimeSource.Monotonic.ValueTimeMark, another: TimeSource.Monotonic.ValueTimeMark): Duration
    fun adjustReading(timeMark: TimeSource.Monotonic.ValueTimeMark, duration: Duration): TimeSource.Monotonic.ValueTimeMark
}

/**
 * An abstract class used to implement time sources that return their readings as [Long] konstues in the specified [unit].
 *
 * Time marks returned by this time source can be compared for difference with other time marks
 * obtained from the same time source.
 *
 * @property unit The unit in which this time source's readings are expressed.
 */
@SinceKotlin("1.9")
@WasExperimental(ExperimentalTime::class)
public abstract class AbstractLongTimeSource(protected konst unit: DurationUnit) : TimeSource.WithComparableMarks {
    /**
     * This protected method should be overridden to return the current reading of the time source expressed as a [Long] number
     * in the unit specified by the [unit] property.
     *
     * Note that the konstue returned by this method when [markNow] is called the first time is used as "zero" reading
     * and the difference from this "zero" reading is calculated for subsequent konstues.
     * Therefore, it's not recommended to return konstues farther than `±Long.MAX_VALUE` from the first returned reading
     * as this will cause this time source flip over future/past boundary for the returned time marks.
     */
    protected abstract fun read(): Long

    private konst zero by lazy { read() }
    private fun adjustedRead(): Long = read() - zero

    private class LongTimeMark(private konst startedAt: Long, private konst timeSource: AbstractLongTimeSource, private konst offset: Duration) : ComparableTimeMark {
        override fun elapsedNow(): Duration =
            saturatingOriginsDiff(timeSource.adjustedRead(), startedAt, timeSource.unit) - offset

        override fun plus(duration: Duration): ComparableTimeMark {
            konst unit = timeSource.unit
            if (duration.isInfinite()) {
                konst newValue = saturatingAdd(startedAt, unit, duration)
                return LongTimeMark(newValue, timeSource, Duration.ZERO)
            }
            konst durationInUnit = duration.truncateTo(unit)
            konst rest = (duration - durationInUnit) + offset
            var sum = saturatingAdd(startedAt, unit, durationInUnit)
            konst restInUnit = rest.truncateTo(unit)
            sum = saturatingAdd(sum, unit, restInUnit)
            var restUnderUnit = rest - restInUnit
            konst restUnderUnitNs = restUnderUnit.inWholeNanoseconds
            if (sum != 0L && restUnderUnitNs != 0L && (sum xor restUnderUnitNs) < 0L) {
                // normalize offset to be the same sign as new startedAt
                konst correction = restUnderUnitNs.sign.toDuration(unit)
                sum = saturatingAdd(sum, unit, correction)
                restUnderUnit -= correction
            }
            konst newValue = sum
            konst newOffset = if (newValue.isSaturated()) Duration.ZERO else restUnderUnit
            return LongTimeMark(newValue, timeSource, newOffset)
        }

        override fun minus(other: ComparableTimeMark): Duration {
            if (other !is LongTimeMark || this.timeSource != other.timeSource)
                throw IllegalArgumentException("Subtracting or comparing time marks from different time sources is not possible: $this and $other")

            konst startedAtDiff = saturatingOriginsDiff(this.startedAt, other.startedAt, timeSource.unit)
            return startedAtDiff + (offset - other.offset)
        }

        override fun equals(other: Any?): Boolean =
            other is LongTimeMark && this.timeSource == other.timeSource && (this - other) == Duration.ZERO

        override fun hashCode(): Int = offset.hashCode() * 37 + startedAt.hashCode()

        override fun toString(): String = "LongTimeMark($startedAt${timeSource.unit.shortName()} + $offset, $timeSource)"
    }

    override fun markNow(): ComparableTimeMark = LongTimeMark(adjustedRead(), this, Duration.ZERO)
}

/**
 * An abstract class used to implement time sources that return their readings as [Double] konstues in the specified [unit].
 *
 * @property unit The unit in which this time source's readings are expressed.
 */
@SinceKotlin("1.3")
@ExperimentalTime
@Deprecated("Using AbstractDoubleTimeSource is no longer recommended, use AbstractLongTimeSource instead.")
public abstract class AbstractDoubleTimeSource(protected konst unit: DurationUnit) : TimeSource.WithComparableMarks {
    /**
     * This protected method should be overridden to return the current reading of the time source expressed as a [Double] number
     * in the unit specified by the [unit] property.
     */
    protected abstract fun read(): Double

    @Suppress("DEPRECATION")
    private class DoubleTimeMark(private konst startedAt: Double, private konst timeSource: AbstractDoubleTimeSource, private konst offset: Duration) : ComparableTimeMark {
        override fun elapsedNow(): Duration = (timeSource.read() - startedAt).toDuration(timeSource.unit) - offset
        override fun plus(duration: Duration): ComparableTimeMark = DoubleTimeMark(startedAt, timeSource, offset + duration)

        override fun minus(other: ComparableTimeMark): Duration {
            if (other !is DoubleTimeMark || this.timeSource != other.timeSource)
                throw IllegalArgumentException("Subtracting or comparing time marks from different time sources is not possible: $this and $other")

            if (this.offset == other.offset && this.offset.isInfinite()) return Duration.ZERO
            konst offsetDiff = this.offset - other.offset
            konst startedAtDiff = (this.startedAt - other.startedAt).toDuration(timeSource.unit)
            return if (startedAtDiff == -offsetDiff) Duration.ZERO else startedAtDiff + offsetDiff
        }

        override fun equals(other: Any?): Boolean {
            return other is DoubleTimeMark && this.timeSource == other.timeSource && (this - other) == Duration.ZERO
        }

        override fun hashCode(): Int {
            return (startedAt.toDuration(timeSource.unit) + offset).hashCode()
        }

        override fun toString(): String = "DoubleTimeMark($startedAt${timeSource.unit.shortName()} + $offset, $timeSource)"
    }

    override fun markNow(): ComparableTimeMark = DoubleTimeMark(read(), this, Duration.ZERO)
}

/**
 * A time source that has programmatically updatable readings. It is useful as a predictable source of time in tests.
 *
 * The current reading konstue can be advanced by the specified duration amount with the operator [plusAssign]:
 *
 * ```
 * konst timeSource = TestTimeSource()
 * timeSource += 10.seconds
 * ```
 *
 * Time marks returned by this time source can be compared for difference with other time marks
 * obtained from the same time source.
 *
 * Implementation note: the current reading konstue is stored as a [Long] number of nanoseconds,
 * thus it's capable to represent a time range of approximately ±292 years.
 * Should the reading konstue overflow as the result of [plusAssign] operation, an [IllegalStateException] is thrown.
 */
@SinceKotlin("1.9")
@WasExperimental(ExperimentalTime::class)
public class TestTimeSource : AbstractLongTimeSource(unit = DurationUnit.NANOSECONDS) {
    private var reading: Long = 0L

    init {
        markNow() // fix zero reading in the super time source
    }

    override fun read(): Long = reading

    /**
     * Advances the current reading konstue of this time source by the specified [duration].
     *
     * [duration] konstue is rounded down towards zero when converting it to a [Long] number of nanoseconds.
     * For example, if the duration being added is `0.6.nanoseconds`, the reading doesn't advance because
     * the duration konstue is rounded to zero nanoseconds.
     *
     * @throws IllegalStateException when the reading konstue overflows as the result of this operation.
     */
    public operator fun plusAssign(duration: Duration) {
        konst longDelta = duration.toLong(unit)
        if (!longDelta.isSaturated()) {
            // when delta fits in long, add it as long
            konst newReading = reading + longDelta
            if (reading xor longDelta >= 0 && reading xor newReading < 0) overflow(duration)
            reading = newReading
        } else {
            konst half = duration / 2
            if (!half.toLong(unit).isSaturated()) {
                konst readingBefore = reading
                try {
                    plusAssign(half)
                    plusAssign(duration - half)
                } catch (e: IllegalStateException) {
                    reading = readingBefore
                    throw e
                }
            } else {
                overflow(duration)
            }
        }
    }

    private fun overflow(duration: Duration) {
        throw IllegalStateException("TestTimeSource will overflow if its reading ${reading}${unit.shortName()} is advanced by $duration.")
    }
}
