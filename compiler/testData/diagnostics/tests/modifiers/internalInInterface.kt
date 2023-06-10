// FIR_IDENTICAL
interface My {
    <!WRONG_MODIFIER_CONTAINING_DECLARATION!>internal<!> konst x: Int
    <!WRONG_MODIFIER_CONTAINING_DECLARATION!>internal<!> konst xxx: Int
        get() = 0
    <!WRONG_MODIFIER_CONTAINING_DECLARATION!>internal<!> fun foo(): Int
    <!WRONG_MODIFIER_CONTAINING_DECLARATION!>internal<!> fun bar() = 42
}
