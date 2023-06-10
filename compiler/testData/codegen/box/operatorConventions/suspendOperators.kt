// WITH_STDLIB
// WITH_COROUTINES
// IGNORE_BACKEND_K1: ANY
// IGNORE_BACKEND: WASM

import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

fun <T> runBlocking(c: suspend () -> T): T {
    var res: T? = null
    c.startCoroutine(Continuation(EmptyCoroutineContext) {
        res = it.getOrThrow()
    })
    return res!!
}

class A {
    var konstue = ""

    suspend operator fun get(x: Int) = konstue
    suspend operator fun set(x: Int, v: String) {
        konstue = v
    }

    operator suspend fun contains(y: String): Boolean = y == konstue
}

fun box() = runBlocking {
    konst a = A()
    if ("" !in a) return@runBlocking "FAIL"
    a[1] = "OK"

    a[2]
}