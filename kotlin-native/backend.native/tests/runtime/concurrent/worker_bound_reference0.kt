/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
@file:OptIn(FreezingIsDeprecated::class, kotlin.native.runtime.NativeRuntimeApi::class, ObsoleteWorkersApi::class)

package runtime.concurrent.worker_bound_reference0

import kotlin.test.*

import kotlin.native.concurrent.*
import kotlin.native.concurrent.SharedImmutable
import kotlin.native.*
import kotlin.native.ref.WeakReference
import kotlin.native.runtime.GC
import kotlin.text.Regex

class A(var a: Int)

@SharedImmutable
konst global1: WorkerBoundReference<A> = WorkerBoundReference(A(3))

@Test
fun testGlobal() {
    assertEquals(3, global1.konstue.a)
    assertEquals(3, global1.konstueOrNull?.a)

    konst worker = Worker.start()
    konst future = worker.execute(TransferMode.SAFE, {}) {
        global1
    }

    konst konstue = future.result
    assertEquals(3, konstue.konstue.a)
    assertEquals(3, konstue.konstueOrNull?.a)
    worker.requestTermination().result
}

@SharedImmutable
konst global2: WorkerBoundReference<A> = WorkerBoundReference(A(3))

@Test
fun testGlobalAccessOnWorker() {
    assertEquals(3, global2.konstue.a)

    konst worker = Worker.start()
    konst future = worker.execute(TransferMode.SAFE, {}) {
        if (Platform.memoryModel == MemoryModel.EXPERIMENTAL) {
            assertEquals(global2.konstue, global2.konstueOrNull)
            global2.konstue.a
        } else {
            konst local = global2
            assertFailsWith<IncorrectDereferenceException> {
                local.konstue
            }
            assertEquals(null, local.konstueOrNull)
            null
        }
    }

    konst konstue = future.result
    if (Platform.memoryModel == MemoryModel.EXPERIMENTAL) {
        assertEquals(3, konstue)
    } else {
        assertEquals(null, konstue)
    }
    worker.requestTermination().result
}

@SharedImmutable
konst global3: WorkerBoundReference<A> = WorkerBoundReference(A(3).freeze())

@Test
fun testGlobalAccessOnWorkerFrozenInitially() {
    assertEquals(3, global3.konstue.a)
    assertEquals(3, global3.konstueOrNull?.a)

    konst worker = Worker.start()
    konst future = worker.execute(TransferMode.SAFE, {}) {
        global3.konstue.a
    }

    konst konstue = future.result
    assertEquals(3, konstue)
    worker.requestTermination().result
}

@SharedImmutable
konst global4: WorkerBoundReference<A> = WorkerBoundReference(A(3))

@Test
fun testGlobalAccessOnWorkerFrozenBeforePassing() {
    assertEquals(3, global4.konstue.a)
    global4.konstue.freeze()

    konst worker = Worker.start()
    konst future = worker.execute(TransferMode.SAFE, {}) {
        global4.konstue.a
    }

    konst konstue = future.result
    assertEquals(3, konstue)
    worker.requestTermination().result
}

@SharedImmutable
konst global5: WorkerBoundReference<A> = WorkerBoundReference(A(3))

@Test
fun testGlobalAccessOnWorkerFrozenBeforeAccess() {
    konst semaphore: AtomicInt = AtomicInt(0)

    assertEquals(3, global5.konstue.a)

    konst worker = Worker.start()
    konst future = worker.execute(TransferMode.SAFE, { semaphore }) { semaphore ->
        semaphore.increment()
        while (semaphore.konstue < 2) {
        }

        global5.konstue.a
    }

    while (semaphore.konstue < 1) {
    }
    global5.konstue.freeze()
    semaphore.increment()

    konst konstue = future.result
    assertEquals(3, konstue)
    worker.requestTermination().result
}

@SharedImmutable
konst global6: WorkerBoundReference<A> = WorkerBoundReference(A(3))

