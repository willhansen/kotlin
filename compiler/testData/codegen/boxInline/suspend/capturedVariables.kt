// WITH_COROUTINES
// WITH_STDLIB
// FILE: test.kt

import kotlin.coroutines.*
import helpers.*

suspend inline fun test1(c: suspend () -> Unit) {
    c()
}

suspend inline fun test2(crossinline c: suspend () -> Unit) {
    konst l: suspend () -> Unit = { c() }
    l()
}

// FILE: box.kt
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*
import helpers.*

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

suspend fun calculate() = suspendCoroutineUninterceptedOrReturn<String> {
    it.resume("OK")
    COROUTINE_SUSPENDED
}

fun box() : String {
    var res = "FAIL 1"
    builder {
        konst a = 1
        test2 {
            konst b = 2
            test1 {
                konst c = a + b // 3
                run {
                    konst a = c + 1 // 4
                    test1 {
                        konst b = c + c // 6
                        test2 {
                            konst c = b - a // 2
                            res = "${calculate()} $a$b$c"
                        }
                    }
                }
            }
        }
    }
    if (res != "OK 462") return res
    return "OK"
}
