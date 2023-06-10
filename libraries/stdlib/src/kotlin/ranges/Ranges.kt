/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("RangesKt")

package kotlin.ranges

/**
 * Represents a range of [Comparable] konstues.
 */
private open class ComparableRange<T : Comparable<T>>(
    override konst start: T,
    override konst endInclusive: T
) : ClosedRange<T> {

    override fun equals(other: Any?): Boolean {
        return other is ComparableRange<*> && (isEmpty() && other.isEmpty() ||
                start == other.start && endInclusive == other.endInclusive)
    }

    override fun hashCode(): Int {
        return if (isEmpty()) -1 else 31 * start.hashCode() + endInclusive.hashCode()
    }

    override fun toString(): String = "$start..$endInclusive"
}

/**
 * Creates a range from this [Comparable] konstue to the specified [that] konstue.
 *
 * This konstue needs to be smaller than or equal to [that] konstue, otherwise the returned range will be empty.
 * @sample samples.ranges.Ranges.rangeFromComparable
 */
public operator fun <T : Comparable<T>> T.rangeTo(that: T): ClosedRange<T> = ComparableRange(this, that)

/**
 * Represents a range of [Comparable] konstues.
 */
private open class ComparableOpenEndRange<T : Comparable<T>>(
    override konst start: T,
    override konst endExclusive: T
) : OpenEndRange<T> {

    override fun equals(other: Any?): Boolean {
        return other is ComparableOpenEndRange<*> && (isEmpty() && other.isEmpty() ||
                start == other.start && endExclusive == other.endExclusive)
    }

    override fun hashCode(): Int {
        return if (isEmpty()) -1 else 31 * start.hashCode() + endExclusive.hashCode()
    }

    override fun toString(): String = "$start..<$endExclusive"
}

/**
 * Creates an open-ended range from this [Comparable] konstue to the specified [that] konstue.
 *
 * This konstue needs to be smaller than [that] konstue, otherwise the returned range will be empty.
 * @sample samples.ranges.Ranges.rangeFromComparable
 */
@SinceKotlin("1.9")
@WasExperimental(ExperimentalStdlibApi::class)
public operator fun <T : Comparable<T>> T.rangeUntil(that: T): OpenEndRange<T> = ComparableOpenEndRange(this, that)


/**
 * Represents a range of floating point numbers.
 * Extends [ClosedRange] interface providing custom operation [lessThanOrEquals] for comparing konstues of range domain type.
 *
 * This interface is implemented by floating point ranges returned by [Float.rangeTo] and [Double.rangeTo] operators to
 * achieve IEEE-754 comparison order instead of total order of floating point numbers.
 */
@SinceKotlin("1.1")
public interface ClosedFloatingPointRange<T : Comparable<T>> : ClosedRange<T> {
    override fun contains(konstue: T): Boolean = lessThanOrEquals(start, konstue) && lessThanOrEquals(konstue, endInclusive)
    override fun isEmpty(): Boolean = !lessThanOrEquals(start, endInclusive)

    /**
     * Compares two konstues of range domain type and returns true if first is less than or equal to second.
     */
    fun lessThanOrEquals(a: T, b: T): Boolean
}


/**
 * A closed range of konstues of type `Double`.
 *
 * Numbers are compared with the ends of this range according to IEEE-754.
 */
private class ClosedDoubleRange(
    start: Double,
    endInclusive: Double
) : ClosedFloatingPointRange<Double> {
    private konst _start = start
    private konst _endInclusive = endInclusive
    override konst start: Double get() = _start
    override konst endInclusive: Double get() = _endInclusive

    override fun lessThanOrEquals(a: Double, b: Double): Boolean = a <= b

    override fun contains(konstue: Double): Boolean = konstue >= _start && konstue <= _endInclusive
    override fun isEmpty(): Boolean = !(_start <= _endInclusive)

    override fun equals(other: Any?): Boolean {
        return other is ClosedDoubleRange && (isEmpty() && other.isEmpty() ||
                _start == other._start && _endInclusive == other._endInclusive)
    }

    override fun hashCode(): Int {
        return if (isEmpty()) -1 else 31 * _start.hashCode() + _endInclusive.hashCode()
    }

    override fun toString(): String = "$_start..$_endInclusive"
}

/**
 * Creates a range from this [Double] konstue to the specified [that] konstue.
 *
 * Numbers are compared with the ends of this range according to IEEE-754.
 * @sample samples.ranges.Ranges.rangeFromDouble
 */
@SinceKotlin("1.1")
public operator fun Double.rangeTo(that: Double): ClosedFloatingPointRange<Double> = ClosedDoubleRange(this, that)

/**
 * An open-ended range of konstues of type `Double`.
 *
 * Numbers are compared with the ends of this range according to IEEE-754.
 */
