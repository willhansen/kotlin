// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

konst postponedActions = ArrayList<() -> Unit>()

suspend fun suspendWithException(): String = suspendCoroutine { x ->
    postponedActions.add {
        x.resumeWithException(Exception("OK"))
    }
}

suspend fun catchException(): String {
    try {
        return suspendWithException()
    }
    catch(e: Exception) {
        return e.message!!
    }
}

fun run(c: suspend () -> String): String {
    var res: String = "FAIL 0"
    c.startCoroutine(handleResultContinuation {
        res = it
    })
    postponedActions[0]()
    return res
}

fun box(): String {
    return run {
        catchException()
    }
}
