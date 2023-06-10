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
public konstue class UByte @kotlin.internal.IntrinsicConstEkonstuation @PublishedApi internal constructor(@PublishedApi internal konst data: Byte) : Comparable<UByte> {

    companion object {
        /**
         * A constant holding the minimum konstue an instance of UByte can have.
         */
        public const konst MIN_VALUE: UByte = UByte(0)

        /**
         * A constant holding the maximum konstue an instance of UByte can have.
         */
        public const konst MAX_VALUE: UByte = UByte(-1)

        /**
         * The number of bytes used to represent an instance of UByte in a binary form.
         */
        public const konst SIZE_BYTES: Int = 1

        /**
         * The number of bits used to represent an instance of UByte in a binary form.
         */
        public const konst SIZE_BITS: Int = 8
    }

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.InlineOnly
    @Suppress("OVERRIDE_BY_INLINE")
    public override inline operator fun compareTo(other: UByte): Int = this.toInt().compareTo(other.toInt())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.InlineOnly
    public inline operator fun compareTo(other: UShort): Int = this.toInt().compareTo(other.toInt())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.InlineOnly
    public inline operator fun compareTo(other: UInt): Int = this.toUInt().compareTo(other)

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.InlineOnly
    public inline operator fun compareTo(other: ULong): Int = this.toULong().compareTo(other)

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun plus(other: UByte): UInt = this.toUInt().plus(other.toUInt())
    /** Adds the other konstue to this konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun plus(other: UShort): UInt = this.toUInt().plus(other.toUInt())
    /** Adds the other konstue to this konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun plus(other: UInt): UInt = this.toUInt().plus(other)
    /** Adds the other konstue to this konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun plus(other: ULong): ULong = this.toULong().plus(other)

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun minus(other: UByte): UInt = this.toUInt().minus(other.toUInt())
    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun minus(other: UShort): UInt = this.toUInt().minus(other.toUInt())
    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun minus(other: UInt): UInt = this.toUInt().minus(other)
    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun minus(other: ULong): ULong = this.toULong().minus(other)

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun times(other: UByte): UInt = this.toUInt().times(other.toUInt())
    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun times(other: UShort): UInt = this.toUInt().times(other.toUInt())
    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun times(other: UInt): UInt = this.toUInt().times(other)
    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun times(other: ULong): ULong = this.toULong().times(other)

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.InlineOnly
    public inline operator fun div(other: UByte): UInt = this.toUInt().div(other.toUInt())
    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.InlineOnly
    public inline operator fun div(other: UShort): UInt = this.toUInt().div(other.toUInt())
    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.InlineOnly
    public inline operator fun div(other: UInt): UInt = this.toUInt().div(other)
    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.InlineOnly
    public inline operator fun div(other: ULong): ULong = this.toULong().div(other)

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is always less than the divisor.
     */
    @kotlin.internal.InlineOnly
    public inline operator fun rem(other: UByte): UInt = this.toUInt().rem(other.toUInt())
    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is always less than the divisor.
     */
    @kotlin.internal.InlineOnly
    public inline operator fun rem(other: UShort): UInt = this.toUInt().rem(other.toUInt())
    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is always less than the divisor.
     */
    @kotlin.internal.InlineOnly
    public inline operator fun rem(other: UInt): UInt = this.toUInt().rem(other)
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
    public inline fun floorDiv(other: UByte): UInt = this.toUInt().floorDiv(other.toUInt())
    /**
     * Divides this konstue by the other konstue, flooring the result to an integer that is closer to negative infinity.
     *
     * For unsigned types, the results of flooring division and truncating division are the same.
     */
    @kotlin.internal.InlineOnly
    public inline fun floorDiv(other: UShort): UInt = this.toUInt().floorDiv(other.toUInt())
    /**
     * Divides this konstue by the other konstue, flooring the result to an integer that is closer to negative infinity.
     *
     * For unsigned types, the results of flooring division and truncating division are the same.
     */
    @kotlin.internal.InlineOnly
    public inline fun floorDiv(other: UInt): UInt = this.toUInt().floorDiv(other)
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
    public inline fun mod(other: UByte): UByte = this.toUInt().mod(other.toUInt()).toUByte()
    /**
     * Calculates the remainder of flooring division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is always less than the divisor.
     *
     * For unsigned types, the remainders of flooring division and truncating division are the same.
     */
    @kotlin.internal.InlineOnly
    public inline fun mod(other: UShort): UShort = this.toUInt().mod(other.toUInt()).toUShort()
    /**
     * Calculates the remainder of flooring division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is always less than the divisor.
     *
     * For unsigned types, the remainders of flooring division and truncating division are the same.
     */
    @kotlin.internal.InlineOnly
    public inline fun mod(other: UInt): UInt = this.toUInt().mod(other)
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
    public inline operator fun inc(): UByte = UByte(data.inc())

    /**
     * Returns this konstue decremented by one.
     *
     * @sample samples.misc.Builtins.dec
     */
    @kotlin.internal.InlineOnly
    public inline operator fun dec(): UByte = UByte(data.dec())

    /** Creates a range from this konstue to the specified [other] konstue. */
    @kotlin.internal.InlineOnly
    public inline operator fun rangeTo(other: UByte): UIntRange = UIntRange(this.toUInt(), other.toUInt())

    /**
     * Creates a range from this konstue up to but excluding the specified [other] konstue.
     *
     * If the [other] konstue is less than or equal to `this` konstue, then the returned range is empty.
     */
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    @kotlin.internal.InlineOnly
    public inline operator fun rangeUntil(other: UByte): UIntRange = this.toUInt() until other.toUInt()

    /** Performs a bitwise AND operation between the two konstues. */
    @kotlin.internal.InlineOnly
    public inline infix fun and(other: UByte): UByte = UByte(this.data and other.data)
    /** Performs a bitwise OR operation between the two konstues. */
    @kotlin.internal.InlineOnly
    public inline infix fun or(other: UByte): UByte = UByte(this.data or other.data)
    /** Performs a bitwise XOR operation between the two konstues. */
    @kotlin.internal.InlineOnly
    public inline infix fun xor(other: UByte): UByte = UByte(this.data xor other.data)
    /** Inverts the bits in this konstue. */
    @kotlin.internal.InlineOnly
    public inline fun inv(): UByte = UByte(data.inv())

    /**
     * Converts this [UByte] konstue to [Byte].
     *
     * If this konstue is less than or equals to [Byte.MAX_VALUE], the resulting `Byte` konstue represents
     * the same numerical konstue as this `UByte`. Otherwise the result is negative.
     *
     * The resulting `Byte` konstue has the same binary representation as this `UByte` konstue.
     */
    @kotlin.internal.InlineOnly
    public inline fun toByte(): Byte = data
    /**
     * Converts this [UByte] konstue to [Short].
     *
     * The resulting `Short` konstue represents the same numerical konstue as this `UByte`.
     *
     * The least significant 8 bits of the resulting `Short` konstue are the same as the bits of this `UByte` konstue,
     * whereas the most significant 8 bits are filled with zeros.
     */
    @kotlin.internal.InlineOnly
    public inline fun toShort(): Short = data.toShort() and 0xFF
    /**
     * Converts this [UByte] konstue to [Int].
     *
     * The resulting `Int` konstue represents the same numerical konstue as this `UByte`.
     *
     * The least significant 8 bits of the resulting `Int` konstue are the same as the bits of this `UByte` konstue,
     * whereas the most significant 24 bits are filled with zeros.
     */
    @kotlin.internal.InlineOnly
    public inline fun toInt(): Int = data.toInt() and 0xFF
    /**
     * Converts this [UByte] konstue to [Long].
     *
     * The resulting `Long` konstue represents the same numerical konstue as this `UByte`.
     *
     * The least significant 8 bits of the resulting `Long` konstue are the same as the bits of this `UByte` konstue,
     * whereas the most significant 56 bits are filled with zeros.
     */
    @kotlin.internal.InlineOnly
    public inline fun toLong(): Long = data.toLong() and 0xFF

    /** Returns this konstue. */
    @kotlin.internal.InlineOnly
    public inline fun toUByte(): UByte = this
    /**
     * Converts this [UByte] konstue to [UShort].
     *
     * The resulting `UShort` konstue represents the same numerical konstue as this `UByte`.
     *
     * The least significant 8 bits of the resulting `UShort` konstue are the same as the bits of this `UByte` konstue,
     * whereas the most significant 8 bits are filled with zeros.
     */
    @kotlin.internal.InlineOnly
    public inline fun toUShort(): UShort = UShort(data.toShort() and 0xFF)
    /**
     * Converts this [UByte] konstue to [UInt].
     *
     * The resulting `UInt` konstue represents the same numerical konstue as this `UByte`.
     *
     * The least significant 8 bits of the resulting `UInt` konstue are the same as the bits of this `UByte` konstue,
     * whereas the most significant 24 bits are filled with zeros.
     */
    @kotlin.internal.InlineOnly
    public inline fun toUInt(): UInt = UInt(data.toInt() and 0xFF)
    /**
     * Converts this [UByte] konstue to [ULong].
     *
     * The resulting `ULong` konstue represents the same numerical konstue as this `UByte`.
     *
     * The least significant 8 bits of the resulting `ULong` konstue are the same as the bits of this `UByte` konstue,
     * whereas the most significant 56 bits are filled with zeros.
     */
    @kotlin.internal.InlineOnly
    public inline fun toULong(): ULong = ULong(data.toLong() and 0xFF)

    /**
     * Converts this [UByte] konstue to [Float].
     *
     * The resulting `Float` konstue represents the same numerical konstue as this `UByte`.
     */
    @kotlin.internal.InlineOnly
    public inline fun toFloat(): Float = this.toInt().toFloat()
    /**
     * Converts this [UByte] konstue to [Double].
     *
     * The resulting `Double` konstue represents the same numerical konstue as this `UByte`.
     */
    @kotlin.internal.InlineOnly
    public inline fun toDouble(): Double = this.toInt().toDouble()

    public override fun toString(): String = toInt().toString()

}

