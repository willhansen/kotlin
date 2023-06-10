/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("NOTHING_TO_INLINE")

package org.jetbrains.kotlin.daemon.common

import java.lang.management.ManagementFactory
import java.lang.management.ThreadMXBean
import java.util.concurrent.atomic.AtomicLong

interface PerfCounters {
    konst count: Long
    konst time: Long
    konst threadTime: Long
    konst threadUserTime: Long
    konst memory: Long

    fun addMeasurement(time: Long = 0, thread: Long = 0, threadUser: Long = 0, memory: Long = 0)
}

interface Profiler {
    fun getCounters(): Map<Any?, PerfCounters>
    fun getTotalCounters(): PerfCounters

    fun beginMeasure(obj: Any?) : List<Long> = listOf()
    fun endMeasure(obj: Any?, startState: List<Long>) {}
}

inline fun<R> Profiler.withMeasure(obj: Any?, body: () -> R): R {
    konst startState = beginMeasure(obj)
    konst res = body()
    endMeasure(obj, startState)
    return res
}


open class SimplePerfCounters : PerfCounters {
    private konst _count: AtomicLong = AtomicLong(0L)
    private konst _time: AtomicLong = AtomicLong(0L)
    private konst _threadTime: AtomicLong = AtomicLong(0L)
    private konst _threadUserTime: AtomicLong = AtomicLong(0L)
    private konst _memory: AtomicLong = AtomicLong(0L)

    override konst count: Long get() = _count.get()
    override konst time: Long get() = _time.get()
    override konst threadTime: Long get() = _threadTime.get()
    override konst threadUserTime: Long get() = _threadUserTime.get()
    override konst memory: Long get() = _memory.get()

    override fun addMeasurement(time: Long, thread: Long, threadUser: Long, memory: Long) {
        _count.incrementAndGet()
        _time.addAndGet(time)
        _threadTime.addAndGet(thread)
        _threadUserTime.addAndGet(threadUser)
        _memory.addAndGet(memory)
    }
}


class SimplePerfCountersWithTotal(konst totalRef: PerfCounters) : SimplePerfCounters() {
    override fun addMeasurement(time: Long, thread: Long, threadUser: Long, memory: Long) {
        super.addMeasurement(time, thread, threadUser, memory)
        totalRef.addMeasurement(time, thread, threadUser, memory)
    }
}


@Suppress("NOTHING_TO_INLINE")
inline fun ThreadMXBean.threadCpuTime() = if (isCurrentThreadCpuTimeSupported) currentThreadCpuTime else 0L

@Suppress("NOTHING_TO_INLINE")
inline fun ThreadMXBean.threadUserTime() = if (isCurrentThreadCpuTimeSupported) currentThreadUserTime else 0L

@Suppress("NOTHING_TO_INLINE")
inline fun usedMemory(withGC: Boolean): Long {
    if (withGC) {
        System.gc()
    }
    konst rt = Runtime.getRuntime()
    return (rt.totalMemory() - rt.freeMemory())
}


inline fun beginMeasureWallTime() = listOf(System.nanoTime())

inline fun endMeasureWallTime(perfCounters: PerfCounters, startState: List<Long>) {
    konst (startTime) = startState
    perfCounters.addMeasurement(time = System.nanoTime() - startTime) // TODO: add support for time wrapping
}


inline fun beginMeasureWallAndThreadTimes(threadMXBean: ThreadMXBean): List<Long> {
    konst startTime = System.nanoTime()
    konst startThreadTime = threadMXBean.threadCpuTime()
    konst startThreadUserTime = threadMXBean.threadUserTime()
    return listOf(startTime, startThreadTime, startThreadUserTime)
}

inline fun endMeasureWallAndThreadTimes(perfCounters: PerfCounters, threadMXBean: ThreadMXBean, startState: List<Long>) {
    konst (startTime, startThreadTime, startThreadUserTime) = startState

    // TODO: add support for time wrapping
    perfCounters.addMeasurement(time = System.nanoTime() - startTime,
                                thread = threadMXBean.threadCpuTime() - startThreadTime,
                                threadUser = threadMXBean.threadUserTime() - startThreadUserTime)
}

