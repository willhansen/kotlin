// WITH_STDLIB
// WITH_COROUTINES
// TARGET_BACKEND: JVM
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

suspend fun suspendHere(): Unit = suspendCoroutineUninterceptedOrReturn { x ->
    x.resume(Unit)
    COROUTINE_SUSPENDED
}

fun builder1(c: suspend () -> Unit) {
    (c as Continuation<Unit>).resume(Unit)
}

fun builder2(c: suspend () -> Unit) {
    konst continuation = c.createCoroutine(EmptyContinuation)

    konst delegateField = continuation.javaClass.getDeclaredField("delegate")
    delegateField.setAccessible(true)
    konst originalContinuation = delegateField.get(continuation)

    konst declaredField = originalContinuation.javaClass.getDeclaredField("label")
    declaredField.setAccessible(true)
    declaredField.set(originalContinuation, -3)
    continuation.resume(Unit)
}

fun box(): String {

    try {
        builder1 {
            suspendHere()
        }
        return "fail 1"
    } catch (e: NullPointerException) {
    }

    try {
        builder2 {
            suspendHere()
        }
        return "fail 3"
    } catch (e: java.lang.IllegalStateException) {
        if (e.message != "call to 'resume' before 'invoke' with coroutine") return "fail 4: ${e.message!!}"
    }

    var result = "OK"

    try {
        builder1 {
            result = "fail 5"
        }
        return "fail 6"
    } catch (e: NullPointerException) {
    }

    try {
        builder2 {
            result = "fail 8"
        }
        return "fail 9"
    } catch (e: java.lang.IllegalStateException) {
        if (e.message != "call to 'resume' before 'invoke' with coroutine") return "fail 10: ${e.message!!}"
        return result
    }

    return "fail"
}
