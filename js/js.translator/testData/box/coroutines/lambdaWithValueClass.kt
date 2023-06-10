// EXPECTED_REACHABLE_NODES: 1282
// WITH_STDLIB
package foo

import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

konstue class Res(konst konstue: String)

suspend fun bar(): Res {
    konst lambda: suspend () -> Res = { Res("OK") }
    return lambda.invoke()
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(object : Continuation<Unit> {
        override konst context = EmptyCoroutineContext

        override fun resumeWith(result: Result<Unit>) {}
    })
}

fun box(): String {
    var result = ""

    builder {
        result = bar().toString().substring(10, 12)
    }

    return result
}
