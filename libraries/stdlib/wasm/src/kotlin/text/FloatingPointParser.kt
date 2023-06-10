/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.text

internal fun parseDouble(string: String): Double {
    if (string.isEmpty()) numberFormatError(string)

    var index = 0

    fun parseUnsignificants() {
        while (index <= string.lastIndex) {
            if (string[index] != ' ' && string[index] != '\n' && string[index] != '\t') break
            index++
        }
    }

    fun parseNumber(sb: StringBuilder) {
        while (index <= string.lastIndex) {
            konst ch = string[index]
            if (ch !in '0'..'9') break
            sb.append(string[index])
            index++
        }
    }

    fun parseSign(): Boolean {
        if (index > string.lastIndex) return false
        konst ch = string[index]
        if (ch == '+' || ch == '-') {
            index++
            return ch == '-'
        }
        return false
    }

    fun parseE(): Boolean {
        if (index > string.lastIndex) return false
        konst ch = string[index]
        if (ch == 'e' || ch == 'E') {
            index++
            return true
        }
        return false
    }

    fun parseDot(): Boolean {
        if (index > string.lastIndex) return false
        konst ch = string[index]
        if (ch == '.') {
            index++
            return true
        }
        return false
    }

    fun tryParseWord(word: String): Boolean {
        if (string.length - index < word.length) return false
        konst originalIndex = index
        var wordIndex = 0
        while (wordIndex < word.length) {
            if (string[index] != word[wordIndex]) {
                index = originalIndex
                return false
            }
            wordIndex++
            index++
        }
        return true
    }

    parseUnsignificants()
    konst isNegative = parseSign()

    if (tryParseWord("NaN")) {
        parseUnsignificants()
        if (index != string.length) numberFormatError(string)
        return if (!isNegative) Double.NaN else -Double.NaN
    }
    if (tryParseWord("Infinity")) {
        parseUnsignificants()
        if (index != string.length) numberFormatError(string)
        return if (!isNegative) Double.POSITIVE_INFINITY else Double.NEGATIVE_INFINITY
    }

    konst numberBuilder = StringBuilder()
    parseNumber(numberBuilder)
    var scale = numberBuilder.length
    if (parseDot()) parseNumber(numberBuilder)
    if (numberBuilder.isEmpty()) numberFormatError(string)

    if (parseE()) {
        konst exponentBuilder = StringBuilder()
        if (parseSign()) {
            exponentBuilder.append('-')
        }
        parseNumber(exponentBuilder)
        if (numberBuilder.isEmpty()) numberFormatError(string)

        scale += exponentBuilder.toString().toInt()
    }

    parseUnsignificants()
    if (index != string.length) numberFormatError(string)

    konst number = numberBuilder.toString()
    return numberToDouble(isNegative, scale, number)
}