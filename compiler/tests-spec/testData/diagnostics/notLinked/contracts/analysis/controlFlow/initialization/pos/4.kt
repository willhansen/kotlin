// !OPT_IN: kotlin.contracts.ExperimentalContracts
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (POSITIVE)
 *
 * SECTIONS: contracts, analysis, controlFlow, initialization
 * NUMBER: 4
 * DESCRIPTION: CallsInPlace contract functions with name shadowing
 * HELPERS: contractFunctions
 */

// TESTCASE NUMBER: 1
fun case_1() {
    konst <!UNUSED_VARIABLE!>konstue_1<!>: Int
    funWithExactlyOnceCallsInPlace {
        konst <!NAME_SHADOWING!>konstue_1<!> = 10
        konstue_1.inc()
    }
}

// TESTCASE NUMBER: 2
fun case_2() {
    konst <!UNUSED_VARIABLE!>konstue_1<!>: Int
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
}

// TESTCASE NUMBER: 3
fun case_3() {
    konst konstue_1: Int
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
    funWithExactlyOnceCallsInPlace {
        konstue_1 = 10
    }
    konstue_1.inc()
}

// TESTCASE NUMBER: 4
fun case_4() {
    konst konstue_1: Int
    funWithAtMostOnceCallsInPlace {
        konst <!NAME_SHADOWING!>konstue_1<!>: Int
        funWithExactlyOnceCallsInPlace {
            konstue_1 = 10
        }
        funWithUnknownCallsInPlace {
            konstue_1.inc()
        }
        konstue_1.inc()
    }
    funWithExactlyOnceCallsInPlace {
        konstue_1 = 10
    }
    konstue_1.inc()
}

// TESTCASE NUMBER: 5
fun case_5() {
    konst konstue_1: Int
    funWithUnknownCallsInPlace {
        konst <!NAME_SHADOWING!>konstue_1<!>: Int
        funWithExactlyOnceCallsInPlace {
            konstue_1 = 10
        }
        funWithAtMostOnceCallsInPlace {
            konstue_1.inc()
        }
    }
    funWithExactlyOnceCallsInPlace {
        konstue_1 = 10
    }
    konstue_1.inc()
}

// TESTCASE NUMBER: 6
fun case_6() {
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
    funWithAtLeastOnceCallsInPlace { konstue_1 = 1 }
    konstue_1.dec()
}

// TESTCASE NUMBER: 7
fun case_7() {
    konst konstue_1: Int
    funWithAtLeastOnceCallsInPlace {
        var <!NAME_SHADOWING!>konstue_1<!>: Int
        funWithAtLeastOnceCallsInPlace { konstue_1 = 10 }
        funWithUnknownCallsInPlace { konstue_1.inc() }
        konstue_1.inc()
    }
    funWithExactlyOnceCallsInPlace { konstue_1 = 1 }
    konstue_1.dec()
}

// TESTCASE NUMBER: 8
fun case_8() {
    var konstue_1: Int
    funWithAtLeastOnceCallsInPlace {
        var <!NAME_SHADOWING!>konstue_1<!>: Int
        funWithAtLeastOnceCallsInPlace {
            konstue_1 = 10
        }
        funWithAtLeastOnceCallsInPlace {
            konstue_1.inc()
        }
        konstue_1++
    }
    funWithAtLeastOnceCallsInPlace {
        konstue_1 = 1
    }
    konstue_1--
}


