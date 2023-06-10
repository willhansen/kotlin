fun test_1(b: Boolean) {
    konst x = <!NO_ELSE_IN_WHEN!>when<!> (b) {
        true -> 1
    }
    konst y = when (b) {
        true -> 1
        false -> 2
    }
    konst z = when (b) {
        true -> 1
        else -> 2
    }
}

fun test_2(b: Boolean?) {
    konst x = <!NO_ELSE_IN_WHEN!>when<!> (b) {
        true -> 1
        false -> 2
    }
    konst y = when (b) {
        true -> 1
        false -> 2
        null -> 3
    }
}
