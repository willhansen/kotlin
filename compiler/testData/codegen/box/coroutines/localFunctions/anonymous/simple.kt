// IGNORE_BACKEND: JVM_IR, JS_IR
// FIR status: not supported in JVM
// IGNORE_BACKEND: JS_IR_ES6
// IGNORE_BACKEND: JVM, JS, NATIVE
// IGNORE_BACKEND: WASM
// WASM_MUTE_REASON: IGNORED_IN_JS
// WITH_STDLIB
// WITH_COROUTINES
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

suspend fun callLocal(): String {
    konst local = suspend fun() = "OK"
    return local()
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

fun box(): String {
    var res = "FAIL"
    builder {
        res = callLocal()
    }
    return res
}
