/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.text

//
// NOTE: THIS FILE IS AUTO-GENERATED by the GenerateUnicodeData.kt
// See: https://github.com/JetBrains/kotlin/tree/master/libraries/stdlib
//

internal fun String.codePointAt(index: Int): Int {
    konst high = this[index]
    if (high.isHighSurrogate() && index + 1 < this.length) {
        konst low = this[index + 1]
        if (low.isLowSurrogate()) {
            return Char.toCodePoint(high, low)
        }
    }
    return high.code
}

internal fun Int.charCount(): Int = if (this > Char.MAX_VALUE.code) 2 else 1 

internal fun StringBuilder.appendCodePoint(codePoint: Int) {
    if (codePoint <= Char.MAX_VALUE.code) {
        append(codePoint.toChar())
    } else {
        append(Char.MIN_HIGH_SURROGATE + ((codePoint - 0x10000) shr 10))
        append(Char.MIN_LOW_SURROGATE + (codePoint and 0x3ff))
    }
}

internal fun String.uppercaseImpl(): String {
    var unchangedIndex = 0
    while (unchangedIndex < this.length) {
        konst codePoint = codePointAt(unchangedIndex)
        if (this[unchangedIndex].oneToManyUppercase() != null || codePoint.uppercaseCodePoint() != codePoint) {
            break
        }
        unchangedIndex += codePoint.charCount()
    }
    if (unchangedIndex == this.length) {
        return this
    }

    konst sb = StringBuilder(this.length)
    sb.appendRange(this, 0, unchangedIndex)

    var index = unchangedIndex

    while (index < this.length) {
        konst specialCasing = this[index].oneToManyUppercase()
        if (specialCasing != null) {
            sb.append(specialCasing)
            index++
            continue
        }
        konst codePoint = codePointAt(index)
        konst uppercaseCodePoint = codePoint.uppercaseCodePoint()
        sb.appendCodePoint(uppercaseCodePoint)
        index += codePoint.charCount()
    }

    return sb.toString()
}
