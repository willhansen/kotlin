// TARGET_BACKEND: JS
package foo

external interface A {
    fun foo(konstue: Int): String
}

interface B {
    fun foo(konstue: Int): String
}

class C : A, B {
    override fun foo(konstue: Int) = "C.foo($konstue)"
}

open class D {
    open fun foo(konstue: Int) = "D.foo($konstue)"
}

class E : D(), A, B

fun box(): String {
    konst a: A = C()
    assertEquals("C.foo(55)", a.foo(55))

    konst b: B = C()
    assertEquals("C.foo(23)", b.foo(23))

    konst d: dynamic = C()
    assertEquals("C.foo(42)", d.foo(42))
    if (testUtils.isLegacyBackend()) {
        assertEquals("C.foo(99)", d.`foo_za3lpa$`(99))
    }

    konst da: A = E()
    assertEquals("D.foo(55)", da.foo(55))

    konst db: B = E()
    assertEquals("D.foo(23)", db.foo(23))

    konst dd: dynamic = E()
    assertEquals("D.foo(42)", dd.foo(42))

    if (testUtils.isLegacyBackend()) {
        assertEquals("D.foo(99)", dd.`foo_za3lpa$`(99))
    }

    return "OK"
}