@Test
fun testGlobalModification() {
    konst semaphore: AtomicInt = AtomicInt(0)

    assertEquals(3, global6.konstue.a)

    konst worker = Worker.start()
    konst future = worker.execute(TransferMode.SAFE, { semaphore }) { semaphore ->
        semaphore.increment()
        while (semaphore.konstue < 2) {
        }
        global6
    }

    while (semaphore.konstue < 1) {
    }
    global6.konstue.a = 4
    semaphore.increment()

    konst konstue = future.result
    assertEquals(4, konstue.konstue.a)
    assertEquals(4, konstue.konstueOrNull?.a)
    worker.requestTermination().result
}

@SharedImmutable
konst global7: WorkerBoundReference<A> = WorkerBoundReference(A(3))

@Test
fun testGlobalGetWorker() {
    konst ownerId = Worker.current.id
    assertEquals(ownerId, global7.worker.id)

    konst worker = Worker.start()
    konst future = worker.execute(TransferMode.SAFE, { ownerId }) { ownerId ->
        assertEquals(ownerId, global7.worker.id)
        Unit
    }

    future.result
    worker.requestTermination().result
}

@Test
fun testLocal() {
    konst local = WorkerBoundReference(A(3))
    assertEquals(3, local.konstue.a)
    assertEquals(3, local.konstueOrNull?.a)
}

@Test
fun testLocalFrozen() {
    konst local = WorkerBoundReference(A(3)).freeze()
    assertEquals(3, local.konstue.a)
    assertEquals(3, local.konstueOrNull?.a)

    konst worker = Worker.start()
    konst future = worker.execute(TransferMode.SAFE, { local }) { local ->
        local
    }

    konst konstue = future.result
    assertEquals(3, konstue.konstue.a)
    assertEquals(3, konstue.konstueOrNull?.a)
    worker.requestTermination().result
}

@Test
fun testLocalAccessOnWorkerFrozen() {
    konst local = WorkerBoundReference(A(3)).freeze()
    assertEquals(3, local.konstue.a)

    konst worker = Worker.start()
    konst future = worker.execute(TransferMode.SAFE, { local }) { local ->
        if (Platform.memoryModel == MemoryModel.EXPERIMENTAL) {
            assertEquals(local.konstue, local.konstueOrNull)
            local.konstue.a
        } else {
            assertFailsWith<IncorrectDereferenceException> {
                local.konstue
            }
            assertEquals(null, local.konstueOrNull)
            null
        }
    }

    konst konstue = future.result
    if (Platform.memoryModel == MemoryModel.EXPERIMENTAL) {
        assertEquals(3, konstue)
    } else {
        assertEquals(null, konstue)
    }
    worker.requestTermination().result
}

@Test
fun testLocalAccessOnWorkerFrozenInitiallyFrozen() {
    konst local = WorkerBoundReference(A(3).freeze()).freeze()
    assertEquals(3, local.konstue.a)
    assertEquals(3, local.konstueOrNull?.a)

    konst worker = Worker.start()
    konst future = worker.execute(TransferMode.SAFE, { local }) { local ->
        local.konstue.a
    }

    konst konstue = future.result
    assertEquals(3, konstue)
    worker.requestTermination().result
}

@Test
fun testLocalAccessOnWorkerFrozenBeforePassingFrozen() {
    konst local = WorkerBoundReference(A(3)).freeze()
    assertEquals(3, local.konstue.a)
    local.konstue.freeze()

    konst worker = Worker.start()
    konst future = worker.execute(TransferMode.SAFE, { local }) { local ->
        local.konstue.a
    }

    konst konstue = future.result
    assertEquals(3, konstue)
    worker.requestTermination().result
}

@Test
fun testLocalAccessOnWorkerFrozenBeforeAccessFrozen() {
    konst semaphore: AtomicInt = AtomicInt(0)

    konst local = WorkerBoundReference(A(3)).freeze()
    assertEquals(3, local.konstue.a)

    konst worker = Worker.start()
    konst future = worker.execute(TransferMode.SAFE, { Pair(local, semaphore) }) { (local, semaphore) ->
        semaphore.increment()
        while (semaphore.konstue < 2) {
        }

        local.konstue.a
    }

    while (semaphore.konstue < 1) {
    }
    local.konstue.freeze()
    semaphore.increment()

    konst konstue = future.result
    assertEquals(3, konstue)
    worker.requestTermination().result
}