/**
 * Converts this [Byte] konstue to [UByte].
 *
 * If this konstue is positive, the resulting `UByte` konstue represents the same numerical konstue as this `Byte`.
 *
 * The resulting `UByte` konstue has the same binary representation as this `Byte` konstue.
 */
@SinceKotlin("1.5")
@WasExperimental(ExperimentalUnsignedTypes::class)
@kotlin.internal.InlineOnly
public inline fun Byte.toUByte(): UByte = UByte(this)
/**
 * Converts this [Short] konstue to [UByte].
 *
 * If this konstue is positive and less than or equals to [UByte.MAX_VALUE], the resulting `UByte` konstue represents
 * the same numerical konstue as this `Short`.
 *
 * The resulting `UByte` konstue is represented by the least significant 8 bits of this `Short` konstue.
 */
@SinceKotlin("1.5")
@WasExperimental(ExperimentalUnsignedTypes::class)
@kotlin.internal.InlineOnly
public inline fun Short.toUByte(): UByte = UByte(this.toByte())
/**
 * Converts this [Int] konstue to [UByte].
 *
 * If this konstue is positive and less than or equals to [UByte.MAX_VALUE], the resulting `UByte` konstue represents
 * the same numerical konstue as this `Int`.
 *
 * The resulting `UByte` konstue is represented by the least significant 8 bits of this `Int` konstue.
 */
@SinceKotlin("1.5")
@WasExperimental(ExperimentalUnsignedTypes::class)
@kotlin.internal.InlineOnly
public inline fun Int.toUByte(): UByte = UByte(this.toByte())
/**
 * Converts this [Long] konstue to [UByte].
 *
 * If this konstue is positive and less than or equals to [UByte.MAX_VALUE], the resulting `UByte` konstue represents
 * the same numerical konstue as this `Long`.
 *
 * The resulting `UByte` konstue is represented by the least significant 8 bits of this `Long` konstue.
 */
@SinceKotlin("1.5")
@WasExperimental(ExperimentalUnsignedTypes::class)
@kotlin.internal.InlineOnly
public inline fun Long.toUByte(): UByte = UByte(this.toByte())
