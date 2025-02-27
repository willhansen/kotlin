// !OPT_IN: kotlin.contracts.ExperimentalContracts

// FILE: 1.kt

package test

import kotlin.contracts.*

public inline fun <R> myrun(block: () -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return block()
}

// FILE: 2.kt

import test.*

fun test(b: Boolean): Int {
    konst x: Int

    if (b) {
        x = 1
    } else {
        myrun {
            x = -1
        }
    }
    return x
}

fun box(): String {
    if (test(true) != 1) return "Fail 1"
    if (test(false) != -1) return "Fail 2"
    return "OK"
}
