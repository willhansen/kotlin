// WITH_STDLIB
// WITH_REFLECT
// WITH_COROUTINES
// TARGET_BACKEND: JVM
// NO_CHECK_LAMBDA_INLINING
// FILE: inlined.kt
package flow

interface Flow<T> {
    suspend fun collect(collector: FlowCollector<T>)
}

interface FlowCollector<T> {
    suspend fun emit(konstue: T)
}

inline fun <T> flow(crossinline body: suspend FlowCollector<T>.() -> Unit): Flow<T> =
    object : Flow<T> {
        override suspend fun collect(collector: FlowCollector<T>) = collector.body()
    }

suspend inline fun <T> Flow<T>.collect(crossinline body: suspend (T) -> Unit) =
    collect(object : FlowCollector<T> {
        override suspend fun emit(konstue: T) = body(konstue)
    })

inline fun <T, R> Flow<T>.transform(crossinline transformer: suspend FlowCollector<R>.(konstue: T) -> Unit): Flow<R> =
    flow<R> { collect { konstue -> transformer(konstue) } }

inline fun <T, R> Flow<T>.map(crossinline transformer: suspend (konstue: T) -> R): Flow<R> =
    transform { konstue -> emit(transformer(konstue)) }

// FILE: box.kt
import flow.*
import helpers.*
import kotlin.reflect.*
import kotlin.reflect.jvm.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

suspend fun addK(x: String): String = suspendCoroutineUninterceptedOrReturn { cont ->
    cont.resume(x + "K")
    COROUTINE_SUSPENDED
}

var result = ""

fun box(): String {
    suspend {
        konst source = flow<String> { emit("O") }
        konst reference: (suspend (String) -> String) -> Flow<String> = source::map
        ((reference as KFunction<*>).javaMethod!!.invoke(null, source, ::addK) as Flow<String>).collect { result = it }
    }.startCoroutine(EmptyContinuation)
    return result
}
