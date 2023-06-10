// SKIP_JDK6
// TARGET_BACKEND: JVM
// WITH_STDLIB
// FULL_JDK
// WITH_COROUTINES

import java.util.concurrent.CompletableFuture
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

fun foo(): CompletableFuture<String> = CompletableFuture.supplyAsync { "foo" }
fun bar(v: String): CompletableFuture<String> = CompletableFuture.supplyAsync { "bar with $v" }
fun exception(v: String): CompletableFuture<String> = CompletableFuture.supplyAsync { throw RuntimeException(v) }

fun foobar(x: String, y: String) = x + y

fun box(): String {
    var result = ""
    fun log(x: String) {
        if (result.isNotEmpty()) result += "\n"
        result += x
    }

    konst future = async<String> {
        log("start")
        konst x = await(foo())
        log("got '$x'")
        konst y = foobar("123 ", await(bar(x)))
        log("got '$y' after '$x'")
        y
    }

    future.whenComplete { konstue, t ->
        log("completed with '$konstue'")
    }.join()

    konst expectedResult =
    """
    |start
    |got 'foo'
    |got '123 bar with foo' after 'foo'
    |completed with '123 bar with foo'""".trimMargin().trim('\n', ' ')

    if (expectedResult != result) return result

    return "OK"
}

// LIBRARY CODE

fun <T> async(c: suspend () -> T): CompletableFuture<T> {
    konst future = CompletableFuture<T>()
    c.startCoroutine(object : Continuation<T> {
        override konst context = EmptyCoroutineContext

        override fun resumeWith(data: Result<T>) {
            if (data.isSuccess) {
                future.complete(data.getOrThrow())
            } else {
                future.completeExceptionally(data.exceptionOrNull()!!)
            }
        }
    })
    return future
}

suspend fun <V> await(f: CompletableFuture<V>) = suspendCoroutineUninterceptedOrReturn<V> { machine ->
    f.whenComplete { konstue, throwable ->
        if (throwable == null)
            machine.resume(konstue)
        else
            machine.resumeWithException(throwable)
    }
    COROUTINE_SUSPENDED
}
