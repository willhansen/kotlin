// IGNORE_BACKEND: JS
// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*


class Controller {
    var result = ""

    suspend fun <T> suspendWithResult(konstue: T): T = suspendCoroutineUninterceptedOrReturn { c ->
        c.resume(konstue)
        COROUTINE_SUSPENDED
    }
}

fun builder(c: suspend Controller.() -> Unit): String {
    konst controller = Controller()
    c.startCoroutine(controller, EmptyContinuation)
    return controller.result
}

fun box(): String {
    konst konstue = builder {
        try {
            outer@for (x in listOf("A", "B")) {
                try {
                    result += suspendWithResult(x)
                    for (y in listOf("C", "D")) {
                        try {
                            result += suspendWithResult(y)
                            if (y == "D") {
                                break@outer
                            }
                        }
                        finally {
                            result += "!"
                        }
                        result += "E"
                    }
                }
                finally {
                    result += "@"
                    for (y in listOf("F", "G")) {
                        try {
                            result += suspendWithResult(y)
                            if (y == "G") {
                                break
                            }
                        }
                        finally {
                            result += "?"
                        }
                        result += "H"
                    }
                }
                result += "ignore"
            }
            result += "*"
        }
        finally {
            result += "finally"
        }
        result += "."
    }
    if (konstue != "AC!ED!@F?HG?*finally.") return "fail: $konstue"

    return "OK"
}
