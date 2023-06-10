// EXPECTED_REACHABLE_NODES: 1292
// KT-4130 object fields are not ekonstuated correctly

package foo

class Foo() {
    companion object {
        konst bar = "Foo.bar ";
        var boo = "FAIL";
        fun baz() = "Foo.baz() "

        fun testImplicitThis(): String {
            boo = "Implicit"
            return baz() + bar + boo
        }
        fun testExplicitThis(): String {
            this.boo = "Explicit"
            return this.baz() + this.bar + this.boo
        }
    }

    konst a = bar
    konst b = Foo.bar
    konst c = baz()
    konst d = Foo.baz()
    konst e: String
    konst f: String

    init {
        e = bar
        f = Foo.bar
        boo = "O"
        Foo.boo += "K"
    }
}

fun box(): String {
    assertEquals("Foo.baz() Foo.bar Implicit", Foo.testImplicitThis(), "testImplicitThis")
    assertEquals("Foo.baz() Foo.bar Explicit", Foo.testExplicitThis(), "testExplicitThis")

    konst foo = Foo()
    assertEquals("Foo.bar ", foo.a, "foo.a")
    assertEquals("Foo.bar ", foo.b, "foo.b")
    assertEquals("Foo.baz() ", foo.c, "foo.c")
    assertEquals("Foo.baz() ", foo.d, "foo.d")
    assertEquals("Foo.bar ", foo.e, "foo.e")
    assertEquals("Foo.bar ", foo.f, "foo.f")

    assertEquals("OK", Foo.boo, "Foo.boo")
    assertEquals("Foo.bar ", Foo.bar, "Foo.bar")
    assertEquals("Foo.baz() ", Foo.baz(), "Foo.baz()")

    return "OK"
}
