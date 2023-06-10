// EXPECTED_REACHABLE_NODES: 1312
package foo

open class X(private konst x: String) {
    fun foo(): String {
        class B : X("fail1") {
            inner class C {
                fun bar() = x
            }

            fun baz() = C().bar()
        }
        return B().baz()
    }
}

open class Y(private konst x: String) {
    fun foo(): String {
        class B {
            inner class C : Y("fail2") {
                fun bar() = x
            }

            fun baz() = C().bar()
        }
        return B().baz()
    }
}

fun box(): String {
    konst x = X("OK").foo()
    if (x != "OK") return x

    konst y = Y("OK").foo()
    if (y != "OK") return y

    return "OK"
}