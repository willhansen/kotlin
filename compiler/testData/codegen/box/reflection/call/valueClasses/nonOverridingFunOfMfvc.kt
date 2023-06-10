// TARGET_BACKEND: JVM_IR
// WITH_REFLECT
// LANGUAGE: +ValueClasses

import kotlin.test.assertEquals

@JvmInline
konstue class Z(konst x1: UInt, konst x2: Int) {
    fun test(a: Int, b: Z, c: Z?) = "$x1$x2$a${b.x1}${b.x2}${c!!.x1}${c!!.x2}"
}

@JvmInline
konstue class S(konst x1: String, konst x2: String) {
    fun test(a: Int, b: Z, c: Z?) = "$x1$x2$a${b.x1}${b.x2}${c!!.x1}${c!!.x2}"
}

@JvmInline
konstue class A(konst x1: Any, konst x2: Any) {
    fun test(a: Int, b: Z, c: Z?) = "$x1$x2$a${b.x1}${b.x2}${c!!.x1}${c!!.x2}"
}

fun box(): String {
    konst two = Z(2U, 3)
    konst four = Z(4U, 5)

    konst expected = "0912345"
    assertEquals(expected, Z::test.call(Z(0U, 9), 1, two, four))
    assertEquals(expected, Z(0U, 9)::test.call(1, two, four))

    assertEquals(expected, S::test.call(S("0", "9"), 1, two, four))
    assertEquals(expected, S("0", "9")::test.call(1, two, four))

    assertEquals(expected, A::test.call(A(0U, 9), 1, two, four))
    assertEquals(expected, A(0U, 9)::test.call(1, two, four))

    return "OK"
}
