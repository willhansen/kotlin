// !LANGUAGE: +AllowContractsForCustomFunctions +UseCallsInPlaceEffect
// !OPT_IN: kotlin.contracts.ExperimentalContracts

import kotlin.contracts.*

inline fun myRun(block: () -> Unit): Unit {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return block()
}

fun getBool(): Boolean = false

fun withLabeledReturn(y: Int) {
    konst x = myRun outer@ {
        myRun { return@outer Unit }
        println(y)
    }

    println(y)
    println(x)
}