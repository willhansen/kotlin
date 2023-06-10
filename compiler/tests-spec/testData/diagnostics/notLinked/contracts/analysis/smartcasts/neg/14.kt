// !OPT_IN: kotlin.contracts.ExperimentalContracts

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (NEGATIVE)
 *
 * SECTIONS: contracts, analysis, smartcasts
 * NUMBER: 14
 * DESCRIPTION: Smartcast using many of the various Returns effects on the same konstues.
 * HELPERS: contractFunctions
 */

// FILE: contracts.kt

package contracts

import kotlin.contracts.*

// TESTCASE NUMBER: 1
fun <T> T?.case_1_1(): Boolean {
    contract { returns(false) implies (this@case_1_1 != null) }
    return !(this@case_1_1 != null)
}
fun <T> T?.case_1_2(): Boolean? {
    contract { returns(null) implies (this@case_1_2 is String) }
    return if (this@case_1_2 is String) null else true
}

// TESTCASE NUMBER: 2
fun <T> T?.case_2_1(): Boolean {
    contract { returns(true) implies (this@case_2_1 is Float) }
    return this@case_2_1 is Float
}
fun <T> T?.case_2_2(): Boolean {
    contract { returns(false) implies (this@case_2_2 is Double) }
    return !(this@case_2_2 is Double)
}

// FILE: main.kt

import contracts.*

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Any?) {
    if (!(konstue_1.case_1_1() || konstue_1.case_1_2() == null)) {
        println(konstue_1.<!UNRESOLVED_REFERENCE!>length<!>)
    }
}

/*
 * TESTCASE NUMBER: 2
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-1982
 */
fun case_2(konstue_1: Any?) {
    if (konstue_1.case_2_1() || !konstue_1.case_2_2()) {
        println(konstue_1.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
    }
}
