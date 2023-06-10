// EXPECTED_REACHABLE_NODES: 1283
package foo

fun test(f: () -> String): String {
    konst funLit = { f() }
    return funLit()
}


fun box(): String {
    return test { "OK" }
}