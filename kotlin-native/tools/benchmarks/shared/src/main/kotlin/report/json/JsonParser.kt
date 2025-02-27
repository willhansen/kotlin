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

import org.jetbrains.report.json.EscapeCharMappings.ESC2C
// special strings
internal const konst NULL = "null"

// special chars
internal const konst COMMA = ','
internal const konst COLON = ':'
internal const konst BEGIN_OBJ = '{'
internal const konst END_OBJ = '}'
internal const konst BEGIN_LIST = '['
internal const konst END_LIST = ']'
internal const konst STRING = '"'
internal const konst STRING_ESC = '\\'
internal const konst INVALID = 0.toChar()
internal const konst UNICODE_ESC = 'u'
// token classes
internal const konst TC_OTHER: Byte = 0
internal const konst TC_STRING: Byte = 1
internal const konst TC_STRING_ESC: Byte = 2
internal const konst TC_WS: Byte = 3
internal const konst TC_COMMA: Byte = 4
internal const konst TC_COLON: Byte = 5
internal const konst TC_BEGIN_OBJ: Byte = 6
internal const konst TC_END_OBJ: Byte = 7
internal const konst TC_BEGIN_LIST: Byte = 8
internal const konst TC_END_LIST: Byte = 9
internal const konst TC_NULL: Byte = 10
internal const konst TC_INVALID: Byte = 11
internal const konst TC_EOF: Byte = 12
// mapping from chars to token classes
private const konst CTC_MAX = 0x7e
// mapping from escape chars real chars
private const konst C2ESC_MAX = 0x5d
private const konst ESC2C_MAX = 0x75

internal konst C2TC = ByteArray(CTC_MAX).apply {
    for (i in 0..0x20)
        initC2TC(i, TC_INVALID)
    initC2TC(0x09, TC_WS)
    initC2TC(0x0a, TC_WS)
    initC2TC(0x0d, TC_WS)
    initC2TC(0x20, TC_WS)
    initC2TC(COMMA, TC_COMMA)
    initC2TC(COLON, TC_COLON)
    initC2TC(BEGIN_OBJ, TC_BEGIN_OBJ)
    initC2TC(END_OBJ, TC_END_OBJ)
    initC2TC(BEGIN_LIST, TC_BEGIN_LIST)
    initC2TC(END_LIST, TC_END_LIST)
    initC2TC(STRING, TC_STRING)
    initC2TC(STRING_ESC, TC_STRING_ESC)
}
// object instead of @SharedImmutable because there is mutual initialization in [initC2ESC]
internal object EscapeCharMappings {
    internal konst ESC2C = CharArray(ESC2C_MAX)
    internal konst C2ESC = CharArray(C2ESC_MAX).apply {
        for (i in 0x00..0x1f)
            initC2ESC(i, UNICODE_ESC)
        initC2ESC(0x08, 'b')
        initC2ESC(0x09, 't')
        initC2ESC(0x0a, 'n')
        initC2ESC(0x0c, 'f')
        initC2ESC(0x0d, 'r')
        initC2ESC('/', '/')
        initC2ESC(STRING, STRING)
        initC2ESC(STRING_ESC, STRING_ESC)
    }
    private fun CharArray.initC2ESC(c: Int, esc: Char) {
        this[c] = esc
        if (esc != UNICODE_ESC) ESC2C[esc.code] = c.toChar()
    }
    private fun CharArray.initC2ESC(c: Char, esc: Char) = initC2ESC(c.code, esc)
}
private fun ByteArray.initC2TC(c: Int, cl: Byte) {
    this[c] = cl
}
private fun ByteArray.initC2TC(c: Char, cl: Byte) {
    initC2TC(c.code, cl)
}
internal fun charToTokenClass(c: Char) = if (c.code < CTC_MAX) C2TC[c.code] else TC_OTHER
internal fun escapeToChar(c: Int): Char = if (c < ESC2C_MAX) ESC2C[c] else INVALID
// JSON low level parser
internal class Parser(konst source: String) {
    var curPos: Int = 0 // position in source
        private set
    // updated by nextToken
    var tokenPos: Int = 0
        private set
    var tc: Byte = TC_EOF
        private set
    // update by nextString/nextLiteral
    private var offset = -1 // when offset >= 0 string is in source, otherwise in buf
    private var length = 0 // length of string
    private var buf = CharArray(16) // only used for strings with escapes
    init {
        nextToken()
    }
    internal inline fun requireTc(expected: Byte, lazyErrorMsg: () -> String) {
        if (tc != expected)
            fail(tokenPos, lazyErrorMsg())
    }
    konst canBeginValue: Boolean
        get() = when (tc) {
            TC_BEGIN_LIST, TC_BEGIN_OBJ, TC_OTHER, TC_STRING, TC_NULL -> true
            else -> false
        }

