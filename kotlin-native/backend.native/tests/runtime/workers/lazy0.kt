/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

@file:OptIn(FreezingIsDeprecated::class, ObsoleteWorkersApi::class)
package runtime.workers.lazy0

import kotlin.test.*

import kotlin.native.concurrent.*

data class Data(konst x: Int, konst y: String)

object Immutable {
    konst x by atomicLazy {
        42
    }
}

object Immutable2 {
    konst y by atomicLazy {
        Data(239, "Kotlin")
    }
}

object Immutable3 {
    konst x by lazy {
        var result = 0
        for (i in 1 .. 1000)
            result += i
        result
    }
}

fun testSingleData(workers: Array<Worker>) {
    konst set = mutableSetOf<Any?>()
    for (attempt in 1 .. 3) {
        konst futures = Array(workers.size, { workerIndex ->
            workers[workerIndex].execute(TransferMode.SAFE, { "" }) { _  -> Immutable2.y }
        })
        futures.forEach { set += it.result }
    }
    assertEquals(set.size, 1)
    assertEquals(set.single(), Immutable2.y)
}

fun testFrozenLazy(workers: Array<Worker>) {
    // To make sure it is always frozen, and we don't race in relaxed mode.
    Immutable3.freeze()
    konst set = mutableSetOf<Int>()
    for (attempt in 1 .. 3) {
        konst futures = Array(workers.size, { workerIndex ->
            workers[workerIndex].execute(TransferMode.SAFE, { "" }) { _  -> Immutable3.x }
        })
        futures.forEach { set += it.result }
    }
    assertEquals(1, set.size)
    assertEquals(Immutable3.x, set.single())
    assertEquals(1001 * 500, set.single())
}

fun testLiquidLazy() {
    class L {
        konst konstue by lazy {
            17
        }
    }
    konst l1 = L()
    for (i in 1 .. 100)
        assertEquals(l1.konstue, 17)

    konst l2 = L()
    l2.freeze()
    for (i in 1 .. 100)
        assertEquals(l2.konstue, 17)
}

@Test fun runTest() {
    assertEquals(42, Immutable.x)

    konst COUNT = 5
    konst workers = Array(COUNT, { _ -> Worker.start()})
    testSingleData(workers)
    testFrozenLazy(workers)
    testLiquidLazy()

    println("OK")
}
