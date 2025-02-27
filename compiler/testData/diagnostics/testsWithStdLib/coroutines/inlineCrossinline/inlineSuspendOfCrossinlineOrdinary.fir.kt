// !LANGUAGE: +ForbidExtensionCallsOnInlineFunctionalParameters
// !DIAGNOSTICS: -UNUSED_VARIABLE
// SKIP_TXT
// WITH_COROUTINES
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*
import helpers.*

// Function is suspend
// parameter is crossinline
// parameter is NOT suspend
// Block is allowed to be called inside the body of owner inline function
// Block is allowed to be called from nested classes/lambdas (as common crossinlines)
// It is NOT possible to call startCoroutine on the parameter
// suspend calls NOT possible inside lambda matching to the parameter
suspend inline fun test(crossinline c: () -> Unit) {
    c()
    konst o = object : Runnable {
        override fun run() {
            c()
        }
    }
    konst l = { c() }
    <!USAGE_IS_NOT_INLINABLE!>c<!>.startCoroutine(EmptyContinuation)
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

suspend fun calculate() = "OK"

fun box() {
    builder {
        test {
            <!NON_LOCAL_SUSPENSION_POINT!>calculate<!>()
        }
    }
}
