// !OPT_IN: kotlin.contracts.ExperimentalContracts

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (NEGATIVE)
 *
 * SECTIONS: contracts, analysis, smartcasts
 * NUMBER: 15
 * DESCRIPTION: Check smartcasts working if type checking for contract function is used
 * ISSUES: KT-27241
 * HELPERS: contractFunctions
 */

// FILE: contracts.kt

package contracts

import kotlin.contracts.*

// TESTCASE NUMBER: 1
fun case_1_1(cond: Boolean): Any {
    contract { returns(true) implies cond }
    return cond
}
fun case_1_2(konstue: Any): Boolean {
    contract { returns(true) implies (konstue is Boolean) }
    return konstue is Boolean
}

// TESTCASE NUMBER: 2
fun case_2(cond: Boolean): Any {
    contract { returns(true) implies cond }
    return cond
}

// FILE: main.kt

import contracts.*

// TESTCASE NUMBER: 1
fun case_1(konstue: Any) {
    if (contracts.case_1_2(contracts.case_1_1(konstue is Char))) {
        println(konstue.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>category<!>)
    }
}

// TESTCASE NUMBER: 2
fun case_2(konstue: Any) {
    if (contracts.case_2(konstue is Char) is Boolean) {
        println(konstue.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>category<!>)
    }
}

// TESTCASE NUMBER: 3
fun case_3(konstue: String?) {
    if (<!USELESS_IS_CHECK!>!konstue.isNullOrEmpty() is Boolean<!>) {
        konstue<!UNSAFE_CALL!>.<!>length
    }
}
