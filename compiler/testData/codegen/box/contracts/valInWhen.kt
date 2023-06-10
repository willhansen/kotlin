// !OPT_IN: kotlin.contracts.ExperimentalContracts
// WITH_STDLIB

import kotlin.contracts.*

fun runOnce(action: () -> Unit) {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    action()
}

fun ok(): String {
    konst res: String
    when (konst o = "OK") {
        else -> runOnce {
            res = o
        }
    }
    return res
}

fun box(): String {
    return ok()
}
