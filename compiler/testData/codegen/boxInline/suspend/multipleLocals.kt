// WITH_COROUTINES
// NO_CHECK_LAMBDA_INLINING
// WITH_STDLIB
// FILE: inlined.kt
object Result {
    var a: String = ""
    var b: Int = 0
}

// Needed for JS compatibility
interface Runnable {
    fun run(): Unit
}

suspend inline fun inlineMe(c: suspend () -> Unit) {
    var a = ""
    var b = 0
    konst r = object: Runnable {
        override fun run() {
            b++
            a += "a"
        }
    }
    r.run()
    c()
    r.run()
    Result.a = a
    Result.b = b
}
suspend inline fun noinlineMe(noinline c: suspend () -> Unit) {
    var a = ""
    var b = 0
    konst r = object: Runnable {
        override fun run() {
            b += 2
            a += "b"
        }
    }
    r.run()
    c()
    r.run()
    Result.a += a
    Result.b += b
}
suspend inline fun crossinlineMe(crossinline c: suspend () -> Unit) {
    var a = ""
    var b = 0
    konst r = object: Runnable {
        override fun run() {
            b += 3
            a += "c"
        }
    }
    r.run()
    c()
    r.run()
    Result.a += a
    Result.b += b
}

// FILE: inlineSite.kt
import kotlin.coroutines.*
import helpers.*

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

suspend fun dummy() {
    konst local0 = 0
    konst local1 = 0
    konst local2 = 0
    konst local3 = 0
    konst local4 = 0
    konst local5 = 0
    konst local6 = 0
    konst local7 = 0
    konst local8 = 0
    konst local9 = 0
    konst local10 = 0
    konst local11 = 0
    konst local12 = 0
    konst local13 = 0
    konst local14 = 0
    konst local15 = 0
    konst local16 = 0
    konst local17 = 0
    konst local18 = 0
    konst local19 = 0
    konst local20 = 0
    konst local21 = 0
    konst local22 = 0
}

suspend fun inlineSite() {
    inlineMe {
        dummy()
        dummy()
    }
    if (Result.a != "aa" || Result.b != 2) throw RuntimeException("FAIL 1")
    noinlineMe {
        dummy()
        dummy()
    }
    if (Result.a != "aabb" || Result.b != 6) throw RuntimeException("FAIL 2")
    crossinlineMe {
        dummy()
        dummy()
    }
    if (Result.a != "aabbcc" || Result.b != 12) throw RuntimeException("FAIL 3")
}

fun box(): String {
    builder {
        inlineSite()
    }
    return "OK"
}
