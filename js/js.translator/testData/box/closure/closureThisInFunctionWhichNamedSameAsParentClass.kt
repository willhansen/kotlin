// EXPECTED_REACHABLE_NODES: 1288
package foo

class B(konst b: String)

class A(konst a: String) {
    fun B.A() = { a + b }

    fun foo(a: B) = a.A()()
}

fun box(): String {
    konst bar = A("bar")
    konst baz = B("baz")

    konst r = bar.foo(baz)
    if (r != "barbaz") return "$r";

    return "OK"
}