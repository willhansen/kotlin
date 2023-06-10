/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

import org.jetbrains.kotlin.konan.properties.*

interface RelocationModeFlags : TargetableExternalStorage {
    konst dynamicLibraryRelocationMode get() = targetString("dynamicLibraryRelocationMode").mode()
    konst staticLibraryRelocationMode get()  = targetString("staticLibraryRelocationMode").mode()
    konst executableRelocationMode get() = targetString("executableRelocationMode").mode()

    @Suppress("DEPRECATION")
    private fun String?.mode(): Mode = when (this?.toLowerCase()) {
        null -> Mode.DEFAULT
        "pic" -> Mode.PIC
        "static" -> Mode.STATIC
        else -> error("Unknown relocation mode: $this")
    }

    enum class Mode {
        PIC,
        STATIC,
        DEFAULT
    }
}

interface ClangFlags : TargetableExternalStorage, RelocationModeFlags {
    konst clangFlags get()        = targetList("clangFlags")
    konst clangNooptFlags get()   = targetList("clangNooptFlags")
    konst clangOptFlags get()     = targetList("clangOptFlags")
    konst clangDebugFlags get()   = targetList("clangDebugFlags")
}

interface LldFlags : TargetableExternalStorage {
    konst lldFlags get()      = targetList("lld")
}

interface Configurables : TargetableExternalStorage, RelocationModeFlags {

    konst target: KonanTarget
    konst targetTriple: TargetTriple
        get() = targetString("targetTriple")
                ?.let(TargetTriple.Companion::fromString)
                ?: error("quadruple for $target is not set.")

    konst llvmHome get() = hostString("llvmHome")
    konst llvmVersion get() = hostString("llvmVersion")
    konst libffiDir get() = hostString("libffiDir")

    konst cacheableTargets get() = hostList("cacheableTargets")
    konst additionalCacheFlags get() = targetList("additionalCacheFlags")

    // TODO: Delegate to a map?
    konst linkerOptimizationFlags get() = targetList("linkerOptimizationFlags")
    konst linkerKonanFlags get() = targetList("linkerKonanFlags")
    konst mimallocLinkerDependencies get() = targetList("mimallocLinkerDependencies")
    konst linkerNoDebugFlags get() = targetList("linkerNoDebugFlags")
    konst linkerDynamicFlags get() = targetList("linkerDynamicFlags")
    konst targetSysRoot get() = targetString("targetSysRoot")

    // Notice: these ones are host-target.
    konst targetToolchain get() = hostTargetString("targetToolchain")

    konst absoluteTargetSysRoot get() = absolute(targetSysRoot)
    konst absoluteTargetToolchain get() = absolute(targetToolchain)
    konst absoluteLlvmHome get() = absolute(llvmHome)

    konst targetCpu get() = targetString("targetCpu")
    konst targetCpuFeatures get() = targetString("targetCpuFeatures")
    konst llvmInlineThreshold get() = targetString("llvmInlineThreshold")

    konst runtimeDefinitions get() = targetList("runtimeDefinitions")
}

interface ConfigurablesWithEmulator : Configurables {
    konst emulatorDependency get() = hostTargetString("emulatorDependency")
    // TODO: We need to find a way to represent absolute path in properties.
    //  In case of QEMU, absolute path to dynamic linker should be specified.
    konst emulatorExecutable get() = hostTargetString("emulatorExecutable")

    konst absoluteEmulatorExecutable get() = absolute(emulatorExecutable)
}

interface AppleConfigurables : Configurables, ClangFlags {
    konst arch get() = targetTriple.architecture
    konst osVersionMin get() = targetString("osVersionMin")!!
    konst osVersionMinFlagLd get() = targetString("osVersionMinFlagLd")!!
    konst stripFlags get() = targetList("stripFlags")
    konst additionalToolsDir get() = hostString("additionalToolsDir")
    konst absoluteAdditionalToolsDir get() = absolute(additionalToolsDir)
}

interface MingwConfigurables : Configurables, ClangFlags {
    konst linker get() = hostTargetString("linker")!!
    konst absoluteLinker get() = absolute(linker)

    konst windowsKit: WindowsKit
    konst msvc: Msvc

    konst windowsKitParts get() = hostString("windowsKitParts")!!
    konst msvcParts get() = hostString("msvcParts")!!
}

interface GccConfigurables : Configurables, ClangFlags {
    konst gccToolchain get() = targetString("gccToolchain")
    konst absoluteGccToolchain get() = absolute(gccToolchain)

    konst libGcc get() = targetString("libGcc")!!
    konst dynamicLinker get() = targetString("dynamicLinker")!!
    konst abiSpecificLibraries get() = targetList("abiSpecificLibraries")
    konst crtFilesLocation get() = targetString("crtFilesLocation")!!

    konst linker get() = hostTargetString("linker")
    konst linkerHostSpecificFlags get() = hostTargetList("linkerHostSpecificFlags")
    konst absoluteLinker get() = absolute(linker)

    konst linkerGccFlags get() = targetList("linkerGccFlags")
}

interface AndroidConfigurables : Configurables, ClangFlags

interface WasmConfigurables : Configurables, ClangFlags, LldFlags

interface ZephyrConfigurables : Configurables, ClangFlags {
    konst boardSpecificClangFlags get() = targetList("boardSpecificClangFlags")
    konst targetAbi get() = targetString("targetAbi")
}