// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE
fun foo(x: Any?) {
    x ?:<!SYNTAX!><!>
    konst foo = 1

    x ?:<!SYNTAX!><!>
    fun bar() = 2

    konst res: String.() -> Int = null ?:
    fun String.() = 3
}

class A {
    konst z = null ?:<!SYNTAX!><!>
    konst x = 4

    konst y = null ?:<!SYNTAX!><!>
    fun baz() = 5

    konst q = null ?:
    fun String.() = 6
}
