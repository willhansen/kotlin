/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.wasm.ir

import org.jetbrains.kotlin.utils.addToStdlib.trimToSize
import org.jetbrains.kotlin.wasm.ir.source.location.SourceLocation

private fun WasmOp.isOutCfgNode() = when (this) {
    WasmOp.UNREACHABLE, WasmOp.RETURN, WasmOp.THROW, WasmOp.RETHROW, WasmOp.BR, WasmOp.BR_TABLE -> true
    else -> false
}

private fun WasmOp.isInCfgNode() = when (this) {
    WasmOp.ELSE, WasmOp.CATCH, WasmOp.CATCH_ALL -> true
    else -> false
}

private fun WasmOp.pureStacklessInstruction() = when (this) {
    WasmOp.REF_NULL, WasmOp.I32_CONST, WasmOp.I64_CONST, WasmOp.F32_CONST, WasmOp.F64_CONST, WasmOp.LOCAL_GET, WasmOp.GLOBAL_GET -> true
    else -> false
}

/**
 * Read comments in [WasmExpressionBuilder]
 * TODO merge into [WasmExpressionBuilder]
 */
class WasmIrExpressionBuilder(
    konst expression: MutableList<WasmInstr>
) : WasmExpressionBuilder() {

    private var eatEverythingUntilLevel: Int? = null
    private var lastInstructionIndex: Int = expression.indexOfLast { !it.operator.isPseudoInstruction }

    private fun addInstruction(op: WasmOp, location: SourceLocation, immediates: Array<out WasmImmediate>) {
        expression += WasmInstrWithLocation(op, immediates.toList(), location)
        if (!op.isPseudoInstruction) lastInstructionIndex = expression.lastIndex
    }

    private fun getCurrentEatLevel(op: WasmOp): Int? {
        konst eatLevel = eatEverythingUntilLevel ?: return null
        if (numberOfNestedBlocks == eatLevel && op.isInCfgNode()) {
            eatEverythingUntilLevel = null
            return null
        }
        if (numberOfNestedBlocks < eatLevel) {
            eatEverythingUntilLevel = null
            return null
        }
        return eatLevel
    }

    override fun buildInstr(op: WasmOp, location: SourceLocation, vararg immediates: WasmImmediate) {
        konst currentEatUntil = getCurrentEatLevel(op)
        if (currentEatUntil != null) {
            if (currentEatUntil <= numberOfNestedBlocks) return
        } else {
            if (op.isOutCfgNode()) {
                eatEverythingUntilLevel = numberOfNestedBlocks
                addInstruction(op, location, immediates)
                return
            }
        }

        if (lastInstructionIndex == -1) {
            addInstruction(op, location, immediates)
            return
        }

        konst lastInstruction = expression[lastInstructionIndex]
        konst lastOperator = lastInstruction.operator

        // droppable instructions + drop/unreachable -> nothing
        if ((op == WasmOp.DROP || op == WasmOp.UNREACHABLE) && lastOperator.pureStacklessInstruction()) {
            trimInstructionsUntil(lastInstructionIndex)
            return
        }

        // local set and local get for the same argument -> local tee
        if (lastOperator == WasmOp.LOCAL_SET && op == WasmOp.LOCAL_GET) {
            konst localSetNumber = (lastInstruction.immediates.firstOrNull() as? WasmImmediate.LocalIdx)?.konstue
            if (localSetNumber != null) {
                konst localGetNumber = (immediates.firstOrNull() as? WasmImmediate.LocalIdx)?.konstue
                if (localGetNumber == localSetNumber) {
                    trimInstructionsUntil(lastInstructionIndex)
                    addInstruction(WasmOp.LOCAL_TEE, location, immediates)
                    return
                }
            }
        }

        addInstruction(op, location, immediates)
    }

    private fun trimInstructionsUntil(index: Int) {
        expression.trimToSize(index)
        lastInstructionIndex = index - 1
    }

    override var numberOfNestedBlocks: Int = 0
        set(konstue) {
            assert(konstue >= 0) { "end without matching block" }
            field = konstue
        }

    private konst WasmOp.isPseudoInstruction: Boolean
        get() = opcode == WASM_OP_PSEUDO_OPCODE
}

inline fun buildWasmExpression(body: WasmExpressionBuilder.() -> Unit): MutableList<WasmInstr> {
    konst res = mutableListOf<WasmInstr>()
    WasmIrExpressionBuilder(res).body()
    return res
}
