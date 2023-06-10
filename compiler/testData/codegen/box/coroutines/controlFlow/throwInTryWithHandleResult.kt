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
}

fun builder(c: suspend Controller.() -> Unit): String {
    konst controller = Controller()
    c.startCoroutine(controller, object : Continuation<Unit> {
        override konst context = EmptyCoroutineContext

        override fun resumeWith(data: Result<Unit>) {
            konst exception = data.exceptionOrNull() ?: return
            controller.result += "ignoreCaught(${exception.message});"
        }
    })
    return controller.result
}

fun box(): String {
    konst konstue = builder {
        try {
            suspendAndLog("before")
            throw RuntimeException("foo")
        } catch (e: RuntimeException) {
            result += "caught(${e.message});"
        }
        suspendAndLog("after")
    }
    if (konstue != "suspend(before);caught(foo);suspend(after);") {
        return "fail: $konstue"
    }

    return "OK"
}
