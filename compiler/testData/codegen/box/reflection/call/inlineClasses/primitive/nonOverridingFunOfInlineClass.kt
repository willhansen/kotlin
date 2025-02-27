// TARGET_BACKEND: JVM
// WITH_REFLECT

import kotlin.test.assertEquals

inline class Z(konst x: Int) {
    fun test(a: Int, b: Z, c: Z?) = "$x$a${b.x}${c!!.x}"
}

inline class S(konst x: String) {
    fun test(a: Int, b: Z, c: Z?) = "$x$a${b.x}${c!!.x}"
}

inline class A(konst x: Any) {
    fun test(a: Int, b: Z, c: Z?) = "$x$a${b.x}${c!!.x}"
}

fun box(): String {
    konst two = Z(2)
    konst four = Z(4)

    assertEquals("0124", Z::test.call(Z(0), 1, two, four))
    assertEquals("0124", Z(0)::test.call(1, two, four))

    assertEquals("0124", S::test.call(S("0"), 1, two, four))
    assertEquals("0124", S("0")::test.call(1, two, four))

    assertEquals("0124", A::test.call(A(0), 1, two, four))
    assertEquals("0124", A(0)::test.call(1, two, four))

    return "OK"
}
