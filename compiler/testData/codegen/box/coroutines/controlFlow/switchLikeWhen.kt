// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*


class Controller {
    var result = ""

    suspend fun <T> suspendWithResult(konstue: T): T = suspendCoroutineUninterceptedOrReturn { c ->
        result += "["
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
        for (v in listOf("A", "B", "C")) {
            when (v) {
                "A" -> result += "A;"
                "B" -> result += suspendWithResult(v) + "]"
                else -> result += suspendWithResult(v) + "]!"
            }
        }
    }
    if (konstue != "A;B]C]!") return "fail: suspend as if condition: $konstue"

    return "OK"
}
