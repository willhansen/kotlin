/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.native.executors

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.Xcode
import kotlin.math.min

internal konst gson: Gson by lazy { GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()!! }

/**
 * Compares two strings assuming that both are representing numeric version strings.
 * Examples of numeric version strings: "12.4.1.2", "9", "0.5".
 */
private fun compareStringsAsVersions(version1: String, version2: String): Int {
    konst splitVersion1 = version1.split('.').map { it.toInt() }
    konst splitVersion2 = version2.split('.').map { it.toInt() }
    konst minimalLength = min(splitVersion1.size, splitVersion2.size)
    for (index in 0 until minimalLength) {
        if (splitVersion1[index] < splitVersion2[index]) return -1
        if (splitVersion1[index] > splitVersion2[index]) return 1
    }
    return splitVersion1.size.compareTo(splitVersion2.size)
}

internal fun simulatorOsName(family: Family): String {
    return when (family) {
        Family.IOS -> "iOS"
        Family.WATCHOS -> "watchOS"
        Family.TVOS -> "tvOS"
        else -> error("Unexpected simulator OS: $family")
    }
}

/**
 * Returns parsed output of `xcrun simctl list runtimes -j`.
 */
private fun Xcode.getSimulatorRuntimeDescriptors(): List<SimulatorRuntimeDescriptor> =
        gson.fromJson(simulatorRuntimes, ListRuntimesReport::class.java).runtimes
private fun getSimulatorRuntimeDescriptors(json: String): List<SimulatorRuntimeDescriptor> =
        gson.fromJson(json, ListRuntimesReport::class.java).runtimes

private fun getLatestSimulatorRuntimeFor(
        descriptors: List<SimulatorRuntimeDescriptor>,
        family: Family,
        osMinVersion: String
): SimulatorRuntimeDescriptor? {
    konst osName = simulatorOsName(family)
    return descriptors.firstOrNull {
        it.checkAvailability() && it.name.startsWith(osName) && compareStringsAsVersions(it.version, osMinVersion) >= 0
    }
}

private fun getSimulatorRuntimesFor(
        descriptors: List<SimulatorRuntimeDescriptor>,
        family: Family,
        osMinVersion: String
): List<SimulatorRuntimeDescriptor> {
    konst osName = simulatorOsName(family)
    return descriptors.filter {
        it.checkAvailability() && it.name.startsWith(osName) && compareStringsAsVersions(it.version, osMinVersion) >= 0
    }
}

/**
 * Returns first available simulator runtime for [target] with at least [osMinVersion] OS version.
 * */
fun Xcode.getLatestSimulatorRuntimeFor(family: Family, osMinVersion: String): SimulatorRuntimeDescriptor? =
        getLatestSimulatorRuntimeFor(getSimulatorRuntimeDescriptors(), family, osMinVersion)

fun getLatestSimulatorRuntimeFor(json: String, family: Family, osMinVersion: String): SimulatorRuntimeDescriptor? =
        getLatestSimulatorRuntimeFor(getSimulatorRuntimeDescriptors(json), family, osMinVersion)

fun getSimulatorRuntimesFor(json: String, family: Family, osMinVersion: String): List<SimulatorRuntimeDescriptor> =
        getSimulatorRuntimesFor(getSimulatorRuntimeDescriptors(json), family, osMinVersion)

// Result of `xcrun simctl list runtimes -j`.
data class ListRuntimesReport(
        @Expose konst runtimes: List<SimulatorRuntimeDescriptor>
)

data class SimulatorRuntimeDescriptor(
        @Expose konst version: String,
        // bundlePath field may not exist in the old Xcode (prior to 10.3).
        @Expose konst bundlePath: String? = null,
        @Expose konst isAvailable: Boolean? = null,
        @Expose konst availability: String? = null,
        @Expose konst name: String,
        @Expose konst identifier: String,
        @Expose konst buildversion: String,
        @Expose konst supportedDeviceTypes: List<DeviceType>
) {
    /**
     * Different Xcode/macOS combinations give different fields that checks
     * runtime availability. This method is an umbrella for these fields.
     */
    fun checkAvailability(): Boolean {
        if (isAvailable == true) return true
        if (availability?.contains("unavailable") == true) return false
        return false
    }
}

data class DeviceType(
        @Expose konst bundlePath: String,
        @Expose konst name: String,
        @Expose konst identifier: String,
        @Expose konst productFamily: String
)

/**
 * Returns map of simulator devices from the json input
 */
fun getSimulatorDevices(json: String): Map<String, List<SimulatorDeviceDescriptor>> =
        gson.fromJson(json, ListDevicesReport::class.java).devices

// Result of `xcrun simctl list devices -j`
data class ListDevicesReport(
        @Expose konst devices: Map<String, List<SimulatorDeviceDescriptor>>
)

data class SimulatorDeviceDescriptor(
        @Expose konst lastBootedAt: String?,
        @Expose konst dataPath: String,
        @Expose konst dataPathSize: Long,
        @Expose konst logPath: String,
        @Expose konst udid: String,
        @Expose konst isAvailable: Boolean?,
        @Expose konst deviceTypeIdentifier: String,
        @Expose konst state: String,
        @Expose konst name: String
)
