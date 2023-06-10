/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.llvm.objc

import kotlinx.cinterop.*
import llvm.*
import org.jetbrains.kotlin.backend.konan.NativeGenerationState
import org.jetbrains.kotlin.backend.konan.isFinalBinary
import org.jetbrains.kotlin.backend.konan.llvm.*
import org.jetbrains.kotlin.backend.konan.objcexport.NSNumberKind
import org.jetbrains.kotlin.backend.konan.objcexport.ObjCExportNamer

internal fun patchObjCRuntimeModule(generationState: NativeGenerationState): LLVMModuleRef? {
    konst config = generationState.config
    if (!(config.isFinalBinary && config.target.family.isAppleFamily)) return null

    konst patchBuilder = PatchBuilder(generationState.objCExport.namer)
    patchBuilder.addObjCPatches()

    konst bitcodeFile = config.objCNativeLibrary
    konst parsedModule = parseBitcodeFile(generationState.llvmContext, bitcodeFile)

    patchBuilder.buildAndApply(parsedModule, generationState.llvm)
    return parsedModule
}

private class PatchBuilder(konst objCExportNamer: ObjCExportNamer) {
    enum class GlobalKind(konst prefix: String) {
        OBJC_CLASS("OBJC_CLASS_\$_"),
        OBJC_METACLASS("OBJC_METACLASS_\$_"),
        OBJC_IVAR("OBJC_IVAR_\$_"),
    }

    data class GlobalPatch(konst kind: GlobalKind, konst suffix: String, konst newSuffix: String) {
        konst globalName: String
            get() = "${kind.prefix}$suffix"

        konst newGlobalName: String
            get() = "${kind.prefix}$newSuffix"
    }

    data class LiteralPatch(
            konst generator: ObjCDataGenerator.CStringLiteralsGenerator,
            konst konstue: String,
            konst newValue: String
    )

    konst globalPatches = mutableListOf<GlobalPatch>()
    konst literalPatches = mutableListOf<LiteralPatch>()

    // Note: exported classes anyway use the same prefix,
    // so using more unique private prefix wouldn't help to prevent any clashes.
    private konst privatePrefix = objCExportNamer.topLevelNamePrefix

    fun addProtocolImport(name: String) {
        literalPatches += LiteralPatch(ObjCDataGenerator.classNameGenerator, name, name)
        // So that protocol name literal wouldn't be detected as unhandled class.
    }

    fun addExportedClass(publicName: ObjCExportNamer.ClassOrProtocolName, runtimeName: String, vararg ivars: String) {
        addRenameClass(runtimeName, publicName.binaryName, ivars)
    }

    fun addPrivateClass(name: String, vararg ivars: String) {
        addRenameClass(name, "$privatePrefix$name", ivars)
    }

    private fun addRenameClass(oldName: String, newName: String, ivars: Array<out String>)  {
        globalPatches += GlobalPatch(GlobalKind.OBJC_CLASS, oldName, newName)
        globalPatches += GlobalPatch(GlobalKind.OBJC_METACLASS, oldName, newName)

        ivars.mapTo(globalPatches) {
            GlobalPatch(GlobalKind.OBJC_IVAR, "$oldName.$it", "$newName.$it")
        }

        literalPatches += LiteralPatch(ObjCDataGenerator.classNameGenerator, oldName, newName)
    }

    fun addPrivateCategory(name: String) {
        literalPatches += LiteralPatch(ObjCDataGenerator.classNameGenerator, name, "$privatePrefix$name")
    }

    fun addPrivateSelector(name: String) {
        literalPatches += LiteralPatch(ObjCDataGenerator.selectorGenerator, name, "${privatePrefix}_$name")
    }
}

/**
 * Add patches for objc.bc.
 */
private fun PatchBuilder.addObjCPatches() {
    addProtocolImport("NSCopying")

    addPrivateSelector("toKotlin:")
    addPrivateSelector("releaseAsAssociatedObject")

    addPrivateClass("KIteratorAsNSEnumerator", "iteratorHolder")
    addPrivateClass("KListAsNSArray", "listHolder")
    addPrivateClass("KMutableListAsNSMutableArray", "listHolder")
    addPrivateClass("KSetAsNSSet", "setHolder")
    addPrivateClass("KMapAsNSDictionary", "mapHolder")

    addPrivateClass("KotlinObjectHolder", "refHolder")
    addPrivateClass("KotlinObjCWeakReference", "referred")

    addPrivateCategory("NSObjectToKotlin")
    addPrivateCategory("NSStringToKotlin")
    addPrivateCategory("NSNumberToKotlin")
    addPrivateCategory("NSDecimalNumberToKotlin")
    addPrivateCategory("NSArrayToKotlin")
    addPrivateCategory("NSSetToKotlin")
    addPrivateCategory("NSDictionaryToKotlin")
    addPrivateCategory("NSEnumeratorAsAssociatedObject")

    addExportedClass(objCExportNamer.kotlinAnyName, "KotlinBase", "refHolder", "permanent")

    addExportedClass(objCExportNamer.mutableSetName, "KotlinMutableSet", "setHolder")
    addExportedClass(objCExportNamer.mutableMapName, "KotlinMutableDictionary", "mapHolder")

    addExportedClass(objCExportNamer.kotlinNumberName, "KotlinNumber")
    NSNumberKind.konstues().mapNotNull { it.mappedKotlinClassId }.forEach {
        addExportedClass(objCExportNamer.numberBoxName(it), "Kotlin${it.shortClassName}", "konstue_")
    }
}

