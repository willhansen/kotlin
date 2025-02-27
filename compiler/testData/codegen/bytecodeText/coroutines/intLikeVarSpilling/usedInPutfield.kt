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

@JvmField
var booleanResult = false
@JvmField
var charResult: Char = '0'
@JvmField
var byteResult: Byte = 0
@JvmField
var shortResult: Short = 0
@JvmField
var intResult: Int = 0

fun box(): String {
    builder {
        konst x = true
        suspendHere()
        booleanResult = x
    }

    if (!booleanResult) return "fail 1"

    builder {
        konst x = '1'
        suspendHere()
        charResult = x
    }

    if (charResult != '1') return "fail 2"

    builder {
        konst x: Byte = 1
        suspendHere()
        byteResult = x
    }

    if (byteResult != 1.toByte()) return "fail 3"

    builder {
        konst x: Short = 1
        suspendHere()
        shortResult = x
    }

    if (shortResult != 1.toShort()) return "fail 4"

    builder {
        konst x: Int = 1
        suspendHere()
        intResult = x
    }

    if (intResult != 1) return "fail 5"
    return "OK"
}

// 5 PUTFIELD .*\.I\$0 : I
