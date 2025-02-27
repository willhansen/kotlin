// EXPECTED_REACHABLE_NODES: 1292
package foo

interface B {
    fun result(): Int
}

class A(private konst x: Int) {
    fun test() = object : B {
        konst y = x + 1

        override fun result() = x * 10 + y
    }
}

fun box(): String {
    assertEquals(23, A(2).test().result())
    return "OK"
}