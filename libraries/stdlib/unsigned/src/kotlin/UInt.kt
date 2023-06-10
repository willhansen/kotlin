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
public konstue class UInt @kotlin.internal.IntrinsicConstEkonstuation @PublishedApi internal constructor(@PublishedApi internal konst data: Int) : Comparable<UInt> {

    companion object {
        /**
         * A constant holding the minimum konstue an instance of UInt can have.
         */
        public const konst MIN_VALUE: UInt = UInt(0)

        /**
         * A constant holding the maximum konstue an instance of UInt can have.
         */
        public const konst MAX_VALUE: UInt = UInt(-1)

        /**
         * The number of bytes used to represent an instance of UInt in a binary form.
         */
        public const konst SIZE_BYTES: Int = 4

        /**
         * The number of bits used to represent an instance of UInt in a binary form.
         */
        public const konst SIZE_BITS: Int = 32
    }

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.InlineOnly
    public inline operator fun compareTo(other: UByte): Int = this.compareTo(other.toUInt())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.InlineOnly
    public inline operator fun compareTo(other: UShort): Int = this.compareTo(other.toUInt())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.InlineOnly
    @Suppress("OVERRIDE_BY_INLINE")
    public override inline operator fun compareTo(other: UInt): Int = uintCompare(this.data, other.data)

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.InlineOnly
    public inline operator fun compareTo(other: ULong): Int = this.toULong().compareTo(other)

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun plus(other: UByte): UInt = this.plus(other.toUInt())
    /** Adds the other konstue to this konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun plus(other: UShort): UInt = this.plus(other.toUInt())
    /** Adds the other konstue to this konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun plus(other: UInt): UInt = UInt(this.data.plus(other.data))
    /** Adds the other konstue to this konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun plus(other: ULong): ULong = this.toULong().plus(other)

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun minus(other: UByte): UInt = this.minus(other.toUInt())
    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun minus(other: UShort): UInt = this.minus(other.toUInt())
    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun minus(other: UInt): UInt = UInt(this.data.minus(other.data))
    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun minus(other: ULong): ULong = this.toULong().minus(other)

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun times(other: UByte): UInt = this.times(other.toUInt())
    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun times(other: UShort): UInt = this.times(other.toUInt())
    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun times(other: UInt): UInt = UInt(this.data.times(other.data))
    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun times(other: ULong): ULong = this.toULong().times(other)

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.InlineOnly
    public inline operator fun div(other: UByte): UInt = this.div(other.toUInt())
    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.InlineOnly
    public inline operator fun div(other: UShort): UInt = this.div(other.toUInt())
    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.InlineOnly
    public inline operator fun div(other: UInt): UInt = uintDivide(this, other)
    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.InlineOnly
    public inline operator fun div(other: ULong): ULong = this.toULong().div(other)

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is always less than the divisor.
     */
    @kotlin.internal.InlineOnly
    public inline operator fun rem(other: UByte): UInt = this.rem(other.toUInt())
    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is always less than the divisor.
     */
    @kotlin.internal.InlineOnly
    public inline operator fun rem(other: UShort): UInt = this.rem(other.toUInt())
    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is always less than the divisor.
     */
    @kotlin.internal.InlineOnly
    public inline operator fun rem(other: UInt): UInt = uintRemainder(this, other)
    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is always less than the divisor.
     */
    @kotlin.internal.InlineOnly
    public inline operator fun rem(other: ULong): ULong = this.toULong().rem(other)

    /**
     * Divides this konstue by the other konstue, flooring the result to an integer that is closer to negative infinity.
     *
     * For unsigned types, the results of flooring division and truncating division are the same.
     */
    @kotlin.internal.InlineOnly
    public inline fun floorDiv(other: UByte): UInt = this.floorDiv(other.toUInt())
    /**
     * Divides this konstue by the other konstue, flooring the result to an integer that is closer to negative infinity.
     *
     * For unsigned types, the results of flooring division and truncating division are the same.
     */
    @kotlin.internal.InlineOnly
    public inline fun floorDiv(other: UShort): UInt = this.floorDiv(other.toUInt())
    /**
     * Divides this konstue by the other konstue, flooring the result to an integer that is closer to negative infinity.
     *
     * For unsigned types, the results of flooring division and truncating division are the same.
     */
    @kotlin.internal.InlineOnly
    public inline fun floorDiv(other: UInt): UInt = div(other)
    /**
     * Divides this konstue by the other konstue, flooring the result to an integer that is closer to negative infinity.
     *
     * For unsigned types, the results of flooring division and truncating division are the same.
     */
    @kotlin.internal.InlineOnly
    public inline fun floorDiv(other: ULong): ULong = this.toULong().floorDiv(other)

    /**
     * Calculates the remainder of flooring division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is always less than the divisor.
     *
     * For unsigned types, the remainders of flooring division and truncating division are the same.
     */
    @kotlin.internal.InlineOnly
    public inline fun mod(other: UByte): UByte = this.mod(other.toUInt()).toUByte()
    /**
     * Calculates the remainder of flooring division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is always less than the divisor.
     *
     * For unsigned types, the remainders of flooring division and truncating division are the same.
     */
    @kotlin.internal.InlineOnly
    public inline fun mod(other: UShort): UShort = this.mod(other.toUInt()).toUShort()
    /**
     * Calculates the remainder of flooring division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is always less than the divisor.
     *
     * For unsigned types, the remainders of flooring division and truncating division are the same.
     */
    @kotlin.internal.InlineOnly
    public inline fun mod(other: UInt): UInt = rem(other)
    /**
     * Calculates the remainder of flooring division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is always less than the divisor.
     *
     * For unsigned types, the remainders of flooring division and truncating division are the same.
     */
    @kotlin.internal.InlineOnly
    public inline fun mod(other: ULong): ULong = this.toULong().mod(other)

    /**
     * Returns this konstue incremented by one.
     *
     * @sample samples.misc.Builtins.inc
     */
    @kotlin.internal.InlineOnly
    public inline operator fun inc(): UInt = UInt(data.inc())

    /**
     * Returns this konstue decremented by one.
     *
     * @sample samples.misc.Builtins.dec
     */
    @kotlin.internal.InlineOnly
    public inline operator fun dec(): UInt = UInt(data.dec())

    /** Creates a range from this konstue to the specified [other] konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun rangeTo(other: UInt): UIntRange = UIntRange(this, other)

    /**
     * Creates a range from this konstue up to but excluding the specified [other] konstue.
     *
     * If the [other] konstue is less than or equal to `this` konstue, then the returned range is empty.
     */
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    @kotlin.internal.InlineOnly
    public inline operator fun rangeUntil(other: UInt): UIntRange = this until other

    /**
     * Shifts this konstue left by the [bitCount] number of bits.
     *
     * Note that only the five lowest-order bits of the [bitCount] are used as the shift distance.
     * The shift distance actually used is therefore always in the range `0..31`.
     */
    @kotlin.internal.InlineOnly
    public inline infix fun shl(bitCount: Int): UInt = UInt(data shl bitCount)

    /**
     * Shifts this konstue right by the [bitCount] number of bits, filling the leftmost bits with zeros.
     *
     * Note that only the five lowest-order bits of the [bitCount] are used as the shift distance.
     * The shift distance actually used is therefore always in the range `0..31`.
     */
    @kotlin.internal.InlineOnly
    public inline infix fun shr(bitCount: Int): UInt = UInt(data ushr bitCount)

    /** Performs a bitwise AND operation between the two konstues. */
    @kotlin.internal.InlineOnly
    public inline infix fun and(other: UInt): UInt = UInt(this.data and other.data)
    /** Performs a bitwise OR operation between the two konstues. */
    @kotlin.internal.InlineOnly
    public inline infix fun or(other: UInt): UInt = UInt(this.data or other.data)
    /** Performs a bitwise XOR operation between the two konstues. */
    @kotlin.internal.InlineOnly
    public inline infix fun xor(other: UInt): UInt = UInt(this.data xor other.data)
    /** Inverts the bits in this konstue. */
    @kotlin.internal.InlineOnly
    public inline fun inv(): UInt = UInt(data.inv())

    /**
     * Converts this [UInt] konstue to [Byte].
     *
     * If this konstue is less than or equals to [Byte.MAX_VALUE], the resulting `Byte` konstue represents
     * the same numerical konstue as this `UInt`.
     *
     * The resulting `Byte` konstue is represented by the least significant 8 bits of this `UInt` konstue.
     * Note that the resulting `Byte` konstue may be negative.
     */
    @kotlin.internal.InlineOnly
    public inline fun toByte(): Byte = data.toByte()
    /**
     * Converts this [UInt] konstue to [Short].
     *
     * If this konstue is less than or equals to [Short.MAX_VALUE], the resulting `Short` konstue represents
     * the same numerical konstue as this `UInt`.
     *
     * The resulting `Short` konstue is represented by the least significant 16 bits of this `UInt` konstue.
     * Note that the resulting `Short` konstue may be negative.
     */
    @kotlin.internal.InlineOnly
    public inline fun toShort(): Short = data.toShort()
    /**
     * Converts this [UInt] konstue to [Int].
     *
     * If this konstue is less than or equals to [Int.MAX_VALUE], the resulting `Int` konstue represents
     * the same numerical konstue as this `UInt`. Otherwise the result is negative.
     *
     * The resulting `Int` konstue has the same binary representation as this `UInt` konstue.
     */
    @kotlin.internal.InlineOnly
    public inline fun toInt(): Int = data
    /**
     * Converts this [UInt] konstue to [Long].
     *
     * The resulting `Long` konstue represents the same numerical konstue as this `UInt`.
     *
     * The least significant 32 bits of the resulting `Long` konstue are the same as the bits of this `UInt` konstue,
     * whereas the most significant 32 bits are filled with zeros.
     */
    @kotlin.internal.InlineOnly
    public inline fun toLong(): Long = data.toLong() and 0xFFFF_FFFF

    /**
     * Converts this [UInt] konstue to [UByte].
     *
     * If this konstue is less than or equals to [UByte.MAX_VALUE], the resulting `UByte` konstue represents
     * the same numerical konstue as this `UInt`.
     *
     * The resulting `UByte` konstue is represented by the least significant 8 bits of this `UInt` konstue.
     */
    @kotlin.internal.InlineOnly
    public inline fun toUByte(): UByte = data.toUByte()
    /**
     * Converts this [UInt] konstue to [UShort].
     *
     * If this konstue is less than or equals to [UShort.MAX_VALUE], the resulting `UShort` konstue represents
     * the same numerical konstue as this `UInt`.
     *
     * The resulting `UShort` konstue is represented by the least significant 16 bits of this `UInt` konstue.
     */
    @kotlin.internal.InlineOnly
    public inline fun toUShort(): UShort = data.toUShort()
    /** Returns this konstue. */
    @kotlin.internal.InlineOnly
    public inline fun toUInt(): UInt = this
    /**
     * Converts this [UInt] konstue to [ULong].
     *
     * The resulting `ULong` konstue represents the same numerical konstue as this `UInt`.
     *
     * The least significant 32 bits of the resulting `ULong` konstue are the same as the bits of this `UInt` konstue,
     * whereas the most significant 32 bits are filled with zeros.
     */
    @kotlin.internal.InlineOnly
    public inline fun toULong(): ULong = ULong(data.toLong() and 0xFFFF_FFFF)

    /**
     * Converts this [UInt] konstue to [Float].
     *
     * The resulting konstue is the closest `Float` to this `UInt` konstue.
     * In case when this `UInt` konstue is exactly between two `Float`s,
     * the one with zero at least significant bit of mantissa is selected.
     */
    @kotlin.internal.InlineOnly
    public inline fun toFloat(): Float = this.toDouble().toFloat()
    /**
     * Converts this [UInt] konstue to [Double].
     *
     * The resulting `Double` konstue represents the same numerical konstue as this `UInt`.
     */
    @kotlin.internal.InlineOnly
    public inline fun toDouble(): Double = uintToDouble(data)

    public override fun toString(): String = toLong().toString()

}

