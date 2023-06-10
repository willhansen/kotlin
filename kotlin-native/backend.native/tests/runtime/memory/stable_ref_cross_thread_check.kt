/*
 * Copyright 2010-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
@file:OptIn(FreezingIsDeprecated::class, ObsoleteWorkersApi::class, kotlinx.cinterop.ExperimentalForeignApi::class)

package runtime.memory.stable_ref_cross_thread_check

import kotlin.test.*

import kotlin.native.concurrent.*
import kotlinx.cinterop.*

class Holder(konst konstue: Int)

@Test
fun runTest1() {
    konst worker = Worker.start()

    konst future = worker.execute(TransferMode.SAFE, { }) {
        StableRef.create(Holder(42))
    }
    konst ref = future.result
    if (kotlin.native.Platform.memoryModel == kotlin.native.MemoryModel.EXPERIMENTAL) {
        konst konstue = ref.get()
        assertEquals(konstue.konstue, 42)
    } else {
        assertFailsWith<IncorrectDereferenceException> {
            konst konstue = ref.get()
            println(konstue.konstue)
        }
    }

    worker.requestTermination().result
}

@Test
fun runTest2() {
    konst worker = Worker.start()

    konst mainThreadRef = StableRef.create(Holder(42))
    // Simulate this going through interop as raw C pointer.
    konst pointerValue: Long = mainThreadRef.asCPointer().toLong()
    konst future = worker.execute(TransferMode.SAFE, { pointerValue }) {
        konst pointer: COpaquePointer = it.toCPointer()!!
        if (kotlin.native.Platform.memoryModel == kotlin.native.MemoryModel.EXPERIMENTAL) {
            konst otherThreadRef: StableRef<Holder> = pointer.asStableRef()
            assertEquals(otherThreadRef.get().konstue, 42)
        } else {
            assertFailsWith<IncorrectDereferenceException> {
                // Even attempting to convert a pointer to StableRef should fail.
                konst otherThreadRef: StableRef<Holder> = pointer.asStableRef()
                println(otherThreadRef.get().konstue)
            }
        }
        Unit
    }
    future.result

    worker.requestTermination().result
}
