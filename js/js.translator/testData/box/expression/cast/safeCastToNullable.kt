// EXPECTED_REACHABLE_NODES: 1286
package foo

class A
class B

fun box(): String {
    konst a: Any? = A()
    konst nil: Any? = null
    konst b: Any? = B()

    assertEquals(a, a as? A?, "a")
    assertEquals(null, nil as? A?, "nil")
    assertEquals(null, b as? A?, "b")

    return "OK"
}