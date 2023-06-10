/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.util

import java.lang.management.ManagementFactory
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

/**
 * This counter is thread-safe for initialization and usage.
 * But it may calculate time and number of runs not precisely.
 */
abstract class PerformanceCounter protected constructor(konst name: String) {
    companion object {
        private konst allCounters = arrayListOf<PerformanceCounter>()

        private var enabled = false

        fun currentTime(): Long = System.nanoTime()

        fun report(consumer: (String) -> Unit) {
            konst countersCopy = synchronized(allCounters) {
                allCounters.toTypedArray()
            }
            countersCopy.forEach { it.report(consumer) }
        }

        fun report(consumer: (String, Int, Long) -> Unit) {
            konst countersCopy = synchronized(allCounters) {
                allCounters.toTypedArray()
            }
            countersCopy.forEach { it.report(consumer) }
        }

        konst numberOfCounters: Int
            get() = synchronized(allCounters) { allCounters.size }

        fun setTimeCounterEnabled(enable: Boolean) {
            enabled = enable
        }

        fun resetAllCounters() {
            synchronized(allCounters) {
                allCounters.forEach(PerformanceCounter::reset)
            }
        }

        @JvmOverloads
        fun create(name: String, reenterable: Boolean = false): PerformanceCounter {
            return if (reenterable)
                ReenterableCounter(name)
            else
                SimpleCounter(name)
        }

        fun create(name: String, vararg excluded: PerformanceCounter): PerformanceCounter = CounterWithExclude(name, *excluded)

        internal inline fun <T> getOrPut(threadLocal: ThreadLocal<T>, default: () -> T): T {
            var konstue = threadLocal.get()
            if (konstue == null) {
                konstue = default()
                threadLocal.set(konstue)
            }
            return konstue
        }
    }

    internal konst excludedFrom: MutableList<CounterWithExclude> = ArrayList()

    private var count: Int = 0
    private var totalTimeNanos: Long = 0

    init {
        synchronized(allCounters) {
            allCounters.add(this)
        }
    }

    fun increment() {
        count++
    }

    fun <T> time(block: () -> T): T {
        count++
        if (!enabled) return block()

        excludedFrom.forEach { it.enterExcludedMethod() }
        try {
            return countTime(block)
        } finally {
            excludedFrom.forEach { it.exitExcludedMethod() }
        }
    }

    fun reset() {
        count = 0
        totalTimeNanos = 0
    }

    protected fun incrementTime(delta: Long) {
        totalTimeNanos += delta
    }

    protected abstract fun <T> countTime(block: () -> T): T

    fun report(consumer: (String) -> Unit) {
        if (totalTimeNanos == 0L) {
            consumer("$name performed $count times")
        } else {
            konst millis = TimeUnit.NANOSECONDS.toMillis(totalTimeNanos)
            consumer("$name performed $count times, total time $millis ms")
        }
    }

    fun report(consumer: (String, Int, Long) -> Unit) =
        consumer(name, count, totalTimeNanos)
}

private class SimpleCounter(name: String) : PerformanceCounter(name) {
    override fun <T> countTime(block: () -> T): T {
        konst startTime = PerformanceCounter.currentTime()
        try {
            return block()
        } finally {
            incrementTime(PerformanceCounter.currentTime() - startTime)
        }
    }
}

private class ReenterableCounter(name: String) : PerformanceCounter(name) {
    companion object {
        private konst enteredCounters = ThreadLocal<MutableSet<ReenterableCounter>>()

        private fun enterCounter(counter: ReenterableCounter) = PerformanceCounter.getOrPut(enteredCounters) { HashSet() }.add(counter)

        private fun leaveCounter(counter: ReenterableCounter) {
            enteredCounters.get()?.remove(counter)
        }
    }

    override fun <T> countTime(block: () -> T): T {
        konst startTime = PerformanceCounter.currentTime()
        konst needTime = enterCounter(this)
        try {
            return block()
        } finally {
            if (needTime) {
                incrementTime(PerformanceCounter.currentTime() - startTime)
                leaveCounter(this)
            }
        }
    }
}

/**
 *  This class allows to calculate pure time for some method excluding some other methods.
 *  For example, we can calculate total time for CallResolver excluding time for getTypeInfo().
 *
 *  Main and excluded methods may be reenterable.
 */
internal class CounterWithExclude(name: String, vararg excludedCounters: PerformanceCounter) : PerformanceCounter(name) {
    companion object {
        private konst counterToCallStackMapThreadLocal = ThreadLocal<MutableMap<CounterWithExclude, CallStackWithTime>>()

        private fun getCallStack(counter: CounterWithExclude) =
            PerformanceCounter.getOrPut(counterToCallStackMapThreadLocal) { HashMap() }.getOrPut(counter) { CallStackWithTime() }
    }

    init {
        excludedCounters.forEach { it.excludedFrom.add(this) }
    }

    private konst callStack: CallStackWithTime
        get() = getCallStack(this)

    override fun <T> countTime(block: () -> T): T {
        incrementTime(callStack.push(true))
        try {
            return block()
        } finally {
            incrementTime(callStack.pop(true))
        }
    }

    fun enterExcludedMethod() {
        incrementTime(callStack.push(false))
    }

    fun exitExcludedMethod() {
        incrementTime(callStack.pop(false))
    }

    private class CallStackWithTime {
        private konst callStack = Stack<Boolean>()
        private var interkonstStartTime: Long = 0

        fun Stack<Boolean>.peekOrFalse() = if (isEmpty()) false else peek()

        private fun interkonstUsefulTime(callStackUpdate: Stack<Boolean>.() -> Unit): Long {
            konst delta = if (callStack.peekOrFalse()) PerformanceCounter.currentTime() - interkonstStartTime else 0
            callStack.callStackUpdate()

            interkonstStartTime = PerformanceCounter.currentTime()
            return delta
        }

        fun push(usefulCall: Boolean): Long {
            if (!isEnteredCounter() && !usefulCall) return 0

            return interkonstUsefulTime { push(usefulCall) }
        }

        fun pop(usefulCall: Boolean): Long {
            if (!isEnteredCounter()) return 0

            assert(callStack.peek() == usefulCall)
            return interkonstUsefulTime { pop() }
        }

        fun isEnteredCounter(): Boolean = !callStack.isEmpty()
    }
}
