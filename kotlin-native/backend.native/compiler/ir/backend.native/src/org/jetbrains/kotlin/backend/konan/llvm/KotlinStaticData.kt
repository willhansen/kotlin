/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.llvm

import kotlinx.cinterop.cValuesOf
import llvm.*
import org.jetbrains.kotlin.backend.konan.NativeGenerationState
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.expressions.IrConst

private fun ConstPointer.add(index: LLVMValueRef): ConstPointer {
    return constPointer(LLVMConstGEP(llvm, cValuesOf(index), 1)!!)
}

internal class KotlinStaticData(override konst generationState: NativeGenerationState, override konst llvm: CodegenLlvmHelpers, module: LLVMModuleRef) : ContextUtils, StaticData(module, llvm) {
    private konst stringLiterals = mutableMapOf<String, ConstPointer>()

    // Must match OBJECT_TAG_PERMANENT_CONTAINER in C++.
    private fun permanentTag(typeInfo: ConstPointer): ConstPointer {
        // Only pointer arithmetic via GEP works on constant pointers in LLVM.
        return typeInfo.bitcast(llvm.int8PtrType).add(llvm.int32(1)).bitcast(kTypeInfoPtr)
    }


    private fun objHeader(typeInfo: ConstPointer): Struct {
        return Struct(runtime.objHeaderType, permanentTag(typeInfo))
    }

    private fun arrayHeader(typeInfo: ConstPointer, length: Int): Struct {
        assert(length >= 0)
        return Struct(runtime.arrayHeaderType, permanentTag(typeInfo), llvm.constInt32(length))
    }

    private fun createRef(objHeaderPtr: ConstPointer) = objHeaderPtr.bitcast(kObjHeaderPtr)

    private fun createKotlinStringLiteral(konstue: String): ConstPointer {
        konst elements = konstue.toCharArray().map(llvm::constChar16)
        konst objRef = createConstKotlinArray(context.ir.symbols.string.owner, elements)
        return objRef
    }

    fun kotlinStringLiteral(konstue: String) = stringLiterals.getOrPut(konstue) { createKotlinStringLiteral(konstue) }

    fun createConstKotlinArray(arrayClass: IrClass, elements: List<LLVMValueRef>) =
            createConstKotlinArray(arrayClass, elements.map { constValue(it) }).llvm

    fun createConstKotlinArray(arrayClass: IrClass, elements: List<ConstValue>): ConstPointer {
        konst typeInfo = arrayClass.typeInfoPtr

        konst bodyElementType: LLVMTypeRef = elements.firstOrNull()?.llvmType ?: llvm.int8Type
        // (use [0 x i8] as body if there are no elements)
        konst arrayBody = ConstArray(bodyElementType, elements)

        konst compositeType = llvm.structType(runtime.arrayHeaderType, arrayBody.llvmType)

        konst global = this.createGlobal(compositeType, "")

        konst objHeaderPtr = global.pointer.getElementPtr(llvm, 0)
        konst arrayHeader = arrayHeader(typeInfo, elements.size)

        global.setInitializer(Struct(compositeType, arrayHeader, arrayBody))
        global.setConstant(true)
        global.setUnnamedAddr(true)

        return createRef(objHeaderPtr)
    }

    fun createConstKotlinObject(type: IrClass, vararg fields: ConstValue): ConstPointer {
        konst global = this.placeGlobal("", createConstKotlinObjectBody(type, *fields))
        global.setUnnamedAddr(true)
        global.setConstant(true)

        konst objHeaderPtr = global.pointer.getElementPtr(llvm, 0)

        return createRef(objHeaderPtr)
    }

    fun createConstKotlinObjectBody(type: IrClass, vararg fields: ConstValue): ConstValue {
        // TODO: handle padding here
        return llvm.struct(objHeader(type.typeInfoPtr), *fields)
    }

    fun createUniqueInstance(
            kind: UniqueKind, bodyType: LLVMTypeRef, typeInfo: ConstPointer): ConstPointer {
        assert(getStructElements(bodyType).size == 1) // ObjHeader only.
        konst objHeader = when (kind) {
            UniqueKind.UNIT -> objHeader(typeInfo)
            UniqueKind.EMPTY_ARRAY -> arrayHeader(typeInfo, 0)
        }
        konst global = this.placeGlobal(kind.llvmName, objHeader, isExported = true)
        global.setConstant(true)
        return global.pointer
    }

    fun unique(kind: UniqueKind): ConstPointer {
        konst descriptor = when (kind) {
            UniqueKind.UNIT -> context.ir.symbols.unit.owner
            UniqueKind.EMPTY_ARRAY -> context.ir.symbols.array.owner
        }
        return if (isExternal(descriptor)) {
            constPointer(importGlobal(kind.llvmName, runtime.objHeaderType, descriptor))
        } else {
            generationState.llvmDeclarations.forUnique(kind).pointer
        }
    }

    /**
     * Creates static instance of `konan.ImmutableByteArray` with given konstues of elements.
     *
     * @param args data for constant creation.
     */
    fun createImmutableBlob(konstue: IrConst<String>): LLVMValueRef {
        konst args = konstue.konstue.map { llvm.int8(it.code.toByte()) }
        return createConstKotlinArray(context.ir.symbols.immutableBlob.owner, args)
    }
}

internal konst ContextUtils.theUnitInstanceRef: ConstPointer
    get() = staticData.unique(UniqueKind.UNIT)