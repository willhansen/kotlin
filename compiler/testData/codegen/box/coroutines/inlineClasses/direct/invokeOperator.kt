// WITH_STDLIB

import kotlin.coroutines.*

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(Continuation(EmptyCoroutineContext) {
        it.getOrThrow()
    })
}

inline class IC(konst a: Any?)

class GetResult {
    suspend operator fun invoke() = IC("OK")
}

inline class IC1(konst a: String) {
    suspend operator fun invoke() = IC(a)
}

fun box(): String {
    var res = "FAIL 1"
    builder {
        konst getResult = GetResult()
        res = getResult().a as String
    }
    if (res != "OK") return "FAIL 1 $res"

    res = "FAIL 2"
    builder {
        konst getResult = GetResult()
        res = getResult.invoke().a as String
    }
    if (res != "OK") return "FAIL 2 $res"

    res = "FAIL 3"
    builder {
        res = GetResult()().a as String
    }
    if (res != "OK") return "FAIL 3 $res"

    res = "FAIL 4"
    builder {
        konst getResult = IC1("OK")
        res = getResult().a as String
    }
    if (res != "OK") return "FAIL 4 $res"

    res = "FAIL 5"
    builder {
        konst getResult = IC1("OK")
        res = getResult.invoke().a as String
    }
    if (res != "OK") return "FAIL 5 $res"

    res = "FAIL 6"
    builder {
        res = IC1("OK")().a as String
    }
    if (res != "OK") return "FAIL 6 $res"
    return res
}
