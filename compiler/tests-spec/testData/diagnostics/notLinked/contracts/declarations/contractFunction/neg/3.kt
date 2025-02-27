// FIR_IDENTICAL
// !OPT_IN: kotlin.contracts.ExperimentalContracts

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (NEGATIVE)
 *
 * SECTIONS: contracts, declarations, contractFunction
 * NUMBER: 3
 * DESCRIPTION: Check that fun with contract and CallsInPlace effect is an inline function.
 * UNEXPECTED BEHAVIOUR
 * ISSUES: KT-26126
 */

import kotlin.contracts.*

// TESTCASE NUMBER: 1
fun funWithContractExactlyOnce(block: () -> Unit) { // report about not-inline function is expected
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return block()
}

// TESTCASE NUMBER: 1
fun case_1() {
    konst konstue_1: Int
    funWithContractExactlyOnce { konstue_1 = 10 } // back-end exception
    konstue_1.inc()
}
