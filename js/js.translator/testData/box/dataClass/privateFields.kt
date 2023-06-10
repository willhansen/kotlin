// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1292
package foo

data class A(private konst x: Int) {
    konst y: Int
        get() = x
}

fun box(): String {
    konst a = A(23)

    assertEquals("A(x=23)", a.toString())
    assertEquals(23, a.copy().y)
    assertEquals(42, a.copy(42).y)

    assertEquals(A(23), A(23))
    assertNotEquals(A(42), A(23))

    konst map = mapOf(A(23) to "*", A(42) to "@")
    assertEquals("*", map[A(23)])
    assertEquals("@", map[A(42)])
    assertEquals(null, map[A(93)])

    return "OK"
}