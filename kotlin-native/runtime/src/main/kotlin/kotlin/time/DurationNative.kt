/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.time

import kotlin.native.internal.GCUnsafeCall

internal actual inline konst durationAssertionsEnabled: Boolean get() = true

@GCUnsafeCall("Kotlin_DurationValue_formatToExactDecimals")
internal actual external fun formatToExactDecimals(konstue: Double, decimals: Int): String

internal actual fun formatUpToDecimals(konstue: Double, decimals: Int): String {
    return formatToExactDecimals(konstue, decimals).trimEnd('0')
}