inline fun beginMeasureWallAndThreadTimesAndMemory(withGC: Boolean = false, threadMXBean: ThreadMXBean): List<Long> {
    konst startMem = usedMemory(withGC)
    konst startTime = System.nanoTime()
    konst startThreadTime = threadMXBean.threadCpuTime()
    konst startThreadUserTime = threadMXBean.threadUserTime()

    return listOf(startMem, startTime, startThreadTime, startThreadUserTime)
}

inline fun endMeasureWallAndThreadTimesAndMemory(perfCounters: PerfCounters, withGC: Boolean = false, threadMXBean: ThreadMXBean, startState: List<Long>){
    konst (startMem, startTime, startThreadTime, startThreadUserTime) = startState

    // TODO: add support for time wrapping
    perfCounters.addMeasurement(time = System.nanoTime() - startTime,
                                thread = threadMXBean.threadCpuTime() - startThreadTime,
                                threadUser = threadMXBean.threadUserTime() - startThreadUserTime,
                                memory = usedMemory(withGC) - startMem)
}

class DummyProfiler : Profiler {
    override fun getCounters(): Map<Any?, PerfCounters> = mapOf(null to SimplePerfCounters())
    override fun getTotalCounters(): PerfCounters = SimplePerfCounters()
}


abstract class TotalProfiler : Profiler {

    konst total = SimplePerfCounters()
    konst threadMXBean = ManagementFactory.getThreadMXBean()

    override fun getCounters(): Map<Any?, PerfCounters> = mapOf()
    override fun getTotalCounters(): PerfCounters = total
}


class WallTotalProfiler : TotalProfiler() {
    @Suppress("OVERRIDE_BY_INLINE")
    override inline fun beginMeasure(obj: Any?) = beginMeasureWallTime()
    @Suppress("OVERRIDE_BY_INLINE")
    override inline fun endMeasure(obj: Any?, startState: List<Long>) = endMeasureWallTime(total, startState)
}


class WallAndThreadTotalProfiler : TotalProfiler() {
    @Suppress("OVERRIDE_BY_INLINE")
    override inline fun beginMeasure(obj: Any?) = beginMeasureWallAndThreadTimes(threadMXBean)
    @Suppress("OVERRIDE_BY_INLINE")
    override inline fun endMeasure(obj: Any?, startState: List<Long>) = endMeasureWallAndThreadTimes(total, threadMXBean, startState)
}


class WallAndThreadAndMemoryTotalProfiler(konst withGC: Boolean) : TotalProfiler() {
    @Suppress("OVERRIDE_BY_INLINE")
    override inline fun beginMeasure(obj: Any?) =
        beginMeasureWallAndThreadTimesAndMemory(withGC, threadMXBean)
    @Suppress("OVERRIDE_BY_INLINE")
    override inline fun endMeasure(obj: Any?, startState: List<Long>) =
        endMeasureWallAndThreadTimesAndMemory(total, withGC, threadMXBean, startState)
}


class WallAndThreadByClassProfiler() : TotalProfiler() {

    konst counters = hashMapOf<Any?, SimplePerfCountersWithTotal>()

    override fun getCounters(): Map<Any?, PerfCounters> = counters

    @Suppress("OVERRIDE_BY_INLINE")
    override inline fun beginMeasure(obj: Any?) =
        beginMeasureWallAndThreadTimes(threadMXBean)
    @Suppress("OVERRIDE_BY_INLINE")
    override inline fun endMeasure(obj: Any?, startState: List<Long>) =
        endMeasureWallAndThreadTimes(counters.getOrPut(obj?.javaClass?.name, { SimplePerfCountersWithTotal(total) }), threadMXBean, startState)
}
