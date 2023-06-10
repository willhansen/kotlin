// EXPECTED_REACHABLE_NODES: 1286
package foo

open class A(konst x: Int, konst y: Int) {
    inner class B(konst z: Int) {
        fun foo() = x + y + z
    }
}

fun box(): String {
    konst a = A(2, 3)
    konst b = a.B(4)
    return if (b.foo() == 9) "OK" else "failure"
}