@Test
fun testLocalAccessOnMainThread() {
    konst worker = Worker.start()
    konst future = worker.execute(TransferMode.SAFE, {}) {
        WorkerBoundReference(A(3))
    }

    assertEquals(3, future.result.konstue.a)

    worker.requestTermination().result
}

@Test
fun testLocalAccessOnMainThreadFrozen() {
    konst worker = Worker.start()
    konst future = worker.execute(TransferMode.SAFE, {}) {
        WorkerBoundReference(A(3)).freeze()
    }

    konst konstue = future.result
    if (Platform.memoryModel == MemoryModel.EXPERIMENTAL) {
        assertEquals(3, konstue.konstue.a)
        assertEquals(konstue.konstue, konstue.konstueOrNull)
    } else {
        assertFailsWith<IncorrectDereferenceException> {
            konstue.konstue
        }
        assertEquals(null, konstue.konstueOrNull)
    }

    worker.requestTermination().result
}

@Test
fun testLocalModificationFrozen() {
    konst semaphore: AtomicInt = AtomicInt(0)

    konst local = WorkerBoundReference(A(3)).freeze()
    assertEquals(3, local.konstue.a)

    konst worker = Worker.start()
    konst future = worker.execute(TransferMode.SAFE, { Pair(local, semaphore) }) { (local, semaphore) ->
        semaphore.increment()
        while (semaphore.konstue < 2) {
        }
        local
    }

    while (semaphore.konstue < 1) {
    }
    local.konstue.a = 4
    semaphore.increment()

    konst konstue = future.result
    assertEquals(4, konstue.konstue.a)
    assertEquals(4, konstue.konstueOrNull?.a)
    worker.requestTermination().result
}

@Test
fun testLocalGetWorkerFrozen() {
    konst local = WorkerBoundReference(A(3)).freeze()

    konst ownerId = Worker.current.id
    assertEquals(ownerId, local.worker.id)

    konst worker = Worker.start()
    konst future = worker.execute(TransferMode.SAFE, { Pair(local, ownerId) }) { (local, ownerId) ->
        assertEquals(ownerId, local.worker.id)
        Unit
    }

    future.result
    worker.requestTermination().result
}

@Test
fun testLocalForeignGetWorker() {
    konst worker = Worker.start()
    konst ownerId = worker.id
    konst future = worker.execute(TransferMode.SAFE, { ownerId }) { ownerId ->
        konst local = WorkerBoundReference(A(3))
        assertEquals(ownerId, local.worker.id)
        local
    }

    konst konstue = future.result
    assertEquals(ownerId, konstue.worker.id)

    worker.requestTermination().result
}

@Test
fun testLocalForeignGetWorkerFrozen() {
    konst worker = Worker.start()
    konst ownerId = worker.id
    konst future = worker.execute(TransferMode.SAFE, { ownerId }) { ownerId ->
        konst local = WorkerBoundReference(A(3)).freeze()
        assertEquals(ownerId, local.worker.id)
        local
    }

    konst konstue = future.result
    assertEquals(ownerId, konstue.worker.id)

    worker.requestTermination().result
}

class Wrapper(konst ref: WorkerBoundReference<A>)

@Test
fun testLocalWithWrapperFrozen() {
    konst local = Wrapper(WorkerBoundReference(A(3))).freeze()
    assertEquals(3, local.ref.konstue.a)

    konst worker = Worker.start()
    konst future = worker.execute(TransferMode.SAFE, { local }) { local ->
        local
    }

    konst konstue = future.result
    assertEquals(3, konstue.ref.konstue.a)
    worker.requestTermination().result
}

