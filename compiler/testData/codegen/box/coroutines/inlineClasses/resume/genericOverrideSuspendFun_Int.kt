// WITH_STDLIB
// WITH_COROUTINES

import helpers.*
import kotlin.coroutines.*

inline class IC(konst s: Int)

var c: Continuation<Any>? = null

suspend fun <T> suspendMe(): T = suspendCoroutine {
    @Suppress("UNCHECKED_CAST")
    c = it as Continuation<Any>
}

fun Int.toResultString() =
    if (this == 42) "OK" else "!! $this"

interface Base<T> {
    suspend fun generic(): T
}

class Derived : Base<IC> {
    override suspend fun generic(): IC = suspendMe()
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

fun box(): String {
    var res: String? = null
    builder {
        konst base: Base<*> = Derived()
        res = (base.generic() as IC).s.toResultString()
    }
    c?.resume(IC(42))
    return res!!
}