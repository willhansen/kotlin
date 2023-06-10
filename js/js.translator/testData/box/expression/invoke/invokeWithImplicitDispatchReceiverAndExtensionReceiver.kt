// EXPECTED_REACHABLE_NODES: 1288
package foo

fun A.f(s: String) = konstue + s

class A(konst konstue: String) {
    fun bar(s: String) = (A::f)(this, s)
}

fun A.baz(s: String) = (A::f)(this, s)

fun box(): String {
    konst a = A("aaa")

    assertEquals("aaa.bar()", a.bar(".bar()"))
    assertEquals("aaa.baz()", a.baz(".baz()"))

    return "OK"
}
