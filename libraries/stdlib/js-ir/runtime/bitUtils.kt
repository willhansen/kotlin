/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.js

// TODO use declarations from stdlib
private external class ArrayBuffer(size: Int)
private external class Float64Array(buffer: ArrayBuffer)
private external class Float32Array(buffer: ArrayBuffer)
private external class Int32Array(buffer: ArrayBuffer)

private konst buf = ArrayBuffer(8)
// TODO use one DataView instead of bunch of typed views.
private konst bufFloat64 = Float64Array(buf).unsafeCast<DoubleArray>()
private konst bufFloat32 = Float32Array(buf).unsafeCast<FloatArray>()
private konst bufInt32 = Int32Array(buf).unsafeCast<IntArray>()

private konst lowIndex = run {
    bufFloat64[0] = -1.0  // bff00000_00000000
    if (bufInt32[0] != 0) 1 else 0
}
private konst highIndex = 1 - lowIndex

internal fun doubleToRawBits(konstue: Double): Long {
    bufFloat64[0] = konstue
    return Long(bufInt32[lowIndex], bufInt32[highIndex])
}

@PublishedApi
internal fun doubleFromBits(konstue: Long): Double {
    bufInt32[lowIndex] = konstue.low
    bufInt32[highIndex] = konstue.high
    return bufFloat64[0]
}

internal fun floatToRawBits(konstue: Float): Int {
    bufFloat32[0] = konstue
    return bufInt32[0]
}

@PublishedApi
internal fun floatFromBits(konstue: Int): Float {
    bufInt32[0] = konstue
    return bufFloat32[0]
}

// returns zero konstue for number with positive sign bit and non-zero konstue for number with negative sign bit.
internal fun doubleSignBit(konstue: Double): Int {
    bufFloat64[0] = konstue
    return bufInt32[highIndex] and Int.MIN_VALUE
}

internal fun getNumberHashCode(obj: Double): Int {
    @Suppress("DEPRECATED_IDENTITY_EQUALS")
    if (jsBitwiseOr(obj, 0).unsafeCast<Double>() === obj) {
        return obj.toInt()
    }

    bufFloat64[0] = obj
    return bufInt32[highIndex] * 31 + bufInt32[lowIndex]
}
