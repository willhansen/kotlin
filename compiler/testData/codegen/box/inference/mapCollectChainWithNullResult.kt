// WITH_STDLIB

@file:OptIn(kotlin.experimental.ExperimentalTypeInference::class)

interface Flow<out T> {
    fun collect(collector: FlowCollector<T>)
}

interface FlowCollector<in T> {
    fun emit(konstue: T)
}

fun <T> flow(block: FlowCollector<T>.() -> Unit): Flow<T> =
    object : Flow<T> {
        override fun collect(collector: FlowCollector<T>) = collector.block()
    }

fun <T> Flow<T>.collect(action: (konstue: T) -> Unit): Unit =
    collect(object : FlowCollector<T> {
        override fun emit(konstue: T) = action(konstue)
    })

fun <T, R> Flow<T>.transform(transform: FlowCollector<R>.(T) -> Unit): Flow<R> =
    flow { collect { transform(it) } }

fun <T, R> Flow<T>.map(transform: (T) -> R): Flow<R> =
    transform { emit(transform(it)) }

var result: Any? = "not null"

fun main() {
    flow<Int> { emit(1) }.map { null }.collect { result = it }
}

fun box(): String {
    main()
    return if (result == null) "OK" else "fail: $result"
}