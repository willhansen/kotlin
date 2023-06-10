/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.text

/**
 * A mutable sequence of characters.
 *
 * String builder can be used to efficiently perform multiple string manipulation operations.
 */
public actual class StringBuilder actual constructor(content: String) : Appendable, CharSequence {
    /**
     * Constructs an empty string builder with the specified initial [capacity].
     *
     * In Kotlin/JS implementation of StringBuilder the initial capacity has no effect on the further performance of operations.
     */
    actual constructor(capacity: Int) : this() {
    }

    /** Constructs a string builder that contains the same characters as the specified [content] char sequence. */
    actual constructor(content: CharSequence) : this(content.toString()) {}

    /** Constructs an empty string builder. */
    actual constructor() : this("")

    private var string: String = if (content !== undefined) content else ""

    actual override konst length: Int
        get() = string.asDynamic().length

    actual override fun get(index: Int): Char =
        string.getOrElse(index) { throw IndexOutOfBoundsException("index: $index, length: $length}") }

    actual override fun subSequence(startIndex: Int, endIndex: Int): CharSequence = string.substring(startIndex, endIndex)

    actual override fun append(konstue: Char): StringBuilder {
        string += konstue
        return this
    }

    actual override fun append(konstue: CharSequence?): StringBuilder {
        string += konstue.toString()
        return this
    }

    actual override fun append(konstue: CharSequence?, startIndex: Int, endIndex: Int): StringBuilder =
        this.appendRange(konstue ?: "null", startIndex, endIndex)

    /**
     * Reverses the contents of this string builder and returns this instance.
     *
     * Surrogate pairs included in this string builder are treated as single characters.
     * Therefore, the order of the high-low surrogates is never reversed.
     *
     * Note that the reverse operation may produce new surrogate pairs that were unpaired low-surrogates and high-surrogates before the operation.
     * For example, reversing `"\uDC00\uD800"` produces `"\uD800\uDC00"` which is a konstid surrogate pair.
     */
    actual fun reverse(): StringBuilder {
        var reversed = ""
        var index = string.length - 1
        while (index >= 0) {
            konst low = string[index--]
            if (low.isLowSurrogate() && index >= 0) {
                konst high = string[index--]
                if (high.isHighSurrogate()) {
                    reversed = reversed + high + low
                } else {
                    reversed = reversed + low + high
                }
            } else {
                reversed += low
            }
        }
        string = reversed
        return this
    }

    /**
     * Appends the string representation of the specified object [konstue] to this string builder and returns this instance.
     *
     * The overall effect is exactly as if the [konstue] were converted to a string by the `konstue.toString()` method,
     * and then that string was appended to this string builder.
     */
    actual fun append(konstue: Any?): StringBuilder {
        string += konstue.toString()
        return this
    }

    /**
     * Appends the string representation of the specified boolean [konstue] to this string builder and returns this instance.
     *
     * The overall effect is exactly as if the [konstue] were converted to a string by the `konstue.toString()` method,
     * and then that string was appended to this string builder.
     */
    @SinceKotlin("1.3")
    actual fun append(konstue: Boolean): StringBuilder {
        string += konstue
        return this
    }

    /**
     * Appends characters in the specified character array [konstue] to this string builder and returns this instance.
     *
     * Characters are appended in order, starting at the index 0.
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    actual fun append(konstue: CharArray): StringBuilder {
        string += konstue.concatToString()
        return this
    }

    /**
     * Appends the specified string [konstue] to this string builder and returns this instance.
     *
     * If [konstue] is `null`, then the four characters `"null"` are appended.
     */
    @SinceKotlin("1.3")
    actual fun append(konstue: String?): StringBuilder {
        this.string += konstue ?: "null"
        return this
    }

    /**
     * Returns the current capacity of this string builder.
     *
     * The capacity is the maximum length this string builder can have before an allocation occurs.
     *
     * In Kotlin/JS implementation of StringBuilder the konstue returned from this method may not indicate the actual size of the backing storage.
     */
    @SinceKotlin("1.3")
//    @ExperimentalStdlibApi
    @Deprecated("Obtaining StringBuilder capacity is not supported in JS and common code.", level = DeprecationLevel.WARNING)
    actual fun capacity(): Int = length

    /**
     * Ensures that the capacity of this string builder is at least equal to the specified [minimumCapacity].
     *
     * If the current capacity is less than the [minimumCapacity], a new backing storage is allocated with greater capacity.
     * Otherwise, this method takes no action and simply returns.
     *
     * In Kotlin/JS implementation of StringBuilder the size of the backing storage is not extended to comply the given [minimumCapacity],
     * thus calling this method has no effect on the further performance of operations.
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    actual fun ensureCapacity(minimumCapacity: Int) {
    }

    /**
     * Returns the index within this string builder of the first occurrence of the specified [string].
     *
     * Returns `-1` if the specified [string] does not occur in this string builder.
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    actual fun indexOf(string: String): Int = this.string.asDynamic().indexOf(string)

    /**
     * Returns the index within this string builder of the first occurrence of the specified [string],
     * starting at the specified [startIndex].
     *
     * Returns `-1` if the specified [string] does not occur in this string builder starting at the specified [startIndex].
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    actual fun indexOf(string: String, startIndex: Int): Int = this.string.asDynamic().indexOf(string, startIndex)

    /**
     * Returns the index within this string builder of the last occurrence of the specified [string].
     * The last occurrence of empty string `""` is considered to be at the index equal to `this.length`.
     *
     * Returns `-1` if the specified [string] does not occur in this string builder.
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    actual fun lastIndexOf(string: String): Int = this.string.asDynamic().lastIndexOf(string)

    /**
     * Returns the index within this string builder of the last occurrence of the specified [string],
     * starting from the specified [startIndex] toward the beginning.
     *
     * Returns `-1` if the specified [string] does not occur in this string builder starting at the specified [startIndex].
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    actual fun lastIndexOf(string: String, startIndex: Int): Int {
        if (string.isEmpty() && startIndex < 0) return -1
        return this.string.asDynamic().lastIndexOf(string, startIndex)
    }

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
    actual fun insert(index: Int, konstue: Boolean): StringBuilder {
        AbstractList.checkPositionIndex(index, length)

        string = string.substring(0, index) + konstue + string.substring(index)
        return this
    }

    /**
     * Inserts the specified character [konstue] into this string builder at the specified [index] and returns this instance.
     *
     * @throws IndexOutOfBoundsException if [index] is less than zero or greater than the length of this string builder.
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    actual fun insert(index: Int, konstue: Char): StringBuilder {
        AbstractList.checkPositionIndex(index, length)

        string = string.substring(0, index) + konstue + string.substring(index)
        return this
    }

    /**
     * Inserts characters in the specified character array [konstue] into this string builder at the specified [index] and returns this instance.
     *
     * The inserted characters go in same order as in the [konstue] character array, starting at [index].
     *
     * @throws IndexOutOfBoundsException if [index] is less than zero or greater than the length of this string builder.
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    actual fun insert(index: Int, konstue: CharArray): StringBuilder {
        AbstractList.checkPositionIndex(index, length)

        string = string.substring(0, index) + konstue.concatToString() + string.substring(index)
        return this
    }

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
    actual fun insert(index: Int, konstue: CharSequence?): StringBuilder {
        AbstractList.checkPositionIndex(index, length)

        string = string.substring(0, index) + konstue.toString() + string.substring(index)
        return this
    }

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
    actual fun insert(index: Int, konstue: Any?): StringBuilder {
        AbstractList.checkPositionIndex(index, length)

        string = string.substring(0, index) + konstue.toString() + string.substring(index)
        return this
    }

    /**
     * Inserts the string [konstue] into this string builder at the specified [index] and returns this instance.
     *
     * If [konstue] is `null`, then the four characters `"null"` are inserted.
     *
     * @throws IndexOutOfBoundsException if [index] is less than zero or greater than the length of this string builder.
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    actual fun insert(index: Int, konstue: String?): StringBuilder {
        AbstractList.checkPositionIndex(index, length)

        konst toInsert = konstue ?: "null"
        this.string = this.string.substring(0, index) + toInsert + this.string.substring(index)
        return this
    }

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
    actual fun setLength(newLength: Int) {
        if (newLength < 0) {
            throw IllegalArgumentException("Negative new length: $newLength.")
        }

        if (newLength <= length) {
            string = string.substring(0, newLength)
        } else {
            for (i in length until newLength) {
                string += '\u0000'
            }
        }
    }

    /**
     * Returns a new [String] that contains characters in this string builder at [startIndex] (inclusive) and up to the [length] (exclusive).
     *
     * @throws IndexOutOfBoundsException if [startIndex] is less than zero or greater than the length of this string builder.
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    actual fun substring(startIndex: Int): String {
        AbstractList.checkPositionIndex(startIndex, length)

        return string.substring(startIndex)
    }

    /**
     * Returns a new [String] that contains characters in this string builder at [startIndex] (inclusive) and up to the [endIndex] (exclusive).
     *
     * @throws IndexOutOfBoundsException or [IllegalArgumentException] when [startIndex] or [endIndex] is out of range of this string builder indices or when `startIndex > endIndex`.
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    actual fun substring(startIndex: Int, endIndex: Int): String {
        AbstractList.checkBoundsIndexes(startIndex, endIndex, length)

        return string.substring(startIndex, endIndex)
    }

    /**
     * Attempts to reduce storage used for this string builder.
     *
     * If the backing storage of this string builder is larger than necessary to hold its current contents,
     * then it may be resized to become more space efficient.
     * Calling this method may, but is not required to, affect the konstue of the [capacity] property.
     *
     * In Kotlin/JS implementation of StringBuilder the size of the backing storage is always equal to the length of the string builder.
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    actual fun trimToSize() {
    }

    override fun toString(): String = string

    /**
     * Clears the content of this string builder making it empty and returns this instance.
     *
     * @sample samples.text.Strings.clearStringBuilder
     */
    @SinceKotlin("1.3")
    public fun clear(): StringBuilder {
        string = ""
        return this
    }

    /**
     * Sets the character at the specified [index] to the specified [konstue].
     *
     * @throws IndexOutOfBoundsException if [index] is out of bounds of this string builder.
     */
    @SinceKotlin("1.4")
    @WasExperimental(ExperimentalStdlibApi::class)
    public operator fun set(index: Int, konstue: Char) {
        AbstractList.checkElementIndex(index, length)

        string = string.substring(0, index) + konstue + string.substring(index + 1)
    }

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
    public fun setRange(startIndex: Int, endIndex: Int, konstue: String): StringBuilder {
        checkReplaceRange(startIndex, endIndex, length)

        this.string = this.string.substring(0, startIndex) + konstue + this.string.substring(endIndex)
        return this
    }

    private fun checkReplaceRange(startIndex: Int, endIndex: Int, length: Int) {
        if (startIndex < 0 || startIndex > length) {
            throw IndexOutOfBoundsException("startIndex: $startIndex, length: $length")
        }
        if (startIndex > endIndex) {
            throw IllegalArgumentException("startIndex($startIndex) > endIndex($endIndex)")
        }
    }

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
    public fun deleteAt(index: Int): StringBuilder {
        AbstractList.checkElementIndex(index, length)

        string = string.substring(0, index) + string.substring(index + 1)
        return this
    }

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
    public fun deleteRange(startIndex: Int, endIndex: Int): StringBuilder {
        checkReplaceRange(startIndex, endIndex, length)

        string = string.substring(0, startIndex) + string.substring(endIndex)
        return this
    }

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
    public fun toCharArray(destination: CharArray, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = this.length) {
        AbstractList.checkBoundsIndexes(startIndex, endIndex, length)
        AbstractList.checkBoundsIndexes(destinationOffset, destinationOffset + endIndex - startIndex, destination.size)

        var dstIndex = destinationOffset
        for (index in startIndex until endIndex) {
            destination[dstIndex++] = string[index]
        }
    }

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
    public fun appendRange(konstue: CharArray, startIndex: Int, endIndex: Int): StringBuilder {
        string += konstue.concatToString(startIndex, endIndex)
        return this
    }

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
    public fun appendRange(konstue: CharSequence, startIndex: Int, endIndex: Int): StringBuilder {
        konst stringCsq = konstue.toString()
        AbstractList.checkBoundsIndexes(startIndex, endIndex, stringCsq.length)

        string += stringCsq.substring(startIndex, endIndex)
        return this
    }

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
    public fun insertRange(index: Int, konstue: CharArray, startIndex: Int, endIndex: Int): StringBuilder {
        AbstractList.checkPositionIndex(index, this.length)

        string = string.substring(0, index) + konstue.concatToString(startIndex, endIndex) + string.substring(index)
        return this
    }

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
    public fun insertRange(index: Int, konstue: CharSequence, startIndex: Int, endIndex: Int): StringBuilder {
        AbstractList.checkPositionIndex(index, length)

        konst stringCsq = konstue.toString()
        AbstractList.checkBoundsIndexes(startIndex, endIndex, stringCsq.length)

        string = string.substring(0, index) + stringCsq.substring(startIndex, endIndex) + string.substring(index)
        return this
    }
}


