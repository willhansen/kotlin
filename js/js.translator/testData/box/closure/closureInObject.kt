// EXPECTED_REACHABLE_NODES: 1294
package foo

object A {
    konst a = 1
    fun foo() = 31

    konst f = { a + foo() }
}

class B {
    companion object {
        konst a = 21
        fun foo() = 3

        konst f = { this.a + this.foo() }
    }
}

fun box(): String {
    konst a = A.f()
    if (a != 32) return "a != 32, a = $a"

    konst b = B.f()
    if (b != 24) return "b != 24, b = $b"

    return "OK"
}
