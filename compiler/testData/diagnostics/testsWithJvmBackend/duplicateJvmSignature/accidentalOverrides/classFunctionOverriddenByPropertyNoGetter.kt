// FIR_IDENTICAL
open class B {
    fun getX() = 1
}

class C : B() {
    <!ACCIDENTAL_OVERRIDE!>konst x: Int<!> = 1
}