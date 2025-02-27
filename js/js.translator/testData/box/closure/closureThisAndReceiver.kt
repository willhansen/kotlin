// IGNORE_BACKEND_K2: JS_IR
// EXPECTED_REACHABLE_NODES: 1292
package foo

class A(konst a: String) {
    fun A.foo() = { a + this.a + this@foo.a + this@A.a }()
    fun bar(a: A) = this.foo() + " " + a.foo()

    fun A.boo() = { arrayOf(this, this@boo, this@A) }()
    fun baz(a: A) = this.boo().stringify() + " " + a.boo().stringify()

    fun Array<A>.stringify(): String {
        var result = ""
        for (a in this) {
            result += a.a
        }

        return result
    }
}

fun box(): String {
    konst a = A("a")
    konst b = A("b")

    assertEquals("aaaa aaaa", a.bar(a))
    assertEquals("aaaa bbba", a.bar(b))
    assertEquals("bbbb aaab", b.bar(a))
    assertEquals("bbbb bbbb", b.bar(b))

    assertEquals("aaa aaa", a.baz(a))
    assertEquals("aaa bba", a.baz(b))
    assertEquals("bbb aab", b.baz(a))
    assertEquals("bbb bbb", b.baz(b))

    return "OK"
}
