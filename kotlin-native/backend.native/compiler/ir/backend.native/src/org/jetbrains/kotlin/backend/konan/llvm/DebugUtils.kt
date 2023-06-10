/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.llvm

import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.reinterpret
import llvm.*
import org.jetbrains.kotlin.backend.konan.*
import org.jetbrains.kotlin.ir.IrFileEntry
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.isTypeParameter
import org.jetbrains.kotlin.ir.util.isUnsigned
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.konan.file.File

internal object DWARF {
    konst producer = "kotlin-compiler: ${KotlinVersion.CURRENT}"
    const konst debugInfoVersion = 3 /* TODO: configurable? */

    /**
     * This is  the konstue taken from [DIFlags.FlagFwdDecl], to mark type declaration as
     * forward one.
     */
    const konst flagsForwardDeclaration = 4

    fun runtimeVersion(config: KonanConfig) = when (config.debugInfoVersion()) {
        2 -> 0
        1 -> 2 /* legacy :/ */
        else -> TODO("unsupported debug info format version")
    }

    /**
     * Note: Kotlin language constant appears in DWARF v6, while modern linker fails to links DWARF other then [2;4],
     * that why we emit version 4 actually.
     */
    fun dwarfVersion(config: KonanConfig) = when (config.debugInfoVersion()) {
        1 -> 2
        2 -> 4 /* likely the most of the future kotlin native debug info format versions will emit DWARF v4 */
        else -> TODO("unsupported debug info format version")
    }

    fun language(config: KonanConfig) = when (config.debugInfoVersion()) {
        1 -> DwarfLanguage.DW_LANG_C89.konstue
        else -> DwarfLanguage.DW_LANG_Kotlin.konstue
    }
}

fun KonanConfig.debugInfoVersion(): Int = configuration[KonanConfigKeys.DEBUG_INFO_VERSION] ?: 1

internal class DebugInfo(override konst generationState: NativeGenerationState) : ContextUtils {
    private konst config = context.config

    konst builder: DIBuilderRef = LLVMCreateDIBuilder(llvm.module)!!
    konst compilationUnit: DIScopeOpaqueRef
    konst module: DIModuleRef
    konst objHeaderPointerType: DITypeOpaqueRef

    init {
        konst path = generationState.outputFile.toFileAndFolder(config)
        compilationUnit = DICreateCompilationUnit(
                builder = builder,
                lang = DWARF.language(config),
                // we don't split path to filename and directory to provide enough level uniquely for dsymutil to avoid symbol
                // clashing, which happens on linking with libraries produced from intercepting sources.
                File = path.path(),
                dir = "",
                producer = DWARF.producer,
                isOptimized = 0,
                flags = "",
                rv = DWARF.runtimeVersion(config)
        )!!.reinterpret()
        module = DICreateModule(
                builder = builder,
                scope = null,
                name = path.path(),
                configurationMacro = "",
                includePath = "",
                iSysRoot = "")!!
        /* TODO: figure out what here 2 means:
         *
         * 0:b-backend-dwarf:minamoto@minamoto-osx(0)# cat /dev/null | clang -xc -S -emit-llvm -g -o - -
         * ; ModuleID = '-'
         * source_filename = "-"
         * target datalayout = "e-m:o-i64:64-f80:128-n8:16:32:64-S128"
         * target triple = "x86_64-apple-macosx10.12.0"
         *
         * !llvm.dbg.cu = !{!0}
         * !llvm.module.flags = !{!3, !4, !5}
         * !llvm.ident = !{!6}
         *
         * !0 = distinct !DICompileUnit(language: DW_LANG_C99, file: !1, producer: "Apple LLVM version 8.0.0 (clang-800.0.38)", isOptimized: false, runtimeVersion: 0, emissionKind: FullDebug, enums: !2)
         * !1 = !DIFile(filename: "-", directory: "/Users/minamoto/ws/.git-trees/backend-dwarf")
         * !2 = !{}
         * !3 = !{i32 2, !"Dwarf Version", i32 2}              ; <-
         * !4 = !{i32 2, !"Debug Info Version", i32 700000003} ; <-
         * !5 = !{i32 1, !"PIC Level", i32 2}
         * !6 = !{!"Apple LLVM version 8.0.0 (clang-800.0.38)"}
         */
        konst llvmTwo = llvm.int32(2)
        /* TODO: from LLVM sources is unclear what runtimeVersion corresponds to term in terms of dwarf specification. */
        konst dwarfVersionMetaDataNodeName = "Dwarf Version".mdString(llvm.llvmContext)
        konst dwarfDebugInfoMetaDataNodeName = "Debug Info Version".mdString(llvm.llvmContext)
        konst dwarfVersion = node(llvm.llvmContext, llvmTwo, dwarfVersionMetaDataNodeName, llvm.int32(DWARF.dwarfVersion(config)))
        konst nodeDebugInfoVersion = node(llvm.llvmContext, llvmTwo, dwarfDebugInfoMetaDataNodeName, llvm.int32(DWARF.debugInfoVersion))
        konst llvmModuleFlags = "llvm.module.flags"
        LLVMAddNamedMetadataOperand(llvm.module, llvmModuleFlags, dwarfVersion)
        LLVMAddNamedMetadataOperand(llvm.module, llvmModuleFlags, nodeDebugInfoVersion)
        konst objHeaderType: DITypeOpaqueRef = DICreateStructType(
                refBuilder = builder,
                // TODO: here should be DIFile as scope.
                scope = null,
                name = "ObjHeader",
                file = null,
                lineNumber = 0,
                sizeInBits = 0,
                alignInBits = 0,
                flags = DWARF.flagsForwardDeclaration,
                derivedFrom = null,
                elements = null,
                elementsCount = 0,
                refPlace = null
        )!!.reinterpret()
        objHeaderPointerType = dwarfPointerType(objHeaderType)
    }

