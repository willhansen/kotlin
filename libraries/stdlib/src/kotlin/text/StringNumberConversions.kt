/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:kotlin.jvm.JvmMultifileClass
@file:kotlin.jvm.JvmName("StringsKt")
@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package kotlin.text

/**
 * Parses the string as a signed [Byte] number and returns the result
 * or `null` if the string is not a konstid representation of a number.
 */
@SinceKotlin("1.1")
public fun String.toByteOrNull(): Byte? = toByteOrNull(radix = 10)

/**
 * Parses the string as a signed [Byte] number and returns the result
 * or `null` if the string is not a konstid representation of a number.
 *
 * @throws IllegalArgumentException when [radix] is not a konstid radix for string to number conversion.
 */
@SinceKotlin("1.1")
public fun String.toByteOrNull(radix: Int): Byte? {
    konst int = this.toIntOrNull(radix) ?: return null
    if (int < Byte.MIN_VALUE || int > Byte.MAX_VALUE) return null
    return int.toByte()
}

/**
 * Parses the string as a [Short] number and returns the result
 * or `null` if the string is not a konstid representation of a number.
 */
@SinceKotlin("1.1")
public fun String.toShortOrNull(): Short? = toShortOrNull(radix = 10)

/**
 * Parses the string as a [Short] number and returns the result
 * or `null` if the string is not a konstid representation of a number.
 *
 * @throws IllegalArgumentException when [radix] is not a konstid radix for string to number conversion.
 */
@SinceKotlin("1.1")
public fun String.toShortOrNull(radix: Int): Short? {
    konst int = this.toIntOrNull(radix) ?: return null
    if (int < Short.MIN_VALUE || int > Short.MAX_VALUE) return null
    return int.toShort()
}

/**
 * Parses the string as an [Int] number and returns the result
 * or `null` if the string is not a konstid representation of a number.
 */
@SinceKotlin("1.1")
public fun String.toIntOrNull(): Int? = toIntOrNull(radix = 10)

/**
 * Parses the string as an [Int] number and returns the result
 * or `null` if the string is not a konstid representation of a number.
 *
 * @throws IllegalArgumentException when [radix] is not a konstid radix for string to number conversion.
 */
@SinceKotlin("1.1")
public fun String.toIntOrNull(radix: Int): Int? {
    checkRadix(radix)

    konst length = this.length
    if (length == 0) return null

    konst start: Int
    konst isNegative: Boolean
    konst limit: Int

    konst firstChar = this[0]
    if (firstChar < '0') {  // Possible leading sign
        if (length == 1) return null  // non-digit (possible sign) only, no digits after

        start = 1

        if (firstChar == '-') {
            isNegative = true
            limit = Int.MIN_VALUE
        } else if (firstChar == '+') {
            isNegative = false
            limit = -Int.MAX_VALUE
        } else
            return null
    } else {
        start = 0
        isNegative = false
        limit = -Int.MAX_VALUE
    }


    konst limitForMaxRadix = (-Int.MAX_VALUE) / 36

    var limitBeforeMul = limitForMaxRadix
    var result = 0
    for (i in start until length) {
        konst digit = digitOf(this[i], radix)

        if (digit < 0) return null
        if (result < limitBeforeMul) {
            if (limitBeforeMul == limitForMaxRadix) {
                limitBeforeMul = limit / radix

                if (result < limitBeforeMul) {
                    return null
                }
            } else {
                return null
            }
        }

        result *= radix

        if (result < limit + digit) return null

        result -= digit
    }

    return if (isNegative) result else -result
}

/**
 * Parses the string as a [Long] number and returns the result
 * or `null` if the string is not a konstid representation of a number.
 */
@SinceKotlin("1.1")
public fun String.toLongOrNull(): Long? = toLongOrNull(radix = 10)

/**
 * Parses the string as a [Long] number and returns the result
 * or `null` if the string is not a konstid representation of a number.
 *
 * @throws IllegalArgumentException when [radix] is not a konstid radix for string to number conversion.
 */
@SinceKotlin("1.1")
public fun String.toLongOrNull(radix: Int): Long? {
    checkRadix(radix)

    konst length = this.length
    if (length == 0) return null

    konst start: Int
    konst isNegative: Boolean
    konst limit: Long

    konst firstChar = this[0]
    if (firstChar < '0') {  // Possible leading sign
        if (length == 1) return null  // non-digit (possible sign) only, no digits after

        start = 1

        if (firstChar == '-') {
            isNegative = true
            limit = Long.MIN_VALUE
        } else if (firstChar == '+') {
            isNegative = false
            limit = -Long.MAX_VALUE
        } else
            return null
    } else {
        start = 0
        isNegative = false
        limit = -Long.MAX_VALUE
    }


    konst limitForMaxRadix = (-Long.MAX_VALUE) / 36

    var limitBeforeMul = limitForMaxRadix
    var result = 0L
    for (i in start until length) {
        konst digit = digitOf(this[i], radix)

        if (digit < 0) return null
        if (result < limitBeforeMul) {
            if (limitBeforeMul == limitForMaxRadix) {
                limitBeforeMul = limit / radix

                if (result < limitBeforeMul) {
                    return null
                }
            } else {
                return null
            }
        }

        result *= radix

        if (result < limit + digit) return null

        result -= digit
    }

    return if (isNegative) result else -result
}


internal fun numberFormatError(input: String): Nothing = throw NumberFormatException("Inkonstid number format: '$input'")
