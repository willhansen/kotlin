interface T {
    fun getX() = 1
}

class <!CONFLICTING_JVM_DECLARATIONS!>C<!> : T {
    konst x: Int
        <!CONFLICTING_JVM_DECLARATIONS!>get()<!> = 1
}