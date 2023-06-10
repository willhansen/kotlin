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
    konst konstue = builder {
        var r = ""

        for (x in listOf("O", "$", "K")) {
            if (x == "$") continue
            run {
                r += suspendWithResult(x)
            }
        }
        run {
            r += "."
        }
        result = r
    }

    if (konstue != "OK.") return "fail: suspend in for body: $konstue"

    return "OK"
}
