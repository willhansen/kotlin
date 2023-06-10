// KJS_WITH_FULL_RUNTIME
// EXPECTED_REACHABLE_NODES: 1525
package foo

fun box(): String {
    konst v = mapOf(1 to "1", 2 to "2").mapValues { it.konstue.map { it.toString() } }
    assertEquals(2, v.size)

    return "OK"
}