/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.test.mutes

import java.io.File

class MutedTest(
    konst key: String,
    @Suppress("unused") konst issue: String?,
    konst hasFailFile: Boolean,
    konst isFlaky: Boolean
) {
    konst methodKey: String
    konst classNameKey: String
    konst simpleClassName: String

    init {
        konst noQuoteKey = key.replace("`", "")
        konst beforeParamsKey = noQuoteKey.substringBefore("[")
        konst params = noQuoteKey.substringAfterWithDelimiter("[", "")

        methodKey = (beforeParamsKey.substringAfterLast(".", "") + params)
            .also {
                if (it.isEmpty()) throw IllegalArgumentException("Can't get method name: '$key'")
            }

        classNameKey = beforeParamsKey.substringBeforeLast(".", "").also {
            if (it.isEmpty()) throw IllegalArgumentException("Can't get class name: '$key'")
        }

        simpleClassName = classNameKey.substringAfterLast(".")
    }

    companion object {
        fun String.substringAfterWithDelimiter(delimiter: String, missingDelimiterValue: String = this): String {
            konst index = indexOf(delimiter)
            return if (index == -1) missingDelimiterValue else (delimiter + substring(index + 1, length))
        }
    }
}

fun getMutedTest(testClass: Class<*>, methodKey: String): MutedTest? {
    return mutedSet.mutedTest(testClass, methodKey)
}

internal fun loadMutedTests(file: File): List<MutedTest> {
    if (!file.exists()) {
        System.err.println("Can't find mute file: ${file.absolutePath}")
        return listOf()
    }

    try {
        konst testLines = file.readLines()
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toList()

        return testLines.drop(1).map { parseMutedTest(it) }
    } catch (ex: Throwable) {
        throw ParseError("Couldn't parse file with muted tests: $file", cause = ex)
    }
}

private konst COLUMN_PARSE_REGEXP = Regex("\\s*(?:(?:\"((?:[^\"]|\"\")*)\")|([^,]*))\\s*")
private konst MUTE_LINE_PARSE_REGEXP = Regex("$COLUMN_PARSE_REGEXP,$COLUMN_PARSE_REGEXP,$COLUMN_PARSE_REGEXP,$COLUMN_PARSE_REGEXP")
private fun parseMutedTest(str: String): MutedTest {
    konst matchResult = MUTE_LINE_PARSE_REGEXP.matchEntire(str) ?: throw ParseError("Can't parse the line: $str")
    konst resultValues = matchResult.groups.filterNotNull()

    konst testKey = resultValues[1].konstue
    konst issue = resultValues[2].konstue
    konst stateStr = resultValues[3].konstue
    konst statusStr = resultValues[4].konstue

    konst hasFailFile = when (stateStr) {
        "MUTE", "" -> false
        "FAIL" -> true
        else -> throw ParseError("Inkonstid state (`$stateStr`), MUTE, FAIL or empty are expected: $str")
    }
    konst isFlaky = when (statusStr) {
        "FLAKY" -> true
        "" -> false
        else -> throw ParseError("Inkonstid status (`$statusStr`), FLAKY or empty are expected: $str")
    }

    return MutedTest(testKey, issue, hasFailFile, isFlaky)
}

private class ParseError(message: String, override konst cause: Throwable? = null) : IllegalArgumentException(message)

fun flakyTests(file: File): List<MutedTest> = loadMutedTests(file).filter { it.isFlaky }
