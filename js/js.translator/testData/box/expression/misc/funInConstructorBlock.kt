// EXPECTED_REACHABLE_NODES: 1286
package foo

class A() {
    fun lold() = "OK"
    konst p: () -> String
    init {
        p = { { lold() }() }
    }
}


fun box(): String {
    return A().p()
}
