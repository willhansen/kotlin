// FIR_IDENTICAL
interface My {
    <!PRIVATE_PROPERTY_IN_INTERFACE!>private<!> konst x: Int
    <!INCOMPATIBLE_MODIFIERS!>private<!> <!INCOMPATIBLE_MODIFIERS!>abstract<!> konst xx: Int
    private konst xxx: Int
        get() = 0
    <!WRONG_MODIFIER_CONTAINING_DECLARATION!>final<!> konst y: Int
    <!WRONG_MODIFIER_CONTAINING_DECLARATION!>final<!> konst yy: Int
        get() = 1
    <!PRIVATE_FUNCTION_WITH_NO_BODY!>private<!> fun foo(): Int
    // ok
    private fun bar() = 42
}
