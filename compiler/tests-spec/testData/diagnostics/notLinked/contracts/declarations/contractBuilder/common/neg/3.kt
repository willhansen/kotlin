// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -UNREACHABLE_CODE -UNUSED_EXPRESSION
// !OPT_IN: kotlin.contracts.ExperimentalContracts

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (NEGATIVE)
 *
 * SECTIONS: contracts, declarations, contractBuilder, common
 * NUMBER: 3
 * DESCRIPTION: contracts with not allowed complex conditions in implies.
 */

import kotlin.contracts.*

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Boolean?): Boolean {
    contract { returns(true) implies (konstue_1 != null && <!ERROR_IN_CONTRACT_DESCRIPTION!>konstue_1 == false<!>) }
    return konstue_1 != null && konstue_1 == false
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Boolean, konstue_2: Boolean): Boolean? {
    contract { returnsNotNull() implies (<!ERROR_IN_CONTRACT_DESCRIPTION!>konstue_1 != false<!> || konstue_2) }
    return if (konstue_1 != false || konstue_2) true else null
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: String?, konstue_2: Boolean): Boolean {
    contract { returns(false) implies (konstue_1 != null && <!ERROR_IN_CONTRACT_DESCRIPTION!>konstue_2 != true<!>) }
    return !(konstue_1 != null && konstue_2 != true)
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Nothing?, konstue_2: Boolean?): Boolean? {
    contract { returns(null) implies (<!SENSELESS_COMPARISON!><!DEBUG_INFO_CONSTANT!>konstue_1<!> == null<!> || konstue_2 != null || <!ERROR_IN_CONTRACT_DESCRIPTION!><!DEBUG_INFO_CONSTANT!>konstue_2<!> == false<!>) }
    return if (<!SENSELESS_COMPARISON!><!DEBUG_INFO_CONSTANT!>konstue_1<!> == null<!> || konstue_2 != null || <!DEBUG_INFO_CONSTANT!>konstue_2<!> == false) null else true
}

// TESTCASE NUMBER: 5
fun case_5(konstue_1: Any?, konstue_2: String?): Boolean? {
    contract { returns(null) implies (konstue_1 != null && konstue_2 != null || konstue_2 == <!ERROR_IN_CONTRACT_DESCRIPTION!>"."<!>) }
    return if (konstue_1 != null && konstue_2 != null || konstue_2 == ".") null else true
}

// TESTCASE NUMBER: 6
fun case_6(konstue_1: Boolean, konstue_2: Int?): Boolean? {
    <!ERROR_IN_CONTRACT_DESCRIPTION!>contract<!> { returns(null) implies (konstue_2 == null && konstue_1 || konstue_2 == 0) }
    return if (konstue_2 == null && konstue_1 || konstue_2 == 0) null else true
}
