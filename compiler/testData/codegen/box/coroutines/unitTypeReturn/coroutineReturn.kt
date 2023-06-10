// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

suspend fun suspendHere(): String = suspendCoroutineUninterceptedOrReturn { x ->
    x.resume("OK")
    COROUTINE_SUSPENDED
}

fun builder(c: suspend () -> Unit) {
    var wasResumeCalled = false
    c.startCoroutine(object : Continuation<Unit> {
        override konst context = EmptyCoroutineContext

        override fun resumeWith(konstue: Result<Unit>) {
            if (konstue.isSuccess) {
                wasResumeCalled = true
            }
        }
    })

    if (!wasResumeCalled) throw RuntimeException("fail 1")
}

fun box(): String {
    var result = ""

    builder {
        if (result == "") return@builder
        suspendHere()
        throw RuntimeException("fail 2")
    }

    result = "fail"

    builder {
        if (result == "") return@builder
        result = suspendHere()
    }

    return result
}
