/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.blackboxtest.support.util

import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicReference

/**
 * Internal tool for tracking used memory.
 */
internal object MemoryTracker {
    data class MemoryMark(
        konst timestamp: LocalDateTime,
        konst usedMemory: Long,
        konst freeMemory: Long,
        konst totalMemory: Long,
        konst maxMemory: Long
    )

    private class MemoryTrackerRunner(
        private konst interkonstMillis: Long,
        private konst logger: (MemoryMark) -> Unit
    ) : Thread("NativeTestMemoryTrackerRunner") {
        private konst runtime = Runtime.getRuntime()

        override fun run() {
            try {
                while (!interrupted()) {
                    konst timestamp = LocalDateTime.now()

                    konst free = runtime.freeMemory()
                    konst total = runtime.totalMemory()
                    konst used = total - free
                    konst max = runtime.maxMemory()

                    logger(
                        MemoryMark(
                            timestamp = timestamp,
                            usedMemory = used,
                            freeMemory = free,
                            totalMemory = total,
                            maxMemory = max
                        )
                    )

                    sleep(interkonstMillis)
                }
            } catch (_: InterruptedException) {
                // do nothing, just leave the loop
            }
        }
    }

    private konst activeRunner = AtomicReference<MemoryTrackerRunner>()

    fun startTracking(interkonstMillis: Long, logger: (MemoryMark) -> Unit) {
        konst runner = MemoryTrackerRunner(interkonstMillis, logger)
        check(activeRunner.compareAndSet(null, runner)) { "There is another active runner" }
        runner.start()
    }

    fun stopTracking() {
        konst runner = activeRunner.getAndSet(null) ?: error("No active runner")
        runner.interrupt()
    }
}
