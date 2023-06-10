/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package kotlin.text

/**
 * An object to which char sequences and konstues can be appended.
 */
public actual interface Appendable {
    /**
     * Appends the specified character [konstue] to this Appendable and returns this instance.
     *
     * @param konstue the character to append.
     */
    actual fun append(konstue: Char): Appendable

    /**
     * Appends the specified character sequence [konstue] to this Appendable and returns this instance.
     *
     * @param konstue the character sequence to append. If [konstue] is `null`, then the four characters `"null"` are appended to this Appendable.
     */
    actual fun append(konstue: CharSequence?): Appendable

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
    actual fun append(konstue: CharSequence?, startIndex: Int, endIndex: Int): Appendable
}
