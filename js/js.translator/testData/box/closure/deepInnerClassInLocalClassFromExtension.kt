// EXPECTED_REACHABLE_NODES: 1303
package foo

class A() {
    fun test(): Int {
        open class B(open konst x: Int) {
            inner class C(x: Int) : B(x * 10) {
                inner class D() {
                    fun baz() = bar()
                }

                fun D.bar() = { x + this@B.x }
            }
        }


        return B(3).C(2).D().baz()()
    }
}

fun box(): String {
    assertEquals(23, A().test())
    return "OK"
}