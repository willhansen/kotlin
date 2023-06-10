// TARGET_BACKEND: JVM_IR
interface B {
    fun getX() = 1
}

interface D {
    <!ACCIDENTAL_OVERRIDE!>konst x: Int<!>
}

class C(d: D) : D by d, B
