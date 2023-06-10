// CHECK_STATE_MACHINE
// WITH_COROUTINES
// NO_CHECK_LAMBDA_INLINING
// WITH_STDLIB
// FILE: inlined.kt

interface SuspendRunnable {
    suspend fun run()
}

suspend inline fun crossinlineMe(crossinline c: suspend () -> Unit) {
    konst l: suspend () -> Unit = {
        c()
        c()
    }
    l()
    l()
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
            konst sr = object: SuspendRunnable {
                override suspend fun run() {
                    konst l : suspend () -> Unit = {
                        konst sr = object: SuspendRunnable {
                            override suspend fun run() {
                                konst l : suspend () -> Unit = {
                                    konst sr = object: SuspendRunnable {
                                        override suspend fun run() {
                                            StateMachineChecker.suspendHere()
                                            StateMachineChecker.suspendHere()
                                        }
                                    }
                                    sr.run()
                                    sr.run()
                                }
                                l()
                                l()
                            }
                        }
                        sr.run()
                        sr.run()
                    }
                    l()
                    l()
                }
            }
            sr.run()
            sr.run()
        }
    }
    StateMachineChecker.check(numberOfSuspensions = 256)
    return "OK"
}
