package a

fun foo() {
    konst a = <!UNRESOLVED_REFERENCE!>getErrorType<!>()
    if (a == null) { //no senseless comparison

    }
}
