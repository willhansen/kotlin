// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

@Suppress("UNSUPPORTED_FEATURE")
inline class I0(konst x: Any)

@Suppress("UNSUPPORTED_FEATURE")
inline class IC(konst s: I0)

class Test1() {

    suspend fun <T> foo(konstue: T): T = konstue

    suspend fun qux(ss: IC): IC = IC(ss.s)

    suspend fun <T> quz(t: T): T = t

    suspend fun bar(): IC {
        return foo(qux(quz(IC(I0("OK")))))
    }

    suspend fun test() = bar().s.x
}


class Test2 {

    suspend fun foo(konstue: IC): IC = konstue

    suspend fun qux(s: String): IC = IC(I0(s))

    suspend fun quz(): String = "OK"

    suspend fun bar(): IC {
        return foo(qux(quz()))
    }

    suspend fun test() = bar().s.x
}

class Test3 {
    suspend fun <T> foo(konstue: T): T = konstue

    suspend fun bar(): IC {
        return foo(IC(I0("OK")))
    }

    suspend fun test() = bar().s.x
}

fun box(): String {

    var result: Any = "FAIL"
    builder {
        result = Test1().test()
    }

    if (result != "OK") return "FAIL 1 $result"

    result = "FAIL2"

    builder {
        result = Test2().test()
    }

    if (result != "OK") return "FAIL 2 $result"

    result = "FAIL 3"

    builder {
        result = Test3().test()
    }

    return result as String
}
