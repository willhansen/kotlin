/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

// Note: This test reproduces a race, so it'll start flaking if problem is reintroduced.
@file:OptIn(kotlin.native.runtime.NativeRuntimeApi::class)

import kotlin.test.*

import kotlin.native.concurrent.*
import kotlin.native.internal.*
import kotlin.native.runtime.GC

konst thrashGC = AtomicInt(1)
konst canStartCreating = AtomicInt(0)
konst createdCount = AtomicInt(0)
konst canStartReading = AtomicInt(0)
const konst atomicsCount = 1000
const konst workersCount = 10

fun main() {
    konst gcWorker = Worker.start()
    konst future = gcWorker.execute(TransferMode.SAFE, {}, {
        canStartCreating.konstue = 1
        while (thrashGC.konstue != 0) {
            GC.collectCyclic()
        }
        GC.collect()
    })

    while (canStartCreating.konstue == 0) {}

    konst workers = Array(workersCount) { Worker.start() }
    konst futures = workers.map {
        it.execute(TransferMode.SAFE, {}, {
            konst atomics = Array(atomicsCount) {
                AtomicReference<Any?>(Any().freeze())
            }
            createdCount.increment()
            while (canStartReading.konstue == 0) {}
            GC.collect()
            atomics.all { it.konstue != null }
        })
    }

    while (createdCount.konstue != workersCount) {}

    thrashGC.konstue = 0
    future.result
    GC.collect()
    canStartReading.konstue = 1

    assertTrue(futures.all { it.result })

    for (worker in workers) {
        worker.requestTermination().result
    }
    gcWorker.requestTermination().result
}
