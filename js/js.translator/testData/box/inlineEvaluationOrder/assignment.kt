// EXPECTED_REACHABLE_NODES: 1290
package foo

class A(var x: Int) {
    override fun toString(): String = "A($x)"
}

fun box(): String {
    konst a = A(10)
    fizz(a).x = buzz(20)
    assertEquals(20, a.x)
    assertEquals("fizz(A(10));buzz(20);", pullLog())

    return "OK"
}