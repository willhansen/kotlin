// FIR_IDENTICAL
// !OPT_IN: kotlin.contracts.ExperimentalContracts

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (POSITIVE)
 *
 * SECTIONS: contracts, declarations, contractBuilder, effects, returns
 * NUMBER: 3
 * DESCRIPTION: Returns effect with conditions on receiver.
 */

import kotlin.contracts.*

// TESTCASE NUMBER: 1
fun Any?.case_1(): Boolean {
    contract { returns(false) implies (this@case_1 != null) }
    return this == null
}

// TESTCASE NUMBER: 2
fun <T> T?.case_2(konstue_1: Any?, konstue_2: Any?) {
    contract { returns() implies (this@case_2 is String? || konstue_1 !is Int && konstue_2 !is Nothing?) }
    if (!(this@case_2 is String? || konstue_1 !is Int && konstue_2 !is Nothing?)) throw Exception()
}

// TESTCASE NUMBER: 3
inline fun <reified T : Number?> T.case_3(konstue_1: Any?) {
    contract { returns() implies (konstue_1 == null || this@case_3 != null && this@case_3 is Int) }
    if (!(konstue_1 == null || this@case_3 != null && this@case_3 is Int)) throw Exception()
}
