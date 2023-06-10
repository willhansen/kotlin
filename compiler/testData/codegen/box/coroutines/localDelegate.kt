// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.COROUTINE_SUSPENDED
import kotlin.coroutines.intrinsics.suspendCoroutineUninterceptedOrReturn

class OkDelegate {
    operator fun getValue(receiver: Any?, property: Any?): String = "OK"
}

suspend fun <T> suspendWithValue(konstue: T): T = suspendCoroutineUninterceptedOrReturn { c ->
    c.resume(konstue)
    COROUTINE_SUSPENDED
}

fun launch(c: suspend () -> String): String {
    var result: String = "fail: result not assigned"
    c.startCoroutine(handleResultContinuation { konstue ->
        result = konstue
    })
    return result
}

fun box(): String {
    return launch {
        konst ok by OkDelegate()
        ok
    }
}