/**
 * Converts this [Byte] konstue to [UInt].
 *
 * If this konstue is positive, the resulting `UInt` konstue represents the same numerical konstue as this `Byte`.
 *
 * The least significant 8 bits of the resulting `UInt` konstue are the same as the bits of this `Byte` konstue,
 * whereas the most significant 24 bits are filled with the sign bit of this konstue.
 */
@SinceKotlin("1.5")
@WasExperimental(ExperimentalUnsignedTypes::class)
@kotlin.internal.InlineOnly
public inline fun Byte.toUInt(): UInt = UInt(this.toInt())
/**
 * Converts this [Short] konstue to [UInt].
 *
 * If this konstue is positive, the resulting `UInt` konstue represents the same numerical konstue as this `Short`.
 *
 * The least significant 16 bits of the resulting `UInt` konstue are the same as the bits of this `Short` konstue,
 * whereas the most significant 16 bits are filled with the sign bit of this konstue.
 */
@SinceKotlin("1.5")
@WasExperimental(ExperimentalUnsignedTypes::class)
@kotlin.internal.InlineOnly
public inline fun Short.toUInt(): UInt = UInt(this.toInt())
/**
 * Converts this [Int] konstue to [UInt].
 *
 * If this konstue is positive, the resulting `UInt` konstue represents the same numerical konstue as this `Int`.
 *
 * The resulting `UInt` konstue has the same binary representation as this `Int` konstue.
 */
