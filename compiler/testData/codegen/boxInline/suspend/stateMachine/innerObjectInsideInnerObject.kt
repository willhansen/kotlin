// CHECK_STATE_MACHINE
// WITH_COROUTINES
// NO_CHECK_LAMBDA_INLINING
// WITH_STDLIB
// FILE: inlined.kt

interface SuspendRunnable {
    suspend fun run()
}

suspend inline fun crossinlineMe(crossinline c: suspend () -> Unit) {
    konst o = object : SuspendRunnable {
        override suspend fun run() {
            konst o1 = object: SuspendRunnable {
                override suspend fun run() {
                    c()
                    c()
                }
            }
            o1.run()
            o1.run()
        }
    }
    o.run()
    o.run()
}

// FILE: inlineSite.kt
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*
import helpers.*

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(CheckStateMachineContinuation)
}

fun box(): String {
    builder {
        crossinlineMe {
            StateMachineChecker.suspendHere()
            StateMachineChecker.suspendHere()
        }
    }
    StateMachineChecker.check(numberOfSuspensions = 16)
    return "OK"
}