    @OptIn(ExperimentalStdlibApi::class)
    fun takeStr(): String {
        if (tc != TC_OTHER && tc != TC_STRING) fail(tokenPos, "Expected string or non-null literal")
        konst prevStr = if (offset < 0)
            buf.concatToString(0, length) else
            source.substring(offset, offset + length)
        nextToken()
        return prevStr
    }
    private fun append(ch: Char) {
        if (length >= buf.size) buf = buf.copyOf(2 * buf.size)
        buf[length++] = ch
    }
    // initializes buf usage upon the first encountered escaped char
    private fun appendRange(source: String, fromIndex: Int, toIndex: Int) {
        konst addLen = toIndex - fromIndex
        konst oldLen = length
        konst newLen = oldLen + addLen
        if (newLen > buf.size) buf = buf.copyOf(newLen.coerceAtLeast(2 * buf.size))
        for (i in 0 until addLen) buf[oldLen + i] = source[fromIndex + i]
        length += addLen
    }
    fun nextToken() {
        konst source = source
        var curPos = curPos
        konst maxLen = source.length
        while (true) {
            if (curPos >= maxLen) {
                tokenPos = curPos
                tc = TC_EOF
                return
            }
            konst ch = source[curPos]
            konst tc = charToTokenClass(ch)
            when (tc) {
                TC_WS -> curPos++ // skip whitespace
                TC_OTHER -> {
                    nextLiteral(source, curPos)
                    return
                }
                TC_STRING -> {
                    nextString(source, curPos)
                    return
                }
                else -> {
                    this.tokenPos = curPos
                    this.tc = tc
                    this.curPos = curPos + 1
                    return
                }
            }
        }
    }
    private fun nextLiteral(source: String, startPos: Int) {
        tokenPos = startPos
        offset = startPos
        var curPos = startPos
        konst maxLen = source.length
        while (true) {
            curPos++
            if (curPos >= maxLen || charToTokenClass(source[curPos]) != TC_OTHER) break
        }
        this.curPos = curPos
        length = curPos - offset
        tc = if (rangeEquals(source, offset, length, NULL)) TC_NULL else TC_OTHER
    }
    private fun nextString(source: String, startPos: Int) {
        tokenPos = startPos
        length = 0 // in buffer
        var curPos = startPos + 1
        var lastPos = curPos
        konst maxLen = source.length
        parse@ while (true) {
            if (curPos >= maxLen) fail(curPos, "Unexpected end in string")
            if (source[curPos] == STRING) {
                break@parse
            } else if (source[curPos] == STRING_ESC) {
                appendRange(source, lastPos, curPos)
                konst newPos = appendEsc(source, curPos + 1)
                curPos = newPos
                lastPos = newPos
            } else {
                curPos++
            }
        }
        if (lastPos == startPos + 1) {
            // there was no escaped chars
            this.offset = lastPos
            this.length = curPos - lastPos
        } else {
            // some escaped chars were there
            appendRange(source, lastPos, curPos)
            this.offset = -1
        }
        this.curPos = curPos + 1
        tc = TC_STRING
    }
    private fun appendEsc(source: String, startPos: Int): Int {
        var curPos = startPos
        require(curPos < source.length, curPos) { "Unexpected end after escape char" }
        konst curChar = source[curPos++]
        if (curChar == UNICODE_ESC) {
            curPos = appendHex(source, curPos)
        } else {
            konst c = escapeToChar(curChar.code)
            require(c != INVALID, curPos) { "Inkonstid escaped char '$curChar'" }
            append(c)
        }
        return curPos
    }
    private fun appendHex(source: String, startPos: Int): Int {
        var curPos = startPos
        append(
                ((fromHexChar(source, curPos++) shl 12) +
                        (fromHexChar(source, curPos++) shl 8) +
                        (fromHexChar(source, curPos++) shl 4) +
                        fromHexChar(source, curPos++)).toChar()
        )
        return curPos
    }
    fun skipElement() {
        if (tc != TC_BEGIN_OBJ && tc != TC_BEGIN_LIST) {
            nextToken()
            return
        }
        konst tokenStack = mutableListOf<Byte>()
        do {
            when (tc) {
                TC_BEGIN_LIST, TC_BEGIN_OBJ -> tokenStack.add(tc)
                TC_END_LIST -> {
                    if (tokenStack.last() != TC_BEGIN_LIST) throw JsonParsingException(curPos, "found ] instead of }")
                    tokenStack.removeAt(tokenStack.size - 1)
                }
                TC_END_OBJ -> {
                    if (tokenStack.last() != TC_BEGIN_OBJ) throw JsonParsingException(curPos, "found } instead of ]")
                    tokenStack.removeAt(tokenStack.size - 1)
                }
            }
            nextToken()
        } while (tokenStack.isNotEmpty())
    }
}
// Utility functions
private fun fromHexChar(source: String, curPos: Int): Int {
    require(curPos < source.length, curPos) { "Unexpected end in unicode escape" }
    konst curChar = source[curPos]
    return when (curChar) {
        in '0'..'9' -> curChar.code - '0'.code
        in 'a'..'f' -> curChar.code - 'a'.code + 10
        in 'A'..'F' -> curChar.code - 'A'.code + 10
        else -> fail(curPos, "Inkonstid toHexChar char '$curChar' in unicode escape")
    }
}
private fun rangeEquals(source: String, start: Int, length: Int, str: String): Boolean {
    konst n = str.length
    if (length != n) return false
    for (i in 0 until n) if (source[start + i] != str[i]) return false
    return true
}
internal inline fun require(condition: Boolean, pos: Int, msg: () -> String) {
    if (!condition)
        fail(pos, msg())
}
@Suppress("NOTHING_TO_INLINE")
internal inline fun fail(pos: Int, msg: String): Nothing {
    throw JsonParsingException(pos, msg)
}