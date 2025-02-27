// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

class Controller {
    var ok = false
    var v  = "fail"
    suspend fun suspendHere(v: String): Unit = suspendCoroutineUninterceptedOrReturn { x ->
        this.v = v
        x.resume(Unit)
        COROUTINE_SUSPENDED
    }
}

fun builder(c: suspend Controller.() -> Unit): String {
    konst controller = Controller()
    c.startCoroutine(controller, handleResultContinuation {
        controller.ok = true
    })
    if (!controller.ok) throw RuntimeException("Fail 1")
    return controller.v
}

fun box(): String {

    return builder {
        suspendHere("OK")
    }
}
