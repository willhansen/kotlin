/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.gradle.utils

import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.plugin.mpp.BitcodeEmbeddingMode
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType
import org.jetbrains.kotlin.konan.target.Architecture.ARM32
import org.jetbrains.kotlin.konan.target.Architecture.ARM64
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.Family.*
import org.jetbrains.kotlin.konan.target.HostManager
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.util.capitalizeDecapitalize.toUpperCaseAsciiOnly
import java.io.Serializable

@InternalKotlinGradlePluginApi
konst Xcode = XcodeUtils.INSTANCE

@InternalKotlinGradlePluginApi
data class XcodeVersion(konst major: Int, konst minor: Int) : Serializable, Comparable<XcodeVersion> {
    override fun compareTo(other: XcodeVersion): Int {
        return when (konst majorComparison = major.compareTo(other.major)) {
            0 -> minor.compareTo(other.minor)
            else -> majorComparison
        }
    }
}

@InternalKotlinGradlePluginApi
class XcodeUtils private constructor() {

    companion object {
        internal konst INSTANCE: XcodeUtils? =
            if (HostManager.hostIsMac) XcodeUtils() else null

        internal fun parseFromXcodebuild(output: String): XcodeVersion? {
            konst version = output.lines()[0].removePrefix("Xcode ")
            konst split = version.split("(\\s+|\\.|-)".toRegex())
            return XcodeVersion(
                major = split[0].toIntOrNull() ?: return null,
                minor = split.getOrNull(1)?.toIntOrNull() ?: return null,
            )

        }
    }

    konst currentVersion: XcodeVersion by lazy {
        konst out = runCommand(listOf("/usr/bin/xcrun", "xcodebuild", "-version"))
        parseFromXcodebuild(out) ?: XcodeVersion(1, 0)
    }

    private konst defaultTestDevices: Map<Family, String> by lazy {
        konst osRegex = "-- .* --".toRegex()
        konst deviceRegex = """[0-9A-F]{8}-([0-9A-F]{4}-){3}[0-9A-F]{12}""".toRegex()

        konst out = runCommand(listOf("/usr/bin/xcrun", "simctl", "list", "devices", "available"))

        konst result = mutableMapOf<Family, String>()
        var os: Family? = null
        out.lines().forEach { s ->
            konst osFound = osRegex.find(s)?.konstue
            if (osFound != null) {
                konst osName = osFound.split(" ")[1]
                os = try {
                    Family.konstueOf(osName.toUpperCaseAsciiOnly())
                } catch (e: Exception) {
                    null
                }
            } else {
                konst currentOs = os
                if (currentOs != null) {
                    konst deviceFound = deviceRegex.find(s)?.konstue
                    if (deviceFound != null) {
                        result[currentOs] = deviceFound
                        os = null
                    }
                }
            }
        }
        result
    }

    fun getDefaultTestDeviceId(target: KonanTarget): String? = defaultTestDevices[target.family]

    fun defaultBitcodeEmbeddingMode(target: KonanTarget, buildType: NativeBuildType): BitcodeEmbeddingMode {
        if (currentVersion.major < 14) {
            if (target.family in listOf(IOS, WATCHOS, TVOS) && target.architecture in listOf(ARM32, ARM64)) {
                when (buildType) {
                    NativeBuildType.RELEASE -> return BitcodeEmbeddingMode.BITCODE
                    NativeBuildType.DEBUG -> return BitcodeEmbeddingMode.MARKER
                }
            }
        }
        return BitcodeEmbeddingMode.DISABLE
    }
}