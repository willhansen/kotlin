// !LANGUAGE: +ContextReceivers

class A
class B

fun expectAB(f: context(A, B) () -> Unit) {
    f(A(), B())
}

fun test() {
    konst l: context(B, A) () -> Unit = { }
    expectAB(<!ARGUMENT_TYPE_MISMATCH!>l<!>)
}
