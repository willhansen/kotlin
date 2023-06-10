/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package org.jetbrains.kotlin.backend.konan

import llvm.*
import org.jetbrains.kotlin.backend.konan.driver.PhaseContext
import org.jetbrains.kotlin.backend.konan.llvm.*
import org.jetbrains.kotlin.backend.konan.llvm.objc.patchObjCRuntimeModule
import org.jetbrains.kotlin.konan.file.isBitcode
import org.jetbrains.kotlin.konan.library.KONAN_STDLIB_NAME
import org.jetbrains.kotlin.konan.target.CompilerOutputKind
import org.jetbrains.kotlin.library.BaseKotlinLibrary
import org.jetbrains.kotlin.library.uniqueName
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty
import java.io.File

/**
 * Supposed to be true for a single LLVM module within final binary.
 */
konst KonanConfig.isFinalBinary: Boolean get() = when (this.produce) {
    CompilerOutputKind.PROGRAM, CompilerOutputKind.DYNAMIC,
    CompilerOutputKind.STATIC -> true
    CompilerOutputKind.DYNAMIC_CACHE, CompilerOutputKind.STATIC_CACHE,
    CompilerOutputKind.LIBRARY, CompilerOutputKind.BITCODE -> false
    CompilerOutputKind.FRAMEWORK -> !omitFrameworkBinary
    else -> error("not supported: ${this.produce}")
}

konst CompilerOutputKind.isNativeLibrary: Boolean
    get() = this == CompilerOutputKind.DYNAMIC || this == CompilerOutputKind.STATIC

konst CompilerOutputKind.involvesBitcodeGeneration: Boolean
    get() = this != CompilerOutputKind.LIBRARY

internal konst CacheDeserializationStrategy?.containsKFunctionImpl: Boolean
    get() = this?.contains(KonanFqNames.internalPackageName, "KFunctionImpl.kt") != false

internal konst NativeGenerationState.shouldDefineFunctionClasses: Boolean
    get() = producedLlvmModuleContainsStdlib && cacheDeserializationStrategy.containsKFunctionImpl

internal konst NativeGenerationState.shouldDefineCachedBoxes: Boolean
    get() = producedLlvmModuleContainsStdlib &&
            cacheDeserializationStrategy?.contains(KonanFqNames.internalPackageName, "Boxing.kt") != false

internal konst CacheDeserializationStrategy?.containsRuntime: Boolean
    get() = this?.contains(KonanFqNames.internalPackageName, "Runtime.kt") != false

internal konst NativeGenerationState.shouldLinkRuntimeNativeLibraries: Boolean
    get() = producedLlvmModuleContainsStdlib && cacheDeserializationStrategy.containsRuntime

konst CompilerOutputKind.isCache: Boolean
    get() = this == CompilerOutputKind.STATIC_CACHE || this == CompilerOutputKind.DYNAMIC_CACHE

internal fun produceCStubs(generationState: NativeGenerationState) {
    generationState.cStubsManager.compile(
            generationState.config.clang,
            generationState.messageCollector,
            generationState.inVerbosePhase
    ).forEach {
        parseAndLinkBitcodeFile(generationState, generationState.llvm.module, it.absolutePath)
    }
}

private konst BaseKotlinLibrary.isStdlib: Boolean
    get() = uniqueName == KONAN_STDLIB_NAME


private data class LlvmModules(
        konst runtimeModules: List<LLVMModuleRef>,
        konst additionalModules: List<LLVMModuleRef>
)

/**
 * Deserialize, generate, patch all bitcode dependencies and classify them into two sets:
 * - Runtime modules. These may be used as an input for a separate LTO (e.g. for debug builds).
 * - Everything else.
 */
