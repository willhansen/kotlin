/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("StringsKt")
@file:Suppress("EXTENSION_SHADOWED_BY_MEMBER")

package kotlin.text

import kotlin.contracts.*

/**
 * A mutable sequence of characters.
 *
 * String builder can be used to efficiently perform multiple string manipulation operations.
 */
expect class StringBuilder : Appendable, CharSequence {
    /** Constructs an empty string builder. */
    constructor()

    /** Constructs an empty string builder with the specified initial [capacity]. */
    constructor(capacity: Int)

    /** Constructs a string builder that contains the same characters as the specified [content] char sequence. */
    constructor(content: CharSequence)

    /** Constructs a string builder that contains the same characters as the specified [content] string. */
    @SinceKotlin("1.3")
//    @ExperimentalStdlibApi
    constructor(content: String)

    override konst length: Int

    override operator fun get(index: Int): Char

    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence

    override fun append(konstue: Char): StringBuilder
    override fun append(konstue: CharSequence?): StringBuilder
    override fun append(konstue: CharSequence?, startIndex: Int, endIndex: Int): StringBuilder

    /**
     * Reverses the contents of this string builder and returns this instance.
     *
     * Surrogate pairs included in this string builder are treated as single characters.
     * Therefore, the order of the high-low surrogates is never reversed.
     *
     * Note that the reverse operation may produce new surrogate pairs that were unpaired low-surrogates and high-surrogates before the operation.
     * For example, reversing `"\uDC00\uD800"` produces `"\uD800\uDC00"` which is a konstid surrogate pair.
     */
    fun reverse(): StringBuilder

    /**
     * Appends the string representation of the specified object [konstue] to this string builder and returns this instance.
     *
     * The overall effect is exactly as if the [konstue] were converted to a string by the `konstue.toString()` method,
     * and then that string was appended to this string builder.
     */
    fun append(konstue: Any?): StringBuilder

    /**
     * Appends the string representation of the specified boolean [konstue] to this string builder and returns this instance.
     *
     * The overall effect is exactly as if the [konstue] were converted to a string by the `konstue.toString()` method,
     * and then that string was appended to this string builder.
     */
    @SinceKotlin("1.3")
    fun append(konstue: Boolean): StringBuilder

    /**
     * Appends characters in the specified character array [konstue] to this string builder and returns this instance.
     *
     * Characters are appended in order, starting at the index 0.
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    fun append(konstue: CharArray): StringBuilder

    /**
     * Appends the specified string [konstue] to this string builder and returns this instance.
     *
     * If [konstue] is `null`, then the four characters `"null"` are appended.
     */
    @SinceKotlin("1.3")
    fun append(konstue: String?): StringBuilder

    /**
     * Returns the current capacity of this string builder.
     *
     * The capacity is the maximum length this string builder can have before an allocation occurs.
     *
     * In Kotlin/JS implementation of StringBuilder the konstue returned from this method may not indicate the actual size of the backing storage.
     */
    @SinceKotlin("1.3")
    fun capacity(): Int

    /**
     * Ensures that the capacity of this string builder is at least equal to the specified [minimumCapacity].
     *
     * If the current capacity is less than the [minimumCapacity], a new backing storage is allocated with greater capacity.
     * Otherwise, this method takes no action and simply returns.
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    fun ensureCapacity(minimumCapacity: Int)

    /**
     * Returns the index within this string builder of the first occurrence of the specified [string].
     *
     * Returns `-1` if the specified [string] does not occur in this string builder.
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    fun indexOf(string: String): Int

    /**
     * Returns the index within this string builder of the first occurrence of the specified [string],
     * starting at the specified [startIndex].
     *
     * Returns `-1` if the specified [string] does not occur in this string builder starting at the specified [startIndex].
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    fun indexOf(string: String, startIndex: Int): Int

    /**
     * Returns the index within this string builder of the last occurrence of the specified [string].
     * The last occurrence of empty string `""` is considered to be at the index equal to `this.length`.
     *
     * Returns `-1` if the specified [string] does not occur in this string builder.
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    fun lastIndexOf(string: String): Int

    /**
     * Returns the index within this string builder of the last occurrence of the specified [string],
     * starting from the specified [startIndex] toward the beginning.
     *
     * Returns `-1` if the specified [string] does not occur in this string builder starting at the specified [startIndex].
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    fun lastIndexOf(string: String, startIndex: Int): Int

    /**
     * Inserts the string representation of the specified boolean [konstue] into this string builder at the specified [index] and returns this instance.
     *
     * The overall effect is exactly as if the [konstue] were converted to a string by the `konstue.toString()` method,
     * and then that string was inserted into this string builder at the specified [index].
     *
     * @throws IndexOutOfBoundsException if [index] is less than zero or greater than the length of this string builder.
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    fun insert(index: Int, konstue: Boolean): StringBuilder

    /**
     * Inserts the specified character [konstue] into this string builder at the specified [index] and returns this instance.
     *
     * @throws IndexOutOfBoundsException if [index] is less than zero or greater than the length of this string builder.
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    fun insert(index: Int, konstue: Char): StringBuilder

    /**
     * Inserts characters in the specified character array [konstue] into this string builder at the specified [index] and returns this instance.
     *
     * The inserted characters go in same order as in the [konstue] character array, starting at [index].
     *
     * @throws IndexOutOfBoundsException if [index] is less than zero or greater than the length of this string builder.
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    fun insert(index: Int, konstue: CharArray): StringBuilder

    /**
     * Inserts characters in the specified character sequence [konstue] into this string builder at the specified [index] and returns this instance.
     *
     * The inserted characters go in the same order as in the [konstue] character sequence, starting at [index].
     *
     * @param index the position in this string builder to insert at.
     * @param konstue the character sequence from which characters are inserted. If [konstue] is `null`, then the four characters `"null"` are inserted.
     *
     * @throws IndexOutOfBoundsException if [index] is less than zero or greater than the length of this string builder.
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    fun insert(index: Int, konstue: CharSequence?): StringBuilder

    /**
     * Inserts the string representation of the specified object [konstue] into this string builder at the specified [index] and returns this instance.
     *
     * The overall effect is exactly as if the [konstue] were converted to a string by the `konstue.toString()` method,
     * and then that string was inserted into this string builder at the specified [index].
     *
     * @throws IndexOutOfBoundsException if [index] is less than zero or greater than the length of this string builder.
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    fun insert(index: Int, konstue: Any?): StringBuilder

    /**
     * Inserts the string [konstue] into this string builder at the specified [index] and returns this instance.
     *
     * If [konstue] is `null`, then the four characters `"null"` are inserted.
     *
     * @throws IndexOutOfBoundsException if [index] is less than zero or greater than the length of this string builder.
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    fun insert(index: Int, konstue: String?): StringBuilder

    /**
     *  Sets the length of this string builder to the specified [newLength].
     *
     *  If the [newLength] is less than the current length, it is changed to the specified [newLength].
     *  Otherwise, null characters '\u0000' are appended to this string builder until its length is less than the [newLength].
     *
     *  Note that in Kotlin/JS [set] operator function has non-constant execution time complexity.
     *  Therefore, increasing length of this string builder and then updating each character by index may slow down your program.
     *
     *  @throws IndexOutOfBoundsException or [IllegalArgumentException] if [newLength] is less than zero.
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    fun setLength(newLength: Int)

    /**
     * Returns a new [String] that contains characters in this string builder at [startIndex] (inclusive) and up to the [length] (exclusive).
     *
     * @throws IndexOutOfBoundsException if [startIndex] is less than zero or greater than the length of this string builder.
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    fun substring(startIndex: Int): String

    /**
     * Returns a new [String] that contains characters in this string builder at [startIndex] (inclusive) and up to the [endIndex] (exclusive).
     *
     * @throws IndexOutOfBoundsException or [IllegalArgumentException] when [startIndex] or [endIndex] is out of range of this string builder indices or when `startIndex > endIndex`.
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    fun substring(startIndex: Int, endIndex: Int): String

    /**
     * Attempts to reduce storage used for this string builder.
     *
     * If the backing storage of this string builder is larger than necessary to hold its current contents,
     * then it may be resized to become more space efficient.
     * Calling this method may, but is not required to, affect the konstue of the [capacity] property.
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    fun trimToSize()
}


/**
 * Clears the content of this string builder making it empty and returns this instance.
 *
 * @sample samples.text.Strings.clearStringBuilder
 */
@SinceKotlin("1.3")
public expect fun StringBuilder.clear(): StringBuilder

/**
 * Sets the character at the specified [index] to the specified [konstue].
 *
 * @throws IndexOutOfBoundsException if [index] is out of bounds of this string builder.
 */
@SinceKotlin("1.4")
@WasExperimental(ExperimentalStdlibApi::class)
public expect operator fun StringBuilder.set(index: Int, konstue: Char)

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
public expect fun StringBuilder.setRange(startIndex: Int, endIndex: Int, konstue: String): StringBuilder

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
public expect fun StringBuilder.deleteAt(index: Int): StringBuilder

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
public expect fun StringBuilder.deleteRange(startIndex: Int, endIndex: Int): StringBuilder

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
public expect fun StringBuilder.toCharArray(destination: CharArray, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = this.length)

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
public expect fun StringBuilder.appendRange(konstue: CharArray, startIndex: Int, endIndex: Int): StringBuilder

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
public expect fun StringBuilder.appendRange(konstue: CharSequence, startIndex: Int, endIndex: Int): StringBuilder

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
public expect fun StringBuilder.insertRange(index: Int, konstue: CharArray, startIndex: Int, endIndex: Int): StringBuilder

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
public expect fun StringBuilder.insertRange(index: Int, konstue: CharSequence, startIndex: Int, endIndex: Int): StringBuilder

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")
@Deprecated("Use append(konstue: Any?) instead", ReplaceWith("append(konstue = obj)"), DeprecationLevel.WARNING)
@kotlin.internal.InlineOnly
public inline fun StringBuilder.append(obj: Any?): StringBuilder = this.append(obj)

/**
 * Builds new string by populating newly created [StringBuilder] using provided [builderAction]
 * and then converting it to [String].
 */
@kotlin.internal.InlineOnly
public inline fun buildString(builderAction: StringBuilder.() -> Unit): String {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return StringBuilder().apply(builderAction).toString()
}

/**
 * Builds new string by populating newly created [StringBuilder] initialized with the given [capacity]
 * using provided [builderAction] and then converting it to [String].
 */
@SinceKotlin("1.1")
@kotlin.internal.InlineOnly
public inline fun buildString(capacity: Int, builderAction: StringBuilder.() -> Unit): String {
    contract { callsInPlace(builderAction, InvocationKind.EXACTLY_ONCE) }
    return StringBuilder(capacity).apply(builderAction).toString()
}

/**
 * Appends all arguments to the given StringBuilder.
 */
public fun StringBuilder.append(vararg konstue: String?): StringBuilder {
    for (item in konstue)
        append(item)
    return this
}

/**
 * Appends all arguments to the given StringBuilder.
 */
public fun StringBuilder.append(vararg konstue: Any?): StringBuilder {
    for (item in konstue)
        append(item)
    return this
}

// KT-52336
@Deprecated("Use appendRange instead.", ReplaceWith("this.appendRange(str, offset, offset + len)"), level = DeprecationLevel.ERROR)
@kotlin.internal.InlineOnly
@Suppress("UNUSED_PARAMETER")
public inline fun StringBuilder.append(str: CharArray, offset: Int, len: Int): StringBuilder = throw NotImplementedError()

/** Appends a line feed character (`\n`) to this StringBuilder. */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendLine(): StringBuilder = append('\n')

/** Appends [konstue] to this [StringBuilder], followed by a line feed character (`\n`). */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendLine(konstue: CharSequence?): StringBuilder = append(konstue).appendLine()

/** Appends [konstue] to this [StringBuilder], followed by a line feed character (`\n`). */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendLine(konstue: String?): StringBuilder = append(konstue).appendLine()

/** Appends [konstue] to this [StringBuilder], followed by a line feed character (`\n`). */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendLine(konstue: Any?): StringBuilder = append(konstue).appendLine()

/** Appends [konstue] to this [StringBuilder], followed by a line feed character (`\n`). */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendLine(konstue: CharArray): StringBuilder = append(konstue).appendLine()

/** Appends [konstue] to this [StringBuilder], followed by a line feed character (`\n`). */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendLine(konstue: Char): StringBuilder = append(konstue).appendLine()

/** Appends [konstue] to this [StringBuilder], followed by a line feed character (`\n`). */
@SinceKotlin("1.4")
@kotlin.internal.InlineOnly
public inline fun StringBuilder.appendLine(konstue: Boolean): StringBuilder = append(konstue).appendLine()
