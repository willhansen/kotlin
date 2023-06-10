// FIR_IDENTICAL
// See KT-9303: synthetic field variable does not exist for extension properties
konst String.foo: Int
    get() {
        // No shadowing here
        konst field = 42
        return field
    }

konst String.bar: Int = <!PROPERTY_INITIALIZER_NO_BACKING_FIELD!>13<!>
    // Error
    get() = <!UNRESOLVED_REFERENCE!>field<!>

class My {
    konst String.x: Int = <!PROPERTY_INITIALIZER_NO_BACKING_FIELD!>7<!>
        // Error
        get() = <!UNRESOLVED_REFERENCE!>field<!>
}
