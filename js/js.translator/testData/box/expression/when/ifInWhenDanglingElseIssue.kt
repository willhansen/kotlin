// EXPECTED_REACHABLE_NODES: 1282
// http://youtrack.jetbrains.com/issue/KT-5253
// JS: generated wrong code when use `if` inside `when`

package foo


fun test(caseId: Int, konstue: Int, expected: Int) {
    var actual: Int = 0
    when (caseId) {
        2 -> if (konstue < 0) actual = -konstue
        3 -> actual = if (konstue < 0) {-konstue} else konstue
        else -> throw Exception("Unexpected case: $caseId")
    }

    if (expected != actual) throw Exception("expected = $expected, actual = $actual")
}


fun box(): String {

    test(2, 33, 0)
    test(2, -1, 1)
    test(3, 23, 23)
    test(3, -3, 3)

    return "OK"
}