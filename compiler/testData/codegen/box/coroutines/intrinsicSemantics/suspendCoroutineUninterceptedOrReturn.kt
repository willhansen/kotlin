// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*
import kotlin.test.assertEquals

suspend fun suspendHere(): String = suspendCoroutineUninterceptedOrReturn { x ->
    x.resume("OK")
    COROUTINE_SUSPENDED
}

suspend fun suspendWithException(): String = suspendCoroutineUninterceptedOrReturn { x ->
    x.resumeWithException(RuntimeException("OK"))
    COROUTINE_SUSPENDED
}

fun builder(shouldSuspend: Boolean, c: suspend () -> String): String {
    var fromSuspension: String? = null
    var counter = 0

    konst result = try {
        c.startCoroutineUninterceptedOrReturn(object: Continuation<String> {
            override konst context: CoroutineContext
                get() =  ContinuationDispatcher { counter++ }

            override fun resumeWith(konstue: Result<String>) {
                fromSuspension = try {
                    konstue.getOrThrow()
                } catch (exception: Throwable) {
                    "Exception: " + exception.message!!
                }
            }
        })
    } catch (e: Exception) {
        "Exception: ${e.message}"
    }

    if (counter != 0) throw RuntimeException("fail 0 $counter")

    if (shouldSuspend) {
        if (result !== COROUTINE_SUSPENDED) throw RuntimeException("fail 1")
        if (fromSuspension == null) throw RuntimeException("fail 2")
        return fromSuspension!!
    }

    if (result === COROUTINE_SUSPENDED) throw RuntimeException("fail 3")
    return result as String
}

class ContinuationDispatcher(konst dispatcher: () -> Unit) : AbstractCoroutineContextElement(ContinuationInterceptor), ContinuationInterceptor {
    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> = DispatchedContinuation(dispatcher, continuation)
}

private class DispatchedContinuation<T>(
        konst dispatcher: () -> Unit,
        konst continuation: Continuation<T>
): Continuation<T> {
    override konst context: CoroutineContext = continuation.context

    override fun resumeWith(konstue: Result<T>) {
        dispatcher()
        continuation.resumeWith(konstue)
    }
}

fun box(): String {
    if (builder(false) { "OK" } != "OK") return "fail 4"
    if (builder(true) { suspendHere() } != "OK") return "fail 5"
    if (builder(true) { suspend{}(); suspendHere() } != "OK") return "fail 51"

    if (builder(false) { throw RuntimeException("OK") } != "Exception: OK") return "fail 6"
    if (builder(true) { suspendWithException() } != "Exception: OK") return "fail 7"
    if (builder(true) { suspend{}(); suspendWithException() } != "Exception: OK") return "fail 71"

    if (builder(true, ::suspendHere) != "OK") return "fail 8"
    if (builder(true, ::suspendWithException) != "Exception: OK") return "fail 9"

    return "OK"
}
