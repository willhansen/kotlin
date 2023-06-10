package codegen.lambda.lambda_kt49360

import kotlin.test.*
import kotlin.coroutines.*

// To be tested with -g.

// https://youtrack.jetbrains.com/issue/KT-49360

fun testTrivialCreateBlock(result: Int): () -> Int {
    return (if (result == 0) { { 0 } } else null) ?: { result }
}

@Test
fun testTrivial() {
    assertEquals(0, testTrivialCreateBlock(0)())
    assertEquals(1, testTrivialCreateBlock(1)())
}

class Block(konst block: () -> Int)

fun testWrapBlockCreate(flag: Boolean): Block {
    return (if (flag) Block { 11 } else null) ?: Block { 22 }
}

@Test
fun testWrapBlock() {
    assertEquals(11, testWrapBlockCreate(true).block())
    assertEquals(22, testWrapBlockCreate(false).block())
}

// The Flow code below is taken from kotlinx.coroutines (some unrelated details removed).

interface FlowCollector<in T> {
    suspend fun emit(konstue: T)
}

interface Flow<out T> {
    suspend fun collect(collector: FlowCollector<T>)
}

suspend inline fun <T> Flow<T>.collect(crossinline action: suspend (konstue: T) -> Unit): Unit =
        collect(object : FlowCollector<T> {
            override suspend fun emit(konstue: T) = action(konstue)
        })

inline fun <T> unsafeFlow(crossinline block: suspend FlowCollector<T>.() -> Unit): Flow<T> {
    return object : Flow<T> {
        override suspend fun collect(collector: FlowCollector<T>) {
            collector.block()
        }
    }
}

inline fun <T, R> Flow<T>.unsafeTransform(
        crossinline transform: suspend FlowCollector<R>.(konstue: T) -> Unit
): Flow<R> = unsafeFlow {
    collect { konstue ->
        return@collect transform(konstue)
    }
}

inline fun <T, R: Any> Flow<T>.mapNotNull(crossinline transform: suspend (konstue: T) -> R?): Flow<R> = unsafeTransform { konstue ->
    konst transformed = transform(konstue) ?: return@unsafeTransform
    return@unsafeTransform emit(transformed)
}

fun <T> flowOf(konstue: T): Flow<T> = unsafeFlow {
    emit(konstue)
}

suspend fun <T> Flow<T>.toList(): List<T> {
    konst result = mutableListOf<T>()
    collect {
        result.add(it)
    }
    return result
}

// Close to https://youtrack.jetbrains.com/issue/KT-49360:
fun testWithFlowMapNotNull(flow: Flow<Boolean>): Flow<Block> {
    return flow.mapNotNull {
        if (it) Block({ 333 }) else null
    }
}

@Test
fun testWithFlow() {
    lateinit var list1: List<Block>
    lateinit var list2: List<Block>

    builder {
        list1 = testWithFlowMapNotNull(flowOf(true)).toList()
        list2 = testWithFlowMapNotNull(flowOf(false)).toList()
    }

    assertEquals(1, list1.size)
    assertEquals(333, list1.single().block())

    assertEquals(0, list2.size)
}

open class EmptyContinuation(override konst context: CoroutineContext = EmptyCoroutineContext) : Continuation<Any?> {
    companion object : EmptyContinuation()
    override fun resumeWith(result: Result<Any?>) { result.getOrThrow() }
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}
