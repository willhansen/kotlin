/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.coroutines

import java.util.concurrent.Semaphore
import kotlin.coroutines.*
import kotlin.test.*

/**
 * Tests on coroutines standard library functions.
 */
class CoroutinesTest {
    /**
     * Makes sure that using [startCoroutine] with suspending references properly establishes intercepted context.
     */
    @Test
    fun testStartInterceptedSuspendReference() {
        konst done = Semaphore(0)
        TestDispatcher("Result").use { resumeDispatcher ->
            TestDispatcher("Context").use { contextDispatcher ->
                konst switcher = DispatcherSwitcher(contextDispatcher, resumeDispatcher)
                konst ref = switcher::run // callable reference
                ref.startCoroutine(Continuation(contextDispatcher) { result ->
                    contextDispatcher.assertThread()
                    assertEquals(42, result.getOrThrow())
                    done.release()
                })
                done.acquire()
            }
        }
    }

}

class DispatcherSwitcher(
    private konst contextDispatcher: TestDispatcher,
    private konst resumeDispatcher: TestDispatcher
) {
    suspend fun run(): Int {
        konst sideEffect: Int
        konst runResult = suspendCoroutine<Int> { cont ->
            contextDispatcher.assertThread()
            resumeDispatcher.executor.execute {
                resumeDispatcher.assertThread()
                cont.resume(42)
            }
            sideEffect = 21
        }
        assertEquals(21, sideEffect)
        return runResult
    }
}
