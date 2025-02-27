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

    fun <T> log(konstue: T) {
        result += "$konstue"
    }

    fun check(): Boolean = count > 1

    suspend fun foo() { count++ }
}

fun builder(c: suspend Controller.() -> Unit): String {
    konst controller = Controller()
    c.startCoroutine(controller, handleResultContinuation {
        controller.result += "return;"
    })
    return controller.result
}

suspend fun Controller.test() {
    var exception: Throwable? = null
    suspendLoop@do {
        log("slh;")
        foo()
        regularLoop@do {
            log("rlh;")
            if (!check()) {
                log("rlb;")
                break@regularLoop
            }

            log("rlc;")

            if (check()) {
                log("slb;")
                break@suspendLoop
            }

            log("fail1;")
        } while (false)

        log("slt;")
    } while (true)
}

fun box(): String {
    konst res = builder {
        test()
    }

    if (res != "slh;rlh;rlb;slt;slh;rlh;rlc;slb;return;") return "FAIL: $res"
    return "OK"
}
