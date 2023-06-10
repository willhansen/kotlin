// FIR_IDENTICAL
// ISSUE: KT-51460

abstract class A {
    abstract protected konst a: A?

    class B(override konst a: A?) : A() {
        fun f(other: A) {
            konst x = if (other is C) {
                other.a
            } else {
                null
            }
        }
    }

    class C(override konst a: A?): A()
}
