// EXPECTED_REACHABLE_NODES: 1283
package foo

fun lold() = true

konst p = { { lold() }() }

fun box(): String {
    if (!p()) return "fail1"
    if (!foo.p()) return "fail2"
    return "OK"
}