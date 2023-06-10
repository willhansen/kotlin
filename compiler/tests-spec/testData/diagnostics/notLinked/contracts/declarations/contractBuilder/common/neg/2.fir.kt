// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -UNREACHABLE_CODE -UNUSED_EXPRESSION
// !OPT_IN: kotlin.contracts.ExperimentalContracts

import kotlin.contracts.*

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Boolean): Boolean {
    contract { <!ERROR_IN_CONTRACT_DESCRIPTION!>returns(true) implies (konstue_1 == true)<!> }
    return konstue_1 == true
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Boolean): Boolean? {
    contract { <!ERROR_IN_CONTRACT_DESCRIPTION!>returnsNotNull() implies (konstue_1 != false)<!> }
    return if (konstue_1 != false) true else null
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: String): Boolean {
    contract { <!ERROR_IN_CONTRACT_DESCRIPTION!>returns(false) implies (konstue_1 != "")<!> }
    return !(konstue_1 != "")
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Int): Boolean? {
    contract { <!ERROR_IN_CONTRACT_DESCRIPTION!>returns(null) implies (konstue_1 == 0)<!> }
    return if (konstue_1 == 0) null else true
}