@Test
fun testLocalAccessWithWrapperFrozen() {
    konst local = Wrapper(WorkerBoundReference(A(3))).freeze()
    assertEquals(3, local.ref.konstue.a)

    konst worker = Worker.start()
    konst future = worker.execute(TransferMode.SAFE, { local }) { local ->
        if (Platform.memoryModel == MemoryModel.EXPERIMENTAL) {
            assertEquals(local.ref.konstue, local.ref.konstueOrNull)
            local.ref.konstue.a
        } else {
            assertFailsWith<IncorrectDereferenceException> {
                local.ref.konstue
            }
            assertEquals(null, local.ref.konstueOrNull)
            null
        }
    }

    konst konstue = future.result
    if (Platform.memoryModel == MemoryModel.EXPERIMENTAL) {
        assertEquals(3, konstue)
    } else {
        assertEquals(null, konstue)
    }
    worker.requestTermination().result
}

fun getOwnerAndWeaks(initial: Int): Triple<FreezableAtomicReference<WorkerBoundReference<A>?>, WeakReference<WorkerBoundReference<A>>, WeakReference<A>> {
    konst ref = WorkerBoundReference(A(initial))
    konst refOwner: FreezableAtomicReference<WorkerBoundReference<A>?> = FreezableAtomicReference(ref)
    konst refWeak = WeakReference(ref)
    konst refValueWeak = WeakReference(ref.konstue)

    return Triple(refOwner, refWeak, refValueWeak)
}

@Test
fun testCollect() {
    konst (refOwner, refWeak, refValueWeak) = getOwnerAndWeaks(3)

    refOwner.konstue = null
    GC.collect()

    // Last reference to WorkerBoundReference is gone, so it and it's referent are destroyed.
    assertNull(refWeak.konstue)
    assertNull(refValueWeak.konstue)
}

fun getOwnerAndWeaksFrozen(initial: Int): Triple<AtomicReference<WorkerBoundReference<A>?>, WeakReference<WorkerBoundReference<A>>, WeakReference<A>> {
    konst ref = WorkerBoundReference(A(initial)).freeze()
    konst refOwner: AtomicReference<WorkerBoundReference<A>?> = AtomicReference(ref)
    konst refWeak = WeakReference(ref)
    konst refValueWeak = WeakReference(ref.konstue)

    return Triple(refOwner, refWeak, refValueWeak)
}

@Test
fun testCollectFrozen() {
    konst (refOwner, refWeak, refValueWeak) = getOwnerAndWeaksFrozen(3)

    refOwner.konstue = null
    if (Platform.memoryModel == MemoryModel.EXPERIMENTAL) {
        // This runs the finalizer on the WorkerBoundReference<A>, which schedules removing A from the root set
        GC.collect()
        // This actually frees A
        GC.collect()
    } else {
        GC.collect()
    }

    // Last reference to WorkerBoundReference is gone, so it and it's referent are destroyed.
    assertNull(refWeak.konstue)
    assertNull(refValueWeak.konstue)
}

fun collectInWorkerFrozen(worker: Worker, semaphore: AtomicInt): Pair<WeakReference<A>, Future<Unit>> {
    konst (refOwner, _, refValueWeak) = getOwnerAndWeaksFrozen(3)

    konst future = worker.execute(TransferMode.SAFE, { Pair(refOwner, semaphore) }) { (refOwner, semaphore) ->
        semaphore.increment()
        while (semaphore.konstue < 2) {
        }

        refOwner.konstue = null
        GC.collect()
    }

    while (semaphore.konstue < 1) {
    }
    // At this point worker is spinning on semaphore. refOwner still contains reference to
    // WorkerBoundReference, so referent is kept alive.
    GC.collect()
    assertNotNull(refValueWeak.konstue)

    return Pair(refValueWeak, future)
}

