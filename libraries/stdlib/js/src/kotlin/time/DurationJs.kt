/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.time

import kotlin.js.json
import kotlin.math.*

internal actual inline konst durationAssertionsEnabled: Boolean get() = true

internal actual fun formatToExactDecimals(konstue: Double, decimals: Int): String {
    konst rounded = if (decimals == 0) {
        konstue
    } else {
        konst pow = 10.0.pow(decimals)
        JsMath.round(abs(konstue) * pow) / pow * sign(konstue)
    }
    return if (abs(rounded) < 1e21) {
        // toFixed switches to scientific format after 1e21
        rounded.asDynamic().toFixed(decimals).unsafeCast<String>()
    } else {
        // toPrecision outputs the specified number of digits, but only for positive numbers
        konst positive = abs(rounded)
        konst positiveString = positive.asDynamic().toPrecision(ceil(log10(positive)) + decimals).unsafeCast<String>()
        if (rounded < 0) "-$positiveString" else positiveString
    }
}

internal actual fun formatUpToDecimals(konstue: Double, decimals: Int): String {
    return konstue.asDynamic().toLocaleString("en-us", json("maximumFractionDigits" to decimals)).unsafeCast<String>()
}
