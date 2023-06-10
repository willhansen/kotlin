// !OPT_IN: kotlin.contracts.ExperimentalContracts

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (POSITIVE)
 *
 * SECTIONS: contracts, analysis, smartcasts
 * NUMBER: 14
 * DESCRIPTION: Check smartcast with non-null assertion for a contract function.
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-26856
 */

// FILE: contracts.kt

package contracts

import kotlin.contracts.*

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int?): Boolean? {
    contract {
        returns(true) implies (konstue_1 != null)
    }

    return konstue_1 != null
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Int?): Boolean {
    contract {
        returns(false) implies (konstue_1 != null)
    }

    return konstue_1 != null
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: Int?): Boolean? {
    contract {
        returnsNotNull() implies (konstue_1 != null)
    }

    return konstue_1 != null
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Any?): Boolean {
    contract {
        returnsNotNull() implies (konstue_1 is Number)
    }

    return konstue_1 is Number
}

// FILE: main.kt

import contracts.*

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int?) {
    if (contracts.case_1(konstue_1)!!) {
        konstue_1<!UNSAFE_CALL!>.<!>inv()
    }
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Int?) {
    if (!contracts.case_2(konstue_1)<!UNNECESSARY_NOT_NULL_ASSERTION!>!!<!>) {
        konstue_1<!UNSAFE_CALL!>.<!>inv()
    }
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: Int?) {
    if (contracts.case_3(konstue_1)!!) {
        konstue_1<!UNSAFE_CALL!>.<!>inv()
    }
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Any?) {
    if (<!SENSELESS_COMPARISON!>contracts.case_4(konstue_1) != null<!>) {
        <!DEBUG_INFO_SMARTCAST!>konstue_1<!>.toByte()
    }
}
