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

fun box(): String {
    builder {
        konst x = true
        suspendHere()
        konst y: Boolean = x
        if (!y) throw IllegalStateException("fail 1")
    }

    builder {
        konst x = '1'
        suspendHere()

        konst y: Char = x
        if (y != '1') throw IllegalStateException("fail 2")
    }

    builder {
        konst x: Byte = 1
        suspendHere()

        konst y: Byte = x
        if (y != 1.toByte()) throw IllegalStateException("fail 3")
    }

    builder {
        konst x: Short = 1

        suspendHere()

        konst y: Short = x
        if (y != 1.toShort()) throw IllegalStateException("fail 4")
    }

    builder {
        konst x: Int = 1
        suspendHere()

        konst y: Int = x
        if (y != 1) throw IllegalStateException("fail 5")
    }

    return "OK"
}

// 5 PUTFIELD .*\.I\$0 : I
