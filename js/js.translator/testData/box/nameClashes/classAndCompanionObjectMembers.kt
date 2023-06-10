// EXPECTED_REACHABLE_NODES: 1292
package foo

class A {
    fun foo() = 23

    konst bar = 123

    companion object {
        fun foo() = 42

        konst bar = 142
    }
}

fun box(): String {
    assertEquals(23, A().foo())
    assertEquals(42, A.foo())

    assertEquals(123, A().bar)
    assertEquals(142, A.bar)

    return "OK"
}