// See KT-12809
open class A(konst a: Any) {
    override fun toString() = a.toString()
}

object B : A(B.<!UNINITIALIZED_VARIABLE!>foo<!>) { // call B.foo should be not-allowed
    konst foo = 4
}