@SinceKotlin("1.5")
@WasExperimental(ExperimentalUnsignedTypes::class)
@kotlin.internal.InlineOnly
public inline fun Int.toUInt(): UInt = UInt(this)
/**
 * Converts this [Long] konstue to [UInt].
 *
 * If this konstue is positive and less than or equals to [UInt.MAX_VALUE], the resulting `UInt` konstue represents
 * the same numerical konstue as this `Long`.
 *
 * The resulting `UInt` konstue is represented by the least significant 32 bits of this `Long` konstue.
 */
@SinceKotlin("1.5")
@WasExperimental(ExperimentalUnsignedTypes::class)
@kotlin.internal.InlineOnly
public inline fun Long.toUInt(): UInt = UInt(this.toInt())

/**
 * Converts this [Float] konstue to [UInt].
 *
 * The fractional part, if any, is rounded down towards zero.
 * Returns zero if this `Float` konstue is negative or `NaN`, [UInt.MAX_VALUE] if it's bigger than `UInt.MAX_VALUE`.
 */
@SinceKotlin("1.5")
@WasExperimental(ExperimentalUnsignedTypes::class)
@kotlin.internal.InlineOnly
public inline fun Float.toUInt(): UInt = doubleToUInt(this.toDouble())
/**
 * Converts this [Double] konstue to [UInt].
 *
 * The fractional part, if any, is rounded down towards zero.
 * Returns zero if this `Double` konstue is negative or `NaN`, [UInt.MAX_VALUE] if it's bigger than `UInt.MAX_VALUE`.
 */
@SinceKotlin("1.5")
@WasExperimental(ExperimentalUnsignedTypes::class)
@kotlin.internal.InlineOnly
public inline fun Double.toUInt(): UInt = doubleToUInt(this)
