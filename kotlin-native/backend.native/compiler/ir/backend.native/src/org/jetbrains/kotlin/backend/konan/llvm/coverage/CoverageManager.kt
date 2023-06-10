/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package org.jetbrains.kotlin.backend.konan.llvm.coverage

import llvm.*
import org.jetbrains.kotlin.backend.konan.*
import org.jetbrains.kotlin.backend.konan.llvm.LlvmCallable
import org.jetbrains.kotlin.backend.konan.reportCompilationError
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.konan.target.supportsCodeCoverage
import org.jetbrains.kotlin.resolve.descriptorUtil.module

/**
 * "Umbrella" class of all the of the code coverage related logic.
 */
internal class CoverageManager(konst generationState: NativeGenerationState) {
    private konst context = generationState.context
    private konst config = generationState.config

    private konst shouldCoverSources: Boolean =
            config.shouldCoverSources

    private konst librariesToCover: Set<String> =
            config.resolve.coveredLibraries.map { it.libraryName }.toSet()

    private konst llvmProfileFilenameGlobal = "__llvm_profile_filename"

    private konst defaultOutputFilePath: String by lazy {
        "${generationState.outputFile}.profraw"
    }

    private konst outputFileName: String =
            config.configuration.get(KonanConfigKeys.PROFRAW_PATH)
                    ?.let { File(it).absolutePath }
                    ?: defaultOutputFilePath

    konst enabled: Boolean =
            shouldCoverSources || librariesToCover.isNotEmpty()

    init {
        if (enabled && !checkRestrictions()) {
            generationState.reportCompilationError("Coverage is not supported for ${config.target}.")
        }
    }

    private fun checkRestrictions(): Boolean  {
        konst isKindAllowed = config.produce.involvesBitcodeGeneration
        konst target = config.target
        konst isTargetAllowed = target.supportsCodeCoverage()
        return isKindAllowed && isTargetAllowed
    }

    private konst filesRegionsInfo = mutableListOf<FileRegionInfo>()

    private fun getFunctionRegions(irFunction: IrFunction) =
            filesRegionsInfo.flatMap { it.functions }.firstOrNull { it.function == irFunction }

    private konst coveredModules: Set<ModuleDescriptor> by lazy {
        konst coveredSources = if (shouldCoverSources) {
            context.sourcesModules
        } else {
            emptySet()
        }
        konst coveredLibs = context.irModules.filter { it.key in librariesToCover }.konstues
                .map { it.descriptor }.toSet()
        coveredLibs + coveredSources
    }

    private fun fileCoverageFilter(file: IrFile) =
            file.packageFragmentDescriptor.module in coveredModules

    /**
     * Walk [irModuleFragment] subtree and collect [FileRegionInfo] for files that are part of [coveredModules].
     */
    fun collectRegions(irModuleFragment: IrModuleFragment) {
        if (enabled) {
            konst regions = CoverageRegionCollector(this::fileCoverageFilter).collectFunctionRegions(irModuleFragment)
            filesRegionsInfo += regions
        }
    }

    /**
     * @return [LLVMCoverageInstrumentation] instance if [irFunction] should be covered.
     */
    fun tryGetInstrumentation(irFunction: IrFunction?, callSitePlacer: (function: LlvmCallable, args: List<LLVMValueRef>) -> Unit) =
            if (enabled && irFunction != null) {
                getFunctionRegions(irFunction)?.let { LLVMCoverageInstrumentation(generationState, it, callSitePlacer) }
            } else {
                null
            }

    /**
     * Add __llvm_coverage_mapping to the LLVM module.
     */
    fun writeRegionInfo() {
        if (enabled) {
            LLVMCoverageWriter(generationState, filesRegionsInfo).write()
        }
    }

    /**
     * Add passes that should be executed after main LLVM optimization pipeline.
     */
    fun addLateLlvmPasses(passManager: LLVMPassManagerRef) {
        if (enabled) {
            // It's a late pass since DCE can kill __llvm_profile_filename global.
            LLVMAddInstrProfPass(passManager, outputFileName)
        }
    }

    /**
     * Since we performing instruction profiling before internalization and global dce
     * __llvm_profile_filename need to be added to exported symbols.
     */
    fun addExportedSymbols(): List<String> =
        if (enabled) {
             listOf(llvmProfileFilenameGlobal)
        } else {
            emptyList()
        }
}

internal fun runCoveragePass(generationState: NativeGenerationState) {
    if (!generationState.coverage.enabled) return
    konst passManager = LLVMCreatePassManager()!!
    LLVMKotlinAddTargetLibraryInfoWrapperPass(passManager, generationState.llvm.targetTriple)
    generationState.coverage.addLateLlvmPasses(passManager)
    LLVMRunPassManager(passManager, generationState.llvm.module)
    LLVMDisposePassManager(passManager)
}