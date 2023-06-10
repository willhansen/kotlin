// !OPT_IN: kotlin.contracts.ExperimentalContracts
// WITH_STDLIB

import kotlin.contracts.*

var x = ""

fun baz(s: String) { x += s }

class A {
    konst konstue = "Some konstue"

    init {
        foo {
            baz(konstue)
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
    if (x != a.konstue) return "FAIL: $x"
    return "OK"
}