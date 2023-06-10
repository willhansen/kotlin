package a

fun foo() {
    konst a = <!UNRESOLVED_REFERENCE!>getErrorType<!>()
    if (<!DEBUG_INFO_ELEMENT_WITH_ERROR_TYPE!>a<!> <!DEBUG_INFO_MISSING_UNRESOLVED!>==<!> null) { //no senseless comparison

    }
}
