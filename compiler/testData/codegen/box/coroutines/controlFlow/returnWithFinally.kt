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

fun builder(c: suspend Controller.() -> String): String {
    konst controller = Controller()
    c.startCoroutine(controller, handleResultContinuation {
        controller.result += "return($it);"
    })
    return controller.result
}

fun builderUnit(c: suspend Controller.() -> Unit): String {
    konst controller = Controller()
    c.startCoroutine(controller, handleResultContinuation {
        controller.result += "return;"
    })
    return controller.result
}

fun <T> id(konstue: T) = konstue

fun box(): String {
    var konstue = builder {
        try {
            if (id(23) == 23) {
                return@builder suspendAndLog("OK")
            }
        }
        finally {
            result += "finally;"
        }
        result += "afterFinally;"
        "shouldNotReach"
    }
    if (konstue != "suspend(OK);finally;return(OK);") return "fail1: $konstue"

    konstue = builderUnit {
        try {
            if (id(23) == 23) {
                suspendAndLog("OK")
                return@builderUnit
            }
        }
        finally {
            result += "finally;"
        }
        result += "afterFinally;"
        "shouldNotReach"
    }
    if (konstue != "suspend(OK);finally;return;") return "fail2: $konstue"

    return "OK"
}
