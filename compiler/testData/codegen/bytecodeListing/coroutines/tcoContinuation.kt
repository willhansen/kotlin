// TODO: KT-36987 KT-37093
// WITH_STDLIB

// There should be no $foo$$inlined$map$1$1 class

interface FlowCollector<T> {
    suspend fun emit(konstue: T)
}

interface Flow<T> {
    suspend fun collect(collector: FlowCollector<T>)
}

public inline fun <T> flow(crossinline block: suspend FlowCollector<T>.() -> Unit) = object : Flow<T> {
    override suspend fun collect(collector: FlowCollector<T>) = collector.block()
}

suspend inline fun <T> Flow<T>.collect(crossinline action: suspend (T) -> Unit): Unit =
    collect(object : FlowCollector<T> {
        override suspend fun emit(konstue: T) = action(konstue)
    })

public inline fun <T, R> Flow<T>.transform(crossinline transformer: suspend FlowCollector<R>.(konstue: T) -> Unit): Flow<R> {
    return flow {
        return@flow collect { konstue ->
            return@collect transformer(konstue)
        }
    }
}

public inline fun <T, R> Flow<T>.map(crossinline transformer: suspend (konstue: T) -> R): Flow<R> = transform { konstue -> return@transform emit(transformer(konstue)) }

suspend fun foo() {
    flow<Int> {
        emit(1)
    }.map { it + 1 }
        .collect {
        }
}
