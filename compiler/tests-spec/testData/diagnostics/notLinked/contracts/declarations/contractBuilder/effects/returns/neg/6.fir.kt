// !OPT_IN: kotlin.contracts.ExperimentalContracts

import kotlin.contracts.*

// TESTCASE NUMBER: 1
fun Boolean?.case_1(): Boolean {
    contract { <!ERROR_IN_CONTRACT_DESCRIPTION!>returns(true) implies (this@case_1 != null && this@case_1)<!> }
    return this != null && this
}

// TESTCASE NUMBER: 2
fun <T : <!FINAL_UPPER_BOUND!>Boolean<!>>T?.case_2(): Boolean {
    contract { <!ERROR_IN_CONTRACT_DESCRIPTION!>returns(true) implies (this@case_2 != null && this@case_2 !is Nothing && this@case_2)<!> }
    return this != null && this !is Nothing && this
}

// TESTCASE NUMBER: 3
fun <T>T?.case_3() {
    contract { <!ERROR_IN_CONTRACT_DESCRIPTION!>returns() implies (this@case_3 == null || this@case_3 is Boolean? && !this@case_3)<!> }
    if (!(this == null || this is Boolean? && !this)) throw Exception()
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Boolean?): Boolean {
    contract { <!ERROR_IN_CONTRACT_DESCRIPTION!>returns(true) implies (konstue_1 != null && !konstue_1)<!> }
    return konstue_1 != null && !konstue_1
}

// TESTCASE NUMBER: 5
fun Boolean.case_5(konstue_1: Any?): Boolean? {
    contract { <!ERROR_IN_CONTRACT_DESCRIPTION!>returnsNotNull() implies (konstue_1 is Boolean? && konstue_1 != null && konstue_1)<!> }
    return if (konstue_1 is Boolean? && konstue_1 != null && konstue_1) true else null
}

// TESTCASE NUMBER: 6
fun Boolean?.case_6(): Boolean? {
    contract { <!ERROR_IN_CONTRACT_DESCRIPTION!>returnsNotNull() implies (this@case_6 != null && this@case_6)<!> }
    return if (this@case_6 != null && this@case_6) true else null
}

// TESTCASE NUMBER: 7
fun <T : Boolean?> T.case_7(konstue_1: Any?): Boolean? {
    contract { <!ERROR_IN_CONTRACT_DESCRIPTION!>returnsNotNull() implies (konstue_1 is Boolean? && konstue_1 != null && konstue_1 && this@case_7 != null && this@case_7)<!> }
    return if (konstue_1 is Boolean? && konstue_1 != null && konstue_1 && this@case_7 != null && this@case_7) true else null
}
