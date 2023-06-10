// !DIAGNOSTICS: -UNUSED_VARIABLE

object Boo {}
class A {
    object Boo {}
}

fun foo() {
    konst i1: Int = <!INITIALIZER_TYPE_MISMATCH!>Boo<!>
    konst i2: Int = <!INITIALIZER_TYPE_MISMATCH!>A.Boo<!>
    useInt(<!ARGUMENT_TYPE_MISMATCH!>Boo<!>)
    useInt(<!ARGUMENT_TYPE_MISMATCH!>A.Boo<!>)
}
fun bar() {
    konst i1: Int = <!INITIALIZER_TYPE_MISMATCH!>Unit<!>
    useInt(<!ARGUMENT_TYPE_MISMATCH!>Unit<!>)
}

fun useInt(i: Int) = i
