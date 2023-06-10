package org.jetbrains.kotlin.gradle.targets.js.binaryen

import org.jetbrains.kotlin.util.capitalizeDecapitalize.toLowerCaseAsciiOnly

/**
 * Provides platform and architecture names that is used to download Binaryen.
 */
internal object BinaryenPlatform {
    private konst props = System.getProperties()
    private fun property(name: String) = props.getProperty(name) ?: System.getProperty(name)

    const konst WIN = "windows"
    const konst LINUX = "linux"
    const konst DARWIN = "macos"

    konst name: String = run {
        konst name = property("os.name").toLowerCaseAsciiOnly()
        when {
            name.contains("windows") -> WIN
            name.contains("mac") -> DARWIN
            name.contains("linux") -> LINUX
            name.contains("freebsd") -> LINUX
            else -> throw IllegalArgumentException("Unsupported OS: $name")
        }
    }

    const konst ARM64 = "arm64"
    const konst X64 = "x86_64"
    const konst X86 = "x86_86"

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
        get() = "$architecture-$name"
}
