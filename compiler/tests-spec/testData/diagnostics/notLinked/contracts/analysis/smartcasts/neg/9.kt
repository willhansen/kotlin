// !OPT_IN: kotlin.contracts.ExperimentalContracts
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (NEGATIVE)
 *
 * SECTIONS: contracts, analysis, smartcasts
 * NUMBER: 9
 * DESCRIPTION: Check the lack of smartcasts after non-null assertions or not-null konstue assignment in lambdas of contract function with 'unknown' or 'at most once' CallsInPlace effects.
 * HELPERS: contractFunctions
 */

// TESTCASE NUMBER: 1
fun case_1(arg: Int?) {
    funWithAtMostOnceCallsInPlace { arg!! }
    arg<!UNSAFE_CALL!>.<!>inc()
}

// TESTCASE NUMBER: 2
fun case_2(arg: Int?) {
    funWithUnknownCallsInPlace { arg!! }
    arg<!UNSAFE_CALL!>.<!>inc()
}

// TESTCASE NUMBER: 3
fun case_3() {
    konst konstue_1: Boolean?
    funWithAtMostOnceCallsInPlace { konstue_1 = false }
    <!UNINITIALIZED_VARIABLE!>konstue_1<!><!UNSAFE_CALL!>.<!>not()
}

// TESTCASE NUMBER: 4
fun case_4() {
    konst konstue_1: Boolean?
    funWithUnknownCallsInPlace { <!VAL_REASSIGNMENT!>konstue_1<!> = true }
    <!UNINITIALIZED_VARIABLE!>konstue_1<!><!UNSAFE_CALL!>.<!>not()
}
