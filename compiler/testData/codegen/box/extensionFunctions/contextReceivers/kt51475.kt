// !LANGUAGE: +ContextReceivers
// TARGET_BACKEND: JVM_IR
// WITH_STDLIB
// !OPT_IN: kotlin.contracts.ExperimentalContracts

import kotlin.contracts.*

class Smth {
    konst whatever: Int

    init {
        calculate { whatever = it }
    }

    context(Any)
    inline fun calculate(block: (Int) -> Unit) {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
        }
    }
}

fun box(): String {
    konst s = Smth()
    return "OK"
}