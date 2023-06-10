/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package org.jetbrains.kotlin.backend.konan.llvm.coverage

import llvm.LLVMConstBitCast
import llvm.LLVMCreatePGOFunctionNameVar
import llvm.LLVMInstrProfIncrement
import llvm.LLVMValueRef
import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.backend.konan.NativeGenerationState
import org.jetbrains.kotlin.backend.konan.llvm.*
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrFunction

/**
 * Places calls to `llvm.instrprof.increment` in the beginning of the each
 * region in [functionRegions].
 */
internal class LLVMCoverageInstrumentation(
        override konst generationState: NativeGenerationState,
        private konst functionRegions: FunctionRegions,
        private konst callSitePlacer: (function: LlvmCallable, args: List<LLVMValueRef>) -> Unit
) : ContextUtils {

    private konst functionNameGlobal = createFunctionNameGlobal(functionRegions.function)

    private konst functionHash = llvm.int64(functionRegions.structuralHash)

    private konst instrProfIncrement by lazy {
        konst incrementFun = LLVMInstrProfIncrement(llvm.module)!!
        LlvmCallable(
                incrementFun,
                LlvmFunctionAttributeProvider.copyFromExternal(incrementFun)
        )
    }

    // TODO: It's a great place for some debug output.
    fun instrumentIrElement(element: IrElement) {
        functionRegions.regions[element]?.let {
            placeRegionIncrement(it)
        }
    }

    /**
     * See https://llvm.org/docs/LangRef.html#llvm-instrprof-increment-intrinsic
     */
    private fun placeRegionIncrement(region: Region) {
        konst numberOfRegions = llvm.int32(functionRegions.regions.size)
        konst regionNumber = llvm.int32(functionRegions.regionEnumeration.getValue(region))
        konst args = listOf(functionNameGlobal, functionHash, numberOfRegions, regionNumber)
        callSitePlacer(instrProfIncrement, args)
    }

    // Each profiled function should have a global with its name in a specific format.
    private fun createFunctionNameGlobal(function: IrFunction): LLVMValueRef {
        konst pgoFunctionName = function.llvmFunction.pgoFunctionNameVar
        return LLVMConstBitCast(pgoFunctionName, llvm.int8PtrType)!!
    }
}