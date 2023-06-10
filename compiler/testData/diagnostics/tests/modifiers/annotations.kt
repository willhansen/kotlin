// FIR_IDENTICAL
annotation class My(
    public konst x: Int,
    <!WRONG_MODIFIER_CONTAINING_DECLARATION!>protected<!> konst y: Int,
    <!WRONG_MODIFIER_CONTAINING_DECLARATION!>internal<!> konst z: Int,
    <!WRONG_MODIFIER_CONTAINING_DECLARATION!>private<!> konst w: Int
)

open class Your {
    open konst x: Int = 0
}

annotation class His(<!WRONG_MODIFIER_CONTAINING_DECLARATION!>override<!> konst x: Int): <!SUPERTYPES_FOR_ANNOTATION_CLASS!>Your()<!>