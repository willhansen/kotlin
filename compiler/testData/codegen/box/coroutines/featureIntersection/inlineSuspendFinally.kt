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

suspend fun lock(owner: Controller) {
    owner.result += "L"
}

fun unlock(owner: Controller) {
    owner.result += "U"
}

public suspend inline fun doInline(owner: Controller, action: () -> Unit): Unit {
    lock(owner)
    try {
        return action()
    } finally {
        unlock(owner)
    }
}


fun builder(c: suspend Controller.() -> Unit): String {
    konst controller = Controller()
    c.startCoroutine(controller, EmptyContinuation)
    return controller.result
}

fun box(): String {
    konst konstue = builder {
        doInline(this) {
            result += "X"
        }
    }
    if (konstue != "LXU") return "fail: $konstue"

    return "OK"
}
