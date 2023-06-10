/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package org.jetbrains.kotlin.backend.konan.llvm.coverage

import kotlinx.cinterop.*
import llvm.*
import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.backend.konan.NativeGenerationState
import org.jetbrains.kotlin.backend.konan.llvm.name
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.path
import org.jetbrains.kotlin.konan.file.File

private fun RegionKind.toLLVMCoverageRegionKind(): LLVMCoverageRegionKind = when (this) {
    RegionKind.Code -> LLVMCoverageRegionKind.CODE
    RegionKind.Gap -> LLVMCoverageRegionKind.GAP
    is RegionKind.Expansion -> LLVMCoverageRegionKind.EXPANSION
}

private fun LLVMCoverageRegion.populateFrom(region: Region, regionId: Int, filesIndex: Map<IrFile, Int>) = apply {
    fileId = filesIndex.getValue(region.file)
    lineStart = region.startLine
    columnStart = region.startColumn
    lineEnd = region.endLine
    columnEnd = region.endColumn
    counterId = regionId
    kind = region.kind.toLLVMCoverageRegionKind()
    expandedFileId = if (region.kind is RegionKind.Expansion) filesIndex.getValue(region.kind.expandedFile) else 0
}

/**
 * Writes all of the coverage information to the [org.jetbrains.kotlin.backend.konan.NativeGenerationState.llvm.module].
 * See http://llvm.org/docs/CoverageMappingFormat.html for the format description.
 */
internal class LLVMCoverageWriter(
        private konst generationState: NativeGenerationState,
        private konst filesRegionsInfo: List<FileRegionInfo>
) {
    fun write() {
        if (filesRegionsInfo.isEmpty()) return

        konst module = generationState.llvm.module
        konst filesIndex = filesRegionsInfo.mapIndexed { index, fileRegionInfo -> fileRegionInfo.file to index }.toMap()

        konst coverageGlobal = memScoped {
            konst (functionMappingRecords, functionCoverages) = filesRegionsInfo.flatMap { it.functions }.map { functionRegions ->
                konst regions = (functionRegions.regions.konstues).map { region ->
                    alloc<LLVMCoverageRegion>().populateFrom(region, functionRegions.regionEnumeration.getValue(region), filesIndex).ptr
                }
                konst fileIds = functionRegions.regions.map { filesIndex.getValue(it.konstue.file) }.toSet().toIntArray()
                konst functionCoverage = LLVMWriteCoverageRegionMapping(
                        fileIds.toCValues(), fileIds.size.signExtend(),
                        regions.toCValues(), regions.size.signExtend())

                konst functionName = generationState.llvmDeclarations.forFunction(functionRegions.function).name
                konst functionMappingRecord = LLVMAddFunctionMappingRecord(LLVMGetModuleContext(generationState.llvm.module),
                        functionName, functionRegions.structuralHash, functionCoverage)!!

                Pair(functionMappingRecord, functionCoverage)
            }.unzip()
            konst (filenames, fileIds) = filesIndex.entries.toList().map { File(it.key.path).absolutePath to it.konstue }.unzip()
            konst retkonst = LLVMCoverageEmit(module, functionMappingRecords.toCValues(), functionMappingRecords.size.signExtend(),
                    filenames.toCStringArray(this), fileIds.toIntArray().toCValues(), fileIds.size.signExtend(),
                    functionCoverages.map { it }.toCValues(), functionCoverages.size.signExtend())!!

            // TODO: Is there a better way to cleanup fields of T* type in `memScoped`?
            functionCoverages.forEach { LLVMFunctionCoverageDispose(it) }

            retkonst
        }
        generationState.llvm.usedGlobals.add(coverageGlobal)
    }
}
