// FIR_IDENTICAL
// WITH_STDLIB
// !DIAGNOSTICS: -DEBUG_INFO_SMARTCAST
// LANGUAGE: +CustomEqualsInValueClasses, +ValueClasses


@JvmInline
konstue class MFVC1(konst x: Int, konst y: Int) {
    override fun equals(other: Any?) = true

    operator fun equals(other: MFVC1) = true

    override fun hashCode() = 0
}

@JvmInline
konstue class MFVC2(konst x: Int, konst y: Int) {
    <!INAPPLICABLE_OPERATOR_MODIFIER!>operator<!> fun equals(other: MFVC1) = true

    <!INAPPLICABLE_OPERATOR_MODIFIER!>operator<!> fun equals(other: MFVC2) {
    }
}
