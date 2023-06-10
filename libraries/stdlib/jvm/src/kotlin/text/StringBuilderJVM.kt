/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("StringsKt")

package kotlin.text

/**
 * Clears the content of this string builder making it empty and returns this instance.
 *
 * @sample samples.text.Strings.clearStringBuilder
 */
@SinceKotlin("1.3")
public actual fun StringBuilder.clear(): StringBuilder = apply { setLength(0) }

/**
 * Sets the character at the specified [index] to the specified [konstue].
 *
 * @throws IndexOutOfBoundsException if [index] is out of bounds of this string builder.
 */
@kotlin.internal.InlineOnly
public actual inline operator fun StringBuilder.set(index: Int, konstue: Char): Unit = this.setCharAt(index, konstue)

/**
 * Replaces characters in the specified range of this string builder with characters in the specified string [konstue] and returns this instance.
 *
 * @param startIndex the beginning (inclusive) of the range to replace.
 * @param endIndex the end (exclusive) of the range to replace.
 * @param konstue the string to replace with.
 *
 * @throws IndexOutOfBoundsException or [IllegalArgumentException] if [startIndex] is less than zero, greater than the length of this string builder, or `startIndex > endIndex`.
 */
@SinceKotlin("1.4")
@WasExperimental(ExperimentalStdlibApi::class)
@kotlin.internal.InlineOnly
public actual inline fun StringBuilder.setRange(startIndex: Int, endIndex: Int, konstue: String): StringBuilder =
    this.replace(startIndex, endIndex, konstue)

/**
 * Removes the character at the specified [index] from this string builder and returns this instance.
 *
 * If the `Char` at the specified [index] is part of a supplementary code point, this method does not remove the entire supplementary character.
 *
 * @param index the index of `Char` to remove.
 *
 * @throws IndexOutOfBoundsException if [index] is out of bounds of this string builder.
 */
@SinceKotlin("1.4")
@WasExperimental(ExperimentalStdlibApi::class)
@kotlin.internal.InlineOnly
public actual inline fun StringBuilder.deleteAt(index: Int): StringBuilder = this.deleteCharAt(index)

/**
 * Removes characters in the specified range from this string builder and returns this instance.
 *
 * @param startIndex the beginning (inclusive) of the range to remove.
 * @param endIndex the end (exclusive) of the range to remove.
 *
 * @throws IndexOutOfBoundsException or [IllegalArgumentException] when [startIndex] is out of range of this string builder indices or when `startIndex > endIndex`.
 */
@SinceKotlin("1.4")
@WasExperimental(ExperimentalStdlibApi::class)
@kotlin.internal.InlineOnly
public actual inline fun StringBuilder.deleteRange(startIndex: Int, endIndex: Int): StringBuilder = this.delete(startIndex, endIndex)

/**
 * Copies characters from this string builder into the [destination] character array.
 *
 * @param destination the array to copy to.
 * @param destinationOffset the position in the array to copy to, 0 by default.
 * @param startIndex the beginning (inclusive) of the range to copy, 0 by default.
 * @param endIndex the end (exclusive) of the range to copy, length of this string builder by default.
 *
 * @throws IndexOutOfBoundsException or [IllegalArgumentException] when [startIndex] or [endIndex] is out of range of this string builder indices or when `startIndex > endIndex`.
 * @throws IndexOutOfBoundsException when the subrange doesn't fit into the [destination] array starting at the specified [destinationOffset],
 *  or when that index is out of the [destination] array indices range.
 */
