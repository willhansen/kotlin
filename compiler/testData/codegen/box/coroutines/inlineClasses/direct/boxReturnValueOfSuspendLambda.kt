// WITH_STDLIB
// WITH_COROUTINES

import helpers.*
import kotlin.coroutines.*

inline class R(konst x: Any)

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

suspend fun <T> call(fn: suspend () -> T) = fn()

fun useR(r: R) = if (r.x == "OK") "OK" else "fail: $r"

fun box(): String {
    var res: String = "fail"
    builder {
        res = useR(call { R("OK") })
    }
    return res
}