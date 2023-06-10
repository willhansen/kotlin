// FIR_IDENTICAL
interface A { fun f() }

open class P(konst z: B)

class B : A {
    override fun f() {}
    class C : A by <!NO_THIS!>this<!> {}
    class D(konst x : B = <!NO_THIS!>this<!>)
    class E : P(<!NO_THIS!>this<!>)
}