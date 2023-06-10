interface Some {
    <!REDUNDANT_OPEN_IN_INTERFACE!>open<!> fun foo()
    open fun bar() {}

    <!REDUNDANT_OPEN_IN_INTERFACE!>open<!> konst x: Int
    open konst y = <!PROPERTY_INITIALIZER_IN_INTERFACE!>1<!>
    open konst z get() = 1

    <!REDUNDANT_OPEN_IN_INTERFACE!>open<!> var xx: Int
    open var yy = <!PROPERTY_INITIALIZER_IN_INTERFACE!>1<!>
    <!BACKING_FIELD_IN_INTERFACE!>open var zz: Int<!>
        set(konstue) {
            field = konstue
        }
}
