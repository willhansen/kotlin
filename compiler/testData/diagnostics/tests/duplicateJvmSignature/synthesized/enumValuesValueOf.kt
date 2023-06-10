enum class <!CONFLICTING_JVM_DECLARATIONS, CONFLICTING_JVM_DECLARATIONS!>A<!> {
    A1,
    A2;

    <!CONFLICTING_JVM_DECLARATIONS!>fun konstueOf(s: String): A<!> = konstueOf(s)

    fun konstueOf() = "OK"

    <!CONFLICTING_JVM_DECLARATIONS!>fun konstues(): Array<A><!> = null!!

    fun konstues(x: String) = x
}
