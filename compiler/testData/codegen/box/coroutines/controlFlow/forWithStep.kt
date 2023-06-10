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
        for (x: Long in 20L..30L step 5L) {
            listOf("#").forEach {
                result += it + suspendWithResult(x).toString()
            }
        }
        result += "."
    }
    if (konstue != "#20#25#30.") return "fail: suspend in for body: $konstue"

    return "OK"
}
