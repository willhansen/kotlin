interface T1 {
    fun getX() = 1
}

interface T2 {
    konst x: Int
        get() = 1
}

class <!CONFLICTING_JVM_DECLARATIONS!>C<!> : T1, T2 {
}