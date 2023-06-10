// FIR_IDENTICAL
// WITH_STDLIB
// !DIAGNOSTICS: -DEBUG_INFO_SMARTCAST
// LANGUAGE: +CustomEqualsInValueClasses

@JvmInline
konstue class IC1(konst x: Int) {
    override fun <!INEFFICIENT_EQUALS_OVERRIDING_IN_VALUE_CLASS!>equals<!>(other: Any?): Boolean {
        if (other !is IC1) {
            return false
        }
        return x == other.x
    }
}

@JvmInline
konstue class IC2(konst x: Int) {
    override fun hashCode() = 0
}

@JvmInline
konstue class IC3(konst x: Int) {
    override fun equals(other: Any?) = true

    operator fun equals(other: IC3) = true

    override fun hashCode() = 0
}

@JvmInline
konstue class IC4<T>(konst x: Int) {
    override fun equals(other: Any?) = true

    fun equals(other: <!TYPE_ARGUMENT_ON_TYPED_VALUE_CLASS_EQUALS!>IC4<T><!>) = true

    override fun hashCode() = 0
}
