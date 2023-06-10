// WITH_STDLIB
// WITH_COROUTINES
// FILE: 1.kt
inline fun <T, R> (suspend () -> T).map(crossinline transform: suspend (T) -> R): suspend () -> R =
    { transform(this()) }

// FILE: 2.kt
import helpers.*
import kotlin.coroutines.*

inline class C(konst konstue: String)

fun box(): String {
    var result = "fail"
    suspend {
        result = suspend { C("OK") }.map { it }().konstue
    }.startCoroutine(EmptyContinuation)
    return result
}
