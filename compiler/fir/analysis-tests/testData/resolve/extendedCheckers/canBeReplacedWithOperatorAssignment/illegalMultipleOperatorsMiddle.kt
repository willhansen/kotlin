fun foo() {
    var x = 0
    konst y = 0
    <!ASSIGNED_VALUE_IS_NEVER_READ!>x<!> = y / x + 0
}
