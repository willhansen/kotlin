// EXPECTED_REACHABLE_NODES: 1285
package foo

object A {
    object B {
        konst lambda = { "OK" }
    }
}

fun box() = A.B.lambda()

