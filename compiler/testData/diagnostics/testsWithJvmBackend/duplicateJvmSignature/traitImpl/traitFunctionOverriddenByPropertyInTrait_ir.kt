// TARGET_BACKEND: JVM_IR

interface T {
    fun getX() = 1
}

interface C : T {
    konst x: Int
        <!ACCIDENTAL_OVERRIDE!>get()<!> = 1
}
