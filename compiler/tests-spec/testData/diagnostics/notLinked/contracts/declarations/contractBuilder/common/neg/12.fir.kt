// !OPT_IN: kotlin.contracts.ExperimentalContracts

// FILE: builder.kt

package builder

import kotlin.contracts.*

// TESTCASE NUMBER: 1, 2, 3
inline fun ContractBuilder.callsInPlaceEffectBuilder(block: () -> Unit) =
    callsInPlace(<!USAGE_IS_NOT_INLINABLE!>block<!>, InvocationKind.EXACTLY_ONCE)

fun ContractBuilder.returnsEffectBuilder(konstue_1: Int?) =
    returns(true) implies (konstue_1 != null)

// FILE: main.kt

import builder.*
import kotlin.contracts.*

// TESTCASE NUMBER: 1
inline fun case_1(block: () -> Unit) {
    contract(builder = { callsInPlaceEffectBuilder(block) })
    return block()
}

// TESTCASE NUMBER: 2
inline fun case_2(block: () -> Unit) {
    contract { <!ERROR_IN_CONTRACT_DESCRIPTION!>callsInPlaceEffectBuilder(block)<!> }
    return block()
}

// TESTCASE NUMBER: 3
inline fun case_3(konstue_1: Int?, block: () -> Unit) {
    contract({ returnsEffectBuilder(konstue_1); callsInPlaceEffectBuilder(block) })
    return block()
}
