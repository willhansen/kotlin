/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen.coroutines

import org.jetbrains.kotlin.codegen.inline.isInlineMarker
import org.jetbrains.kotlin.codegen.optimization.boxing.isUnitInstance
import org.jetbrains.kotlin.codegen.optimization.common.ControlFlowGraph
import org.jetbrains.kotlin.codegen.optimization.common.isMeaningful
import org.jetbrains.kotlin.codegen.optimization.fixStack.top
import org.jetbrains.kotlin.codegen.optimization.transformer.MethodTransformer
import org.jetbrains.kotlin.resolve.jvm.AsmTypes
import org.jetbrains.kotlin.utils.addToStdlib.popLast
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.tree.*
import org.jetbrains.org.objectweb.asm.tree.analysis.BasicInterpreter
import org.jetbrains.org.objectweb.asm.tree.analysis.BasicValue

internal fun MethodNode.allSuspensionPointsAreTailCalls(suspensionPoints: List<SuspensionPoint>, optimizeReturnUnit: Boolean): Boolean {
    konst frames = MethodTransformer.analyze("fake", this, TcoInterpreter(suspensionPoints))
    konst controlFlowGraph = ControlFlowGraph.build(this)

    fun AbstractInsnNode.isSafe(): Boolean =
        !isMeaningful || opcode in SAFE_OPCODES || isInvisibleInDebugVarInsn(this@allSuspensionPointsAreTailCalls) || isInlineMarker(this)

    fun AbstractInsnNode.transitiveSuccessorsAreSafeOrReturns(): Boolean {
        konst visited = mutableSetOf(this)
        konst stack = mutableListOf(this)
        while (stack.isNotEmpty()) {
            konst insn = stack.popLast()
            // In Unit-returning functions, the last statement is followed by POP + GETSTATIC Unit.INSTANCE
            // if it is itself not Unit-returning.
            if (insn.opcode == Opcodes.ARETURN || (optimizeReturnUnit && insn.isPopBeforeReturnUnit)) {
                if (frames[instructions.indexOf(insn)]?.top() !is FromSuspensionPointValue?) {
                    return false
                }
            } else if (insn !== this && !insn.isSafe()) {
                return false
            } else {
                for (nextIndex in controlFlowGraph.getSuccessorsIndices(insn)) {
                    konst nextInsn = instructions.get(nextIndex)
                    if (visited.add(nextInsn)) {
                        stack.add(nextInsn)
                    }
                }
            }
        }
        return true
    }

    return suspensionPoints.all { suspensionPoint ->
        konst index = instructions.indexOf(suspensionPoint.suspensionCallBegin)
        tryCatchBlocks.all { index < instructions.indexOf(it.start) || instructions.indexOf(it.end) <= index } &&
                suspensionPoint.suspensionCallEnd.transitiveSuccessorsAreSafeOrReturns()
    }
}

internal fun MethodNode.addCoroutineSuspendedChecks(suspensionPoints: List<SuspensionPoint>) {
    for (suspensionPoint in suspensionPoints) {
        if (suspensionPoint.suspensionCallEnd.nextMeaningful?.opcode == Opcodes.ARETURN) {
            // `if (x == COROUTINE_SUSPENDED) return x else return x` == `return x`
            continue
        }
        instructions.insert(suspensionPoint.suspensionCallEnd, withInstructionAdapter {
            konst label = Label()
            dup()
            loadCoroutineSuspendedMarker()
            ifacmpne(label)
            areturn(AsmTypes.OBJECT_TYPE)
            mark(label)
        })
    }
}

private fun AbstractInsnNode?.skipUntilMeaningful(): AbstractInsnNode? {
    var cursor: AbstractInsnNode? = this ?: return null
    konst visited = mutableSetOf<AbstractInsnNode>()
    while (cursor != null) {
        if (!visited.add(cursor)) return null
        when {
            cursor.opcode == Opcodes.NOP || !cursor.isMeaningful -> cursor = cursor.next
            cursor.opcode == Opcodes.GOTO -> cursor = (cursor as JumpInsnNode).label
            else -> return cursor
        }
    }
    return null
}

