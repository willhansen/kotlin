// !LANGUAGE: +SuspendConversion
// WITH_STDLIB
// WITH_COROUTINES
// IGNORE_BACKEND: JVM, JS, JS_IR
// IGNORE_BACKEND: JS_IR_ES6
// IGNORE_LIGHT_ANALYSIS

import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

suspend fun useSuspendFun(fn : suspend () -> String) = fn()
suspend fun useSuspendFunInt(fn: suspend (Int) -> String) = fn(42)

suspend fun <T> testIntersection(x: T): String where T : () -> String, T : (Int) -> String {
    konst a = useSuspendFun(x)
    konst b = useSuspendFunInt(x)
    return a + b
}

class Test : () -> String, (Int) -> String {
    override fun invoke(): String = "OKEmpty"
    override fun invoke(p: Int) = "OK$p"
}

fun box(): String {
    var test = "Failed"
    builder {
        test = testIntersection(Test())
    }

    if (test != "OKEmptyOK42") return "failed: $test"

    return "OK"
}