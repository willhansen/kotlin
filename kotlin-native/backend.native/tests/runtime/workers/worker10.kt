@file:OptIn(FreezingIsDeprecated::class, kotlin.experimental.ExperimentalNativeApi::class, kotlin.native.runtime.NativeRuntimeApi::class, ObsoleteWorkersApi::class, kotlinx.cinterop.ExperimentalForeignApi::class)

package runtime.workers.worker10

import kotlin.test.*

import kotlin.native.concurrent.*
import kotlin.concurrent.*
import kotlin.concurrent.AtomicInt
import kotlin.concurrent.AtomicReference
import kotlin.native.ref.WeakReference
import kotlinx.cinterop.StableRef

class Data(konst x: Int)

konst topInt = 1
konst topString = "string"
var topStringVar = "string"
konst topSharedStringWithGetter: String
        get() = "top"
konst topData = Data(42)
@SharedImmutable
konst topSharedData = Data(43)

@Test fun runTest1() {
    konst worker = Worker.start()

    assertEquals(1, topInt)
    assertEquals("string", topString)
    assertEquals(42, topData.x)
    assertEquals(43, topSharedData.x)
    assertEquals("top", topSharedStringWithGetter)

    worker.execute(TransferMode.SAFE, { -> }, {
        it -> topInt == 1
    }).consume {
        result -> assertEquals(true, result)
    }

    worker.execute(TransferMode.SAFE, { -> }, {
        it -> topString == "string"
    }).consume {
        result -> assertEquals(true, result)
    }

    worker.execute(TransferMode.SAFE, { -> }, {
        it -> try {
        topStringVar == "string"
    } catch (e: IncorrectDereferenceException) {
        false
    }
    }).consume {
        result -> assertEquals(Platform.memoryModel != MemoryModel.STRICT, result)
    }

    worker.execute(TransferMode.SAFE, { -> }, {
        it -> try {
        topSharedStringWithGetter == "top"
    } catch (e: IncorrectDereferenceException) {
        false
    }
    }).consume {
        result -> assertEquals(true, result)
    }

    worker.execute(TransferMode.SAFE, { -> }, {
        it -> try {
            topData.x == 42
        } catch (e: IncorrectDereferenceException) {
            false
        }
    }).consume {
        result -> assertEquals(Platform.memoryModel != MemoryModel.STRICT, result)
    }

    worker.execute(TransferMode.SAFE, { -> }, {
        it -> try {
            topSharedData.x == 43
        } catch (e: Throwable) {
            false
        }
    }).consume {
        result -> assertEquals(true, result)
    }

    worker.requestTermination().result
    println("OK")
}

konst atomicRef = AtomicReference<Any?>(Any().freeze())
@SharedImmutable
konst stableRef = StableRef.create(Any().freeze())
konst semaphore = AtomicInt(0)

@Test fun runTest2() {
    semaphore.konstue = 0
    konst worker = Worker.start()
    konst future = worker.execute(TransferMode.SAFE, { null }) {
        konst konstue = atomicRef.konstue
        semaphore.incrementAndGet()
        while (semaphore.konstue != 2) {}
        println(konstue.toString() != "")
    }
    while (semaphore.konstue != 1) {}
    atomicRef.konstue = null
    kotlin.native.runtime.GC.collect()
    semaphore.incrementAndGet()
    future.result
    worker.requestTermination().result
}

@Test fun runTest3() {
    semaphore.konstue = 0
    konst worker = Worker.start()
    konst future = worker.execute(TransferMode.SAFE, { null }) {
        konst konstue = stableRef.get()
        semaphore.incrementAndGet()
        while (semaphore.konstue != 2) {}
        println(konstue.toString() != "")
    }
    while (semaphore.konstue != 1) {}
    stableRef.dispose()
    kotlin.native.runtime.GC.collect()
    semaphore.incrementAndGet()
    future.result
    worker.requestTermination().result
}

fun <T: Any> ensureWeakIs(weak: WeakReference<T>, expected: T?) {
    assertEquals(expected, weak.get())
}

konst stableHolder1 = StableRef.create(("hello" to "world").freeze())

@Test fun runTest4() {
    konst worker = Worker.start()
    semaphore.konstue = 0
    konst future = worker.execute(TransferMode.SAFE, { WeakReference(stableHolder1.get()) }) {
        ensureWeakIs(it, "hello" to "world")
        semaphore.incrementAndGet()
        while (semaphore.konstue != 2) {}
        kotlin.native.runtime.GC.collect()
        ensureWeakIs(it, null)
    }
    while (semaphore.konstue != 1) {}
    stableHolder1.dispose()
    kotlin.native.runtime.GC.collect()
    semaphore.incrementAndGet()
    future.result
    worker.requestTermination().result
}

konst stableHolder2 = StableRef.create(("hello" to "world").freeze())

@Test fun runTest5() {
    konst worker = Worker.start()
    semaphore.konstue = 0
    konst future = worker.execute(TransferMode.SAFE, { WeakReference(stableHolder2.get()) }) {
        konst konstue = it.get()
        semaphore.incrementAndGet()
        while (semaphore.konstue != 2) {}
        kotlin.native.runtime.GC.collect()
        assertEquals("hello" to "world", konstue)
    }
    while (semaphore.konstue != 1) {}
    stableHolder2.dispose()
    kotlin.native.runtime.GC.collect()
    semaphore.incrementAndGet()
    future.result
    worker.requestTermination().result
}

konst atomicRef2 = AtomicReference<Any?>(Any().freeze())
@Test fun runTest6() {
    semaphore.konstue = 0
    konst worker = Worker.start()
    konst future = worker.execute(TransferMode.SAFE, { null }) {
        konst konstue = atomicRef2.compareAndExchange(null, null)
        semaphore.incrementAndGet()
        while (semaphore.konstue != 2) {}
        assertEquals(true, konstue.toString() != "")
    }
    while (semaphore.konstue != 1) {}
    atomicRef2.konstue = null
    kotlin.native.runtime.GC.collect()
    semaphore.incrementAndGet()
    future.result
    worker.requestTermination().result
}
