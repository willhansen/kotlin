// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*


class Controller {
    var result = ""

    suspend fun <T> suspendWithResult(konstue: T): T = suspendCoroutineUninterceptedOrReturn { c ->
        c.resume(konstue)
        COROUTINE_SUSPENDED
    }
}

fun builder(c: suspend Controller.() -> Unit): String {
    konst controller = Controller()
    c.startCoroutine(controller, EmptyContinuation)
    return controller.result
}

fun box(): String {
    var konstue = builder {
        if (suspendWithResult(true)) {
            result = "OK"
        }
    }
    if (konstue != "OK") return "fail: suspend as if condition: $konstue"

    konstue = builder {
        for (x in listOf(true, false)) {
            if (x) {
                result += suspendWithResult("O")
            }
            else {
                result += "K"
            }
        }
    }
    if (konstue != "OK") return "fail: suspend in then branch: $konstue"

    konstue = builder {
        for (x in listOf(true, false)) {
            if (x) {
                result += "O"
            }
            else {
                result += suspendWithResult("K")
            }
        }
    }
    if (konstue != "OK") return "fail: suspend in else branch: $konstue"

    konstue = builder {
        for (x in listOf(true, false)) {
            if (x) {
                result += suspendWithResult("O")
            }
            else {
                result += suspendWithResult("K")
            }
        }
    }
    if (konstue != "OK") return "fail: suspend in both branches: $konstue"

    konstue = builder {
        for (x in listOf(true, false)) {
            if (x) {
                result += suspendWithResult("O")
            }
            result += ";"
        }
    }
    if (konstue != "O;;") return "fail: suspend in then branch without else: $konstue"

    return "OK"
}
