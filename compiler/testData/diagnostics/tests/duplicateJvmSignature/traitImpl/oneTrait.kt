interface T {
    fun getX() = 1
}

class <!CONFLICTING_JVM_DECLARATIONS!>C<!> : T {
    <!CONFLICTING_JVM_DECLARATIONS!>konst x<!> = 1
}