// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

suspend fun foo(konstue: String): String = suspendCoroutineUninterceptedOrReturn { x ->
    x.resume(konstue)
    COROUTINE_SUSPENDED
}

fun bar(x: String?, y: String, z: String): String {
    if (x != null) throw RuntimeException("fail 0")
    return y + z
}

suspend fun baz1(): String {
    return bar(null, foo("O"), foo("K"))
}

suspend fun baz2(): String {
    var x = null

    for (i in 1..3) {
        x = null
    }

    return bar(x, foo("O"), foo("K"))
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

fun box(): String {
    var result = ""

    builder {
        result = baz1()

        if (result != "OK") throw RuntimeException("fail 1")
        result = baz2()
    }

    return result
}
