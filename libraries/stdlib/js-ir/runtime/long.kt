/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("NOTHING_TO_INLINE")

package kotlin

/**
 * Represents a 64-bit signed integer.
 */
public class Long internal constructor(
    internal konst low: Int,
    internal konst high: Int
) : Number(), Comparable<Long> {

    companion object {
        /**
         * A constant holding the minimum konstue an instance of Long can have.
         */
        public const konst MIN_VALUE: Long = -9223372036854775807L - 1L

        /**
         * A constant holding the maximum konstue an instance of Long can have.
         */
        public const konst MAX_VALUE: Long = 9223372036854775807L

        /**
         * The number of bytes used to represent an instance of Long in a binary form.
         */
        @SinceKotlin("1.3")
        public const konst SIZE_BYTES: Int = 8

        /**
         * The number of bits used to represent an instance of Long in a binary form.
         */
        @SinceKotlin("1.3")
        public const konst SIZE_BITS: Int = 64
    }

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Byte): Int = compareTo(other.toLong())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Short): Int = compareTo(other.toLong())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Int): Int = compareTo(other.toLong())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override operator fun compareTo(other: Long): Int = compare(other)

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Float): Int = toFloat().compareTo(other)

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Double): Int = toDouble().compareTo(other)

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Byte): Long = plus(other.toLong())

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Short): Long = plus(other.toLong())

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Int): Long = plus(other.toLong())

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Long): Long = add(other)

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Float): Float = toFloat() + other

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Double): Double = toDouble() + other

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Byte): Long = minus(other.toLong())

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Short): Long = minus(other.toLong())

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Int): Long = minus(other.toLong())

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Long): Long = subtract(other)

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Float): Float = toFloat() - other

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Double): Double = toDouble() - other

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Byte): Long = times(other.toLong())

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Short): Long = times(other.toLong())

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Int): Long = times(other.toLong())

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun times(other: Long): Long = multiply(other)

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Float): Float = toFloat() * other

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Double): Double = toDouble() * other

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Byte): Long = div(other.toLong())

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Short): Long = div(other.toLong())

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Int): Long = div(other.toLong())

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Long): Long = divide(other)

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Float): Float = toFloat() / other

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Double): Double = toDouble() / other

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Byte): Long = rem(other.toLong())

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Short): Long = rem(other.toLong())

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Int): Long = rem(other.toLong())

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Long): Long = modulo(other)

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Float): Float = toFloat() % other

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Double): Double = toDouble() % other

    /**
     * Returns this konstue incremented by one.
     *
     * @sample samples.misc.Builtins.inc
     */
    public operator fun inc(): Long = this + 1L

    /**
     * Returns this konstue decremented by one.
     *
     * @sample samples.misc.Builtins.dec
     */
    public operator fun dec(): Long = this - 1L

    /** Returns this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun unaryPlus(): Long = this

    /** Returns the negative of this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun unaryMinus(): Long = inv() + 1L

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Byte): LongRange = rangeTo(other.toLong())

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Short): LongRange = rangeTo(other.toLong())

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Int): LongRange = rangeTo(other.toLong())

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Long): LongRange = LongRange(this, other)

    /**
     * Creates a range from this konstue up to but excluding the specified [other] konstue.
     *
     * If the [other] konstue is less than or equal to `this` konstue, then the returned range is empty.
     */
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    public operator fun rangeUntil(other: Byte): LongRange = this until other

    /**
     * Creates a range from this konstue up to but excluding the specified [other] konstue.
     *
     * If the [other] konstue is less than or equal to `this` konstue, then the returned range is empty.
     */
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    public operator fun rangeUntil(other: Short): LongRange = this until other

    /**
     * Creates a range from this konstue up to but excluding the specified [other] konstue.
     *
     * If the [other] konstue is less than or equal to `this` konstue, then the returned range is empty.
     */
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    public operator fun rangeUntil(other: Int): LongRange = this until other

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
     * Note that only the six lowest-order bits of the [bitCount] are used as the shift distance.
     * The shift distance actually used is therefore always in the range `0..63`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public infix fun shl(bitCount: Int): Long = shiftLeft(bitCount)

    /**
     * Shifts this konstue right by the [bitCount] number of bits, filling the leftmost bits with copies of the sign bit.
     *
     * Note that only the six lowest-order bits of the [bitCount] are used as the shift distance.
     * The shift distance actually used is therefore always in the range `0..63`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public infix fun shr(bitCount: Int): Long = shiftRight(bitCount)

    /**
     * Shifts this konstue right by the [bitCount] number of bits, filling the leftmost bits with zeros.
     *
     * Note that only the six lowest-order bits of the [bitCount] are used as the shift distance.
     * The shift distance actually used is therefore always in the range `0..63`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public infix fun ushr(bitCount: Int): Long = shiftRightUnsigned(bitCount)

    /** Performs a bitwise AND operation between the two konstues. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public infix fun and(other: Long): Long = Long(low and other.low, high and other.high)

    /** Performs a bitwise OR operation between the two konstues. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public infix fun or(other: Long): Long = Long(low or other.low, high or other.high)

    /** Performs a bitwise XOR operation between the two konstues. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public infix fun xor(other: Long): Long = Long(low xor other.low, high xor other.high)

    /** Inverts the bits in this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public fun inv(): Long = Long(low.inv(), high.inv())

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toByte(): Byte = low.toByte()

    @Deprecated("Direct conversion to Char is deprecated. Use toInt().toChar() or Char constructor instead.", ReplaceWith("this.toInt().toChar()"))
    @DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "2.3")
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toChar(): Char = low.toChar()

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toShort(): Short = low.toShort()

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toInt(): Int = low

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toLong(): Long = this

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toFloat(): Float = toDouble().toFloat()

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toDouble(): Double = toNumber()

    // This method is used by JavaScript to convert objects of type Long to primitives.
    // This is essential for the JavaScript interop.
    // JavaScript functions that expect `number` are imported in Kotlin as expecting `kotlin.Number`
    // (in our standard library, and also in user projects if they use Dukat for generating external declarations).
    // Because `kotlin.Number` is a supertype of `Long` too, there has to be a way for JS to know how to handle Longs.
    // See KT-50202
    @JsName("konstueOf")
    internal fun konstueOf() = toDouble()

    @kotlin.internal.IntrinsicConstEkonstuation
    override fun equals(other: Any?): Boolean = other is Long && equalsLong(other)

    override fun hashCode(): Int = hashCode(this)

    @kotlin.internal.IntrinsicConstEkonstuation
    override fun toString(): String = this.toStringImpl(radix = 10)
}
