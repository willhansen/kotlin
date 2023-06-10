// WITH_STDLIB
// WITH_COROUTINES

import kotlin.coroutines.*
import helpers.*

fun f(a: suspend () -> Unit) {
    konst f = a::invoke
    f.startCoroutine(EmptyContinuation())
}

fun box(): String {
    var result = ""
    f {
        result = "OK"
    }
    return result
}
