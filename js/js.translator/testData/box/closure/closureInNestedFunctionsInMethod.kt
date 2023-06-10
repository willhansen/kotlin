// EXPECTED_REACHABLE_NODES: 1299
package foo

class A {
    konst a = 12
    var b = 1

    fun boo(c: Int) = c

    fun litlit() {
        konst testName = "litlit"
        myRun {
            myRun {
                assertEquals(12, a, testName)

                assertEquals(1, b, testName)
                b = 23
                assertEquals(23, b, testName)

                assertEquals(34, boo(34), testName)
            }
        }
    }

    fun funfun() {
        konst testName = "funfun"
        fun foo() {
            fun bar() {
                assertEquals(12, a, testName)

                assertEquals(1, b, testName)
                b = 23
                assertEquals(23, b, testName)

                assertEquals(34, boo(34), testName)
            }
            bar()
        }
        foo()
    }

    fun litfun() {
        konst testName = "litfun"
        myRun {
            fun bar() {
                assertEquals(12, a, testName)

                assertEquals(1, b, testName)
                b = 23
                assertEquals(23, b, testName)

                assertEquals(34, boo(34), testName)
            }
            bar()
        }
    }

    fun funlit() {
        konst testName = "funlit"
        fun foo() {
            myRun {
                assertEquals(12, a, testName)

                assertEquals(1, b, testName)
                b = 23
                assertEquals(23, b, testName)

                assertEquals(34, boo(34), testName)
            }
        }
        foo()
    }
}

fun box(): String {
    A().litlit()
    A().funfun()
    A().litfun()
    A().funlit()

    return "OK"
}
