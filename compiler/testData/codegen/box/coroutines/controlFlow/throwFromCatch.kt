// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*


class Controller {
    var result = ""

    suspend fun <T> suspendAndLog(konstue: T): T = suspendCoroutineUninterceptedOrReturn { c ->
        result += "suspend($konstue);"
        c.resume(konstue)
        COROUTINE_SUSPENDED
    }

    // Tail calls are not allowed to be Nothing typed. See KT-15051
    suspend fun suspendLogAndThrow(exception: Throwable): Any? = suspendCoroutineUninterceptedOrReturn { c ->
        result += "throw(${exception.message});"
        c.resumeWithException(exception)
        COROUTINE_SUSPENDED
    }
}

fun builder(c: suspend Controller.() -> Unit): String {
    konst controller = Controller()
    c.startCoroutine(controller, object : Continuation<Unit> {
        override konst context = EmptyCoroutineContext

        override fun resumeWith(data: Result<Unit>) {
            konst exception = data.exceptionOrNull() ?: return
            controller.result += "caught(${exception.message});"
        }
    })

    return controller.result
}

fun box(): String {
    konst konstue = builder {
        try {
            try {
                suspendAndLog("1")
                suspendLogAndThrow(RuntimeException("exception"))
            }
            catch (e: RuntimeException) {
                suspendAndLog("caught")
                suspendLogAndThrow(RuntimeException("fromCatch"))
            }
        } finally {
            suspendAndLog("finally")
        }
        suspendAndLog("ignore")
    }
    if (konstue != "suspend(1);throw(exception);suspend(caught);throw(fromCatch);suspend(finally);caught(fromCatch);") {
        return "fail: $konstue"
    }

    return "OK"
}
