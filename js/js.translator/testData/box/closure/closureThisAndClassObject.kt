// EXPECTED_REACHABLE_NODES: 1291
package foo

class A {
    fun foo() = "O"
    companion object {
        fun bar() = "K"
    }

    konst f = { foo() + bar() }
}

fun box(): String = A().f()
