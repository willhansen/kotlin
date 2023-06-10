// LANGUAGE: -BreakContinueInInlineLambdas
// !OPT_IN: kotlin.contracts.ExperimentalContracts
// SKIP_TXT

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Boolean) {
    while (konstue_1) {
        funWithExactlyOnceCallsInPlace {
            <!UNSUPPORTED_FEATURE!>break<!>
        }
        println("1")
    }

    loop@ for (i in 0..10) {
        funWithExactlyOnceCallsInPlace {
            <!UNSUPPORTED_FEATURE!>break@loop<!>
        }
        println("1")
    }
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Boolean) {
    for (i in 0..10) {
        funWithExactlyOnceCallsInPlace {
            <!UNSUPPORTED_FEATURE!>continue<!>
        }
        println("1")
    }

    loop@ while (konstue_1) {
        funWithExactlyOnceCallsInPlace {
            <!UNSUPPORTED_FEATURE!>continue@loop<!>
        }
        println("1")
    }
}
