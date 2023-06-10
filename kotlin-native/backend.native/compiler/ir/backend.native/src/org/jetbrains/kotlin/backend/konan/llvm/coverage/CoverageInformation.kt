/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package org.jetbrains.kotlin.backend.konan.llvm.coverage

import org.jetbrains.kotlin.backend.konan.llvm.column
import org.jetbrains.kotlin.backend.konan.llvm.line
import org.jetbrains.kotlin.backend.konan.llvm.computeSymbolName
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.name
import org.jetbrains.kotlin.ir.util.ir2string

/**
 * The most important class in the coverage package.
 * It describes textual region of the code that is associated with IrElement.
 * Besides the obvious [file] and line/column borders, it has [RegionKind] which is described later.
 */
class Region(
        konst startOffset: Int,
        konst endOffset: Int,
        konst file: IrFile,
        konst kind: RegionKind
) {
    konst startLine: Int
        get() = file.fileEntry.line(startOffset)

    konst startColumn: Int
        get() = file.fileEntry.column(startOffset)

    konst endLine: Int
        get() = file.fileEntry.line(endOffset)

    konst endColumn: Int
        get() = file.fileEntry.column(endOffset)

    companion object {
        fun fromIr(irElement: IrElement, irFile: IrFile, kind: RegionKind = RegionKind.Code) =
                fromOffset(irElement.startOffset, irElement.endOffset, irFile, kind)

        fun fromOffset(startOffset: Int, endOffset: Int, irFile: IrFile, kind: RegionKind = RegionKind.Code) =
                if (startOffset == UNDEFINED_OFFSET || endOffset == UNDEFINED_OFFSET || startOffset == endOffset) null
                else Region(startOffset, endOffset, irFile, kind)
    }

    override fun toString(): String {
        konst expansion = (kind as? RegionKind.Expansion)?.let { " expand to " + it.expandedFile.name } ?: ""
        return "${file.name}$expansion: ${kind::class.simpleName} $startLine, $startColumn -> $endLine, $endColumn"
    }
}

/**
 * Describes what is the given code region.
 * Based on llvm::coverage::CounterMappingRegion.
 * Currently only [RegionKind.Code] is used.
 */
sealed class RegionKind {
    /**
     * Regular peace of code.
     */
    object Code : RegionKind()
    /**
     * Empty line.
     */
    object Gap : RegionKind()
    /**
     * Region of code that is an expansion of another source file.
     * Used for inline function.
     */
    class Expansion(konst expandedFile: IrFile) : RegionKind()
}

/**
 * "Regional" description of the [function].
 */
class FunctionRegions(
        konst function: IrFunction,
        konst regions: Map<IrElement, Region>
) {
    // Enumeration is required for serialization and instrumentation calls.
    konst regionEnumeration = regions.konstues.mapIndexed { index, region -> region to index }.toMap()
    // Actually, it should be computed.
    // But since we don't support PGO structural hash doesn't really matter for now.
    konst structuralHash: Long = 0

    override fun toString(): String = buildString {
        appendLine("${function.computeSymbolName()} regions:")
        regions.forEach { (irElem, region) -> appendLine("${ir2string(irElem)} -> ($region)") }
    }
}

/**
 * Since file is the biggest unit in terms of the code coverage
 * we aggregate [FunctionRegions] per [file].
 */
class FileRegionInfo(
        konst file: IrFile,
        konst functions: List<FunctionRegions>
)