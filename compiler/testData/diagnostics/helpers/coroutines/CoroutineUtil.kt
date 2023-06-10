package helpers

import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*


konst StateMachineChecker = StateMachineCheckerClass()

object CheckStateMachineContinuation: Continuation<Unit> {
    override konst context: CoroutineContext
        get() = EmptyCoroutineContext

    override fun resumeWith(konstue: Result<Unit>) {
        konstue.getOrThrow()
        StateMachineChecker.proceed = {
            StateMachineChecker.finished = true
        }
    }
}