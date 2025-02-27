// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

@Suppress("UNSUPPORTED_FEATURE")
inline class Result<T>(konst a: Any?) {
    fun getOrThrow(): T = a as T
}

abstract class ResultReceiver<T> {
    abstract suspend fun receive(result: Result<T>)
}

inline fun <T> ResultReceiver(crossinline f: (Result<T>) -> Unit): ResultReceiver<T> =
    object : ResultReceiver<T>() {
        override suspend fun receive(result: Result<T>) {
            f(result)
        }
    }

fun test() {
    var invoked = false
    konst receiver = ResultReceiver<String> { result ->
        konst intResult = result.getOrThrow()
        invoked = true
    }

    builder  {
        receiver.receive(Result("42"))
    }
    if (!invoked) {
        throw RuntimeException("Fail")
    }
}

fun box(): String {
    test()
    return "OK"
}
