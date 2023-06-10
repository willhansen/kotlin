// EXPECTED_REACHABLE_NODES: 1282
// KT-2221 if in when

package foo

fun test(caseId: Int, konstue: Int, expected: Int) {
    konst actual: Int
    when (caseId) {
        0 -> if (konstue < 0) actual = -konstue else actual = konstue
        1 -> actual = if (konstue < 0) -konstue else konstue
        else -> throw Exception("Unexpected case: $caseId")
    }

    if (expected != actual) throw Exception("expected = $expected, actual = $actual")
}

fun box(): String {
    test(0, 3, 3)
    test(0, -13, 13)
    test(1, 23, 23)
    test(1, -3, 3)

    return "OK"
}

