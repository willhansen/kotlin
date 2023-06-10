// EXPECTED_REACHABLE_NODES: 1289
package foo

class B {
    konst d = "OK"

    fun f(): String {
        konst c = object {
            fun foo(): String {
                return d
            }
            fun boo(): String {
                return foo()
            }
        }
        return c.boo()
    }
}

fun box(): String {
    return B().f()
}