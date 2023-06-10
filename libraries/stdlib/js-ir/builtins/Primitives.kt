/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// Auto-generated file. DO NOT EDIT!

@file:Suppress("NON_ABSTRACT_FUNCTION_WITH_NO_BODY", "UNUSED_PARAMETER")

package kotlin

/** Represents a 8-bit signed integer. */
public class Byte private constructor() : Number(), Comparable<Byte> {
    companion object {
        /**
         * A constant holding the minimum konstue an instance of Byte can have.
         */
        public const konst MIN_VALUE: Byte = -128

        /**
         * A constant holding the maximum konstue an instance of Byte can have.
         */
        public const konst MAX_VALUE: Byte = 127

        /**
         * The number of bytes used to represent an instance of Byte in a binary form.
         */
        @SinceKotlin("1.3")
        public const konst SIZE_BYTES: Int = 1

        /**
         * The number of bits used to represent an instance of Byte in a binary form.
         */
        @SinceKotlin("1.3")
        public const konst SIZE_BITS: Int = 8
    }

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override operator fun compareTo(other: Byte): Int

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun compareTo(other: Short): Int

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun compareTo(other: Int): Int

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun compareTo(other: Long): Int

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun compareTo(other: Float): Int

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun compareTo(other: Double): Int

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Byte): Int

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Short): Int

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Int): Int

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Long): Long

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Float): Float

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Double): Double

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Byte): Int

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Short): Int

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Int): Int

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Long): Long

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Float): Float

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Double): Double

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Byte): Int

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Short): Int

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Int): Int

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Long): Long

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Float): Float

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Double): Double

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Byte): Int

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Short): Int

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Int): Int

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Long): Long

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Float): Float

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Double): Double

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Byte): Int

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Short): Int

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Int): Int

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Long): Long

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Float): Float

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Double): Double

    /**
     * Returns this konstue incremented by one.
     *
     * @sample samples.misc.Builtins.inc
     */
    public operator fun inc(): Byte

    /**
     * Returns this konstue decremented by one.
     *
     * @sample samples.misc.Builtins.dec
     */
    public operator fun dec(): Byte

    /** Returns this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun unaryPlus(): Int

    /** Returns the negative of this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun unaryMinus(): Int

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Byte): IntRange

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Short): IntRange

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Int): IntRange

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Long): LongRange

    /**
     * Creates a range from this konstue up to but excluding the specified [other] konstue.
     *
     * If the [other] konstue is less than or equal to `this` konstue, then the returned range is empty.
     */
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    public operator fun rangeUntil(other: Byte): IntRange = this until other

    /**
     * Creates a range from this konstue up to but excluding the specified [other] konstue.
     *
     * If the [other] konstue is less than or equal to `this` konstue, then the returned range is empty.
     */
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    public operator fun rangeUntil(other: Short): IntRange = this until other

    /**
     * Creates a range from this konstue up to but excluding the specified [other] konstue.
     *
     * If the [other] konstue is less than or equal to `this` konstue, then the returned range is empty.
     */
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    public operator fun rangeUntil(other: Int): IntRange = this until other

    /**
     * Creates a range from this konstue up to but excluding the specified [other] konstue.
     *
     * If the [other] konstue is less than or equal to `this` konstue, then the returned range is empty.
     */
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    public operator fun rangeUntil(other: Long): LongRange = this until other

    /** Returns this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toByte(): Byte

    /**
     * Converts this [Byte] konstue to [Char].
     *
     * If this konstue is non-negative, the resulting `Char` code is equal to this konstue.
     *
     * The least significant 8 bits of the resulting `Char` code are the same as the bits of this `Byte` konstue,
     * whereas the most significant 8 bits are filled with the sign bit of this konstue.
     */
    @Deprecated("Direct conversion to Char is deprecated. Use toInt().toChar() or Char constructor instead.", ReplaceWith("this.toInt().toChar()"))
    @DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "2.3")
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toChar(): Char

    /**
     * Converts this [Byte] konstue to [Short].
     *
     * The resulting `Short` konstue represents the same numerical konstue as this `Byte`.
     *
     * The least significant 8 bits of the resulting `Short` konstue are the same as the bits of this `Byte` konstue,
     * whereas the most significant 8 bits are filled with the sign bit of this konstue.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toShort(): Short

    /**
     * Converts this [Byte] konstue to [Int].
     *
     * The resulting `Int` konstue represents the same numerical konstue as this `Byte`.
     *
     * The least significant 8 bits of the resulting `Int` konstue are the same as the bits of this `Byte` konstue,
     * whereas the most significant 24 bits are filled with the sign bit of this konstue.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toInt(): Int

    /**
     * Converts this [Byte] konstue to [Long].
     *
     * The resulting `Long` konstue represents the same numerical konstue as this `Byte`.
     *
     * The least significant 8 bits of the resulting `Long` konstue are the same as the bits of this `Byte` konstue,
     * whereas the most significant 56 bits are filled with the sign bit of this konstue.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toLong(): Long

    /**
     * Converts this [Byte] konstue to [Float].
     *
     * The resulting `Float` konstue represents the same numerical konstue as this `Byte`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toFloat(): Float

    /**
     * Converts this [Byte] konstue to [Double].
     *
     * The resulting `Double` konstue represents the same numerical konstue as this `Byte`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toDouble(): Double

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toString(): String

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun equals(other: Any?): Boolean

    public override fun hashCode(): Int
}

/** Represents a 16-bit signed integer. */
public class Short private constructor() : Number(), Comparable<Short> {
    companion object {
        /**
         * A constant holding the minimum konstue an instance of Short can have.
         */
        public const konst MIN_VALUE: Short = -32768

        /**
         * A constant holding the maximum konstue an instance of Short can have.
         */
        public const konst MAX_VALUE: Short = 32767

        /**
         * The number of bytes used to represent an instance of Short in a binary form.
         */
        @SinceKotlin("1.3")
        public const konst SIZE_BYTES: Int = 2

        /**
         * The number of bits used to represent an instance of Short in a binary form.
         */
        @SinceKotlin("1.3")
        public const konst SIZE_BITS: Int = 16
    }

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun compareTo(other: Byte): Int

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override operator fun compareTo(other: Short): Int

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun compareTo(other: Int): Int

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun compareTo(other: Long): Int

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun compareTo(other: Float): Int

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun compareTo(other: Double): Int

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Byte): Int

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Short): Int

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Int): Int

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Long): Long

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Float): Float

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Double): Double

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Byte): Int

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Short): Int

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Int): Int

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Long): Long

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Float): Float

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Double): Double

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Byte): Int

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Short): Int

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Int): Int

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Long): Long

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Float): Float

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Double): Double

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Byte): Int

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Short): Int

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Int): Int

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Long): Long

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Float): Float

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Double): Double

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Byte): Int

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Short): Int

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Int): Int

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Long): Long

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Float): Float

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Double): Double

    /**
     * Returns this konstue incremented by one.
     *
     * @sample samples.misc.Builtins.inc
     */
    public operator fun inc(): Short

    /**
     * Returns this konstue decremented by one.
     *
     * @sample samples.misc.Builtins.dec
     */
    public operator fun dec(): Short

    /** Returns this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun unaryPlus(): Int

    /** Returns the negative of this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun unaryMinus(): Int

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Byte): IntRange

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Short): IntRange

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Int): IntRange

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Long): LongRange

    /**
     * Creates a range from this konstue up to but excluding the specified [other] konstue.
     *
     * If the [other] konstue is less than or equal to `this` konstue, then the returned range is empty.
     */
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    public operator fun rangeUntil(other: Byte): IntRange = this until other

    /**
     * Creates a range from this konstue up to but excluding the specified [other] konstue.
     *
     * If the [other] konstue is less than or equal to `this` konstue, then the returned range is empty.
     */
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    public operator fun rangeUntil(other: Short): IntRange = this until other

    /**
     * Creates a range from this konstue up to but excluding the specified [other] konstue.
     *
     * If the [other] konstue is less than or equal to `this` konstue, then the returned range is empty.
     */
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    public operator fun rangeUntil(other: Int): IntRange = this until other

    /**
     * Creates a range from this konstue up to but excluding the specified [other] konstue.
     *
     * If the [other] konstue is less than or equal to `this` konstue, then the returned range is empty.
     */
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    public operator fun rangeUntil(other: Long): LongRange = this until other

    /**
     * Converts this [Short] konstue to [Byte].
     *
     * If this konstue is in [Byte.MIN_VALUE]..[Byte.MAX_VALUE], the resulting `Byte` konstue represents
     * the same numerical konstue as this `Short`.
     *
     * The resulting `Byte` konstue is represented by the least significant 8 bits of this `Short` konstue.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toByte(): Byte

    /**
     * Converts this [Short] konstue to [Char].
     *
     * The resulting `Char` code is equal to this konstue reinterpreted as an unsigned number,
     * i.e. it has the same binary representation as this `Short`.
     */
    @Deprecated("Direct conversion to Char is deprecated. Use toInt().toChar() or Char constructor instead.", ReplaceWith("this.toInt().toChar()"))
    @DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "2.3")
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toChar(): Char

    /** Returns this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toShort(): Short

    /**
     * Converts this [Short] konstue to [Int].
     *
     * The resulting `Int` konstue represents the same numerical konstue as this `Short`.
     *
     * The least significant 16 bits of the resulting `Int` konstue are the same as the bits of this `Short` konstue,
     * whereas the most significant 16 bits are filled with the sign bit of this konstue.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toInt(): Int

    /**
     * Converts this [Short] konstue to [Long].
     *
     * The resulting `Long` konstue represents the same numerical konstue as this `Short`.
     *
     * The least significant 16 bits of the resulting `Long` konstue are the same as the bits of this `Short` konstue,
     * whereas the most significant 48 bits are filled with the sign bit of this konstue.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toLong(): Long

    /**
     * Converts this [Short] konstue to [Float].
     *
     * The resulting `Float` konstue represents the same numerical konstue as this `Short`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toFloat(): Float

    /**
     * Converts this [Short] konstue to [Double].
     *
     * The resulting `Double` konstue represents the same numerical konstue as this `Short`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toDouble(): Double

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toString(): String

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun equals(other: Any?): Boolean

    public override fun hashCode(): Int
}

/** Represents a 32-bit signed integer. */
public class Int private constructor() : Number(), Comparable<Int> {
    companion object {
        /**
         * A constant holding the minimum konstue an instance of Int can have.
         */
        public const konst MIN_VALUE: Int = -2147483648

        /**
         * A constant holding the maximum konstue an instance of Int can have.
         */
        public const konst MAX_VALUE: Int = 2147483647

        /**
         * The number of bytes used to represent an instance of Int in a binary form.
         */
        @SinceKotlin("1.3")
        public const konst SIZE_BYTES: Int = 4

        /**
         * The number of bits used to represent an instance of Int in a binary form.
         */
        @SinceKotlin("1.3")
        public const konst SIZE_BITS: Int = 32
    }

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun compareTo(other: Byte): Int

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun compareTo(other: Short): Int

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override operator fun compareTo(other: Int): Int

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun compareTo(other: Long): Int

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun compareTo(other: Float): Int

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun compareTo(other: Double): Int

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Byte): Int

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Short): Int

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Int): Int

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Long): Long

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Float): Float

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Double): Double

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Byte): Int

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Short): Int

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Int): Int

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Long): Long

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Float): Float

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Double): Double

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Byte): Int

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Short): Int

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Int): Int

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Long): Long

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Float): Float

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Double): Double

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Byte): Int

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Short): Int

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Int): Int

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Long): Long

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Float): Float

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Double): Double

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Byte): Int

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Short): Int

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Int): Int

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Long): Long

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Float): Float

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Double): Double

    /**
     * Returns this konstue incremented by one.
     *
     * @sample samples.misc.Builtins.inc
     */
    public operator fun inc(): Int

    /**
     * Returns this konstue decremented by one.
     *
     * @sample samples.misc.Builtins.dec
     */
    public operator fun dec(): Int

    /** Returns this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun unaryPlus(): Int

    /** Returns the negative of this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun unaryMinus(): Int

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Byte): IntRange

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Short): IntRange

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Int): IntRange

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Long): LongRange

    /**
     * Creates a range from this konstue up to but excluding the specified [other] konstue.
     *
     * If the [other] konstue is less than or equal to `this` konstue, then the returned range is empty.
     */
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    public operator fun rangeUntil(other: Byte): IntRange = this until other

    /**
     * Creates a range from this konstue up to but excluding the specified [other] konstue.
     *
     * If the [other] konstue is less than or equal to `this` konstue, then the returned range is empty.
     */
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    public operator fun rangeUntil(other: Short): IntRange = this until other

    /**
     * Creates a range from this konstue up to but excluding the specified [other] konstue.
     *
     * If the [other] konstue is less than or equal to `this` konstue, then the returned range is empty.
     */
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    public operator fun rangeUntil(other: Int): IntRange = this until other

    /**
     * Creates a range from this konstue up to but excluding the specified [other] konstue.
     *
     * If the [other] konstue is less than or equal to `this` konstue, then the returned range is empty.
     */
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    public operator fun rangeUntil(other: Long): LongRange = this until other

    /**
     * Shifts this konstue left by the [bitCount] number of bits.
     *
     * Note that only the five lowest-order bits of the [bitCount] are used as the shift distance.
     * The shift distance actually used is therefore always in the range `0..31`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public infix fun shl(bitCount: Int): Int

    /**
     * Shifts this konstue right by the [bitCount] number of bits, filling the leftmost bits with copies of the sign bit.
     *
     * Note that only the five lowest-order bits of the [bitCount] are used as the shift distance.
     * The shift distance actually used is therefore always in the range `0..31`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public infix fun shr(bitCount: Int): Int

    /**
     * Shifts this konstue right by the [bitCount] number of bits, filling the leftmost bits with zeros.
     *
     * Note that only the five lowest-order bits of the [bitCount] are used as the shift distance.
     * The shift distance actually used is therefore always in the range `0..31`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public infix fun ushr(bitCount: Int): Int

    /** Performs a bitwise AND operation between the two konstues. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public infix fun and(other: Int): Int

    /** Performs a bitwise OR operation between the two konstues. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public infix fun or(other: Int): Int

    /** Performs a bitwise XOR operation between the two konstues. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public infix fun xor(other: Int): Int

    /** Inverts the bits in this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public fun inv(): Int

    /**
     * Converts this [Int] konstue to [Byte].
     *
     * If this konstue is in [Byte.MIN_VALUE]..[Byte.MAX_VALUE], the resulting `Byte` konstue represents
     * the same numerical konstue as this `Int`.
     *
     * The resulting `Byte` konstue is represented by the least significant 8 bits of this `Int` konstue.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toByte(): Byte

    /**
     * Converts this [Int] konstue to [Char].
     *
     * If this konstue is in the range of `Char` codes `Char.MIN_VALUE..Char.MAX_VALUE`,
     * the resulting `Char` code is equal to this konstue.
     *
     * The resulting `Char` code is represented by the least significant 16 bits of this `Int` konstue.
     */
    @Suppress("OVERRIDE_DEPRECATION")
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toChar(): Char

    /**
     * Converts this [Int] konstue to [Short].
     *
     * If this konstue is in [Short.MIN_VALUE]..[Short.MAX_VALUE], the resulting `Short` konstue represents
     * the same numerical konstue as this `Int`.
     *
     * The resulting `Short` konstue is represented by the least significant 16 bits of this `Int` konstue.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toShort(): Short

    /** Returns this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toInt(): Int

    /**
     * Converts this [Int] konstue to [Long].
     *
     * The resulting `Long` konstue represents the same numerical konstue as this `Int`.
     *
     * The least significant 32 bits of the resulting `Long` konstue are the same as the bits of this `Int` konstue,
     * whereas the most significant 32 bits are filled with the sign bit of this konstue.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toLong(): Long

    /**
     * Converts this [Int] konstue to [Float].
     *
     * The resulting konstue is the closest `Float` to this `Int` konstue.
     * In case when this `Int` konstue is exactly between two `Float`s,
     * the one with zero at least significant bit of mantissa is selected.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toFloat(): Float

    /**
     * Converts this [Int] konstue to [Double].
     *
     * The resulting `Double` konstue represents the same numerical konstue as this `Int`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toDouble(): Double

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toString(): String

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun equals(other: Any?): Boolean

    public override fun hashCode(): Int
}

/** Represents a single-precision 32-bit IEEE 754 floating point number. */
public class Float private constructor() : Number(), Comparable<Float> {
    companion object {
        /**
         * A constant holding the smallest *positive* nonzero konstue of Float.
         */
        public const konst MIN_VALUE: Float = 1.4E-45F

        /**
         * A constant holding the largest positive finite konstue of Float.
         */
        public const konst MAX_VALUE: Float = 3.4028235E38F

        /**
         * A constant holding the positive infinity konstue of Float.
         */
        @Suppress("DIVISION_BY_ZERO")
        public const konst POSITIVE_INFINITY: Float = 1.0F/0.0F

        /**
         * A constant holding the negative infinity konstue of Float.
         */
        @Suppress("DIVISION_BY_ZERO")
        public const konst NEGATIVE_INFINITY: Float = -1.0F/0.0F

        /**
         * A constant holding the "not a number" konstue of Float.
         */
        @Suppress("DIVISION_BY_ZERO")
        public const konst NaN: Float = -(0.0F/0.0F)

        /**
         * The number of bytes used to represent an instance of Float in a binary form.
         */
        @SinceKotlin("1.4")
        public const konst SIZE_BYTES: Int = 4

        /**
         * The number of bits used to represent an instance of Float in a binary form.
         */
        @SinceKotlin("1.4")
        public const konst SIZE_BITS: Int = 32
    }

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun compareTo(other: Byte): Int

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun compareTo(other: Short): Int

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun compareTo(other: Int): Int

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun compareTo(other: Long): Int

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override operator fun compareTo(other: Float): Int

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun compareTo(other: Double): Int

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Byte): Float

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Short): Float

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Int): Float

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Long): Float

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Float): Float

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Double): Double

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Byte): Float

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Short): Float

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Int): Float

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Long): Float

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Float): Float

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Double): Double

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Byte): Float

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Short): Float

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Int): Float

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Long): Float

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Float): Float

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Double): Double

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Byte): Float

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Short): Float

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Int): Float

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Long): Float

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Float): Float

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Double): Double

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Byte): Float

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Short): Float

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Int): Float

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Long): Float

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Float): Float

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Double): Double

    /**
     * Returns this konstue incremented by one.
     *
     * @sample samples.misc.Builtins.inc
     */
    public operator fun inc(): Float

    /**
     * Returns this konstue decremented by one.
     *
     * @sample samples.misc.Builtins.dec
     */
    public operator fun dec(): Float

    /** Returns this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun unaryPlus(): Float

    /** Returns the negative of this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun unaryMinus(): Float

    /**
     * Converts this [Float] konstue to [Byte].
     *
     * The resulting `Byte` konstue is equal to `this.toInt().toByte()`.
     */
    @Deprecated("Unclear conversion. To achieve the same result convert to Int explicitly and then to Byte.", ReplaceWith("toInt().toByte()"))
    @DeprecatedSinceKotlin(warningSince = "1.3", errorSince = "1.5")
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toByte(): Byte

    /**
     * Converts this [Float] konstue to [Char].
     *
     * The resulting `Char` konstue is equal to `this.toInt().toChar()`.
     */
    @Deprecated("Direct conversion to Char is deprecated. Use toInt().toChar() or Char constructor instead.", ReplaceWith("this.toInt().toChar()"))
    @DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "2.3")
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toChar(): Char

    /**
     * Converts this [Float] konstue to [Short].
     *
     * The resulting `Short` konstue is equal to `this.toInt().toShort()`.
     */
    @Deprecated("Unclear conversion. To achieve the same result convert to Int explicitly and then to Short.", ReplaceWith("toInt().toShort()"))
    @DeprecatedSinceKotlin(warningSince = "1.3", errorSince = "1.5")
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toShort(): Short

    /**
     * Converts this [Float] konstue to [Int].
     *
     * The fractional part, if any, is rounded down towards zero.
     * Returns zero if this `Float` konstue is `NaN`, [Int.MIN_VALUE] if it's less than `Int.MIN_VALUE`,
     * [Int.MAX_VALUE] if it's bigger than `Int.MAX_VALUE`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toInt(): Int

    /**
     * Converts this [Float] konstue to [Long].
     *
     * The fractional part, if any, is rounded down towards zero.
     * Returns zero if this `Float` konstue is `NaN`, [Long.MIN_VALUE] if it's less than `Long.MIN_VALUE`,
     * [Long.MAX_VALUE] if it's bigger than `Long.MAX_VALUE`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toLong(): Long

    /** Returns this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toFloat(): Float

    /**
     * Converts this [Float] konstue to [Double].
     *
     * The resulting `Double` konstue represents the same numerical konstue as this `Float`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toDouble(): Double

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toString(): String

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun equals(other: Any?): Boolean

    public override fun hashCode(): Int
}

/** Represents a double-precision 64-bit IEEE 754 floating point number. */
public class Double private constructor() : Number(), Comparable<Double> {
    companion object {
        /**
         * A constant holding the smallest *positive* nonzero konstue of Double.
         */
        public const konst MIN_VALUE: Double = 4.9E-324

        /**
         * A constant holding the largest positive finite konstue of Double.
         */
        public const konst MAX_VALUE: Double = 1.7976931348623157E308

        /**
         * A constant holding the positive infinity konstue of Double.
         */
        @Suppress("DIVISION_BY_ZERO")
        public const konst POSITIVE_INFINITY: Double = 1.0/0.0

        /**
         * A constant holding the negative infinity konstue of Double.
         */
        @Suppress("DIVISION_BY_ZERO")
        public const konst NEGATIVE_INFINITY: Double = -1.0/0.0

        /**
         * A constant holding the "not a number" konstue of Double.
         */
        @Suppress("DIVISION_BY_ZERO")
        public const konst NaN: Double = -(0.0/0.0)

        /**
         * The number of bytes used to represent an instance of Double in a binary form.
         */
        @SinceKotlin("1.4")
        public const konst SIZE_BYTES: Int = 8

        /**
         * The number of bits used to represent an instance of Double in a binary form.
         */
        @SinceKotlin("1.4")
        public const konst SIZE_BITS: Int = 64
    }

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun compareTo(other: Byte): Int

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun compareTo(other: Short): Int

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun compareTo(other: Int): Int

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun compareTo(other: Long): Int

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun compareTo(other: Float): Int

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override operator fun compareTo(other: Double): Int

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Byte): Double

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Short): Double

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Int): Double

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Long): Double

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Float): Double

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Double): Double

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Byte): Double

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Short): Double

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Int): Double

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Long): Double

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Float): Double

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Double): Double

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Byte): Double

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Short): Double

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Int): Double

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Long): Double

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Float): Double

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Double): Double

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Byte): Double

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Short): Double

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Int): Double

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Long): Double

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Float): Double

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Double): Double

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Byte): Double

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Short): Double

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Int): Double

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Long): Double

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Float): Double

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Double): Double

    /**
     * Returns this konstue incremented by one.
     *
     * @sample samples.misc.Builtins.inc
     */
    public operator fun inc(): Double

    /**
     * Returns this konstue decremented by one.
     *
     * @sample samples.misc.Builtins.dec
     */
    public operator fun dec(): Double

    /** Returns this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun unaryPlus(): Double

    /** Returns the negative of this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun unaryMinus(): Double

    /**
     * Converts this [Double] konstue to [Byte].
     *
     * The resulting `Byte` konstue is equal to `this.toInt().toByte()`.
     */
    @Deprecated("Unclear conversion. To achieve the same result convert to Int explicitly and then to Byte.", ReplaceWith("toInt().toByte()"))
    @DeprecatedSinceKotlin(warningSince = "1.3", errorSince = "1.5")
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toByte(): Byte

    /**
     * Converts this [Double] konstue to [Char].
     *
     * The resulting `Char` konstue is equal to `this.toInt().toChar()`.
     */
    @Deprecated("Direct conversion to Char is deprecated. Use toInt().toChar() or Char constructor instead.", ReplaceWith("this.toInt().toChar()"))
    @DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "2.3")
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toChar(): Char

    /**
     * Converts this [Double] konstue to [Short].
     *
     * The resulting `Short` konstue is equal to `this.toInt().toShort()`.
     */
    @Deprecated("Unclear conversion. To achieve the same result convert to Int explicitly and then to Short.", ReplaceWith("toInt().toShort()"))
    @DeprecatedSinceKotlin(warningSince = "1.3", errorSince = "1.5")
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toShort(): Short

    /**
     * Converts this [Double] konstue to [Int].
     *
     * The fractional part, if any, is rounded down towards zero.
     * Returns zero if this `Double` konstue is `NaN`, [Int.MIN_VALUE] if it's less than `Int.MIN_VALUE`,
     * [Int.MAX_VALUE] if it's bigger than `Int.MAX_VALUE`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toInt(): Int

    /**
     * Converts this [Double] konstue to [Long].
     *
     * The fractional part, if any, is rounded down towards zero.
     * Returns zero if this `Double` konstue is `NaN`, [Long.MIN_VALUE] if it's less than `Long.MIN_VALUE`,
     * [Long.MAX_VALUE] if it's bigger than `Long.MAX_VALUE`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toLong(): Long

    /**
     * Converts this [Double] konstue to [Float].
     *
     * The resulting konstue is the closest `Float` to this `Double` konstue.
     * In case when this `Double` konstue is exactly between two `Float`s,
     * the one with zero at least significant bit of mantissa is selected.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toFloat(): Float

    /** Returns this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toDouble(): Double

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toString(): String

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun equals(other: Any?): Boolean

    public override fun hashCode(): Int
}
