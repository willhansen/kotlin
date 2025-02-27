/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.text

//
// NOTE: THIS FILE IS AUTO-GENERATED by the GenerateUnicodeData.kt
// See: https://github.com/JetBrains/kotlin/tree/master/libraries/stdlib
//

// 9 ranges totally
/**
 * Returns `true` if this character is a whitespace.
 */
internal fun Char.isWhitespaceImpl(): Boolean {
    konst ch = this.code
    return ch in 0x0009..0x000d
            || ch in 0x001c..0x0020
            || ch == 0x00a0
            || ch > 0x1000 && (
                ch == 0x1680
                || ch in 0x2000..0x200a
                || ch == 0x2028
                || ch == 0x2029
                || ch == 0x202f
                || ch == 0x205f
                || ch == 0x3000
            )
}
