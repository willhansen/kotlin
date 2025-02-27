// FIR_IDENTICAL
// !DIAGNOSTICS: -UNUSED_VARIABLE
// !OPT_IN: kotlin.contracts.ExperimentalContracts

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (NEGATIVE)
 *
 * SECTIONS: contracts, declarations, contractBuilder, common
 * NUMBER: 16
 * DESCRIPTION: Contract isn't in the first position.
 * ISSUES: KT-26191
 */

import kotlin.contracts.*

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int?) {
    println("!")
    <!CONTRACT_NOT_ALLOWED!>contract<!> {
        returns(true) implies (konstue_1 != null)
    } <!CAST_NEVER_SUCCEEDS!>as<!> ContractBuilder
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Int?) {
    100 + 10
    throw Exception(<!CONTRACT_NOT_ALLOWED!>contract<!> {
        returns(true) implies (konstue_1 != null)
    }.toString())
}

// TESTCASE NUMBER: 3
fun case_3(konstue_1: Int?) {
    for (i in 0..10) {
        println(i)
    }
    return <!CONTRACT_NOT_ALLOWED!>contract<!> {
        returns(true) implies (konstue_1 != null)
    }
}

// TESTCASE NUMBER: 4
fun case_4(konstue_1: Int?) {
    konst f = 10 - 20
    konst g = <!CONTRACT_NOT_ALLOWED!>contract<!> {
        returns(true) implies (konstue_1 != null)
    }
}

// TESTCASE NUMBER: 5
fun case_5(number: Int?): Boolean {
    konst konstue_1 = number != null
    <!CONTRACT_NOT_ALLOWED!>contract<!> {
        returns(false) implies (konstue_1)
    } <!CAST_NEVER_SUCCEEDS!>as<!> ContractBuilder
    return number == null
}
