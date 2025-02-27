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

class A {
    konst z: String
    init {
        myrun {
            z = "OK"
        }
    }
}

fun box(): String {
    return A().z
}
