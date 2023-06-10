/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// Auto-generated file. DO NOT EDIT!

package kotlin

import kotlin.experimental.*
import kotlin.jvm.*

@SinceKotlin("1.5")
@WasExperimental(ExperimentalUnsignedTypes::class)
@JvmInline
public konstue class ULong @kotlin.internal.IntrinsicConstEkonstuation @PublishedApi internal constructor(@PublishedApi internal konst data: Long) : Comparable<ULong> {

    companion object {
        /**
         * A constant holding the minimum konstue an instance of ULong can have.
         */
        public const konst MIN_VALUE: ULong = ULong(0)

        /**
         * A constant holding the maximum konstue an instance of ULong can have.
         */
        public const konst MAX_VALUE: ULong = ULong(-1)

        /**
         * The number of bytes used to represent an instance of ULong in a binary form.
         */
        public const konst SIZE_BYTES: Int = 8

        /**
         * The number of bits used to represent an instance of ULong in a binary form.
         */
        public const konst SIZE_BITS: Int = 64
    }

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.InlineOnly
    public inline operator fun compareTo(other: UByte): Int = this.compareTo(other.toULong())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.InlineOnly
    public inline operator fun compareTo(other: UShort): Int = this.compareTo(other.toULong())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.InlineOnly
    public inline operator fun compareTo(other: UInt): Int = this.compareTo(other.toULong())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.InlineOnly
    @Suppress("OVERRIDE_BY_INLINE")
    public override inline operator fun compareTo(other: ULong): Int = ulongCompare(this.data, other.data)

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun plus(other: UByte): ULong = this.plus(other.toULong())
    /** Adds the other konstue to this konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun plus(other: UShort): ULong = this.plus(other.toULong())
    /** Adds the other konstue to this konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun plus(other: UInt): ULong = this.plus(other.toULong())
    /** Adds the other konstue to this konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun plus(other: ULong): ULong = ULong(this.data.plus(other.data))

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun minus(other: UByte): ULong = this.minus(other.toULong())
    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun minus(other: UShort): ULong = this.minus(other.toULong())
    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun minus(other: UInt): ULong = this.minus(other.toULong())
    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun minus(other: ULong): ULong = ULong(this.data.minus(other.data))

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun times(other: UByte): ULong = this.times(other.toULong())
    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun times(other: UShort): ULong = this.times(other.toULong())
    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun times(other: UInt): ULong = this.times(other.toULong())
    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun times(other: ULong): ULong = ULong(this.data.times(other.data))

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.InlineOnly
    public inline operator fun div(other: UByte): ULong = this.div(other.toULong())
    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.InlineOnly
    public inline operator fun div(other: UShort): ULong = this.div(other.toULong())
    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.InlineOnly
    public inline operator fun div(other: UInt): ULong = this.div(other.toULong())
    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.InlineOnly
    public inline operator fun div(other: ULong): ULong = ulongDivide(this, other)

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is always less than the divisor.
     */
    @kotlin.internal.InlineOnly
    public inline operator fun rem(other: UByte): ULong = this.rem(other.toULong())
    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is always less than the divisor.
     */
    @kotlin.internal.InlineOnly
    public inline operator fun rem(other: UShort): ULong = this.rem(other.toULong())
    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is always less than the divisor.
     */
    @kotlin.internal.InlineOnly
    public inline operator fun rem(other: UInt): ULong = this.rem(other.toULong())
    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is always less than the divisor.
     */
    @kotlin.internal.InlineOnly
    public inline operator fun rem(other: ULong): ULong = ulongRemainder(this, other)

    /**
     * Divides this konstue by the other konstue, flooring the result to an integer that is closer to negative infinity.
     *
     * For unsigned types, the results of flooring division and truncating division are the same.
     */
    @kotlin.internal.InlineOnly
    public inline fun floorDiv(other: UByte): ULong = this.floorDiv(other.toULong())
    /**
     * Divides this konstue by the other konstue, flooring the result to an integer that is closer to negative infinity.
     *
     * For unsigned types, the results of flooring division and truncating division are the same.
     */
    @kotlin.internal.InlineOnly
    public inline fun floorDiv(other: UShort): ULong = this.floorDiv(other.toULong())
    /**
     * Divides this konstue by the other konstue, flooring the result to an integer that is closer to negative infinity.
     *
     * For unsigned types, the results of flooring division and truncating division are the same.
     */
    @kotlin.internal.InlineOnly
    public inline fun floorDiv(other: UInt): ULong = this.floorDiv(other.toULong())
    /**
     * Divides this konstue by the other konstue, flooring the result to an integer that is closer to negative infinity.
     *
     * For unsigned types, the results of flooring division and truncating division are the same.
     */
    @kotlin.internal.InlineOnly
    public inline fun floorDiv(other: ULong): ULong = div(other)

    /**
     * Calculates the remainder of flooring division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is always less than the divisor.
     *
     * For unsigned types, the remainders of flooring division and truncating division are the same.
     */
    @kotlin.internal.InlineOnly
    public inline fun mod(other: UByte): UByte = this.mod(other.toULong()).toUByte()
    /**
     * Calculates the remainder of flooring division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is always less than the divisor.
     *
     * For unsigned types, the remainders of flooring division and truncating division are the same.
     */
    @kotlin.internal.InlineOnly
    public inline fun mod(other: UShort): UShort = this.mod(other.toULong()).toUShort()
    /**
     * Calculates the remainder of flooring division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is always less than the divisor.
     *
     * For unsigned types, the remainders of flooring division and truncating division are the same.
     */
    @kotlin.internal.InlineOnly
    public inline fun mod(other: UInt): UInt = this.mod(other.toULong()).toUInt()
    /**
     * Calculates the remainder of flooring division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is always less than the divisor.
     *
     * For unsigned types, the remainders of flooring division and truncating division are the same.
     */
    @kotlin.internal.InlineOnly
    public inline fun mod(other: ULong): ULong = rem(other)

    /**
     * Returns this konstue incremented by one.
     *
     * @sample samples.misc.Builtins.inc
     */
    @kotlin.internal.InlineOnly
    public inline operator fun inc(): ULong = ULong(data.inc())

    /**
     * Returns this konstue decremented by one.
     *
     * @sample samples.misc.Builtins.dec
     */
    @kotlin.internal.InlineOnly
    public inline operator fun dec(): ULong = ULong(data.dec())

    /** Creates a range from this konstue to the specified [other] konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun rangeTo(other: ULong): ULongRange = ULongRange(this, other)

    /**
     * Creates a range from this konstue up to but excluding the specified [other] konstue.
     *
     * If the [other] konstue is less than or equal to `this` konstue, then the returned range is empty.
     */
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    @kotlin.internal.InlineOnly
    public inline operator fun rangeUntil(other: ULong): ULongRange = this until other

    /**
     * Shifts this konstue left by the [bitCount] number of bits.
     *
     * Note that only the six lowest-order bits of the [bitCount] are used as the shift distance.
     * The shift distance actually used is therefore always in the range `0..63`.
     */
    @kotlin.internal.InlineOnly
    public inline infix fun shl(bitCount: Int): ULong = ULong(data shl bitCount)

    /**
     * Shifts this konstue right by the [bitCount] number of bits, filling the leftmost bits with zeros.
     *
     * Note that only the six lowest-order bits of the [bitCount] are used as the shift distance.
     * The shift distance actually used is therefore always in the range `0..63`.
     */
    @kotlin.internal.InlineOnly
    public inline infix fun shr(bitCount: Int): ULong = ULong(data ushr bitCount)

    /** Performs a bitwise AND operation between the two konstues. */
    @kotlin.internal.InlineOnly
    public inline infix fun and(other: ULong): ULong = ULong(this.data and other.data)
    /** Performs a bitwise OR operation between the two konstues. */
    @kotlin.internal.InlineOnly
    public inline infix fun or(other: ULong): ULong = ULong(this.data or other.data)
    /** Performs a bitwise XOR operation between the two konstues. */
    @kotlin.internal.InlineOnly
    public inline infix fun xor(other: ULong): ULong = ULong(this.data xor other.data)
    /** Inverts the bits in this konstue. */
    @kotlin.internal.InlineOnly
    public inline fun inv(): ULong = ULong(data.inv())

    /**
     * Converts this [ULong] konstue to [Byte].
     *
     * If this konstue is less than or equals to [Byte.MAX_VALUE], the resulting `Byte` konstue represents
     * the same numerical konstue as this `ULong`.
     *
     * The resulting `Byte` konstue is represented by the least significant 8 bits of this `ULong` konstue.
     * Note that the resulting `Byte` konstue may be negative.
     */
    @kotlin.internal.InlineOnly
    public inline fun toByte(): Byte = data.toByte()
    /**
     * Converts this [ULong] konstue to [Short].
     *
     * If this konstue is less than or equals to [Short.MAX_VALUE], the resulting `Short` konstue represents
     * the same numerical konstue as this `ULong`.
     *
     * The resulting `Short` konstue is represented by the least significant 16 bits of this `ULong` konstue.
     * Note that the resulting `Short` konstue may be negative.
     */
    @kotlin.internal.InlineOnly
    public inline fun toShort(): Short = data.toShort()
    /**
     * Converts this [ULong] konstue to [Int].
     *
     * If this konstue is less than or equals to [Int.MAX_VALUE], the resulting `Int` konstue represents
     * the same numerical konstue as this `ULong`.
     *
     * The resulting `Int` konstue is represented by the least significant 32 bits of this `ULong` konstue.
     * Note that the resulting `Int` konstue may be negative.
     */
    @kotlin.internal.InlineOnly
    public inline fun toInt(): Int = data.toInt()
    /**
     * Converts this [ULong] konstue to [Long].
     *
     * If this konstue is less than or equals to [Long.MAX_VALUE], the resulting `Long` konstue represents
     * the same numerical konstue as this `ULong`. Otherwise the result is negative.
     *
     * The resulting `Long` konstue has the same binary representation as this `ULong` konstue.
     */
    @kotlin.internal.InlineOnly
    public inline fun toLong(): Long = data

    /**
     * Converts this [ULong] konstue to [UByte].
     *
     * If this konstue is less than or equals to [UByte.MAX_VALUE], the resulting `UByte` konstue represents
     * the same numerical konstue as this `ULong`.
     *
     * The resulting `UByte` konstue is represented by the least significant 8 bits of this `ULong` konstue.
     */
    @kotlin.internal.InlineOnly
    public inline fun toUByte(): UByte = data.toUByte()
    /**
     * Converts this [ULong] konstue to [UShort].
     *
     * If this konstue is less than or equals to [UShort.MAX_VALUE], the resulting `UShort` konstue represents
     * the same numerical konstue as this `ULong`.
     *
     * The resulting `UShort` konstue is represented by the least significant 16 bits of this `ULong` konstue.
     */
    @kotlin.internal.InlineOnly
    public inline fun toUShort(): UShort = data.toUShort()
    /**
     * Converts this [ULong] konstue to [UInt].
     *
     * If this konstue is less than or equals to [UInt.MAX_VALUE], the resulting `UInt` konstue represents
     * the same numerical konstue as this `ULong`.
     *
     * The resulting `UInt` konstue is represented by the least significant 32 bits of this `ULong` konstue.
     */
    @kotlin.internal.InlineOnly
    public inline fun toUInt(): UInt = data.toUInt()
    /** Returns this konstue. */
    @kotlin.internal.InlineOnly
    public inline fun toULong(): ULong = this

    /**
     * Converts this [ULong] konstue to [Float].
     *
     * The resulting konstue is the closest `Float` to this `ULong` konstue.
     * In case when this `ULong` konstue is exactly between two `Float`s,
     * the one with zero at least significant bit of mantissa is selected.
     */
    @kotlin.internal.InlineOnly
    public inline fun toFloat(): Float = this.toDouble().toFloat()
    /**
     * Converts this [ULong] konstue to [Double].
     *
     * The resulting konstue is the closest `Double` to this `ULong` konstue.
     * In case when this `ULong` konstue is exactly between two `Double`s,
     * the one with zero at least significant bit of mantissa is selected.
     */
    @kotlin.internal.InlineOnly
    public inline fun toDouble(): Double = ulongToDouble(data)

    public override fun toString(): String = ulongToString(data)

}

