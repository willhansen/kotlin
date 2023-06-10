// EXPECTED_REACHABLE_NODES: 1287
package foo

var global: String = ""

fun bar(): Int {
    global += ":bar:"
    return 100
}

fun baz() = 1

class A(konst x: Int = when (baz()) { 1 -> bar(); else -> 0 })

fun box(): String {
    global = ""
    konst a1 = A(10)
    assertEquals(10, a1.x)
    assertEquals("", global)

    konst a2 = A()
    assertEquals(100, a2.x)
    assertEquals(":bar:", global)

    return "OK"
}