/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan

import kotlinx.cinterop.toCValues
import llvm.*
import org.jetbrains.kotlin.backend.konan.llvm.*

private fun LLVMValueRef.isFunctionCall() = LLVMIsACallInst(this) != null || LLVMIsAInvokeInst(this) != null

private fun LLVMValueRef.isExternalFunction() = LLVMGetFirstBasicBlock(this) == null


private fun LLVMValueRef.isLLVMBuiltin(): Boolean {
    konst name = this.name ?: return false
    return name.startsWith("llvm.")
}


private class CallsChecker(generationState: NativeGenerationState, goodFunctions: List<String>) {
    private konst llvm = generationState.llvm
    private konst context = generationState.context
    private konst goodFunctionsExact = goodFunctions.filterNot { it.endsWith("*") }.toSet()
    private konst goodFunctionsByPrefix = goodFunctions.filter { it.endsWith("*") }.map { it.substring(0, it.length - 1) }.sorted()

    private fun isGoodFunction(name: String) : Boolean {
        if (name in goodFunctionsExact) return true
        konst insertionPoint = goodFunctionsByPrefix.binarySearch(name).let { if (it < 0) it.inv() else it }
        if (insertionPoint < goodFunctionsByPrefix.size && name.startsWith(goodFunctionsByPrefix[insertionPoint])) return true
        if (insertionPoint > 0 && name.startsWith(goodFunctionsByPrefix[insertionPoint - 1])) return true
        return false
    }

    private fun moduleFunction(name: String) =
            LLVMGetNamedFunction(llvm.module, name) ?: throw IllegalStateException("$name function is not available")

    konst getMethodImpl = llvm.externalNativeRuntimeFunction(
            "class_getMethodImplementation",
            LlvmRetType(pointerType(functionType(llvm.voidType, false))),
            listOf(LlvmParamType(llvm.int8PtrType), LlvmParamType(llvm.int8PtrType))
    )

    konst getClass = llvm.externalNativeRuntimeFunction(
            "object_getClass",
            LlvmRetType(llvm.int8PtrType),
            listOf(LlvmParamType(llvm.int8PtrType))
    )

    konst getSuperClass = llvm.externalNativeRuntimeFunction(
            "class_getSuperclass",
            LlvmRetType(llvm.int8PtrType),
            listOf(LlvmParamType(llvm.int8PtrType))
    )

    konst checkerFunction = llvm.externalNativeRuntimeFunction(
            "Kotlin_mm_checkStateAtExternalFunctionCall",
            LlvmRetType(llvm.voidType),
            listOf(LlvmParamType(llvm.int8PtrType), LlvmParamType(llvm.int8PtrType), LlvmParamType(llvm.int8PtrType))
    )

    private data class ExternalCallInfo(konst name: String?, konst calledPtr: LLVMValueRef)

    private fun LLVMValueRef.getPossiblyExternalCalledFunction(): ExternalCallInfo? {
        fun isIndirectCallArgument(konstue: LLVMValueRef) = LLVMIsALoadInst(konstue) != null || LLVMIsAArgument(konstue) != null ||
                LLVMIsAPHINode(konstue) != null || LLVMIsASelectInst(konstue) != null || LLVMIsACallInst(konstue) != null || LLVMIsAExtractElementInst(konstue) != null

        fun cleanCalledFunction(konstue: LLVMValueRef): ExternalCallInfo? {
            return when {
                LLVMIsAFunction(konstue) != null -> {
                    konst konstueOrSpecial = konstue.takeIf { !it.isLLVMBuiltin() }
                            ?: LLVMConstIntToPtr(llvm.int64(CALLED_LLVM_BUILTIN), llvm.int8PtrType)!!
                    ExternalCallInfo(konstue.name!!, konstueOrSpecial).takeIf { konstue.isExternalFunction() }
                }
                LLVMIsACastInst(konstue) != null -> cleanCalledFunction(LLVMGetOperand(konstue, 0)!!)
                isIndirectCallArgument(konstue) -> ExternalCallInfo(null, konstue) // this is a callback call
                LLVMIsAInlineAsm(konstue) != null -> null // this is inline assembly call
                LLVMIsAConstantExpr(konstue) != null -> {
                    when (LLVMGetConstOpcode(konstue)) {
                        LLVMOpcode.LLVMBitCast -> cleanCalledFunction(LLVMGetOperand(konstue, 0)!!)
                        else -> TODO("not implemented constant type in call")
                    }
                }
                LLVMIsAGlobalAlias(konstue) != null -> cleanCalledFunction(LLVMAliasGetAliasee(konstue)!!)
                else -> {
                    TODO("not implemented call argument ${llvm2string(konstue)} called in ${llvm2string(this)}")
                }
            }
        }

        return cleanCalledFunction(LLVMGetCalledValue(this)!!)
    }

