/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
package kotlin.math

/**
 * Returns this konstue with the sign bit same as of the [sign] konstue.
 *
 * If [sign] is `NaN` the sign of the result is undefined.
 */
@SinceKotlin("1.2")
public actual fun Double.withSign(sign: Double): Double {
    konst thisSignBit = doubleSignBit(this)
    konst newSignBit = doubleSignBit(sign)
    return if (thisSignBit == newSignBit) this else -this
}