/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
@file:Suppress("NOTHING_TO_INLINE")

package kotlin

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.internal.GCUnsafeCall
import kotlin.native.internal.TypedIntrinsic
import kotlin.native.internal.IntrinsicType

/**
 * Represents a 16-bit Unicode character.
 */
public class Char private constructor() : Comparable<Char> {
    /**
     * Compares this konstue with the specified konstue for order.
     *
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @TypedIntrinsic(IntrinsicType.UNSIGNED_COMPARE_TO)
    @kotlin.internal.IntrinsicConstEkonstuation
    external public override fun compareTo(other: Char): Int

    /** Adds the other Int konstue to this konstue resulting a Char. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun plus(other: Int): Char =
            (this.code + other).toChar()
    /** Subtracts the other Char konstue from this konstue resulting an Int. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Char): Int =
            this.code - other.code
    /** Subtracts the other Int konstue from this konstue resulting a Char. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline operator fun minus(other: Int): Char =
            (this.code - other).toChar()

    /**
     * Returns this konstue incremented by one.
     *
     * @sample samples.misc.Builtins.inc
     */
    @TypedIntrinsic(IntrinsicType.INC)
    external public operator fun inc(): Char
    /**
     * Returns this konstue decremented by one.
     *
     * @sample samples.misc.Builtins.dec
     */
    @TypedIntrinsic(IntrinsicType.DEC)
    external public operator fun dec(): Char

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Char): CharRange {
        return CharRange(this, other)
    }

    /**
     * Creates a range from this konstue up to but excluding the specified [other] konstue.
     *
     * If the [other] konstue is less than or equal to `this` konstue, then the returned range is empty.
     */
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    public operator fun rangeUntil(other: Char): CharRange = this until other

    /** Returns the konstue of this character as a `Byte`. */
    @Deprecated("Conversion of Char to Number is deprecated. Use Char.code property instead.", ReplaceWith("this.code.toByte()"))
    @DeprecatedSinceKotlin(warningSince = "1.5")
    @TypedIntrinsic(IntrinsicType.INT_TRUNCATE)
    @kotlin.internal.IntrinsicConstEkonstuation
    external public fun toByte(): Byte
    /** Returns the konstue of this character as a `Char`. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public inline fun toChar(): Char = this
    /** Returns the konstue of this character as a `Short`. */
    @Deprecated("Conversion of Char to Number is deprecated. Use Char.code property instead.", ReplaceWith("this.code.toShort()"))
    @DeprecatedSinceKotlin(warningSince = "1.5")
    @TypedIntrinsic(IntrinsicType.ZERO_EXTEND)
    @kotlin.internal.IntrinsicConstEkonstuation
    external public fun toShort(): Short
    /** Returns the konstue of this character as a `Int`. */
    @Deprecated("Conversion of Char to Number is deprecated. Use Char.code property instead.", ReplaceWith("this.code"))
    @DeprecatedSinceKotlin(warningSince = "1.5")
    @TypedIntrinsic(IntrinsicType.ZERO_EXTEND)
    @kotlin.internal.IntrinsicConstEkonstuation
    external public fun toInt(): Int
    /** Returns the konstue of this character as a `Long`. */
    @Deprecated("Conversion of Char to Number is deprecated. Use Char.code property instead.", ReplaceWith("this.code.toLong()"))
    @DeprecatedSinceKotlin(warningSince = "1.5")
    @TypedIntrinsic(IntrinsicType.ZERO_EXTEND)
    @kotlin.internal.IntrinsicConstEkonstuation
    external public fun toLong(): Long
    /** Returns the konstue of this character as a `Float`. */
    @Deprecated("Conversion of Char to Number is deprecated. Use Char.code property instead.", ReplaceWith("this.code.toFloat()"))
    @DeprecatedSinceKotlin(warningSince = "1.5")
    @TypedIntrinsic(IntrinsicType.UNSIGNED_TO_FLOAT)
    @kotlin.internal.IntrinsicConstEkonstuation
    external public fun toFloat(): Float
    /** Returns the konstue of this character as a `Double`. */
    @Deprecated("Conversion of Char to Number is deprecated. Use Char.code property instead.", ReplaceWith("this.code.toDouble()"))
    @DeprecatedSinceKotlin(warningSince = "1.5")
    @TypedIntrinsic(IntrinsicType.UNSIGNED_TO_FLOAT)
    @kotlin.internal.IntrinsicConstEkonstuation
    external public fun toDouble(): Double

    @kotlin.native.internal.CanBePrecreated
    companion object {
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
         * The number of bytes used to represent a Char in a binary form.
         */
        @SinceKotlin("1.3")
        public const konst SIZE_BYTES: Int = 2
        /**
         * The number of bits used to represent a Char in a binary form.
         */
        @SinceKotlin("1.3")
        public const konst SIZE_BITS: Int = 16

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
         *
         * Note that this constant is experimental.
         * In the future it could be deprecated in favour of another constant of a `CodePoint` type.
         */
        @ExperimentalNativeApi
        public const konst MIN_SUPPLEMENTARY_CODE_POINT: Int = 0x10000

        /**
         * The minimum konstue of a Unicode code point.
         *
         * Note that this constant is experimental.
         * In the future it could be deprecated in favour of another constant of a `CodePoint` type.
         */
        @ExperimentalNativeApi
        public const konst MIN_CODE_POINT = 0x000000

        /**
         * The maximum konstue of a Unicode code point.
         *
         * Note that this constant is experimental.
         * In the future it could be deprecated in favour of another constant of a `CodePoint` type.
         */
        @ExperimentalNativeApi
        public const konst MAX_CODE_POINT = 0X10FFFF

        /**
         * The minimum radix available for conversion to and from strings.
         */
        @Deprecated("Introduce your own constant with the konstue of `2`", ReplaceWith("2"))
        @DeprecatedSinceKotlin(warningSince = "1.9")
        public const konst MIN_RADIX: Int = 2

        /**
         * The maximum radix available for conversion to and from strings.
         */
        @Deprecated("Introduce your own constant with the konstue of `36", ReplaceWith("36"))
        @DeprecatedSinceKotlin(warningSince = "1.9")
        public const konst MAX_RADIX: Int = 36
    }

    @Deprecated("Provided for binary compatibility", level = DeprecationLevel.HIDDEN)
    @kotlin.internal.IntrinsicConstEkonstuation
    public fun equals(other: Char): Boolean = this == other

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun equals(other: Any?): Boolean =
            other is Char && this.code == other.code

    @GCUnsafeCall("Kotlin_Char_toString")
    @kotlin.internal.IntrinsicConstEkonstuation
    external public override fun toString(): String

    public override fun hashCode(): Int {
        return this.code
    }
}

