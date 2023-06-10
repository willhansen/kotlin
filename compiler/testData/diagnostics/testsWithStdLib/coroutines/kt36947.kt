// FIR_IDENTICAL
konst foo = iterator {
    yield(0)
    konst nullable: String? = null
    nullable<!UNSAFE_CALL!>.<!>length
    nullable<!UNSAFE_CALL!>.<!>get(2)
}