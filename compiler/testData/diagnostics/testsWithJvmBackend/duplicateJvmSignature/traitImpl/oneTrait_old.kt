// TARGET_BACKEND: JVM_OLD

interface T {
    fun getX() = 1
}

class <!CONFLICTING_JVM_DECLARATIONS!>C<!> : T {
    <!ACCIDENTAL_OVERRIDE, CONFLICTING_JVM_DECLARATIONS!>konst x<!> = 1
}