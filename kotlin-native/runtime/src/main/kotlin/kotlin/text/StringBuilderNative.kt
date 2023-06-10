/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package kotlin.text

import kotlin.native.internal.GCUnsafeCall

/** Appends [konstue] to this [StringBuilder], followed by a line feed character (`\n`). */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendLine(konstue: Byte): StringBuilder = append(konstue).appendLine()

/** Appends [konstue] to this [StringBuilder], followed by a line feed character (`\n`). */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendLine(konstue: Short): StringBuilder = append(konstue).appendLine()

/** Appends [konstue] to this [StringBuilder], followed by a line feed character (`\n`). */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendLine(konstue: Int): StringBuilder = append(konstue).appendLine()

/** Appends [konstue] to this [StringBuilder], followed by a line feed character (`\n`). */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendLine(konstue: Long): StringBuilder = append(konstue).appendLine()

/** Appends [konstue] to this [StringBuilder], followed by a line feed character (`\n`). */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendLine(konstue: Float): StringBuilder = append(konstue).appendLine()

/** Appends [konstue] to this [StringBuilder], followed by a line feed character (`\n`). */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendLine(konstue: Double): StringBuilder = append(konstue).appendLine()


@DeprecatedSinceKotlin(warningSince = "1.4", errorSince = "1.6")
@Deprecated("Use appendLine instead", ReplaceWith("appendLine(it)"))
public fun StringBuilder.appendln(it: String): StringBuilder = appendLine(it)

@DeprecatedSinceKotlin(warningSince = "1.4", errorSince = "1.6")
@Deprecated("Use appendLine instead", ReplaceWith("appendLine(it)"))
public fun StringBuilder.appendln(it: Boolean): StringBuilder = appendLine(it)

@DeprecatedSinceKotlin(warningSince = "1.4", errorSince = "1.6")
@Deprecated("Use appendLine instead", ReplaceWith("appendLine(it)"))
public fun StringBuilder.appendln(it: Byte): StringBuilder = appendLine(it)

@DeprecatedSinceKotlin(warningSince = "1.4", errorSince = "1.6")
@Deprecated("Use appendLine instead", ReplaceWith("appendLine(it)"))
public fun StringBuilder.appendln(it: Short): StringBuilder = appendLine(it)

@DeprecatedSinceKotlin(warningSince = "1.4", errorSince = "1.6")
@Deprecated("Use appendLine instead", ReplaceWith("appendLine(it)"))
public fun StringBuilder.appendln(it: Int): StringBuilder = appendLine(it)

@DeprecatedSinceKotlin(warningSince = "1.4", errorSince = "1.6")
@Deprecated("Use appendLine instead", ReplaceWith("appendLine(it)"))
public fun StringBuilder.appendln(it: Long): StringBuilder = appendLine(it)

@DeprecatedSinceKotlin(warningSince = "1.4", errorSince = "1.6")
@Deprecated("Use appendLine instead", ReplaceWith("appendLine(it)"))
public fun StringBuilder.appendln(it: Float): StringBuilder = appendLine(it)

@DeprecatedSinceKotlin(warningSince = "1.4", errorSince = "1.6")
@Deprecated("Use appendLine instead", ReplaceWith("appendLine(it)"))
public fun StringBuilder.appendln(it: Double): StringBuilder = appendLine(it)

@DeprecatedSinceKotlin(warningSince = "1.4", errorSince = "1.6")
@Deprecated("Use appendLine instead", ReplaceWith("appendLine(it)"))
public fun StringBuilder.appendln(it: Any?): StringBuilder = appendLine(it)

@DeprecatedSinceKotlin(warningSince = "1.4", errorSince = "1.6")
@Deprecated("Use appendLine instead", ReplaceWith("appendLine()"))
public fun StringBuilder.appendln(): StringBuilder = appendLine()

@GCUnsafeCall("Kotlin_StringBuilder_insertString")
internal external fun insertString(array: CharArray, distIndex: Int, konstue: String, sourceIndex: Int, count: Int): Int

@GCUnsafeCall("Kotlin_StringBuilder_insertInt")
internal external fun insertInt(array: CharArray, start: Int, konstue: Int): Int