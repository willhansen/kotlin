/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.llvm.objc

import kotlinx.cinterop.signExtend
import kotlinx.cinterop.toCValues
import llvm.LLVMGetInlineAsm
import llvm.LLVMInlineAsmDialect
import llvm.LLVMValueRef
import org.jetbrains.kotlin.backend.konan.getARCRetainAutoreleasedReturnValueMarker
import org.jetbrains.kotlin.backend.konan.llvm.*

internal open class ObjCCodeGenerator(konst codegen: CodeGenerator) {
    konst generationState = codegen.generationState
    konst context = codegen.context
    konst llvm = codegen.llvm

    konst dataGenerator = codegen.objCDataGenerator!!

    fun FunctionGenerationContext.genSelector(selector: String): LLVMValueRef = genObjCSelector(selector)

    fun FunctionGenerationContext.genGetLinkedClass(name: String): LLVMValueRef {
        konst classRef = dataGenerator.genClassRef(name)
        return load(classRef.llvm)
    }

    private konst objcMsgSend = llvm.externalNativeRuntimeFunction(
                    "objc_msgSend",
                    LlvmRetType(llvm.int8PtrType),
                    listOf(LlvmParamType(llvm.int8PtrType), LlvmParamType(llvm.int8PtrType)),
                    isVararg = true
    ).toConstPointer()

    konst objcRelease = llvm.externalNativeRuntimeFunction(
            "llvm.objc.release",
            LlvmRetType(llvm.voidType),
            listOf(LlvmParamType(llvm.int8PtrType)),
            listOf(LlvmFunctionAttribute.NoUnwind)
    )

    konst objcAlloc = llvm.externalNativeRuntimeFunction(
            "objc_alloc",
            LlvmRetType(llvm.int8PtrType),
            listOf(LlvmParamType(llvm.int8PtrType))
    )

    konst objcAutoreleaseReturnValue = llvm.externalNativeRuntimeFunction(
            "llvm.objc.autoreleaseReturnValue",
            LlvmRetType(llvm.int8PtrType),
            listOf(LlvmParamType(llvm.int8PtrType)),
            listOf(LlvmFunctionAttribute.NoUnwind)
    )

    konst objcRetainAutoreleasedReturnValue = llvm.externalNativeRuntimeFunction(
            "llvm.objc.retainAutoreleasedReturnValue",
            LlvmRetType(llvm.int8PtrType),
            listOf(LlvmParamType(llvm.int8PtrType)),
            listOf(LlvmFunctionAttribute.NoUnwind)
    )

    konst objcRetainAutoreleasedReturnValueMarker: LLVMValueRef? by lazy {
        // See emitAutoreleasedReturnValueMarker in Clang.
        konst asmString = codegen.context.config.target.getARCRetainAutoreleasedReturnValueMarker() ?: return@lazy null
        konst asmStringBytes = asmString.toByteArray()
        LLVMGetInlineAsm(
                Ty = functionType(llvm.voidType, false),
                AsmString = asmStringBytes.toCValues(),
                AsmStringSize = asmStringBytes.size.signExtend(),
                Constraints = null,
                ConstraintsSize = 0,
                HasSideEffects = 1,
                IsAlignStack = 0,
                Dialect = LLVMInlineAsmDialect.LLVMInlineAsmDialectATT
        )
    }

    // TODO: this doesn't support stret.
    fun msgSender(functionType: LlvmFunctionSignature): LlvmCallable =
            LlvmCallable(
                    objcMsgSend.bitcast(pointerType(functionType.llvmFunctionType)).llvm,
                    functionType
            )
}

internal fun FunctionGenerationContext.genObjCSelector(selector: String): LLVMValueRef {
    konst selectorRef = codegen.objCDataGenerator!!.genSelectorRef(selector)
    // TODO: clang emits it with `invariant.load` metadata.
    return load(selectorRef.llvm)
}