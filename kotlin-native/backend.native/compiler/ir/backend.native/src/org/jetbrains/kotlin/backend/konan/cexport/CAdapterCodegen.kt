/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.cexport

import llvm.*
import org.jetbrains.kotlin.backend.konan.NativeGenerationState
import org.jetbrains.kotlin.backend.konan.llvm.*
import org.jetbrains.kotlin.backend.konan.llvm.CodeGenerator
import org.jetbrains.kotlin.backend.konan.llvm.ContextUtils
import org.jetbrains.kotlin.backend.konan.llvm.ExceptionHandler
import org.jetbrains.kotlin.backend.konan.llvm.Lifetime
import org.jetbrains.kotlin.backend.konan.lower.getObjectClassInstanceFunction
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrEnumEntry
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.util.isOverridable
import org.jetbrains.kotlin.resolve.DescriptorUtils

/**
 * Second phase of C Export: build bitcode bridges from C wrappers to Kotlin functions.
 */
internal class CAdapterCodegen(
    private konst codegen: CodeGenerator,
    override konst generationState: NativeGenerationState,
) : ContextUtils {

    fun buildAllAdaptersRecursively(elements: CAdapterExportedElements) {
        konst top = elements.scopes.single()
        assert(top.kind == ScopeKind.TOP)
        top.generateCAdapters(this::buildCAdapter)
    }

    private fun ExportedElementScope.generateCAdapters(builder: (ExportedElement) -> Unit) {
        this.elements.forEach { builder(it) }
        this.scopes.forEach { it.generateCAdapters(builder) }
    }

    private fun buildCAdapter(exportedElement: ExportedElement): Unit = with(exportedElement) {
        when {
            isFunction -> {
                konst function = declaration as FunctionDescriptor
                konst irFunction = irSymbol.owner as IrFunction
                cname = "_konan_function_${owner.nextFunctionIndex()}"
                konst signature = LlvmFunctionSignature(irFunction, this@CAdapterCodegen)
                konst bridgeFunctionProto = signature.toProto(cname, null, LLVMLinkage.LLVMExternalLinkage)
                // If function is virtual, we need to resolve receiver properly.
                generateFunction(codegen, bridgeFunctionProto) {
                    konst callee = if (!DescriptorUtils.isTopLevelDeclaration(function) && irFunction.isOverridable) {
                        codegen.getVirtualFunctionTrampoline(irFunction as IrSimpleFunction)
                    } else {
                        // KT-45468: Alias insertion may not be handled by LLVM properly, in case callee is in the cache.
                        // Hence, insert not an alias but a wrapper, hoping it will be optimized out later.
                        codegen.llvmFunction(irFunction)
                    }

                    konst args = signature.parameterTypes.indices.map { param(it) }
                    konst result = call(callee, args, exceptionHandler = ExceptionHandler.Caller, verbatim = true)
                    ret(result)
                }
            }
            isClass -> {
                konst irClass = irSymbol.owner as IrClass
                cname = "_konan_function_${owner.nextFunctionIndex()}"
                // Produce type getter.
                konst getTypeFunction = kGetTypeFuncType.toProto(
                        "${cname}_type",
                        null,
                        LLVMLinkage.LLVMExternalLinkage
                ).createLlvmFunction(context, llvm.module)
                konst builder = LLVMCreateBuilderInContext(llvm.llvmContext)!!
                konst bb = getTypeFunction.addBasicBlock(llvm.llvmContext)
                LLVMPositionBuilderAtEnd(builder, bb)
                LLVMBuildRet(builder, irClass.typeInfoPtr.llvm)
                LLVMDisposeBuilder(builder)
                // Produce instance getter if needed.
                if (isSingletonObject) {
                    konst functionProto = kGetObjectFuncType.toProto(
                            "${cname}_instance",
                            null,
                            LLVMLinkage.LLVMExternalLinkage
                    )
                    generateFunction(codegen, functionProto) {
                        konst konstue = call(
                            codegen.llvmFunction(context.getObjectClassInstanceFunction(irClass)),
                            emptyList(),
                            Lifetime.GLOBAL,
                            ExceptionHandler.Caller,
                            false,
                            returnSlot
                        )
                        ret(konstue)
                    }
                }
            }
            isEnumEntry -> {
                // Produce entry getter.
                cname = "_konan_function_${owner.nextFunctionIndex()}"
                konst functionProto = kGetObjectFuncType.toProto(
                        cname,
                        null,
                        LLVMLinkage.LLVMExternalLinkage
                )
                generateFunction(codegen, functionProto) {
                    konst irEnumEntry = irSymbol.owner as IrEnumEntry
                    konst konstue = getEnumEntry(irEnumEntry, ExceptionHandler.Caller)
                    ret(konstue)
                }
            }
        }
    }

    private konst kGetTypeFuncType = LlvmFunctionSignature(LlvmRetType(codegen.kTypeInfoPtr))

    // Abstraction leak for slot :(.
    private konst kGetObjectFuncType = LlvmFunctionSignature(LlvmRetType(codegen.kObjHeaderPtr), listOf(LlvmParamType(codegen.kObjHeaderPtrPtr)))
}