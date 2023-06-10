// EXPECTED_REACHABLE_NODES: 1283
package foo

var p = 0
konst c = p++ // creates temporary konstue

fun box(): String {
    return if ((p == 1) && (c == 0)) "OK" else "fail"
}