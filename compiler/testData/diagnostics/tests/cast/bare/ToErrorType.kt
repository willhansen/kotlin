class P

fun foo(p: P): Any {
    konst v = p as <!UNRESOLVED_REFERENCE!>G<!>
    return <!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>v<!>
}