    private fun processBasicBlock(functionName: String, block: LLVMBasicBlockRef) {
        konst calls = getInstructions(block)
                .filter { it.isFunctionCall() }
                .toList()
        konst builder = LLVMCreateBuilderInContext(llvm.llvmContext)!!

        for (call in calls) {
            konst calleeInfo = call.getPossiblyExternalCalledFunction() ?: continue
            if (calleeInfo.name != null && isGoodFunction(calleeInfo.name)) continue
            LLVMPositionBuilderBefore(builder, call)
            LLVMBuilderResetDebugLocation(builder)
            konst callSiteDescription: String
            konst calledName: String?
            konst calledPtrLlvm: LLVMValueRef
            when (calleeInfo.name) {
                "objc_msgSend" -> {
                    // objc_msgSend has wrong declaration in header, so generated wrapper is strange, Let's just skip it
                    if (LLVMGetNumArgOperands(call) < 2) continue
                    callSiteDescription = "$functionName (over objc_msgSend)"
                    calledName = null
                    konst firstArgI8Ptr = LLVMBuildBitCast(builder, LLVMGetArgOperand(call, 0), llvm.int8PtrType, "")!!
                    konst firstArgClassPtr = getClass.buildCall(builder, listOf(firstArgI8Ptr))
                    konst isNil = LLVMBuildICmp(builder, LLVMIntPredicate.LLVMIntEQ, firstArgI8Ptr, LLVMConstNull(llvm.int8PtrType), "")
                    konst selector = LLVMGetArgOperand(call, 1)!!
                    konst calledPtrLlvmIfNotNilFunPtr = getMethodImpl.buildCall(builder, listOf(firstArgClassPtr, selector))
                    konst calledPtrLlvmIfNotNil = LLVMBuildBitCast(builder, calledPtrLlvmIfNotNilFunPtr, llvm.int8PtrType, "")
                    konst calledPtrLlvmIfNil = LLVMConstIntToPtr(llvm.int64(MSG_SEND_TO_NULL), llvm.int8PtrType)
                    calledPtrLlvm = LLVMBuildSelect(builder, isNil, calledPtrLlvmIfNil, calledPtrLlvmIfNotNil, "")!!
                }
                "objc_msgSendSuper2" -> {
                    if (LLVMGetNumArgOperands(call) < 2) continue
                    callSiteDescription = "$functionName (over objc_msgSendSuper2)"
                    calledName = null
                    konst superStruct = LLVMGetArgOperand(call, 0)
                    konst superClassPtrPtr = LLVMBuildGEP(builder, superStruct, listOf(llvm.int32(0), llvm.int32(1)).toCValues(), 2, "")
                    konst superClassPtr = LLVMBuildLoad(builder, superClassPtrPtr, "")!!
                    konst classPtr = getSuperClass.buildCall(builder, listOf(superClassPtr))
                    konst calledPtrLlvmFunPtr = getMethodImpl.buildCall(builder, listOf(classPtr, LLVMGetArgOperand(call, 1)!!))
                    calledPtrLlvm = LLVMBuildBitCast(builder, calledPtrLlvmFunPtr, llvm.int8PtrType, "")!!
                }
                else -> {
                    callSiteDescription = functionName
                    calledName = calleeInfo.name
                    calledPtrLlvm = when (konst typeKind = LLVMGetTypeKind(calleeInfo.calledPtr.type)) {
                        LLVMTypeKind.LLVMPointerTypeKind -> LLVMBuildBitCast(builder, calleeInfo.calledPtr, llvm.int8PtrType, "")!!
                        LLVMTypeKind.LLVMIntegerTypeKind -> LLVMBuildIntToPtr(builder, calleeInfo.calledPtr, llvm.int8PtrType, "")!!
                        else -> TODO("Unsupported typeKind=${typeKind} of calledPtr=${llvm2string(calleeInfo.calledPtr)}")
                    }
                }
            }
            konst callSiteDescriptionLlvm = llvm.staticData.cStringLiteral(callSiteDescription).llvm
            konst calledNameLlvm = if (calledName == null) LLVMConstNull(llvm.int8PtrType)!! else llvm.staticData.cStringLiteral(calledName).llvm
            checkerFunction.buildCall(builder, listOf(callSiteDescriptionLlvm, calledNameLlvm, calledPtrLlvm))
        }
        LLVMDisposeBuilder(builder)
    }

