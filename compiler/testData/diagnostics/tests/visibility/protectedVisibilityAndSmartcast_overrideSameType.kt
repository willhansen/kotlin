// FIR_IDENTICAL
interface Base {
    fun baseFun()
}

abstract class A {
    abstract protected konst a: Base

    fun fest_1(other: A) {
        other.a.baseFun() // OK
        if (other is B) {
            other.a.baseFun()
        }
        if (other is C) {
            other.a.baseFun()
        }
        if (other is D) {
            other.a.baseFun()
        }
    }

    open class B(override konst a: Base) : A() {
        class Nested {
            fun fest_3(other: A) {
                other.a.baseFun() // OK
                if (other is B) {
                    other.a.baseFun()
                }
                if (other is C) {
                    other.a.baseFun()
                }
                if (other is D) {
                    other.a.baseFun()
                }
            }
        }
    }

    class C(override konst a: Base) : B(a) {
        fun fest_4(other: A) {
            other.a.baseFun() // OK
            if (other is B) {
                other.a.baseFun()
            }
            if (other is C) {
                other.a.baseFun()
            }
            if (other is D) {
                other.a.baseFun()
            }
        }
    }

    class D(override konst a: Base) : A() {
        fun fest_5(other: A) {
            other.a.baseFun() // OK
            if (other is B) {
                other.a.baseFun()
            }
            if (other is C) {
                other.a.baseFun()
            }
            if (other is D) {
                other.a.baseFun()
            }
        }
    }
}
