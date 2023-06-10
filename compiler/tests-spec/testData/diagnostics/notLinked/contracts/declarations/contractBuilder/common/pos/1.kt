// !OPT_IN: kotlin.contracts.ExperimentalContracts

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (POSITIVE)
 *
 * SECTIONS: contracts, declarations, contractBuilder, common
 * NUMBER: 1
 * DESCRIPTION: Functions with simple contracts.
 */

import kotlin.contracts.*

// TESTCASE NUMBER: 1
inline fun case_1(block: () -> Unit) {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return block()
}

// TESTCASE NUMBER: 2
inline fun case_2(konstue_1: Int?, block: () -> Unit): Boolean {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        returns(true) implies (konstue_1 != null)
    }
    block()
    return konstue_1 != null
}

// TESTCASE NUMBER: 3
inline fun <T> T?.case_3(konstue_1: Int?, konstue_2: Boolean, konstue_3: Int?, block: () -> Unit): Boolean? {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        returns(true) implies (konstue_1 != null)
        returns(false) implies (!konstue_2)
        returnsNotNull() implies (this@case_3 != null && konstue_3 != null)
    }
    block()
    return konstue_1 != null
}
