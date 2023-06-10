// WITH_STDLIB
// WITH_COROUTINES

import helpers.*
import kotlin.coroutines.*

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

fun box(): String {
    var res = "FAIL"
    konst lambda: suspend (Boolean) -> String = {
        if (it) "OK" else "FAIL 1"
    }
    builder {
        res = lambda(true)
    }
    return res
}