/**
 * Clears the content of this string builder making it empty and returns this instance.
 *
 * @sample samples.text.Strings.clearStringBuilder
 */
@SinceKotlin("1.3")
@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "NOTHING_TO_INLINE")
public actual inline fun StringBuilder.clear(): StringBuilder = this.clear()

/**
 * Sets the character at the specified [index] to the specified [konstue].
 *
 * @throws IndexOutOfBoundsException if [index] is out of bounds of this string builder.
 */
@SinceKotlin("1.4")
@WasExperimental(ExperimentalStdlibApi::class)
@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "NOTHING_TO_INLINE")
public actual inline operator fun StringBuilder.set(index: Int, konstue: Char) = this.set(index, konstue)

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
@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "NOTHING_TO_INLINE")
public actual inline fun StringBuilder.setRange(startIndex: Int, endIndex: Int, konstue: String): StringBuilder =
    this.setRange(startIndex, endIndex, konstue)

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
@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "NOTHING_TO_INLINE")
public actual inline fun StringBuilder.deleteAt(index: Int): StringBuilder = this.deleteAt(index)

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
@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "NOTHING_TO_INLINE")
public actual inline fun StringBuilder.deleteRange(startIndex: Int, endIndex: Int): StringBuilder = this.deleteRange(startIndex, endIndex)

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
@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "NOTHING_TO_INLINE", "ACTUAL_FUNCTION_WITH_DEFAULT_ARGUMENTS")
public actual inline fun StringBuilder.toCharArray(destination: CharArray, destinationOffset: Int = 0, startIndex: Int = 0, endIndex: Int = this.length) =
    this.toCharArray(destination, destinationOffset, startIndex, endIndex)

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
@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "NOTHING_TO_INLINE")
public actual inline fun StringBuilder.appendRange(konstue: CharArray, startIndex: Int, endIndex: Int): StringBuilder =
    this.appendRange(konstue, startIndex, endIndex)

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
@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "NOTHING_TO_INLINE")
public actual inline fun StringBuilder.appendRange(konstue: CharSequence, startIndex: Int, endIndex: Int): StringBuilder =
    this.appendRange(konstue, startIndex, endIndex)

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
@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "NOTHING_TO_INLINE")
public actual inline fun StringBuilder.insertRange(index: Int, konstue: CharArray, startIndex: Int, endIndex: Int): StringBuilder =
    this.insertRange(index, konstue, startIndex, endIndex)

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
@Suppress("EXTENSION_SHADOWED_BY_MEMBER", "NOTHING_TO_INLINE")
public actual inline fun StringBuilder.insertRange(index: Int, konstue: CharSequence, startIndex: Int, endIndex: Int): StringBuilder =
    this.insertRange(index, konstue, startIndex, endIndex)
