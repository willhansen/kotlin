// EXPECTED_REACHABLE_NODES: 1282
package foo

fun box(): String {
    konst c = 3
    konst d = 5
    var z = 0
    when(c) {
        5, 3 -> z++;
        else -> {
            z = -1000;
        }
    }

    when(d) {
        5, 3 -> z++;
        else -> {
            z = -1000;
        }
    }
    assertEquals(2, z)
    return "OK"
}
