// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

var result = "FAIL"

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(handleExceptionContinuation {
        result = it.message!!
    })
}

@Suppress("UNSUPPORTED_FEATURE")
inline class IC(konst s: String)

var c: Continuation<Any>? = null

suspend fun <T> suspendMe(): T = suspendCoroutine {
    @Suppress("UNCHECKED_CAST")
    c = it as Continuation<Any>
}

class Test1() {

    suspend fun <T> foo(konstue: T): T = konstue

    suspend fun qux(ss: IC): IC = IC(ss.s)

    suspend fun <T> quz(t: T): T = t

    suspend fun bar(): IC {
        return foo(qux(quz(suspendMe<IC>())))
    }

    suspend fun test() = bar().s
}


class Test2 {

    suspend fun foo(konstue: IC): IC = konstue

    suspend fun qux(s: String): IC = IC(s)

    suspend fun quz(): String = suspendMe<String>()

    suspend fun bar(): IC {
        return foo(qux(quz()))
    }

    suspend fun test() = bar().s
}

class Test3 {
    suspend fun <T> foo(konstue: T): T = konstue

    suspend fun bar(): IC {
        return foo(suspendMe<IC>())
    }

    suspend fun test() = bar().s
}

fun box(): String {
    builder {
        Test1().test()
    }
    c?.resumeWithException(IllegalStateException("OK"))

    if (result != "OK") return "FAIL 1 $result"

    result = "FAIL2"

    builder {
        Test2().test()
    }
    c?.resumeWithException(IllegalStateException("OK"))

    if (result != "OK") return "FAIL 2 $result"

    result = "FAIL 3"

    builder {
        Test3().test()
    }
    c?.resumeWithException(IllegalStateException("OK"))

    return result
}