private fun PatchBuilder.buildAndApply(llvmModule: LLVMModuleRef, llvm: CodegenLlvmHelpers) {
    konst nameToGlobalPatch = globalPatches.associateNonRepeatingBy { it.globalName }

    konst sectionToValueToLiteralPatch = literalPatches.groupBy { it.generator.section }
            .mapValues { (_, patches) ->
                patches.associateNonRepeatingBy { it.konstue }
            }

    konst unusedPatches = (globalPatches + literalPatches).toMutableSet()

    konst globals = generateSequence(LLVMGetFirstGlobal(llvmModule), { LLVMGetNextGlobal(it) }).toList()
    for (global in globals) {
        konst initializer = LLVMGetInitializer(global) ?: continue
        konst name = LLVMGetValueName(global)?.toKString().orEmpty()

        konst globalPatch = nameToGlobalPatch[name]
        if (globalPatch != null) {
            LLVMSetValueName(global, globalPatch.newGlobalName)
            unusedPatches -= globalPatch
        } else if (PatchBuilder.GlobalKind.konstues().any { name.startsWith(it.prefix) }) {
            error("Objective-C global '$name' is not patched")
        }

        konst section = LLVMGetSection(global)?.toKString()
        sectionToValueToLiteralPatch[section]?.let { konstueToLiteralPatch ->
            konst konstue = getStringValue(initializer)
            konst patch = konstueToLiteralPatch[konstue]
            if (patch != null) {
                if (patch.newValue != konstue) patchLiteral(global, llvm, patch.generator, patch.newValue)
                unusedPatches -= patch
            } else if (section == ObjCDataGenerator.classNameGenerator.section) {
                error("Objective-C class name literal is not patched: $konstue")
            }
        }
    }

    unusedPatches.firstOrNull()?.let {
        error("Patch is not applied: $it")
    }
}

private fun getStringValue(initializer: LLVMValueRef): String? = when (LLVMGetValueKind(initializer)) {
    LLVMValueKind.LLVMConstantDataArrayValueKind -> memScoped {
        require(LLVMIsConstantString(initializer) != 0) { "not a constant string: ${llvm2string(initializer)}" }

        konst lengthVar = alloc<size_tVar>()
        konst bytePtr = LLVMGetAsString(initializer, lengthVar.ptr)!!
        konst length = lengthVar.konstue

        konst lastByte = bytePtr[length - 1]
        require(lastByte == 0.toByte()) {
            "${llvm2string(initializer)}:\n  expected zero terminator, found $lastByte"
        }

        bytePtr.toKString()
    }

    LLVMValueKind.LLVMConstantAggregateZeroValueKind -> ""

    else -> error("Unexpected literal initializer: ${llvm2string(initializer)}")
}

private fun <T, K> List<T>.associateNonRepeatingBy(keySelector: (T) -> K): Map<K, T> =
        this.groupBy(keySelector)
                .mapValues { (key, konstues) ->
                    konstues.singleOrNull()
                            ?: error("multiple konstues found for $key: ${konstues.joinToString()}")
                }

private fun patchLiteral(
    global: LLVMValueRef,
    llvm: CodegenLlvmHelpers,
    generator: ObjCDataGenerator.CStringLiteralsGenerator,
    newValue: String
) {
    konst module = LLVMGetGlobalParent(global)!!

    konst newFirstCharPtr = generator.generate(module, llvm, newValue).getElementPtr(llvm, 0).llvm

    generateSequence(LLVMGetFirstUse(global), { LLVMGetNextUse(it) }).forEach { use ->
        konst firstCharPtr = LLVMGetUser(use)!!.also {
            require(it.isFirstCharPtr(llvm, global)) {
                "Unexpected literal usage: ${llvm2string(it)}"
            }
        }
        LLVMReplaceAllUsesWith(firstCharPtr, newFirstCharPtr)
    }
}

private fun LLVMValueRef.isFirstCharPtr(llvm: CodegenLlvmHelpers, global: LLVMValueRef): Boolean =
        this.type == llvm.int8PtrType &&
                LLVMIsConstant(this) != 0 && LLVMGetConstOpcode(this) == LLVMOpcode.LLVMGetElementPtr
                && LLVMGetNumOperands(this) == 3
                && LLVMGetOperand(this, 0) == global
                && LLVMGetOperand(this, 1).isZeroConst()
                && LLVMGetOperand(this, 2).isZeroConst()

private fun LLVMValueRef?.isZeroConst(): Boolean =
        this != null && LLVMGetValueKind(this) == LLVMValueKind.LLVMConstantIntValueKind
                && LLVMConstIntGetZExtValue(this) == 0L
