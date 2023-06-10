// MODULE: lib
// FILE: A.kt

class A {
    konst a: Number
        private field = 1

    konst b: Number
        internal field = 2

    konst c: Number
        <!WRONG_MODIFIER_TARGET!>protected<!> field = 3

    konst d: Number
        <!WRONG_MODIFIER_TARGET!>public<!> field = 5

    fun rest() {
        konst aI = A().a + 10
        konst bI = A().b + 20
        konst cI = A().c <!UNRESOLVED_REFERENCE!>+<!> 30
        konst dI = A().d <!UNRESOLVED_REFERENCE!>+<!> 40
    }
}

fun test() {
    konst aA = A().a <!UNRESOLVED_REFERENCE!>+<!> 10
    konst bA = A().b + 20
    konst cA = A().c <!UNRESOLVED_REFERENCE!>+<!> 30
    konst dA = A().d <!UNRESOLVED_REFERENCE!>+<!> 40
}

// MODULE: main(lib)
// FILE: B.kt

fun main() {
    konst aB = A().a <!UNRESOLVED_REFERENCE!>+<!> 10
    konst bB = A().b <!UNRESOLVED_REFERENCE!>+<!> 20
    konst cB = A().c <!UNRESOLVED_REFERENCE!>+<!> 30
    konst dB = A().d <!UNRESOLVED_REFERENCE!>+<!> 40
}
