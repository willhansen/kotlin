// !OPT_IN: kotlin.contracts.ExperimentalContracts
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (POSITIVE)
 *
 * SECTIONS: contracts, analysis, smartcasts
 * NUMBER: 12
 * DESCRIPTION: Smartcasts after non-null assertions or not-null konstue assignment in lambdas of contract function with 'exactly once' or 'at least once' CallsInPlace effects.
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-26148
 * HELPERS: contractFunctions
 */

// TESTCASE NUMBER: 1
fun case_1(arg: Int?) {
    funWithExactlyOnceCallsInPlace { arg!! }
    arg<!UNSAFE_CALL!>.<!>inc()
}

// TESTCASE NUMBER: 2
fun case_2(arg: Int?) {
    funWithAtLeastOnceCallsInPlace { arg!! }
    arg<!UNSAFE_CALL!>.<!>inc()
}

// TESTCASE NUMBER: 3
fun case_3() {
    konst konstue_1: Boolean?
    funWithExactlyOnceCallsInPlace { konstue_1 = false }
    konstue_1<!UNSAFE_CALL!>.<!>not()
}

// TESTCASE NUMBER: 4
fun case_4() {
    konst konstue_1: Boolean?
    funWithAtLeastOnceCallsInPlace { <!VAL_REASSIGNMENT!>konstue_1<!> = true }
    konstue_1<!UNSAFE_CALL!>.<!>not()
}
