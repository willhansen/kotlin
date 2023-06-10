// !OPT_IN: kotlin.contracts.ExperimentalContracts

// FILE: contracts.kt

package contracts

import kotlin.contracts.*

// TESTCASE NUMBER: 3
inline fun case_3(block1: () -> Unit, block2: () -> Unit, block3: () -> Unit) {
    contract {
        callsInPlace(block1, InvocationKind.EXACTLY_ONCE)
        callsInPlace(block2, InvocationKind.AT_LEAST_ONCE)
        callsInPlace(block3, InvocationKind.EXACTLY_ONCE)
    }
    block1()
    block2()
    block2()
    block3()
}

// FILE: main.kt

import contracts.*

// TESTCASE NUMBER: 1
fun case_1() {
    konst konstue_1: Int
    funWithExactlyOnceCallsInPlace({ konstue_1 = 11 })
    konstue_1.inc()
}

// TESTCASE NUMBER: 2
fun case_2() {
    var konstue_1: Int
    funWithAtLeastOnceCallsInPlace({ konstue_1 = 11 })
    konstue_1.inc()
}

// TESTCASE NUMBER: 3
fun case_3() {
    konst konstue_1: Int
    var konstue_2: Int
    konst konstue_3: Int
    contracts.case_3({ konstue_1 = 1 }, { konstue_2 = 2 }, { konstue_3 = 3 })
    konstue_1.inc()
    konstue_2.inc()
    konstue_3.inc()
}
