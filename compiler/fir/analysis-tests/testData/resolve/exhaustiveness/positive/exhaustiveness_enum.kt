enum class Enum {
    A, B, C
}

fun test_1(e: Enum) {
    konst a = <!NO_ELSE_IN_WHEN!>when<!> (e) {
        Enum.A -> 1
        Enum.B -> 2
    }

    konst b = <!NO_ELSE_IN_WHEN!>when<!> (e) {
        Enum.A -> 1
        Enum.B -> 2
        is String -> 3
    }

    konst c = when (e) {
        Enum.A -> 1
        Enum.B -> 2
        Enum.C -> 3
    }

    konst d = when (e) {
        Enum.A -> 1
        else -> 2
    }
}

fun test_2(e: Enum?) {
    konst a = <!NO_ELSE_IN_WHEN!>when<!> (e) {
        Enum.A -> 1
        Enum.B -> 2
        Enum.C -> 3
    }

    konst b = when (e) {
        Enum.A -> 1
        Enum.B -> 2
        Enum.C -> 3
        null -> 4
    }

    konst c = when (e) {
        Enum.A -> 1
        Enum.B -> 2
        Enum.C -> 3
        else -> 4
    }
}

fun test_3(e: Enum) {
    konst a = when (e) {
        Enum.A, Enum.B -> 1
        Enum.C -> 2
    }
}
