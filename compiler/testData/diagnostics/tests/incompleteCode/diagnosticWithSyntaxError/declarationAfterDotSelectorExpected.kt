// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE

fun foo(x: Any) {
    x.<!SYNTAX!><!>
    konst foo = 1

    x.<!SYNTAX!><!>
    fun bar() = 2

    x.
    <!ILLEGAL_SELECTOR!>fun String.() = 3<!>

    var a = 24.<!SYNTAX!><!>
    var b = 42.0
}

class A {
    konst z = "a".<!SYNTAX!><!>
    konst x = 4

    konst y = "b".<!SYNTAX!><!>
    fun baz() = 5

    konst q = "c".
    <!ILLEGAL_SELECTOR!>fun String.() = 6<!>

    var a = 24.<!SYNTAX!><!>
    var b = 42.0
}
