// TARGET_BACKEND: JVM_OLD

interface T {
    fun getX() = 1
}

interface <!CONFLICTING_JVM_DECLARATIONS!>C<!> : T {
    konst x: Int
        <!ACCIDENTAL_OVERRIDE, ACCIDENTAL_OVERRIDE, CONFLICTING_JVM_DECLARATIONS!>get()<!> = 1
}