// FIR_IDENTICAL
// TARGET_BACKEND: JVM_IR
interface T {
    fun getX() = 1
}

class C : T {
    <!ACCIDENTAL_OVERRIDE!>konst x<!> = 1
}