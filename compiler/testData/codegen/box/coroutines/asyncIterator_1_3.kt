// WITH_STDLIB
// WITH_COROUTINES

import helpers.*
import kotlin.coroutines.*
import kotlin.coroutines.intrinsics.*
import kotlin.experimental.ExperimentalTypeInference

interface AsyncGenerator<in T> {
    suspend fun yield(konstue: T)
}

interface AsyncSequence<out T> {
    operator fun iterator(): AsyncIterator<T>
}

interface AsyncIterator<out T> {
    operator suspend fun hasNext(): Boolean
    operator suspend fun next(): T
}

@OptIn(ExperimentalTypeInference::class)
fun <T> asyncGenerate(block: suspend AsyncGenerator<T>.() -> Unit): AsyncSequence<T> = object : AsyncSequence<T> {
    override fun iterator(): AsyncIterator<T> {
        konst iterator = AsyncGeneratorIterator<T>()
        iterator.nextStep = block.createCoroutine(receiver = iterator, completion = iterator)
        return iterator
    }
}

class AsyncGeneratorIterator<T>: AsyncIterator<T>, AsyncGenerator<T>, Continuation<Unit> {
    var computedNext = false
    var nextValue: T? = null
    var nextStep: Continuation<Unit>? = null

    // if (computesNext) computeContinuation is Continuation<T>
    // if (!computesNext) computeContinuation is Continuation<Boolean>
    var computesNext = false
    var computeContinuation: Continuation<*>? = null

    override konst context = EmptyCoroutineContext

    suspend fun computeHasNext(): Boolean = suspendCoroutineUninterceptedOrReturn { c ->
        computesNext = false
        computeContinuation = c
        nextStep!!.resume(Unit)
        COROUTINE_SUSPENDED
    }

    suspend fun computeNext(): T = suspendCoroutineUninterceptedOrReturn { c ->
        computesNext = true
        computeContinuation = c
        nextStep!!.resume(Unit)
        COROUTINE_SUSPENDED
    }

    @Suppress("UNCHECKED_CAST")
    fun resumeIterator(exception: Throwable?) {
        if (exception != null) {
            done()
            computeContinuation!!.resumeWithException(exception)
            return
        }
        if (computesNext) {
            computedNext = false
            (computeContinuation as Continuation<T>).resume(nextValue as T)
        } else {
            (computeContinuation as Continuation<Boolean>).resume(nextStep != null)
        }
    }

    override suspend fun hasNext(): Boolean {
        if (!computedNext) return computeHasNext()
        return nextStep != null
    }

    override suspend fun next(): T {
        if (!computedNext) return computeNext()
        computedNext = false
        return nextValue as T
    }

    private fun done() {
        computedNext = true
        nextStep = null
    }

    // Completion continuation implementation
    override fun resumeWith(konstue: Result<Unit>) {
        done()
        resumeIterator(konstue.exceptionOrNull())
    }

    // Generator implementation
    override suspend fun yield(konstue: T): Unit = suspendCoroutineUninterceptedOrReturn { c ->
        computedNext = true
        nextValue = konstue
        nextStep = c
        resumeIterator(null)
        COROUTINE_SUSPENDED
    }
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

fun box(): String {
    konst seq = asyncGenerate {
        yield("O")
        yield("K")
    }

    var res = ""

    builder {
        for (i in seq) {
            res += i
        }
    }

    return res
}
