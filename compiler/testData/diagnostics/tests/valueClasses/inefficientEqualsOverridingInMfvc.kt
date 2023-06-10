// FIR_IDENTICAL
// WITH_STDLIB
// !DIAGNOSTICS: -DEBUG_INFO_SMARTCAST
// LANGUAGE: +CustomEqualsInValueClasses +ValueClasses

@JvmInline
konstue class MFVC1(konst x: Int, konst y: Int) {
    override fun <!INEFFICIENT_EQUALS_OVERRIDING_IN_VALUE_CLASS!>equals<!>(other: Any?): Boolean {
        if (other !is MFVC1) {
            return false
        }
        return x == other.x
    }
}

@JvmInline
konstue class MFVC2(konst x: Int, konst y: Int) {
    override fun hashCode() = 0
}

@JvmInline
konstue class MFVC3(konst x: Int, konst y: Int) {
    override fun equals(other: Any?) = true

    fun equals(other: MFVC3) = true

    override fun hashCode() = 0
}
