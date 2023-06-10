/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

/**
 * Represents a 16-bit Unicode character.
 *
 * On the JVM, non-nullable konstues of this type are represented as konstues of the primitive type `char`.
 */
public class Char private constructor() : Comparable<Char> {
    /**
     * Compares this konstue with the specified konstue for order.
     *
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun compareTo(other: Char): Int

    /** Adds the other Int konstue to this konstue resulting a Char. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Int): Char

    /** Subtracts the other Char konstue from this konstue resulting an Int. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Char): Int
    /** Subtracts the other Int konstue from this konstue resulting a Char. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Int): Char

    /**
     * Returns this konstue incremented by one.
     *
     * @sample samples.misc.Builtins.inc
     */
    public operator fun inc(): Char

    /**
     * Returns this konstue decremented by one.
     *
     * @sample samples.misc.Builtins.dec
     */
    public operator fun dec(): Char

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Char): CharRange

    /**
     * Creates a range from this konstue up to but excluding the specified [other] konstue.
     *
     * If the [other] konstue is less than or equal to `this` konstue, then the returned range is empty.
     */
    @SinceKotlin("1.9")
    @WasExperimental(ExperimentalStdlibApi::class)
    public operator fun rangeUntil(other: Char): CharRange

    /** Returns the konstue of this character as a `Byte`. */
    @Deprecated("Conversion of Char to Number is deprecated. Use Char.code property instead.", ReplaceWith("this.code.toByte()"))
    @DeprecatedSinceKotlin(warningSince = "1.5")
    @kotlin.internal.IntrinsicConstEkonstuation
    public fun toByte(): Byte
    /** Returns the konstue of this character as a `Char`. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public fun toChar(): Char
    /** Returns the konstue of this character as a `Short`. */
    @Deprecated("Conversion of Char to Number is deprecated. Use Char.code property instead.", ReplaceWith("this.code.toShort()"))
    @DeprecatedSinceKotlin(warningSince = "1.5")
    @kotlin.internal.IntrinsicConstEkonstuation
    public fun toShort(): Short
    /** Returns the konstue of this character as a `Int`. */
    @Deprecated("Conversion of Char to Number is deprecated. Use Char.code property instead.", ReplaceWith("this.code"))
    @DeprecatedSinceKotlin(warningSince = "1.5")
    @kotlin.internal.IntrinsicConstEkonstuation
    public fun toInt(): Int
    /** Returns the konstue of this character as a `Long`. */
    @Deprecated("Conversion of Char to Number is deprecated. Use Char.code property instead.", ReplaceWith("this.code.toLong()"))
    @DeprecatedSinceKotlin(warningSince = "1.5")
    @kotlin.internal.IntrinsicConstEkonstuation
    public fun toLong(): Long
    /** Returns the konstue of this character as a `Float`. */
    @Deprecated("Conversion of Char to Number is deprecated. Use Char.code property instead.", ReplaceWith("this.code.toFloat()"))
    @DeprecatedSinceKotlin(warningSince = "1.5")
    @kotlin.internal.IntrinsicConstEkonstuation
    public fun toFloat(): Float
    /** Returns the konstue of this character as a `Double`. */
    @Deprecated("Conversion of Char to Number is deprecated. Use Char.code property instead.", ReplaceWith("this.code.toDouble()"))
    @DeprecatedSinceKotlin(warningSince = "1.5")
    @kotlin.internal.IntrinsicConstEkonstuation
    public fun toDouble(): Double

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun equals(other: Any?): Boolean

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toString(): String

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