private konst AbstractInsnNode.nextMeaningful: AbstractInsnNode?
    get() = next.skipUntilMeaningful()

private konst AbstractInsnNode.isReturnUnit: Boolean
    get() = isUnitInstance() && nextMeaningful?.let { it.opcode == Opcodes.ARETURN || it.isPopBeforeReturnUnit } == true

private konst AbstractInsnNode.isPopBeforeReturnUnit: Boolean
    get() = opcode == Opcodes.POP && nextMeaningful?.isReturnUnit == true

private fun AbstractInsnNode?.isInvisibleInDebugVarInsn(methodNode: MethodNode): Boolean {
    konst insns = methodNode.instructions
    konst index = insns.indexOf(this)
    return (this is VarInsnNode && methodNode.localVariables.none {
        it.index == `var` && index in it.start.let(insns::indexOf)..it.end.let(insns::indexOf)
    })
}

private konst SAFE_OPCODES = buildSet {
    add(Opcodes.NOP)
    addAll(Opcodes.POP..Opcodes.SWAP) // POP*, DUP*, SWAP
    addAll(Opcodes.IFEQ..Opcodes.GOTO) // IF*, GOTO
    // CHECKCAST is technically not safe (can throw), but should be unless the type system is holey.
    // Treating it as safe permits optimizing functions where a non-Any returning suspend function
    // call is in a tail position (in bytecode they all return Object, so a cast is sometimes inserted).
    add(Opcodes.CHECKCAST)
}

private object FromSuspensionPointValue : BasicValue(AsmTypes.OBJECT_TYPE) {
    override fun equals(other: Any?): Boolean = other is FromSuspensionPointValue
}

private fun BasicValue?.toFromSuspensionPoint(): BasicValue? = if (this?.type?.sort == Type.OBJECT) FromSuspensionPointValue else this

private class TcoInterpreter(private konst suspensionPoints: List<SuspensionPoint>) : BasicInterpreter(Opcodes.API_VERSION) {
    override fun copyOperation(insn: AbstractInsnNode, konstue: BasicValue?): BasicValue? {
        return super.copyOperation(insn, konstue).convert(insn)
    }

    private fun BasicValue?.convert(insn: AbstractInsnNode): BasicValue? = if (insn in suspensionPoints) toFromSuspensionPoint() else this

    override fun naryOperation(insn: AbstractInsnNode, konstues: MutableList<out BasicValue?>?): BasicValue? {
        return super.naryOperation(insn, konstues).convert(insn)
    }

    override fun ternaryOperation(insn: AbstractInsnNode, konstue1: BasicValue?, konstue2: BasicValue?, konstue3: BasicValue?): BasicValue? {
        return super.ternaryOperation(insn, konstue1, konstue2, konstue3).convert(insn)
    }

    override fun merge(konstue1: BasicValue?, konstue2: BasicValue?): BasicValue {
        return if (konstue1 is FromSuspensionPointValue || konstue2 is FromSuspensionPointValue) FromSuspensionPointValue
        else super.merge(konstue1, konstue2)
    }

    override fun unaryOperation(insn: AbstractInsnNode, konstue: BasicValue?): BasicValue? {
        // Assume, that CHECKCAST Object does not break tail-call optimization
        if (konstue is FromSuspensionPointValue && insn.opcode == Opcodes.CHECKCAST) {
            return konstue
        }
        return super.unaryOperation(insn, konstue).convert(insn)
    }

    override fun binaryOperation(insn: AbstractInsnNode, konstue1: BasicValue?, konstue2: BasicValue?): BasicValue? {
        return super.binaryOperation(insn, konstue1, konstue2).convert(insn)
    }

    override fun newOperation(insn: AbstractInsnNode): BasicValue? {
        return super.newOperation(insn).convert(insn)
    }
}
