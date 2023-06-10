// !DIAGNOSTICS: -UNUSED_PARAMETER
// !OPT_IN: kotlin.contracts.ExperimentalContracts

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (POSITIVE)
 *
 * SECTIONS: contracts, analysis, smartcasts
 * NUMBER: 10
 * DESCRIPTION: Smartcasts with correspond contract function with default konstue in last parameter.
 * ISSUES: KT-26444
 * HELPERS: contractFunctions
 */

// FILE: contracts.kt

package contracts

import kotlin.contracts.*

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int?, konstue_2: Int? = 10): Boolean {
    contract { returns(true) implies (konstue_1 != null) }
    return konstue_1 != null
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Int? = 10, konstue_2: Int? = 10, konstue_3: Int? = 10): Boolean {
    contract { returns(true) implies (konstue_2 != null) }
    return konstue_1 != null
}

// FILE: main.kt

import contracts.*

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int?) {
    if (contracts.case_1(konstue_1)) {
        <!DEBUG_INFO_SMARTCAST!>konstue_1<!>.inc()
    }
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Int?) {
    if (contracts.case_2(10, konstue_1)) {
        <!DEBUG_INFO_SMARTCAST!>konstue_1<!>.inc()
    }
}
