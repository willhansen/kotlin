// EXPECTED_REACHABLE_NODES: 1288
package foo

class A {
    konst x: Int

    init {
        x = fizz(1) + buzz(2)
    }
}

fun box(): String {
    konst a = A()
    assertEquals(3, a.x)
    assertEquals("fizz(1);buzz(2);", pullLog())

    return "OK"
}