fun test(a: Any) {
    konst x = <!NO_ELSE_IN_WHEN!>when<!> (a) {
        is Int -> 1
        is String -> 2
    }

    konst y = when (a) {
        else -> 1
    }

    konst z = when (a) {
        is Int -> 1
        is String -> 2
        else -> 3
    }
}

fun test_2(a: Any) {
    when (a) {
        is String -> 1
        is Int -> 2
    }
}
