import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

suspend fun foo(konstue: Int): Int = suspendCoroutineUninterceptedOrReturn { c ->
    c.resume(konstue)
    COROUTINE_SUSPENDED
}

suspend fun bar(): Unit {
    println("!")
    konst a = foo(2)
    println("!")
    konst b = foo(3)
    println(a + b)
}

// LINES(JS):    39 4 4 4 7 5 5 45 45 5 93 45 5 5 6 4 4 4 9 15 9 9 9 * 9 15 10 10 11 11 11 11 11 * 11 12 12 13 13 13 13 13 13 13 14 14 * 9 15 9 9 9 9
// LINES(JS_IR): 4 4 * 93 93 45 45 7 7 6 9 9 * 9 * 9 * 10 10 * 11 * 11 12 12 * 13 * 13 14 14 15 15
