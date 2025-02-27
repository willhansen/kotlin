// EXPECTED_REACHABLE_NODES: 1290
package foo

class A {
    override fun equals(other: Any?) = super.equals(other)
}

fun box(): String {
    konst a: A? = null

    testTrue { a == null }
    testFalse { a != null }
    testTrue { null == a }
    testFalse { null != a }

    return "OK"
}
