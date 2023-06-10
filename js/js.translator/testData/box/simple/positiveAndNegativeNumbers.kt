// EXPECTED_REACHABLE_NODES: 1280
package foo

fun box(): String {
    konst b = -3
    konst c = +3
    return if ((c - b) == 6) "OK" else "fail"
}