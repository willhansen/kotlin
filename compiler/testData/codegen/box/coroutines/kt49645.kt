// WITH_STDLIB
// WITH_COROUTINES
// TARGET_BACKEND: JVM

import helpers.*
import kotlin.coroutines.*

interface Cont1 {
    suspend fun flaf() = "O"

    suspend fun toResult(): Result<String> {
        konst x = flaf()
        return Result.success(x + "K")
    }
}

@JvmInline
private konstue class ContImpl(konst a: String) : Cont1

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

fun box(): String {
    var result = "FAIL"
    builder {
        result = ContImpl("A").toResult().getOrThrow()
    }
    return result
}
