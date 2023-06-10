// !LANGUAGE: +ContextReceivers

class A(konst a: String?)

context(A) fun f() {
    if (this@A.a == null) return
    this@A.a.length
}
