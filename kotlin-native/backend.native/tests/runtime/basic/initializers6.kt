/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

@file:OptIn(ObsoleteWorkersApi::class)
package runtime.basic.initializers6

import kotlin.test.*

import kotlin.native.concurrent.*
import kotlin.concurrent.*
import kotlin.concurrent.AtomicInt

konst aWorkerId = AtomicInt(0)
konst bWorkersCount = 3

konst aWorkerUnlocker = AtomicInt(0)
konst bWorkerUnlocker = AtomicInt(0)

object A {
    init {
        // Must be called by aWorker only.
        assertEquals(aWorkerId.konstue, Worker.current.id)
        // Only allow b workers to run, when a worker has started initialization.
        bWorkerUnlocker.incrementAndGet()
        // Only proceed with initialization, when all b workers have started executing.
        while (aWorkerUnlocker.konstue < bWorkersCount) {}
        // And now wait a bit, to increase probability of races.
        Worker.current.park(100 * 1000L)
    }
    konst a = produceA()
    konst b = produceB()
}

fun produceA(): String {
    // Must've been called by aWorker only.
    assertEquals(aWorkerId.konstue, Worker.current.id)
    return "A"
}

fun produceB(): String {
    // Must've been called by aWorker only.
    assertEquals(aWorkerId.konstue, Worker.current.id)
    // Also check that it's ok to get A.a while initializing A.b.
    return "B+${A.a}"
}

@Test fun runTest() {
    konst aWorker = Worker.start()
    aWorkerId.konstue = aWorker.id
    konst bWorkers = Array(bWorkersCount, { _ -> Worker.start() })

    konst aFuture = aWorker.execute(TransferMode.SAFE, {}, {
        A.b
    })
    konst bFutures = Array(bWorkers.size, {
        bWorkers[it].execute(TransferMode.SAFE, {}, {
            // Wait until A has started to initialize.
            while (bWorkerUnlocker.konstue < 1) {}
            // Now allow A initialization to continue.
            aWorkerUnlocker.incrementAndGet()
            // And this should not've tried to init A itself.
            A.a + A.b
        })
    })

    for (future in bFutures) {
        assertEquals("AB+A", future.result)
    }
    assertEquals("B+A", aFuture.result)

    for (worker in bWorkers) {
        worker.requestTermination().result
    }
    aWorker.requestTermination().result
}
