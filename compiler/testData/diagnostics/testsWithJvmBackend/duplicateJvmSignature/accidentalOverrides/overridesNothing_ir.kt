// FIR_IDENTICAL
// TARGET_BACKEND: JVM_IR
interface B {
    fun getX() = 1
}

class C : B {
    <!ACCIDENTAL_OVERRIDE!><!NOTHING_TO_OVERRIDE!>override<!> konst x<!> = 1
}