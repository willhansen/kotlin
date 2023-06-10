// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -UNREACHABLE_CODE -UNUSED_EXPRESSION
// !OPT_IN: kotlin.contracts.ExperimentalContracts

import kotlin.contracts.*

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Boolean?): Boolean {
    contract { <!ERROR_IN_CONTRACT_DESCRIPTION!>returns(true) implies (konstue_1 != null && konstue_1 == false)<!> }
    return konstue_1 != null && konstue_1 == false
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Boolean, konstue_2: Boolean): Boolean? {
    contract { <!ERROR_IN_CONTRACT_DESCRIPTION!>returnsNotNull() implies (konstue_1 != false || konstue_2)<!> }
    return if (konstue_1 != false || konstue_2) true else null
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: String?, konstue_2: Boolean): Boolean {
    contract { <!ERROR_IN_CONTRACT_DESCRIPTION!>returns(false) implies (konstue_1 != null && konstue_2 != true)<!> }
    return !(konstue_1 != null && konstue_2 != true)
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Nothing?, konstue_2: Boolean?): Boolean? {
    contract { <!ERROR_IN_CONTRACT_DESCRIPTION!>returns(null) implies (<!SENSELESS_COMPARISON!>konstue_1 == null<!> || konstue_2 != null || <!SENSELESS_COMPARISON!>konstue_2 == false<!>)<!> }
    return if (<!SENSELESS_COMPARISON!>konstue_1 == null<!> || konstue_2 != null || <!SENSELESS_COMPARISON!>konstue_2 == false<!>) null else true
}

// TESTCASE NUMBER: 5
fun case_5(konstue_1: Any?, konstue_2: String?): Boolean? {
    contract { <!ERROR_IN_CONTRACT_DESCRIPTION!>returns(null) implies (konstue_1 != null && konstue_2 != null || konstue_2 == ".")<!> }
    return if (konstue_1 != null && konstue_2 != null || konstue_2 == ".") null else true
}

// TESTCASE NUMBER: 6
fun case_6(konstue_1: Boolean, konstue_2: Int?): Boolean? {
    contract { <!ERROR_IN_CONTRACT_DESCRIPTION!>returns(null) implies (konstue_2 == null && konstue_1 || konstue_2 == 0)<!> }
    return if (konstue_2 == null && konstue_1 || konstue_2 == 0) null else true
}