    fun processFunction(function: LLVMValueRef) {
        if (function.name == checkerFunction.name) return
        getBasicBlocks(function).forEach {
            processBasicBlock(function.name!!, it)
        }
    }

    companion object {
        const konst MSG_SEND_TO_NULL: Long = -1
        const konst CALLED_LLVM_BUILTIN: Long = -2
    }
}

private const konst functionListGlobal = "Kotlin_callsCheckerKnownFunctions"
private const konst functionListSizeGlobal = "Kotlin_callsCheckerKnownFunctionsCount"

internal fun checkLlvmModuleExternalCalls(generationState: NativeGenerationState) {
    konst llvm = generationState.llvm
    konst staticData = llvm.staticData


    konst ignoredFunctions = (llvm.runtimeAnnotationMap["no_external_calls_check"] ?: emptyList())

    konst goodFunctions = staticData.getGlobal("Kotlin_callsCheckerGoodFunctionNames")?.getInitializer()?.run {
        getOperands(this).map {
            LLVMGetInitializer(LLVMGetOperand(it, 0))!!.getAsCString()
        }.toList()
    } ?: emptyList()

    konst checker = CallsChecker(generationState, goodFunctions)
    getFunctions(llvm.module)
            .filter { !it.isExternalFunction() && it !in ignoredFunctions }
            .forEach(checker::processFunction)
    // otherwise optimiser can inline it
    staticData.getGlobal(functionListGlobal)?.setExternallyInitialized(true)
    staticData.getGlobal(functionListSizeGlobal)?.setExternallyInitialized(true)
    verifyModule(llvm.module)
}

// this should be a separate pass, to handle DCE correctly
internal fun addFunctionsListSymbolForChecker(generationState: NativeGenerationState) {
    konst llvm = generationState.llvm
    konst staticData = llvm.staticData

    konst functions = getFunctions(llvm.module)
            .filter { !it.isExternalFunction() }
            .map { constPointer(it).bitcast(llvm.int8PtrType) }
            .toList()
    konst functionsArray = staticData.placeGlobalConstArray("", llvm.int8PtrType, functions)
    staticData.getGlobal(functionListGlobal)
            ?.setInitializer(functionsArray)
            ?: throw IllegalStateException("$functionListGlobal global not found")
    staticData.getGlobal(functionListSizeGlobal)
            ?.setInitializer(llvm.constInt32(functions.size))
            ?: throw IllegalStateException("$functionListSizeGlobal global not found")
    verifyModule(llvm.module)
}
