// IGNORE_BACKEND: JS_IR, JS_IR_ES6
// WITH_COROUTINES
// WITH_STDLIB
// MODULE: lib(support)
// FILE: lib.kt

import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

var continuation: () -> Unit = { }
var log = ""
var finished = false

class C {
    var v: String = ""

    inline suspend fun bar() {
        log += "before bar($v);"
        foo("1:$v")
        log += "inside bar($v);"
        foo("2:$v")
        log += "after bar($v);"
    }
}

suspend fun <T> foo(v: T): T = suspendCoroutineUninterceptedOrReturn { x ->
    continuation = {
        x.resume(v)
    }
    log += "foo($v);"
    COROUTINE_SUSPENDED
}

fun C.builder(c: suspend C.() -> Unit) {
    c.startCoroutine(this, handleResultContinuation {
        continuation = { }
        finished = true
    })
}

// MODULE: main(lib)
// FILE: main.kt

import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

suspend fun C.baz() {
    v = "A"
    bar()
    log += "between bar;"
    v = "B"
    bar()
}

konst expectedString =
        "before bar(A);foo(1:A);@;inside bar(A);foo(2:A);@;after bar(A);" +
        "between bar;" +
        "before bar(B);foo(1:B);@;inside bar(B);foo(2:B);@;after bar(B);"

fun box(): String {
    var c = C()

    c.builder {
        baz()
    }

    while (!finished) {
        log += "@;"
        continuation()
    }

    if (log != expectedString) return "fail: $log"

    return "OK"
}