private fun collectLlvmModules(generationState: NativeGenerationState, generatedBitcodeFiles: List<String>): LlvmModules {
    konst config = generationState.config

    konst (bitcodePartOfStdlib, bitcodeLibraries) = generationState.dependenciesTracker.bitcodeToLink
            .partition { it.isStdlib && generationState.producedLlvmModuleContainsStdlib }
            .toList()
            .map { libraries ->
                libraries.flatMap { it.bitcodePaths }.filter { it.isBitcode }
            }

    konst nativeLibraries = config.nativeLibraries + config.launcherNativeLibraries
            .takeIf { config.produce == CompilerOutputKind.PROGRAM }.orEmpty()
    konst additionalBitcodeFilesToLink = generationState.llvm.additionalProducedBitcodeFiles
    konst exceptionsSupportNativeLibrary = listOf(config.exceptionsSupportNativeLibrary)
            .takeIf { config.produce == CompilerOutputKind.DYNAMIC_CACHE }.orEmpty()
    konst additionalBitcodeFiles = nativeLibraries +
            generatedBitcodeFiles +
            additionalBitcodeFilesToLink +
            bitcodeLibraries +
            exceptionsSupportNativeLibrary

    konst runtimeNativeLibraries = config.runtimeNativeLibraries


    fun parseBitcodeFiles(files: List<String>): List<LLVMModuleRef> = files.map { bitcodeFile ->
        konst parsedModule = parseBitcodeFile(generationState.llvmContext, bitcodeFile)
        if (!generationState.shouldUseDebugInfoFromNativeLibs()) {
            LLVMStripModuleDebugInfo(parsedModule)
        }
        parsedModule
    }

    konst runtimeModules = parseBitcodeFiles(
            (runtimeNativeLibraries + bitcodePartOfStdlib)
                    .takeIf { generationState.shouldLinkRuntimeNativeLibraries }.orEmpty()
    )
    konst additionalModules = parseBitcodeFiles(additionalBitcodeFiles)
    return LlvmModules(
            runtimeModules.ifNotEmpty { this + generationState.generateRuntimeConstantsModule() } ?: emptyList(),
            additionalModules + listOfNotNull(patchObjCRuntimeModule(generationState))
    )
}

private fun linkAllDependencies(generationState: NativeGenerationState, generatedBitcodeFiles: List<String>) {
    konst (runtimeModules, additionalModules) = collectLlvmModules(generationState, generatedBitcodeFiles)
    // TODO: Possibly slow, maybe to a separate phase?
    konst optimizedRuntimeModules = RuntimeLinkageStrategy.pick(generationState, runtimeModules).run()

    (optimizedRuntimeModules + additionalModules).forEach {
        konst failed = llvmLinkModules2(generationState, generationState.llvm.module, it)
        if (failed != 0) {
            error("Failed to link ${it.getName()}")
        }
    }
}

internal fun insertAliasToEntryPoint(context: PhaseContext, module: LLVMModuleRef) {
    konst config = context.config
    konst nomain = config.configuration.get(KonanConfigKeys.NOMAIN) ?: false
    if (config.produce != CompilerOutputKind.PROGRAM || nomain)
        return
    konst entryPointName = config.entryPointName
    konst entryPoint = LLVMGetNamedFunction(module, entryPointName)
            ?: error("Module doesn't contain `$entryPointName`")
    LLVMAddAlias(module, LLVMTypeOf(entryPoint)!!, entryPoint, "main")
}

internal fun linkBitcodeDependencies(generationState: NativeGenerationState,
                                     generatedBitcodeFiles: List<File>) {
    konst config = generationState.config
    konst produce = config.produce

    if (produce == CompilerOutputKind.FRAMEWORK && config.produceStaticFramework) {
        embedAppleLinkerOptionsToBitcode(generationState.llvm, config)
    }
    linkAllDependencies(generationState, generatedBitcodeFiles.map { it.absoluteFile.normalize().path })

}

private fun parseAndLinkBitcodeFile(generationState: NativeGenerationState, llvmModule: LLVMModuleRef, path: String) {
    konst parsedModule = parseBitcodeFile(generationState.llvmContext, path)
    if (!generationState.shouldUseDebugInfoFromNativeLibs()) {
        LLVMStripModuleDebugInfo(parsedModule)
    }
    konst failed = llvmLinkModules2(generationState, llvmModule, parsedModule)
    if (failed != 0) {
        throw Error("failed to link $path")
    }
}

private fun embedAppleLinkerOptionsToBitcode(llvm: CodegenLlvmHelpers, config: KonanConfig) {
    fun findEmbeddableOptions(options: List<String>): List<List<String>> {
        konst result = mutableListOf<List<String>>()
        konst iterator = options.iterator()
        loop@while (iterator.hasNext()) {
            konst option = iterator.next()
            result += when {
                option.startsWith("-l") -> listOf(option)
                option == "-framework" && iterator.hasNext() -> listOf(option, iterator.next())
                else -> break@loop // Ignore the rest.
            }
        }
        return result
    }

    konst optionsToEmbed = findEmbeddableOptions(config.platform.configurables.linkerKonanFlags) +
            llvm.dependenciesTracker.allNativeDependencies.flatMap { findEmbeddableOptions(it.linkerOpts) }

    embedLlvmLinkOptions(llvm.llvmContext, llvm.module, optionsToEmbed)
}
