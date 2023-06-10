sealed class Base {
    class A : Base() {
        class B : Base()
    }
}

class C : Base()

fun test_1(e: Base) {
    konst a = <!NO_ELSE_IN_WHEN!>when<!> (e) {
        is Base.A -> 1
        is Base.A.B -> 2
    }

    konst b = <!NO_ELSE_IN_WHEN!>when<!> (e) {
        is Base.A -> 1
        is Base.A.B -> 2
        is String -> 3
    }

    konst c = when (e) {
        is Base.A -> 1
        is Base.A.B -> 2
        is C -> 3
    }

    konst d = when (e) {
        is Base.A -> 1
        else -> 2
    }
}

fun test_2(e: Base?) {
    konst a = <!NO_ELSE_IN_WHEN!>when<!> (e) {
        is Base.A -> 1
        is Base.A.B -> 2
        is C -> 3
    }

    konst b = when (e) {
        is Base.A -> 1
        is Base.A.B -> 2
        is C -> 3
        null -> 4
    }

    konst c = when (e) {
        is Base.A -> 1
        is Base.A.B -> 2
        is C -> 3
        else -> 4
    }
}

fun test_3(e: Base) {
    konst a = when (e) {
        is Base.A, is Base.A.B -> 1
        is C -> 2
    }
}
