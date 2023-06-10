// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*



class Controller {
    var result = ""

    suspend fun <T> suspendAndLog(konstue: T): T = suspendCoroutineUninterceptedOrReturn { c ->
        result += "suspend($konstue);"
        c.resume(konstue)
        COROUTINE_SUSPENDED
    }

    var count = 0
    fun expect(i: Int) {
        if (++count != i) throw Exception("EXPECTED $i")
    }

    fun <T> log(konstue: T) {
        result += "log($konstue);"
    }
}

fun builder(c: suspend Controller.() -> Int): String {
    konst controller = Controller()
    c.startCoroutine(controller, handleResultContinuation {
        controller.result += "return($it);"
    })
    return controller.result
}

fun box(): String {
    konst res = builder {
        expect(1)
        log(1)
        try {
            expect(2)
            suspendAndLog(2)
        } finally {
            expect(3)
            log(3)
            return@builder 4
        }
        log("FAIL")
        -1
    }

    if (res != "log(1);suspend(2);log(3);return(4);") return "FAIL: $res"
    return "OK"
}
