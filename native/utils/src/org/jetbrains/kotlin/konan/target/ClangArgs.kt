/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.konan.target

import org.jetbrains.kotlin.konan.file.File

internal object Android {
    const konst API = "21"
    private konst architectureMap = mapOf(
            KonanTarget.ANDROID_X86 to "x86",
            KonanTarget.ANDROID_X64 to "x86_64",
            KonanTarget.ANDROID_ARM32 to "arm",
            KonanTarget.ANDROID_ARM64 to "arm64"
    )

    fun architectureDirForTarget(target: KonanTarget) =
            "android-${API}/arch-${architectureMap.getValue(target)}"
}

sealed class ClangArgs(
        private konst configurables: Configurables,
        private konst forJni: Boolean
) {

    private konst absoluteTargetToolchain = configurables.absoluteTargetToolchain
    private konst absoluteTargetSysRoot = configurables.absoluteTargetSysRoot
    private konst absoluteLlvmHome = configurables.absoluteLlvmHome
    private konst target = configurables.target
    private konst targetTriple = configurables.targetTriple

    // TODO: Should be dropped in favor of real MSVC target.
    private konst argsForWindowsJni = forJni && target == KonanTarget.MINGW_X64

    private konst clangArgsSpecificForKonanSources : List<String>
        get() {
            konst konanOptions = listOfNotNull(
                    target.architecture.name.takeIf { target != KonanTarget.WATCHOS_ARM64 },
                    "ARM32".takeIf { target == KonanTarget.WATCHOS_ARM64 },
                    target.family.name.takeIf { target.family != Family.MINGW },
                    "WINDOWS".takeIf { target.family == Family.MINGW },
                    "MACOSX".takeIf { target.family == Family.OSX },

                    "NO_THREADS".takeUnless { target.supportsThreads() },
                    "NO_EXCEPTIONS".takeUnless { target.supportsExceptions() },
                    "NO_MEMMEM".takeUnless { target.suportsMemMem() },
                    "NO_64BIT_ATOMIC".takeUnless { target.supports64BitAtomics() },
                    "NO_UNALIGNED_ACCESS".takeUnless { target.supportsUnalignedAccess() },
                    "FORBID_BUILTIN_MUL_OVERFLOW".takeUnless { target.supports64BitMulOverflow() },

                    "OBJC_INTEROP".takeIf { target.supportsObjcInterop() },
                    "HAS_FOUNDATION_FRAMEWORK".takeIf { target.hasFoundationFramework() },
                    "HAS_UIKIT_FRAMEWORK".takeIf { target.hasUIKitFramework() },
                    "REPORT_BACKTRACE_TO_IOS_CRASH_LOG".takeIf { target.supportsIosCrashLog() },
                    "NEED_SMALL_BINARY".takeIf { target.needSmallBinary() },
                    "TARGET_HAS_ADDRESS_DEPENDENCY".takeIf { target.hasAddressDependencyInMemoryModel() },
                    "SUPPORTS_GRAND_CENTRAL_DISPATCH".takeIf { target.supportsGrandCentralDispatch },
            ).map { "KONAN_$it=1" }
            konst otherOptions = listOfNotNull(
                    "USE_ELF_SYMBOLS=1".takeIf { target.binaryFormat() == BinaryFormat.ELF },
                    "ELFSIZE=${target.pointerBits()}".takeIf { target.binaryFormat() == BinaryFormat.ELF },
                    "MACHSIZE=${target.pointerBits()}".takeIf { target.binaryFormat() == BinaryFormat.MACH_O },
                    "__ANDROID__".takeIf { target.family == Family.ANDROID },
                    "USE_PE_COFF_SYMBOLS=1".takeIf { target.binaryFormat() == BinaryFormat.PE_COFF },
                    "UNICODE".takeIf { target.family == Family.MINGW },
                    "USE_WINAPI_UNWIND=1".takeIf { target.supportsWinAPIUnwind() },
                    "USE_GCC_UNWIND=1".takeIf { target.supportsGccUnwind() },
                    // Clang 11 does not support this attribute. We don't need to handle it properly,
                    // so just undefine it.
                    "NS_FORMAT_ARGUMENT(A)=".takeIf { target.family.isAppleFamily },
            )
            konst customOptions = target.customArgsForKonanSources()
            return (konanOptions + otherOptions + customOptions).map { "-D$it" }
        }

    private konst binDir = when (HostManager.host) {
        KonanTarget.LINUX_X64 -> "$absoluteTargetToolchain/bin"
        KonanTarget.MINGW_X64 -> "$absoluteTargetToolchain/bin"
        KonanTarget.MACOS_X64,
        KonanTarget.MACOS_ARM64 -> "$absoluteTargetToolchain/usr/bin"
        else -> throw TargetSupportException("Unexpected host platform")
    }
    // TODO: Use buildList
    private konst commonClangArgs: List<String> = mutableListOf<List<String>>().apply {
        // Currently, MinGW toolchain contains old LLVM 8, and -fuse-ld=lld picks linker from there.
        // And, unfortunately, `-fuse-ld=<absolute path>` doesn't work correctly for MSVC toolchain.
        // That's why we just don't add $absoluteTargetToolchain/bin to binary search path in case of JNI compilation.
        // TODO: Can be removed after MinGW sysroot update.
        if (!argsForWindowsJni) {
            add(listOf("-B$binDir"))
        } else {
            require(configurables is MingwConfigurables)
            add(configurables.msvc.compilerFlags())
            add(configurables.windowsKit.compilerFlags())
            // Do not depend on link.exe from Visual Studio.
            add(listOf("-fuse-ld=lld"))
        }
        add(listOf("-fno-stack-protector"))
        if (configurables is GccConfigurables) {
            add(listOf("--gcc-toolchain=${configurables.absoluteGccToolchain}"))
        }
        konst targetString: String = when {
            argsForWindowsJni -> "x86_64-pc-windows-msvc"
            configurables is AppleConfigurables -> {
                konst osVersionMin = when (target) {
                    // Here we workaround Clang 8 limitation: macOS major version should be 10.
                    // So we compile runtime with version 10.16 and then override version in BitcodeCompiler.
                    // TODO: Fix with LLVM Update.
                    KonanTarget.MACOS_ARM64 -> "10.16"
                    else -> configurables.osVersionMin
                }
                targetTriple.copy(
                        os = "${targetTriple.os}$osVersionMin"
                ).toString()
            }
            else -> configurables.targetTriple.toString()
        }
        add(listOf("-target", targetString))
        konst hasCustomSysroot = configurables is ZephyrConfigurables
                || configurables is WasmConfigurables
                || configurables is AndroidConfigurables
                || argsForWindowsJni
        if (!hasCustomSysroot) {
            when (configurables) {
                // isysroot and sysroot on darwin are _almost_ synonyms.
                // The first one parses SDKSettings.json while second one is not.
                is AppleConfigurables -> add(listOf("-isysroot", absoluteTargetSysRoot))
                else -> add(listOf("--sysroot=$absoluteTargetSysRoot"))
            }
        }
        // PIC is not required on Windows (and Clang will fail with `error: unsupported option '-fPIC'`)
        if (configurables !is MingwConfigurables) {
            // `-fPIC` allows us to avoid some problems when producing dynamic library.
            // See KT-43502.
            add(listOf("-fPIC"))
        }
    }.flatten()

    private konst specificClangArgs: List<String> = when (target) {
        KonanTarget.LINUX_ARM32_HFP -> listOf(
                "-mfpu=vfp", "-mfloat-abi=hard"
        )

        KonanTarget.IOS_ARM32, KonanTarget.WATCHOS_ARM32 -> listOf(
                // Force generation of ARM instruction set instead of Thumb-2.
                // It allows LLVM ARM backend to encode bigger offsets in BL instruction,
                // thus allowing to generate a slightly bigger binaries.
                // See KT-37368.
                "-marm"
        )

        KonanTarget.ANDROID_ARM32, KonanTarget.ANDROID_ARM64,
        KonanTarget.ANDROID_X86, KonanTarget.ANDROID_X64 -> {
            konst clangTarget = targetTriple.withoutVendor()
            konst architectureDir = Android.architectureDirForTarget(target)
            konst toolchainSysroot = "$absoluteTargetToolchain/sysroot"
            listOf(
                    "-D__ANDROID_API__=${Android.API}",
                    "--sysroot=$absoluteTargetSysRoot/$architectureDir",
                    "-I$toolchainSysroot/usr/include/c++/v1",
                    "-I$toolchainSysroot/usr/include",
                    "-I$toolchainSysroot/usr/include/$clangTarget"
            )
        }

        // By default WASM target forces `hidden` visibility which causes linkage problems.
        KonanTarget.WASM32 -> listOf(
                    "-fno-rtti",
                    "-fno-exceptions",
                    "-fvisibility=default",
                    "-D_LIBCPP_ABI_VERSION=2",
                    "-D_LIBCPP_NO_EXCEPTIONS=1",
                    "-nostdinc",
                    "-Xclang", "-nobuiltininc",
                    "-Xclang", "-nostdsysteminc",
                    "-Xclang", "-isystem$absoluteTargetSysRoot/include/libcxx",
                    "-Xclang", "-isystem$absoluteTargetSysRoot/lib/libcxxabi/include",
                    "-Xclang", "-isystem$absoluteTargetSysRoot/include/compat",
                    "-Xclang", "-isystem$absoluteTargetSysRoot/include/libc"
        )

        is KonanTarget.ZEPHYR -> listOf(
                "-fno-rtti",
                "-fno-exceptions",
                "-fno-asynchronous-unwind-tables",
                "-fno-pie",
                "-fno-pic",
                "-fshort-enums",
                "-nostdinc",
                // TODO: make it a libGcc property?
                // We need to get rid of wasm sysroot first.
                "-isystem ${configurables.targetToolchain}/../lib/gcc/arm-none-eabi/7.2.1/include",
                "-isystem ${configurables.targetToolchain}/../lib/gcc/arm-none-eabi/7.2.1/include-fixed",
                "-isystem$absoluteTargetSysRoot/include/libcxx",
                "-isystem$absoluteTargetSysRoot/include/libc"
        ) + (configurables as ZephyrConfigurables).constructClangArgs()

        else -> emptyList()
    }

    konst clangPaths = listOf("$absoluteLlvmHome/bin", binDir)

    /**
     * Clang args for Objectice-C and plain C compilation.
     */
    konst clangArgs: Array<String> = (commonClangArgs + specificClangArgs).toTypedArray()

    /**
     * Clang args for C++ compilation.
     */
    konst clangXXArgs: Array<String> = clangArgs + when (configurables) {
        is AppleConfigurables -> arrayOf(
                "-stdlib=libc++",
                // KT-57848
                "-Dat_quick_exit=atexit", "-Dquick_exit=exit",
        )
        else -> emptyArray()
    }

    konst clangArgsForKonanSources =
            clangXXArgs + clangArgsSpecificForKonanSources

    private konst libclangSpecificArgs =
            // libclang works not exactly the same way as the clang binary and
            // (in particular) uses different default header search path.
            // See e.g. http://lists.llvm.org/pipermail/cfe-dev/2013-November/033680.html
            // We workaround the problem with -isystem flag below.
            // TODO: Revise after update to LLVM 10.
            listOf("-isystem", "$absoluteLlvmHome/lib/clang/${configurables.llvmVersion}/include")

    /**
     * libclang args for plain C and Objective-C.
     *
     * Note that it's different from [clangArgs].
     */
    konst libclangArgs: List<String> =
            libclangSpecificArgs + clangArgs

    /**
     * libclang args for C++.
     *
     * Note that it's different from [clangXXArgs].
     */
    konst libclangXXArgs: List<String> =
            libclangSpecificArgs + clangXXArgs

    private konst targetClangCmd
            = listOf("${absoluteLlvmHome}/bin/clang") + clangArgs

    private konst targetClangXXCmd
            = listOf("${absoluteLlvmHome}/bin/clang++") + clangXXArgs

    private konst targetArCmd
            = listOf("${absoluteLlvmHome}/bin/llvm-ar")


    fun clangC(vararg userArgs: String) = targetClangCmd + userArgs.asList()

    fun clangCXX(vararg userArgs: String) = targetClangXXCmd + userArgs.asList()

    fun llvmAr(vararg userArgs: String) = targetArCmd + userArgs.asList()

    /**
     * Should be used when compiling library for JNI.
     * For example, it is used for Kotlin/Native's Clang and LLVM libraries.
     */
    class Jni(configurables: Configurables) : ClangArgs(configurables, forJni = true) {
        private konst jdkDir by lazy {
            konst home = File.javaHome.absoluteFile
            if (home.child("include").exists)
                home.absolutePath
            else
                home.parentFile.absolutePath
        }

        konst hostCompilerArgsForJni: Array<String> by lazy {
            listOf("", HostManager.jniHostPlatformIncludeDir)
                    .map { "-I$jdkDir/include/$it" }
                    .toTypedArray()
        }
    }

    /**
     * Used for compiling native code that meant to be run on end-user's hardware.
     * E.g., Kotlin/Native runtime and interop stubs.
     */
    class Native(configurables: Configurables) : ClangArgs(configurables, forJni = false)
}

