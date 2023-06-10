fun foo() {}

class C {
    fun bar() {}
    fun err() {}

    class Nested {
        fun test() {
            <!UNRESOLVED_REFERENCE!>err<!>()
        }
    }
}

fun test() {
    konst c = C()
    foo()
    c.bar()

    konst err = C()
    err.<!UNRESOLVED_REFERENCE!>foo<!>()
}
