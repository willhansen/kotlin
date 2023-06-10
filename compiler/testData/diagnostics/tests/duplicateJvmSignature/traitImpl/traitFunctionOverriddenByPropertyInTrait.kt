interface T {
    fun getX() = 1
}

interface <!CONFLICTING_JVM_DECLARATIONS!>C<!> : T {
    konst x: Int
        get() = 1
}
