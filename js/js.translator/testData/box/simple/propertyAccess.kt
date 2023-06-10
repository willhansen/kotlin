// EXPECTED_REACHABLE_NODES: 1282
package foo

class Test() {
    konst p = "OK"
}

fun box(): String {
    return Test().p
}