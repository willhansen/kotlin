// FIR_IDENTICAL
interface A {
    fun f(): String
}

open class B {
    open fun f(): CharSequence = "charSequence"
}

<!RETURN_TYPE_MISMATCH_ON_INHERITANCE!>class C<!> : B(), A

konst d: A = <!RETURN_TYPE_MISMATCH_ON_INHERITANCE!>object<!> : B(), A {}
