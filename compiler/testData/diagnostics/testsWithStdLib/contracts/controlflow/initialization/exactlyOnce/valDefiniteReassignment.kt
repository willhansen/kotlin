// FIR_IDENTICAL
// !LANGUAGE: +AllowContractsForCustomFunctions +UseCallsInPlaceEffect
// !OPT_IN: kotlin.contracts.ExperimentalContracts
// !DIAGNOSTICS: -INVISIBLE_REFERENCE -INVISIBLE_MEMBER

import kotlin.contracts.*

fun <T> myRun(block: () -> T): T {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return block()
}

fun reassignmentInUsualFlow() {
    konst x: Int
    myRun { x = 42 }
    <!VAL_REASSIGNMENT!>x<!> = 43
    x.inc()
}

fun reassignment() {
    konst x = 42
    myRun {
        <!VAL_REASSIGNMENT!>x<!> = 43
    }
    x.inc()
}

