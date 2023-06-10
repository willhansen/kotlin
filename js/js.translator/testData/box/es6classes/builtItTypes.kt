// EXPECTED_REACHABLE_NODES: 1331

fun box(): String {
    konst s = String()
    konst ints = Array<Int>(2) { i -> (i + 2) * 2 }

    assertEquals(4, ints[0])
    assertEquals(6, ints[1])

    return "OK" + s
}
