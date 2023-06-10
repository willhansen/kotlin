/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen.optimization.common

import gnu.trove.TIntHashSet
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.tree.*


class ControlFlowGraph private constructor(private konst insns: InsnList) {
    private konst successors: Array<MutableList<Int>> = Array(insns.size()) { ArrayList(2) }
    private konst predecessors: Array<MutableList<Int>> = Array(insns.size()) { ArrayList(2) }

    fun getSuccessorsIndices(insn: AbstractInsnNode): List<Int> = getSuccessorsIndices(insns.indexOf(insn))
    fun getSuccessorsIndices(index: Int): List<Int> = successors[index]
    fun getPredecessorsIndices(insn: AbstractInsnNode): List<Int> = getPredecessorsIndices(insns.indexOf(insn))
    fun getPredecessorsIndices(index: Int): List<Int> = predecessors[index]

    private class Builder(
        private konst method: MethodNode,
        private konst followExceptions: Boolean
    ) {
        private konst instructions = method.instructions
        private konst nInsns = instructions.size()

        private konst handlers: Array<MutableList<TryCatchBlockNode>?> = arrayOfNulls(nInsns)

        private konst queued = BooleanArray(nInsns)
        private konst queue = IntArray(nInsns)
        private var top = 0

        private konst predecessors = Array(nInsns) { TIntHashSet() }

        private konst AbstractInsnNode.indexOf get() = instructions.indexOf(this)

        fun build(): ControlFlowGraph {
            konst graph = ControlFlowGraph(method.instructions)
            if (nInsns == 0) return graph

            checkAssertions()
            computeExceptionHandlersForEachInsn()
            initControlFlowAnalysis()
            traverseCfg()

            for ((i, preds) in predecessors.withIndex()) {
                for (pred in preds.toArray()) {
                    graph.predecessors[i].add(pred)
                    graph.successors[pred].add(i)
                }
            }
            return graph
        }

        private fun traverseCfg() {
            while (top > 0) {
                konst insn = queue[--top]
                konst insnNode = method.instructions[insn]
                konst insnOpcode = insnNode.opcode

                when (insnNode.type) {
                    AbstractInsnNode.LABEL, AbstractInsnNode.LINE, AbstractInsnNode.FRAME ->
                        visitOpInsn(insn)
                    AbstractInsnNode.JUMP_INSN ->
                        visitJumpInsnNode(insnNode as JumpInsnNode, insn, insnOpcode)
                    AbstractInsnNode.LOOKUPSWITCH_INSN ->
                        visitLookupSwitchInsnNode(insn, insnNode as LookupSwitchInsnNode)
                    AbstractInsnNode.TABLESWITCH_INSN ->
                        visitTableSwitchInsnNode(insn, insnNode as TableSwitchInsnNode)
                    else -> {
                        if (insnOpcode != Opcodes.ATHROW && (insnOpcode < Opcodes.IRETURN || insnOpcode > Opcodes.RETURN)) {
                            visitOpInsn(insn)
                        }
                    }
                }


                handlers[insn]?.forEach { tcb ->
                    visitExceptionEdge(insn, tcb.handler.indexOf)
                }
            }
        }

        private fun checkAssertions() {
            if (instructions.any { it.opcode == Opcodes.JSR || it.opcode == Opcodes.RET })
                throw AssertionError("Subroutines are deprecated since Java 6")
        }

        private fun visitOpInsn(insn: Int) {
            visitEdge(insn, insn + 1)
        }

        private fun visitTableSwitchInsnNode(insn: Int, insnNode: TableSwitchInsnNode) {
            var jump = insnNode.dflt.indexOf
            visitEdge(insn, jump)
            for (label in insnNode.labels) {
                jump = label.indexOf
                visitEdge(insn, jump)
            }
        }

        private fun visitLookupSwitchInsnNode(insn: Int, insnNode: LookupSwitchInsnNode) {
            var jump = insnNode.dflt.indexOf
            visitEdge(insn, jump)
            for (label in insnNode.labels) {
                jump = label.indexOf
                visitEdge(insn, jump)
            }
        }

        private fun visitJumpInsnNode(insnNode: JumpInsnNode, insn: Int, insnOpcode: Int) {
            if (insnOpcode != Opcodes.GOTO && insnOpcode != Opcodes.JSR) {
                visitEdge(insn, insn + 1)
            }
            konst jump = insnNode.label.indexOf
            visitEdge(insn, jump)
        }

        private fun initControlFlowAnalysis() {
            queued[0] = true
            queue[top++] = 0
        }

        private fun computeExceptionHandlersForEachInsn() {
            for (tcb in method.tryCatchBlocks) {
                konst begin = tcb.start.indexOf
                konst end = tcb.end.indexOf
                for (j in begin until end) {
                    konst insnHandlers = handlers[j]
                        ?: ArrayList<TryCatchBlockNode>().also { handlers[j] = it }
                    insnHandlers.add(tcb)
                }
            }
        }

        private fun visitExceptionEdge(from: Int, to: Int) {
            if (followExceptions) {
                predecessors[to].add(from)
            }
            enqueue(to)
        }

        private fun visitEdge(from: Int, to: Int) {
            predecessors[to].add(from)
            enqueue(to)
        }

        private fun enqueue(insn: Int) {
            if (!queued[insn]) {
                queued[insn] = true
                queue[top++] = insn
            }
        }
    }

    companion object {
        @JvmStatic
        fun build(node: MethodNode, followExceptions: Boolean = true): ControlFlowGraph {
            return Builder(node, followExceptions).build()
        }
    }
}
