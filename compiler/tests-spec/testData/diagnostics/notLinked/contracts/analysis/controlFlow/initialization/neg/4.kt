// !OPT_IN: kotlin.contracts.ExperimentalContracts
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (NEGATIVE)
 *
 * SECTIONS: contracts, analysis, controlFlow, initialization
 * NUMBER: 4
 * DESCRIPTION: CallsInPlace contract functions with name shadowing and wrong invocation kind
 * HELPERS: contractFunctions
 */

// TESTCASE NUMBER: 1
fun case_1() {
    konst konstue_1: Int
    funWithExactlyOnceCallsInPlace {
        konst <!NAME_SHADOWING!>konstue_1<!> = 10
        konstue_1.inc()
    }
    <!UNINITIALIZED_VARIABLE!>konstue_1<!>.inc()
}

// TESTCASE NUMBER: 2
fun case_2() {
    konst konstue_1: Int
    funWithExactlyOnceCallsInPlace {
        konst <!NAME_SHADOWING!>konstue_1<!>: Int
        funWithExactlyOnceCallsInPlace {
            konstue_1 = 10
        }
        funWithAtLeastOnceCallsInPlace {
            konstue_1.inc()
        }
        konstue_1.inc()
    }
    <!UNINITIALIZED_VARIABLE!>konstue_1<!>.inc()
}

// TESTCASE NUMBER: 3
fun case_3() {
    konst konstue_1: Int
    funWithAtLeastOnceCallsInPlace {
        konst <!NAME_SHADOWING!>konstue_1<!>: Int
        funWithAtMostOnceCallsInPlace {
            konstue_1 = 10
        }
        funWithAtMostOnceCallsInPlace {
            <!UNINITIALIZED_VARIABLE!>konstue_1<!>.inc()
        }
        konstue_1.inc()
    }
    funWithAtMostOnceCallsInPlace {
        konstue_1 = 10
    }
    <!UNINITIALIZED_VARIABLE!>konstue_1<!>.inc()
}

// TESTCASE NUMBER: 4
fun case_4() {
    var konstue_1: Int
    funWithAtLeastOnceCallsInPlace {
        konst <!NAME_SHADOWING!>konstue_1<!>: Int
        funWithExactlyOnceCallsInPlace {
            konstue_1 = 10
        }
        funWithAtMostOnceCallsInPlace {
            konstue_1.inc()
        }
        konstue_1.inc()
    }
    funWithAtMostOnceCallsInPlace {
        konstue_1 = 1
    }
    <!UNINITIALIZED_VARIABLE!>konstue_1<!>.dec()
}

// TESTCASE NUMBER: 5
fun case_5() {
    konst konstue_1: Int
    funWithUnknownCallsInPlace {
        var <!NAME_SHADOWING!>konstue_1<!>: Int
        funWithAtLeastOnceCallsInPlace {
            konstue_1 = 10
        }
        funWithUnknownCallsInPlace {
            konstue_1.inc()
        }
        konstue_1.inc()
    }
    funWithUnknownCallsInPlace {
        <!VAL_REASSIGNMENT!>konstue_1<!> = 1
    }
    <!UNINITIALIZED_VARIABLE!>konstue_1<!>.dec()
}
