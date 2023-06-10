// EXPECTED_REACHABLE_NODES: 1290
package foo

object A {
    konst x = 23
    konst y = { x }
    fun foo() = { x }
}

fun box(): String {
    assertEquals(23, A.foo()())
    assertEquals(23, A.y())
    return "OK"
}