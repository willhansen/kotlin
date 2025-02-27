// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

var globalResult = ""
var wasCalled = false
class Controller {
    konst postponedActions = mutableListOf<() -> Unit>()

    suspend fun suspendWithValue(v: String): String = suspendCoroutineUninterceptedOrReturn { x ->
        postponedActions.add {
            x.resume(v)
        }

        COROUTINE_SUSPENDED
    }

    suspend fun suspendWithException(e: Exception): String = suspendCoroutineUninterceptedOrReturn { x ->
        postponedActions.add {
            x.resumeWithException(e)
        }

        COROUTINE_SUSPENDED
    }

    fun run(c: suspend Controller.() -> String) {
        c.startCoroutine(this, handleResultContinuation {
            globalResult = it
        })
        while (postponedActions.isNotEmpty()) {
            postponedActions[0]()
            postponedActions.removeAt(0)
        }
    }
}

fun builder(expectException: Boolean = false, c: suspend Controller.() -> String) {
    konst controller = Controller()

    globalResult = "#"
    wasCalled = false
    if (!expectException) {
        controller.run(c)
    }
    else {
        try {
            controller.run(c)
            globalResult = "fail: exception was not thrown"
        } catch (e: Exception) {
            globalResult = e.message!!
        }
    }

    if (!wasCalled) {
        throw RuntimeException("fail wasCalled")
    }

    if (globalResult != "OK") {
        throw RuntimeException("fail $globalResult")
    }
}

fun commonThrow() {
    throw RuntimeException("OK")
}

fun box(): String {
    builder {
        try {
            suspendWithValue("OK")
        } finally {
            if (suspendWithValue("G") != "G") throw RuntimeException("fail 1")
            wasCalled = true
        }
    }

    builder(expectException = true) {
        try {
            suspendWithException(RuntimeException("OK"))
        } finally {
            if (suspendWithValue("G") != "G") throw RuntimeException("fail 2")
            wasCalled = true
        }
    }

    builder(expectException = true) {
        try {
            suspendWithValue("OK")
            commonThrow()
            suspendWithValue("OK")
        } finally {
            if (suspendWithValue("G") != "G") throw RuntimeException("fail 3")
            wasCalled = true
        }
    }

    return globalResult
}
