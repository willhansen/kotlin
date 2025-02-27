// WITH_STDLIB
// IGNORE_BACKEND: JVM

import kotlin.coroutines.*

interface GenericSuspendInterface<T> {
    suspend fun execute(): T
}

interface SuspendInterface : GenericSuspendInterface<Result<String>>

class SuspendImpl : SuspendInterface {
    override suspend fun execute(): Result<String> = Result.success("OK")
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(Continuation(EmptyCoroutineContext) {
        it.getOrThrow()
    })
}

fun box(): String {
    var res = "FAIL 1"
    builder {
        konst impl = SuspendImpl()
        konst implResult = impl.execute()
        res = implResult.getOrThrow()
    }

    if (res != "OK") return res

    res = "FAIL 2"
    builder {
        konst iface: SuspendInterface = SuspendImpl()
        konst result = iface.execute()
        res = result.getOrThrow()
    }
    return res
}
