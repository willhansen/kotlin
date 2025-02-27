// WITH_COROUTINES
// TREAT_AS_ONE_FILE

import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

class Controller {
    suspend fun suspendHere(): Unit = suspendCoroutineUninterceptedOrReturn { x ->
        x.resume(Unit)
        COROUTINE_SUSPENDED
    }
}

fun builder(c: suspend Controller.() -> Unit) {
    c.startCoroutine(Controller(), EmptyContinuation)
}

private var booleanResult = false
fun setBooleanRes(x: Boolean, ignored: Unit) {
    booleanResult = x
}

fun box(): String {
    builder {
        // 'true' konstue is spilled into variable and saved to field before suspension point
        // It's important that there is no type info about this variable in local var table,
        // so we should infer that ICONST_1 is a boolean konstue from it's usage
        setBooleanRes(true, suspendHere())
    }

    if (!booleanResult) return "fail 1"

    return "OK"
}

// 1 PUTFIELD .*\.I\$0 : I
