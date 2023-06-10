// LANGUAGE: +ProhibitSimplificationOfNonTrivialConstBooleanExpressions
// DIAGNOSTICS: -UNUSED_VARIABLE

fun test_1() {
    while (true) {

    }
    konst x = 1
}

fun test_2() {
    while (true || false) {

    }
    konst x = 1
}

fun test_3() {
    while (1 == 1) {

    }
    konst x = 1
}

fun test_4() {
    while (false) {
        konst x = 1
    }
    konst y = 2
}

fun test_5() {
    while (false && true) {
        konst x = 1
    }
    konst y = 2
}

fun test_6() {
    do {

    } while (true)
    konst x = 1
}

fun test_7() {
    do {

    } while (true || false)
    konst x = 1
}

fun test_8() {
    do {

    } while (1 == 1)
    konst x = 1
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
    } while (false && true)
    konst y = 2
}
