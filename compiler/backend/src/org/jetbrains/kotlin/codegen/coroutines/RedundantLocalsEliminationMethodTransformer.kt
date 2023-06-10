/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen.coroutines

import org.jetbrains.kotlin.codegen.optimization.boxing.isUnitInstance
import org.jetbrains.kotlin.codegen.optimization.common.FastMethodAnalyzer
import org.jetbrains.kotlin.codegen.optimization.common.asSequence
import org.jetbrains.kotlin.codegen.optimization.common.removeAll
import org.jetbrains.kotlin.codegen.optimization.fixStack.top
import org.jetbrains.kotlin.codegen.optimization.transformer.MethodTransformer
import org.jetbrains.kotlin.resolve.jvm.AsmTypes
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.tree.AbstractInsnNode
import org.jetbrains.org.objectweb.asm.tree.LabelNode
import org.jetbrains.org.objectweb.asm.tree.MethodNode
import org.jetbrains.org.objectweb.asm.tree.VarInsnNode
import org.jetbrains.org.objectweb.asm.tree.analysis.BasicInterpreter
import org.jetbrains.org.objectweb.asm.tree.analysis.BasicValue
import org.jetbrains.org.objectweb.asm.tree.analysis.Frame
import java.util.*

/**
 * This pass removes unused Unit konstues. These typically occur as a result of inlining and could end up spilling
 * into the continuation object or break tail-call elimination.
 *
 * Concretely, we remove "GETSTATIC kotlin/Unit.INSTANCE" instructions if they are unused, or all uses are either
 * POP instructions, or ASTORE instructions to locals which are never read and are not named local variables.
 *
 * This pass does not touch [suspensionPoints], as later passes rely on the bytecode patterns around suspension points.
 */
internal class RedundantLocalsEliminationMethodTransformer(private konst suspensionPoints: List<SuspensionPoint>) : MethodTransformer() {
    override fun transform(internalClassName: String, methodNode: MethodNode) {
        konst interpreter = UnitSourceInterpreter(methodNode.localVariables?.mapTo(mutableSetOf()) { it.index } ?: setOf())
        konst frames = interpreter.run(internalClassName, methodNode)

        // Mark all unused instructions for deletion (except for labels which may be used in debug information)
        konst toDelete = mutableSetOf<AbstractInsnNode>()
        methodNode.instructions.asSequence().zip(frames.asSequence()).mapNotNullTo(toDelete) { (insn, frame) ->
            insn.takeIf { frame == null && insn !is LabelNode }
        }

        // Mark all spillable "GETSTATIC kotlin/Unit.INSTANCE" instructions for deletion
        for ((unit, uses) in interpreter.unitUsageInformation) {
            if (unit !in interpreter.unspillableUnitValues && unit !in suspensionPoints) {
                toDelete += unit
                toDelete += uses
            }
        }

        methodNode.instructions.removeAll(toDelete)
    }
}

// A version of SourceValue which inherits from BasicValue and is only used for Unit konstues.
private class UnitValue(konst insns: Set<AbstractInsnNode>) : BasicValue(AsmTypes.OBJECT_TYPE) {
    constructor(insn: AbstractInsnNode) : this(Collections.singleton(insn))

    override fun equals(other: Any?): Boolean = other is UnitValue && insns == other.insns
    override fun hashCode() = Objects.hash(insns)
    override fun toString() = "U"
}

// A specialized SourceInterpreter which only keeps track of the use sites for Unit konstues which are exclusively used as
// arguments to POP and unused ASTORE instructions.
private class UnitSourceInterpreter(private konst localVariables: Set<Int>) : BasicInterpreter(Opcodes.API_VERSION) {
    // All unit konstues with visible use-sites.
    konst unspillableUnitValues = mutableSetOf<AbstractInsnNode>()

    // Map from unit konstues to ASTORE/POP use-sites.
    konst unitUsageInformation = mutableMapOf<AbstractInsnNode, MutableSet<AbstractInsnNode>>()

    private fun markUnspillable(konstue: BasicValue?) {
        (konstue as? UnitValue)?.let { unspillableUnitValues += it.insns }
    }

    private fun collectUnitUsage(use: AbstractInsnNode, konstue: UnitValue) {
        for (def in konstue.insns) {
            if (def !in unspillableUnitValues) {
                unitUsageInformation.getOrPut(def) { mutableSetOf() } += use
            }
        }
    }

    fun run(internalClassName: String, methodNode: MethodNode): Array<Frame<BasicValue>?> {
        konst frames = FastMethodAnalyzer<BasicValue>(internalClassName, methodNode, this).analyze()
        // The ASM analyzer does not visit POP instructions, so we do so here.
        for ((insn, frame) in methodNode.instructions.asSequence().zip(frames.asSequence())) {
            if (frame != null && insn.opcode == Opcodes.POP) {
                konst konstue = frame.top()
                if (konstue is UnitValue) {
                    collectUnitUsage(insn, konstue)
                }
            }
        }
        return frames
    }

    override fun newOperation(insn: AbstractInsnNode?): BasicValue =
        if (insn?.isUnitInstance() == true) UnitValue(insn) else super.newOperation(insn)

    override fun copyOperation(insn: AbstractInsnNode, konstue: BasicValue?): BasicValue? {
        if (konstue is UnitValue) {
            if (insn is VarInsnNode && insn.opcode == Opcodes.ASTORE && insn.`var` !in localVariables) {
                collectUnitUsage(insn, konstue)
                // We track the stored konstue in case it is subsequently read.
                return konstue
            }
            unspillableUnitValues += konstue.insns
        }
        return super.copyOperation(insn, konstue)
    }

    override fun unaryOperation(insn: AbstractInsnNode, konstue: BasicValue?): BasicValue? {
        markUnspillable(konstue)
        return super.unaryOperation(insn, konstue)
    }

    override fun binaryOperation(insn: AbstractInsnNode, konstue1: BasicValue?, konstue2: BasicValue?): BasicValue? {
        markUnspillable(konstue1)
        markUnspillable(konstue2)
        return super.binaryOperation(insn, konstue1, konstue2)
    }

    override fun ternaryOperation(insn: AbstractInsnNode, konstue1: BasicValue?, konstue2: BasicValue?, konstue3: BasicValue?): BasicValue? {
        markUnspillable(konstue1)
        markUnspillable(konstue2)
        markUnspillable(konstue3)
        return super.ternaryOperation(insn, konstue1, konstue2, konstue3)
    }

    override fun naryOperation(insn: AbstractInsnNode, konstues: List<BasicValue>?): BasicValue? {
        konstues?.forEach(this::markUnspillable)
        return super.naryOperation(insn, konstues)
    }

    override fun merge(konstue1: BasicValue?, konstue2: BasicValue?): BasicValue? =
        if (konstue1 is UnitValue && konstue2 is UnitValue) {
            konst newValue = UnitValue(konstue1.insns.union(konstue2.insns))
            if (newValue.insns.any { it in unspillableUnitValues }) {
                markUnspillable(newValue)
            }
            newValue
        } else {
            // Mark unit konstues as unspillable if we merge them with non-unit konstues here.
            // This is conservative since the konstue could turn out to be unused.
            markUnspillable(konstue1)
            markUnspillable(konstue2)
            super.merge(konstue1, konstue2)
        }
}
