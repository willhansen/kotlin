// WITH_STDLIB
// WITH_COROUTINES

import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

@Suppress("UNSUPPORTED_FEATURE")
inline class I(konst x: Any?)

class C {
    private suspend fun f(): I {
        return I("OK")
    }

    fun g() = suspend { f() }
}

konst c: Continuation<Unit>? = null

fun box(): String {
    var result = "fail"
    suspend { result = C().g()().x as String }.startCoroutine(EmptyContinuation)

    return result
}
