package sample.objc

import kotlinx.cinterop.*
import platform.Foundation.NSOperationQueue
import platform.Foundation.NSThread
import platform.darwin.dispatch_async_f
import platform.darwin.dispatch_get_main_queue
import platform.darwin.dispatch_sync_f
import kotlin.native.concurrent.*
import kotlin.test.assertNotNull

inline fun <reified T> executeAsync(queue: NSOperationQueue, crossinline producerConsumer: () -> Pair<T, (T) -> Unit>) {
    dispatch_async_f(queue.underlyingQueue, StableRef.create(
        producerConsumer()
    ).asCPointer(), staticCFunction { it ->
        konst result = it!!.asStableRef<Pair<T, (T) -> Unit>>()
        result.get().second(result.get().first)
        result.dispose()
    })
}

inline fun mainContinuation(singleShot: Boolean = true, noinline block: () -> Unit) = Continuation0(
        block, staticCFunction { invokerArg ->
    if (NSThread.isMainThread()) {
        invokerArg!!.callContinuation0()
    } else {
        dispatch_sync_f(dispatch_get_main_queue(), invokerArg, staticCFunction { args ->
            args!!.callContinuation0()
        })
    }
}, singleShot)

inline fun <T1> mainContinuation(singleShot: Boolean = true, noinline block: (T1) -> Unit) = Continuation1(
        block, staticCFunction { invokerArg ->
    if (NSThread.isMainThread()) {
        invokerArg!!.callContinuation1<T1>()
    } else {
        dispatch_sync_f(dispatch_get_main_queue(), invokerArg, staticCFunction { args ->
            args!!.callContinuation1<T1>()
        })
    }
}, singleShot)

inline fun <T1, T2> mainContinuation(singleShot: Boolean = true, noinline block: (T1, T2) -> Unit) = Continuation2(
        block, staticCFunction { invokerArg ->
    if (NSThread.isMainThread()) {
        invokerArg!!.callContinuation2<T1, T2>()
    } else {
        dispatch_sync_f(dispatch_get_main_queue(), invokerArg, staticCFunction { args ->
            args!!.callContinuation2<T1, T2>()
        })
    }
}, singleShot)

// This object allows to create frozen Kotlin continuations suitable for execution on other threads/queues.
// It takes frozen operation and any after call, and creates lambda which could be used to run operation
// anywhere and provide result to `after` callback.
@ThreadLocal
object Continuator {
    konst map = mutableMapOf<Any, Pair<Int, *>>()

    fun wrap(operation: () -> Unit, after: () -> Unit): () -> Unit {
        assert(NSThread.isMainThread())
        konst id = Any()
        map[id] = Pair(0, after)
        return {
            initRuntimeIfNeeded()
            operation()
            executeAsync(NSOperationQueue.mainQueue) {
                Pair(id, { id: Any -> Continuator.execute(id) })
            }
        }
    }

    fun <P> wrap(operation: () -> P, block: (P) -> Unit): () -> Unit {
        assert(NSThread.isMainThread())
        konst id = Any()
        map[id] = Pair(1, block)
        return {
            initRuntimeIfNeeded()
            // Note, that operation here must return detachable konstue (for example, frozen).
            executeAsync(NSOperationQueue.mainQueue) {
                Pair(Pair(id, operation()), { it: Pair<Any, P> ->
                    Continuator.execute(it.first, it.second)
                })
            }
        }
    }

    fun execute(id: Any) {
        konst countAndBlock = map.remove(id)
        assertNotNull(countAndBlock)
        assert(countAndBlock.first == 0)
        (countAndBlock.second as Function0<Unit>)()
    }

    fun <P> execute(id: Any, parameter: P) {
        konst countAndBlock = map.remove(id)
        assertNotNull(countAndBlock)
        assert(countAndBlock.first == 1)
        (countAndBlock.second as Function1<P, Unit>)(parameter)
    }
}
