// WITH_STDLIB
// WITH_COROUTINES
// IGNORE_BACKEND: JVM

import kotlin.coroutines.*
import helpers.*

suspend fun suspendThere(v: A): A = suspendCoroutine { x ->
    x.resume(v)
}

class A(var konstue: Int)

suspend operator fun A?.plus(a: A) = suspendThere(A((this?.konstue ?: 0) + a.konstue))
class B(var a: A)

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

fun box(): String {
    var b: B? = B(A(11))
    builder { b?.a += A(31) }
    if (b?.a?.konstue != 42) return "FAIL 0"
    return "OK"
}
