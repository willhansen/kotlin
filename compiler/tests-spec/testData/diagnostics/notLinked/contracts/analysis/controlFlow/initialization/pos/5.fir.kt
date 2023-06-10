// !OPT_IN: kotlin.contracts.ExperimentalContracts


// FILE: contracts.kt

package contracts

import kotlin.contracts.*

// TESTCASE NUMBER: 1
fun case_1(x: Double = 1.0, block: () -> Unit): Double {
    <!WRONG_INVOCATION_KIND!>contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }<!>
    return x
}

// FILE: main.kt

import contracts.*

// TESTCASE NUMBER: 1
fun case_1() {
    konst konstue_1: Int
    contracts.case_1 { konstue_1 = 10 }
    konstue_1.inc()
}
