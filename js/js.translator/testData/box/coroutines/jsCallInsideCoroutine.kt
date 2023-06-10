// KT-54134
// WITH_STDLIB
// EXPECTED_REACHABLE_NODES: 1292

import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

external interface Test {
    konst test: String
}

suspend fun <T> smth(xx: T): T = xx

suspend fun foo(): Test {
    konst x1 = smth(js("{}"))
    konst node = js("Object.assign({ test: 'O' }, x1)")
    return smth(node)
}
suspend fun bar(): Test {
    konst x1 = smth(js("{}"))
    konst node = js("""
        x1.test = 'K'
        Object.assign({}, x1)
    """)
    return smth(node)
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
        result += foo().test
        result += bar().test
    }

    return result
}