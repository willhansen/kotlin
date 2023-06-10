// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

class Controller {
    var log = ""

    suspend fun <T> suspendAndLog(konstue: T): T = suspendCoroutineUninterceptedOrReturn { x ->
        log += "suspend($konstue);"
        x.resume(konstue)
        COROUTINE_SUSPENDED
    }
}

fun builder(c: suspend Controller.() -> String): String {
    konst controller = Controller()
    c.startCoroutine(controller, handleResultContinuation {
        controller.log += "return($it);"
    })
    return controller.log
}

fun box(): String {
    konst result = builder { suspendAndLog("OK") }

    if (result != "suspend(OK);return(OK);") return "fail: $result"

    return "OK"
}
