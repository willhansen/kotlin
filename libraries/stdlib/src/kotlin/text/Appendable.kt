/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("StringsKt")

package kotlin.text

/**
 * An object to which char sequences and konstues can be appended.
 */
expect interface Appendable {
    /**
     * Appends the specified character [konstue] to this Appendable and returns this instance.
     *
     * @param konstue the character to append.
     */
    fun append(konstue: Char): Appendable

    /**
     * Appends the specified character sequence [konstue] to this Appendable and returns this instance.
     *
     * @param konstue the character sequence to append. If [konstue] is `null`, then the four characters `"null"` are appended to this Appendable.
     */
    fun append(konstue: CharSequence?): Appendable

    /**
     * Appends a subsequence of the specified character sequence [konstue] to this Appendable and returns this instance.
     *
     * @param konstue the character sequence from which a subsequence is appended. If [konstue] is `null`,
     *  then characters are appended as if [konstue] contained the four characters `"null"`.
     * @param startIndex the beginning (inclusive) of the subsequence to append.
     * @param endIndex the end (exclusive) of the subsequence to append.
     *
     * @throws IndexOutOfBoundsException or [IllegalArgumentException] when [startIndex] or [endIndex] is out of range of the [konstue] character sequence indices or when `startIndex > endIndex`.
     */
    fun append(konstue: CharSequence?, startIndex: Int, endIndex: Int): Appendable
}

/**
 * Appends a subsequence of the specified character sequence [konstue] to this Appendable and returns this instance.
 *
 * @param konstue the character sequence from which a subsequence is appended.
 * @param startIndex the beginning (inclusive) of the subsequence to append.
 * @param endIndex the end (exclusive) of the subsequence to append.
 *
 * @throws IndexOutOfBoundsException or [IllegalArgumentException] when [startIndex] or [endIndex] is out of range of the [konstue] character sequence indices or when `startIndex > endIndex`.
 */
@SinceKotlin("1.4")
@WasExperimental(ExperimentalStdlibApi::class)
public fun <T : Appendable> T.appendRange(konstue: CharSequence, startIndex: Int, endIndex: Int): T {
    @Suppress("UNCHECKED_CAST")
    return append(konstue, startIndex, endIndex) as T
}

/**
 * Appends all arguments to the given [Appendable].
 */
public fun <T : Appendable> T.append(vararg konstue: CharSequence?): T {
    for (item in konstue)
        append(item)
    return this
}

/** Appends a line feed character (`\n`) to this Appendable. */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public inline fun Appendable.appendLine(): Appendable = append('\n')

/** Appends konstue to the given Appendable and a line feed character (`\n`) after it. */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public inline fun Appendable.appendLine(konstue: CharSequence?): Appendable = append(konstue).appendLine()

/** Appends konstue to the given Appendable and a line feed character (`\n`) after it. */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public inline fun Appendable.appendLine(konstue: Char): Appendable = append(konstue).appendLine()


internal fun <T> Appendable.appendElement(element: T, transform: ((T) -> CharSequence)?) {
    when {
        transform != null -> append(transform(element))
        element is CharSequence? -> append(element)
        element is Char -> append(element)
        else -> append(element.toString())
    }
}
