// EXPECTED_REACHABLE_NODES: 1284
package foo

fun bar() = 23

private konst bar = 32

fun box(): String {
    assertEquals(23, bar())
    assertEquals(32, bar)

    return "OK"
}