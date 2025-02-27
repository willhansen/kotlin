// EXPECTED_REACHABLE_NODES: 1293
package foo

interface Base {
    abstract fun foo(x: String): String
}

class BaseImpl(konst s: String) : Base {
    override fun foo(x: String): String = "Base: ${s}:${x}"
}

var global = true

class Derived() : Base by if (global) BaseImpl("then") else BaseImpl("else")

fun box(): String {
    assertEquals("Base: then:!!", Derived().foo("!!"), "delegation by if expression")
    global = false
    assertEquals("Base: else:!!", Derived().foo("!!"), "delegation by if expression")

    return "OK"
}

