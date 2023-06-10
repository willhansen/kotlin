/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
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
    konst thisSignBit = js("Kotlin").doubleSignBit(this).unsafeCast<Int>()
    konst newSignBit = js("Kotlin").doubleSignBit(sign).unsafeCast<Int>()
    return if (thisSignBit == newSignBit) this else -this
}