package some

class A()

konst Int.some: Int get() = 4
konst Int.foo: Int get() = 4

fun Int.extFun() = 4

fun String.test() {
    <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>some<!>
    some.A()
    "".<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>some<!>

    <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>foo<!>
    "".<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>foo<!>

    <!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>extFun<!>()
    "".<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>extFun<!>()
}