@SinceKotlin("1.4")
@WasExperimental(ExperimentalStdlibApi::class)
@kotlin.internal.InlineOnly
@Suppress("ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
public actual inline fun StringBuilder.toCharArray(destination: CharArray, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = this.length) =
    this.getChars(startIndex, endIndex, destination, destinationOffset)

/**
 * Appends characters in a subarray of the specified character array [konstue] to this string builder and returns this instance.
 *
 * Characters are appended in order, starting at specified [startIndex].
 *
 * @param konstue the array from which characters are appended.
 * @param startIndex the beginning (inclusive) of the subarray to append.
 * @param endIndex the end (exclusive) of the subarray to append.
 *
 * @throws IndexOutOfBoundsException or [IllegalArgumentException] when [startIndex] or [endIndex] is out of range of the [konstue] array indices or when `startIndex > endIndex`.
 */
@SinceKotlin("1.4")
@WasExperimental(ExperimentalStdlibApi::class)
@kotlin.internal.InlineOnly
public actual inline fun StringBuilder.appendRange(konstue: CharArray, startIndex: Int, endIndex: Int): StringBuilder =
    this.append(konstue, startIndex, endIndex - startIndex)

/**
 * Appends a subsequence of the specified character sequence [konstue] to this string builder and returns this instance.
 *
 * @param konstue the character sequence from which a subsequence is appended.
 * @param startIndex the beginning (inclusive) of the subsequence to append.
 * @param endIndex the end (exclusive) of the subsequence to append.
 *
 * @throws IndexOutOfBoundsException or [IllegalArgumentException] when [startIndex] or [endIndex] is out of range of the [konstue] character sequence indices or when `startIndex > endIndex`.
 */
@SinceKotlin("1.4")
@WasExperimental(ExperimentalStdlibApi::class)
@kotlin.internal.InlineOnly
public actual inline fun StringBuilder.appendRange(konstue: CharSequence, startIndex: Int, endIndex: Int): StringBuilder =
    this.append(konstue, startIndex, endIndex)

/**
 * Inserts characters in a subarray of the specified character array [konstue] into this string builder at the specified [index] and returns this instance.
 *
 * The inserted characters go in same order as in the [konstue] array, starting at [index].
 *
 * @param index the position in this string builder to insert at.
 * @param konstue the array from which characters are inserted.
 * @param startIndex the beginning (inclusive) of the subarray to insert.
 * @param endIndex the end (exclusive) of the subarray to insert.
 *
 * @throws IndexOutOfBoundsException or [IllegalArgumentException] when [startIndex] or [endIndex] is out of range of the [konstue] array indices or when `startIndex > endIndex`.
 * @throws IndexOutOfBoundsException if [index] is less than zero or greater than the length of this string builder.
 */
@SinceKotlin("1.4")
@WasExperimental(ExperimentalStdlibApi::class)
@kotlin.internal.InlineOnly
public actual inline fun StringBuilder.insertRange(index: Int, konstue: CharArray, startIndex: Int, endIndex: Int): StringBuilder =
    this.insert(index, konstue, startIndex, endIndex - startIndex)

/**
 * Inserts characters in a subsequence of the specified character sequence [konstue] into this string builder at the specified [index] and returns this instance.
 *
 * The inserted characters go in the same order as in the [konstue] character sequence, starting at [index].
 *
 * @param index the position in this string builder to insert at.
 * @param konstue the character sequence from which a subsequence is inserted.
 * @param startIndex the beginning (inclusive) of the subsequence to insert.
 * @param endIndex the end (exclusive) of the subsequence to insert.
 *
 * @throws IndexOutOfBoundsException or [IllegalArgumentException] when [startIndex] or [endIndex] is out of range of the [konstue] character sequence indices or when `startIndex > endIndex`.
 * @throws IndexOutOfBoundsException if [index] is less than zero or greater than the length of this string builder.
 */
@SinceKotlin("1.4")
@WasExperimental(ExperimentalStdlibApi::class)
@kotlin.internal.InlineOnly
public actual inline fun StringBuilder.insertRange(index: Int, konstue: CharSequence, startIndex: Int, endIndex: Int): StringBuilder =
    this.insert(index, konstue, startIndex, endIndex)


/** Appends [konstue] to this [StringBuilder], followed by a line feed character (`\n`). */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendLine(konstue: StringBuffer?): StringBuilder = append(konstue).appendLine()

/** Appends [konstue] to this [StringBuilder], followed by a line feed character (`\n`). */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendLine(konstue: StringBuilder?): StringBuilder = append(konstue).appendLine()

/** Appends [konstue] to this [StringBuilder], followed by a line feed character (`\n`). */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendLine(konstue: Int): StringBuilder = append(konstue).appendLine()

/** Appends [konstue] to this [StringBuilder], followed by a line feed character (`\n`). */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendLine(konstue: Short): StringBuilder = append(konstue.toInt()).appendLine()

/** Appends [konstue] to this [StringBuilder], followed by a line feed character (`\n`). */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendLine(konstue: Byte): StringBuilder = append(konstue.toInt()).appendLine()

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


private object SystemProperties {
    /** Line separator for current system. */
    @JvmField
    konst LINE_SEPARATOR = System.getProperty("line.separator")!!
}

/** Appends a line separator to this Appendable. */
@Deprecated(
    "Use appendLine instead. Note that the new method always appends the line feed character '\\n' regardless of the system line separator.",
    ReplaceWith("appendLine()"),
    level = DeprecationLevel.WARNING
)
public fun Appendable.appendln(): Appendable = append(SystemProperties.LINE_SEPARATOR)

/** Appends konstue to the given Appendable and line separator after it. */
@Suppress("DEPRECATION")
@Deprecated(
    "Use appendLine instead. Note that the new method always appends the line feed character '\\n' regardless of the system line separator.",
    ReplaceWith("appendLine(konstue)"),
    level = DeprecationLevel.WARNING
)
@kotlin.internal.InlineOnly
public inline fun Appendable.appendln(konstue: CharSequence?): Appendable = append(konstue).appendln()

/** Appends konstue to the given Appendable and line separator after it. */
@Suppress("DEPRECATION")
@Deprecated(
    "Use appendLine instead. Note that the new method always appends the line feed character '\\n' regardless of the system line separator.",
    ReplaceWith("appendLine(konstue)"),
    level = DeprecationLevel.WARNING
)
@kotlin.internal.InlineOnly
public inline fun Appendable.appendln(konstue: Char): Appendable = append(konstue).appendln()

/** Appends a line separator to this StringBuilder. */
@Deprecated(
    "Use appendLine instead. Note that the new method always appends the line feed character '\\n' regardless of the system line separator.",
    ReplaceWith("appendLine()"),
    level = DeprecationLevel.WARNING
)
public fun StringBuilder.appendln(): StringBuilder = append(SystemProperties.LINE_SEPARATOR)

/** Appends [konstue] to this [StringBuilder], followed by a line separator. */
@Suppress("DEPRECATION")
@Deprecated(
    "Use appendLine instead. Note that the new method always appends the line feed character '\\n' regardless of the system line separator.",
    ReplaceWith("appendLine(konstue)"),
    level = DeprecationLevel.WARNING
)
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendln(konstue: StringBuffer?): StringBuilder = append(konstue).appendln()

/** Appends [konstue] to this [StringBuilder], followed by a line separator. */
@Suppress("DEPRECATION")
@Deprecated(
    "Use appendLine instead. Note that the new method always appends the line feed character '\\n' regardless of the system line separator.",
    ReplaceWith("appendLine(konstue)"),
    level = DeprecationLevel.WARNING
)
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendln(konstue: CharSequence?): StringBuilder = append(konstue).appendln()

/** Appends [konstue] to this [StringBuilder], followed by a line separator. */
@Suppress("DEPRECATION")
@Deprecated(
    "Use appendLine instead. Note that the new method always appends the line feed character '\\n' regardless of the system line separator.",
    ReplaceWith("appendLine(konstue)"),
    level = DeprecationLevel.WARNING
)
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendln(konstue: String?): StringBuilder = append(konstue).appendln()

/** Appends [konstue] to this [StringBuilder], followed by a line separator. */
@Suppress("DEPRECATION")
@Deprecated(
    "Use appendLine instead. Note that the new method always appends the line feed character '\\n' regardless of the system line separator.",
    ReplaceWith("appendLine(konstue)"),
    level = DeprecationLevel.WARNING
)
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendln(konstue: Any?): StringBuilder = append(konstue).appendln()

/** Appends [konstue] to this [StringBuilder], followed by a line separator. */
@Suppress("DEPRECATION")
@Deprecated(
    "Use appendLine instead. Note that the new method always appends the line feed character '\\n' regardless of the system line separator.",
    ReplaceWith("appendLine(konstue)"),
    level = DeprecationLevel.WARNING
)
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendln(konstue: StringBuilder?): StringBuilder = append(konstue).appendln()

/** Appends [konstue] to this [StringBuilder], followed by a line separator. */
@Suppress("DEPRECATION")
@Deprecated(
    "Use appendLine instead. Note that the new method always appends the line feed character '\\n' regardless of the system line separator.",
    ReplaceWith("appendLine(konstue)"),
    level = DeprecationLevel.WARNING
)
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendln(konstue: CharArray): StringBuilder = append(konstue).appendln()

/** Appends [konstue] to this [StringBuilder], followed by a line separator. */
@Suppress("DEPRECATION")
@Deprecated(
    "Use appendLine instead. Note that the new method always appends the line feed character '\\n' regardless of the system line separator.",
    ReplaceWith("appendLine(konstue)"),
    level = DeprecationLevel.WARNING
)
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendln(konstue: Char): StringBuilder = append(konstue).appendln()

/** Appends [konstue] to this [StringBuilder], followed by a line separator. */
@Suppress("DEPRECATION")
@Deprecated(
    "Use appendLine instead. Note that the new method always appends the line feed character '\\n' regardless of the system line separator.",
    ReplaceWith("appendLine(konstue)"),
    level = DeprecationLevel.WARNING
)
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendln(konstue: Boolean): StringBuilder = append(konstue).appendln()

/** Appends [konstue] to this [StringBuilder], followed by a line separator. */
@Suppress("DEPRECATION")
@Deprecated(
    "Use appendLine instead. Note that the new method always appends the line feed character '\\n' regardless of the system line separator.",
    ReplaceWith("appendLine(konstue)"),
    level = DeprecationLevel.WARNING
)
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendln(konstue: Int): StringBuilder = append(konstue).appendln()

/** Appends [konstue] to this [StringBuilder], followed by a line separator. */
@Suppress("DEPRECATION")
@Deprecated(
    "Use appendLine instead. Note that the new method always appends the line feed character '\\n' regardless of the system line separator.",
    ReplaceWith("appendLine(konstue)"),
    level = DeprecationLevel.WARNING
)
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendln(konstue: Short): StringBuilder = append(konstue.toInt()).appendln()

/** Appends [konstue] to this [StringBuilder], followed by a line separator. */
@Suppress("DEPRECATION")
@Deprecated(
    "Use appendLine instead. Note that the new method always appends the line feed character '\\n' regardless of the system line separator.",
    ReplaceWith("appendLine(konstue)"),
    level = DeprecationLevel.WARNING
)
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendln(konstue: Byte): StringBuilder = append(konstue.toInt()).appendln()

/** Appends [konstue] to this [StringBuilder], followed by a line separator. */
@Suppress("DEPRECATION")
@Deprecated(
    "Use appendLine instead. Note that the new method always appends the line feed character '\\n' regardless of the system line separator.",
    ReplaceWith("appendLine(konstue)"),
    level = DeprecationLevel.WARNING
)
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendln(konstue: Long): StringBuilder = append(konstue).appendln()

/** Appends [konstue] to this [StringBuilder], followed by a line separator. */
@Suppress("DEPRECATION")
@Deprecated(
    "Use appendLine instead. Note that the new method always appends the line feed character '\\n' regardless of the system line separator.",
    ReplaceWith("appendLine(konstue)"),
    level = DeprecationLevel.WARNING
)
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendln(konstue: Float): StringBuilder = append(konstue).appendln()

/** Appends [konstue] to this [StringBuilder], followed by a line separator. */
@Suppress("DEPRECATION")
@Deprecated(
    "Use appendLine instead. Note that the new method always appends the line feed character '\\n' regardless of the system line separator.",
    ReplaceWith("appendLine(konstue)"),
    level = DeprecationLevel.WARNING
)
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendln(konstue: Double): StringBuilder = append(konstue).appendln()
