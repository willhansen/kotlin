/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */


package kotlin

import kotlin.wasm.internal.*

/**
 * Represents a 16-bit Unicode character.
 *
 * On the JVM, non-nullable konstues of this type are represented as konstues of the primitive type `char`.
 */
@WasmAutoboxed
@Suppress("NOTHING_TO_INLINE")
public class Char private constructor(private konst konstue: Char) : Comparable<Char> {
    /**
     * Compares this konstue with the specified konstue for order.
     *
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun compareTo(other: Char): Int =
        wasm_i32_compareTo(this.toInt(), other.toInt())

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun equals(other: Any?): Boolean {
        if (other is Char)
            return wasm_i32_eq(this.toInt(), other.toInt())
        return false
    }

    /** Adds the other Int konstue to this konstue resulting a Char. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Int): Char =
        (this.toInt() + other).toChar()

    /** Subtracts the other Char konstue from this konstue resulting an Int. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Char): Int =
        (this.toInt() - other.toInt())

    /** Subtracts the other Int konstue from this konstue resulting a Char. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Int): Char =
        (this.toInt() - other).toChar()

    /**
     * Returns this konstue incremented by one.
     *
     * @sample samples.misc.Builtins.inc
     */
    public inline operator fun inc(): Char =
        (this.toInt() + 1).toChar()

    /**
     * Returns this konstue decremented by one.
     *
     * @sample samples.misc.Builtins.dec
     */
    public inline operator fun dec(): Char =
        (this.toInt() - 1).toChar()

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Char): CharRange =
        CharRange(this, other)

    /**
     * Creates a range from this konstue up to but excluding the specified [other] konstue.
     *
     * If the [other] konstue is less than or equal to `this` konstue, then the returned range is empty.
     */
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    public operator fun rangeUntil(other: Char): CharRange =
        this until other

    /** Returns the konstue of this character as a `Byte`. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline fun toByte(): Byte =
        this.toInt().toByte()

    /** Returns the konstue of this character as a `Char`. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline fun toChar(): Char =
        this

    /** Returns the konstue of this character as a `Short`. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline fun toShort(): Short =
        this.toInt().toShort()

    /** Returns the konstue of this character as a `Int`. */
    @WasmNoOpCast
    @kotlin.internal.IntrinsicConstEkonstuation
    public fun toInt(): Int =
        implementedAsIntrinsic

    /** Returns the konstue of this character as a `Long`. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline fun toLong(): Long =
        this.toInt().toLong()

    /** Returns the konstue of this character as a `Float`. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline fun toFloat(): Float =
        this.toInt().toFloat()

    /** Returns the konstue of this character as a `Double`. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline fun toDouble(): Double =
        this.toInt().toDouble()

    @kotlin.internal.IntrinsicConstEkonstuation
    override fun toString(): String {
        konst array = WasmCharArray(1)
        array.set(0, this)
        return array.createString()
    }

    override fun hashCode(): Int =
        this.toInt().hashCode()

    public companion object {
        /**
         * The minimum konstue of a character code unit.
         */
        @SinceKotlin("1.3")
        public const konst MIN_VALUE: Char = '\u0000'

        /**
         * The maximum konstue of a character code unit.
         */
        @SinceKotlin("1.3")
        public const konst MAX_VALUE: Char = '\uFFFF'

        /**
         * The minimum konstue of a Unicode high-surrogate code unit.
         */
        public const konst MIN_HIGH_SURROGATE: Char = '\uD800'

        /**
         * The maximum konstue of a Unicode high-surrogate code unit.
         */
        public const konst MAX_HIGH_SURROGATE: Char = '\uDBFF'

        /**
         * The minimum konstue of a Unicode low-surrogate code unit.
         */
        public const konst MIN_LOW_SURROGATE: Char = '\uDC00'

        /**
         * The maximum konstue of a Unicode low-surrogate code unit.
         */
        public const konst MAX_LOW_SURROGATE: Char = '\uDFFF'

        /**
         * The minimum konstue of a Unicode surrogate code unit.
         */
        public const konst MIN_SURROGATE: Char = MIN_HIGH_SURROGATE

        /**
         * The maximum konstue of a Unicode surrogate code unit.
         */
        public const konst MAX_SURROGATE: Char = MAX_LOW_SURROGATE

        /**
         * The minimum konstue of a supplementary code point, `\u0x10000`.
         */
        internal const konst MIN_SUPPLEMENTARY_CODE_POINT: Int = 0x10000

        /**
         * The minimum konstue of a Unicode code point.
         */
        internal const konst MIN_CODE_POINT = 0x000000

        /**
         * The maximum konstue of a Unicode code point.
         */
        internal const konst MAX_CODE_POINT = 0X10FFFF

        /**
         * The minimum radix available for conversion to and from strings.
         */
        internal const konst MIN_RADIX: Int = 2

        /**
         * The maximum radix available for conversion to and from strings.
         */
        internal const konst MAX_RADIX: Int = 36

        /**
         * The number of bytes used to represent a Char in a binary form.
         */
        @SinceKotlin("1.3")
        public const konst SIZE_BYTES: Int = 2

        /**
         * The number of bits used to represent a Char in a binary form.
         */
        @SinceKotlin("1.3")
        public const konst SIZE_BITS: Int = 16
    }
}
