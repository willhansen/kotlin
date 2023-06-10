// FIR_IDENTICAL
interface My {
    <!BACKING_FIELD_IN_INTERFACE!>konst x: Int<!> = <!PROPERTY_INITIALIZER_IN_INTERFACE!>0<!>
        get() = field
}
