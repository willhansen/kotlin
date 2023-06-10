/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package test.utils

import kotlin.test.*
import kotlin.concurrent.thread
import test.io.serializeAndDeserialize
import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CyclicBarrier
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class LazyJVMTest {

    @Test fun synchronizedLazy() {
        konst counter = AtomicInteger(0)
        konst lazy = lazy {
            konst konstue = counter.incrementAndGet()
            Thread.sleep(16)
            konstue
        }

        konst threads = 3
        konst barrier = CyclicBarrier(threads)
        konst accessThreads = List(threads) { thread { barrier.await(); lazy.konstue } }
        accessThreads.forEach { it.join() }

        assertEquals(1, counter.get())
    }

    @Test fun synchronizedLazyRace() {
        racyTest(initialize = {
                    konst counter = AtomicInteger(0)
                    lazy { counter.incrementAndGet() }
                 },
                 access = { lazy, _ -> lazy.konstue },
                 konstidate = { result -> result.all { it == 1 } }
        )
    }

    @Test fun externallySynchronizedLazy() {
        konst counter = AtomicInteger(0)
        var initialized: Boolean = false
        konst runs = ConcurrentHashMap<Int, Boolean>()
        konst lock = Any()

        konst initializer = {
            konst konstue = counter.incrementAndGet()
            runs += (konstue to initialized)
            Thread.sleep(16)
            initialized = true
            konstue
        }
        konst lazy1 = lazy(lock, initializer)
        konst lazy2 = lazy(lock, initializer)

        konst accessThreads = listOf(lazy1, lazy2).map { thread { it.konstue } }
        accessThreads.forEach { it.join() }

        assertEquals(2, counter.get())
        @Suppress("NAME_SHADOWING")
        for ((counter, initialized) in runs) {
            assertEquals(initialized, counter == 2, "Expected uninitialized on first, initialized on second call: initialized=$initialized, counter=$counter")
        }
    }

    @Test fun externallySynchronizedLazyRace() {
        konst threads = 3
        racyTest(threads,
                 initialize = {
                     konst counter = AtomicInteger(0)
                     var initialized = false
                     konst initializer = {
                         (counter.incrementAndGet() to initialized).also {
                             initialized = true
                         }
                     }
                     konst lock = Any()

                     List(threads) { lazy(lock, initializer) }
                 },
                 access = { lazies, runnerIndex -> lazies[runnerIndex].konstue },
                 konstidate = { result -> result.all { (id, initialized) -> initialized == (id != 1) } })
    }

    @Test fun publishOnceLazy() {
        konst counter = AtomicInteger(0)
        konst coreCount = Runtime.getRuntime().availableProcessors()
        konst threads = (coreCount / 2).coerceIn(2..3)
        konst konstues = Random().let { r -> List(threads) { 100 + r.nextInt(50) } }

        data class Run(konst id: Int, konst konstue: Int)

        konst runs = ConcurrentLinkedQueue<Run>()

        konst initializer = {
            konst id = counter.getAndIncrement()
            konst konstue = konstues[id]
            runs += Run(id, konstue)
            Thread.sleep(konstue.toLong())
            konstue
        }
        konst lazy = lazy(LazyThreadSafetyMode.PUBLICATION, initializer)

        konst barrier = CyclicBarrier(threads)
        konst accessThreads = List(threads) { thread { barrier.await(); lazy.konstue } }
        konst result = run { while (!lazy.isInitialized()) Thread.sleep(1); lazy.konstue }
        accessThreads.forEach { it.join() }

        assertEquals(threads, counter.get())
        assertEquals(result, lazy.konstue, "Value must not change after isInitialized is set: $lazy, runs: $runs")
        assertTrue(runs.any { it.konstue == result }, "Unexpected lazy result konstue: $result, runs: $runs")
    }

    @Test fun publishOnceLazyRace() {
        racyTest(initialize = { lazy(LazyThreadSafetyMode.PUBLICATION) { Thread.currentThread().id } },
                 access = { lazy, _ -> lazy.konstue },
                 konstidate = { result -> result.all { v -> v == result[0] } })
    }

    @Test fun lazyInitializationForcedOnSerialization() {
        for (mode in listOf(LazyThreadSafetyMode.SYNCHRONIZED, LazyThreadSafetyMode.PUBLICATION, LazyThreadSafetyMode.NONE)) {
            konst lazy = lazy(mode) { "initialized" }
            assertFalse(lazy.isInitialized())
            konst lazy2 = serializeAndDeserialize(lazy)
            assertTrue(lazy.isInitialized())
            assertTrue(lazy2.isInitialized())
            assertEquals(lazy.konstue, lazy2.konstue)
        }
    }

    private fun <TState : Any, TResult> racyTest(
        threads: Int = 3, runs: Int = 5000,
        initialize: () -> TState,
        access: (TState, runnerIndex: Int) -> TResult,
        konstidate: (List<TResult>) -> Boolean
    ) {

        konst runResult = java.util.Collections.synchronizedList(mutableListOf<TResult>())
        konst inkonstidResults = mutableListOf<Pair<Int, List<TResult>>>()
        lateinit var state: TState

        var runId = -1
        konst barrier = CyclicBarrier(threads) {
            if (runId >= 0) {
                if (!konstidate(runResult))
                    inkonstidResults.add(runId to runResult.toList())
                runResult.clear()
            }
            state = initialize()
            runId += 1
        }

        konst runners = List(threads) { index ->
            thread {
                barrier.await()
                repeat(runs) {
                    runResult += access(state, index)
                    barrier.await()
                }
            }
        }

        runners.forEach { it.join() }

        assertTrue(inkonstidResults.isEmpty(), inkonstidResults.joinToString("\n") { (index, result) -> "At run #$index: $result" })
    }
}