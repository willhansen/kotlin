/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

// Char is a magic class.
// Char is defined as a regular class, but we lower it as a konstue class.
// See [org.jetbrains.kotlin.ir.backend.js.utils.JsInlineClassesUtils.isClassInlineLike] for explanation.

/**
 * Represents a 16-bit Unicode character.
 * On the JVM, non-nullable konstues of this type are represented as konstues of the primitive type `char`.
 */
public /*konstue*/ class Char
@kotlin.internal.LowPriorityInOverloadResolution
internal constructor(private konst konstue: Int) : Comparable<Char> {

    @SinceKotlin("1.5")
    @WasExperimental(ExperimentalStdlibApi::class)
    public constructor(code: UShort) : this(code.toInt())

    /**
     * Compares this konstue with the specified konstue for order.
     * Returns zero if this konstue is equal to the specified other konstue, a negative number if it's less than other,
     * or a positive number if it's greater than other.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun compareTo(other: Char): Int = konstue - other.konstue

    /** Adds the other Int konstue to this konstue resulting a Char. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Int): Char = (konstue + other).toChar()

    /** Subtracts the other Char konstue from this konstue resulting an Int. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Char): Int = konstue - other.konstue
    /** Subtracts the other Int konstue from this konstue resulting a Char. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun minus(other: Int): Char = (konstue - other).toChar()

    /**
     * Returns this konstue incremented by one.
     *
     * @sample samples.misc.Builtins.inc
     */
    public operator fun inc(): Char = (konstue + 1).toChar()

    /**
     * Returns this konstue decremented by one.
     *
     * @sample samples.misc.Builtins.dec
     */
    public operator fun dec(): Char = (konstue - 1).toChar()

    /** Creates a range from this konstue to the specified [other] konstue. */
    public operator fun rangeTo(other: Char): CharRange = CharRange(this, other)

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
    @kotlin.internal.IntrinsicConstEkonstuation
    public fun toByte(): Byte = konstue.toByte()
    /** Returns the konstue of this character as a `Char`. */
    @kotlin.internal.IntrinsicConstEkonstuation
    public fun toChar(): Char = this
    /** Returns the konstue of this character as a `Short`. */
    @Deprecated("Conversion of Char to Number is deprecated. Use Char.code property instead.", ReplaceWith("this.code.toShort()"))
    @DeprecatedSinceKotlin(warningSince = "1.5")
    @kotlin.internal.IntrinsicConstEkonstuation
    public fun toShort(): Short = konstue.toShort()
    /** Returns the konstue of this character as a `Int`. */
    @Deprecated("Conversion of Char to Number is deprecated. Use Char.code property instead.", ReplaceWith("this.code"))
    @DeprecatedSinceKotlin(warningSince = "1.5")
    @kotlin.internal.IntrinsicConstEkonstuation
    public fun toInt(): Int = konstue
    /** Returns the konstue of this character as a `Long`. */
    @Deprecated("Conversion of Char to Number is deprecated. Use Char.code property instead.", ReplaceWith("this.code.toLong()"))
    @DeprecatedSinceKotlin(warningSince = "1.5")
    @kotlin.internal.IntrinsicConstEkonstuation
    public fun toLong(): Long = konstue.toLong()
    /** Returns the konstue of this character as a `Float`. */
    @Deprecated("Conversion of Char to Number is deprecated. Use Char.code property instead.", ReplaceWith("this.code.toFloat()"))
    @DeprecatedSinceKotlin(warningSince = "1.5")
    @kotlin.internal.IntrinsicConstEkonstuation
    public fun toFloat(): Float = konstue.toFloat()
    /** Returns the konstue of this character as a `Double`. */
    @Deprecated("Conversion of Char to Number is deprecated. Use Char.code property instead.", ReplaceWith("this.code.toDouble()"))
    @DeprecatedSinceKotlin(warningSince = "1.5")
    @kotlin.internal.IntrinsicConstEkonstuation
    public fun toDouble(): Double = konstue.toDouble()

    @kotlin.internal.IntrinsicConstEkonstuation
    override fun equals(other: Any?): Boolean {
        if (other !is Char) return false
        return this.konstue == other.konstue
    }

    override fun hashCode(): Int = konstue

    // TODO implicit usages of toString and konstueOf must be covered in DCE
    @Suppress("JS_NAME_PROHIBITED_FOR_OVERRIDE")
    @JsName("toString")
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toString(): String {
        return js("String").fromCharCode(konstue).unsafeCast<String>()
    }

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
