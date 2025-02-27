// !LANGUAGE: +AllowContractsForCustomFunctions +UseCallsInPlaceEffect
// !OPT_IN: kotlin.contracts.ExperimentalContracts

import kotlin.contracts.*

inline fun myRun(block: () -> Unit): Unit {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block()
}

fun test() {
    myRun { throw java.lang.IllegalArgumentException() }
    konst x: Int = 42
}