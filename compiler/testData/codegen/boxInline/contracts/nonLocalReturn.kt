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

fun box(): String {
    konst x: Long
    myrun {
        x = 42L
        if (x == 42L) return "OK"
    }
    return "fail"
}
