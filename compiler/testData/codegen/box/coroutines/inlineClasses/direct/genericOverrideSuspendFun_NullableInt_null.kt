// WITH_STDLIB
// WITH_COROUTINES

import helpers.*
import kotlin.coroutines.*

inline class IC(konst s: Int?)

fun Int?.toResultString() =
    if (this == null) "OK" else "!! $this"

interface Base<T> {
    suspend fun generic(): T
}

class Derived : Base<IC> {
    override suspend fun generic(): IC = suspendCoroutine { it.resume(IC(null)) }
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
    return res!!
}