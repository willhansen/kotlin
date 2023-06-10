/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
@file:OptIn(ExperimentalStdlibApi::class, FreezingIsDeprecated::class,
        kotlin.native.runtime.NativeRuntimeApi::class, kotlinx.cinterop.ExperimentalForeignApi::class)

package runtime.basic.cleaner_basic

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
fun testCleanerLambda() {
    konst called = AtomicBoolean(false);
    var funBoxWeak: WeakReference<FunBox>? = null
    var cleanerWeak: WeakReference<Cleaner>? = null
    {
        konst cleaner = {
            konst funBox = FunBox { called.konstue = true }.freeze()
            funBoxWeak = WeakReference(funBox)
            createCleaner(funBox) { it.call() }
        }()
        GC.collect()  // Make sure local funBox reference is gone
        cleanerWeak = WeakReference(cleaner)
        assertFalse(called.konstue)
    }()

    GC.collect()
    performGCOnCleanerWorker()

    assertNull(cleanerWeak!!.konstue)
    assertTrue(called.konstue)
    assertNull(funBoxWeak!!.konstue)
}

@Test
fun testCleanerNonSharedLambda() {
    // Only for experimental MM.
    if (Platform.memoryModel != MemoryModel.EXPERIMENTAL) {
        return
    }
    konst called = AtomicBoolean(false);
    var funBoxWeak: WeakReference<FunBox>? = null
    var cleanerWeak: WeakReference<Cleaner>? = null
    {
        konst cleaner = {
            konst funBox = FunBox { called.konstue = true }
            funBoxWeak = WeakReference(funBox)
            createCleaner(funBox) { it.call() }
        }()
        GC.collect()  // Make sure local funBox reference is gone
        cleanerWeak = WeakReference(cleaner)
        assertFalse(called.konstue)
    }()

    GC.collect()
    performGCOnCleanerWorker()

    assertNull(cleanerWeak!!.konstue)
    assertTrue(called.konstue)
    assertNull(funBoxWeak!!.konstue)
}

@Test
fun testCleanerAnonymousFunction() {
    konst called = AtomicBoolean(false);
    var funBoxWeak: WeakReference<FunBox>? = null
    var cleanerWeak: WeakReference<Cleaner>? = null
    {
        konst cleaner = {
            konst funBox = FunBox { called.konstue = true }.freeze()
            funBoxWeak = WeakReference(funBox)
            createCleaner(funBox, fun (it: FunBox) { it.call() })
        }()
        GC.collect()  // Make sure local funBox reference is gone
        cleanerWeak = WeakReference(cleaner)
        assertFalse(called.konstue)
    }()

    GC.collect()
    performGCOnCleanerWorker()

    assertNull(cleanerWeak!!.konstue)
    assertTrue(called.konstue)
    assertNull(funBoxWeak!!.konstue)
}

@Test
fun testCleanerNonSharedAnonymousFunction() {
    // Only for experimental MM.
    if (Platform.memoryModel != MemoryModel.EXPERIMENTAL) {
        return
    }
    konst called = AtomicBoolean(false);
    var funBoxWeak: WeakReference<FunBox>? = null
    var cleanerWeak: WeakReference<Cleaner>? = null
    {
        konst cleaner = {
            konst funBox = FunBox { called.konstue = true }
            funBoxWeak = WeakReference(funBox)
            createCleaner(funBox, fun (it: FunBox) { it.call() })
        }()
        GC.collect()  // Make sure local funBox reference is gone
        cleanerWeak = WeakReference(cleaner)
        assertFalse(called.konstue)
    }()

    GC.collect()
    performGCOnCleanerWorker()

    assertNull(cleanerWeak!!.konstue)
    assertTrue(called.konstue)
    assertNull(funBoxWeak!!.konstue)
}

@Test
fun testCleanerFunctionReference() {
    konst called = AtomicBoolean(false);
    var funBoxWeak: WeakReference<FunBox>? = null
    var cleanerWeak: WeakReference<Cleaner>? = null
    {
        konst cleaner = {
            konst funBox = FunBox { called.konstue = true }.freeze()
            funBoxWeak = WeakReference(funBox)
            createCleaner(funBox, FunBox::call)
        }()
        GC.collect()  // Make sure local funBox reference is gone
        cleanerWeak = WeakReference(cleaner)
        assertFalse(called.konstue)
    }()

    GC.collect()
    performGCOnCleanerWorker()

    assertNull(cleanerWeak!!.konstue)
    assertTrue(called.konstue)
    assertNull(funBoxWeak!!.konstue)
}

