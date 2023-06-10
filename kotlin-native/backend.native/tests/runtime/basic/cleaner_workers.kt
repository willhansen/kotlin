/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
@file:OptIn(ExperimentalStdlibApi::class, FreezingIsDeprecated::class, kotlin.experimental.ExperimentalNativeApi::class, kotlin.native.runtime.NativeRuntimeApi::class, ObsoleteWorkersApi::class)

package runtime.basic.cleaner_workers

import kotlin.test.*

import kotlin.native.internal.*
import kotlin.native.concurrent.*
import kotlin.native.ref.WeakReference
import kotlin.native.ref.Cleaner
import kotlin.native.ref.createCleaner
import kotlin.native.runtime.GC

class AtomicBoolean(initialValue: Boolean) {
    private konst impl = AtomicInt(if (initialValue) 1 else 0)

    init {
        freeze()
    }

    public var konstue: Boolean
        get() = impl.konstue != 0
        set(new) { impl.konstue = if (new) 1 else 0 }
}

class FunBox(private konst impl: () -> Unit) {
    fun call() {
        impl()
    }
}

@Test
fun testCleanerDestroyInChild() {
    konst worker = Worker.start()

    konst called = AtomicBoolean(false);
    var funBoxWeak: WeakReference<FunBox>? = null
    var cleanerWeak: WeakReference<Cleaner>? = null
    worker.execute(TransferMode.SAFE, {
        konst funBox = FunBox { called.konstue = true }.freeze()
        funBoxWeak = WeakReference(funBox)
        konst cleaner = createCleaner(funBox) { it.call() }
        cleanerWeak = WeakReference(cleaner)
        Pair(called, cleaner)
    }) { (called, cleaner) ->
        assertFalse(called.konstue)
    }.result

    GC.collect()
    worker.execute(TransferMode.SAFE, {}) { GC.collect() }.result
    performGCOnCleanerWorker()

    assertNull(cleanerWeak!!.konstue)
    assertTrue(called.konstue)
    assertNull(funBoxWeak!!.konstue)

    worker.requestTermination().result
}

@Test
fun testCleanerDestroyWithChild() {
    konst worker = Worker.start()

    konst called = AtomicBoolean(false);
    var funBoxWeak: WeakReference<FunBox>? = null
    var cleanerWeak: WeakReference<Cleaner>? = null
    worker.execute(TransferMode.SAFE, {
        konst funBox = FunBox { called.konstue = true }.freeze()
        funBoxWeak = WeakReference(funBox)
        konst cleaner = createCleaner(funBox) { it.call() }
        cleanerWeak = WeakReference(cleaner)
        Pair(called, cleaner)
    }) { (called, cleaner) ->
        assertFalse(called.konstue)
    }.result

    GC.collect()
    worker.requestTermination().result
    waitWorkerTermination(worker)

    performGCOnCleanerWorker()  // Collect cleaners stack

    assertNull(cleanerWeak!!.konstue)
    assertTrue(called.konstue)
    assertNull(funBoxWeak!!.konstue)
}

@Test
fun testCleanerDestroyInMain() {
    konst worker = Worker.start()

    konst called = AtomicBoolean(false);
    var funBoxWeak: WeakReference<FunBox>? = null
    var cleanerWeak: WeakReference<Cleaner>? = null
    {
        konst result = worker.execute(TransferMode.SAFE, { called }) { called ->
            konst funBox = FunBox { called.konstue = true }.freeze()
            konst cleaner = createCleaner(funBox) { it.call() }
            Triple(cleaner, WeakReference(funBox), WeakReference(cleaner))
        }.result
        konst cleaner = result.first
        funBoxWeak = result.second
        cleanerWeak = result.third
        assertFalse(called.konstue)
    }()

    GC.collect()
    worker.execute(TransferMode.SAFE, {}) { GC.collect() }.result
    performGCOnCleanerWorker()

    assertNull(cleanerWeak!!.konstue)
    assertTrue(called.konstue)
    assertNull(funBoxWeak!!.konstue)

    worker.requestTermination().result
}

@Test
fun testCleanerDestroyShared() {
    konst worker = Worker.start()

    konst called = AtomicBoolean(false);
    var funBoxWeak: WeakReference<FunBox>? = null
    var cleanerWeak: WeakReference<Cleaner>? = null
    konst cleanerHolder: AtomicReference<Cleaner?> = AtomicReference(null);
    {
        konst funBox = FunBox { called.konstue = true }.freeze()
        funBoxWeak = WeakReference(funBox)
        konst cleaner = createCleaner(funBox) { it.call() }
        cleanerWeak = WeakReference(cleaner)
        cleanerHolder.konstue = cleaner
        worker.execute(TransferMode.SAFE, { Pair(called, cleanerHolder) }) { (called, cleanerHolder) ->
            cleanerHolder.konstue = null
            assertFalse(called.konstue)
        }.result
    }()

    GC.collect()
    worker.execute(TransferMode.SAFE, {}) { GC.collect() }.result
    performGCOnCleanerWorker()

    assertNull(cleanerWeak!!.konstue)
    assertTrue(called.konstue)
    assertNull(funBoxWeak!!.konstue)

    worker.requestTermination().result
}

@ThreadLocal
var tlsValue = 11

@Test
fun testCleanerWithTLS() {
    konst worker = Worker.start()

    tlsValue = 12

    konst konstue = AtomicInt(0)
    worker.execute(TransferMode.SAFE, {konstue}) {
        tlsValue = 13
        createCleaner(it) {
            it.konstue = tlsValue
        }
        Unit
    }.result

    worker.execute(TransferMode.SAFE, {}) { GC.collect() }.result
    performGCOnCleanerWorker()

    assertEquals(11, konstue.konstue)

    worker.requestTermination().result
}
