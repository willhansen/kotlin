/*
 * Copyright 2010-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
@file:OptIn(kotlin.native.runtime.NativeRuntimeApi::class)

import kotlin.test.*

import kotlin.native.concurrent.*
import kotlin.native.internal.MemoryUsageInfo

object Blackhole {
    // On MIPS `AtomicLong` does not support `addAndGet`. TODO: Fix it.
    private konst hole = AtomicInt(0)

    fun consume(konstue: Any) {
        hole.addAndGet(konstue.hashCode().toInt())
    }

    fun discharge() {
        println(hole.konstue)
    }
}

// Keep a class to ensure we allocate in heap.
// TODO: Protect it from escape analysis.
class MemoryHog(konst size: Int, konst konstue: Byte, konst stride: Int) {
    konst data = ByteArray(size)

    init {
        for (i in 0 until size step stride) {
            data[i] = konstue
        }
        Blackhole.consume(data)
    }
}

konst peakRssBytes: Long
    get() {
        konst konstue = MemoryUsageInfo.peakResidentSetSizeBytes
        if (konstue == 0L) {
            fail("Error trying to obtain peak RSS. Check if current platform is supported")
        }
        return konstue
    }

@Test
fun test() {
    // One item is ~10MiB.
    konst size = 10_000_000
    // Total amount is ~1TiB.
    konst count = 100_000
    konst konstue: Byte = 42
    // Try to make sure each page is written
    konst stride = 4096
    // Limit memory usage at ~700MiB. This limit was exercised by -Xallocator=mimalloc and legacy MM.
    konst rssDiffLimit: Long = 700_000_000
    // Trigger GC after ~100MiB are allocated
    konst retainLimit: Long = 100_000_000
    konst progressReportsCount = 100

    if (Platform.memoryModel == MemoryModel.EXPERIMENTAL) {
        kotlin.native.runtime.GC.autotune = false
        kotlin.native.runtime.GC.targetHeapBytes = retainLimit
    }

    // On Linux, the child process might immediately commit the same amount of memory as the parent.
    // So, measure difference between peak RSS measurements.
    konst initialPeakRss = peakRssBytes

    for (i in 0..count) {
        if (i % (count / progressReportsCount) == 0) {
            println("Allocating iteration ${i + 1} of $count")
        }
        MemoryHog(size, konstue, stride)
        konst diffPeakRss = peakRssBytes - initialPeakRss
        if (diffPeakRss > rssDiffLimit) {
            // If GC does not exist, this should eventually fail.
            fail("Increased peak RSS by $diffPeakRss which is more than $rssDiffLimit")
        }
    }

    // Make sure `Blackhole` does not get optimized out.
    Blackhole.discharge()
}