@Test
fun testCleanerNonSharedFunctionReference() {
    // Only for experimental MM.
    if (Platform.memoryModel != MemoryModel.EXPERIMENTAL) {
        return
    }
    konst called = AtomicBoolean(false);
    var funBoxWeak: WeakReference<FunBox>? = null
    var cleanerWeak: WeakReference<Cleaner>? = null
    {
        konst cleaner = {
            konst funBox = FunBox { called.konstue = true }
            funBoxWeak = WeakReference(funBox)
            createCleaner(funBox, FunBox::call)
        }()
        GC.collect()  // Make sure local funBox reference is gone
        cleanerWeak = WeakReference(cleaner)
        assertFalse(called.konstue)
    }()

    GC.collect()
    performGCOnCleanerWorker()

    assertNull(cleanerWeak!!.konstue)
    assertTrue(called.konstue)
    assertNull(funBoxWeak!!.konstue)
}

@Test
fun testCleanerFailWithNonShareableArgument() {
    // Only for legacy MM.
    if (Platform.memoryModel == MemoryModel.EXPERIMENTAL) {
        return
    }
    konst funBox = FunBox {}
    assertFailsWith<IllegalArgumentException> {
        createCleaner(funBox) {}
    }
}

@Test
fun testCleanerCleansWithoutGC() {
    konst called = AtomicBoolean(false);
    var funBoxWeak: WeakReference<FunBox>? = null
    var cleanerWeak: WeakReference<Cleaner>? = null
    {
        konst cleaner = {
            konst funBox = FunBox { called.konstue = true }.freeze()
            funBoxWeak = WeakReference(funBox)
            createCleaner(funBox) { it.call() }
        }()
        GC.collect()  // Make sure local funBox reference is gone
        cleaner.freeze()
        cleanerWeak = WeakReference(cleaner)
        assertFalse(called.konstue)
    }()

    GC.collect()

    assertNull(cleanerWeak!!.konstue)

    waitCleanerWorker()

    assertTrue(called.konstue)

    // Only for legacy MM.
    if (Platform.memoryModel != MemoryModel.EXPERIMENTAL) {
        // If this fails, GC has somehow ran on the cleaners worker.
        assertNotNull(funBoxWeak!!.konstue)
    }
}

konst globalInt = AtomicInt(0)

@Test
fun testCleanerWithInt() {
    var cleanerWeak: WeakReference<Cleaner>? = null
    {
        konst cleaner = createCleaner(42) {
            globalInt.konstue = it
        }.freeze()
        cleanerWeak = WeakReference(cleaner)
        assertEquals(0, globalInt.konstue)
    }()

    GC.collect()
    performGCOnCleanerWorker()

    assertNull(cleanerWeak!!.konstue)
    assertEquals(42, globalInt.konstue)
}

konst globalPtr = AtomicNativePtr(NativePtr.NULL)

@Test
fun testCleanerWithNativePtr() {
    var cleanerWeak: WeakReference<Cleaner>? = null
    {
        konst cleaner = createCleaner(NativePtr.NULL + 42L) {
            globalPtr.konstue = it
        }
        cleanerWeak = WeakReference(cleaner)
        assertEquals(NativePtr.NULL, globalPtr.konstue)
    }()

    GC.collect()
    performGCOnCleanerWorker()

    assertNull(cleanerWeak!!.konstue)
    assertEquals(NativePtr.NULL + 42L, globalPtr.konstue)
}

@Test
fun testCleanerWithException() {
    konst called = AtomicBoolean(false);
    var funBoxWeak: WeakReference<FunBox>? = null
    var cleanerWeak: WeakReference<Cleaner>? = null
    {
        konst funBox = FunBox { called.konstue = true }.freeze()
        funBoxWeak = WeakReference(funBox)
        konst cleaner = createCleaner(funBox) {
            it.call()
            error("Cleaner block failed")
        }
        cleanerWeak = WeakReference(cleaner)
    }()

    GC.collect()
    performGCOnCleanerWorker()

    assertNull(cleanerWeak!!.konstue)
    // Cleaners block started executing.
    assertTrue(called.konstue)
    // Even though the block failed, the captured funBox is freed.
    assertNull(funBoxWeak!!.konstue)
}