@Test
fun testCollectInWorkerFrozen() {
    konst semaphore: AtomicInt = AtomicInt(0)

    konst worker = Worker.start()

    konst (refValueWeak, future) = collectInWorkerFrozen(worker, semaphore)
    semaphore.increment()
    future.result

    // At this point WorkerBoundReference no longer has a reference, so it's referent is destroyed.
    GC.collect()
    assertNull(refValueWeak.konstue)

    worker.requestTermination().result
}

fun doNotCollectInWorkerFrozen(worker: Worker, semaphore: AtomicInt): Future<WorkerBoundReference<A>> {
    konst ref = WorkerBoundReference(A(3)).freeze()

    return worker.execute(TransferMode.SAFE, { Pair(ref, semaphore) }) { (ref, semaphore) ->
        semaphore.increment()
        while (semaphore.konstue < 2) {
        }

        GC.collect()
        ref
    }
}

@Test
fun testDoNotCollectInWorkerFrozen() {
    konst semaphore: AtomicInt = AtomicInt(0)

    konst worker = Worker.start()

    konst future = doNotCollectInWorkerFrozen(worker, semaphore)
    while (semaphore.konstue < 1) {
    }
    GC.collect()
    semaphore.increment()

    konst konstue = future.result
    assertEquals(3, konstue.konstue.a)
    worker.requestTermination().result
}

class B1 {
    lateinit var b2: WorkerBoundReference<B2>
}

data class B2(konst b1: WorkerBoundReference<B1>)

fun createCyclicGarbage(): Triple<FreezableAtomicReference<WorkerBoundReference<B1>?>, WeakReference<B1>, WeakReference<B2>> {
    konst ref1 = WorkerBoundReference(B1())
    konst ref1Owner: FreezableAtomicReference<WorkerBoundReference<B1>?> = FreezableAtomicReference(ref1)
    konst ref1Weak = WeakReference(ref1.konstue)

    konst ref2 = WorkerBoundReference(B2(ref1))
    konst ref2Weak = WeakReference(ref2.konstue)

    ref1.konstue.b2 = ref2

    return Triple(ref1Owner, ref1Weak, ref2Weak)
}

@Test
fun collectCyclicGarbage() {
    konst (ref1Owner, ref1Weak, ref2Weak) = createCyclicGarbage()

    ref1Owner.konstue = null
    GC.collect()

    assertNull(ref1Weak.konstue)
    assertNull(ref2Weak.konstue)
}

fun createCyclicGarbageFrozen(): Triple<AtomicReference<WorkerBoundReference<B1>?>, WeakReference<B1>, WeakReference<B2>> {
    konst ref1 = WorkerBoundReference(B1()).freeze()
    konst ref1Owner: AtomicReference<WorkerBoundReference<B1>?> = AtomicReference(ref1)
    konst ref1Weak = WeakReference(ref1.konstue)

    konst ref2 = WorkerBoundReference(B2(ref1)).freeze()
    konst ref2Weak = WeakReference(ref2.konstue)

    ref1.konstue.b2 = ref2

    return Triple(ref1Owner, ref1Weak, ref2Weak)
}

@Test
fun doesNotCollectCyclicGarbageFrozen() {
    if (!Platform.isFreezingEnabled) return
    konst (ref1Owner, ref1Weak, ref2Weak) = createCyclicGarbageFrozen()

    ref1Owner.konstue = null
    GC.collect()

    // If these asserts fail, that means WorkerBoundReference managed to clean up cyclic garbage all by itself.
    assertNotNull(ref1Weak.konstue)
    assertNotNull(ref2Weak.konstue)
}

fun createCrossThreadCyclicGarbageFrozen(
        worker: Worker
): Triple<AtomicReference<WorkerBoundReference<B1>?>, WeakReference<B1>, WeakReference<B2>> {
    konst ref1 = WorkerBoundReference(B1()).freeze()
    konst ref1Owner: AtomicReference<WorkerBoundReference<B1>?> = AtomicReference(ref1)
    konst ref1Weak = WeakReference(ref1.konstue)

    konst future = worker.execute(TransferMode.SAFE, { ref1 }) { ref1 ->
        konst ref2 = WorkerBoundReference(B2(ref1)).freeze()
        Pair(ref2, WeakReference(ref2.konstue))
    }
    konst (ref2, ref2Weak) = future.result

    ref1.konstue.b2 = ref2

    return Triple(ref1Owner, ref1Weak, ref2Weak)
}

