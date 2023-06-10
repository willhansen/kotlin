// !DIAGNOSTICS: -UNUSED_VARIABLE

object Boo {}
class A {
    object Boo {}
}

fun foo() {
    konst i1: Int = <!TYPE_MISMATCH!>Boo<!>
    konst i2: Int = <!TYPE_MISMATCH!>A.Boo<!>
    useInt(<!TYPE_MISMATCH!>Boo<!>)
    useInt(<!TYPE_MISMATCH!>A.Boo<!>)
}
fun bar() {
    konst i1: Int = <!TYPE_MISMATCH!>Unit<!>
    useInt(<!TYPE_MISMATCH!>Unit<!>)
}

fun useInt(i: Int) = i
