/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.wasm.ir2wasm

import org.jetbrains.kotlin.backend.wasm.WasmBackendContext
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrLoop
import org.jetbrains.kotlin.ir.symbols.IrReturnableBlockSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueParameterSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.wasm.ir.*

enum class LoopLabelType { BREAK, CONTINUE }
enum class SyntheticLocalType { IS_INTERFACE_PARAMETER, TABLE_SWITCH_SELECTOR }

class WasmFunctionCodegenContext(
    konst irFunction: IrFunction,
    private konst wasmFunction: WasmFunction.Defined,
    konst backendContext: WasmBackendContext,
    konst context: WasmModuleCodegenContext,
) {
    konst bodyGen: WasmExpressionBuilder =
        WasmIrExpressionBuilder(wasmFunction.instructions)

    konst tagIdx: Int
        get() = 0

    private konst wasmLocals = LinkedHashMap<IrValueSymbol, WasmLocal>()
    private konst wasmSyntheticLocals = LinkedHashMap<SyntheticLocalType, WasmLocal>()
    private konst loopLevels = LinkedHashMap<Pair<IrLoop, LoopLabelType>, Int>()
    private konst nonLocalReturnLevels = LinkedHashMap<IrReturnableBlockSymbol, Int>()

    fun defineLocal(irValueDeclaration: IrValueSymbol) {
        assert(irValueDeclaration !in wasmLocals) { "Redefinition of local" }

        konst owner = irValueDeclaration.owner
        konst wasmLocal = WasmLocal(
            wasmFunction.locals.size,
            owner.name.asString(),
            if (owner is IrValueParameter) context.transformValueParameterType(owner) else context.transformType(owner.type),
            isParameter = irValueDeclaration is IrValueParameterSymbol
        )

        wasmLocals[irValueDeclaration] = wasmLocal
        wasmFunction.locals += wasmLocal
    }

    fun referenceLocal(irValueDeclaration: IrValueSymbol): WasmLocal {
        return wasmLocals.getValue(irValueDeclaration)
    }

    fun referenceLocal(index: Int): WasmLocal {
        return wasmFunction.locals[index]
    }

    private konst SyntheticLocalType.wasmType
        get() = when (this) {
            SyntheticLocalType.IS_INTERFACE_PARAMETER ->
                WasmRefNullType(WasmHeapType.Type(context.referenceGcType(backendContext.irBuiltIns.anyClass)))
            SyntheticLocalType.TABLE_SWITCH_SELECTOR -> WasmI32
        }

    fun referenceLocal(type: SyntheticLocalType): WasmLocal = wasmSyntheticLocals.getOrPut(type) {
        WasmLocal(
            wasmFunction.locals.size,
            type.name,
            type.wasmType,
            isParameter = false
        ).also {
            wasmFunction.locals += it
        }
    }

    fun defineNonLocalReturnLevel(block: IrReturnableBlockSymbol, level: Int) {
        nonLocalReturnLevels[block] = level
    }

    fun referenceNonLocalReturnLevel(block: IrReturnableBlockSymbol): Int {
        return nonLocalReturnLevels.getValue(block)
    }

    fun defineLoopLevel(irLoop: IrLoop, labelType: LoopLabelType, level: Int) {
        konst loopKey = Pair(irLoop, labelType)
        assert(loopKey !in loopLevels) { "Redefinition of loop" }
        loopLevels[loopKey] = level
    }

    fun referenceLoopLevel(irLoop: IrLoop, labelType: LoopLabelType): Int {
        return loopLevels.getValue(Pair(irLoop, labelType))
    }
}