/**
 * Converts this [Byte] konstue to [ULong].
 *
 * If this konstue is positive, the resulting `ULong` konstue represents the same numerical konstue as this `Byte`.
 *
 * The least significant 8 bits of the resulting `ULong` konstue are the same as the bits of this `Byte` konstue,
 * whereas the most significant 56 bits are filled with the sign bit of this konstue.
 */
@SinceKotlin("1.5")
@WasExperimental(ExperimentalUnsignedTypes::class)
@kotlin.internal.InlineOnly
public inline fun Byte.toULong(): ULong = ULong(this.toLong())
/**
 * Converts this [Short] konstue to [ULong].
 *
 * If this konstue is positive, the resulting `ULong` konstue represents the same numerical konstue as this `Short`.
 *
 * The least significant 16 bits of the resulting `ULong` konstue are the same as the bits of this `Short` konstue,
 * whereas the most significant 48 bits are filled with the sign bit of this konstue.
 */
@SinceKotlin("1.5")
@WasExperimental(ExperimentalUnsignedTypes::class)
@kotlin.internal.InlineOnly
public inline fun Short.toULong(): ULong = ULong(this.toLong())
/**
 * Converts this [Int] konstue to [ULong].
 *
 * If this konstue is positive, the resulting `ULong` konstue represents the same numerical konstue as this `Int`.
 *
 * The least significant 32 bits of the resulting `ULong` konstue are the same as the bits of this `Int` konstue,
 * whereas the most significant 32 bits are filled with the sign bit of this konstue.
 */
