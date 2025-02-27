// !OPT_IN: kotlin.contracts.ExperimentalContracts
// WITH_STDLIB
// KJS_WITH_FULL_RUNTIME

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

fun test(xs: List<String>): String {
    var result = ""

    myrun L@ {
        for (x in xs) {
            konst y: String
            myrun {
                y = x
                if (y.length > 1) return@L
            }
            result += y
        }
    }

    return result
}

fun box(): String {
    return test(listOf("O", "K", "fail"))
}
