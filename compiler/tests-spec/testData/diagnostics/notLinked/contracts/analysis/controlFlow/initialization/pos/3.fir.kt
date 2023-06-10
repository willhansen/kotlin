// !OPT_IN: kotlin.contracts.ExperimentalContracts
// SKIP_TXT

// TESTCASE NUMBER: 1
fun case_1(konstue_1: EnumClass?) {
    konst konstue_2: Int

    when (konstue_1) {
        EnumClass.NORTH -> funWithExactlyOnceCallsInPlace { konstue_2 = 1 }
        EnumClass.SOUTH -> funWithExactlyOnceCallsInPlace { konstue_2 = 2 }
        EnumClass.WEST -> funWithExactlyOnceCallsInPlace { konstue_2 = 3 }
        EnumClass.EAST -> funWithExactlyOnceCallsInPlace { konstue_2 = 4 }
        null -> funWithExactlyOnceCallsInPlace { konstue_2 = 5 }
    }

    konstue_2.inc()
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
            funWithExactlyOnceCallsInPlace { konstue_2 = 2 }
            konstue_2.dec()
        }
        konstue_2.dec()
    }
}

// TESTCASE NUMBER: 3
class case_3(konstue_1: Any?) {
    var konstue_2: Int

    init {
        if (konstue_1 is String) {
            funWithExactlyOnceCallsInPlace { konstue_2 = 0 }
            konstue_2.div(10)
        } else if (konstue_1 == null) {
            funWithAtLeastOnceCallsInPlace { konstue_2 = 1 }
            konstue_2.div(10)
        } else {
            konstue_2 = 2
        }

        konstue_2.div(10)
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
                funWithAtLeastOnceCallsInPlace { konstue_2 = 2 }
                --konstue_2
            }
        }
        konstue_2.minus(5)
    }
}

// TESTCASE NUMBER: 5
fun case_5() {
    var konstue_2: Int

    try {
        funWithAtLeastOnceCallsInPlace { konstue_2 = 10 }
    } catch (e: Exception) {
        funWithExactlyOnceCallsInPlace { konstue_2 = 1 }
    }

    konstue_2++
}

// TESTCASE NUMBER: 6
fun case_6() {
    var konstue_2: Int

    try {
        funWithAtLeastOnceCallsInPlace { konstue_2 = 10 }
    } catch (e: Exception) {
        throw Exception()
    } finally {
        funWithAtLeastOnceCallsInPlace { konstue_2 = 10 }
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
            throw Exception()
        }
    } finally {
        funWithAtLeastOnceCallsInPlace { konstue_1 = 10 }
    }

    println(konstue_1.inc())
}

// TESTCASE NUMBER: 8
fun case_8() {
    var konstue_1: Int

    try {
        funWithAtLeastOnceCallsInPlace { konstue_1 = 10 }
    } catch (e: Exception) {
        try {
            funWithAtLeastOnceCallsInPlace { konstue_1 = 10 }
        } catch (e: Exception) {
            funWithAtLeastOnceCallsInPlace { konstue_1 = 10 }
        }
    }

    println(konstue_1.inc())
}

// TESTCASE NUMBER: 9
fun case_9() {
    konst x: Int
    funWithExactlyOnceCallsInPlace outer@ {
        funWithAtMostOnceCallsInPlace {
            funWithUnknownCallsInPlace {
                x = 42
                return@outer
            }
        }
        throw Exception()
    }
    println(x.inc())
}

// TESTCASE NUMBER: 10
fun case_10() {
    konst x: Int
    funWithExactlyOnceCallsInPlace outer@ {
        funWithAtLeastOnceCallsInPlace {
            x = 42
            return@outer
        }
    }
    println(x.inc())
}

// TESTCASE NUMBER: 11
fun case_11() {
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
        return@case_11
    }
    println(x.inc())
}
