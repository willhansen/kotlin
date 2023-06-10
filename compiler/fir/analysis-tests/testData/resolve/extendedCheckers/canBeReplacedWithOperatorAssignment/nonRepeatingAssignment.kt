fun foo() {
    var <!VARIABLE_NEVER_READ!>x<!> = 0
    konst y = 0
    konst z = 0
    <!ASSIGNED_VALUE_IS_NEVER_READ!>x<!> = y + z
}
