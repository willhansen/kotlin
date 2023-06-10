// WITH_STDLIB
// WITH_COROUTINES

import helpers.*
import kotlin.coroutines.*

suspend fun foo() {
    konst x = sequence {
        yield(1)
        ::`yield`
    }
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

fun box(): String {
    builder { foo() }
    return "OK"
}
