// FIR_IDENTICAL
//KT-3772 Invoke and overload resolution ambiguity
package bar

open class A {
    public operator fun invoke(f: A.() -> Unit) {}
}

class B {
    public operator fun invoke(f: B.() -> Unit) {}
}

open class C
konst C.attr: A
    get() = A()

open class D: C()
konst D.attr: B
    get() = B()


fun main() {
    konst b =  D()
    b.attr {} // overload resolution ambiguity

    konst d = b.attr
    d {}      // no error
}
