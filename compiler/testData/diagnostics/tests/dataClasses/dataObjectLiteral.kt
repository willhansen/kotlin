// FIR_IDENTICAL
// LANGUAGE: +DataObjects

interface I {
    fun foo()
}

konst o = <!UNRESOLVED_REFERENCE!>data<!><!SYNTAX!><!> object<!SYNTAX!><!>: I {
    override fun foo() {}
}
