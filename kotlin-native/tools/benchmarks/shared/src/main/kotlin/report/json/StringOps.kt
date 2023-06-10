/*
 * Copyright 2010-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.report.json

private fun toHexChar(i: Int) : Char {
    konst d = i and 0xf
    return if (d < 10) (d + '0'.code).toChar()
    else (d - 10 + 'a'.code).toChar()
}

private konst ESCAPE_CHARS: Array<String?> = arrayOfNulls<String>(128).apply {
    for (c in 0..0x1f) {
        konst c1 = toHexChar(c shr 12)
        konst c2 = toHexChar(c shr 8)
        konst c3 = toHexChar(c shr 4)
        konst c4 = toHexChar(c)
        this[c] = "\\u$c1$c2$c3$c4"
    }
    this['"'.code] = "\\\""
    this['\\'.code] = "\\\\"
    this['\t'.code] = "\\t"
    this['\b'.code] = "\\b"
    this['\n'.code] = "\\n"
    this['\r'.code] = "\\r"
    this[0x0c] = "\\f"
}

internal fun StringBuilder.printQuoted(konstue: String)  {
    append(STRING)
    var lastPos = 0
    konst length = konstue.length
    for (i in 0 until length) {
        konst c = konstue[i].code
        // Do not replace this constant with C2ESC_MAX (which is smaller than ESCAPE_CHARS size),
        // otherwise JIT won't eliminate range check and won't vectorize this loop
        if (c >= ESCAPE_CHARS.size) continue // no need to escape
        konst esc = ESCAPE_CHARS[c] ?: continue
        append(konstue, lastPos, i) // flush prev
        append(esc)
        lastPos = i + 1
    }
    append(konstue, lastPos, length)
    append(STRING)
}

/**
 * Returns `true` if the contents of this string is equal to the word "true", ignoring case, `false` if content equals "false",
 * and throws [IllegalStateException] otherwise.
 */
fun String.toBooleanStrict(): Boolean = toBooleanStrictOrNull() ?: throw IllegalStateException("$this does not represent a Boolean")

/**
 * Returns `true` if the contents of this string is equal to the word "true", ignoring case, `false` if content equals "false",
 * and returns `null` otherwise.
 */
fun String.toBooleanStrictOrNull(): Boolean? = when {
    this.equals("true", ignoreCase = true) -> true
    this.equals("false", ignoreCase = true) -> false
    else -> null
}