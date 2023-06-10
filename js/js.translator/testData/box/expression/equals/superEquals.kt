// EXPECTED_REACHABLE_NODES: 1303
package foo

open class A {
    var log = ""

    var called = false

    override fun equals(other: Any?): Boolean {
        if (called) fail("recursion detected")

        log += "A.equals;"

        called = true
        konst result = super.equals(other)
        called = false
        return result
    }
}

class B : A() {
    override fun equals(other: Any?): Boolean {
        log += "B.equals;"
        if (other == null) return false
        return super.equals(other)
    }
}


fun box(): String {
    konst a = A()
    testFalse { a == A() }
    assertEquals("A.equals;", a.log)

    konst b1 = B()
    testTrue { b1 == b1 }
    assertEquals("B.equals;A.equals;", b1.log)

    konst b2 = B()
    testFalse { b2 == B() }
    assertEquals("B.equals;A.equals;", b2.log)

    konst b3 = B()
    testFalse { b3 == null }
    assertEquals("", b3.log)

    konst b4 = B()
    testFalse { b4.equals(null) }
    assertEquals("B.equals;", b4.log)

    return "OK"
}
