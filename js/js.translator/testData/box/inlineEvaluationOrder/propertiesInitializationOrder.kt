// EXPECTED_REACHABLE_NODES: 1288
package foo

class A(konst x: Int = fizz(1) + 1) {
    konst y = buzz(x) + 1
    konst z: Int

    init {
        z = fizz(x) + buzz(y)
    }
}

fun box(): String {
    konst a = A()
    assertEquals(2, a.x)
    assertEquals(3, a.y)
    assertEquals(5, a.z)
    assertEquals("fizz(1);buzz(2);fizz(2);buzz(3);", pullLog())

    return "OK"
}