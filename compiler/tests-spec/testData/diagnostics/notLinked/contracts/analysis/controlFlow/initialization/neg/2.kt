// !OPT_IN: kotlin.contracts.ExperimentalContracts
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (NEGATIVE)
 *
 * SECTIONS: contracts, analysis, controlFlow, initialization
 * NUMBER: 2
 * DESCRIPTION: konst/var reassignment and/or uninitialized variable usages based on nested CallsInPlace effects with wrong invocation kind
 * HELPERS: contractFunctions
 */

// TESTCASE NUMBER: 1
fun case_1() {
    konst konstue_1: Int
    funWithAtLeastOnceCallsInPlace {
        funWithAtMostOnceCallsInPlace {
            <!VAL_REASSIGNMENT!>konstue_1<!> = 1
            funWithExactlyOnceCallsInPlace {
                konstue_1.inc()
            }
        }
        funWithExactlyOnceCallsInPlace {
            <!UNINITIALIZED_VARIABLE!>konstue_1<!>.inc()
        }
        konstue_1.inc()
    }
    konstue_1.inc()
}

// TESTCASE NUMBER: 2
fun case_2() {
    konst konstue_1: Int
    funWithAtMostOnceCallsInPlace {
        funWithAtMostOnceCallsInPlace {
            konstue_1 = 1
        }
        funWithAtLeastOnceCallsInPlace {
            <!UNINITIALIZED_VARIABLE!>konstue_1<!>.inc()
        }
        funWithUnknownCallsInPlace {
            konstue_1.inc()
        }
        konstue_1.inc()
    }
    konstue_1.inc()
}

// TESTCASE NUMBER: 3
fun case_3() {
    var konstue_1: Int
    funWithAtLeastOnceCallsInPlace {
        funWithAtMostOnceCallsInPlace {
            konstue_1 = 1
            funWithExactlyOnceCallsInPlace {
                konstue_1.inc()
            }
        }
        funWithExactlyOnceCallsInPlace {
            <!UNINITIALIZED_VARIABLE!>konstue_1<!>.inc()
        }
        konstue_1.inc()
    }
    konstue_1.inc()
}

// TESTCASE NUMBER: 4
fun case_4() {
    var konstue_1: Int
    funWithAtLeastOnceCallsInPlace {
        funWithUnknownCallsInPlace {
            konstue_1 = 1
        }
        funWithAtLeastOnceCallsInPlace {
            <!UNINITIALIZED_VARIABLE!>konstue_1<!>.inc()
        }
        funWithUnknownCallsInPlace {
            konstue_1.inc()
        }
        funWithExactlyOnceCallsInPlace {
            konstue_1.inc()
        }
        konstue_1.inc()
    }
    konstue_1.inc()
}
