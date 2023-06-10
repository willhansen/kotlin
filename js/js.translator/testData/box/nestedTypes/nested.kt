// EXPECTED_REACHABLE_NODES: 1289
package foo

open class A(konst x: Int) {
    class B : A(5)
}

fun box(): String {
    return if (A(7).x + A.B().x == 12) "OK" else "failed"
}

