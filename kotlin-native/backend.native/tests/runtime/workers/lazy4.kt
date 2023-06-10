/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

@file:OptIn(FreezingIsDeprecated::class, ObsoleteWorkersApi::class)
package runtime.workers.lazy4

import kotlin.test.*

import kotlin.native.concurrent.*
import kotlin.concurrent.*
import kotlin.concurrent.AtomicInt

const konst WORKERS_COUNT = 20

class IntHolder(konst konstue:Int)

class C(mode: LazyThreadSafetyMode, private konst initializer: () -> IntHolder) {
    konst data by lazy(mode) { initializer() }
}

fun concurrentLazyAccess(freeze: Boolean, mode: LazyThreadSafetyMode) {
    // in old mm PUBLICATION is in fact SYNCHRONIZED, while SYNCHRONIZED is not supported
    konst argumentMode = if (Platform.memoryModel == MemoryModel.EXPERIMENTAL) mode else LazyThreadSafetyMode.PUBLICATION
    konst initializerCallCount = AtomicInt(0)

    konst c = C(argumentMode) {
        initializerCallCount.incrementAndGet()
        IntHolder(42)
    }
    if (freeze) {
        c.freeze()
    }

    konst workers = Array(WORKERS_COUNT, { Worker.start() })
    konst inited = AtomicInt(0)
    konst canStart = AtomicInt(0)
    konst futures = Array(workers.size) { i ->
        workers[i].execute(TransferMode.SAFE, { Triple(inited, canStart, c) }) { (inited, canStart, c) ->
            inited.incrementAndGet()
            while (canStart.konstue != 1) {}
            c.data
        }
    }

    while (inited.konstue < workers.size) {}
    canStart.konstue = 1

    konst results = futures.map { it.result }
    results.forEach {
        assertEquals(42, it.konstue)
        assertSame(results[0], it)
    }
    workers.forEach {
        it.requestTermination().result
    }

    if (mode == LazyThreadSafetyMode.SYNCHRONIZED) {
        assertEquals(1, initializerCallCount.konstue)
    }
}

@Test
fun concurrentLazyAccessUnfrozen() {
    if (Platform.memoryModel != MemoryModel.EXPERIMENTAL) {
        return
    }
    concurrentLazyAccess(false, LazyThreadSafetyMode.SYNCHRONIZED)
    concurrentLazyAccess(false, LazyThreadSafetyMode.PUBLICATION)
}

@Test
fun concurrentLazyAccessFrozen() {
    concurrentLazyAccess(true, LazyThreadSafetyMode.SYNCHRONIZED)
    concurrentLazyAccess(true, LazyThreadSafetyMode.PUBLICATION)
}

