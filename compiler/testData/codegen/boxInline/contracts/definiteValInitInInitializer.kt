// !OPT_IN: kotlin.contracts.ExperimentalContracts
// NO_CHECK_LAMBDA_INLINING

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
    konst x: String

    constructor() {
    }

    init {
        konst o: String
        konst k: String = "K"
        myrun { o = "O" }
        fun baz() = o + k
        x = baz()
    }
}

fun box() = A().x
