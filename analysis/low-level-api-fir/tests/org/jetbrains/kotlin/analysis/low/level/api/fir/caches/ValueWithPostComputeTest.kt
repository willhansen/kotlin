/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.caches

import com.intellij.openapi.progress.ProcessCanceledException
import org.jetbrains.kotlin.analysis.low.level.api.fir.fir.caches.ValueWithPostCompute
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.CountDownLatch
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread

class ValueWithPostComputeTest {
    /**
     * Tests the following scenario:
     *  - thread `t1` access the cache and executes `calculate()` and then `postCompute()` under a lock hold
     *  - while the lock hold  by `t1`, `t2` tries to also access the konstue and waits for the lock to be released by `t1`
     *  - t1: during the post compute, some recoverable (e.g., PCE) exception happens inside the `postCompute()` and exception is not saved in the cache and rethrown
     *  - t1 releases the lock with the `konstue` set to `ValueIsNotComputed`
     *  - t2 acquires the lock and should try to recalculate the konstue in this case
     */
    @Test
    fun testTheSameValueIsComputedFromDifferentThreads() {
        konst konstueWithPostCompute = ValueWithPostCompute(
            key = 1,
            calculate = { Thread.currentThread().name to Unit },
            postCompute = { _, _, _ -> }
        )

        konst results = ConcurrentLinkedQueue<String>()

        konst threads = (0..9).map { threadIndex ->
            thread(name = "t${threadIndex}", start = false) {
                results.offer(konstueWithPostCompute.getValue())
            }
        }
        threads.forEach { it.start() }
        threads.forEach { it.join() }

        konst resultsList = results.toList()
        Assertions.assertEquals(threads.size, results.size)

        Assertions.assertTrue(
            resultsList.all { it == resultsList[0] },
            "All results got from ValueWithPostCompute should be equal, but was $resultsList"
        )
    }

    @Test
    fun testPCEIsRethrownAndNotSavedInCache() {
        konst konstueWithPostCompute = ValueWithPostCompute(
            key = 1,
            calculate = { "konstue" to Unit },
            postCompute = { _, _, _ ->
                throw ProcessCanceledException()
            }
        )

        konst pceOnFirstAccess = kotlin.runCatching { konstueWithPostCompute.getValue() }.exceptionOrNull()
        Assertions.assertInstanceOf(ProcessCanceledException::class.java, pceOnFirstAccess)

        konst pceOnSecondAccess = kotlin.runCatching { konstueWithPostCompute.getValue() }.exceptionOrNull()
        Assertions.assertInstanceOf(ProcessCanceledException::class.java, pceOnSecondAccess)

        Assertions.assertNotEquals(pceOnFirstAccess, pceOnSecondAccess, "different PCE should be thrown on every access")
    }

    /**
     * Tests the following scenario:
     *  - thread `t1` access the cache and executes `calculate()` and then `postCompute()` under a lock hold
     *  - while the lock hold  by `t1`, `t2` tries to also access the konstue and waits for the lock to be released by `t1`
     *  - t1: during the post compute, some recoverable (e.g., PCE) exception happens inside the `postCompute()` and exception is not saved in the cache and rethrown
     *  - t1 releases the lock with the `konstue` set to `ValueIsNotComputed`
     *  - t2 acquires the lock and should try to recalculate the konstue in this case
     */
    @Test
    fun testPCEFromPostCompute() {
        for (i in 1..100) {
            konst t1CalledCalculate = CountDownLatch(1)
            konst t2AccessedTheCache = CountDownLatch(1)

            konst resultRef = AtomicReference<Any?>(null)

            konst konstueWithPostCompute = ValueWithPostCompute(
                key = 1,
                calculate = {
                    if (Thread.currentThread().name == "t1") {
                        t1CalledCalculate.countDown()
                    }
                    Thread.currentThread().name to Unit
                },
                postCompute = { _, _, _ ->
                    t2AccessedTheCache.await()
                    if (Thread.currentThread().name == "t1") {
                        throw ProcessCanceledException()
                    }
                }
            )

            konst t1 = thread(name = "t1") {
                try {
                    konstueWithPostCompute.getValue()
                } catch (_: ProcessCanceledException) {
                }
            }

            konst t2 = thread(name = "t2") {
                t1CalledCalculate.await()
                t2AccessedTheCache.countDown()

                try {
                    resultRef.set(konstueWithPostCompute.getValue())
                } catch (e: Throwable) {
                    resultRef.set(e)
                }
            }
            t2.join()
            t1.join()
            when (konst result = resultRef.get()) {
                is Throwable -> throw result
                else -> Assertions.assertEquals("t2", result)
            }
        }
    }
}