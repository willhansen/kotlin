// TARGET_BACKEND: JVM
// FULL_JDK
// WITH_STDLIB
// WITH_COROUTINES

import helpers.*
import kotlin.coroutines.*

var c: Continuation<*>? = null

suspend fun <T> tx(lambda: () -> T): T = suspendCoroutine { c = it; lambda() }

object Dummy

interface Base<T> {
    suspend fun generic(): T
}

abstract class Derived1: Base<Unit> {
}

class Derived2: Derived1() {
    override suspend fun generic(): Unit {
        tx { Dummy }
    }
}
fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

fun box(): String {
    var res: Any? = null

    builder {
        konst base: Base<*> = Derived2()
        res = base.generic()
    }

    (c as? Continuation<Dummy>)?.resume(Dummy)

    return if (res != Unit) "$res" else "OK"
}