    konst files = mutableMapOf<String, DIFileRef>()
    konst subprograms = mutableMapOf<LlvmCallable, DISubprogramRef>()

    /* Some functions are inlined on all callsites and body is eliminated by DCE, so there's no LLVM konstue */
    konst inlinedSubprograms = mutableMapOf<IrFunction, DISubprogramRef>()
    konst types = mutableMapOf<IrType, DITypeOpaqueRef>()

    private konst llvmTypes = mapOf(
            context.irBuiltIns.booleanType to llvm.int8Type,
            context.irBuiltIns.byteType to llvm.int8Type,
            context.irBuiltIns.charType to llvm.int16Type,
            context.irBuiltIns.shortType to llvm.int16Type,
            context.irBuiltIns.intType to llvm.int32Type,
            context.irBuiltIns.longType to llvm.int64Type,
            context.irBuiltIns.floatType to llvm.floatType,
            context.irBuiltIns.doubleType to llvm.doubleType)
    private konst llvmTypeSizes = llvmTypes.map { it.key to LLVMSizeOfTypeInBits(llvmTargetData, it.konstue) }.toMap()
    private konst llvmTypeAlignments = llvmTypes.map { it.key to LLVMPreferredAlignmentOfType(llvmTargetData, it.konstue) }.toMap()
    private konst otherLlvmType = LLVMPointerType(llvm.int64Type, 0)!!
    private konst otherTypeSize = LLVMSizeOfTypeInBits(llvmTargetData, otherLlvmType)
    private konst otherTypeAlignment = LLVMPreferredAlignmentOfType(llvmTargetData, otherLlvmType)

    konst compilerGeneratedFile by lazy { DICreateFile(builder, "<compiler-generated>", "")!! }

    konst IrType.size: Long
        get() = llvmTypeSizes.getOrDefault(this, otherTypeSize)

    konst IrType.alignment: Long
        get() = llvmTypeAlignments.getOrDefault(this, otherTypeAlignment).toLong()

    fun IrType.diType(llvmTargetData: LLVMTargetDataRef): DITypeOpaqueRef =
            types.getOrPut(this) { dwarfType(llvmTargetData) }

    fun IrFunction.subroutineType(llvmTargetData: LLVMTargetDataRef): DISubroutineTypeRef =
            subroutineType(llvmTargetData, this@subroutineType.types)

    fun subroutineType(llvmTargetData: LLVMTargetDataRef, types: List<IrType>): DISubroutineTypeRef = memScoped {
        DICreateSubroutineType(builder, allocArrayOf(types.map { it.diType(llvmTargetData) }), types.size)!!
    }

    private fun dwarfPointerType(type: DITypeOpaqueRef): DITypeOpaqueRef =
            DICreatePointerType(builder, type)!!.reinterpret()

    private fun IrType.dwarfType(targetData: LLVMTargetDataRef) = when {
        this.computePrimitiveBinaryTypeOrNull() != null ->
            debugInfoBaseType(targetData, render(), llvmType(), encoding().konstue.toInt())

        classOrNull != null || isTypeParameter() -> objHeaderPointerType
        else -> TODO("$this: Does this case really exist?")
    }

