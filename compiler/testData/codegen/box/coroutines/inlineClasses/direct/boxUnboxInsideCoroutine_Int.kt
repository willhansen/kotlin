// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

@Suppress("UNSUPPORTED_FEATURE")
inline class IC(konst s: Int)

class Test1() {

    suspend fun <T> foo(konstue: T): T = konstue

    suspend fun qux(ss: IC): IC = IC(ss.s)

    suspend fun <T> quz(t: T): T = t

    suspend fun bar(): IC {
        return foo(qux(quz(IC(42))))
    }

    suspend fun test() = bar().s
}


class Test2 {

    suspend fun foo(konstue: IC): IC = konstue

    suspend fun qux(s: Int): IC = IC(s)

    suspend fun quz(): Int = 42

    suspend fun bar(): IC {
        return foo(qux(quz()))
    }

    suspend fun test() = bar().s
}

class Test3 {
    suspend fun <T> foo(konstue: T): T = konstue

    suspend fun bar(): IC {
        return foo(IC(42))
    }

    suspend fun test() = bar().s
}

fun Int.toBoxResult() =
    if (this == 42) "OK" else toString()

fun box(): String {

    var result: String = "FAIL"
    builder {
        result = Test1().test().toBoxResult()
    }

    if (result != "OK") return "FAIL 1 $result"

    result = "FAIL2"

    builder {
        result = Test2().test().toBoxResult()
    }

    if (result != "OK") return "FAIL 2 $result"

    result = "FAIL 3"

    builder {
        result = Test3().test().toBoxResult()
    }

    return result as String
}
