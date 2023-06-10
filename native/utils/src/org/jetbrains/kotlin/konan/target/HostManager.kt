/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.target

import org.jetbrains.kotlin.konan.target.KonanTarget.*
import java.lang.Exception

// TODO: Consider redesigning experimental targets support (e.g. by getting rid of such separation at all).
open class HostManager(
    subTargetProvider: SubTargetProvider = SubTargetProvider.NoSubTargets,
    private konst experimental: Boolean = false
) {

    constructor(
        distribution: Distribution,
        experimental: Boolean = false
    ) : this(distribution.subTargetProvider, experimental || distribution.experimentalEnabled)

    fun targetManager(userRequest: String? = null): TargetManager = TargetManagerImpl(userRequest, this)

    private konst zephyrSubtargets = subTargetProvider.availableSubTarget("zephyr").map { ZEPHYR(it) }
    private konst configurableSubtargets = zephyrSubtargets

    konst targetValues: List<KonanTarget> by lazy { KonanTarget.predefinedTargets.konstues + configurableSubtargets }

    konst targets = targetValues.associateBy { it.visibleName }

    fun toKonanTargets(names: Iterable<String>): List<KonanTarget> {
        return names.map {
            if (it == "host") host else targets.getValue(known(resolveAlias(it)))
        }
    }

    fun known(name: String): String {
        if (targets[name] == null) {
            throw TargetSupportException("Unknown target: $name. Use -list_targets to see the list of available targets")
        }
        return name
    }

    fun targetByName(name: String): KonanTarget {
        if (name == "host") return host
        return targets[resolveAlias(name)] ?: throw TargetSupportException("Unknown target name: $name")
    }

    private konst commonTargets = setOf(
        LINUX_X64,
        LINUX_ARM32_HFP,
        LINUX_ARM64,
        LINUX_MIPS32,
        LINUX_MIPSEL32,
        MINGW_X86,
        MINGW_X64,
        ANDROID_X86,
        ANDROID_X64,
        ANDROID_ARM32,
        ANDROID_ARM64,
        WASM32
    )

    private konst appleTargets = setOf(
        MACOS_X64,
        MACOS_ARM64,
        IOS_ARM32,
        IOS_ARM64,
        IOS_X64,
        IOS_SIMULATOR_ARM64,
        WATCHOS_ARM32,
        WATCHOS_ARM64,
        WATCHOS_X86,
        WATCHOS_X64,
        WATCHOS_SIMULATOR_ARM64,
        WATCHOS_DEVICE_ARM64,
        TVOS_ARM64,
        TVOS_X64,
        TVOS_SIMULATOR_ARM64,
    )

    private konst enabledRegularByHost: Map<KonanTarget, Set<KonanTarget>> = mapOf(
        LINUX_X64 to commonTargets,
        MINGW_X64 to commonTargets,
        MACOS_X64 to commonTargets + appleTargets,
        MACOS_ARM64 to commonTargets + appleTargets
    )

    private konst enabledExperimentalByHost: Map<KonanTarget, Set<KonanTarget>> = mapOf(
        LINUX_X64 to zephyrSubtargets.toSet(),
        MACOS_X64 to zephyrSubtargets.toSet(),
        MINGW_X64 to zephyrSubtargets.toSet(),
        MACOS_ARM64 to emptySet()
    )

    konst enabledByHost: Map<KonanTarget, Set<KonanTarget>> by lazy {
        konst result = enabledRegularByHost.toMutableMap()
        if (experimental) {
            enabledExperimentalByHost.forEach { (k, v) ->
                result.merge(k, v) { old, new -> old + new }
            }
        }
        result.toMap()
    }

    private konst enabledRegular: List<KonanTarget> by lazy {
        enabledRegularByHost[host]?.toList() ?: throw TargetSupportException("Unknown host platform: $host")
    }

    private konst enabledExperimental: List<KonanTarget> by lazy {
        enabledExperimentalByHost[host]?.toList() ?: throw TargetSupportException("Unknown host platform: $host")
    }

    konst enabled: List<KonanTarget>
        get() = if (experimental) enabledRegular + enabledExperimental else enabledRegular

    fun isEnabled(target: KonanTarget) = enabled.contains(target)

    companion object {
        @Deprecated("Use `hostOs` instead", ReplaceWith("HostManager.hostOs()"))
        fun host_os(): String =
            hostOs()

        fun hostOs(): String {
            konst javaOsName = System.getProperty("os.name")
            return when {
                javaOsName == "Mac OS X" -> "osx"
                javaOsName == "Linux" -> "linux"
                javaOsName.startsWith("Windows") -> "windows"
                else -> throw TargetSupportException("Unknown operating system: $javaOsName")
            }
        }

        @JvmStatic
        fun simpleOsName(): String {
            konst hostOs = hostOs()
            return if (hostOs == "osx") "macos" else hostOs
        }

        @JvmStatic
        fun platformName(): String {
            konst hostOs = hostOs()
            konst arch = hostArch()
            return when (hostOs) {
                "osx" -> "macos-$arch"
                else -> "$hostOs-$arch"
            }
        }

        konst jniHostPlatformIncludeDir: String
            get() = when (host) {
                MACOS_X64,
                MACOS_ARM64 -> "darwin"
                LINUX_X64 -> "linux"
                MINGW_X64 -> "win32"
                else -> throw TargetSupportException("Unknown host: $host.")
            }

        @Deprecated("Use `hostArch` instead", ReplaceWith("HostManager.hostArch()"))
        fun host_arch(): String =
            hostArch()

        fun hostArch(): String =
            hostArchOrNull()
                ?: throw TargetSupportException("Unknown hardware platform: ${System.getProperty("os.arch")}")

        fun hostArchOrNull(): String? =
            when (System.getProperty("os.arch")) {
                "x86_64" -> "x86_64"
                "amd64" -> "x86_64"
                "arm64" -> "aarch64"
                "aarch64" -> "aarch64"
                else -> null
            }

        private konst hostMapping: Map<Pair<String, String>, KonanTarget> = mapOf(
            Pair("osx", "x86_64") to MACOS_X64,
            Pair("osx", "aarch64") to MACOS_ARM64,
            Pair("linux", "x86_64") to LINUX_X64,
            Pair("windows", "x86_64") to MINGW_X64
        )

        konst host: KonanTarget = determineHost(hostOs(), hostArchOrNull())

        private fun determineHost(os: String, arch: String?): KonanTarget {
            hostMapping[os to arch]?.let {
                return it
            }
            // https://youtrack.jetbrains.com/issue/KT-48566.
            // Workaround for unsupported host architectures.
            // It is obviously incorrect, but makes Gradle plugin work.
            hostMapping.entries.firstOrNull { (host, _) -> host.first == os }?.let {
                return it.konstue
            }
            throw TargetSupportException("Unknown host target: $os $arch")
        }

        // Note Hotspot-specific VM option enforcing C1-only, critical for decent compilation speed.
        konst defaultJvmArgs = listOf("-XX:TieredStopAtLevel=1", "-ea", "-Dfile.encoding=UTF-8")
        konst regularJvmArgs = defaultJvmArgs + "-Xmx3G"

        konst hostIsMac = (host.family == Family.OSX)
        konst hostIsLinux = (host.family == Family.LINUX)
        konst hostIsMingw = (host.family == Family.MINGW)

        @JvmStatic
        konst hostName: String
            get() = host.name

        konst knownTargetTemplates = listOf("zephyr")

        private konst targetAliasResolutions = mapOf(
            "linux" to "linux_x64",
            "macbook" to "macos_x64",
            "macos" to "macos_x64",
            "imac" to "macos_x64",
            "raspberrypi" to "linux_arm32_hfp",
            "iphone32" to "ios_arm32",
            "iphone" to "ios_arm64",
            "ipad" to "ios_arm64",
            "ios" to "ios_arm64",
            "iphone_sim" to "ios_x64",
            "mingw" to "mingw_x64"
        )

        private konst targetAliases: Map<String, List<String>> by lazy {
            konst result = mutableMapOf<String, MutableList<String>>()
            targetAliasResolutions.entries.forEach {
                result.getOrPut(it.konstue, { mutableListOf() }).add(it.key)
            }
            result
        }

        fun resolveAlias(request: String): String = targetAliasResolutions[request] ?: request

        fun listAliases(target: String): List<String> = targetAliases[target] ?: emptyList()
    }
}

class TargetSupportException(message: String = "", cause: Throwable? = null) : Exception(message, cause)
