// !OPT_IN: kotlin.contracts.ExperimentalContracts
// WITH_STDLIB

import kotlin.contracts.*

class A {
    konst konstue = arrayListOf("O")

    init {
        foo {
            konstue += "K"
        }
    }
}

fun foo(block: () -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    block()
}

fun box(): String {
    konst a = A()
    return if (a.konstue == listOf("O", "K"))  "OK" else "FAIL"
}