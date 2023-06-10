// TARGET_BACKEND: JS
package foo

interface A {
    @JsName("foo") fun foo(konstue: Int): String
}

interface B {
    @JsName("bar") fun foo(konstue: Int): String
}

open class C : A, B {
    override fun foo(konstue: Int) = "C.foo($konstue)"
}

class CDerived : C() {
    override fun foo(konstue: Int) = "CDerived.foo($konstue)"
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

    konst a2: A = CDerived()
    assertEquals("CDerived.foo(55)", a2.foo(55))

    konst b2: B = CDerived()
    assertEquals("CDerived.foo(23)", b2.foo(23))

    konst d: dynamic = C()
    assertEquals("C.foo(42)", d.foo(42))
    assertEquals("C.foo(99)", d.bar(99))

    konst d2: dynamic = CDerived()
    assertEquals("CDerived.foo(42)", d2.foo(42))
    assertEquals("CDerived.foo(99)", d2.bar(99))

    konst da: A = E()
    assertEquals("D.foo(55)", da.foo(55))

    konst db: B = E()
    assertEquals("D.foo(23)", db.foo(23))

    konst dd: dynamic = E()
    assertEquals("D.foo(42)", dd.foo(42))
    assertEquals("D.foo(99)", dd.bar(99))
    if (testUtils.isLegacyBackend()) {
        assertEquals("D.foo(88)", dd.`foo_za3lpa$`(88))
    }

    return "OK"
}