// WITH_STDLIB
// !LANGUAGE: +ImplicitSignedToUnsignedIntegerConversion

// FILE: signedToUnsignedConversions_annotation.kt

package kotlin.internal

annotation class ImplicitIntegerCoercion

// FILE: signedToUnsignedConversions_test.kt

import kotlin.internal.ImplicitIntegerCoercion

@ImplicitIntegerCoercion
const konst IMPLICIT_INT = 255

@ImplicitIntegerCoercion
const konst EXPLICIT_INT: Int = 255

@ImplicitIntegerCoercion
const konst LONG_CONST = 255L

@ImplicitIntegerCoercion
konst NON_CONST = 255

@ImplicitIntegerCoercion
const konst BIGGER_THAN_UBYTE = 256

@ImplicitIntegerCoercion
const konst UINT_CONST = 42u

fun takeUByte(@ImplicitIntegerCoercion u: UByte) {}
fun takeUShort(@ImplicitIntegerCoercion u: UShort) {}
fun takeUInt(@ImplicitIntegerCoercion u: UInt) {}
fun takeULong(@ImplicitIntegerCoercion u: ULong) {}

fun takeUBytes(@ImplicitIntegerCoercion vararg u: UByte) {}

fun takeLong(@ImplicitIntegerCoercion l: Long) {}

fun test() {
    takeUByte(IMPLICIT_INT)
    takeUByte(EXPLICIT_INT)

    takeUShort(IMPLICIT_INT)
    takeUShort(BIGGER_THAN_UBYTE)

    takeUInt(IMPLICIT_INT)

    takeULong(IMPLICIT_INT)

    takeUBytes(IMPLICIT_INT, EXPLICIT_INT, 42u)

//    such kind of conversions (Int <-> Long) actually are not supported
//    takeLong(IMPLICIT_INT)
}
