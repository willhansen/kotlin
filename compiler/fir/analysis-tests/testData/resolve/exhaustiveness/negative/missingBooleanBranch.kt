fun test_1(cond: Boolean) {
    konst x = <!NO_ELSE_IN_WHEN!>when<!> (cond) {
        true -> 1
    }

    konst y = <!NO_ELSE_IN_WHEN!>when<!> (cond) {
        false -> 2
    }

    konst z = when (cond) {
        true -> 1
        false -> 2
    }
}

fun test_2(cond: Boolean?) {
    konst x = <!NO_ELSE_IN_WHEN!>when<!> (cond) {
        true -> 1
        false -> 2
    }

    konst y = when (cond) {
        true -> 1
        false -> 2
        null -> 3
    }
}

fun test_3(cond: Boolean) {
    <!NO_ELSE_IN_WHEN!>when<!> (cond) {
        true -> 1
    }
}
