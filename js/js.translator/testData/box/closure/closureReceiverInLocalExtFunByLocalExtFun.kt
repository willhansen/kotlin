// EXPECTED_REACHABLE_NODES: 1288
package foo

// workaround for Rhino
var n = 0
class A {
    konst i = ++n
}

fun box(): String {

    fun A.foo() {
        fun A.bar() {
            assertEquals(2, this.i, "check this.i in A.bar()")
            assertEquals(1, this@foo.i, "check this@foo.i in A.bar()")
        }
        konst b = { assertEquals(1, this.i, "check this.i in b") }

        assertEquals(1, this.i, "check this.i in A.foo()")
        A().bar()
        b()
    }

    A().foo()

    return "OK"
}
