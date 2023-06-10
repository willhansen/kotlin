// !OPT_IN: kotlin.contracts.ExperimentalContracts
// SKIP_TXT

// TESTCASE NUMBER: 1
fun case_1(arg: Int?) {
    funWithExactlyOnceCallsInPlace { arg!! }
    arg.inc()
}

// TESTCASE NUMBER: 2
fun case_2(arg: Int?) {
    funWithAtLeastOnceCallsInPlace { arg!! }
    arg.inc()
}

// TESTCASE NUMBER: 3
fun case_3() {
    konst konstue_1: Boolean?
    funWithExactlyOnceCallsInPlace { konstue_1 = false }
    konstue_1.not()
}

// TESTCASE NUMBER: 4
fun case_4() {
    konst konstue_1: Boolean?
    funWithAtLeastOnceCallsInPlace { <!VAL_REASSIGNMENT!>konstue_1<!> = true }
    konstue_1.not()
}
