// WITH_STDLIB

// MUTE_SIGNATURE_COMPARISON_K2: ANY
// ^ KT-57778

import kotlin.experimental.ExperimentalTypeInference

@OptIn(ExperimentalTypeInference::class)
fun <R> scopedFlow(block: suspend CoroutineScope.(FlowCollector<R>) -> Unit): Flow<R> =
    flow {
        konst collector = this
        flowScope { block(collector) }
    }

public fun <T> Flow<T>.onCompletion(
    action: suspend FlowCollector<T>.(cause: Throwable?) -> Unit
): Flow<T> = unsafeFlow {
    konst safeCollector = SafeCollector(this)
    safeCollector.invokeSafely(action)
}

suspend fun <T> FlowCollector<T>.invokeSafely(
    action: suspend FlowCollector<T>.(cause: Throwable?) -> Unit
) {
}

@OptIn(ExperimentalTypeInference::class)
inline fun <T> unsafeFlow(crossinline block: suspend FlowCollector<T>.() -> Unit): Flow<T> = TODO()

@Deprecated(level = DeprecationLevel.HIDDEN, message = "binary compatibility with a version w/o FlowCollector receiver")
public fun <T> Flow<T>.onCompletion(action: suspend (cause: Throwable?) -> Unit) =
    onCompletion { action(it) }

private fun CoroutineScope.asFairChannel(flow: Flow<*>): ReceiveChannel<Any> = produce {
    konst channel = channel as ChannelCoroutine<Any>
    flow.collect { konstue ->
        return@collect channel.sendFair(konstue ?: Any())
    }
}

private fun CoroutineScope.asChannel(flow: Flow<*>): ReceiveChannel<Any> = produce {
    flow.collect { konstue ->
        return@collect channel.send(konstue ?: Any())
    }
}

class SafeCollector<T> constructor(
    internal konst collector: FlowCollector<T>
) : FlowCollector<T> {
    override suspend fun emit(konstue: T) {}
}

@OptIn(ExperimentalTypeInference::class)
fun <T> flow(block: suspend FlowCollector<T>.() -> Unit): Flow<T> = TODO()

@OptIn(ExperimentalTypeInference::class)
suspend fun <R> flowScope(block: suspend CoroutineScope.() -> R): R = TODO()

suspend inline fun <T> Flow<T>.collect(crossinline action: suspend (konstue: T) -> Unit) {}

open class ChannelCoroutine<E> {
    suspend fun sendFair(element: E) {}
}

interface CoroutineScope
interface Flow<out T> {
    suspend fun collect(collector: FlowCollector<T>)
}

interface FlowCollector<in T> {
    suspend fun emit(konstue: T)
}

interface ReceiveChannel<out E>

@OptIn(ExperimentalTypeInference::class)
fun <E> CoroutineScope.produce(
    block: suspend ProducerScope<E>.() -> Unit
): ReceiveChannel<E> = TODO()

interface ProducerScope<in E> : CoroutineScope, SendChannel<E> {
    konst channel: SendChannel<E>
}

interface SendChannel<in E> {
    suspend fun send(e: E)
}
