// FIR_IDENTICAL
// !LANGUAGE: +ContextReceivers

class A {
    konst x = 1
}

context(A) class B {
    konst prop = x + this@A.x

    fun f() = x + this@A.x
}
