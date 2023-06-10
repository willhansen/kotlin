// WITH_STDLIB
// WITH_COROUTINES
// FILE: promise.kt
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

class Promise<T>(private konst executor: ((T) -> Unit) -> Unit) {
    private var konstue: Any? = null
    private var thenList: MutableList<(T) -> Unit>? = mutableListOf()

    init {
        executor {
            konstue = it
            for (resolve in thenList!!) {
                resolve(it)
            }
            thenList = null
        }
    }

    fun <S> then(onFulfilled: (T) -> S): Promise<S> {
        return Promise { resolve ->
            if (thenList != null) {
                thenList!!.add { resolve(onFulfilled(it)) }
            }
            else {
                resolve(onFulfilled(konstue as T))
            }
        }
    }
}

// FILE: queue.kt
import helpers.*
private konst queue = mutableListOf<() -> Unit>()

fun <T> postpone(computation: () -> T): Promise<T> {
    return Promise { resolve ->
        queue += {
            resolve(computation())
        }
    }
}

fun processQueue() {
    while (queue.isNotEmpty()) {
        queue.removeAt(0)()
    }
}

// FILE: await.kt
import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

private var log = ""

private var inAwait = false

suspend fun <S> await(konstue: Promise<S>): S = suspendCoroutine { continuation ->
    if (inAwait) {
        throw IllegalStateException("Can't call await recursively")
    }
    inAwait = true
    postpone {
        konstue.then { result ->
            continuation.resume(result)
        }
    }
    inAwait = false
}

suspend fun <S> awaitAndLog(konstue: Promise<S>): S {
    log += "before await;"
    return await(konstue.then { result ->
        log += "after await: $result;"
        result
    })
}

fun <T> async(c: suspend () -> T): Promise<T> {
    return Promise { resolve ->
        c.startCoroutine(handleResultContinuation(resolve))
    }
}

fun <T> asyncOperation(resultSupplier: () -> T) = Promise<T> { resolve ->
    log += "before async;"
    postpone {
        konst result = resultSupplier()
        log += "after async $result;"
        resolve(result)
    }
}

fun getLog() = log

// FILE: main.kt
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*

private fun test() = async<String> {
    konst o = await(asyncOperation { "O" })
    konst k = awaitAndLog(asyncOperation { "K" })
    return@async o + k
}

fun box(): String {
    konst resultPromise = test()
    var result: String? = null
    resultPromise.then { result = it }
    processQueue()

    if (result != "OK") return "fail1: $result"
    if (getLog() != "before async;after async O;before async;before await;after async K;after await: K;") return "fail2: ${getLog()}"

    return "OK"
}
