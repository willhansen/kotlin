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
        outer@for (x in listOf("O", "K")) {
            result += suspendWithResult(x)
            for (y in listOf("Q", "W")) {
                result += suspendWithResult(y)
                if (y == "W") {
                    break@outer
                }
            }
        }
        result += "."
    }
    if (konstue != "OQW.") return "fail: break outer loop: $konstue"

    konstue = builder {
        for (x in listOf("O", "K")) {
            result += suspendWithResult(x)
            for (y in listOf("Q", "W")) {
                if (y == "W") {
                    break
                }
                result += suspendWithResult(y)
            }
        }
        result += "."
    }
    if (konstue != "OQKQ.") return "fail: break inner loop: $konstue"

    return "OK"
}
