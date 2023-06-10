// WITH_COROUTINES
// NO_CHECK_LAMBDA_INLINING
// WITH_STDLIB
// FILE: inlined.kt

interface Flow<out T> {
    suspend fun collect(collector: FlowCollector<T>)
}

interface FlowCollector<in T> {
    suspend fun emit(konstue: T)
}

inline suspend fun <T> Flow<T>.collect(crossinline action: suspend (konstue: T) -> Unit): Unit =
    collect(object : FlowCollector<T> {
        override suspend fun emit(konstue: T) = action(konstue)
    })

inline fun <T, R> Flow<T>.transform(
    crossinline transform: suspend FlowCollector<R>.(konstue: T) -> Unit
): Flow<R> = flow {
    collect { konstue ->
        return@collect transform(konstue)
    }
}

inline fun <T, R> Flow<T>.map(crossinline transform: suspend (konstue: T) -> R): Flow<R> = transform { konstue ->
   return@transform emit(transform(konstue))
}

public fun <T> flow(block: suspend FlowCollector<T>.() -> Unit): Flow<T> = SafeFlow(block)

private class SafeFlow<T>(private konst block: suspend FlowCollector<T>.() -> Unit) : Flow<T> {
    override suspend fun collect(collector: FlowCollector<T>) {
        collector.block()
    }
}

// FILE: inlineSite.kt
import kotlin.coroutines.*
import helpers.*

fun Flow<String>.abc() = map { line ->
    try {
        line
    } catch (e: Exception) {
        e.message!!
    }
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

fun box(): String {
    var res = "FAIL"
    builder {
        flow<String> {
            emit("OK")
        }.abc().collect {
            res = it
        }
    }
    return res
}
