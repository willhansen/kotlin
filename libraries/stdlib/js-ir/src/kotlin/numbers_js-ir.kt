/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

/**
 * Returns a bit representation of the specified floating-point konstue as [Long]
 * according to the IEEE 754 floating-point "double format" bit layout.
 */
@SinceKotlin("1.2")
public actual fun Double.toBits(): Long =
    doubleToRawBits(if (this.isNaN()) Double.NaN else this)

/**
 * Returns a bit representation of the specified floating-point konstue as [Long]
 * according to the IEEE 754 floating-point "double format" bit layout,
 * preserving `NaN` konstues exact layout.
 */
@SinceKotlin("1.2")
public actual fun Double.toRawBits(): Long =
    doubleToRawBits(this)

/**
 * Returns the [Double] konstue corresponding to a given bit representation.
 */
@SinceKotlin("1.2")
@kotlin.internal.InlineOnly
public actual inline fun Double.Companion.fromBits(bits: Long): Double =
    doubleFromBits(bits)

/**
 * Returns a bit representation of the specified floating-point konstue as [Int]
 * according to the IEEE 754 floating-point "single format" bit layout.
 *
 * Note that in Kotlin/JS [Float] range is wider than "single format" bit layout can represent,
 * so some [Float] konstues may overflow, underflow or loose their accuracy after conversion to bits and back.
 */
@SinceKotlin("1.2")
public actual fun Float.toBits(): Int =
    floatToRawBits(if (this.isNaN()) Float.NaN else this)

/**
 * Returns a bit representation of the specified floating-point konstue as [Int]
 * according to the IEEE 754 floating-point "single format" bit layout,
 * preserving `NaN` konstues exact layout.
 *
 * Note that in Kotlin/JS [Float] range is wider than "single format" bit layout can represent,
 * so some [Float] konstues may overflow, underflow or loose their accuracy after conversion to bits and back.
 */
@SinceKotlin("1.2")
public actual fun Float.toRawBits(): Int =
    floatToRawBits(this)

/**
 * Returns the [Float] konstue corresponding to a given bit representation.
 */
@SinceKotlin("1.2")
@kotlin.internal.InlineOnly
public actual inline fun Float.Companion.fromBits(bits: Int): Float =
    floatFromBits(bits)