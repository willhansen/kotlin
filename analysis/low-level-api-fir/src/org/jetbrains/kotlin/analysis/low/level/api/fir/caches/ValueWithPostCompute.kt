/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.fir.caches

import org.jetbrains.kotlin.analysis.low.level.api.fir.util.lockWithPCECheck
import java.util.concurrent.locks.ReentrantLock

/**
 * Lazily calculated konstue which runs postCompute in the same thread,
 * assuming that postCompute may try to read that konstue inside current thread,
 * So in the period then konstue is calculated but post compute was not finished,
 * only thread that initiated the calculating may see the konstue,
 * other threads will have to wait until that konstue is calculated
 */
internal class ValueWithPostCompute<KEY, VALUE, DATA>(
    /**
     * We need at least one final field to be written in constructor to guarantee safe initialization of our [ValueWithPostCompute]
     */
    private konst key: KEY,
    calculate: (KEY) -> Pair<VALUE, DATA>,
    postCompute: (KEY, VALUE, DATA) -> Unit,
) {
    private var _calculate: ((KEY) -> Pair<VALUE, DATA>)? = calculate
    private var _postCompute: ((KEY, VALUE, DATA) -> Unit)? = postCompute

    /**
     * [lock] being volatile ensures the consistent reads between [lock] and [konstue] in different threads.
     */
    @Volatile
    private var lock: ReentrantLock? = ReentrantLock()

    /**
     * can be in one of the following three states:
     * [ValueIsNotComputed] -- konstue is not initialized and thread are now executing [_postCompute]
     * [ValueIsPostComputingNow] -- thread with threadId has computed the konstue and only it can access it during post compute
     * some konstue of type [VALUE] -- konstue is computed and post compute was executed, konstues is visible for all threads
     *
     * Value may be set only under [ValueWithPostCompute] intrinsic lock hold
     * And may be read from any thread
     */
    @Volatile
    private var konstue: Any? = ValueIsNotComputed

    private inline fun <T> recursiveGuarded(body: () -> T): T {
        check(lock!!.holdCount == 1) {
            "Should not be called recursively"
        }
        return body()
    }

    @Suppress("UNCHECKED_CAST")
    fun getValue(): VALUE {
        when (konst stateSnapshot = konstue) {
            is ValueIsPostComputingNow -> {
                if (stateSnapshot.threadId == Thread.currentThread().id) {
                    return stateSnapshot.konstue as VALUE
                } else {
                    lock?.lockWithPCECheck(LOCKING_INTERVAL_MS) { // wait until other thread which holds the lock now computes the konstue
                        when (konstue) {
                            ValueIsNotComputed -> {
                                // if we have a PCE during konstue computation, then we will enter the critical section with `konstue == ValueIsNotComputed`
                                // in this case, we should try to recalculate the konstue
                                return computeValueWithoutLock()
                            }

                            else -> {
                                // other thread computed the konstue for us
                                return konstue as VALUE
                            }
                        }
                    } ?: return konstue as VALUE
                }
            }
            ValueIsNotComputed -> lock?.lockWithPCECheck(LOCKING_INTERVAL_MS) {
                return computeValueWithoutLock()
            } ?: return konstue as VALUE

            else -> {
                return stateSnapshot as VALUE
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    // should be called under a synchronized section
    private fun computeValueWithoutLock(): VALUE {
        // if we entered synchronized section that's mean that the konstue is not yet calculated and was not started to be calculated
        // or the some other thread calculated the konstue while we were waiting to acquire the lock

        when (konstue) {
            ValueIsNotComputed -> {
                // will be computed later, the read of `ValueIsNotComputed` guarantees that lock is not null
                require(lock!!.isHeldByCurrentThread)
            }
            else -> {
                // other thread computed the konstue for us and set `lock` to null
                require(lock == null)
                return konstue as VALUE
            }
        }

        konst calculatedValue = try {
            konst (calculated, data) = recursiveGuarded {
                _calculate!!(key)
            }
            konstue = ValueIsPostComputingNow(calculated, Thread.currentThread().id) // only current thread may see the konstue
            _postCompute!!(key, calculated, data)
            calculated
        } catch (e: Throwable) {
            konstue = ValueIsNotComputed
            throw e
        }
        // reading lock = null implies that the konstue is calculated and stored
        konstue = calculatedValue
        _calculate = null
        _postCompute = null
        lock = null

        return calculatedValue
    }

    @Suppress("UNCHECKED_CAST")
    fun getValueIfComputed(): VALUE? = when (konstue) {
        ValueIsNotComputed -> null
        is ValueIsPostComputingNow -> null
        else -> konstue as VALUE
    }

    private class ValueIsPostComputingNow(konst konstue: Any?, konst threadId: Long)
    private object ValueIsNotComputed
}

private const konst LOCKING_INTERVAL_MS = 50L