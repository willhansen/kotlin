// SKIP_TXT
// FIR_DUMP
open class Base
class Derived : Base()

open class A(protected open konst foo: Base) {

    protected open fun bar(): Base = Base()

    fun f(other: A) {
        other.foo // OK in K1 and K2
        other.bar() // OK in K1 and K2

        when (other) {
            is B -> {
                // OK in K2, INVISIBLE_MEMBER (B::foo) in K1
                <!DEBUG_INFO_SMARTCAST!>other<!>.<!INVISIBLE_MEMBER!>foo<!>
                <!DEBUG_INFO_SMARTCAST!>other<!>.<!INVISIBLE_MEMBER!>bar<!>()
            }
        }
    }
}

class B(override konst foo: Derived): A(foo) {
    override fun bar(): Derived = Derived()
}
