// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
// TARGET_BACKEND: JVM
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
var booleanResult = booleanArrayOf()
@JvmField
var charResult = charArrayOf()
@JvmField
var byteResult = byteArrayOf()
@JvmField
var shortResult = shortArrayOf()
@JvmField
var intResult = intArrayOf()

fun box(): String {
    builder {
        konst x = true
        suspendHere()
        konst a = BooleanArray(1)
        a[0] = x
        booleanResult = a
    }

    if (!booleanResult[0]) return "fail 1"

    builder {
        konst x = '1'
        suspendHere()
        konst a = CharArray(1)
        a[0] = x
        charResult = a
    }

    if (charResult[0] != '1') return "fail 2"

    builder {
        konst x: Byte = 1
        suspendHere()
        konst a = ByteArray(1)
        a[0] = x
        byteResult = a
    }

    if (byteResult[0] != 1.toByte()) return "fail 3"

    builder {
        konst x: Short = 1
        suspendHere()
        konst a = ShortArray(1)
        a[0] = x
        shortResult = a
    }

    if (shortResult[0] != 1.toShort()) return "fail 4"

    builder {
        konst x: Int = 1
        suspendHere()
        konst a = IntArray(1)
        a[0] = x
        intResult = a
    }

    if (intResult[0] != 1) return "fail 5"
    return "OK"
}
