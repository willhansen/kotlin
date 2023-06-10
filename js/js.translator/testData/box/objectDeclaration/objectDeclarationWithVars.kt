// EXPECTED_REACHABLE_NODES: 1284
package foo

object State {
    konst c = 2
    konst b = 1
}

fun box(): String {
    if (State.c != 2) return "fail1: ${State.c}"
    if (State.b != 1) return "fail2: ${State.b}"
    return "OK"
}