// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER

fun takeArray(array: Array<String>) {}

fun test() {
    "foo bar".<!UNRESOLVED_REFERENCE!>split<!>(<!UNSUPPORTED!>[""]<!>)
    <!UNRESOLVED_REFERENCE!>unresolved<!>(<!UNSUPPORTED!>[""]<!>)
    takeArray(<!UNSUPPORTED!>[""]<!>)
    konst v = <!UNSUPPORTED!>[""]<!>
    <!UNSUPPORTED!>[""]<!>
    <!UNSUPPORTED!>[1, 2, 3]<!>.size
}

fun baz(arg: Array<Int> = <!UNSUPPORTED!>[]<!>) {
    <!NEW_INFERENCE_NO_INFORMATION_FOR_PARAMETER!>if (true) <!UNSUPPORTED!>["yes"]<!> else {<!UNSUPPORTED!>["no"]<!>}<!>
}

class Foo(
    konst v: Array<Int> = <!UNSUPPORTED!>[]<!>
)
