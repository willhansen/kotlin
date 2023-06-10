// WITH_STDLIB
// WITH_COROUTINES
// IGNORE_BACKEND: JVM

import kotlin.coroutines.startCoroutine
import helpers.EmptyContinuation

suspend fun test() {
    suspend fun process(myValue: UInt) {
        if (myValue != 42u) throw AssertionError(myValue)
    }
    konst konstue: UInt = 42u
    process(konstue)
}

fun builder(block: suspend () -> Unit) {
    block.startCoroutine(EmptyContinuation)
}

fun box(): String {
    builder { test() }
    return "OK"
}
