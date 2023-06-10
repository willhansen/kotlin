// WITH_STDLIB
// WITH_COROUTINES

import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

fun <T> builder(konstue: T, c: suspend T.() -> Unit) {
    c.startCoroutine(konstue, EmptyContinuation)
}

interface A<T> {
    konst konstue: T
    var result: T

    fun test(): T {
        builder(konstue) { result = this }
        return result
    }
}

fun box(): String =
    object : A<String> {
        override konst konstue = "OK"
        override var result = "Fail"
    }.test()
