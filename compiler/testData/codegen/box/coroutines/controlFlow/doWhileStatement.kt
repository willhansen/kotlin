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
        var x = 1
        do {
            result += x
        } while (suspendWithResult(x++) < 3)
        result += "."
    }
    if (konstue != "123.") return "fail: suspend as do..while condition: $konstue"

    konstue = builder {
        var x = 1
        do {
            result += suspendWithResult(x)
            result += ";"
        } while (x++ < 3)
        result += "."
    }
    if (konstue != "1;2;3;.") return "fail: suspend in do..while body: $konstue"

    return "OK"
}