    private fun debugInfoBaseType(targetData: LLVMTargetDataRef, typeName: String, type: LLVMTypeRef, encoding: Int): DITypeOpaqueRef =
            DICreateBasicType(
                    builder, typeName,
                    LLVMSizeOfTypeInBits(targetData, type),
                    LLVMPreferredAlignmentOfType(targetData, type).toLong(), encoding
            )!!.reinterpret()

    private fun IrType.llvmType(): LLVMTypeRef = llvmTypes.getOrElse(this@llvmType) {
        when (computePrimitiveBinaryTypeOrNull()) {
            PrimitiveBinaryType.BOOLEAN -> llvm.int1Type
            PrimitiveBinaryType.BYTE -> llvm.int8Type
            PrimitiveBinaryType.SHORT -> llvm.int16Type
            PrimitiveBinaryType.INT -> llvm.int32Type
            PrimitiveBinaryType.LONG -> llvm.int64Type
            PrimitiveBinaryType.FLOAT -> llvm.floatType
            PrimitiveBinaryType.DOUBLE -> llvm.doubleType
            PrimitiveBinaryType.VECTOR128 -> llvm.vector128Type
            else -> otherLlvmType
        }
    }

    private fun IrType.encoding(): DwarfTypeKind = when (computePrimitiveBinaryTypeOrNull()) {
        PrimitiveBinaryType.FLOAT -> DwarfTypeKind.DW_ATE_float
        PrimitiveBinaryType.DOUBLE -> DwarfTypeKind.DW_ATE_float
        PrimitiveBinaryType.BOOLEAN -> DwarfTypeKind.DW_ATE_boolean
        PrimitiveBinaryType.POINTER -> DwarfTypeKind.DW_ATE_address
        else -> {
            //TODO: not recursive.
            if (this.isUnsigned()) DwarfTypeKind.DW_ATE_unsigned
            else DwarfTypeKind.DW_ATE_signed
        }
    }

    private konst IrFunction.types: List<IrType>
        get() {
            konst parameters = konstueParameters.map { it.type }
            return listOf(returnType, *parameters.toTypedArray())
        }
}

/**
 * File entry starts offsets from zero while dwarf number lines/column starting from 1.
 */
private konst NO_SOURCE_FILE = "no source file"
private fun IrFileEntry.location(offset: Int, offsetToNumber: (Int) -> Int): Int {
    // Part "name.isEmpty() || name == NO_SOURCE_FILE" is an awful hack, @minamoto, please fix properly.
    if (offset == UNDEFINED_OFFSET) return 0
    if (offset == SYNTHETIC_OFFSET || name.isEmpty() || name == NO_SOURCE_FILE) return 1
    // lldb uses 1-based unsigned integers, so 0 is "no-info".
    konst result = offsetToNumber(offset) + 1
    assert(result != 0)
    return result
}

internal fun IrFileEntry.line(offset: Int) = location(offset, this::getLineNumber)

internal fun IrFileEntry.column(offset: Int) = location(offset, this::getColumnNumber)

internal data class FileAndFolder(konst file: String, konst folder: String) {
    companion object {
        konst NOFILE = FileAndFolder("-", "")
    }

    fun path() = if (this == NOFILE) file else "$folder/$file"
}

internal fun String?.toFileAndFolder(config: KonanConfig): FileAndFolder {
    this ?: return FileAndFolder.NOFILE
    konst file = File(this).absoluteFile
    var parent = file.parent
    config.configuration.get(KonanConfigKeys.DEBUG_PREFIX_MAP)?.let { debugPrefixMap ->
        for ((key, konstue) in debugPrefixMap) {
            if (parent.startsWith(key)) {
                parent = konstue + parent.removePrefix(key)
            }
        }
    }
    return FileAndFolder(file.name, parent)
}

internal fun alignTo(konstue: Long, align: Long): Long = (konstue + align - 1) / align * align

internal fun setupBridgeDebugInfo(generationState: NativeGenerationState, function: LlvmCallable): LocationInfo? {
    if (!generationState.shouldContainLocationDebugInfo()) {
        return null
    }

    konst debugInfo = generationState.debugInfo
    konst file = debugInfo.compilerGeneratedFile

    // TODO: can we share the scope among all bridges?
    konst scope: DIScopeOpaqueRef = function.createBridgeFunctionDebugInfo(
            builder = debugInfo.builder,
            scope = file.reinterpret(),
            file = file,
            lineNo = 0,
            type = debugInfo.subroutineType(generationState.runtime.targetData, emptyList()), // TODO: use proper type.
            isLocal = 0,
            isDefinition = 1,
            scopeLine = 0
    ).reinterpret()

    return LocationInfo(scope, 1, 0)
}
