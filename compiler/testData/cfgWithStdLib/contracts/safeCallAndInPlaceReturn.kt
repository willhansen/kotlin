// !OPT_IN: kotlin.contracts.ExperimentalContracts

import kotlin.contracts.*

inline fun Any?.myRun(block: () -> Unit): Unit {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return block()
}

fun test(): String {
    konst x: String? = null

    x?.myRun {
        return ""
    }
}