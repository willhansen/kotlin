// TARGET_BACKEND: JVM_IR
interface B {
    fun getX() = 1
}

interface D {
    konst x: Int
}

class <!ACCIDENTAL_OVERRIDE!>C(d: D)<!> : D by d, B