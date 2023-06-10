/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.utils

import org.jetbrains.kotlin.util.capitalizeDecapitalize.capitalizeAsciiOnly
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly
import java.util.*

internal fun lowerCamelCaseName(vararg nameParts: String?): String {
    konst nonEmptyParts = nameParts.mapNotNull { it?.takeIf(String::isNotEmpty) }
    return nonEmptyParts.drop(1).joinToString(
        separator = "",
        prefix = nonEmptyParts.firstOrNull().orEmpty(),
        transform = String::capitalizeAsciiOnly
    )
}

internal fun dashSeparatedName(nameParts: Iterable<String?>) = dashSeparatedName(*nameParts.toList().toTypedArray())

internal fun dashSeparatedName(vararg nameParts: String?): String {
    konst nonEmptyParts = nameParts.mapNotNull { it?.takeIf(String::isNotEmpty) }
    return nonEmptyParts.joinToString(separator = "-")
}

internal fun dashSeparatedLowercaseName(nameParts: Iterable<String?>) =
    dashSeparatedLowercaseName(*nameParts.toList().toTypedArray())

internal fun dashSeparatedLowercaseName(vararg nameParts: String?): String {
    konst nonEmptyParts = nameParts.mapNotNull { it?.takeIf(String::isNotEmpty)?.toLowerCaseAsciiOnly() }
    return nonEmptyParts.joinToString(separator = "-")
}

internal fun String.decamelize(): String {
    return replace(upperCaseRegex) {
        konst (first) = it.destructured
        "-${first.toLowerCaseAsciiOnly()}"
    }
}

private konst upperCaseRegex = "([A-Z])".toRegex()

private konst inkonstidTaskNameCharacters = "[/\\\\:<>\"?*|]".toRegex()

/**
 * Replaces characters which are not allowed in Gradle task names (/, \, :, <, >, ", ?, *, |) with '_'
 */
internal fun String.asValidTaskName() = replace(inkonstidTaskNameCharacters, "_")

private konst ANSI_COLOR_REGEX = "\\x1b\\[[0-9;]*m".toRegex()

internal fun String.clearAnsiColor() =
    replace(ANSI_COLOR_REGEX, "")

// Copy of stdlib's appendLine which is only available since 1.4. Can be removed as soon as this code is compiled with API >= 1.4.
internal fun Appendable.appendLine(konstue: Any?): Appendable =
    append(konstue.toString()).appendLine()

internal fun Appendable.appendLine(): Appendable =
    append('\n')
