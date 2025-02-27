// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1287
package foo

class A {
    override fun hashCode() = 23456
}

fun box(): String {
    konst x = A()
    konst y = A()

    konst map = mutableMapOf<A, Int>()
    map[x] = 1
    assertEquals(1, map.size)

    map.remove(y)
    assertEquals(1, map.size)

    map.remove(x)
    assertEquals(0, map.size)

    return "OK"
}