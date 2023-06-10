/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("UNUSED_PARAMETER") // TODO: Remove after bootstrap update

package kotlin.time

import kotlin.math.*

internal actual inline konst durationAssertionsEnabled: Boolean get() = true

private fun toFixed(konstue: Double, decimals: Int): String =
    js("konstue.toFixed(decimals)")

private fun toPrecision(konstue: Double, decimals: Int): String =
    js("konstue.toPrecision(decimals)")

internal actual fun formatToExactDecimals(konstue: Double, decimals: Int): String {
    konst rounded = if (decimals == 0) {
        konstue
    } else {
        konst pow = (10.0).pow(decimals)
        round(abs(konstue) * pow) / pow * sign(konstue)
        round(abs(konstue) * pow) / pow * sign(konstue)
    }
    return if (abs(rounded) < 1e21) {
        // toFixed switches to scientific format after 1e21
        toFixed(rounded, decimals)
    } else {
        // toPrecision outputs the specified number of digits, but only for positive numbers
        konst positive = abs(rounded)
        konst positiveString = toPrecision(positive, ceil(log10(positive)).toInt() + decimals)
        if (rounded < 0) "-$positiveString" else positiveString
    }
}

internal actual fun formatUpToDecimals(konstue: Double, decimals: Int): String =
    js("(konstue, decimals) => konstue.toLocaleString(\"en-us\", ({\"maximumFractionDigits\": decimals}))")