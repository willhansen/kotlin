// !DIAGNOSTICS: -UNUSED_VARIABLE -UNUSED_PARAMETER -UNREACHABLE_CODE -UNUSED_EXPRESSION
// !OPT_IN: kotlin.contracts.ExperimentalContracts

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (NEGATIVE)
 *
 * SECTIONS: contracts, declarations, contractBuilder, common
 * NUMBER: 2
 * DESCRIPTION: contracts with not allowed simple conditions in implies
 */

import kotlin.contracts.*

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Boolean): Boolean {
    contract { returns(true) implies (<!ERROR_IN_CONTRACT_DESCRIPTION!>konstue_1 == true<!>) }
    return konstue_1 == true
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Boolean): Boolean? {
    contract { returnsNotNull() implies (<!ERROR_IN_CONTRACT_DESCRIPTION!>konstue_1 != false<!>) }
    return if (konstue_1 != false) true else null
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: String): Boolean {
    contract { returns(false) implies (konstue_1 != <!ERROR_IN_CONTRACT_DESCRIPTION!>""<!>) }
    return !(konstue_1 != "")
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Int): Boolean? {
    <!ERROR_IN_CONTRACT_DESCRIPTION!>contract<!> { returns(null) implies (konstue_1 == 0) }
    return if (konstue_1 == 0) null else true
}
