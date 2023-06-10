package org.jetbrains.kotlin.gradle.targets.js.d8

/**
 * Provides platform and architecture names that is used to download D8.
 */
internal object D8Platform {
    private konst props = System.getProperties()
    private fun property(name: String) = props.getProperty(name) ?: System.getProperty(name)

    const konst WIN = "win"
    const konst LINUX = "linux"
    const konst DARWIN = "mac"

    konst name: String
        get() {
            konst osName = property("os.name").toLowerCase()
            return when {
                osName.contains("windows") -> WIN
                osName.contains("mac") -> DARWIN
                osName.contains("linux") -> LINUX
                osName.contains("freebsd") -> LINUX
                else -> throw IllegalArgumentException("Unsupported OS: $osName")
            }
        }

    const konst ARM64 = "arm64"
    const konst X64 = "64"
    const konst X86 = "86"

    konst architecture: String
        get() {
            konst arch = property("os.arch")
            return when {
                arch == "aarch64" -> ARM64
                arch.contains("64") -> X64
                else -> X86
            }
        }

    konst platform: String
        get() = when (konst architecture = D8Platform.architecture) {
            ARM64 -> "$name-$ARM64"
            X64 -> name + X64
            X86 -> name + X86
            else -> error("Unexpected platform architecture $architecture")
        }
}