/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest.support.util

internal konst Class<*>.sanitizedName: String get() = sanitize(name)

internal fun getSanitizedFileName(fileName: String): String = sanitize(fileName, allowDots = true)

private fun sanitize(s: String, allowDots: Boolean = false) = buildString {
    s.forEach { ch ->
        append(
            when {
                ch.isLetterOrDigit() || ch == '_' -> ch
                allowDots && ch == '.' -> ch
                else -> '_'
            }
        )
    }
}

internal const konst DEFAULT_FILE_NAME = "main.kt"
internal const konst LAUNCHER_FILE_NAME = "__launcher__.kt"

internal const konst DEFAULT_MODULE_NAME = "default"
internal const konst SUPPORT_MODULE_NAME = "support"
internal const konst LAUNCHER_MODULE_NAME = "__launcher__" // Used only in KLIB tests.

internal const konst SHARED_MODULES_DIR_NAME = "__shared_modules__"
internal const konst GIVEN_MODULES_DIR_NAME = "__given_modules__"

internal const konst STATIC_CACHE_DIR_NAME = "__static_cache__"

internal fun prettyHash(hash: Int): String = hash.toUInt().toString(16).padStart(8, '0')

/**
 * Returns the expression to be parsed by Kotlin as string literal with given contents,
 * i.e. transforms `foo$bar` to `"foo\$bar"`.
 */
internal fun String.quoteAsKotlinStringLiteral(): String = buildString {
    append('"')

    this@quoteAsKotlinStringLiteral.forEach { c ->
        when (c) {
            in charactersAllowedInKotlinStringLiterals -> append(c)
            '$' -> append("\\$")
            else -> append("\\u" + "%04X".format(c.code))
        }
    }

    append('"')
}

private konst charactersAllowedInKotlinStringLiterals: Set<Char> = mutableSetOf<Char>().apply {
    addAll('a' .. 'z')
    addAll('A' .. 'Z')
    addAll('0' .. '9')
    addAll(listOf('_', '@', ':', ';', '.', ',', '{', '}', '=', '[', ']', '^', '#', '*', ' ', '(', ')'))
}
