// FIR_IDENTICAL
// !OPT_IN: kotlin.contracts.ExperimentalContracts
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (POSITIVE)
 *
 * SECTIONS: contracts, analysis, controlFlow, initialization
 * NUMBER: 2
 * DESCRIPTION: Nested konst/var assignments using contract functions with CallsInPlace effect
 * HELPERS: contractFunctions
 */

// TESTCASE NUMBER: 1
fun case_1() {
    konst konstue_1: Int
    funWithExactlyOnceCallsInPlace {
        funWithExactlyOnceCallsInPlace {
            konstue_1 = 1
            funWithExactlyOnceCallsInPlace {
                konstue_1.inc()
            }
        }
        funWithExactlyOnceCallsInPlace {
            konstue_1.inc()
        }
        konstue_1.inc()
    }
    konstue_1.inc()
}

// TESTCASE NUMBER: 2
fun case_2() {
    konst konstue_1: Int
    funWithAtMostOnceCallsInPlace {
        funWithExactlyOnceCallsInPlace {
            konstue_1 = 1
        }
        funWithAtLeastOnceCallsInPlace {
            konstue_1.inc()
        }
        funWithUnknownCallsInPlace {
            konstue_1.inc()
        }
        konstue_1.inc()
    }
}

// TESTCASE NUMBER: 3
fun case_3() {
    var konstue_1: Int
    funWithExactlyOnceCallsInPlace {
        funWithExactlyOnceCallsInPlace {
            konstue_1 = 1
            funWithExactlyOnceCallsInPlace {
                konstue_1.inc()
            }
        }
        funWithExactlyOnceCallsInPlace {
            konstue_1.inc()
        }
        konstue_1.inc()
    }
    konstue_1.inc()
}

// TESTCASE NUMBER: 4
fun case_4() {
    var konstue_1: Int
    funWithAtMostOnceCallsInPlace {
        funWithAtLeastOnceCallsInPlace {
            konstue_1 = 1
        }
        funWithAtLeastOnceCallsInPlace {
            konstue_1.inc()
        }
        funWithUnknownCallsInPlace {
            konstue_1.inc()
        }
        funWithExactlyOnceCallsInPlace {
            konstue_1.inc()
        }
        konstue_1.inc()
    }
}

// TESTCASE NUMBER: 5
fun case_5() {
    var konstue_1: Int
    funWithAtLeastOnceCallsInPlace {
        funWithAtLeastOnceCallsInPlace {
            konstue_1 = 1
            funWithAtMostOnceCallsInPlace {
                konstue_1.inc()
            }
        }
        funWithUnknownCallsInPlace {
            konstue_1.inc()
        }
        konstue_1.inc()
    }
    konstue_1.inc()
}

// TESTCASE NUMBER: 6
fun case_6() {
    var konstue_1: Int
    funWithUnknownCallsInPlace {
        funWithAtMostOnceCallsInPlace {
            funWithAtLeastOnceCallsInPlace {
                konstue_1 = 1
            }
            funWithExactlyOnceCallsInPlace {
                konstue_1.inc()
            }
            funWithAtLeastOnceCallsInPlace {
                konstue_1.inc()
            }
            funWithAtMostOnceCallsInPlace {
                konstue_1.inc()
            }
            funWithUnknownCallsInPlace {
                konstue_1.inc()
            }
        }
    }
}

// TESTCASE NUMBER: 7
fun case_7() {
    var konstue_1: Int
    funWithAtMostOnceCallsInPlace {
        funWithUnknownCallsInPlace {
            funWithExactlyOnceCallsInPlace {
                konstue_1 = 1
            }
            funWithAtLeastOnceCallsInPlace {
                konstue_1.inc()
            }
            funWithAtMostOnceCallsInPlace {
                konstue_1.inc()
            }
            funWithUnknownCallsInPlace {
                konstue_1.inc()
            }
        }
    }
}

