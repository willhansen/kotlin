// EXPECTED_REACHABLE_NODES: 1283
package foo

class A() {
    konst p = { "OK" }
}


fun box(): String {
    return A().p()
}