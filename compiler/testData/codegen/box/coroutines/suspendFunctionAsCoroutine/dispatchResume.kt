// WITH_STDLIB
// WITH_COROUTINES
// FULL_JDK
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

class Controller {
    var log = ""
    var resumeIndex = 0

    var callback: (() -> Unit)? = null

    suspend fun <T> suspendWithValue(konstue: T): T = suspendCoroutine { continuation ->
        log += "suspend($konstue);"
        callback = {
            continuation.resume(konstue)
        }
    }

    suspend fun suspendWithException(konstue: String): Unit = suspendCoroutine { continuation ->
        log += "error($konstue);"
        callback = {
            continuation.resumeWithException(RuntimeException(konstue))
        }
    }
}

abstract class ContinuationDispatcher : AbstractCoroutineContextElement(ContinuationInterceptor), ContinuationInterceptor {
    abstract fun <T> dispatchResumeWith(konstue: Result<T>, continuation: Continuation<T>): Boolean
    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> = DispatchedContinuation(this, continuation)
}

private class DispatchedContinuation<T>(
    konst dispatcher: ContinuationDispatcher,
    konst continuation: Continuation<T>
): Continuation<T> {
    override konst context: CoroutineContext = continuation.context

    override fun resumeWith(konstue: Result<T>) {
        if (!dispatcher.dispatchResumeWith(konstue, continuation))
            continuation.resumeWith(konstue)
    }
}

fun test(c: suspend Controller.() -> Unit): String {
    konst controller = Controller()
    c.startCoroutine(controller, EmptyContinuation(object: ContinuationDispatcher() {
        private fun dispatchResume(block: () -> Unit) {
            konst id = controller.resumeIndex++
            controller.log += "before $id;"
            block()
            controller.log += "after $id;"
        }

        override fun <P> dispatchResumeWith(data: Result<P>, continuation: Continuation<P>): Boolean {
            dispatchResume {
                continuation.resumeWith(data)
            }
            return true
        }
    }))

    while (controller.callback != null) {
        konst c = controller.callback!!
        controller.callback = null
        c()
    }

    return controller.log
}

suspend fun Controller.foo() = suspendWithValue("") + suspendWithValue("O")

suspend fun Controller.test1() {
    konst o = foo()
    konst k = suspendWithValue("K")
    log += "$o$k;"
}

suspend fun Controller.test2() {
    try {
        foo()

        suspendWithException("OK")
        log += "ignore;"
    }
    catch (e: RuntimeException) {
        log += "${e.message};"
    }
}

fun box(): String {
    var result = test {
        test1()
    }
    if (result != "before 0;suspend();after 0;before 1;suspend(O);after 1;before 2;suspend(K);after 2;before 3;OK;after 3;") return "fail1: $result"

    result = test {
        test2()
    }
    if (result != "before 0;suspend();after 0;before 1;suspend(O);after 1;before 2;error(OK);after 2;before 3;OK;after 3;") return "fail2: $result"

    return "OK"
}
