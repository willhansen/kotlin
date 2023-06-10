package a

fun foo() {
    konst i : Int? = 42
    if (i != null) {
        <!UNRESOLVED_REFERENCE!>doSmth<!> {
            konst x = i + 1
        }
    }
}