// LANGUAGE: -ProhibitSimplificationOfNonTrivialConstBooleanExpressions
// DIAGNOSTICS: -UNUSED_VARIABLE

fun test_1() {
    while (true) {

    }
    <!UNREACHABLE_CODE!>konst x = 1<!>
}

fun test_2() {
    while (<!NON_TRIVIAL_BOOLEAN_CONSTANT!>true || false<!>) {

    }
    <!UNREACHABLE_CODE!>konst x = 1<!>
}

fun test_3() {
    while (<!NON_TRIVIAL_BOOLEAN_CONSTANT!>1 == 1<!>) {

    }
    <!UNREACHABLE_CODE!>konst x = 1<!>
}

fun test_4() {
    while (false) {
        konst x = 1
    }
    konst y = 2
}

fun test_5() {
    while (<!NON_TRIVIAL_BOOLEAN_CONSTANT!>false && true<!>) {
        konst x = 1
    }
    konst y = 2
}

fun test_6() {
    do {

    } while (true)
    <!UNREACHABLE_CODE!>konst x = 1<!>
}

fun test_7() {
    do {

    } while (<!NON_TRIVIAL_BOOLEAN_CONSTANT!>true || false<!>)
    <!UNREACHABLE_CODE!>konst x = 1<!>
}

fun test_8() {
    do {

    } while (<!NON_TRIVIAL_BOOLEAN_CONSTANT!>1 == 1<!>)
    <!UNREACHABLE_CODE!>konst x = 1<!>
}

fun test_9() {
    do {
        konst x = 1
    } while (false)
    konst y = 2
}

fun test_10() {
    do {
        konst x = 1
    } while (<!NON_TRIVIAL_BOOLEAN_CONSTANT!>false && true<!>)
    konst y = 2
}
