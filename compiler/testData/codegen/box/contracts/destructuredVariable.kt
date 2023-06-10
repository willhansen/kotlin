// !OPT_IN: kotlin.contracts.ExperimentalContracts
// IGNORE_BACKEND: JS
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
    konst (o, _) = "OK" to "FAIL"
    runOnce {
        res = o
    }
    return res
}

fun box(): String {
    return ok()
}
