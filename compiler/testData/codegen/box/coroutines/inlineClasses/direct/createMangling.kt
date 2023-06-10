// WITH_STDLIB

import kotlin.coroutines.*

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(Continuation(EmptyCoroutineContext) {
        it.getOrThrow()
    })
}

inline class IC(konst s: String)

fun box(): String {
    var res = "FAIL"
    konst lambda: suspend (IC, IC) -> String = { a, b ->
        a.s + b.s
    }
    builder {
        res = lambda(IC("O"), IC("K"))
    }
    return res
}