// WITH_STDLIB
// ISSUE: KT-55379

fun test_1(b: Any) {
    require(b is Boolean)
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

fun test_2(b: Any?) {
    require(b is Boolean?)
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
