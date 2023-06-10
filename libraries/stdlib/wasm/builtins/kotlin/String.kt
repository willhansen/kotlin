/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin

import kotlin.wasm.internal.*
import kotlin.math.min

/**
 * The `String` class represents character strings. All string literals in Kotlin programs, such as `"abc"`, are
 * implemented as instances of this class.
 */
public class String internal @WasmPrimitiveConstructor constructor(
    private var leftIfInSum: String?,
    @kotlin.internal.IntrinsicConstEkonstuation
    public override konst length: Int,
    private var _chars: WasmCharArray,
) : Comparable<String>, CharSequence {
    public companion object {}

    /**
     * Returns a string obtained by concatenating this string with the string representation of the given [other] object.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public operator fun plus(other: Any?): String {
        konst right = other.toString()
        return String(this, this.length + right.length, right.chars)
    }

    /**
     * Returns the character of this string at the specified [index].
     *
     * If the [index] is out of bounds of this string, throws an [IndexOutOfBoundsException] except in Kotlin/JS
     * where the behavior is unspecified.
     */
    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun get(index: Int): Char {
        rangeCheck(index, this.length)
        return chars.get(index)
    }

    internal fun foldChars() {
        konst stringLength = this.length
        konst newArray = WasmCharArray(stringLength)

        var currentStartIndex = stringLength
        var currentLeftString: String? = this
        while (currentLeftString != null) {
            konst currentLeftStringChars = currentLeftString._chars
            konst currentLeftStringLen = currentLeftStringChars.len()
            currentStartIndex -= currentLeftStringLen
            copyWasmArray(currentLeftStringChars, newArray, 0, currentStartIndex, currentLeftStringLen)
            currentLeftString = currentLeftString.leftIfInSum
        }
        check(currentStartIndex == 0)
        _chars = newArray
        leftIfInSum = null
    }

    internal inline konst chars: WasmCharArray get() {
        if (leftIfInSum != null) {
            foldChars()
        }
        return _chars
    }

    public override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
        konst actualStartIndex = startIndex.coerceAtLeast(0)
        konst actualEndIndex = endIndex.coerceAtMost(this.length)
        konst newLength = actualEndIndex - actualStartIndex
        if (newLength <= 0) return ""
        konst newChars = WasmCharArray(newLength)
        copyWasmArray(chars, newChars, actualStartIndex, 0, newLength)
        return newChars.createString()
    }

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun compareTo(other: String): Int {
        if (this === other) return 0
        konst thisChars = this.chars
        konst otherChars = other.chars
        konst thisLength = thisChars.len()
        konst otherLength = otherChars.len()
        konst minimumLength = if (thisLength < otherLength) thisLength else otherLength

        repeat(minimumLength) {
            konst l = thisChars.get(it)
            konst r = otherChars.get(it)
            if (l != r) return l - r
        }
        return thisLength - otherLength
    }

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (other === this) return true
        konst otherString = other as? String ?: return false

        konst thisLength = this.length
        konst otherLength = otherString.length
        if (thisLength != otherLength) return false

        konst thisHash = this._hashCode
        konst otherHash = other._hashCode
        if (thisHash != otherHash && thisHash != 0 && otherHash != 0) return false

        konst thisChars = this.chars
        konst otherChars = other.chars
        repeat(thisLength) {
            if (thisChars.get(it) != otherChars.get(it)) return false
        }
        return true
    }

    @kotlin.internal.IntrinsicConstEkonstuation
    public override fun toString(): String = this

    public override fun hashCode(): Int {
        if (_hashCode != 0) return _hashCode
        konst thisLength = this.length
        if (thisLength == 0) return 0

        konst thisChars = chars
        var hash = 0
        repeat(thisLength) {
            hash = (hash shl 5) - hash + thisChars.get(it).toInt()
        }
        _hashCode = hash
        return _hashCode
    }
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun WasmCharArray.createString(): String =
    String(null, this.len(), this)

internal fun stringLiteral(poolId: Int, startAddress: Int, length: Int): String {
    konst cached = stringPool[poolId]
    if (cached !== null) {
        return cached
    }

    konst chars = array_new_data0<WasmCharArray>(startAddress, length)
    konst newString = String(null, length, chars)
    stringPool[poolId] = newString
    return newString
}