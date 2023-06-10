// !OPT_IN: kotlin.contracts.ExperimentalContracts
// SKIP_TXT

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (NEGATIVE)
 *
 * SECTIONS: contracts, analysis, controlFlow, initialization
 * NUMBER: 5
 * DESCRIPTION: CallsInPlace contract functions with inkonstid lambda passing to function parameter.
 * HELPERS: contractFunctions
 */

// TESTCASE NUMBER: 1
fun case_1() {
    var konstue_1: Int
    konst l = { konstue_1 = 10 }
    funWithAtLeastOnceCallsInPlace(l)
    <!UNINITIALIZED_VARIABLE!>konstue_1<!>.inc()
}

// TESTCASE NUMBER: 2
fun case_2() {
    var konstue_1: Int
    konst l = fun () { konstue_1 = 10 }
    funWithAtLeastOnceCallsInPlace(l)
    <!UNINITIALIZED_VARIABLE!>konstue_1<!>.inc()
}

// TESTCASE NUMBER: 3
fun case_3() {
    var konstue_1: Int
    funWithAtLeastOnceCallsInPlace(fun () { konstue_1 = 10 })
    <!UNINITIALIZED_VARIABLE!>konstue_1<!>.inc()
}

// TESTCASE NUMBER: 4
fun case_4() {
    konst konstue_1: Int
    konst o = object {
        fun l() { <!CAPTURED_VAL_INITIALIZATION!>konstue_1<!> = 10 }
    }
    funWithExactlyOnceCallsInPlace(o::l)
    <!UNINITIALIZED_VARIABLE!>konstue_1<!>.inc()
}
