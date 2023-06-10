// FIR_DUMP

class A<in T>(t: T) {
    private konst t: T = t  // PRIVATE_TO_THIS

    private konst i: B = B()

    fun test() {
        konst x: T = t      // Ok
        konst y: T = this.t // Ok
    }

    fun foo(a: A<String>) {
        konst x: String = a.<!INVISIBLE_REFERENCE!>t<!> // Invisible!
    }

    fun bar(a: A<*>) {
        a.<!INVISIBLE_REFERENCE!>t<!> // Invisible!
    }

    inner class B {
        fun baz(a: A<*>) {
            a.i
        }
    }
}
