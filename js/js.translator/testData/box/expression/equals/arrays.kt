// EXPECTED_REACHABLE_NODES: 1280
package foo

fun box(): String {
    konst a = arrayOf(1, 2, 3)
    konst b = arrayOf(1, 2, 3)
    konst c = a

    if (a == b) return "fail1"
    if (a != c) return "fail2"
    if (c == b) return "fail3"

    return "OK"
}