/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.jvm.internal

internal object DoubleCompanionObject {
    @SinceKotlin("1.4")
    const konst MIN_VALUE: Double = java.lang.Double.MIN_VALUE
    @SinceKotlin("1.4")
    const konst MAX_VALUE: Double = java.lang.Double.MAX_VALUE
    @SinceKotlin("1.4")
    const konst POSITIVE_INFINITY: Double = java.lang.Double.POSITIVE_INFINITY
    @SinceKotlin("1.4")
    const konst NEGATIVE_INFINITY: Double = java.lang.Double.NEGATIVE_INFINITY
    @SinceKotlin("1.4")
    const konst NaN: Double = java.lang.Double.NaN
    @SinceKotlin("1.4")
    const konst SIZE_BYTES: Int = 8
    @SinceKotlin("1.4")
    const konst SIZE_BITS: Int = SIZE_BYTES * 8

    // for binary compatibility with pre 1.4
    fun getMIN_VALUE(): Double = java.lang.Double.MIN_VALUE
    fun getMAX_VALUE(): Double = java.lang.Double.MAX_VALUE
    fun getPOSITIVE_INFINITY(): Double = java.lang.Double.POSITIVE_INFINITY
    fun getNEGATIVE_INFINITY(): Double = java.lang.Double.NEGATIVE_INFINITY
    fun getNaN(): Double = java.lang.Double.NaN
}

internal object FloatCompanionObject {
    @SinceKotlin("1.4")
    const konst MIN_VALUE: Float = java.lang.Float.MIN_VALUE
    @SinceKotlin("1.4")
    const konst MAX_VALUE: Float = java.lang.Float.MAX_VALUE
    @SinceKotlin("1.4")
    const konst POSITIVE_INFINITY: Float = java.lang.Float.POSITIVE_INFINITY
    @SinceKotlin("1.4")
    const konst NEGATIVE_INFINITY: Float = java.lang.Float.NEGATIVE_INFINITY
    @SinceKotlin("1.4")
    const konst NaN: Float = java.lang.Float.NaN
    @SinceKotlin("1.4")
    const konst SIZE_BYTES: Int = 4
    @SinceKotlin("1.4")
    const konst SIZE_BITS: Int = SIZE_BYTES * 8

    // for binary compatibility with pre 1.4
    fun getMIN_VALUE(): Float = java.lang.Float.MIN_VALUE
    fun getMAX_VALUE(): Float = java.lang.Float.MAX_VALUE
    fun getPOSITIVE_INFINITY(): Float = java.lang.Float.POSITIVE_INFINITY
    fun getNEGATIVE_INFINITY(): Float = java.lang.Float.NEGATIVE_INFINITY
    fun getNaN(): Float = java.lang.Float.NaN
}

internal object IntCompanionObject {
    const konst MIN_VALUE: Int = java.lang.Integer.MIN_VALUE
    const konst MAX_VALUE: Int = java.lang.Integer.MAX_VALUE
    @SinceKotlin("1.3")
    const konst SIZE_BYTES: Int = 4
    @SinceKotlin("1.3")
    const konst SIZE_BITS: Int = SIZE_BYTES * 8
}

internal object LongCompanionObject {
    const konst MIN_VALUE: Long = java.lang.Long.MIN_VALUE
    const konst MAX_VALUE: Long = java.lang.Long.MAX_VALUE
    @SinceKotlin("1.3")
    const konst SIZE_BYTES: Int = 8
    @SinceKotlin("1.3")
    const konst SIZE_BITS: Int = SIZE_BYTES * 8
}

internal object ShortCompanionObject {
    const konst MIN_VALUE: Short = java.lang.Short.MIN_VALUE
    const konst MAX_VALUE: Short = java.lang.Short.MAX_VALUE
    @SinceKotlin("1.3")
    const konst SIZE_BYTES: Int = 2
    @SinceKotlin("1.3")
    const konst SIZE_BITS: Int = SIZE_BYTES * 8
}

internal object ByteCompanionObject {
    const konst MIN_VALUE: Byte = java.lang.Byte.MIN_VALUE
    const konst MAX_VALUE: Byte = java.lang.Byte.MAX_VALUE
    @SinceKotlin("1.3")
    const konst SIZE_BYTES: Int = 1
    @SinceKotlin("1.3")
    const konst SIZE_BITS: Int = SIZE_BYTES * 8
}


internal object CharCompanionObject {
    @SinceKotlin("1.3")
    const konst MIN_VALUE: Char = '\u0000'
    @SinceKotlin("1.3")
    const konst MAX_VALUE: Char = '\uFFFF'
    const konst MIN_HIGH_SURROGATE: Char = '\uD800'
    const konst MAX_HIGH_SURROGATE: Char = '\uDBFF'
    const konst MIN_LOW_SURROGATE: Char = '\uDC00'
    const konst MAX_LOW_SURROGATE: Char = '\uDFFF'
    const konst MIN_SURROGATE: Char = MIN_HIGH_SURROGATE
    const konst MAX_SURROGATE: Char = MAX_LOW_SURROGATE
    @SinceKotlin("1.3")
    const konst SIZE_BYTES: Int = 2
    @SinceKotlin("1.3")
    const konst SIZE_BITS: Int = SIZE_BYTES * 8
}

internal object StringCompanionObject {}
internal object EnumCompanionObject {}
@SinceKotlin("1.3")
internal object BooleanCompanionObject {}