@SinceKotlin("1.5")
@WasExperimental(ExperimentalUnsignedTypes::class)
@kotlin.internal.InlineOnly
public inline fun Int.toULong(): ULong = ULong(this.toLong())
/**
 * Converts this [Long] konstue to [ULong].
 *
 * If this konstue is positive, the resulting `ULong` konstue represents the same numerical konstue as this `Long`.
 *
 * The resulting `ULong` konstue has the same binary representation as this `Long` konstue.
 */
@SinceKotlin("1.5")
@WasExperimental(ExperimentalUnsignedTypes::class)
@kotlin.internal.InlineOnly
public inline fun Long.toULong(): ULong = ULong(this)

/**
 * Converts this [Float] konstue to [ULong].
 *
 * The fractional part, if any, is rounded down towards zero.
 * Returns zero if this `Float` konstue is negative or `NaN`, [ULong.MAX_VALUE] if it's bigger than `ULong.MAX_VALUE`.
 */
@SinceKotlin("1.5")
@WasExperimental(ExperimentalUnsignedTypes::class)
@kotlin.internal.InlineOnly
public inline fun Float.toULong(): ULong = doubleToULong(this.toDouble())
/**
 * Converts this [Double] konstue to [ULong].
 *
 * The fractional part, if any, is rounded down towards zero.
 * Returns zero if this `Double` konstue is negative or `NaN`, [ULong.MAX_VALUE] if it's bigger than `ULong.MAX_VALUE`.
 */
@SinceKotlin("1.5")
@WasExperimental(ExperimentalUnsignedTypes::class)
@kotlin.internal.InlineOnly
public inline fun Double.toULong(): ULong = doubleToULong(this)
