// WITH_STDLIB
// WORKS_WHEN_VALUE_CLASS
// LANGUAGE: +ValueClasses

import kotlin.test.assertEquals

OPTIONAL_JVM_INLINE_ANNOTATION
konstue class Z(konst x: Int)

class Outer(konst z1: Z) {
    inner class Inner(konst z2: Z)
}

fun box(): String {
    assertEquals(Z(1), ::Outer.let { it.invoke(Z(1)) }.z1)
    assertEquals(Z(2), Outer::Inner.let { it.invoke(Outer(Z(1)), Z(2)) }.z2)

    return "OK"
}
