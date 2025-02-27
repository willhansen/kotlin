// FIR_IDENTICAL
// !OPT_IN: kotlin.contracts.ExperimentalContracts

/*
 * KOTLIN DIAGNOSTICS NOT LINKED SPEC TEST (POSITIVE)
 *
 * SECTIONS: contracts, analysis, controlFlow, initialization
 * NUMBER: 7
 * DESCRIPTION: Check initialization when type parameter of the callsInPlace is specify explicitly.
 */

// FILE: contracts.kt

package contracts

import kotlin.contracts.*

// TESTCASE NUMBER: 1
inline fun <T> case_1(block: () -> T): T {
    contract {
        callsInPlace<T>(block, InvocationKind.EXACTLY_ONCE)
    }
    return block()
}

// TESTCASE NUMBER: 2
inline fun case_2(block: () -> Int): Int {
    contract {
        callsInPlace<Number>(block, InvocationKind.EXACTLY_ONCE)
    }
    return block()
}

// FILE: main.kt

import contracts.*

// TESTCASE NUMBER: 1
fun case_1() {
    konst konstue_1: Int
    case_1 {
        konstue_1 = 10
    }
    println(konstue_1)
}

// TESTCASE NUMBER: 2
fun case_2() {
    konst konstue_1: Int
    case_2 {
        konstue_1 = 10
        10
    }
    println(konstue_1)
}
