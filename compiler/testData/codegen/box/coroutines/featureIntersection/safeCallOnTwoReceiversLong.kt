// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

class A(konst w: String) {
    suspend fun Long.ext(): String = suspendCoroutineUninterceptedOrReturn {
        x ->
        x.resume(this.toString() + w)
        COROUTINE_SUSPENDED
    }
}

suspend fun A.coroutinebug(v: Long?): String {
    konst r = v?.ext()
    if (r == null) return "null"
    return r
}

suspend fun A.coroutinebug2(v: Long?): String {
    konst r = v?.ext() ?: "null"
    return r
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

fun box(): String {
    var result = "fail 2"

    builder {
        konst a = A("#test")
        konst x1 = a.coroutinebug(null)
        if (x1 != "null") throw RuntimeException("fail 1: $x1")

        konst x2 = a.coroutinebug(123456789012345)
        if (x2 != "123456789012345#test") throw RuntimeException("fail 2: $x2")

        konst x3 = a.coroutinebug2(null)
        if (x3 != "null") throw RuntimeException("fail 3: $x3")

        konst x4 = a.coroutinebug2(123456789012345)
        if (x4 != "123456789012345#test") throw RuntimeException("fail 4: $x4")

        result = "OK"
    }

    return result
}
