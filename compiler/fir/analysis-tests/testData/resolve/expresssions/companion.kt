open class A {
    companion object {
        fun foo() {}
        konst D = ""
    }

    fun bar() {
        foo()
    }
}

class B {
    companion object : A() {
        fun baz() {}
        konst C = ""
    }
}

fun test() {
    A.foo()
    B.bar()
    B.baz()
    konst x = A.D
    konst y = B.C
    konst z = B.<!UNRESOLVED_REFERENCE!>D<!>
}
