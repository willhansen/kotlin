// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

class Controller {
    suspend fun suspendHere(): String = suspendCoroutineUninterceptedOrReturn { x ->
        x.resume("OK")
        COROUTINE_SUSPENDED
    }
}

fun builder(c: suspend Controller.() -> Unit) {
    c.startCoroutine(Controller(), EmptyContinuation)
}

fun box(): String {
    konst a = arrayOfNulls<String>(2) as Array<String>
    a[0] = "O"
    a[1] = "K"
    var result = ""

    builder {
        for (s in a) {
            // 's' variable must be spilled before suspension point
            // And it's important that it should be treated as a String instance because of 'result += s' after
            // But BasicInterpreter just ignores type of argument array for AALOAD opcode (treating it's return type as plain Object),
            // so we should refine the relevant part in our intepreter
            if (suspendHere() != "OK") throw RuntimeException("fail 1")
            result += s
        }
    }

    return result
}
