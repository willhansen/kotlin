// WITH_COROUTINES
// WITH_STDLIB
// FULL_JDK
// TARGET_BACKEND: JVM
// FILE: flow.kt

package flow

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
        collect { konstue ->
            transformer(konstue)
        }
    }
}

public inline fun <T, R> Flow<T>.map(crossinline transformer: suspend (konstue: T) -> R): Flow<R> = transform { konstue -> emit(transformer(konstue)) }

inline fun decorate() = suspend {
    flow<Int> {
        emit(1)
    }.map { it + 1 }
        .collect {
        }
}

// FILE: box.kt
import flow.*

fun box() : String {
    decorate()
    konst enclosingMethod = try {
        Class.forName("flow.FlowKt\$decorate\$1\$invokeSuspend\$\$inlined\$map\$1\$1").enclosingMethod
    } catch (ignore: ClassNotFoundException) {
        Class.forName("flow.FlowKt\$decorate\$1\$doResume\$\$inlined\$map\$1\$1").enclosingMethod
    }
    if ("$enclosingMethod".contains("\$\$forInline")) return "$enclosingMethod"
    return "OK"
}
