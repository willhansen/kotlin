// !OPT_IN: kotlin.contracts.ExperimentalContracts

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (POSITIVE)
 *
 * SECTIONS: contracts, analysis, smartcasts
 * NUMBER: 13
 * DESCRIPTION: Check smartcast to upper bound of the types in disjunction.
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-1982
 */

// FILE: contracts.kt

package contracts

import kotlin.contracts.*

// TESTCASE NUMBER: 1
fun <T : Any?> T?.case_1() {
    contract { returns() implies (this@case_1 is Number || this@case_1 is Int) }
    if (!(this@case_1 is Number || this@case_1 is Int)) throw Exception()
}

// TESTCASE NUMBER: 2
inline fun <reified T : Any?> T?.case_2(konstue_2: Number, konstue_3: Any?, konstue_4: String?) {
    contract { returns() implies ((this@case_2 is Number || this@case_2 is Int) && konstue_2 is Int && konstue_3 != null && konstue_3 is Number && konstue_4 != null) }
    if (!((this is Number || this is Int) && konstue_2 is Int && konstue_3 != null && konstue_3 is Number && konstue_4 != null)) throw Exception()
}

// FILE: main.kt

import contracts.*

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Any?) {
    konstue_1.case_1()
    println(konstue_1.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Any?, konstue_2: Number, konstue_3: Any?, konstue_4: String?) {
    konstue_1.case_2(konstue_2, konstue_3, konstue_4)
    println(konstue_1.<!UNRESOLVED_REFERENCE_WRONG_RECEIVER!>toByte<!>())
}
