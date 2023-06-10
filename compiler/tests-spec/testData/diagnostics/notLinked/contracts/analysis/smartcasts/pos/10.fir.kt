// !DIAGNOSTICS: -UNUSED_PARAMETER
// !OPT_IN: kotlin.contracts.ExperimentalContracts

// FILE: contracts.kt

package contracts

import kotlin.contracts.*

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int?, konstue_2: Int? = 10): Boolean {
    contract { returns(true) implies (konstue_1 != null) }
    return konstue_1 != null
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Int? = 10, konstue_2: Int? = 10, konstue_3: Int? = 10): Boolean {
    <!WRONG_IMPLIES_CONDITION!>contract { returns(true) implies (konstue_2 != null) }<!>
    return konstue_1 != null
}

// FILE: main.kt

import contracts.*

// TESTCASE NUMBER: 1
fun case_1(konstue_1: Int?) {
    if (contracts.case_1(konstue_1)) {
        konstue_1.inc()
    }
}

// TESTCASE NUMBER: 2
fun case_2(konstue_1: Int?) {
    if (contracts.case_2(10, konstue_1)) {
        konstue_1.inc()
    }
}
