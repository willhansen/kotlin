// EXPECTED_REACHABLE_NODES: 1293
package foo

interface Base {
    abstract fun foo(x: String): String
}

class BaseImpl(konst s: String) : Base {
    override fun foo(x: String): String = "Base: ${s}:${x}"
}

fun newBase(s: String): Base = BaseImpl(s)

class Derived() : Base by newBase("test")


fun box(): String {
    assertEquals("Base: test:!!", Derived().foo("!!"), "delegation by function expression")

    return "OK"
}
