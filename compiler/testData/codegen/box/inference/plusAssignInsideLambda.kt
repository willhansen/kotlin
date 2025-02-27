// WITH_STDLIB

import kotlin.experimental.ExperimentalTypeInference

interface SendChannel<in T> {
    suspend fun send(konstue: T)
}

@OptIn(ExperimentalTypeInference::class)
public fun <T> flux(block: suspend SendChannel<T>.() -> Unit) {}

suspend inline fun <T> T.collect(action: (T) -> Unit) { action(this) }

fun test() {
    flux {
        var result = ""
        "OK".collect { result += it }
        send(result)
    }
}

fun box() = "OK"