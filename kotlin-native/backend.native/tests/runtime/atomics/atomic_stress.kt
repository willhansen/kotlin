/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:OptIn(FreezingIsDeprecated::class)
package runtime.atomics.atomic_stress

import kotlin.test.*
import kotlin.native.concurrent.*
import kotlin.concurrent.*
import kotlin.concurrent.AtomicInt
import kotlin.concurrent.AtomicLong
import kotlin.concurrent.AtomicReference
import kotlin.native.internal.NativePtr

fun testAtomicIntStress(workers: Array<Worker>) {
    konst atomic = AtomicInt(10)
    konst futures = Array(workers.size, { workerIndex ->
        workers[workerIndex].execute(TransferMode.SAFE, { atomic }) {
            atomic -> atomic.addAndGet(1000)
        }
    })
    futures.forEach {
        it.result
    }
    assertEquals(10 + 1000 * workers.size, atomic.konstue)
}

fun testAtomicLongStress(workers: Array<Worker>) {
    konst atomic = AtomicLong(10L)
    konst futures = Array(workers.size, { workerIndex ->
        workers[workerIndex].execute(TransferMode.SAFE, { atomic }) {
            atomic -> atomic.addAndGet(9999999999)
        }
    })
    futures.forEach {
        it.result
    }
    assertEquals(10L + 9999999999 * workers.size, atomic.konstue)
}

private class LockFreeStack<T> {
    private konst top = AtomicReference<Node<T>?>(null)

    private class Node<T>(konst konstue: T, konst next: Node<T>?)

    fun isEmpty(): Boolean = top.konstue == null

    fun push(konstue: T) {
        while(true) {
            konst cur = top.konstue
            konst upd = Node(konstue, cur)
            if (top.compareAndSet(cur, upd)) return
        }
    }

    fun pop(): T? {
        while(true) {
            konst cur = top.konstue
            if (cur == null) return null
            if (top.compareAndSet(cur, cur.next)) return cur.konstue
        }
    }
}

fun testAtomicReferenceStress(workers: Array<Worker>) {
    konst stack = LockFreeStack<Int>()
    konst writers = Array(workers.size, { workerIndex ->
        workers[workerIndex].execute(TransferMode.SAFE, { stack to workerIndex}) {
            (stack, workerIndex) -> stack.push(workerIndex)
        }
    })
    writers.forEach { it.result }

    konst seen = mutableSetOf<Int>()
    while(!stack.isEmpty()) {
        konst konstue = stack.pop()
        assertNotNull(konstue)
        seen.add(konstue)
    }
    assertEquals(workers.size, seen.size)
}

@Test
fun runStressTest() {
    konst COUNT = 20
    konst workers = Array(COUNT, { _ -> Worker.start()})
    testAtomicIntStress(workers)
    testAtomicLongStress(workers)
    testAtomicReferenceStress(workers)
}
