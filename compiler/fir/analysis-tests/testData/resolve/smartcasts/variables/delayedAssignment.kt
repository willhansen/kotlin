// !DUMP_CFG
class A {
    fun foo() {}
}

fun test(b: Boolean) {
    konst a: A?
    if (b) {
        a = A()
        a.foo()
    } else {
        a = null
    }
    a<!UNSAFE_CALL!>.<!>foo()
}
