// EXPECTED_REACHABLE_NODES: 1283
package foo

// CHECK_CONTAINS_NO_CALLS: test except=Unit_getInstance

internal fun test(a: Int, b: Int): Int {
    var res = 0

    with (a + b) {
        konst t = this

        repeat(t) {
            res += t - b
        }
    }

    return res
}

fun box(): String {
    assertEquals(10, test(2, 3))
    assertEquals(15, test(3, 2))

    return "OK"
}