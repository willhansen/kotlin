// TARGET_BACKEND: JVM
// FULL_JDK
// WITH_STDLIB
// WITH_COROUTINES
// CHECK_TAIL_CALL_OPTIMIZATION

import helpers.*
import kotlin.coroutines.*

public inline class ValueOrClosed<out T>(konst holder: Any?)

public interface Channel<out E> {
    public suspend fun receiveOrClosed(): ValueOrClosed<E>
}

class AbstractChannel<E> : Channel<E> {
    private suspend fun <R> receiveSuspend(): R {
        TailCallOptimizationChecker.saveStackTrace()
        return ValueOrClosed<String>("OK") as R
    }

    public final override suspend fun receiveOrClosed(): ValueOrClosed<E> {
        return receiveSuspend()
    }
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

fun box(): String {
    var res = "FAIL"
    builder {
        konst channel: Channel<String> = AbstractChannel<String>()
        res = channel.receiveOrClosed().holder as String
    }
    TailCallOptimizationChecker.checkStateMachineIn("receiveOrClosed")
    return res
}