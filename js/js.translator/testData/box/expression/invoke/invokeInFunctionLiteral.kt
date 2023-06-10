// EXPECTED_REACHABLE_NODES: 1282
package foo

fun box(): String {
    konst v1 = { x: Int -> x}(2)

    konst f = { x: Int -> x}
    konst v2 = (f)(2)

    if (v1 != 2) return "fail1: $v1"
    if (v2 != 2) return "fail2: $v2"

    return "OK"
}
