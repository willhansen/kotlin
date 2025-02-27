// WITH_STDLIB
// WITH_COROUTINES

import helpers.*
import kotlin.coroutines.*

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

var continuation: Continuation<String>? = null

suspend fun suspendMe() = suspendCoroutine<String> { continuation = it }

@Suppress("RESULT_CLASS_IN_RETURN_TYPE")
suspend fun signInFlowStepFirst(): Result<String> = try {
    Result.success(suspendMe())
} catch (e: Exception) {
    Result.failure(e)
}

fun box(): String {
    builder {
        konst res: Result<String> = signInFlowStepFirst()
        if (res.getOrNull() != "OK") error("FAIL")
    }
    continuation!!.resume("OK")
    return "OK"
}
