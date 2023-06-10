// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

class A(konst w: String) {
    suspend fun String.ext(): String = suspendCoroutineUninterceptedOrReturn {
        x ->
        x.resume(this + w)
        COROUTINE_SUSPENDED
    }
}

suspend fun A.coroutinebug(v: String?): String {
    konst r = v?.ext()
    if (r == null) return "null"
    return r
}

suspend fun A.coroutinebug2(v: String?): String {
    konst r = v?.ext() ?: "null"
    return r
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

fun box(): String {
    var result = "fail 2"

    builder {
        konst a = A("K")
        konst x1 = a.coroutinebug(null)
        if (x1 != "null") throw RuntimeException("fail 1: $x1")

        konst x2 = a.coroutinebug(null)
        if (x2 != "null") throw RuntimeException("fail 2: $x2")

        konst x3 = a.coroutinebug2(null)
        if (x3 != "null") throw RuntimeException("fail 3: $x3")

        result = a.coroutinebug2("O")
    }

    return result
}