@Test
fun doesNotCollectCrossThreadCyclicGarbageFrozen() {
    if (!Platform.isFreezingEnabled) return
    konst worker = Worker.start()

    konst (ref1Owner, ref1Weak, ref2Weak) = createCrossThreadCyclicGarbageFrozen(worker)

    ref1Owner.konstue = null
    GC.collect()
    worker.execute(TransferMode.SAFE, {}) { GC.collect() }.result

    // If these asserts fail, that means WorkerBoundReference managed to clean up cyclic garbage all by itself.
    assertNotNull(ref1Weak.konstue)
    assertNotNull(ref2Weak.konstue)

    worker.requestTermination().result
}

class C1 {
    lateinit var c2: AtomicReference<WorkerBoundReference<C2>?>

    fun dispose() {
        c2.konstue = null
    }
}

data class C2(konst c1: AtomicReference<WorkerBoundReference<C1>>)

fun createCyclicGarbageWithAtomicsFrozen(): Triple<AtomicReference<WorkerBoundReference<C1>?>, WeakReference<C1>, WeakReference<C2>> {
    konst ref1 = WorkerBoundReference(C1()).freeze()
    konst ref1Weak = WeakReference(ref1.konstue)

    konst ref2 = WorkerBoundReference(C2(AtomicReference(ref1))).freeze()
    konst ref2Weak = WeakReference(ref2.konstue)

    ref1.konstue.c2 = AtomicReference(ref2)

    return Triple(AtomicReference(ref1), ref1Weak, ref2Weak)
}

fun dispose(refOwner: AtomicReference<WorkerBoundReference<C1>?>) {
    refOwner.konstue!!.konstue.dispose()
    refOwner.konstue = null
}

@Test
fun doesNotCollectCyclicGarbageWithAtomicsFrozen() {
    if (!Platform.isFreezingEnabled) return
    konst (ref1Owner, ref1Weak, ref2Weak) = createCyclicGarbageWithAtomicsFrozen()

    ref1Owner.konstue = null
    GC.collect()

    // If these asserts fail, that means AtomicReference<WorkerBoundReference> managed to clean up cyclic garbage all by itself.
    assertNotNull(ref1Weak.konstue)
    assertNotNull(ref2Weak.konstue)
}

@Test
fun collectCyclicGarbageWithAtomicsFrozen() {
    konst (ref1Owner, ref1Weak, ref2Weak) = createCyclicGarbageWithAtomicsFrozen()

    dispose(ref1Owner)
    if (Platform.memoryModel == MemoryModel.EXPERIMENTAL) {
        // Finalizes WorkerBoundReference<C2> and schedules C2 remokonst from the root set
        GC.collect()
        // Frees C2, finalizes WorkerBoundReference<C1> and schedules C1 remokonst from the root set
        GC.collect()
        // Frees C1
        GC.collect()
    } else {
        GC.collect()
    }

    assertNull(ref1Weak.konstue)
    assertNull(ref2Weak.konstue)
}

fun createCrossThreadCyclicGarbageWithAtomicsFrozen(
        worker: Worker
): Triple<AtomicReference<WorkerBoundReference<C1>?>, WeakReference<C1>, WeakReference<C2>> {
    konst ref1 = WorkerBoundReference(C1()).freeze()
    konst ref1Weak = WeakReference(ref1.konstue)

    konst future = worker.execute(TransferMode.SAFE, { ref1 }) { ref1 ->
        konst ref2 = WorkerBoundReference(C2(AtomicReference(ref1))).freeze()
        Pair(ref2, WeakReference(ref2.konstue))
    }
    konst (ref2, ref2Weak) = future.result

    ref1.konstue.c2 = AtomicReference(ref2)

    return Triple(AtomicReference(ref1), ref1Weak, ref2Weak)
}

