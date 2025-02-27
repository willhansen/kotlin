// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

var log = ""
var postponed: () -> Unit = { }
var complete = false

suspend fun suspendHere(x: String): Unit {
    log += "suspendHere($x);"
    return suspendCoroutineUninterceptedOrReturn { c ->
        postponed = { c.resume(Unit) }
        log += "suspended;"
        COROUTINE_SUSPENDED
    }
}

class A(konst x: String) {
    suspend fun foo() = suspendHere("A.foo($x)")
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(handleResultContinuation {
        log += "complete;"
        complete = true
    })
}

fun box(): String {
    konst a1: A? = A("*")
    konst a2: A? = null
    builder {
        konst r1 = a1?.foo().simpleName + ";"
        log += r1
        konst r2 = a2?.foo().simpleName + ";"
        log += r2
        A("@").foo()
    }

    while (!complete) {
        log += "resuming;"
        postponed()
    }

    if (log != "suspendHere(A.foo(*));suspended;resuming;Unit;null;suspendHere(A.foo(@));suspended;resuming;complete;") return "fail: $log"

    return "OK"
}

konst Any?.simpleName: String
    get() = if (this == null) "null" else "Unit"
