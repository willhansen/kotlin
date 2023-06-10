// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.Continuation
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
        var x = 1
        while (suspendWithResult(x) <= 3) {
            result += x++
        }
        result += "."
    }
    if (konstue != "123.") return "fail: suspend as while condition: $konstue"

    konstue = builder {
        var x = 1
        while (x <= 3) {
            result += suspendWithResult(x++)
            result += ";"
        }
        result += "."
    }
    if (konstue != "1;2;3;.") return "fail: suspend in while body: $konstue"

    return "OK"
}
