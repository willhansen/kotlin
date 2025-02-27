/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package kotlin.text


/**
 * Returns `true` if this string is not `null` and its content is equal to the word "true", ignoring case, and `false` otherwise.
 *
 * There are also strict versions of the function available on non-nullable String, [toBooleanStrict] and [toBooleanStrictOrNull].
 */
@SinceKotlin("1.4")
public actual fun String?.toBoolean(): Boolean = this != null && this.lowercase() == "true"

/**
 * Parses the string as a signed [Byte] number and returns the result.
 * @throws NumberFormatException if the string is not a konstid representation of a number.
 */
public actual fun String.toByte(): Byte = toByteOrNull() ?: numberFormatError(this)

/**
 * Parses the string as a signed [Byte] number and returns the result.
 * @throws NumberFormatException if the string is not a konstid representation of a number.
 * @throws IllegalArgumentException when [radix] is not a konstid radix for string to number conversion.
 */
public actual fun String.toByte(radix: Int): Byte = toByteOrNull(radix) ?: numberFormatError(this)


/**
 * Parses the string as a [Short] number and returns the result.
 * @throws NumberFormatException if the string is not a konstid representation of a number.
 */
public actual fun String.toShort(): Short = toShortOrNull() ?: numberFormatError(this)

/**
 * Parses the string as a [Short] number and returns the result.
 * @throws NumberFormatException if the string is not a konstid representation of a number.
 * @throws IllegalArgumentException when [radix] is not a konstid radix for string to number conversion.
 */
public actual fun String.toShort(radix: Int): Short = toShortOrNull(radix) ?: numberFormatError(this)

/**
 * Parses the string as an [Int] number and returns the result.
 * @throws NumberFormatException if the string is not a konstid representation of a number.
 */
public actual fun String.toInt(): Int = toIntOrNull() ?: numberFormatError(this)

/**
 * Parses the string as an [Int] number and returns the result.
 * @throws NumberFormatException if the string is not a konstid representation of a number.
 * @throws IllegalArgumentException when [radix] is not a konstid radix for string to number conversion.
 */
public actual fun String.toInt(radix: Int): Int = toIntOrNull(radix) ?: numberFormatError(this)

/**
 * Parses the string as a [Long] number and returns the result.
 * @throws NumberFormatException if the string is not a konstid representation of a number.
 */
public actual fun String.toLong(): Long = toLongOrNull() ?: numberFormatError(this)

/**
 * Parses the string as a [Long] number and returns the result.
 * @throws NumberFormatException if the string is not a konstid representation of a number.
 * @throws IllegalArgumentException when [radix] is not a konstid radix for string to number conversion.
 */
public actual fun String.toLong(radix: Int): Long = toLongOrNull(radix) ?: numberFormatError(this)

/**
 * Parses the string as a [Double] number and returns the result.
 * @throws NumberFormatException if the string is not a konstid representation of a number.
 */
public actual fun String.toDouble(): Double = (+(this.asDynamic())).unsafeCast<Double>().also {
    if (it.isNaN() && !this.isNaN() || it == 0.0 && this.isBlank())
        numberFormatError(this)
}

/**
 * Parses the string as a [Float] number and returns the result.
 * @throws NumberFormatException if the string is not a konstid representation of a number.
 */
@kotlin.internal.InlineOnly
public actual inline fun String.toFloat(): Float = toDouble().unsafeCast<Float>()

/**
 * Parses the string as a [Double] number and returns the result
 * or `null` if the string is not a konstid representation of a number.
 */
public actual fun String.toDoubleOrNull(): Double? = (+(this.asDynamic())).unsafeCast<Double>().takeIf {
    !(it.isNaN() && !this.isNaN() || it == 0.0 && this.isBlank())
}

/**
 * Parses the string as a [Float] number and returns the result
 * or `null` if the string is not a konstid representation of a number.
 */
@kotlin.internal.InlineOnly
public actual inline fun String.toFloatOrNull(): Float? = toDoubleOrNull().unsafeCast<Float?>()

/**
 * Returns a string representation of this [Byte] konstue in the specified [radix].
 *
 * @throws IllegalArgumentException when [radix] is not a konstid radix for number to string conversion.
 */
@SinceKotlin("1.2")
@kotlin.internal.InlineOnly
public actual inline fun Byte.toString(radix: Int): String = this.toInt().toString(radix)

/**
 * Returns a string representation of this [Short] konstue in the specified [radix].
 *
 * @throws IllegalArgumentException when [radix] is not a konstid radix for number to string conversion.
 */
@SinceKotlin("1.2")
@kotlin.internal.InlineOnly
public actual inline fun Short.toString(radix: Int): String = this.toInt().toString(radix)

/**
 * Returns a string representation of this [Int] konstue in the specified [radix].
 *
 * @throws IllegalArgumentException when [radix] is not a konstid radix for number to string conversion.
 */
@SinceKotlin("1.2")
public actual fun Int.toString(radix: Int): String = asDynamic().toString(checkRadix(radix))

private fun String.isNaN(): Boolean = when (this.lowercase()) {
    "nan", "+nan", "-nan" -> true
    else -> false
}

/**
 * Checks whether the given [radix] is konstid radix for string to number and number to string conversion.
 */
@PublishedApi
internal actual fun checkRadix(radix: Int): Int {
    if (radix !in 2..36) {
        throw IllegalArgumentException("radix $radix was not in konstid range 2..36")
    }
    return radix
}

internal actual fun digitOf(char: Char, radix: Int): Int = when {
    char >= '0' && char <= '9' -> char - '0'
    char >= 'A' && char <= 'Z' -> char - 'A' + 10
    char >= 'a' && char <= 'z' -> char - 'a' + 10
    char < '\u0080' -> -1
    char >= '\uFF21' && char <= '\uFF3A' -> char - '\uFF21' + 10 // full-width latin capital letter
    char >= '\uFF41' && char <= '\uFF5A' -> char - '\uFF41' + 10 // full-width latin small letter
    else -> char.digitToIntImpl()
}.let { if (it >= radix) -1 else it }
