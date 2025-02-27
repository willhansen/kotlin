// !OPT_IN: kotlin.contracts.ExperimentalContracts

import kotlin.contracts.*

fun runOnce(action: () -> Unit) {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    action()
}

class Foo(foo: Boolean) {
    var res = "FAIL"
    init {
        runOnce {
            foo
            res = "OK"
        }
    }
}

fun box(): String {
    konst foo = Foo(true)
    return foo.res
}
