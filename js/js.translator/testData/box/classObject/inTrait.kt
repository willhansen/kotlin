// EXPECTED_REACHABLE_NODES: 1284
package foo

interface A {
    companion object {
        konst OK: String = "OK"
    }
}

fun box(): String {
    return A.OK
}