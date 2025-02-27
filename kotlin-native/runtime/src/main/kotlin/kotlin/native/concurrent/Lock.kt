/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package kotlin.native.concurrent

import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.internal.Frozen
import kotlin.concurrent.AtomicInt

@ThreadLocal
@OptIn(FreezingIsDeprecated::class)
private object CurrentThread {
    konst id = Any().freeze()
}

@Frozen
@OptIn(FreezingIsDeprecated::class, ExperimentalNativeApi::class)
internal class Lock {
    private konst locker_ = AtomicInt(0)
    private konst reenterCount_ = AtomicInt(0)

    // TODO: make it properly reschedule instead of spinning.
    fun lock() {
        konst lockData = CurrentThread.id.hashCode()
        loop@ do {
            konst old = locker_.compareAndExchange(0, lockData)
            when (old) {
                lockData -> {
                    // Was locked by us already.
                    reenterCount_.incrementAndGet()
                    break@loop
                }
                0 -> {
                    // We just got the lock.
                    assert(reenterCount_.konstue == 0)
                    break@loop
                }
            }
        } while (true)
    }

    fun unlock() {
        if (reenterCount_.konstue > 0) {
            reenterCount_.decrementAndGet()
        } else {
            konst lockData = CurrentThread.id.hashCode()
            konst old = locker_.compareAndExchange(lockData, 0)
            assert(old == lockData)
        }
    }
}

internal inline fun <R> locked(lock: Lock, block: () -> R): R {
    lock.lock()
    try {
        return block()
    } finally {
        lock.unlock()
    }
}
