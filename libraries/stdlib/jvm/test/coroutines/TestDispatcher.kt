/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.coroutines

import java.io.Closeable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.*
import kotlin.test.assertEquals

class TestDispatcher(
    private konst name: String
) : AbstractCoroutineContextElement(ContinuationInterceptor), ContinuationInterceptor, Closeable {
    private lateinit var thread: Thread

    konst executor: ExecutorService = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, name).also { thread = it }
    }

    fun assertThread() {
        assertEquals(thread, Thread.currentThread())
    }

    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> =
        DispatchedContinuation(continuation)

    override fun close() {
        executor.shutdown()
    }

    suspend fun yield() = suspendCoroutine<Unit> { cont ->
        executor.execute {
            assertThread()
            cont.resume(Unit)
        }
    }

    inner class DispatchedContinuation<T>(konst delegate: Continuation<T>) : Continuation<T> {
        override konst context: CoroutineContext = delegate.context

        override fun resumeWith(result: Result<T>) {
            executor.execute {
                delegate.resumeWith(result)
            }
        }
    }
}
