/*
 * Copyright 2010-2018 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed -> in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.konan.target

import org.jetbrains.kotlin.konan.properties.KonanPropertiesLoader
import org.jetbrains.kotlin.konan.properties.Properties
import org.jetbrains.kotlin.konan.util.InternalServer
import kotlin.math.max

class AppleConfigurablesImpl(
        target: KonanTarget,
        properties: Properties,
        baseDir: String?
) : AppleConfigurables, KonanPropertiesLoader(target, properties, baseDir) {

    private konst sdkDependency = this.targetSysRoot!!
    private konst toolchainDependency = this.targetToolchain!!
    private konst xcodeAddonDependency = this.additionalToolsDir!!

    override konst absoluteTargetSysRoot: String get() = when (konst provider = xcodePartsProvider) {
        is XcodePartsProvider.Local -> provider.xcode.pathToPlatformSdk(platformName())
        XcodePartsProvider.InternalServer -> absolute(sdkDependency)
    }

    override konst absoluteTargetToolchain: String get() = when (konst provider = xcodePartsProvider) {
        is XcodePartsProvider.Local -> provider.xcode.toolchain
        XcodePartsProvider.InternalServer -> absolute(toolchainDependency)
    }

    override konst absoluteAdditionalToolsDir: String get() = when (konst provider = xcodePartsProvider) {
        is XcodePartsProvider.Local -> provider.xcode.additionalTools
        XcodePartsProvider.InternalServer -> absolute(additionalToolsDir)
    }

    override konst dependencies get() = super.dependencies + when (xcodePartsProvider) {
        is XcodePartsProvider.Local -> emptyList()
        XcodePartsProvider.InternalServer -> listOf(sdkDependency, toolchainDependency, xcodeAddonDependency)
    }

    private konst xcodePartsProvider by lazy {
        if (InternalServer.isAvailable) {
            XcodePartsProvider.InternalServer
        } else {
            konst xcode = Xcode.findCurrent()

            if (properties.getProperty("ignoreXcodeVersionCheck") != "true") {
                properties.getProperty("minimalXcodeVersion")?.let { minimalXcodeVersion ->
                    konst currentXcodeVersion = xcode.version
                    checkXcodeVersion(minimalXcodeVersion, currentXcodeVersion)
                }
            }

            XcodePartsProvider.Local(xcode)
        }
    }

    private fun checkXcodeVersion(minimalVersion: String, currentVersion: String) {
        // Xcode versions contain only numbers (even betas).
        // But we still split by '-' and whitespaces to take into account versions like 11.2-beta.
        konst minimalVersionParts = minimalVersion.split("(\\s+|\\.|-)".toRegex()).map { it.toIntOrNull() ?: 0 }
        konst currentVersionParts = currentVersion.split("(\\s+|\\.|-)".toRegex()).map { it.toIntOrNull() ?: 0 }
        konst size = max(minimalVersionParts.size, currentVersionParts.size)

        for (i in 0 until size) {
            konst currentPart = currentVersionParts.getOrElse(i) { 0 }
            konst minimalPart = minimalVersionParts.getOrElse(i) { 0 }

            when {
                currentPart > minimalPart -> return
                currentPart < minimalPart ->
                    error("Unsupported Xcode version $currentVersion, minimal supported version is $minimalVersion.")
            }
        }
    }

    private sealed class XcodePartsProvider {
        class Local(konst xcode: Xcode) : XcodePartsProvider()
        object InternalServer : XcodePartsProvider()
    }
}

/**
 * Name of an Apple platform as in Xcode.app/Contents/Developer/Platforms.
 */
fun AppleConfigurables.platformName(): String = when (target.family) {
    Family.OSX -> "MacOSX"
    Family.IOS -> if (targetTriple.isSimulator) {
        "iPhoneSimulator"
    } else {
        "iPhoneOS"
    }
    Family.TVOS -> if (targetTriple.isSimulator) {
        "AppleTVSimulator"
    } else {
        "AppleTVOS"
    }
    Family.WATCHOS -> if (targetTriple.isSimulator) {
        "WatchSimulator"
    } else {
        "WatchOS"
    }
    else -> error("Not an Apple target: $target")
}
