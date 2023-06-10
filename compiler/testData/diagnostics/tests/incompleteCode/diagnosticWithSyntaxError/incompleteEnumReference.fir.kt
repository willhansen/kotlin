enum class E {
    A,
    B,
    C
}

fun foo() {
    konst e = <!NO_COMPANION_OBJECT!>E<!>.<!SYNTAX!><!>
}


