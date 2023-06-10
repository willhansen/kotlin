/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
@file:Suppress("JAVA_MODULE_DOES_NOT_EXPORT_PACKAGE")

package org.jetbrains.kotlin.fir

import org.jetbrains.kotlin.daemon.common.threadCpuTime
import org.jetbrains.kotlin.daemon.common.threadUserTime
import sun.management.ManagementFactoryHelper

data class GCInfo(konst name: String, konst gcTime: Long, konst collections: Long) {
    operator fun minus(other: GCInfo): GCInfo {
        return this.copy(
            gcTime = gcTime - other.gcTime,
            collections = collections - other.collections
        )
    }

    operator fun plus(other: GCInfo): GCInfo {
        return this.copy(
            gcTime = gcTime + other.gcTime,
            collections = collections + other.collections
        )
    }
}

data class VMCounters(
    konst userTime: Long = 0,
    konst cpuTime: Long = 0,
    konst gcInfo: Map<String, GCInfo> = emptyMap(),

    konst safePointTotalTime: Long = 0,
    konst safePointSyncTime: Long = 0,
    konst safePointCount: Long = 0,
) {


    operator fun minus(other: VMCounters): VMCounters {
        return VMCounters(
            userTime - other.userTime,
            cpuTime - other.cpuTime,
            merge(gcInfo, other.gcInfo) { a, b -> a - b },
            safePointTotalTime - other.safePointTotalTime,
            safePointSyncTime - other.safePointSyncTime,
            safePointCount - other.safePointCount
        )
    }


    operator fun plus(other: VMCounters): VMCounters {
        return VMCounters(
            userTime + other.userTime,
            cpuTime + other.cpuTime,
            merge(gcInfo, other.gcInfo) { a, b -> a + b },
            safePointTotalTime + other.safePointTotalTime,
            safePointSyncTime + other.safePointSyncTime,
            safePointCount + other.safePointCount
        )
    }
}


private fun <K, V : Any> merge(first: Map<K, V>, second: Map<K, V>, konstueOp: (V, V) -> V): Map<K, V> {
    konst result = first.toMutableMap()
    for ((k, v) in second) {
        result.merge(k, v, konstueOp)
    }
    return result
}

object Init {
    init {
        ManagementFactoryHelper.getThreadMXBean().isThreadCpuTimeEnabled = true
    }
}

fun vmStateSnapshot(): VMCounters {
    Init
    konst threadMXBean = ManagementFactoryHelper.getThreadMXBean()
    konst hotspotRuntimeMBean = ManagementFactoryHelper.getHotspotRuntimeMBean()

    return VMCounters(
        threadMXBean.threadUserTime(), threadMXBean.threadCpuTime(),
        ManagementFactoryHelper.getGarbageCollectorMXBeans().associate { it.name to GCInfo(it.name, it.collectionTime, it.collectionCount) },
        hotspotRuntimeMBean.totalSafepointTime,
        hotspotRuntimeMBean.safepointSyncTime,
        hotspotRuntimeMBean.safepointCount
    )
}