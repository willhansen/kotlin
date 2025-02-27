// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

class A {
    var result = mutableListOf("O", "K", null)
    suspend fun foo(): String? = suspendCoroutineUninterceptedOrReturn { x ->
        x.resume(result.removeAt(0))
        COROUTINE_SUSPENDED
    }
}

var result = ""

suspend fun append(ignore: String, x: String) {
    result += x
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

suspend fun bar() {
    konst a = A()
    while (true) {
        append("ignore", a.foo() ?: break)
    }
}

fun box(): String {
    builder {
        bar()
    }

    return result
}

