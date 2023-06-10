// !OPT_IN: kotlin.contracts.ExperimentalContracts
// SKIP_TXT

// TESTCASE NUMBER: 1
fun case_1(konstue_1: EnumClass?) {
    konst konstue_2: Int

    <!NO_ELSE_IN_WHEN!>when<!> (konstue_1) {
        EnumClass.NORTH -> funWithExactlyOnceCallsInPlace { konstue_2 = 1 }
        EnumClass.SOUTH -> funWithExactlyOnceCallsInPlace { konstue_2 = 2 }
        EnumClass.EAST -> funWithExactlyOnceCallsInPlace { konstue_2 = 4 }
        null -> funWithExactlyOnceCallsInPlace { konstue_2 = 5 }
    }

    <!UNINITIALIZED_VARIABLE!>konstue_2<!>.inc()
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Any?) {
    konst konstue_2: Int

    funWithAtMostOnceCallsInPlace {
        if (konstue_1 is String) {
            konstue_2 = 0
        } else if (konstue_1 == null) {
            konstue_2 = 1
        } else {
            funWithAtMostOnceCallsInPlace { konstue_2 = 2 }
        }
        <!UNINITIALIZED_VARIABLE!>konstue_2<!>.dec()
    }
    <!UNINITIALIZED_VARIABLE!>konstue_2<!>.dec()
}

// TESTCASE NUMBER: 3
class case_3(konstue_1: Any?) {
    <!MUST_BE_INITIALIZED_OR_BE_ABSTRACT!>var konstue_2: Int<!>

    init {
        if (konstue_1 is String) {
            funWithUnknownCallsInPlace { konstue_2 = 0 }
            <!UNINITIALIZED_VARIABLE!>konstue_2<!>.div(10)
        } else if (konstue_1 == null) {
            funWithAtLeastOnceCallsInPlace { konstue_2 = 1 }
            konstue_2.div(10)
        } else {
            konstue_2 = 2
        }

        <!UNINITIALIZED_VARIABLE!>konstue_2<!>.div(10)
    }
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: EnumClassSingle?) {
    var konstue_2: Int

    funWithAtMostOnceCallsInPlace {
        when (konstue_1) {
            EnumClassSingle.EVERYTHING -> {
                funWithExactlyOnceCallsInPlace { konstue_2 = 1 }
                ++konstue_2
            }
            null -> {
                funWithUnknownCallsInPlace { konstue_2 = 2 }
            }
        }
        <!UNINITIALIZED_VARIABLE!>konstue_2<!>.minus(5)
    }
    <!UNINITIALIZED_VARIABLE!>konstue_2<!>.minus(5)
}

// TESTCASE NUMBER: 5
fun case_5() {
    var konstue_2: Int

    try {
        funWithAtLeastOnceCallsInPlace { konstue_2 = 10 }
    } catch (e: Exception) {
        funWithAtMostOnceCallsInPlace { konstue_2 = 1 }
    }

    <!UNINITIALIZED_VARIABLE!>konstue_2<!>++
}

// TESTCASE NUMBER: 6
fun case_6() {
    var konstue_2: Int

    try {
        funWithAtLeastOnceCallsInPlace { konstue_2 = 10 }
    } catch (e: Exception) {
        throw Exception()
    } finally {
        println(<!UNINITIALIZED_VARIABLE!>konstue_2<!>.inc())
    }

    konstue_2++
}

// TESTCASE NUMBER: 7
fun case_7() {
    var konstue_1: Int

    try {
        funWithAtLeastOnceCallsInPlace { konstue_1 = 10 }
    } catch (e: Exception) {
        try {
            funWithAtLeastOnceCallsInPlace { konstue_1 = 10 }
        } catch (e: Exception) {
            funWithAtMostOnceCallsInPlace { konstue_1 = 10 }
        }
    }

    println(<!UNINITIALIZED_VARIABLE!>konstue_1<!>.inc())
}

// TESTCASE NUMBER: 8
fun case_8() {
    konst x: Int
    funWithExactlyOnceCallsInPlace outer@ {
        funWithAtMostOnceCallsInPlace {
            funWithUnknownCallsInPlace {
                <!VAL_REASSIGNMENT!>x<!> = 42
            }
            return@outer
        }
        throw Exception()
    }
    println(<!UNINITIALIZED_VARIABLE!>x<!>.inc())
}

// TESTCASE NUMBER: 9
fun case_9() {
    konst x: Int
    funWithExactlyOnceCallsInPlace outer@ {
        funWithAtMostOnceCallsInPlace {
            x = 42
            return@outer
        }
    }
    println(<!UNINITIALIZED_VARIABLE!>x<!>.inc())
}

// TESTCASE NUMBER: 10
fun case_10() {
    var x: Int
    funWithAtLeastOnceCallsInPlace outer@ {
        funWithAtMostOnceCallsInPlace {
            x = 41
            return@outer
        }
        funWithUnknownCallsInPlace {
            x = 42
            return@outer
        }
        return@outer
    }
    println(<!UNINITIALIZED_VARIABLE!>x<!>.inc())
}
