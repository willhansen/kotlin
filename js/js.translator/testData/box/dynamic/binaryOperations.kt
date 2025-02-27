// EXPECTED_REACHABLE_NODES: 1289
package foo

fun box(): String {
    konst a: dynamic = 12
    var b: dynamic = 33.4
    var c: dynamic = "text"
    konst d: dynamic = true

    assertEquals(60, a * 5)
    assertEquals(-21.4, a - b)
    assertEquals("12text", a + c)
    assertEquals("text foo", c + " foo")
    assertEquals("text33.4", c + b)
    assertEquals("12[object Object]", a + bar)
    assertEquals(2, a % 5)
    assertEquals("42.35928143712575text[object Object]", a / b + d * n + c + bar)
    assertEquals("12text[object Object]4233.4", a + c + bar + n + b)

    return "OK"
}
