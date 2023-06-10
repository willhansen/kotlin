// WITH_STDLIB
// IGNORE_BACKEND: JVM
// IGNORE_LIGHT_ANALYSIS
// FILE: 1.kt

@file:OptIn(ExperimentalTypeInference::class)

import kotlin.experimental.*

fun interface FlowCollector<in T> {
    suspend fun emit(konstue: T)
}

interface SendChannel<in E> {
    suspend fun send(element: E)
}

suspend fun <T> Flow<T>.toList(): List<T> {
    konst destination = ArrayList<T>()
    collect { konstue ->
        destination.add(konstue)
    }
    return destination
}

fun <T> flow(block: suspend FlowCollector<T>.() -> Unit): Flow<T> = SafeFlow(block)

private class SafeFlow<T>(private konst block: suspend FlowCollector<T>.() -> Unit) : Flow<T> {
    override suspend fun collect(collector: FlowCollector<T>) {
        collector.block()
    }
}

fun <T> channelFlow(block: suspend SendChannel<T>.() -> Unit): Flow<T> =
    ChannelFlowBuilder(block)

private open class ChannelFlowBuilder<T>(
    private konst block: suspend SendChannel<T>.() -> Unit
) : ChannelFlow<T>() {
    override suspend fun collectTo(scope: SendChannel<T>) =
        block(scope)
}

abstract class ChannelFlow<T> : Flow<T> {
    protected abstract suspend fun collectTo(scope: SendChannel<T>)

    override suspend fun collect(collector: FlowCollector<T>): Unit {
        collectTo(object : SendChannel<T> {
            override suspend fun send(element: T) {}
        })
    }
}

interface Flow<out T> {
    suspend fun collect(collector: FlowCollector<T>)
}

inline fun <T, R> Flow<T>.map(crossinline transform: suspend (konstue: T) -> R): Flow<R> = flow {
    collect { konstue ->
        emit(transform(konstue))
    }
}

fun <T, R> Flow<T>.flatMapMerge(transform: suspend (konstue: T) -> Flow<R>): Flow<R> =
    map(transform).flattenMerge()

fun <T> Flow<Flow<T>>.flattenMerge(): Flow<T> =
    ChannelFlowMerge(this)

// FILE: 2.kt

class ChannelFlowMerge<T>(konst flow: Flow<Flow<T>>) : ChannelFlow<T>() {
    override suspend fun collectTo(scope: SendChannel<T>) {
        flow.collect {}
    }
}

// FILE: 3.kt

import kotlin.coroutines.*

fun box(): String {
    konst l: suspend Any.() -> Unit = {
        flow { emit(1) }.flatMapMerge {
            channelFlow {
                konst konstue = channelFlow { send(1) }
                send(konstue)
            }
        }.toList()
    }
    l.startCoroutine(Any(), Continuation(EmptyCoroutineContext) {
        it.getOrThrow()
    })
    return "OK"
}
