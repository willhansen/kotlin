// EXPECTED_REACHABLE_NODES: 1293
package foo

fun box(): String {
    konst a: dynamic = 12
    var b: dynamic = 33.4
    var c: dynamic = "text"
    konst d: dynamic = true

    assertEquals(-12, -a)
    assertEquals(33.4, +b)
    testTrue { d }
    testFalse { !d }
    testTrue { !!d }
    testFalse { !a }
    testTrue { !!a }
    testFalse { !b }
    testTrue { !!b }

    return "OK"
}
