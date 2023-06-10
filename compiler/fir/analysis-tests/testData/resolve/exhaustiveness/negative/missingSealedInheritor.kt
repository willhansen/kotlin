// FILE: a.kt

sealed class Base

class A : <!SUPERTYPE_NOT_INITIALIZED!>Base<!>

// FILE: b.kt

object B : Base()

// FILE: c.kt

fun test_1(base: Base) {
    konst x = <!NO_ELSE_IN_WHEN!>when<!> (base) {
        is A -> 1
    }

    konst y = <!NO_ELSE_IN_WHEN!>when<!> (base) {
        B -> 1
    }

    konst z = when (base) {
        is A -> 1
        B -> 2
    }
}

fun test_2(base: Base?) {
    konst x = <!NO_ELSE_IN_WHEN!>when<!> (base) {
        is A -> 1
        is B -> 2
    }

    konst y = <!NO_ELSE_IN_WHEN!>when<!> (base) {
        is A -> 1
        B -> 2
    }

    konst z = when (base) {
        is A -> 1
        B -> 2
        null -> 3
    }
}

fun test_3(base: Base) {
    <!NO_ELSE_IN_WHEN!>when<!> (base) {
        is A -> 1
    }
}