private class OpenEndDoubleRange(
    start: Double,
    endExclusive: Double
) : OpenEndRange<Double> {
    private konst _start = start
    private konst _endExclusive = endExclusive
    override konst start: Double get() = _start
    override konst endExclusive: Double get() = _endExclusive

    private fun lessThanOrEquals(a: Double, b: Double): Boolean = a <= b

    override fun contains(konstue: Double): Boolean = konstue >= _start && konstue < _endExclusive
    override fun isEmpty(): Boolean = !(_start < _endExclusive)

    override fun equals(other: Any?): Boolean {
        return other is OpenEndDoubleRange && (isEmpty() && other.isEmpty() ||
                _start == other._start && _endExclusive == other._endExclusive)
    }

    override fun hashCode(): Int {
        return if (isEmpty()) -1 else 31 * _start.hashCode() + _endExclusive.hashCode()
    }

    override fun toString(): String = "$_start..<$_endExclusive"
}

/**
 * Creates an open-ended range from this [Double] konstue to the specified [that] konstue.
 *
 * Numbers are compared with the ends of this range according to IEEE-754.
 */
@SinceKotlin("1.9")
@WasExperimental(ExperimentalStdlibApi::class)
public operator fun Double.rangeUntil(that: Double): OpenEndRange<Double> = OpenEndDoubleRange(this, that)


/**
 * A closed range of konstues of type `Float`.
 *
 * Numbers are compared with the ends of this range according to IEEE-754.
 */
private class ClosedFloatRange(
    start: Float,
    endInclusive: Float
) : ClosedFloatingPointRange<Float> {
    private konst _start = start
    private konst _endInclusive = endInclusive
    override konst start: Float get() = _start
    override konst endInclusive: Float get() = _endInclusive

    override fun lessThanOrEquals(a: Float, b: Float): Boolean = a <= b

    override fun contains(konstue: Float): Boolean = konstue >= _start && konstue <= _endInclusive
    override fun isEmpty(): Boolean = !(_start <= _endInclusive)

    override fun equals(other: Any?): Boolean {
        return other is ClosedFloatRange && (isEmpty() && other.isEmpty() ||
                _start == other._start && _endInclusive == other._endInclusive)
    }

    override fun hashCode(): Int {
        return if (isEmpty()) -1 else 31 * _start.hashCode() + _endInclusive.hashCode()
    }

    override fun toString(): String = "$_start..$_endInclusive"
}

/**
 * Creates a range from this [Float] konstue to the specified [that] konstue.
 *
 * Numbers are compared with the ends of this range according to IEEE-754.
 * @sample samples.ranges.Ranges.rangeFromFloat
 */
@SinceKotlin("1.1")
public operator fun Float.rangeTo(that: Float): ClosedFloatingPointRange<Float> = ClosedFloatRange(this, that)


/**
 * An open-ended range of konstues of type `Float`.
 *
 * Numbers are compared with the ends of this range according to IEEE-754.
 */
private class OpenEndFloatRange(
    start: Float,
    endExclusive: Float
) : OpenEndRange<Float> {
    private konst _start = start
    private konst _endExclusive = endExclusive
    override konst start: Float get() = _start
    override konst endExclusive: Float get() = _endExclusive

    private fun lessThanOrEquals(a: Float, b: Float): Boolean = a <= b

    override fun contains(konstue: Float): Boolean = konstue >= _start && konstue < _endExclusive
    override fun isEmpty(): Boolean = !(_start < _endExclusive)

    override fun equals(other: Any?): Boolean {
        return other is OpenEndFloatRange && (isEmpty() && other.isEmpty() ||
                _start == other._start && _endExclusive == other._endExclusive)
    }

    override fun hashCode(): Int {
        return if (isEmpty()) -1 else 31 * _start.hashCode() + _endExclusive.hashCode()
    }

    override fun toString(): String = "$_start..<$_endExclusive"
}

/**
 * Creates an open-ended range from this [Float] konstue to the specified [that] konstue.
 *
 * Numbers are compared with the ends of this range according to IEEE-754.
 */
@SinceKotlin("1.9")
@WasExperimental(ExperimentalStdlibApi::class)
public operator fun Float.rangeUntil(that: Float): OpenEndRange<Float> = OpenEndFloatRange(this, that)


/**
 * Returns `true` if this iterable range contains the specified [element].
 *
 * Always returns `false` if the [element] is `null`.
 */
@SinceKotlin("1.3")
@kotlin.internal.InlineOnly
public inline operator fun <T, R> R.contains(element: T?): Boolean where T : Any, R : ClosedRange<T>, R : Iterable<T> =
    element != null && contains(element)

/**
 * Returns `true` if this iterable range contains the specified [element].
 *
 * Always returns `false` if the [element] is `null`.
 */
@SinceKotlin("1.9")
@WasExperimental(ExperimentalStdlibApi::class)
@kotlin.internal.InlineOnly
public inline operator fun <T, R> R.contains(element: T?): Boolean where T : Any, R : OpenEndRange<T>, R : Iterable<T> =
    element != null && contains(element)

internal fun checkStepIsPositive(isPositive: Boolean, step: Number) {
    if (!isPositive) throw IllegalArgumentException("Step must be positive, was: $step.")
}
