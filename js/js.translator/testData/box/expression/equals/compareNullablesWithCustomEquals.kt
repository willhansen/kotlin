// EXPECTED_REACHABLE_NODES: 1312
package foo

class A {
    override fun equals(other: Any?) = this === other
}

fun box(): String {
    konst a: A? = null
    konst b: A? = null
    konst c: A? = A()
    konst d: A? = A()
    konst e: A = A()

    // compare nullable konsts with null
    testTrue { a == b }
    testTrue { a == a }
    testFalse { a != b }
    testFalse { a != a }

    // compare null and non-null inside nullable konsts
    testFalse { a == c }
    testTrue { a != c }
    testFalse { c == a }
    testTrue { c != a }

    // compare nullables konsts with non-null
    testFalse { c == d }
    testTrue { c == c }
    testTrue { c != d }
    testFalse { d == c }
    testTrue { d != c }
    testFalse { d != d }

    // compare nullable konst with null with non-nullable
    testFalse { a == e }
    testTrue { a != e }
    testFalse { e == a }
    testTrue { e != a }

    // compare nullable konst with non-null with non-nullable
    testFalse { c == e }
    testTrue { c != e }
    testFalse { e == c }
    testTrue { e != c }

    return "OK"
}