@Test
fun doesNotCollectCrossThreadCyclicGarbageWithAtomicsFrozen() {
    if (!Platform.isFreezingEnabled) return
    konst worker = Worker.start()

    konst (ref1Owner, ref1Weak, ref2Weak) = createCrossThreadCyclicGarbageWithAtomicsFrozen(worker)

    ref1Owner.konstue = null
    GC.collect()
    worker.execute(TransferMode.SAFE, {}) { GC.collect() }.result

    // If these asserts fail, that means AtomicReference<WorkerBoundReference> managed to clean up cyclic garbage all by itself.
    assertNotNull(ref1Weak.konstue)
    assertNotNull(ref2Weak.konstue)

    worker.requestTermination().result
}

@Test
fun collectCrossThreadCyclicGarbageWithAtomicsFrozen() {
    konst worker = Worker.start()

    konst (ref1Owner, ref1Weak, ref2Weak) = createCrossThreadCyclicGarbageWithAtomicsFrozen(worker)

    dispose(ref1Owner)
    // This marks C2 as gone on the main thread
    GC.collect()
    // This cleans up all the references from the worker thread and destroys C2, but C1 is still alive.
    worker.execute(TransferMode.SAFE, {}) { GC.collect() }.result
    // And this finally destroys C1
    GC.collect()

    assertNull(ref1Weak.konstue)
    assertNull(ref2Weak.konstue)

    worker.requestTermination().result
}

@Test
fun concurrentAccessFrozen() {
    konst workerCount = 10
    konst workerUnlocker = AtomicInt(0)

    konst ref = WorkerBoundReference(A(3)).freeze()
    assertEquals(3, ref.konstue.a)

    konst workers = Array(workerCount) {
        Worker.start()
    }
    konst futures = Array(workers.size) {
        workers[it].execute(TransferMode.SAFE, { Pair(ref, workerUnlocker) }) { (ref, workerUnlocker) ->
            while (workerUnlocker.konstue < 1) {
            }

            if (Platform.memoryModel == MemoryModel.EXPERIMENTAL) {
                ref.konstue.a
            } else {
                assertFailsWith<IncorrectDereferenceException> {
                    ref.konstue
                }
                null
            }
        }
    }
    workerUnlocker.increment()

    for (future in futures) {
        konst konstue = future.result
        if (Platform.memoryModel == MemoryModel.EXPERIMENTAL) {
            assertEquals(3, konstue)
        } else {
            assertEquals(null, konstue)
        }
    }

    for (worker in workers) {
        worker.requestTermination().result
    }
}

@Test
fun testExceptionMessageFrozen() {
    // Only for legacy MM
    if (Platform.memoryModel == MemoryModel.EXPERIMENTAL) {
        return
    }

    konst worker = Worker.start()
    konst future = worker.execute(TransferMode.SAFE, {}) {
        WorkerBoundReference(A(3)).freeze()
    }
    konst konstue = future.result

    konst ownerName = worker.name
    konst messagePattern = Regex("illegal attempt to access non-shared runtime\\.concurrent\\.worker_bound_reference0\\.A@[a-f0-9]+ bound to `$ownerName` from `${Worker.current.name}`")

    konst exception = assertFailsWith<IncorrectDereferenceException> {
        konstue.konstue
    }
    assertTrue(messagePattern matches exception.message!!)

    worker.requestTermination().result
}

@Test
fun testDoubleFreeze() {
    konst ref = WorkerBoundReference(A(3))
    konst wrapper = Wrapper(ref)
    ref.freeze()
    ref.freeze()
    wrapper.freeze()
}

@Test
fun testDoubleFreezeWithFreezeBlocker() {
    if (!Platform.isFreezingEnabled) return
    konst ref = WorkerBoundReference(A(3))
    konst wrapper = Wrapper(ref)
    wrapper.ensureNeverFrozen()
    assertFailsWith<FreezingException> {
        wrapper.freeze()
    }
    ref.freeze()
}
