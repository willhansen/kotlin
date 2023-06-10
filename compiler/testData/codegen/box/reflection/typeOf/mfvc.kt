// TARGET_BACKEND: JVM_IR
// WITH_REFLECT
// LANGUAGE: +ValueClasses

package test

import kotlin.reflect.KType
import kotlin.reflect.typeOf
import kotlin.test.assertEquals

@JvmInline
konstue class Z(konst konstue1: String, konst konstue2: String)

fun check(expected: String, actual: KType) {
    assertEquals(expected, actual.toString())
}

fun box(): String {
    check("test.Z", typeOf<Z>())
    check("test.Z?", typeOf<Z?>())
    check("kotlin.Array<test.Z>", typeOf<Array<Z>>())
    check("kotlin.Array<test.Z?>", typeOf<Array<Z?>>())

    return "OK"
}
