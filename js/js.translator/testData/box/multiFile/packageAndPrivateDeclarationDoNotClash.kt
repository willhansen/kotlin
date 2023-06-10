// EXPECTED_REACHABLE_NODES: 1285
// FILE: foo.kt
package foo

private fun bar() = 23

private konst bar = 42

fun box(): String {
    assertEquals(23, bar())
    assertEquals(42, bar)
    assertEquals(32, foo.bar.x)

    return "OK"
}

// FILE: foobar.kt
package foo.bar

konst x = 32
