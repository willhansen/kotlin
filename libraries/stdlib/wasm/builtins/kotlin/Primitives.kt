/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

// Auto-generated file. DO NOT EDIT!

@file:Suppress("OVERRIDE_BY_INLINE", "NOTHING_TO_INLINE", "unused", "UNUSED_PARAMETER")

package kotlin

import kotlin.wasm.internal.*

/** Represents a 8-bit signed integer. */
@WasmAutoboxed
public class Byte private constructor(private konst konstue: Byte) : Number(), Comparable<Byte> {
    public companion object {
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
    public override inline operator fun compareTo(other: Byte): Int =
        wasm_i32_compareTo(this.toInt(), other.toInt())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Short): Int =
        this.toShort().compareTo(other)

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Int): Int =
        this.toInt().compareTo(other)

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Long): Int =
        this.toLong().compareTo(other)

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Float): Int =
        this.toFloat().compareTo(other)

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Double): Int =
        this.toDouble().compareTo(other)

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Byte): Int =
        this.toInt() + other.toInt()

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Short): Int =
        this.toInt() + other.toInt()

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Int): Int =
        this.toInt() + other

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Long): Long =
        this.toLong() + other

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Float): Float =
        this.toFloat() + other

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Double): Double =
        this.toDouble() + other

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Byte): Int =
        this.toInt() - other.toInt()

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Short): Int =
        this.toInt() - other.toInt()

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Int): Int =
        this.toInt() - other

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Long): Long =
        this.toLong() - other

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Float): Float =
        this.toFloat() - other

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Double): Double =
        this.toDouble() - other

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Byte): Int =
        this.toInt() * other.toInt()

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Short): Int =
        this.toInt() * other.toInt()

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Int): Int =
        this.toInt() * other

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Long): Long =
        this.toLong() * other

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Float): Float =
        this.toFloat() * other

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Double): Double =
        this.toDouble() * other

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Byte): Int =
        this.toInt() / other.toInt()

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Short): Int =
        this.toInt() / other.toInt()

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Int): Int =
        this.toInt() / other

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Long): Long =
        this.toLong() / other

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Float): Float =
        this.toFloat() / other

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Double): Double =
        this.toDouble() / other

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Byte): Int =
        this.toInt() % other.toInt()

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Short): Int =
        this.toInt() % other.toInt()

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Int): Int =
        this.toInt() % other

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Long): Long =
        this.toLong() % other

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Float): Float =
        this.toFloat() % other

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Double): Double =
        this.toDouble() % other

    /**
     * Returns this konstue incremented by one.
     *
     * @sample samples.misc.Builtins.inc
     */
    public inline operator fun inc(): Byte =
        (this + 1).toByte()

    /**
     * Returns this konstue decremented by one.
     *
     * @sample samples.misc.Builtins.dec
     */
    public inline operator fun dec(): Byte =
        (this - 1).toByte()

    /** Returns this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun unaryPlus(): Int =
        this.toInt()

    /** Returns the negative of this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun unaryMinus(): Int =
        -this.toInt()

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Byte): IntRange {
        return IntRange(this.toInt(), other.toInt())
    }

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Short): IntRange {
        return IntRange(this.toInt(), other.toInt())
    }

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Int): IntRange {
        return IntRange(this.toInt(), other)
    }

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Long): LongRange {
        return LongRange(this.toLong(), other)
    }

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
    public override inline fun toByte(): Byte =
        this

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
    public override fun toChar(): Char = reinterpretAsInt().reinterpretAsChar()

    /**
     * Converts this [Byte] konstue to [Short].
     *
     * The resulting `Short` konstue represents the same numerical konstue as this `Byte`.
     *
     * The least significant 8 bits of the resulting `Short` konstue are the same as the bits of this `Byte` konstue,
     * whereas the most significant 8 bits are filled with the sign bit of this konstue.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toShort(): Short = reinterpretAsInt().reinterpretAsShort()

    /**
     * Converts this [Byte] konstue to [Int].
     *
     * The resulting `Int` konstue represents the same numerical konstue as this `Byte`.
     *
     * The least significant 8 bits of the resulting `Int` konstue are the same as the bits of this `Byte` konstue,
     * whereas the most significant 24 bits are filled with the sign bit of this konstue.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toInt(): Int = reinterpretAsInt()

    /**
     * Converts this [Byte] konstue to [Long].
     *
     * The resulting `Long` konstue represents the same numerical konstue as this `Byte`.
     *
     * The least significant 8 bits of the resulting `Long` konstue are the same as the bits of this `Byte` konstue,
     * whereas the most significant 56 bits are filled with the sign bit of this konstue.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toLong(): Long = wasm_i64_extend_i32_s(this.toInt())

    /**
     * Converts this [Byte] konstue to [Float].
     *
     * The resulting `Float` konstue represents the same numerical konstue as this `Byte`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toFloat(): Float = wasm_f32_convert_i32_s(this.toInt())

    /**
     * Converts this [Byte] konstue to [Double].
     *
     * The resulting `Double` konstue represents the same numerical konstue as this `Byte`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toDouble(): Double = wasm_f64_convert_i32_s(this.toInt())

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toString(): String =
        this.toInt().toString()

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun equals(other: Any?): Boolean =
        other is Byte && wasm_i32_eq(this.toInt(), other.toInt())

    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.I32_EQ)
    public fun equals(other: Byte): Boolean =
        implementedAsIntrinsic

    public override fun hashCode(): Int = this.toInt()

    @WasmNoOpCast
    @PublishedApi
    internal fun reinterpretAsInt(): Int =
        implementedAsIntrinsic
}

/** Represents a 16-bit signed integer. */
@WasmAutoboxed
public class Short private constructor(private konst konstue: Short) : Number(), Comparable<Short> {
    public companion object {
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
    public inline operator fun compareTo(other: Byte): Int =
        this.compareTo(other.toShort())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override inline operator fun compareTo(other: Short): Int =
        this.toInt().compareTo(other.toInt())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Int): Int =
        this.toInt().compareTo(other)

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Long): Int =
        this.toLong().compareTo(other)

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Float): Int =
        this.toFloat().compareTo(other)

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Double): Int =
        this.toDouble().compareTo(other)

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Byte): Int =
        this.toInt() + other.toInt()

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Short): Int =
        this.toInt() + other.toInt()

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Int): Int =
        this.toInt() + other

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Long): Long =
        this.toLong() + other

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Float): Float =
        this.toFloat() + other

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Double): Double =
        this.toDouble() + other

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Byte): Int =
        this.toInt() - other.toInt()

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Short): Int =
        this.toInt() - other.toInt()

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Int): Int =
        this.toInt() - other

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Long): Long =
        this.toLong() - other

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Float): Float =
        this.toFloat() - other

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Double): Double =
        this.toDouble() - other

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Byte): Int =
        this.toInt() * other.toInt()

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Short): Int =
        this.toInt() * other.toInt()

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Int): Int =
        this.toInt() * other

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Long): Long =
        this.toLong() * other

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Float): Float =
        this.toFloat() * other

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Double): Double =
        this.toDouble() * other

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Byte): Int =
        this.toInt() / other.toInt()

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Short): Int =
        this.toInt() / other.toInt()

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Int): Int =
        this.toInt() / other

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Long): Long =
        this.toLong() / other

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Float): Float =
        this.toFloat() / other

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Double): Double =
        this.toDouble() / other

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Byte): Int =
        this.toInt() % other.toInt()

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Short): Int =
        this.toInt() % other.toInt()

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Int): Int =
        this.toInt() % other

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Long): Long =
        this.toLong() % other

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Float): Float =
        this.toFloat() % other

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Double): Double =
        this.toDouble() % other

    /**
     * Returns this konstue incremented by one.
     *
     * @sample samples.misc.Builtins.inc
     */
    public inline operator fun inc(): Short =
        (this + 1).toShort()

    /**
     * Returns this konstue decremented by one.
     *
     * @sample samples.misc.Builtins.dec
     */
    public inline operator fun dec(): Short =
        (this - 1).toShort()

    /** Returns this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun unaryPlus(): Int =
        this.toInt()

    /** Returns the negative of this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun unaryMinus(): Int =
        -this.toInt()

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Byte): IntRange {
        return IntRange(this.toInt(), other.toInt())
    }

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Short): IntRange {
        return IntRange(this.toInt(), other.toInt())
    }

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Int): IntRange {
        return IntRange(this.toInt(), other)
    }

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Long): LongRange {
        return LongRange(this.toLong(), other)
    }

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
    public override inline fun toByte(): Byte = this.toInt().toByte()

    /**
     * Converts this [Short] konstue to [Char].
     *
     * The resulting `Char` code is equal to this konstue reinterpreted as an unsigned number,
     * i.e. it has the same binary representation as this `Short`.
     */
    @Deprecated("Direct conversion to Char is deprecated. Use toInt().toChar() or Char constructor instead.", ReplaceWith("this.toInt().toChar()"))
    @DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "2.3")
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toChar(): Char = reinterpretAsInt().reinterpretAsChar()

    /** Returns this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override inline fun toShort(): Short =
        this

    /**
     * Converts this [Short] konstue to [Int].
     *
     * The resulting `Int` konstue represents the same numerical konstue as this `Short`.
     *
     * The least significant 16 bits of the resulting `Int` konstue are the same as the bits of this `Short` konstue,
     * whereas the most significant 16 bits are filled with the sign bit of this konstue.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toInt(): Int = reinterpretAsInt()

    /**
     * Converts this [Short] konstue to [Long].
     *
     * The resulting `Long` konstue represents the same numerical konstue as this `Short`.
     *
     * The least significant 16 bits of the resulting `Long` konstue are the same as the bits of this `Short` konstue,
     * whereas the most significant 48 bits are filled with the sign bit of this konstue.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toLong(): Long = wasm_i64_extend_i32_s(this.toInt())

    /**
     * Converts this [Short] konstue to [Float].
     *
     * The resulting `Float` konstue represents the same numerical konstue as this `Short`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toFloat(): Float = wasm_f32_convert_i32_s(this.toInt())

    /**
     * Converts this [Short] konstue to [Double].
     *
     * The resulting `Double` konstue represents the same numerical konstue as this `Short`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toDouble(): Double = wasm_f64_convert_i32_s(this.toInt())

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toString(): String =
        this.toInt().toString()

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun equals(other: Any?): Boolean =
        other is Short && wasm_i32_eq(this.toInt(), other.toInt())

    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.I32_EQ)
    public fun equals(other: Short): Boolean =
        implementedAsIntrinsic

    public override fun hashCode(): Int = this.toInt()

    @WasmNoOpCast
    @PublishedApi
    internal fun reinterpretAsInt(): Int =
        implementedAsIntrinsic
}

/** Represents a 32-bit signed integer. */
@WasmAutoboxed
public class Int private constructor(private konst konstue: Int) : Number(), Comparable<Int> {
    public companion object {
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
    public inline operator fun compareTo(other: Byte): Int =
        this.compareTo(other.toInt())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Short): Int =
        this.compareTo(other.toInt())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override inline operator fun compareTo(other: Int): Int =
        wasm_i32_compareTo(this, other)

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Long): Int =
        this.toLong().compareTo(other)

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Float): Int =
        this.toFloat().compareTo(other)

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Double): Int =
        this.toDouble().compareTo(other)

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Byte): Int =
        this + other.toInt()

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Short): Int =
        this + other.toInt()

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.I32_ADD)
    public operator fun plus(other: Int): Int =
        implementedAsIntrinsic

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Long): Long =
        this.toLong() + other

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Float): Float =
        this.toFloat() + other

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Double): Double =
        this.toDouble() + other

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Byte): Int =
        this - other.toInt()

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Short): Int =
        this - other.toInt()

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.I32_SUB)
    public operator fun minus(other: Int): Int =
        implementedAsIntrinsic

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Long): Long =
        this.toLong() - other

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Float): Float =
        this.toFloat() - other

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Double): Double =
        this.toDouble() - other

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Byte): Int =
        this * other.toInt()

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Short): Int =
        this * other.toInt()

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.I32_MUL)
    public operator fun times(other: Int): Int =
        implementedAsIntrinsic

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Long): Long =
        this.toLong() * other

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Float): Float =
        this.toFloat() * other

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Double): Double =
        this.toDouble() * other

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Byte): Int =
        this / other.toInt()

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Short): Int =
        this / other.toInt()

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Int): Int =
        if (this == Int.MIN_VALUE && other == -1) Int.MIN_VALUE else wasm_i32_div_s(this, other)

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Long): Long =
        this.toLong() / other

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Float): Float =
        this.toFloat() / other

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Double): Double =
        this.toDouble() / other

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Byte): Int =
        this % other.toInt()

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Short): Int =
        this % other.toInt()

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.I32_REM_S)
    public operator fun rem(other: Int): Int =
        implementedAsIntrinsic

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Long): Long =
        this.toLong() % other

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Float): Float =
        this.toFloat() % other

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Double): Double =
        this.toDouble() % other

    /**
     * Returns this konstue incremented by one.
     *
     * @sample samples.misc.Builtins.inc
     */
    public inline operator fun inc(): Int =
        this + 1

    /**
     * Returns this konstue decremented by one.
     *
     * @sample samples.misc.Builtins.dec
     */
    // TODO: Fix test compiler/testData/codegen/box/functions/invoke/invoke.kt with inline dec
    public operator fun dec(): Int =
        this - 1

    /** Returns this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun unaryPlus(): Int =
        this

    /** Returns the negative of this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun unaryMinus(): Int =
        0 - this

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Byte): IntRange {
        return IntRange(this, other.toInt())
    }

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Short): IntRange {
        return IntRange(this, other.toInt())
    }

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Int): IntRange {
        return IntRange(this, other)
    }

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Long): LongRange {
        return LongRange(this.toLong(), other)
    }

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
    @WasmOp(WasmOp.I32_SHL)
    public infix fun shl(bitCount: Int): Int =
        implementedAsIntrinsic

    /**
     * Shifts this konstue right by the [bitCount] number of bits, filling the leftmost bits with copies of the sign bit.
     *
     * Note that only the five lowest-order bits of the [bitCount] are used as the shift distance.
     * The shift distance actually used is therefore always in the range `0..31`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.I32_SHR_S)
    public infix fun shr(bitCount: Int): Int =
        implementedAsIntrinsic

    /**
     * Shifts this konstue right by the [bitCount] number of bits, filling the leftmost bits with zeros.
     *
     * Note that only the five lowest-order bits of the [bitCount] are used as the shift distance.
     * The shift distance actually used is therefore always in the range `0..31`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.I32_SHR_U)
    public infix fun ushr(bitCount: Int): Int =
        implementedAsIntrinsic

    /** Performs a bitwise AND operation between the two konstues. */
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.I32_AND)
    public infix fun and(other: Int): Int =
        implementedAsIntrinsic

    /** Performs a bitwise OR operation between the two konstues. */
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.I32_OR)
    public infix fun or(other: Int): Int =
        implementedAsIntrinsic

    /** Performs a bitwise XOR operation between the two konstues. */
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.I32_XOR)
    public infix fun xor(other: Int): Int =
        implementedAsIntrinsic

    /** Inverts the bits in this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline fun inv(): Int =
        this.xor(-1)

    /**
     * Converts this [Int] konstue to [Byte].
     *
     * If this konstue is in [Byte.MIN_VALUE]..[Byte.MAX_VALUE], the resulting `Byte` konstue represents
     * the same numerical konstue as this `Int`.
     *
     * The resulting `Byte` konstue is represented by the least significant 8 bits of this `Int` konstue.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toByte(): Byte = ((this shl 24) shr 24).reinterpretAsByte()

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
    public override fun toChar(): Char = (this and 0xFFFF).reinterpretAsChar()

    /**
     * Converts this [Int] konstue to [Short].
     *
     * If this konstue is in [Short.MIN_VALUE]..[Short.MAX_VALUE], the resulting `Short` konstue represents
     * the same numerical konstue as this `Int`.
     *
     * The resulting `Short` konstue is represented by the least significant 16 bits of this `Int` konstue.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toShort(): Short = ((this shl 16) shr 16).reinterpretAsShort()

    /** Returns this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override inline fun toInt(): Int =
        this

    /**
     * Converts this [Int] konstue to [Long].
     *
     * The resulting `Long` konstue represents the same numerical konstue as this `Int`.
     *
     * The least significant 32 bits of the resulting `Long` konstue are the same as the bits of this `Int` konstue,
     * whereas the most significant 32 bits are filled with the sign bit of this konstue.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toLong(): Long = wasm_i64_extend_i32_s(this)

    /**
     * Converts this [Int] konstue to [Float].
     *
     * The resulting konstue is the closest `Float` to this `Int` konstue.
     * In case when this `Int` konstue is exactly between two `Float`s,
     * the one with zero at least significant bit of mantissa is selected.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toFloat(): Float = wasm_f32_convert_i32_s(this)

    /**
     * Converts this [Int] konstue to [Double].
     *
     * The resulting `Double` konstue represents the same numerical konstue as this `Int`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toDouble(): Double = wasm_f64_convert_i32_s(this)

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toString(): String =
        itoa32(this, 10)

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun equals(other: Any?): Boolean =
        other is Int && wasm_i32_eq(this, other)

    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.I32_EQ)
    public fun equals(other: Int): Boolean =
        implementedAsIntrinsic

    public override fun hashCode(): Int = this

    @WasmNoOpCast
    @PublishedApi
    internal fun reinterpretAsBoolean(): Boolean =
        implementedAsIntrinsic

    @WasmNoOpCast
    @PublishedApi
    internal fun reinterpretAsByte(): Byte =
        implementedAsIntrinsic

    @WasmNoOpCast
    @PublishedApi
    internal fun reinterpretAsShort(): Short =
        implementedAsIntrinsic

    @WasmNoOpCast
    @PublishedApi
    internal fun reinterpretAsChar(): Char =
        implementedAsIntrinsic
}

/** Represents a 64-bit signed integer. */
@WasmAutoboxed
public class Long private constructor(private konst konstue: Long) : Number(), Comparable<Long> {
    public companion object {
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
    public inline operator fun compareTo(other: Byte): Int =
        this.compareTo(other.toLong())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Short): Int =
        this.compareTo(other.toLong())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Int): Int =
        this.compareTo(other.toLong())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override inline operator fun compareTo(other: Long): Int =
        wasm_i64_compareTo(this, other)

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Float): Int =
        this.toFloat().compareTo(other)

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Double): Int =
        this.toDouble().compareTo(other)

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Byte): Long =
        this + other.toLong()

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Short): Long =
        this + other.toLong()

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Int): Long =
        this + other.toLong()

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.I64_ADD)
    public operator fun plus(other: Long): Long =
        implementedAsIntrinsic

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Float): Float =
        this.toFloat() + other

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Double): Double =
        this.toDouble() + other

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Byte): Long =
        this - other.toLong()

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Short): Long =
        this - other.toLong()

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Int): Long =
        this - other.toLong()

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.I64_SUB)
    public operator fun minus(other: Long): Long =
        implementedAsIntrinsic

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Float): Float =
        this.toFloat() - other

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Double): Double =
        this.toDouble() - other

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Byte): Long =
        this * other.toLong()

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Short): Long =
        this * other.toLong()

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Int): Long =
        this * other.toLong()

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.I64_MUL)
    public operator fun times(other: Long): Long =
        implementedAsIntrinsic

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Float): Float =
        this.toFloat() * other

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Double): Double =
        this.toDouble() * other

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Byte): Long =
        this / other.toLong()

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Short): Long =
        this / other.toLong()

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Int): Long =
        this / other.toLong()

    /** Divides this konstue by the other konstue, truncating the result to an integer that is closer to zero. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun div(other: Long): Long =
        if (this == Long.MIN_VALUE && other == -1L) Long.MIN_VALUE else wasm_i64_div_s(this, other)

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Float): Float =
        this.toFloat() / other

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Double): Double =
        this.toDouble() / other

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Byte): Long =
        this % other.toLong()

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Short): Long =
        this % other.toLong()

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Int): Long =
        this % other.toLong()

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.I64_REM_S)
    public operator fun rem(other: Long): Long =
        implementedAsIntrinsic

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Float): Float =
        this.toFloat() % other

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Double): Double =
        this.toDouble() % other

    /**
     * Returns this konstue incremented by one.
     *
     * @sample samples.misc.Builtins.inc
     */
    public inline operator fun inc(): Long =
        this + 1L

    /**
     * Returns this konstue decremented by one.
     *
     * @sample samples.misc.Builtins.dec
     */
    public inline operator fun dec(): Long =
        this - 1L

    /** Returns this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun unaryPlus(): Long =
        this

    /** Returns the negative of this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun unaryMinus(): Long =
        0L - this

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Byte): LongRange {
        return LongRange(this, other.toLong())
    }

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Short): LongRange {
        return LongRange(this, other.toLong())
    }

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Int): LongRange {
        return LongRange(this, other.toLong())
    }

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Long): LongRange {
        return LongRange(this, other)
    }

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
    public inline infix fun shl(bitCount: Int): Long =
        wasm_i64_shl(this, bitCount.toLong())

    /**
     * Shifts this konstue right by the [bitCount] number of bits, filling the leftmost bits with copies of the sign bit.
     *
     * Note that only the six lowest-order bits of the [bitCount] are used as the shift distance.
     * The shift distance actually used is therefore always in the range `0..63`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline infix fun shr(bitCount: Int): Long =
        wasm_i64_shr_s(this, bitCount.toLong())

    /**
     * Shifts this konstue right by the [bitCount] number of bits, filling the leftmost bits with zeros.
     *
     * Note that only the six lowest-order bits of the [bitCount] are used as the shift distance.
     * The shift distance actually used is therefore always in the range `0..63`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline infix fun ushr(bitCount: Int): Long =
        wasm_i64_shr_u(this, bitCount.toLong())

    /** Performs a bitwise AND operation between the two konstues. */
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.I64_AND)
    public infix fun and(other: Long): Long =
        implementedAsIntrinsic

    /** Performs a bitwise OR operation between the two konstues. */
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.I64_OR)
    public infix fun or(other: Long): Long =
        implementedAsIntrinsic

    /** Performs a bitwise XOR operation between the two konstues. */
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.I64_XOR)
    public infix fun xor(other: Long): Long =
        implementedAsIntrinsic

    /** Inverts the bits in this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline fun inv(): Long =
        this.xor(-1L)

    /**
     * Converts this [Long] konstue to [Byte].
     *
     * If this konstue is in [Byte.MIN_VALUE]..[Byte.MAX_VALUE], the resulting `Byte` konstue represents
     * the same numerical konstue as this `Long`.
     *
     * The resulting `Byte` konstue is represented by the least significant 8 bits of this `Long` konstue.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override inline fun toByte(): Byte = this.toInt().toByte()

    /**
     * Converts this [Long] konstue to [Char].
     *
     * If this konstue is in the range of `Char` codes `Char.MIN_VALUE..Char.MAX_VALUE`,
     * the resulting `Char` code is equal to this konstue.
     *
     * The resulting `Char` code is represented by the least significant 16 bits of this `Long` konstue.
     */
    @Deprecated("Direct conversion to Char is deprecated. Use toInt().toChar() or Char constructor instead.", ReplaceWith("this.toInt().toChar()"))
    @DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "2.3")
    @kotlin.internal.IntrinsicConstEkonstuation
    public override inline fun toChar(): Char = this.toInt().toChar()

    /**
     * Converts this [Long] konstue to [Short].
     *
     * If this konstue is in [Short.MIN_VALUE]..[Short.MAX_VALUE], the resulting `Short` konstue represents
     * the same numerical konstue as this `Long`.
     *
     * The resulting `Short` konstue is represented by the least significant 16 bits of this `Long` konstue.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override inline fun toShort(): Short = this.toInt().toShort()

    /**
     * Converts this [Long] konstue to [Int].
     *
     * If this konstue is in [Int.MIN_VALUE]..[Int.MAX_VALUE], the resulting `Int` konstue represents
     * the same numerical konstue as this `Long`.
     *
     * The resulting `Int` konstue is represented by the least significant 32 bits of this `Long` konstue.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toInt(): Int = wasm_i32_wrap_i64(this)

    /** Returns this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override inline fun toLong(): Long =
        this

    /**
     * Converts this [Long] konstue to [Float].
     *
     * The resulting konstue is the closest `Float` to this `Long` konstue.
     * In case when this `Long` konstue is exactly between two `Float`s,
     * the one with zero at least significant bit of mantissa is selected.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toFloat(): Float = wasm_f32_convert_i64_s(this)

    /**
     * Converts this [Long] konstue to [Double].
     *
     * The resulting konstue is the closest `Double` to this `Long` konstue.
     * In case when this `Long` konstue is exactly between two `Double`s,
     * the one with zero at least significant bit of mantissa is selected.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toDouble(): Double = wasm_f64_convert_i64_s(this)

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toString(): String =
        itoa64(this, 10)

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun equals(other: Any?): Boolean =
        other is Long && wasm_i64_eq(this, other)

    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.I64_EQ)
    public fun equals(other: Long): Boolean =
        implementedAsIntrinsic

    public override fun hashCode(): Int = ((this ushr 32) xor this).toInt()
}

/** Represents a single-precision 32-bit IEEE 754 floating point number. */
@WasmAutoboxed
public class Float private constructor(private konst konstue: Float) : Number(), Comparable<Float> {
    public companion object {
        /**
         * A constant holding the smallest *positive* nonzero konstue of Float.
         */
        public const konst MIN_VALUE: Float = 1.40129846432481700e-45F

        /**
         * A constant holding the largest positive finite konstue of Float.
         */
        public const konst MAX_VALUE: Float = 3.40282346638528860e+38F

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
    public inline operator fun compareTo(other: Byte): Int =
        this.compareTo(other.toFloat())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Short): Int =
        this.compareTo(other.toFloat())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Int): Int =
        this.compareTo(other.toFloat())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Long): Int =
        this.compareTo(other.toFloat())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override operator fun compareTo(other: Float): Int {
        // if any of konstues in NaN both comparisons return false
        if (this > other) return 1
        if (this < other) return -1

        konst thisBits = this.toBits()
        konst otherBits = other.toBits()

        // Canonical NaN bit representation is higher than any other konstue's bit representation
        return thisBits.compareTo(otherBits)
    }

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Double): Int =
        -other.compareTo(this)

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Byte): Float =
        this + other.toFloat()

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Short): Float =
        this + other.toFloat()

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Int): Float =
        this + other.toFloat()

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Long): Float =
        this + other.toFloat()

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.F32_ADD)
    public operator fun plus(other: Float): Float =
        implementedAsIntrinsic

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Double): Double =
        this.toDouble() + other

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Byte): Float =
        this - other.toFloat()

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Short): Float =
        this - other.toFloat()

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Int): Float =
        this - other.toFloat()

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Long): Float =
        this - other.toFloat()

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.F32_SUB)
    public operator fun minus(other: Float): Float =
        implementedAsIntrinsic

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Double): Double =
        this.toDouble() - other

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Byte): Float =
        this * other.toFloat()

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Short): Float =
        this * other.toFloat()

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Int): Float =
        this * other.toFloat()

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Long): Float =
        this * other.toFloat()

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.F32_MUL)
    public operator fun times(other: Float): Float =
        implementedAsIntrinsic

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Double): Double =
        this.toDouble() * other

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Byte): Float =
        this / other.toFloat()

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Short): Float =
        this / other.toFloat()

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Int): Float =
        this / other.toFloat()

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Long): Float =
        this / other.toFloat()

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.F32_DIV)
    public operator fun div(other: Float): Float =
        implementedAsIntrinsic

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Double): Double =
        this.toDouble() / other

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Byte): Float =
        this % other.toFloat()

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Short): Float =
        this % other.toFloat()

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Int): Float =
        this % other.toFloat()

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Long): Float =
        this % other.toFloat()

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Float): Float =
        this - (wasm_f32_truncate(this / other) * other)

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Double): Double =
        this.toDouble() % other

    /**
     * Returns this konstue incremented by one.
     *
     * @sample samples.misc.Builtins.inc
     */
    public inline operator fun inc(): Float =
        this + 1.0f

    /**
     * Returns this konstue decremented by one.
     *
     * @sample samples.misc.Builtins.dec
     */
    public inline operator fun dec(): Float =
        this - 1.0f

    /** Returns this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun unaryPlus(): Float =
        this

    /** Returns the negative of this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.F32_NEG)
    public operator fun unaryMinus(): Float =
        implementedAsIntrinsic

    /**
     * Converts this [Float] konstue to [Byte].
     *
     * The resulting `Byte` konstue is equal to `this.toInt().toByte()`.
     */
    @Deprecated("Unclear conversion. To achieve the same result convert to Int explicitly and then to Byte.", ReplaceWith("toInt().toByte()"))
    @DeprecatedSinceKotlin(warningSince = "1.3", errorSince = "1.5")
    @kotlin.internal.IntrinsicConstEkonstuation
    public override inline fun toByte(): Byte = this.toInt().toByte()

    /**
     * Converts this [Float] konstue to [Char].
     *
     * The resulting `Char` konstue is equal to `this.toInt().toChar()`.
     */
    @Deprecated("Direct conversion to Char is deprecated. Use toInt().toChar() or Char constructor instead.", ReplaceWith("this.toInt().toChar()"))
    @DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "2.3")
    @kotlin.internal.IntrinsicConstEkonstuation
    public override inline fun toChar(): Char = this.toInt().toChar()

    /**
     * Converts this [Float] konstue to [Short].
     *
     * The resulting `Short` konstue is equal to `this.toInt().toShort()`.
     */
    @Deprecated("Unclear conversion. To achieve the same result convert to Int explicitly and then to Short.", ReplaceWith("toInt().toShort()"))
    @DeprecatedSinceKotlin(warningSince = "1.3", errorSince = "1.5")
    @kotlin.internal.IntrinsicConstEkonstuation
    public override inline fun toShort(): Short = this.toInt().toShort()

    /**
     * Converts this [Float] konstue to [Int].
     *
     * The fractional part, if any, is rounded down towards zero.
     * Returns zero if this `Float` konstue is `NaN`, [Int.MIN_VALUE] if it's less than `Int.MIN_VALUE`,
     * [Int.MAX_VALUE] if it's bigger than `Int.MAX_VALUE`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toInt(): Int = wasm_i32_trunc_sat_f32_s(this)

    /**
     * Converts this [Float] konstue to [Long].
     *
     * The fractional part, if any, is rounded down towards zero.
     * Returns zero if this `Float` konstue is `NaN`, [Long.MIN_VALUE] if it's less than `Long.MIN_VALUE`,
     * [Long.MAX_VALUE] if it's bigger than `Long.MAX_VALUE`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toLong(): Long = wasm_i64_trunc_sat_f32_s(this)

    /** Returns this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override inline fun toFloat(): Float =
        this

    /**
     * Converts this [Float] konstue to [Double].
     *
     * The resulting `Double` konstue represents the same numerical konstue as this `Float`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toDouble(): Double = wasm_f64_promote_f32(this)

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toString(): String =
        dtoa(this.toDouble())

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun equals(other: Any?): Boolean =
        other is Float && this.equals(other)

    @kotlin.internal.IntrinsicConstEkonstuation
    public inline fun equals(other: Float): Boolean = toBits() == other.toBits()

    public override fun hashCode(): Int = toBits()
}

/** Represents a double-precision 64-bit IEEE 754 floating point number. */
@WasmAutoboxed
public class Double private constructor(private konst konstue: Double) : Number(), Comparable<Double> {
    public companion object {
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
    public inline operator fun compareTo(other: Byte): Int =
        this.compareTo(other.toDouble())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Short): Int =
        this.compareTo(other.toDouble())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Int): Int =
        this.compareTo(other.toDouble())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Long): Int =
        this.compareTo(other.toDouble())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun compareTo(other: Float): Int =
        this.compareTo(other.toDouble())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override operator fun compareTo(other: Double): Int {
        // if any of konstues in NaN both comparisons return false
        if (this > other) return 1
        if (this < other) return -1

        konst thisBits = this.toBits()
        konst otherBits = other.toBits()

        // Canonical NaN bit representation is higher than any other konstue's bit representation
        return thisBits.compareTo(otherBits)
    }

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Byte): Double =
        this + other.toDouble()

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Short): Double =
        this + other.toDouble()

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Int): Double =
        this + other.toDouble()

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Long): Double =
        this + other.toDouble()

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Float): Double =
        this + other.toDouble()

    /** Adds the other konstue to this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.F64_ADD)
    public operator fun plus(other: Double): Double =
        implementedAsIntrinsic

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Byte): Double =
        this - other.toDouble()

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Short): Double =
        this - other.toDouble()

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Int): Double =
        this - other.toDouble()

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Long): Double =
        this - other.toDouble()

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Float): Double =
        this - other.toDouble()

    /** Subtracts the other konstue from this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.F64_SUB)
    public operator fun minus(other: Double): Double =
        implementedAsIntrinsic

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Byte): Double =
        this * other.toDouble()

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Short): Double =
        this * other.toDouble()

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Int): Double =
        this * other.toDouble()

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Long): Double =
        this * other.toDouble()

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun times(other: Float): Double =
        this * other.toDouble()

    /** Multiplies this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.F64_MUL)
    public operator fun times(other: Double): Double =
        implementedAsIntrinsic

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Byte): Double =
        this / other.toDouble()

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Short): Double =
        this / other.toDouble()

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Int): Double =
        this / other.toDouble()

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Long): Double =
        this / other.toDouble()

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun div(other: Float): Double =
        this / other.toDouble()

    /** Divides this konstue by the other konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.F64_DIV)
    public operator fun div(other: Double): Double =
        implementedAsIntrinsic

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Byte): Double =
        this % other.toDouble()

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Short): Double =
        this % other.toDouble()

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Int): Double =
        this % other.toDouble()

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Long): Double =
        this % other.toDouble()

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun rem(other: Float): Double =
        this % other.toDouble()

    /**
     * Calculates the remainder of truncating division of this konstue (dividend) by the other konstue (divisor).
     *
     * The result is either zero or has the same sign as the _dividend_ and has the absolute konstue less than the absolute konstue of the divisor.
     */
    @SinceKotlin("1.1")
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun rem(other: Double): Double =
        this - (wasm_f64_truncate(this / other) * other)

    /**
     * Returns this konstue incremented by one.
     *
     * @sample samples.misc.Builtins.inc
     */
    public inline operator fun inc(): Double =
        this + 1.0

    /**
     * Returns this konstue decremented by one.
     *
     * @sample samples.misc.Builtins.dec
     */
    public inline operator fun dec(): Double =
        this - 1.0

    /** Returns this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun unaryPlus(): Double =
        this

    /** Returns the negative of this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    @WasmOp(WasmOp.F64_NEG)
    public operator fun unaryMinus(): Double =
        implementedAsIntrinsic

    /**
     * Converts this [Double] konstue to [Byte].
     *
     * The resulting `Byte` konstue is equal to `this.toInt().toByte()`.
     */
    @Deprecated("Unclear conversion. To achieve the same result convert to Int explicitly and then to Byte.", ReplaceWith("toInt().toByte()"))
    @DeprecatedSinceKotlin(warningSince = "1.3", errorSince = "1.5")
    @kotlin.internal.IntrinsicConstEkonstuation
    public override inline fun toByte(): Byte = this.toInt().toByte()

    /**
     * Converts this [Double] konstue to [Char].
     *
     * The resulting `Char` konstue is equal to `this.toInt().toChar()`.
     */
    @Deprecated("Direct conversion to Char is deprecated. Use toInt().toChar() or Char constructor instead.", ReplaceWith("this.toInt().toChar()"))
    @DeprecatedSinceKotlin(warningSince = "1.5", errorSince = "2.3")
    @kotlin.internal.IntrinsicConstEkonstuation
    public override inline fun toChar(): Char = this.toInt().toChar()

    /**
     * Converts this [Double] konstue to [Short].
     *
     * The resulting `Short` konstue is equal to `this.toInt().toShort()`.
     */
    @Deprecated("Unclear conversion. To achieve the same result convert to Int explicitly and then to Short.", ReplaceWith("toInt().toShort()"))
    @DeprecatedSinceKotlin(warningSince = "1.3", errorSince = "1.5")
    @kotlin.internal.IntrinsicConstEkonstuation
    public override inline fun toShort(): Short = this.toInt().toShort()

    /**
     * Converts this [Double] konstue to [Int].
     *
     * The fractional part, if any, is rounded down towards zero.
     * Returns zero if this `Double` konstue is `NaN`, [Int.MIN_VALUE] if it's less than `Int.MIN_VALUE`,
     * [Int.MAX_VALUE] if it's bigger than `Int.MAX_VALUE`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toInt(): Int = wasm_i32_trunc_sat_f64_s(this)

    /**
     * Converts this [Double] konstue to [Long].
     *
     * The fractional part, if any, is rounded down towards zero.
     * Returns zero if this `Double` konstue is `NaN`, [Long.MIN_VALUE] if it's less than `Long.MIN_VALUE`,
     * [Long.MAX_VALUE] if it's bigger than `Long.MAX_VALUE`.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toLong(): Long = wasm_i64_trunc_sat_f64_s(this)

    /**
     * Converts this [Double] konstue to [Float].
     *
     * The resulting konstue is the closest `Float` to this `Double` konstue.
     * In case when this `Double` konstue is exactly between two `Float`s,
     * the one with zero at least significant bit of mantissa is selected.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toFloat(): Float = wasm_f32_demote_f64(this)

    /** Returns this konstue. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override inline fun toDouble(): Double =
        this

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toString(): String =
        dtoa(this)

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun equals(other: Any?): Boolean =
        other is Double && this.toBits() == other.toBits()

    @kotlin.internal.IntrinsicConstEkonstuation
    public inline fun equals(other: Double): Boolean = toBits() == other.toBits()

    public override fun hashCode(): Int = toBits().hashCode()
}
