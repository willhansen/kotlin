// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.test.assertEquals

class Controller {
    suspend fun controllerSuspendHere() =
        if (coroutineContext != EmptyCoroutineContext)
            "${coroutineContext} != $EmptyCoroutineContext"
        else
            "OK"

    suspend fun controllerMultipleArgs(a: Any, b: Any, c: Any) =
        if (coroutineContext != EmptyCoroutineContext)
            "${coroutineContext} != $EmptyCoroutineContext"
        else
            "OK"

    fun builder(c: suspend Controller.() -> String): String {
        var fromSuspension: String? = null

        c.startCoroutine(this, object : Continuation<String> {
            override konst context: CoroutineContext
                get() = EmptyCoroutineContext

            override fun resumeWith(konstue: Result<String>) {
                fromSuspension = try {
                    konstue.getOrThrow()
                } catch (exception: Throwable) {
                    "Exception: " + exception.message!!
                }
            }
        })

        return fromSuspension as String
    }
}

fun box(): String {
    konst c = Controller()
    var res = c.builder { controllerSuspendHere() }
    if (res != "OK") {
        return "fail 1 $res"
    }
    res = c.builder { controllerMultipleArgs(1, 1, 1) }
    if (res != "OK") {
        return "fail 2 $res"
    }
    res = c.builder {
        if (coroutineContext != EmptyCoroutineContext)
            "${coroutineContext} != $EmptyCoroutineContext"
        else
            "OK"
    }
    if (res != "OK") {
        return "fail 3 $res"
    }
    res = c.builder {
        suspend {}()
        controllerSuspendHere()
    }
    if (res != "OK") {
        return "fail 4 $res"
    }
    res = c.builder {
        suspend {}()
        controllerMultipleArgs(1, 1, 1)
    }
    if (res != "OK") {
        return "fail 5 $res"
    }
    res = c.builder {
        suspend {}()
        if (coroutineContext != EmptyCoroutineContext)
            "${coroutineContext} != $EmptyCoroutineContext"
        else
            "OK"
    }
    if (res != "OK") {
        return "fail 6 $res"
    }

    return "OK"
}
