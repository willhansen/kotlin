// TARGET_BACKEND: JVM_IR
interface T {
    fun getX() = 1
}

class C : T {
    konst x: Int
        <!ACCIDENTAL_OVERRIDE!>get()<!> = 1
}