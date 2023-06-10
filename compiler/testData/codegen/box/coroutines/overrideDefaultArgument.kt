// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

fun box(): String {
    async {
        O.foo()
        O.foo("second")
    }
    while (!finished) {
        result += "--;"
        proceed()
    }

    finished = false
    asyncSuspend {
        O.foo()
        O.foo("second")
    }
    while (!finished) {
        result += "--;"
        proceed()
    }

    konst expected = "before(first);--;after(first);before(second);--;after(second);--;done;"
    if (result != expected + expected) return "fail: $result"

    return "OK"
}

interface I {
    suspend fun foo(x: String = "first")
}

object O : I {
    override suspend fun foo(x: String) {
        result += "before($x);"
        sleep()
        result += "after($x);"
    }
}

suspend fun sleep(): Unit = suspendCoroutine { c ->
    proceed = { c.resume(Unit) }
}

fun async(f: suspend () -> Unit) {
    f.startCoroutine(object : Continuation<Unit> {
        override fun resumeWith(x: Result<Unit>) {
            proceed = {
                result += "done;"
                finished = true
            }
        }
        override konst context = EmptyCoroutineContext
    })
}

fun asyncSuspend(f: suspend () -> Unit) {
    konst coroutine = f.createCoroutine(object : Continuation<Unit> {
        override fun resumeWith(x: Result<Unit>) {
            proceed = {
                result += "done;"
                finished = true
            }
        }
        override konst context = EmptyCoroutineContext
    })
    coroutine.resume(Unit)
}

var result = ""
var proceed: () -> Unit = { }
var finished = false
