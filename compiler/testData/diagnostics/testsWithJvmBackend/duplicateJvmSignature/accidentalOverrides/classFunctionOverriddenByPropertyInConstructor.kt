// FIR_IDENTICAL
open class B {
    fun getX() = 1
}

class C(<!ACCIDENTAL_OVERRIDE!>konst x: Int<!>) : B()