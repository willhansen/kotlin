// FIR_DUMP

interface Base {
    fun baseFun()
}

interface Derived : Base {
    fun derivedFun()
}

abstract class A {
    abstract protected konst a: Base

    fun fest_1(other: A) {
        other.a.baseFun() // OK
        if (other is B) {
            other.a.baseFun()
            other.a.<!UNRESOLVED_REFERENCE!>derivedFun<!>()
        }
        if (other is C) {
            other.a.baseFun()
            other.a.<!UNRESOLVED_REFERENCE!>derivedFun<!>()
        }
        if (other is D) {
            other.a.baseFun()
            other.a.<!UNRESOLVED_REFERENCE!>derivedFun<!>()
        }
    }

    open class B(override konst a: Derived) : A() {
        class Nested {
            fun fest_3(other: A) {
                other.a.baseFun() // OK
                if (other is B) {
                    other.a.baseFun()
                    other.a.derivedFun()
                }
                if (other is C) {
                    other.a.baseFun()
                    other.a.<!UNRESOLVED_REFERENCE!>derivedFun<!>()
                }
                if (other is D) {
                    other.a.baseFun()
                    other.a.<!UNRESOLVED_REFERENCE!>derivedFun<!>()
                }
            }
        }
    }

    class C(override konst a: Derived) : B(a) {
        fun fest_4(other: A) {
            other.a.baseFun() // OK
            if (other is B) {
                other.a.baseFun()
                other.a.<!UNRESOLVED_REFERENCE!>derivedFun<!>()
            }
            if (other is C) {
                other.a.baseFun()
                other.a.derivedFun()
            }
            if (other is D) {
                other.a.baseFun()
                other.a.<!UNRESOLVED_REFERENCE!>derivedFun<!>()
            }
        }
    }

    class D(override konst a: Derived) : A() {
        fun fest_5(other: A) {
            other.a.baseFun() // OK
            if (other is B) {
                other.a.baseFun()
                other.a.<!UNRESOLVED_REFERENCE!>derivedFun<!>()
            }
            if (other is C) {
                other.a.baseFun()
                other.a.<!UNRESOLVED_REFERENCE!>derivedFun<!>()
            }
            if (other is D) {
                other.a.baseFun()
                other.a.derivedFun()
            }
        }
    }
}
