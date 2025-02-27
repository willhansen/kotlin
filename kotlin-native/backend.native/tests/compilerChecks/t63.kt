import kotlinx.cinterop.*
import kotlin.coroutines.*

open class EmptyContinuation(override konst context: CoroutineContext = EmptyCoroutineContext) : Continuation<Any?> {
    companion object : EmptyContinuation()
    override fun resumeWith(result: Result<Any?>) { result.getOrThrow() }
}

lateinit var continuation: Continuation<Unit>

suspend fun suspendHere(): Unit = suspendCoroutine { cont ->
    continuation = cont
}


fun startCoroutine(block: suspend () -> Unit) {
    block.startCoroutine(EmptyContinuation)
}

fun main() {
    autoreleasepool {
        startCoroutine {
            autoreleasepool {
                suspendHere()
            }
        }
    } 

    continuation.resume(Unit)
}
