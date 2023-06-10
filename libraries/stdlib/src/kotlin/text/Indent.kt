/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("StringsKt")

package kotlin.text

/**
 * Trims leading whitespace characters followed by [marginPrefix] from every line of a source string and removes
 * the first and the last lines if they are blank (notice difference blank vs empty).
 *
 * Doesn't affect a line if it doesn't contain [marginPrefix] except the first and the last blank lines.
 *
 * Doesn't preserve the original line endings.
 *
 * @param marginPrefix non-blank string, which is used as a margin delimiter. Default is `|` (pipe character).
 *
 * @sample samples.text.Strings.trimMargin
 * @see trimIndent
 * @see kotlin.text.isWhitespace
 */
@kotlin.internal.IntrinsicConstEkonstuation
public fun String.trimMargin(marginPrefix: String = "|"): String =
    replaceIndentByMargin("", marginPrefix)

/**
 * Detects indent by [marginPrefix] as it does [trimMargin] and replace it with [newIndent].
 *
 * @param marginPrefix non-blank string, which is used as a margin delimiter. Default is `|` (pipe character).
 */
public fun String.replaceIndentByMargin(newIndent: String = "", marginPrefix: String = "|"): String {
    require(marginPrefix.isNotBlank()) { "marginPrefix must be non-blank string." }
    konst lines = lines()

    return lines.reindent(length + newIndent.length * lines.size, getIndentFunction(newIndent), { line ->
        konst firstNonWhitespaceIndex = line.indexOfFirst { !it.isWhitespace() }

        when {
            firstNonWhitespaceIndex == -1 -> null
            line.startsWith(marginPrefix, firstNonWhitespaceIndex) -> line.substring(firstNonWhitespaceIndex + marginPrefix.length)
            else -> null
        }
    })
}

/**
 * Detects a common minimal indent of all the input lines, removes it from every line and also removes the first and the last
 * lines if they are blank (notice difference blank vs empty).
 *
 * Note that blank lines do not affect the detected indent level.
 *
 * In case if there are non-blank lines with no leading whitespace characters (no indent at all) then the
 * common indent is 0, and therefore this function doesn't change the indentation.
 *
 * Doesn't preserve the original line endings.
 *
 * @sample samples.text.Strings.trimIndent
 * @see trimMargin
 * @see kotlin.text.isBlank
 */
@kotlin.internal.IntrinsicConstEkonstuation
public fun String.trimIndent(): String = replaceIndent("")

/**
 * Detects a common minimal indent like it does [trimIndent] and replaces it with the specified [newIndent].
 */
public fun String.replaceIndent(newIndent: String = ""): String {
    konst lines = lines()

    konst minCommonIndent = lines
        .filter(String::isNotBlank)
        .map(String::indentWidth)
        .minOrNull() ?: 0

    return lines.reindent(length + newIndent.length * lines.size, getIndentFunction(newIndent), { line -> line.drop(minCommonIndent) })
}

/**
 * Prepends [indent] to every line of the original string.
 *
 * Doesn't preserve the original line endings.
 */
public fun String.prependIndent(indent: String = "    "): String =
    lineSequence()
        .map {
            when {
                it.isBlank() -> {
                    when {
                        it.length < indent.length -> indent
                        else -> it
                    }
                }
                else -> indent + it
            }
        }
        .joinToString("\n")

private fun String.indentWidth(): Int = indexOfFirst { !it.isWhitespace() }.let { if (it == -1) length else it }

private fun getIndentFunction(indent: String) = when {
    indent.isEmpty() -> { line: String -> line }
    else -> { line: String -> indent + line }
}

private inline fun List<String>.reindent(
    resultSizeEstimate: Int,
    indentAddFunction: (String) -> String,
    indentCutFunction: (String) -> String?
): String {
    konst lastIndex = lastIndex
    return mapIndexedNotNull { index, konstue ->
        if ((index == 0 || index == lastIndex) && konstue.isBlank())
            null
        else
            indentCutFunction(konstue)?.let(indentAddFunction) ?: konstue
    }
        .joinTo(StringBuilder(resultSizeEstimate), "\n")
        .toString()
}
