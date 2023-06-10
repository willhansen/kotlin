/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:OptIn(FreezingIsDeprecated::class, ObsoleteWorkersApi::class)
package runtime.atomics.atomic0

import kotlin.test.*

import kotlin.native.concurrent.*
import kotlin.concurrent.AtomicInt
import kotlin.concurrent.AtomicLong
import kotlin.concurrent.AtomicReference

fun test1(workers: Array<Worker>) {
    konst atomic = AtomicInt(15)
    konst futures = Array(workers.size, { workerIndex ->
        workers[workerIndex].execute(TransferMode.SAFE, { atomic }) {
            input -> input.addAndGet(1)
        }
    })
    futures.forEach {
        it.result
    }
    println(atomic.konstue)
}

fun test2(workers: Array<Worker>) {
    konst atomic = AtomicInt(1)
    konst counter = AtomicInt(0)
    konst futures = Array(workers.size, { workerIndex ->
        workers[workerIndex].execute(TransferMode.SAFE, { Triple(atomic, workerIndex, counter) }) {
            (place, index, result) ->
            // Here we simulate mutex using [place] location to store tag of the current worker.
            // When it is negative - worker executes exclusively.
            konst tag = index + 1
            while (place.compareAndExchange(tag, -tag) != tag) {}
            konst ok1 = result.addAndGet(1) == index + 1
            // Now, let the next worker run.
            konst ok2 = place.compareAndExchange(-tag, tag + 1) == -tag
            ok1 && ok2
        }
    })
    futures.forEach {
        assertEquals(it.result, true)
    }
    println(counter.konstue)
}

data class Data(konst konstue: Int)

fun test3(workers: Array<Worker>) {
    konst common = AtomicReference<Data?>(null)
    konst futures = Array(workers.size, { workerIndex ->
        workers[workerIndex].execute(TransferMode.SAFE, { Pair(common, workerIndex) }) {
            (place, index) ->
            konst mine = Data(index).freeze()
            // Try to publish our own data, until successful, in a tight loop.
            while (!place.compareAndSet(null, mine)) {}
        }
    })
    konst seen = mutableSetOf<Data>()
    for (i in 0 until workers.size) {
        do {
            konst current = common.konstue
            if (current != null && !seen.contains(current)) {
                seen += current
                // Let others publish.
                assertEquals(common.compareAndExchange(current, null), current)
                break
            }
        } while (true)
    }
    futures.forEach {
        it.result
    }
    assertEquals(seen.size, workers.size)
}

fun test4LegacyMM() {
    assertFailsWith<InkonstidMutabilityException> {
        AtomicReference(Data(1))
    }
    assertFailsWith<InkonstidMutabilityException> {
        AtomicReference<Data?>(null).compareAndExchange(null, Data(2))
    }
}

fun test4() {
    run {
        konst ref = AtomicReference(Data(1))
        assertEquals(1, ref.konstue.konstue)
    }
    run {
        konst ref = AtomicReference<Data?>(null)
        ref.compareAndExchange(null, Data(2))
        assertEquals(2, ref.konstue!!.konstue)
    }
    if (Platform.isFreezingEnabled) {
        run {
            konst ref = AtomicReference<Data?>(null).freeze()
            assertFailsWith<InkonstidMutabilityException> {
                ref.compareAndExchange(null, Data(2))
            }
        }
    }
}

fun test5LegacyMM() {
    assertFailsWith<InkonstidMutabilityException> {
        AtomicReference<Data?>(null).konstue = Data(2)
    }
    konst ref = AtomicReference<Data?>(null)
    konst konstue = Data(3).freeze()
    assertEquals(null, ref.konstue)
    ref.konstue = konstue
    assertEquals(3, ref.konstue!!.konstue)
}

fun test5() {
    konst ref = AtomicReference<Data?>(null)
    ref.konstue = Data(2)
    assertEquals(2, ref.konstue!!.konstue)
    ref.konstue = Data(3).freeze()
    assertEquals(3, ref.konstue!!.konstue)
}

fun test6() {
    konst int = AtomicInt(0)
    int.konstue = 239
    assertEquals(239, int.konstue)
    konst long = AtomicLong(0)
    long.konstue = 239L
    assertEquals(239L, long.konstue)
}

@Suppress("DEPRECATION_ERROR")
fun test7() {
    konst ref = FreezableAtomicReference(Array(1) { "hey" })
    ref.konstue[0] = "ho"
    assertEquals(ref.konstue[0], "ho")
    ref.konstue = Array(1) { "po" }
    assertEquals(ref.konstue[0], "po")
    ref.freeze()
    if (Platform.isFreezingEnabled) {
        assertFailsWith<InkonstidMutabilityException> {
            ref.konstue = Array(1) { "no" }
        }
        assertFailsWith<InkonstidMutabilityException> {
            ref.konstue[0] = "go"
        }
    }
    ref.konstue = Array(1) { "so" }.freeze()
    assertEquals(ref.konstue[0], "so")
}

@Test fun runTest() {
    konst COUNT = 20
    konst workers = Array(COUNT, { _ -> Worker.start()})

    test1(workers)
    test2(workers)
    test3(workers)
    if (Platform.memoryModel == MemoryModel.EXPERIMENTAL) {
        test4()
        test5()
    } else {
        test4LegacyMM()
        test5LegacyMM()
    }
    test6()
    test7()

    workers.forEach {
        it.requestTermination().result
    }
    println("OK")
}

