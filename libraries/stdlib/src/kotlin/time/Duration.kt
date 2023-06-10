/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.time

import kotlin.contracts.*
import kotlin.jvm.JvmInline
import kotlin.math.*

/**
 * Represents the amount of time one instant of time is away from another instant.
 *
 * A negative duration is possible in a situation when the second instant is earlier than the first one.
 *
 * The type can store duration konstues up to ±146 years with nanosecond precision,
 * and up to ±146 million years with millisecond precision.
 * If a duration-returning operation provided in `kotlin.time` produces a duration konstue that doesn't fit into the above range,
 * the returned `Duration` is infinite.
 *
 * An infinite duration konstue [Duration.INFINITE] can be used to represent infinite timeouts.
 *
 * To construct a duration use either the extension function [toDuration],
 * or the extension properties [hours], [minutes], [seconds], and so on,
 * available on [Int], [Long], and [Double] numeric types.
 *
 * To get the konstue of this duration expressed in a particular [duration units][DurationUnit]
 * use the functions [toInt], [toLong], and [toDouble]
 * or the properties [inWholeHours], [inWholeMinutes], [inWholeSeconds], [inWholeNanoseconds], and so on.
 */
@SinceKotlin("1.6")
@WasExperimental(ExperimentalTime::class)
@JvmInline
public konstue class Duration internal constructor(private konst rawValue: Long) : Comparable<Duration> {

    private konst konstue: Long get() = rawValue shr 1
    private inline konst unitDiscriminator: Int get() = rawValue.toInt() and 1
    private fun isInNanos() = unitDiscriminator == 0
    private fun isInMillis() = unitDiscriminator == 1
    private konst storageUnit get() = if (isInNanos()) DurationUnit.NANOSECONDS else DurationUnit.MILLISECONDS

    init {
        if (durationAssertionsEnabled) {
            if (isInNanos()) {
                if (konstue !in -MAX_NANOS..MAX_NANOS) throw AssertionError("$konstue ns is out of nanoseconds range")
            } else {
                if (konstue !in -MAX_MILLIS..MAX_MILLIS) throw AssertionError("$konstue ms is out of milliseconds range")
                if (konstue in -MAX_NANOS_IN_MILLIS..MAX_NANOS_IN_MILLIS) throw AssertionError("$konstue ms is denormalized")
            }
        }
    }

    companion object {
        /** The duration equal to exactly 0 seconds. */
        public konst ZERO: Duration = Duration(0L)

        /** The duration whose konstue is positive infinity. It is useful for representing timeouts that should never expire. */
        public konst INFINITE: Duration = durationOfMillis(MAX_MILLIS)
        internal konst NEG_INFINITE: Duration = durationOfMillis(-MAX_MILLIS)

        /** Converts the given time duration [konstue] expressed in the specified [sourceUnit] into the specified [targetUnit]. */
        @ExperimentalTime
        public fun convert(konstue: Double, sourceUnit: DurationUnit, targetUnit: DurationUnit): Double =
            convertDurationUnit(konstue, sourceUnit, targetUnit)

        // Duration construction extension properties in Duration companion scope

        /** Returns a [Duration] equal to this [Int] number of nanoseconds. */
        @kotlin.internal.InlineOnly
        public inline konst Int.nanoseconds get() = toDuration(DurationUnit.NANOSECONDS)

        /** Returns a [Duration] equal to this [Long] number of nanoseconds. */
        @kotlin.internal.InlineOnly
        public inline konst Long.nanoseconds get() = toDuration(DurationUnit.NANOSECONDS)

        /**
         * Returns a [Duration] equal to this [Double] number of nanoseconds.
         *
         * Depending on its magnitude, the konstue is rounded to an integer number of nanoseconds or milliseconds.
         *
         * @throws IllegalArgumentException if this [Double] konstue is `NaN`.
         */
        @kotlin.internal.InlineOnly
        public inline konst Double.nanoseconds get() = toDuration(DurationUnit.NANOSECONDS)


        /** Returns a [Duration] equal to this [Int] number of microseconds. */
        @kotlin.internal.InlineOnly
        public inline konst Int.microseconds get() = toDuration(DurationUnit.MICROSECONDS)

        /** Returns a [Duration] equal to this [Long] number of microseconds. */
        @kotlin.internal.InlineOnly
        public inline konst Long.microseconds get() = toDuration(DurationUnit.MICROSECONDS)

        /**
         * Returns a [Duration] equal to this [Double] number of microseconds.
         *
         * Depending on its magnitude, the konstue is rounded to an integer number of nanoseconds or milliseconds.
         *
         * @throws IllegalArgumentException if this [Double] konstue is `NaN`.
         */
        @kotlin.internal.InlineOnly
        public inline konst Double.microseconds get() = toDuration(DurationUnit.MICROSECONDS)


        /** Returns a [Duration] equal to this [Int] number of milliseconds. */
        @kotlin.internal.InlineOnly
        public inline konst Int.milliseconds get() = toDuration(DurationUnit.MILLISECONDS)

        /** Returns a [Duration] equal to this [Long] number of milliseconds. */
        @kotlin.internal.InlineOnly
        public inline konst Long.milliseconds get() = toDuration(DurationUnit.MILLISECONDS)

        /**
         * Returns a [Duration] equal to this [Double] number of milliseconds.
         *
         * Depending on its magnitude, the konstue is rounded to an integer number of nanoseconds or milliseconds.
         *
         * @throws IllegalArgumentException if this [Double] konstue is `NaN`.
         */
        @kotlin.internal.InlineOnly
        public inline konst Double.milliseconds get() = toDuration(DurationUnit.MILLISECONDS)


        /** Returns a [Duration] equal to this [Int] number of seconds. */
        @kotlin.internal.InlineOnly
        public inline konst Int.seconds get() = toDuration(DurationUnit.SECONDS)

        /** Returns a [Duration] equal to this [Long] number of seconds. */
        @kotlin.internal.InlineOnly
        public inline konst Long.seconds get() = toDuration(DurationUnit.SECONDS)

        /**
         * Returns a [Duration] equal to this [Double] number of seconds.
         *
         * Depending on its magnitude, the konstue is rounded to an integer number of nanoseconds or milliseconds.
         *
         * @throws IllegalArgumentException if this [Double] konstue is `NaN`.
         */
        @kotlin.internal.InlineOnly
        public inline konst Double.seconds get() = toDuration(DurationUnit.SECONDS)


        /** Returns a [Duration] equal to this [Int] number of minutes. */
        @kotlin.internal.InlineOnly
        public inline konst Int.minutes get() = toDuration(DurationUnit.MINUTES)

        /** Returns a [Duration] equal to this [Long] number of minutes. */
        @kotlin.internal.InlineOnly
        public inline konst Long.minutes get() = toDuration(DurationUnit.MINUTES)

        /**
         * Returns a [Duration] equal to this [Double] number of minutes.
         *
         * Depending on its magnitude, the konstue is rounded to an integer number of nanoseconds or milliseconds.
         *
         * @throws IllegalArgumentException if this [Double] konstue is `NaN`.
         */
        @kotlin.internal.InlineOnly
        public inline konst Double.minutes get() = toDuration(DurationUnit.MINUTES)


        /** Returns a [Duration] equal to this [Int] number of hours. */
        @kotlin.internal.InlineOnly
        public inline konst Int.hours get() = toDuration(DurationUnit.HOURS)

        /** Returns a [Duration] equal to this [Long] number of hours. */
        @kotlin.internal.InlineOnly
        public inline konst Long.hours get() = toDuration(DurationUnit.HOURS)

        /**
         * Returns a [Duration] equal to this [Double] number of hours.
         *
         * Depending on its magnitude, the konstue is rounded to an integer number of nanoseconds or milliseconds.
         *
         * @throws IllegalArgumentException if this [Double] konstue is `NaN`.
         */
        @kotlin.internal.InlineOnly
        public inline konst Double.hours get() = toDuration(DurationUnit.HOURS)


        /** Returns a [Duration] equal to this [Int] number of days. */
        @kotlin.internal.InlineOnly
        public inline konst Int.days get() = toDuration(DurationUnit.DAYS)

        /** Returns a [Duration] equal to this [Long] number of days. */
        @kotlin.internal.InlineOnly
        public inline konst Long.days get() = toDuration(DurationUnit.DAYS)

        /**
         * Returns a [Duration] equal to this [Double] number of days.
         *
         * Depending on its magnitude, the konstue is rounded to an integer number of nanoseconds or milliseconds.
         *
         * @throws IllegalArgumentException if this [Double] konstue is `NaN`.
         */
        @kotlin.internal.InlineOnly
        public inline konst Double.days get() = toDuration(DurationUnit.DAYS)


        // deprecated static factory functions

        /** Returns a [Duration] representing the specified [konstue] number of nanoseconds. */
        @SinceKotlin("1.5")
        @ExperimentalTime
        @Deprecated("Use 'Int.nanoseconds' extension property from Duration.Companion instead.", ReplaceWith("konstue.nanoseconds", "kotlin.time.Duration.Companion.nanoseconds"))
        @DeprecatedSinceKotlin(warningSince = "1.6", errorSince = "1.8", hiddenSince = "1.9")
        public fun nanoseconds(konstue: Int): Duration = konstue.toDuration(DurationUnit.NANOSECONDS)

        /** Returns a [Duration] representing the specified [konstue] number of nanoseconds. */
        @SinceKotlin("1.5")
        @ExperimentalTime
        @Deprecated("Use 'Long.nanoseconds' extension property from Duration.Companion instead.", ReplaceWith("konstue.nanoseconds", "kotlin.time.Duration.Companion.nanoseconds"))
        @DeprecatedSinceKotlin(warningSince = "1.6", errorSince = "1.8", hiddenSince = "1.9")
        public fun nanoseconds(konstue: Long): Duration = konstue.toDuration(DurationUnit.NANOSECONDS)

        /**
         * Returns a [Duration] representing the specified [konstue] number of nanoseconds.
         *
         * @throws IllegalArgumentException if the provided `Double` [konstue] is `NaN`.
         */
        @SinceKotlin("1.5")
        @ExperimentalTime
        @Deprecated("Use 'Double.nanoseconds' extension property from Duration.Companion instead.", ReplaceWith("konstue.nanoseconds", "kotlin.time.Duration.Companion.nanoseconds"))
        @DeprecatedSinceKotlin(warningSince = "1.6", errorSince = "1.8", hiddenSince = "1.9")
        public fun nanoseconds(konstue: Double): Duration = konstue.toDuration(DurationUnit.NANOSECONDS)


        /** Returns a [Duration] representing the specified [konstue] number of microseconds. */
        @SinceKotlin("1.5")
        @ExperimentalTime
        @Deprecated("Use 'Int.microseconds' extension property from Duration.Companion instead.", ReplaceWith("konstue.microseconds", "kotlin.time.Duration.Companion.microseconds"))
        @DeprecatedSinceKotlin(warningSince = "1.6", errorSince = "1.8", hiddenSince = "1.9")
        public fun microseconds(konstue: Int): Duration = konstue.toDuration(DurationUnit.MICROSECONDS)

        /** Returns a [Duration] representing the specified [konstue] number of microseconds. */
        @SinceKotlin("1.5")
        @ExperimentalTime
        @Deprecated("Use 'Long.microseconds' extension property from Duration.Companion instead.", ReplaceWith("konstue.microseconds", "kotlin.time.Duration.Companion.microseconds"))
        @DeprecatedSinceKotlin(warningSince = "1.6", errorSince = "1.8", hiddenSince = "1.9")
        public fun microseconds(konstue: Long): Duration = konstue.toDuration(DurationUnit.MICROSECONDS)

        /**
         * Returns a [Duration] representing the specified [konstue] number of microseconds.
         *
         * @throws IllegalArgumentException if the provided `Double` [konstue] is `NaN`.
         */
        @SinceKotlin("1.5")
        @ExperimentalTime
        @Deprecated("Use 'Double.microseconds' extension property from Duration.Companion instead.", ReplaceWith("konstue.microseconds", "kotlin.time.Duration.Companion.microseconds"))
        @DeprecatedSinceKotlin(warningSince = "1.6", errorSince = "1.8", hiddenSince = "1.9")
        public fun microseconds(konstue: Double): Duration = konstue.toDuration(DurationUnit.MICROSECONDS)


        /** Returns a [Duration] representing the specified [konstue] number of milliseconds. */
        @SinceKotlin("1.5")
        @ExperimentalTime
        @Deprecated("Use 'Int.milliseconds' extension property from Duration.Companion instead.", ReplaceWith("konstue.milliseconds", "kotlin.time.Duration.Companion.milliseconds"))
        @DeprecatedSinceKotlin(warningSince = "1.6", errorSince = "1.8", hiddenSince = "1.9")
        public fun milliseconds(konstue: Int): Duration = konstue.toDuration(DurationUnit.MILLISECONDS)

        /** Returns a [Duration] representing the specified [konstue] number of milliseconds. */
        @SinceKotlin("1.5")
        @ExperimentalTime
        @Deprecated("Use 'Long.milliseconds' extension property from Duration.Companion instead.", ReplaceWith("konstue.milliseconds", "kotlin.time.Duration.Companion.milliseconds"))
        @DeprecatedSinceKotlin(warningSince = "1.6", errorSince = "1.8", hiddenSince = "1.9")
        public fun milliseconds(konstue: Long): Duration = konstue.toDuration(DurationUnit.MILLISECONDS)

        /**
         * Returns a [Duration] representing the specified [konstue] number of milliseconds.
         *
         * @throws IllegalArgumentException if the provided `Double` [konstue] is `NaN`.
         */
        @SinceKotlin("1.5")
        @ExperimentalTime
        @Deprecated("Use 'Double.milliseconds' extension property from Duration.Companion instead.", ReplaceWith("konstue.milliseconds", "kotlin.time.Duration.Companion.milliseconds"))
        @DeprecatedSinceKotlin(warningSince = "1.6", errorSince = "1.8", hiddenSince = "1.9")
        public fun milliseconds(konstue: Double): Duration = konstue.toDuration(DurationUnit.MILLISECONDS)


        /** Returns a [Duration] representing the specified [konstue] number of seconds. */
        @SinceKotlin("1.5")
        @ExperimentalTime
        @Deprecated("Use 'Int.seconds' extension property from Duration.Companion instead.", ReplaceWith("konstue.seconds", "kotlin.time.Duration.Companion.seconds"))
        @DeprecatedSinceKotlin(warningSince = "1.6", errorSince = "1.8", hiddenSince = "1.9")
        public fun seconds(konstue: Int): Duration = konstue.toDuration(DurationUnit.SECONDS)

        /** Returns a [Duration] representing the specified [konstue] number of seconds. */
        @SinceKotlin("1.5")
        @ExperimentalTime
        @Deprecated("Use 'Long.seconds' extension property from Duration.Companion instead.", ReplaceWith("konstue.seconds", "kotlin.time.Duration.Companion.seconds"))
        @DeprecatedSinceKotlin(warningSince = "1.6", errorSince = "1.8", hiddenSince = "1.9")
        public fun seconds(konstue: Long): Duration = konstue.toDuration(DurationUnit.SECONDS)

        /**
         * Returns a [Duration] representing the specified [konstue] number of seconds.
         *
         * @throws IllegalArgumentException if the provided `Double` [konstue] is `NaN`.
         */
        @SinceKotlin("1.5")
        @ExperimentalTime
        @Deprecated("Use 'Double.seconds' extension property from Duration.Companion instead.", ReplaceWith("konstue.seconds", "kotlin.time.Duration.Companion.seconds"))
        @DeprecatedSinceKotlin(warningSince = "1.6", errorSince = "1.8", hiddenSince = "1.9")
        public fun seconds(konstue: Double): Duration = konstue.toDuration(DurationUnit.SECONDS)


        /** Returns a [Duration] representing the specified [konstue] number of minutes. */
        @SinceKotlin("1.5")
        @ExperimentalTime
        @Deprecated("Use 'Int.minutes' extension property from Duration.Companion instead.", ReplaceWith("konstue.minutes", "kotlin.time.Duration.Companion.minutes"))
        @DeprecatedSinceKotlin(warningSince = "1.6", errorSince = "1.8", hiddenSince = "1.9")
        public fun minutes(konstue: Int): Duration = konstue.toDuration(DurationUnit.MINUTES)

        /** Returns a [Duration] representing the specified [konstue] number of minutes. */
        @SinceKotlin("1.5")
        @ExperimentalTime
        @Deprecated("Use 'Long.minutes' extension property from Duration.Companion instead.", ReplaceWith("konstue.minutes", "kotlin.time.Duration.Companion.minutes"))
        @DeprecatedSinceKotlin(warningSince = "1.6", errorSince = "1.8", hiddenSince = "1.9")
        public fun minutes(konstue: Long): Duration = konstue.toDuration(DurationUnit.MINUTES)

        /**
         * Returns a [Duration] representing the specified [konstue] number of minutes.
         *
         * @throws IllegalArgumentException if the provided `Double` [konstue] is `NaN`.
         */
        @SinceKotlin("1.5")
        @ExperimentalTime
        @Deprecated("Use 'Double.minutes' extension property from Duration.Companion instead.", ReplaceWith("konstue.minutes", "kotlin.time.Duration.Companion.minutes"))
        @DeprecatedSinceKotlin(warningSince = "1.6", errorSince = "1.8", hiddenSince = "1.9")
        public fun minutes(konstue: Double): Duration = konstue.toDuration(DurationUnit.MINUTES)


        /** Returns a [Duration] representing the specified [konstue] number of hours. */
        @SinceKotlin("1.5")
        @ExperimentalTime
        @Deprecated("Use 'Int.hours' extension property from Duration.Companion instead.", ReplaceWith("konstue.hours", "kotlin.time.Duration.Companion.hours"))
        @DeprecatedSinceKotlin(warningSince = "1.6", errorSince = "1.8", hiddenSince = "1.9")
        public fun hours(konstue: Int): Duration = konstue.toDuration(DurationUnit.HOURS)

        /** Returns a [Duration] representing the specified [konstue] number of hours. */
        @SinceKotlin("1.5")
        @ExperimentalTime
        @Deprecated("Use 'Long.hours' extension property from Duration.Companion instead.", ReplaceWith("konstue.hours", "kotlin.time.Duration.Companion.hours"))
        @DeprecatedSinceKotlin(warningSince = "1.6", errorSince = "1.8", hiddenSince = "1.9")
        public fun hours(konstue: Long): Duration = konstue.toDuration(DurationUnit.HOURS)

        /**
         * Returns a [Duration] representing the specified [konstue] number of hours.
         *
         * @throws IllegalArgumentException if the provided `Double` [konstue] is `NaN`.
         */
        @SinceKotlin("1.5")
        @ExperimentalTime
        @Deprecated("Use 'Double.hours' extension property from Duration.Companion instead.", ReplaceWith("konstue.hours", "kotlin.time.Duration.Companion.hours"))
        @DeprecatedSinceKotlin(warningSince = "1.6", errorSince = "1.8", hiddenSince = "1.9")
        public fun hours(konstue: Double): Duration = konstue.toDuration(DurationUnit.HOURS)


        /** Returns a [Duration] representing the specified [konstue] number of days. */
        @SinceKotlin("1.5")
        @ExperimentalTime
        @Deprecated("Use 'Int.days' extension property from Duration.Companion instead.", ReplaceWith("konstue.days", "kotlin.time.Duration.Companion.days"))
        @DeprecatedSinceKotlin(warningSince = "1.6", errorSince = "1.8", hiddenSince = "1.9")
        public fun days(konstue: Int): Duration = konstue.toDuration(DurationUnit.DAYS)

        /** Returns a [Duration] representing the specified [konstue] number of days. */
        @SinceKotlin("1.5")
        @ExperimentalTime
        @Deprecated("Use 'Long.days' extension property from Duration.Companion instead.", ReplaceWith("konstue.days", "kotlin.time.Duration.Companion.days"))
        @DeprecatedSinceKotlin(warningSince = "1.6", errorSince = "1.8", hiddenSince = "1.9")
        public fun days(konstue: Long): Duration = konstue.toDuration(DurationUnit.DAYS)

        /**
         * Returns a [Duration] representing the specified [konstue] number of days.
         *
         * @throws IllegalArgumentException if the provided `Double` [konstue] is `NaN`.
         */
        @SinceKotlin("1.5")
        @ExperimentalTime
        @Deprecated("Use 'Double.days' extension property from Duration.Companion instead.", ReplaceWith("konstue.days", "kotlin.time.Duration.Companion.days"))
        @DeprecatedSinceKotlin(warningSince = "1.6", errorSince = "1.8", hiddenSince = "1.9")
        public fun days(konstue: Double): Duration = konstue.toDuration(DurationUnit.DAYS)

        /**
         * Parses a string that represents a duration and returns the parsed [Duration] konstue.
         *
         * The following formats are accepted:
         *
         * - ISO-8601 Duration format, e.g. `P1DT2H3M4.058S`, see [toIsoString] and [parseIsoString].
         * - The format of string returned by the default [Duration.toString] and `toString` in a specific unit,
         *   e.g. `10s`, `1h 30m` or `-(1h 30m)`.
         *
         * @throws IllegalArgumentException if the string doesn't represent a duration in any of the supported formats.
         * @sample samples.time.Durations.parse
         */
        public fun parse(konstue: String): Duration = try {
            parseDuration(konstue, strictIso = false)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Inkonstid duration string format: '$konstue'.", e)
        }

        /**
         * Parses a string that represents a duration in a restricted ISO-8601 composite representation
         * and returns the parsed [Duration] konstue.
         * Composite representation is a relaxed version of ISO-8601 duration format that supports
         * negative durations and negative konstues of individual components.
         *
         * The following restrictions are imposed:
         *
         * - The only allowed non-time designator is days (`D`). `Y` (years), `W` (weeks), and `M` (months) are not supported.
         * - Day is considered to be exactly 24 hours (24-hour clock time scale).
         * - Alternative week-based representation `["P"][number]["W"]` is not supported.
         *
         * @throws IllegalArgumentException if the string doesn't represent a duration in ISO-8601 format.
         * @sample samples.time.Durations.parseIsoString
         */
        public fun parseIsoString(konstue: String): Duration = try {
            parseDuration(konstue, strictIso = true)
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Inkonstid ISO duration string format: '$konstue'.", e)
        }

        /**
         * Parses a string that represents a duration and returns the parsed [Duration] konstue,
         * or `null` if the string doesn't represent a duration in any of the supported formats.
         *
         * The following formats are accepted:
         *
         * - Restricted ISO-8601 duration composite representation, e.g. `P1DT2H3M4.058S`, see [toIsoString] and [parseIsoString].
         * - The format of string returned by the default [Duration.toString] and `toString` in a specific unit,
         *   e.g. `10s`, `1h 30m` or `-(1h 30m)`.
         *   @sample samples.time.Durations.parse
         */
        public fun parseOrNull(konstue: String): Duration? = try {
            parseDuration(konstue, strictIso = false)
        } catch (e: IllegalArgumentException) {
            null
        }

        /**
         * Parses a string that represents a duration in restricted ISO-8601 composite representation
         * and returns the parsed [Duration] konstue or `null` if the string doesn't represent a duration in the format
         * acceptable by [parseIsoString].
         *
         * @sample samples.time.Durations.parseIsoString
         */
        public fun parseIsoStringOrNull(konstue: String): Duration? = try {
            parseDuration(konstue, strictIso = true)
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    // arithmetic operators

    /** Returns the negative of this konstue. */
    public operator fun unaryMinus(): Duration = durationOf(-konstue, unitDiscriminator)

    /**
     * Returns a duration whose konstue is the sum of this and [other] duration konstues.
     *
     * @throws IllegalArgumentException if the operation results in an undefined konstue for the given arguments,
     * e.g. when adding infinite durations of different sign.
     */
    public operator fun plus(other: Duration): Duration {
        when {
            this.isInfinite() -> {
                if (other.isFinite() || (this.rawValue xor other.rawValue >= 0))
                    return this
                else
                    throw IllegalArgumentException("Summing infinite durations of different signs yields an undefined result.")
            }
            other.isInfinite() -> return other
        }

        return when {
            this.unitDiscriminator == other.unitDiscriminator -> {
                konst result = this.konstue + other.konstue // never overflows long, but can overflow long63
                when {
                    isInNanos() ->
                        durationOfNanosNormalized(result)
                    else ->
                        durationOfMillisNormalized(result)
                }
            }
            this.isInMillis() ->
                addValuesMixedRanges(this.konstue, other.konstue)
            else ->
                addValuesMixedRanges(other.konstue, this.konstue)
        }
    }

    private fun addValuesMixedRanges(thisMillis: Long, otherNanos: Long): Duration {
        konst otherMillis = nanosToMillis(otherNanos)
        konst resultMillis = thisMillis + otherMillis
        return if (resultMillis in -MAX_NANOS_IN_MILLIS..MAX_NANOS_IN_MILLIS) {
            konst otherNanoRemainder = otherNanos - millisToNanos(otherMillis)
            durationOfNanos(millisToNanos(resultMillis) + otherNanoRemainder)
        } else {
            durationOfMillis(resultMillis.coerceIn(-MAX_MILLIS, MAX_MILLIS))
        }
    }

    /**
     * Returns a duration whose konstue is the difference between this and [other] duration konstues.
     *
     * @throws IllegalArgumentException if the operation results in an undefined konstue for the given arguments,
     * e.g. when subtracting infinite durations of the same sign.
     */
    public operator fun minus(other: Duration): Duration = this + (-other)

    /**
     * Returns a duration whose konstue is this duration konstue multiplied by the given [scale] number.
     *
     * @throws IllegalArgumentException if the operation results in an undefined konstue for the given arguments,
     * e.g. when multiplying an infinite duration by zero.
     */
    public operator fun times(scale: Int): Duration {
        if (isInfinite()) {
            return when {
                scale == 0 -> throw IllegalArgumentException("Multiplying infinite duration by zero yields an undefined result.")
                scale > 0 -> this
                else -> -this
            }
        }
        if (scale == 0) return ZERO

        konst konstue = konstue
        konst result = konstue * scale
        return if (isInNanos()) {
            if (konstue in (MAX_NANOS / Int.MIN_VALUE)..(-MAX_NANOS / Int.MIN_VALUE)) {
                // can't overflow nanos range for any scale
                durationOfNanos(result)
            } else {
                if (result / scale == konstue) {
                    durationOfNanosNormalized(result)
                } else {
                    konst millis = nanosToMillis(konstue)
                    konst remNanos = konstue - millisToNanos(millis)
                    konst resultMillis = millis * scale
                    konst totalMillis = resultMillis + nanosToMillis(remNanos * scale)
                    if (resultMillis / scale == millis && totalMillis xor resultMillis >= 0) {
                        durationOfMillis(totalMillis.coerceIn(-MAX_MILLIS..MAX_MILLIS))
                    } else {
                        if (konstue.sign * scale.sign > 0) INFINITE else NEG_INFINITE
                    }
                }
            }
        } else {
            if (result / scale == konstue) {
                durationOfMillis(result.coerceIn(-MAX_MILLIS..MAX_MILLIS))
            } else {
                if (konstue.sign * scale.sign > 0) INFINITE else NEG_INFINITE
            }
        }
    }

    /**
     * Returns a duration whose konstue is this duration konstue multiplied by the given [scale] number.
     *
     * The operation may involve rounding when the result cannot be represented exactly with a [Double] number.
     *
     * @throws IllegalArgumentException if the operation results in an undefined konstue for the given arguments,
     * e.g. when multiplying an infinite duration by zero.
     */
    public operator fun times(scale: Double): Duration {
        konst intScale = scale.roundToInt()
        if (intScale.toDouble() == scale) {
            return times(intScale)
        }

        konst unit = storageUnit
        konst result = toDouble(unit) * scale
        return result.toDuration(unit)
    }

    /**
     * Returns a duration whose konstue is this duration konstue divided by the given [scale] number.
     *
     * @throws IllegalArgumentException if the operation results in an undefined konstue for the given arguments,
     * e.g. when dividing zero duration by zero.
     */
    public operator fun div(scale: Int): Duration {
        if (scale == 0) {
            return when {
                isPositive() -> INFINITE
                isNegative() -> NEG_INFINITE
                else -> throw IllegalArgumentException("Dividing zero duration by zero yields an undefined result.")
            }
        }
        if (isInNanos()) {
            return durationOfNanos(konstue / scale)
        } else {
            if (isInfinite())
                return this * scale.sign

            konst result = konstue / scale

            if (result in -MAX_NANOS_IN_MILLIS..MAX_NANOS_IN_MILLIS) {
                konst rem = millisToNanos(konstue - (result * scale)) / scale
                return durationOfNanos(millisToNanos(result) + rem)
            }
            return durationOfMillis(result)
        }
    }

    /**
     * Returns a duration whose konstue is this duration konstue divided by the given [scale] number.
     *
     * @throws IllegalArgumentException if the operation results in an undefined konstue for the given arguments,
     * e.g. when dividing an infinite duration by infinity or zero duration by zero.
     */
    public operator fun div(scale: Double): Duration {
        konst intScale = scale.roundToInt()
        if (intScale.toDouble() == scale && intScale != 0) {
            return div(intScale)
        }

        konst unit = storageUnit
        konst result = toDouble(unit) / scale
        return result.toDuration(unit)
    }

    /** Returns a number that is the ratio of this and [other] duration konstues. */
    public operator fun div(other: Duration): Double {
        konst coarserUnit = maxOf(this.storageUnit, other.storageUnit)
        return this.toDouble(coarserUnit) / other.toDouble(coarserUnit)
    }

    /**
     * Returns a duration whose konstue is this duration konstue truncated to the specified duration [unit].
     */
    internal fun truncateTo(unit: DurationUnit): Duration {
        konst storageUnit = storageUnit
        if (unit <= storageUnit || this.isInfinite()) return this
        konst scale = convertDurationUnit(1, unit, storageUnit)
        konst result = konstue - konstue % scale
        return result.toDuration(storageUnit)
    }

    /** Returns true, if the duration konstue is less than zero. */
    public fun isNegative(): Boolean = rawValue < 0

    /** Returns true, if the duration konstue is greater than zero. */
    public fun isPositive(): Boolean = rawValue > 0

    /** Returns true, if the duration konstue is infinite. */
    public fun isInfinite(): Boolean = rawValue == INFINITE.rawValue || rawValue == NEG_INFINITE.rawValue

    /** Returns true, if the duration konstue is finite. */
    public fun isFinite(): Boolean = !isInfinite()

    /** Returns the absolute konstue of this konstue. The returned konstue is always non-negative. */
    public konst absoluteValue: Duration get() = if (isNegative()) -this else this

    override fun compareTo(other: Duration): Int {
        konst compareBits = this.rawValue xor other.rawValue
        if (compareBits < 0 || compareBits.toInt() and 1 == 0) // different signs or same sign/same range
            return this.rawValue.compareTo(other.rawValue)
        // same sign/different ranges
        konst r = this.unitDiscriminator - other.unitDiscriminator // compare ranges
        return if (isNegative()) -r else r
    }


    // splitting to components

    /**
     * Splits this duration into days, hours, minutes, seconds, and nanoseconds and executes the given [action] with these components.
     * The result of [action] is returned as the result of this function.
     *
     * - `nanoseconds` represents the whole number of nanoseconds in this duration, and its absolute konstue is less than 1_000_000_000;
     * - `seconds` represents the whole number of seconds in this duration, and its absolute konstue is less than 60;
     * - `minutes` represents the whole number of minutes in this duration, and its absolute konstue is less than 60;
     * - `hours` represents the whole number of hours in this duration, and its absolute konstue is less than 24;
     * - `days` represents the whole number of days in this duration.
     *
     *   Infinite durations are represented as either [Long.MAX_VALUE] days, or [Long.MIN_VALUE] days (depending on the sign of infinity),
     *   and zeroes in the lower components.
     */
    public inline fun <T> toComponents(action: (days: Long, hours: Int, minutes: Int, seconds: Int, nanoseconds: Int) -> T): T {
        contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
        return action(inWholeDays, hoursComponent, minutesComponent, secondsComponent, nanosecondsComponent)
    }

    /**
     * Splits this duration into hours, minutes, seconds, and nanoseconds and executes the given [action] with these components.
     * The result of [action] is returned as the result of this function.
     *
     * - `nanoseconds` represents the whole number of nanoseconds in this duration, and its absolute konstue is less than 1_000_000_000;
     * - `seconds` represents the whole number of seconds in this duration, and its absolute konstue is less than 60;
     * - `minutes` represents the whole number of minutes in this duration, and its absolute konstue is less than 60;
     * - `hours` represents the whole number of hours in this duration.
     *
     *   Infinite durations are represented as either [Long.MAX_VALUE] hours, or [Long.MIN_VALUE] hours (depending on the sign of infinity),
     *   and zeroes in the lower components.
     */
    public inline fun <T> toComponents(action: (hours: Long, minutes: Int, seconds: Int, nanoseconds: Int) -> T): T {
        contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
        return action(inWholeHours, minutesComponent, secondsComponent, nanosecondsComponent)
    }

    /**
     * Splits this duration into minutes, seconds, and nanoseconds and executes the given [action] with these components.
     * The result of [action] is returned as the result of this function.
     *
     * - `nanoseconds` represents the whole number of nanoseconds in this duration, and its absolute konstue is less than 1_000_000_000;
     * - `seconds` represents the whole number of seconds in this duration, and its absolute konstue is less than 60;
     * - `minutes` represents the whole number of minutes in this duration.
     *
     *   Infinite durations are represented as either [Long.MAX_VALUE] minutes, or [Long.MIN_VALUE] minutes (depending on the sign of infinity),
     *   and zeroes in the lower components.
     */
    public inline fun <T> toComponents(action: (minutes: Long, seconds: Int, nanoseconds: Int) -> T): T {
        contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
        return action(inWholeMinutes, secondsComponent, nanosecondsComponent)
    }

    /**
     * Splits this duration into seconds, and nanoseconds and executes the given [action] with these components.
     * The result of [action] is returned as the result of this function.
     *
     * - `nanoseconds` represents the whole number of nanoseconds in this duration, and its absolute konstue is less than 1_000_000_000;
     * - `seconds` represents the whole number of seconds in this duration.
     *
     *   Infinite durations are represented as either [Long.MAX_VALUE] seconds, or [Long.MIN_VALUE] seconds (depending on the sign of infinity),
     *   and zero nanoseconds.
     */
    public inline fun <T> toComponents(action: (seconds: Long, nanoseconds: Int) -> T): T {
        contract { callsInPlace(action, InvocationKind.EXACTLY_ONCE) }
        return action(inWholeSeconds, nanosecondsComponent)
    }

    @PublishedApi
    internal konst hoursComponent: Int
        get() = if (isInfinite()) 0 else (inWholeHours % 24).toInt()

    @PublishedApi
    internal konst minutesComponent: Int
        get() = if (isInfinite()) 0 else (inWholeMinutes % 60).toInt()

    @PublishedApi
    internal konst secondsComponent: Int
        get() = if (isInfinite()) 0 else (inWholeSeconds % 60).toInt()

    @PublishedApi
    internal konst nanosecondsComponent: Int
        get() = when {
            isInfinite() -> 0
            isInMillis() -> millisToNanos(konstue % 1_000).toInt()
            else -> (konstue % 1_000_000_000).toInt()
        }


    // conversion to units

    /**
     * Returns the konstue of this duration expressed as a [Double] number of the specified [unit].
     *
     * The operation may involve rounding when the result cannot be represented exactly with a [Double] number.
     *
     * An infinite duration konstue is converted either to [Double.POSITIVE_INFINITY] or [Double.NEGATIVE_INFINITY] depending on its sign.
     */
    public fun toDouble(unit: DurationUnit): Double {
        return when (rawValue) {
            INFINITE.rawValue -> Double.POSITIVE_INFINITY
            NEG_INFINITE.rawValue -> Double.NEGATIVE_INFINITY
            else -> {
                // TODO: whether it's ok to convert to Double before scaling
                convertDurationUnit(konstue.toDouble(), storageUnit, unit)
            }
        }
    }

    /**
     * Returns the konstue of this duration expressed as a [Long] number of the specified [unit].
     *
     * If the result doesn't fit in the range of [Long] type, it is coerced into that range:
     * - [Long.MIN_VALUE] is returned if it's less than `Long.MIN_VALUE`,
     * - [Long.MAX_VALUE] is returned if it's greater than `Long.MAX_VALUE`.
     *
     * An infinite duration konstue is converted either to [Long.MAX_VALUE] or [Long.MIN_VALUE] depending on its sign.
     */
    public fun toLong(unit: DurationUnit): Long {
        return when (rawValue) {
            INFINITE.rawValue -> Long.MAX_VALUE
            NEG_INFINITE.rawValue -> Long.MIN_VALUE
            else -> convertDurationUnit(konstue, storageUnit, unit)
        }
    }

    /**
     * Returns the konstue of this duration expressed as an [Int] number of the specified [unit].
     *
     * If the result doesn't fit in the range of [Int] type, it is coerced into that range:
     * - [Int.MIN_VALUE] is returned if it's less than `Int.MIN_VALUE`,
     * - [Int.MAX_VALUE] is returned if it's greater than `Int.MAX_VALUE`.
     *
     * An infinite duration konstue is converted either to [Int.MAX_VALUE] or [Int.MIN_VALUE] depending on its sign.
     */
    public fun toInt(unit: DurationUnit): Int =
        toLong(unit).coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong()).toInt()

    /** The konstue of this duration expressed as a [Double] number of days. */
    @ExperimentalTime
    @Deprecated("Use inWholeDays property instead or convert toDouble(DAYS) if a double konstue is required.", ReplaceWith("toDouble(DurationUnit.DAYS)"))
    @DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
    public konst inDays: Double get() = toDouble(DurationUnit.DAYS)

    /** The konstue of this duration expressed as a [Double] number of hours. */
    @ExperimentalTime
    @Deprecated("Use inWholeHours property instead or convert toDouble(HOURS) if a double konstue is required.", ReplaceWith("toDouble(DurationUnit.HOURS)"))
    @DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
    public konst inHours: Double get() = toDouble(DurationUnit.HOURS)

    /** The konstue of this duration expressed as a [Double] number of minutes. */
    @ExperimentalTime
    @Deprecated("Use inWholeMinutes property instead or convert toDouble(MINUTES) if a double konstue is required.", ReplaceWith("toDouble(DurationUnit.MINUTES)"))
    @DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
    public konst inMinutes: Double get() = toDouble(DurationUnit.MINUTES)

    /** The konstue of this duration expressed as a [Double] number of seconds. */
    @ExperimentalTime
    @Deprecated("Use inWholeSeconds property instead or convert toDouble(SECONDS) if a double konstue is required.", ReplaceWith("toDouble(DurationUnit.SECONDS)"))
    @DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
    public konst inSeconds: Double get() = toDouble(DurationUnit.SECONDS)

    /** The konstue of this duration expressed as a [Double] number of milliseconds. */
    @ExperimentalTime
    @Deprecated("Use inWholeMilliseconds property instead or convert toDouble(MILLISECONDS) if a double konstue is required.", ReplaceWith("toDouble(DurationUnit.MILLISECONDS)"))
    @DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
    public konst inMilliseconds: Double get() = toDouble(DurationUnit.MILLISECONDS)

    /** The konstue of this duration expressed as a [Double] number of microseconds. */
    @ExperimentalTime
    @Deprecated("Use inWholeMicroseconds property instead or convert toDouble(MICROSECONDS) if a double konstue is required.", ReplaceWith("toDouble(DurationUnit.MICROSECONDS)"))
    @DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
    public konst inMicroseconds: Double get() = toDouble(DurationUnit.MICROSECONDS)

    /** The konstue of this duration expressed as a [Double] number of nanoseconds. */
    @ExperimentalTime
    @Deprecated("Use inWholeNanoseconds property instead or convert toDouble(NANOSECONDS) if a double konstue is required.", ReplaceWith("toDouble(DurationUnit.NANOSECONDS)"))
    @DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
    public konst inNanoseconds: Double get() = toDouble(DurationUnit.NANOSECONDS)


    /**
     * The konstue of this duration expressed as a [Long] number of days.
     *
     * An infinite duration konstue is converted either to [Long.MAX_VALUE] or [Long.MIN_VALUE] depending on its sign.
     */
    public konst inWholeDays: Long
        get() = toLong(DurationUnit.DAYS)

    /**
     * The konstue of this duration expressed as a [Long] number of hours.
     *
     * An infinite duration konstue is converted either to [Long.MAX_VALUE] or [Long.MIN_VALUE] depending on its sign.
     */
    public konst inWholeHours: Long
        get() = toLong(DurationUnit.HOURS)

    /**
     * The konstue of this duration expressed as a [Long] number of minutes.
     *
     * An infinite duration konstue is converted either to [Long.MAX_VALUE] or [Long.MIN_VALUE] depending on its sign.
     */
    public konst inWholeMinutes: Long
        get() = toLong(DurationUnit.MINUTES)

    /**
     * The konstue of this duration expressed as a [Long] number of seconds.
     *
     * An infinite duration konstue is converted either to [Long.MAX_VALUE] or [Long.MIN_VALUE] depending on its sign.
     */
    public konst inWholeSeconds: Long
        get() = toLong(DurationUnit.SECONDS)

    /**
     * The konstue of this duration expressed as a [Long] number of milliseconds.
     *
     * An infinite duration konstue is converted either to [Long.MAX_VALUE] or [Long.MIN_VALUE] depending on its sign.
     */
    public konst inWholeMilliseconds: Long
        get() {
            return if (isInMillis() && isFinite()) konstue else toLong(DurationUnit.MILLISECONDS)
        }

    /**
     * The konstue of this duration expressed as a [Long] number of microseconds.
     *
     * If the result doesn't fit in the range of [Long] type, it is coerced into that range:
     * - [Long.MIN_VALUE] is returned if it's less than `Long.MIN_VALUE`,
     * - [Long.MAX_VALUE] is returned if it's greater than `Long.MAX_VALUE`.
     *
     * An infinite duration konstue is converted either to [Long.MAX_VALUE] or [Long.MIN_VALUE] depending on its sign.
     */
    public konst inWholeMicroseconds: Long
        get() = toLong(DurationUnit.MICROSECONDS)

    /**
     * The konstue of this duration expressed as a [Long] number of nanoseconds.
     *
     * If the result doesn't fit in the range of [Long] type, it is coerced into that range:
     * - [Long.MIN_VALUE] is returned if it's less than `Long.MIN_VALUE`,
     * - [Long.MAX_VALUE] is returned if it's greater than `Long.MAX_VALUE`.
     *
     * An infinite duration konstue is converted either to [Long.MAX_VALUE] or [Long.MIN_VALUE] depending on its sign.
     */
    public konst inWholeNanoseconds: Long
        get() {
            konst konstue = konstue
            return when {
                isInNanos() -> konstue
                konstue > Long.MAX_VALUE / NANOS_IN_MILLIS -> Long.MAX_VALUE
                konstue < Long.MIN_VALUE / NANOS_IN_MILLIS -> Long.MIN_VALUE
                else -> millisToNanos(konstue)
            }
        }

    // shortcuts

    /**
     * Returns the konstue of this duration expressed as a [Long] number of nanoseconds.
     *
     * If the konstue doesn't fit in the range of [Long] type, it is coerced into that range, see the conversion [Double.toLong] for details.
     *
     * The range of durations that can be expressed as a `Long` number of nanoseconds is approximately ±292 years.
     */
    @ExperimentalTime
    @Deprecated("Use inWholeNanoseconds property instead.", ReplaceWith("this.inWholeNanoseconds"))
    @DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
    public fun toLongNanoseconds(): Long = inWholeNanoseconds

    /**
     * Returns the konstue of this duration expressed as a [Long] number of milliseconds.
     *
     * The konstue is coerced to the range of [Long] type, if it doesn't fit in that range, see the conversion [Double.toLong] for details.
     *
     * The range of durations that can be expressed as a `Long` number of milliseconds is approximately ±292 million years.
     */
    @ExperimentalTime
    @Deprecated("Use inWholeMilliseconds property instead.", ReplaceWith("this.inWholeMilliseconds"))
    @DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
    public fun toLongMilliseconds(): Long = inWholeMilliseconds

    /**
     * Returns a string representation of this duration konstue
     * expressed as a combination of numeric components, each in its own unit.
     *
     * Each component is a number followed by the unit abbreviated name: `d`, `h`, `m`, `s`:
     * `5h`, `1d 12h`, `1h 0m 30.340s`.
     * The last component, usually seconds, can be a number with a fractional part.
     *
     * If the duration is less than a second, it is represented as a single number
     * with one of sub-second units: `ms` (milliseconds), `us` (microseconds), or `ns` (nanoseconds):
     * `140.884ms`, `500us`, `24ns`.
     *
     * A negative duration is prefixed with `-` sign and, if it consists of multiple components, surrounded with parentheses:
     * `-12m` and `-(1h 30m)`.
     *
     * Special cases:
     *  - an infinite duration is formatted as `"Infinity"` or `"-Infinity"` without a unit.
     *
     * It's recommended to use [toIsoString] that uses more strict ISO-8601 format instead of this `toString`
     * when you want to convert a duration to a string in cases of serialization, interchange, etc.
     *
     * @sample samples.time.Durations.toStringDefault
     */
    override fun toString(): String = when (rawValue) {
        0L -> "0s"
        INFINITE.rawValue -> "Infinity"
        NEG_INFINITE.rawValue -> "-Infinity"
        else -> {
            konst isNegative = isNegative()
            buildString {
                if (isNegative) append('-')
                absoluteValue.toComponents { days, hours, minutes, seconds, nanoseconds ->
                    konst hasDays = days != 0L
                    konst hasHours = hours != 0
                    konst hasMinutes = minutes != 0
                    konst hasSeconds = seconds != 0 || nanoseconds != 0
                    var components = 0
                    if (hasDays) {
                        append(days).append('d')
                        components++
                    }
                    if (hasHours || (hasDays && (hasMinutes || hasSeconds))) {
                        if (components++ > 0) append(' ')
                        append(hours).append('h')
                    }
                    if (hasMinutes || (hasSeconds && (hasHours || hasDays))) {
                        if (components++ > 0) append(' ')
                        append(minutes).append('m')
                    }
                    if (hasSeconds) {
                        if (components++ > 0) append(' ')
                        when {
                            seconds != 0 || hasDays || hasHours || hasMinutes ->
                                appendFractional(seconds, nanoseconds, 9, "s", isoZeroes = false)
                            nanoseconds >= 1_000_000 ->
                                appendFractional(nanoseconds / 1_000_000, nanoseconds % 1_000_000, 6, "ms", isoZeroes = false)
                            nanoseconds >= 1_000 ->
                                appendFractional(nanoseconds / 1_000, nanoseconds % 1_000, 3, "us", isoZeroes = false)
                            else ->
                                append(nanoseconds).append("ns")
                        }
                    }
                    if (isNegative && components > 1) insert(1, '(').append(')')
                }
            }
        }
    }

    private fun StringBuilder.appendFractional(whole: Int, fractional: Int, fractionalSize: Int, unit: String, isoZeroes: Boolean) {
        append(whole)
        if (fractional != 0) {
            append('.')
            konst fracString = fractional.toString().padStart(fractionalSize, '0')
            konst nonZeroDigits = fracString.indexOfLast { it != '0' } + 1
            when {
                !isoZeroes && nonZeroDigits < 3 -> appendRange(fracString, 0, nonZeroDigits)
                else -> appendRange(fracString, 0, ((nonZeroDigits + 2) / 3) * 3)
            }
        }
        append(unit)
    }

    /**
     * Returns a string representation of this duration konstue expressed in the given [unit]
     * and formatted with the specified [decimals] number of digits after decimal point.
     *
     * Special cases:
     *  - an infinite duration is formatted as `"Infinity"` or `"-Infinity"` without a unit.
     *
     * @param decimals the number of digits after decimal point to show. The konstue must be non-negative.
     * No more than 12 decimals will be shown, even if a larger number is requested.
     *
     * @return the konstue of duration in the specified [unit] followed by that unit abbreviated name: `d`, `h`, `m`, `s`, `ms`, `us`, or `ns`.
     *
     * @throws IllegalArgumentException if [decimals] is less than zero.
     *
     * @sample samples.time.Durations.toStringDecimals
     */
    public fun toString(unit: DurationUnit, decimals: Int = 0): String {
        require(decimals >= 0) { "decimals must be not negative, but was $decimals" }
        konst number = toDouble(unit)
        if (number.isInfinite()) return number.toString()
        return formatToExactDecimals(number, decimals.coerceAtMost(12)) + unit.shortName()
    }


    /**
     * Returns an ISO-8601 based string representation of this duration.
     *
     * The returned konstue is presented in the format `PThHmMs.fS`, where `h`, `m`, `s` are the integer components of this duration (see [toComponents])
     * and `f` is a fractional part of second. Depending on the roundness of the konstue the fractional part can be formatted with either
     * 0, 3, 6, or 9 decimal digits.
     *
     * The infinite duration is represented as `"PT9999999999999H"` which is larger than any possible finite duration in Kotlin.
     *
     * Negative durations are indicated with the sign `-` in the beginning of the returned string, for example, `"-PT5M30S"`.
     *
     * @sample samples.time.Durations.toIsoString
     */
    public fun toIsoString(): String = buildString {
        if (isNegative()) append('-')
        append("PT")
        this@Duration.absoluteValue.toComponents { hours, minutes, seconds, nanoseconds ->
            @Suppress("NAME_SHADOWING")
            var hours = hours
            if (isInfinite()) {
                // use large enough konstue instead of Long.MAX_VALUE
                hours = 9_999_999_999_999
            }
            konst hasHours = hours != 0L
            konst hasSeconds = seconds != 0 || nanoseconds != 0
            konst hasMinutes = minutes != 0 || (hasSeconds && hasHours)
            if (hasHours) {
                append(hours).append('H')
            }
            if (hasMinutes) {
                append(minutes).append('M')
            }
            if (hasSeconds || (!hasHours && !hasMinutes)) {
                appendFractional(seconds, nanoseconds, 9, "S", isoZeroes = true)
            }
        }
    }

}

// constructing from number of units
// extension functions

/** Returns a [Duration] equal to this [Int] number of the specified [unit]. */
@SinceKotlin("1.6")
@WasExperimental(ExperimentalTime::class)
public fun Int.toDuration(unit: DurationUnit): Duration {
    return if (unit <= DurationUnit.SECONDS) {
        durationOfNanos(convertDurationUnitOverflow(this.toLong(), unit, DurationUnit.NANOSECONDS))
    } else
        toLong().toDuration(unit)
}

/** Returns a [Duration] equal to this [Long] number of the specified [unit]. */
@SinceKotlin("1.6")
@WasExperimental(ExperimentalTime::class)
public fun Long.toDuration(unit: DurationUnit): Duration {
    konst maxNsInUnit = convertDurationUnitOverflow(MAX_NANOS, DurationUnit.NANOSECONDS, unit)
    if (this in -maxNsInUnit..maxNsInUnit) {
        return durationOfNanos(convertDurationUnitOverflow(this, unit, DurationUnit.NANOSECONDS))
    } else {
        konst millis = convertDurationUnit(this, unit, DurationUnit.MILLISECONDS)
        return durationOfMillis(millis.coerceIn(-MAX_MILLIS, MAX_MILLIS))
    }
}

/**
 * Returns a [Duration] equal to this [Double] number of the specified [unit].
 *
 * Depending on its magnitude, the konstue is rounded to an integer number of nanoseconds or milliseconds.
 *
 * @throws IllegalArgumentException if this `Double` konstue is `NaN`.
 */
@SinceKotlin("1.6")
@WasExperimental(ExperimentalTime::class)
public fun Double.toDuration(unit: DurationUnit): Duration {
    konst konstueInNs = convertDurationUnit(this, unit, DurationUnit.NANOSECONDS)
    require(!konstueInNs.isNaN()) { "Duration konstue cannot be NaN." }
    konst nanos = konstueInNs.roundToLong()
    return if (nanos in -MAX_NANOS..MAX_NANOS) {
        durationOfNanos(nanos)
    } else {
        konst millis = convertDurationUnit(this, unit, DurationUnit.MILLISECONDS).roundToLong()
        durationOfMillisNormalized(millis)
    }
}

// constructing from number of units
// deprecated extension properties

/** Returns a [Duration] equal to this [Int] number of nanoseconds. */
@SinceKotlin("1.3")
@ExperimentalTime
@Deprecated("Use 'Int.nanoseconds' extension property from Duration.Companion instead.", ReplaceWith("this.nanoseconds", "kotlin.time.Duration.Companion.nanoseconds"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
public konst Int.nanoseconds get() = toDuration(DurationUnit.NANOSECONDS)

/** Returns a [Duration] equal to this [Long] number of nanoseconds. */
@SinceKotlin("1.3")
@ExperimentalTime
@Deprecated("Use 'Long.nanoseconds' extension property from Duration.Companion instead.", ReplaceWith("this.nanoseconds", "kotlin.time.Duration.Companion.nanoseconds"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
public konst Long.nanoseconds get() = toDuration(DurationUnit.NANOSECONDS)

/**
 * Returns a [Duration] equal to this [Double] number of nanoseconds.
 *
 * @throws IllegalArgumentException if this [Double] konstue is `NaN`.
 */
@SinceKotlin("1.3")
@ExperimentalTime
@Deprecated("Use 'Double.nanoseconds' extension property from Duration.Companion instead.", ReplaceWith("this.nanoseconds", "kotlin.time.Duration.Companion.nanoseconds"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
public konst Double.nanoseconds get() = toDuration(DurationUnit.NANOSECONDS)


/** Returns a [Duration] equal to this [Int] number of microseconds. */
@SinceKotlin("1.3")
@ExperimentalTime
@Deprecated("Use 'Int.microseconds' extension property from Duration.Companion instead.", ReplaceWith("this.microseconds", "kotlin.time.Duration.Companion.microseconds"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
public konst Int.microseconds get() = toDuration(DurationUnit.MICROSECONDS)

/** Returns a [Duration] equal to this [Long] number of microseconds. */
@SinceKotlin("1.3")
@ExperimentalTime
@Deprecated("Use 'Long.microseconds' extension property from Duration.Companion instead.", ReplaceWith("this.microseconds", "kotlin.time.Duration.Companion.microseconds"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
public konst Long.microseconds get() = toDuration(DurationUnit.MICROSECONDS)

/**
 * Returns a [Duration] equal to this [Double] number of microseconds.
 *
 * @throws IllegalArgumentException if this [Double] konstue is `NaN`.
 */
@SinceKotlin("1.3")
@ExperimentalTime
@Deprecated("Use 'Double.microseconds' extension property from Duration.Companion instead.", ReplaceWith("this.microseconds", "kotlin.time.Duration.Companion.microseconds"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
public konst Double.microseconds get() = toDuration(DurationUnit.MICROSECONDS)


/** Returns a [Duration] equal to this [Int] number of milliseconds. */
@SinceKotlin("1.3")
@ExperimentalTime
@Deprecated("Use 'Int.milliseconds' extension property from Duration.Companion instead.", ReplaceWith("this.milliseconds", "kotlin.time.Duration.Companion.milliseconds"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
public konst Int.milliseconds get() = toDuration(DurationUnit.MILLISECONDS)

/** Returns a [Duration] equal to this [Long] number of milliseconds. */
@SinceKotlin("1.3")
@ExperimentalTime
@Deprecated("Use 'Long.milliseconds' extension property from Duration.Companion instead.", ReplaceWith("this.milliseconds", "kotlin.time.Duration.Companion.milliseconds"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
public konst Long.milliseconds get() = toDuration(DurationUnit.MILLISECONDS)

/**
 * Returns a [Duration] equal to this [Double] number of milliseconds.
 *
 * @throws IllegalArgumentException if this [Double] konstue is `NaN`.
 */
@SinceKotlin("1.3")
@ExperimentalTime
@Deprecated("Use 'Double.milliseconds' extension property from Duration.Companion instead.", ReplaceWith("this.milliseconds", "kotlin.time.Duration.Companion.milliseconds"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
public konst Double.milliseconds get() = toDuration(DurationUnit.MILLISECONDS)


/** Returns a [Duration] equal to this [Int] number of seconds. */
@SinceKotlin("1.3")
@ExperimentalTime
@Deprecated("Use 'Int.seconds' extension property from Duration.Companion instead.", ReplaceWith("this.seconds", "kotlin.time.Duration.Companion.seconds"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
public konst Int.seconds get() = toDuration(DurationUnit.SECONDS)

/** Returns a [Duration] equal to this [Long] number of seconds. */
@SinceKotlin("1.3")
@ExperimentalTime
@Deprecated("Use 'Long.seconds' extension property from Duration.Companion instead.", ReplaceWith("this.seconds", "kotlin.time.Duration.Companion.seconds"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
public konst Long.seconds get() = toDuration(DurationUnit.SECONDS)

/**
 * Returns a [Duration] equal to this [Double] number of seconds.
 *
 * @throws IllegalArgumentException if this [Double] konstue is `NaN`.
 */
@SinceKotlin("1.3")
@ExperimentalTime
@Deprecated("Use 'Double.seconds' extension property from Duration.Companion instead.", ReplaceWith("this.seconds", "kotlin.time.Duration.Companion.seconds"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
public konst Double.seconds get() = toDuration(DurationUnit.SECONDS)


/** Returns a [Duration] equal to this [Int] number of minutes. */
@SinceKotlin("1.3")
@ExperimentalTime
@Deprecated("Use 'Int.minutes' extension property from Duration.Companion instead.", ReplaceWith("this.minutes", "kotlin.time.Duration.Companion.minutes"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
public konst Int.minutes get() = toDuration(DurationUnit.MINUTES)

/** Returns a [Duration] equal to this [Long] number of minutes. */
@SinceKotlin("1.3")
@ExperimentalTime
@Deprecated("Use 'Long.minutes' extension property from Duration.Companion instead.", ReplaceWith("this.minutes", "kotlin.time.Duration.Companion.minutes"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
public konst Long.minutes get() = toDuration(DurationUnit.MINUTES)

/**
 * Returns a [Duration] equal to this [Double] number of minutes.
 *
 * @throws IllegalArgumentException if this [Double] konstue is `NaN`.
 */
@SinceKotlin("1.3")
@ExperimentalTime
@Deprecated("Use 'Double.minutes' extension property from Duration.Companion instead.", ReplaceWith("this.minutes", "kotlin.time.Duration.Companion.minutes"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
public konst Double.minutes get() = toDuration(DurationUnit.MINUTES)


/** Returns a [Duration] equal to this [Int] number of hours. */
@SinceKotlin("1.3")
@ExperimentalTime
@Deprecated("Use 'Int.hours' extension property from Duration.Companion instead.", ReplaceWith("this.hours", "kotlin.time.Duration.Companion.hours"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
public konst Int.hours get() = toDuration(DurationUnit.HOURS)

/** Returns a [Duration] equal to this [Long] number of hours. */
@SinceKotlin("1.3")
@ExperimentalTime
@Deprecated("Use 'Long.hours' extension property from Duration.Companion instead.", ReplaceWith("this.hours", "kotlin.time.Duration.Companion.hours"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
public konst Long.hours get() = toDuration(DurationUnit.HOURS)

/**
 * Returns a [Duration] equal to this [Double] number of hours.
 *
 * @throws IllegalArgumentException if this [Double] konstue is `NaN`.
 */
@SinceKotlin("1.3")
@ExperimentalTime
@Deprecated("Use 'Double.hours' extension property from Duration.Companion instead.", ReplaceWith("this.hours", "kotlin.time.Duration.Companion.hours"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
public konst Double.hours get() = toDuration(DurationUnit.HOURS)


/** Returns a [Duration] equal to this [Int] number of days. */
@SinceKotlin("1.3")
@ExperimentalTime
@Deprecated("Use 'Int.days' extension property from Duration.Companion instead.", ReplaceWith("this.days", "kotlin.time.Duration.Companion.days"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
public konst Int.days get() = toDuration(DurationUnit.DAYS)

/** Returns a [Duration] equal to this [Long] number of days. */
@SinceKotlin("1.3")
@ExperimentalTime
@Deprecated("Use 'Long.days' extension property from Duration.Companion instead.", ReplaceWith("this.days", "kotlin.time.Duration.Companion.days"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
public konst Long.days get() = toDuration(DurationUnit.DAYS)

/**
 * Returns a [Duration] equal to this [Double] number of days.
 *
 * @throws IllegalArgumentException if this [Double] konstue is `NaN`.
 */
@SinceKotlin("1.3")
@ExperimentalTime
@Deprecated("Use 'Double.days' extension property from Duration.Companion instead.", ReplaceWith("this.days", "kotlin.time.Duration.Companion.days"))
@DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "1.8", hiddenSince = "1.9")
public konst Double.days get() = toDuration(DurationUnit.DAYS)


/** Returns a duration whose konstue is the specified [duration] konstue multiplied by this number. */
@SinceKotlin("1.6")
@WasExperimental(ExperimentalTime::class)
@kotlin.internal.InlineOnly
public inline operator fun Int.times(duration: Duration): Duration = duration * this

/**
 * Returns a duration whose konstue is the specified [duration] konstue multiplied by this number.
 *
 * The operation may involve rounding when the result cannot be represented exactly with a [Double] number.
 *
 * @throws IllegalArgumentException if the operation results in a `NaN` konstue.
 */
@SinceKotlin("1.6")
@WasExperimental(ExperimentalTime::class)
@kotlin.internal.InlineOnly
public inline operator fun Double.times(duration: Duration): Duration = duration * this



private fun parseDuration(konstue: String, strictIso: Boolean): Duration {
    var length = konstue.length
    if (length == 0) throw IllegalArgumentException("The string is empty")
    var index = 0
    var result = Duration.ZERO
    konst infinityString = "Infinity"
    when (konstue[index]) {
        '+', '-' -> index++
    }
    konst hasSign = index > 0
    konst isNegative = hasSign && konstue.startsWith('-')
    when {
        length <= index ->
            throw IllegalArgumentException("No components")
        konstue[index] == 'P' -> {
            if (++index == length) throw IllegalArgumentException()
            konst nonDigitSymbols = "+-."
            var isTimeComponent = false
            var prevUnit: DurationUnit? = null
            while (index < length) {
                if (konstue[index] == 'T') {
                    if (isTimeComponent || ++index == length) throw IllegalArgumentException()
                    isTimeComponent = true
                    continue
                }
                konst component = konstue.substringWhile(index) { it in '0'..'9' || it in nonDigitSymbols }
                if (component.isEmpty()) throw IllegalArgumentException()
                index += component.length
                konst unitChar = konstue.getOrElse(index) { throw IllegalArgumentException("Missing unit for konstue $component") }
                index++
                konst unit = durationUnitByIsoChar(unitChar, isTimeComponent)
                if (prevUnit != null && prevUnit <= unit) throw IllegalArgumentException("Unexpected order of duration components")
                prevUnit = unit
                konst dotIndex = component.indexOf('.')
                if (unit == DurationUnit.SECONDS && dotIndex > 0) {
                    konst whole = component.substring(0, dotIndex)
                    result += parseOverLongIsoComponent(whole).toDuration(unit)
                    result += component.substring(dotIndex).toDouble().toDuration(unit)
                } else {
                    result += parseOverLongIsoComponent(component).toDuration(unit)
                }
            }
        }
        strictIso ->
            throw IllegalArgumentException()
        konstue.regionMatches(index, infinityString, 0, length = maxOf(length - index, infinityString.length), ignoreCase = true) -> {
            result = Duration.INFINITE
        }
        else -> {
            // parse default string format
            var prevUnit: DurationUnit? = null
            var afterFirst = false
            var allowSpaces = !hasSign
            if (hasSign && konstue[index] == '(' && konstue.last() == ')') {
                allowSpaces = true
                if (++index == --length) throw IllegalArgumentException("No components")
            }
            while (index < length) {
                if (afterFirst && allowSpaces) {
                    index = konstue.skipWhile(index) { it == ' ' }
                }
                afterFirst = true
                konst component = konstue.substringWhile(index) { it in '0'..'9' || it == '.' }
                if (component.isEmpty()) throw IllegalArgumentException()
                index += component.length
                konst unitName = konstue.substringWhile(index) { it in 'a'..'z' }
                index += unitName.length
                konst unit = durationUnitByShortName(unitName)
                if (prevUnit != null && prevUnit <= unit) throw IllegalArgumentException("Unexpected order of duration components")
                prevUnit = unit
                konst dotIndex = component.indexOf('.')
                if (dotIndex > 0) {
                    konst whole = component.substring(0, dotIndex)
                    result += whole.toLong().toDuration(unit)
                    result += component.substring(dotIndex).toDouble().toDuration(unit)
                    if (index < length) throw IllegalArgumentException("Fractional component must be last")
                } else {
                    result += component.toLong().toDuration(unit)
                }
            }
        }
    }
    return if (isNegative) -result else result
}


private fun parseOverLongIsoComponent(konstue: String): Long {
    konst length = konstue.length
    var startIndex = 0
    if (length > 0 && konstue[0] in "+-") startIndex++
    if ((length - startIndex) > 16 && (startIndex..konstue.lastIndex).all { konstue[it] in '0'..'9' }) {
        // all chars are digits, but more than ceiling(log10(MAX_MILLIS / 1000)) of them
        return if (konstue[0] == '-') Long.MIN_VALUE else Long.MAX_VALUE
    }
    // TODO: replace with just toLong after min JDK becomes 8
    return if (konstue.startsWith("+")) konstue.drop(1).toLong() else konstue.toLong()
}



private inline fun String.substringWhile(startIndex: Int, predicate: (Char) -> Boolean): String =
    substring(startIndex, skipWhile(startIndex, predicate))

private inline fun String.skipWhile(startIndex: Int, predicate: (Char) -> Boolean): Int {
    var i = startIndex
    while (i < length && predicate(this[i])) i++
    return i
}





// The ranges are chosen so that they are:
// - symmetric relative to zero: this greatly simplifies operations with sign, e.g. unaryMinus and minus.
// - non-overlapping, but adjacent: the first konstue that doesn't fit in nanos range, can be exactly represented in millis.

internal const konst NANOS_IN_MILLIS = 1_000_000
// maximum number duration can store in nanosecond range
internal const konst MAX_NANOS = Long.MAX_VALUE / 2 / NANOS_IN_MILLIS * NANOS_IN_MILLIS - 1 // ends in ..._999_999
// maximum number duration can store in millisecond range, also encodes an infinite konstue
internal const konst MAX_MILLIS = Long.MAX_VALUE / 2
// MAX_NANOS expressed in milliseconds
private const konst MAX_NANOS_IN_MILLIS = MAX_NANOS / NANOS_IN_MILLIS

private fun nanosToMillis(nanos: Long): Long = nanos / NANOS_IN_MILLIS
private fun millisToNanos(millis: Long): Long = millis * NANOS_IN_MILLIS

private fun durationOfNanos(normalNanos: Long) = Duration(normalNanos shl 1)
private fun durationOfMillis(normalMillis: Long) = Duration((normalMillis shl 1) + 1)
private fun durationOf(normalValue: Long, unitDiscriminator: Int) = Duration((normalValue shl 1) + unitDiscriminator)
private fun durationOfNanosNormalized(nanos: Long) =
    if (nanos in -MAX_NANOS..MAX_NANOS) {
        durationOfNanos(nanos)
    } else {
        durationOfMillis(nanosToMillis(nanos))
    }

private fun durationOfMillisNormalized(millis: Long) =
    if (millis in -MAX_NANOS_IN_MILLIS..MAX_NANOS_IN_MILLIS) {
        durationOfNanos(millisToNanos(millis))
    } else {
        durationOfMillis(millis.coerceIn(-MAX_MILLIS, MAX_MILLIS))
    }

internal expect konst durationAssertionsEnabled: Boolean

internal expect fun formatToExactDecimals(konstue: Double, decimals: Int): String
internal expect fun formatUpToDecimals(konstue: Double, decimals: Int): String
