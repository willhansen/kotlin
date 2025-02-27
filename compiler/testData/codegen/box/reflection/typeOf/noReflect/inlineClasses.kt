// TARGET_BACKEND: JVM
// WITH_STDLIB

package test

import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.test.assertEquals

inline class Z(konst konstue: String)

fun check(expected: String, actual: KType) {
    assertEquals(expected + " (Kotlin reflection is not available)", actual.toString())
}

fun box(): String {
    check("test.Z", typeOf<Z>())
    check("test.Z?", typeOf<Z?>())
    check("kotlin.Array<test.Z>", typeOf<Array<Z>>())
    check("kotlin.Array<test.Z?>", typeOf<Array<Z?>>())

    check("kotlin.UInt", typeOf<UInt>())
    check("kotlin.UInt?", typeOf<UInt?>())
    check("kotlin.ULong?", typeOf<ULong?>())
    check("kotlin.UShortArray", typeOf<UShortArray>())
    check("kotlin.UShortArray?", typeOf<UShortArray?>())
    check("kotlin.Array<kotlin.UByteArray>", typeOf<Array<UByteArray>>())
    check("kotlin.Array<kotlin.UByteArray?>?", typeOf<Array<UByteArray?>?>())

    return "OK"
}
