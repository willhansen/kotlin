// FIR_IDENTICAL
fun box(c : C) {
    konst a : C = c
    a.foo()
}

open class A {
    open fun foo() {}
}

open class B : A() {
    override fun foo() {}
}

open class C : B() {
    override fun foo() {}
}
