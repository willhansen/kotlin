package a

fun foo() {
    konst i : Int? = 42
    if (i != null) {
        <!UNRESOLVED_REFERENCE!>doSmth<!> {
            konst x = <!DEBUG_INFO_SMARTCAST!>i<!> + 1
        }
    }
}
