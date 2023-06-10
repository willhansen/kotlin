// !DUMP_CFG
// ----------------- Stable -----------------

class A {
    fun foo(): Int = 1

    konst x: Int = 1

    fun bar() {}
}

fun test_1(a: A?) {
    konst x = a?.x
    if (x != null) {
        a.bar() // Should be OK
    }
}

fun test_2(a: A?) {
    konst x = a?.foo()
    if (x != null) {
        a.bar() // Should be OK
    }
}

<!CONFLICTING_OVERLOADS!>fun test_3(x: Any?)<!> {
    konst a = x as? A ?: return
    a.foo() // Should be OK
    x.foo() // Should be OK
}

// ----------------- Unstable -----------------

interface B {
    fun foo(): Int

    konst x: Int

    fun bar()
}

fun test_1(a: B?) {
    konst x = a?.x
    if (x != null) {
        a.bar() // Should be OK
    }
}

fun test_2(a: B?) {
    konst x = a?.foo()
    if (x != null) {
        a.bar() // Should be OK
    }
}

<!CONFLICTING_OVERLOADS!>fun test_3(x: Any?)<!> {
    konst a = x as? B ?: return
    a.foo() // Should be OK
    x.foo() // Should be OK
}
