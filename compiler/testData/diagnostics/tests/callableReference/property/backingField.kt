// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_EXPRESSION

konst i: Int = 10
    get() {
        ::<!UNSUPPORTED!>field<!>
        return field
    }
