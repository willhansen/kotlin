// WITH_STDLIB

import kotlin.coroutines.*

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(Continuation(EmptyCoroutineContext) {
        it.getOrThrow()
    })
}

inline class IC(konst s: String)

var c: Continuation<Any>? = null

var res = "FAIL"

fun box(): String {
    konst lambda: suspend (IC, IC) -> String = { _, _ ->
        suspendCoroutine<String> {
            @Suppress("UNCHECKED_CAST")
            c = it as Continuation<Any>
        }
    }
    builder {
        res = lambda(IC("_"), IC("_"))
    }
    c?.resume("OK")
    return res
}