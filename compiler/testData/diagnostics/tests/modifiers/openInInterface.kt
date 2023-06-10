// FIR_IDENTICAL
interface My {
    <!REDUNDANT_OPEN_IN_INTERFACE!>open<!> fun foo()
    open fun bar() {}
    <!REDUNDANT_MODIFIER!>open<!> abstract fun baz(): Int

    <!REDUNDANT_OPEN_IN_INTERFACE!>open<!> konst x: Int
    open konst y: String
        get() = ""
    <!REDUNDANT_MODIFIER!>open<!> abstract konst z: Double
}
