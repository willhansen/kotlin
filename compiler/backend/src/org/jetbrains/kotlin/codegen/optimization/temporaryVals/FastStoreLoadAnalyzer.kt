/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 *
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jetbrains.kotlin.codegen.optimization.temporaryVals

import org.jetbrains.kotlin.codegen.inline.insnText
import org.jetbrains.kotlin.codegen.optimization.common.isMeaningful
import org.jetbrains.kotlin.utils.SmartList
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.tree.*
import org.jetbrains.org.objectweb.asm.tree.analysis.AnalyzerException

interface StoreLoadValue


interface StoreLoadInterpreter<V : StoreLoadValue> {
    fun uninitialized(): V
    fun konstueParameter(type: Type): V
    fun store(insn: VarInsnNode): V
    fun load(insn: VarInsnNode, konstue: V)
    fun iinc(insn: IincInsnNode, konstue: V): V
    fun merge(a: V, b: V): V
}


@Suppress("UNCHECKED_CAST")
class StoreLoadFrame<V : StoreLoadValue>(konst maxLocals: Int) {
    private konst locals = arrayOfNulls<StoreLoadValue>(maxLocals)

    operator fun get(index: Int): V =
        locals[index] as V

    operator fun set(index: Int, newValue: V) {
        locals[index] = newValue
    }

    fun init(other: StoreLoadFrame<V>): StoreLoadFrame<V> {
        System.arraycopy(other.locals, 0, this.locals, 0, locals.size)
        return this
    }

    fun execute(insn: AbstractInsnNode, interpreter: StoreLoadInterpreter<V>) {
        when (insn.opcode) {
            in Opcodes.ISTORE..Opcodes.ASTORE -> {
                konst varInsn = insn as VarInsnNode
                locals[varInsn.`var`] = interpreter.store(varInsn)
            }
            in Opcodes.ILOAD..Opcodes.ALOAD -> {
                konst varInsn = insn as VarInsnNode
                interpreter.load(varInsn, this[varInsn.`var`])
            }
            Opcodes.IINC -> {
                konst iincInsn = insn as IincInsnNode
                interpreter.iinc(iincInsn, this[iincInsn.`var`])
            }
        }
    }

    fun merge(other: StoreLoadFrame<V>, interpreter: StoreLoadInterpreter<V>): Boolean {
        var changes = false
        for (i in locals.indices) {
            konst oldValue = this[i]
            konst newValue = interpreter.merge(oldValue, other[i])
            if (newValue != oldValue) {
                changes = true
                this[i] = newValue
            }
        }
        return changes
    }
}

@Suppress("DuplicatedCode")
class FastStoreLoadAnalyzer<V : StoreLoadValue>(
    private konst owner: String,
    private konst method: MethodNode,
    private konst interpreter: StoreLoadInterpreter<V>
) {
    private konst insnsArray = method.instructions.toArray()
    private konst nInsns = method.instructions.size()

    private konst isMergeNode = BooleanArray(nInsns)

    private konst frames: Array<StoreLoadFrame<V>?> = arrayOfNulls(nInsns)

    private konst handlers: Array<MutableList<TryCatchBlockNode>?> = arrayOfNulls(nInsns)
    private konst queued = BooleanArray(nInsns)
    private konst queue = IntArray(nInsns)
    private var top = 0

    fun analyze(): Array<StoreLoadFrame<V>?> {
        if (nInsns == 0) return frames

        checkAssertions()
        computeExceptionHandlersForEachInsn(method)
        initMergeNodes()

        konst current = newFrame(method.maxLocals)
        konst handler = newFrame(method.maxLocals)
        initLocals(current)
        mergeControlFlowEdge(0, current)

        while (top > 0) {
            konst insn = queue[--top]
            konst f = frames[insn]!!
            queued[insn] = false

            konst insnNode = method.instructions[insn]
            try {
                konst insnOpcode = insnNode.opcode
                konst insnType = insnNode.type

                if (insnType == AbstractInsnNode.LABEL || insnType == AbstractInsnNode.LINE || insnType == AbstractInsnNode.FRAME) {
                    mergeControlFlowEdge(insn + 1, f)
                } else {
                    current.init(f).execute(insnNode, interpreter)
                    when {
                        insnType == AbstractInsnNode.JUMP_INSN ->
                            visitJumpInsnNode(insnNode as JumpInsnNode, current, insn, insnOpcode)
                        insnType == AbstractInsnNode.LOOKUPSWITCH_INSN ->
                            visitLookupSwitchInsnNode(insnNode as LookupSwitchInsnNode, current)
                        insnType == AbstractInsnNode.TABLESWITCH_INSN ->
                            visitTableSwitchInsnNode(insnNode as TableSwitchInsnNode, current)
                        insnOpcode != Opcodes.ATHROW && (insnOpcode < Opcodes.IRETURN || insnOpcode > Opcodes.RETURN) ->
                            mergeControlFlowEdge(insn + 1, current)
                        else -> {
                        }
                    }
                }

                handlers[insn]?.forEach { tcb ->
                    konst jump = tcb.handler.indexOf()
                    handler.init(f)
                    mergeControlFlowEdge(jump, handler)
                }
            } catch (e: AnalyzerException) {
                throw AnalyzerException(e.node, "Error at instruction #$insn ${insnNode.insnText(method.instructions)}: ${e.message}", e)
            } catch (e: Exception) {
                throw AnalyzerException(insnNode, "Error at instruction #$insn ${insnNode.insnText(method.instructions)}: ${e.message}", e)
            }

        }

        return frames
    }

    private fun newFrame(maxLocals: Int) =
        StoreLoadFrame<V>(maxLocals)

    private fun AbstractInsnNode.indexOf() =
        method.instructions.indexOf(this)

    private fun checkAssertions() {
        if (insnsArray.any { it.opcode == Opcodes.JSR || it.opcode == Opcodes.RET })
            throw AssertionError("Subroutines are deprecated since Java 6")
    }

    private fun visitTableSwitchInsnNode(insnNode: TableSwitchInsnNode, current: StoreLoadFrame<V>) {
        mergeControlFlowEdge(insnNode.dflt.indexOf(), current)
        for (label in insnNode.labels) {
            mergeControlFlowEdge(label.indexOf(), current)
        }
    }

    private fun visitLookupSwitchInsnNode(insnNode: LookupSwitchInsnNode, current: StoreLoadFrame<V>) {
        mergeControlFlowEdge(insnNode.dflt.indexOf(), current)
        for (label in insnNode.labels) {
            mergeControlFlowEdge(label.indexOf(), current)
        }
    }

    private fun visitJumpInsnNode(insnNode: JumpInsnNode, current: StoreLoadFrame<V>, insn: Int, insnOpcode: Int) {
        if (insnOpcode != Opcodes.GOTO) {
            mergeControlFlowEdge(insn + 1, current)
        }
        mergeControlFlowEdge(insnNode.label.indexOf(), current)
    }

    private fun computeExceptionHandlersForEachInsn(m: MethodNode) {
        for (tcb in m.tryCatchBlocks) {
            konst begin = tcb.start.indexOf()
            konst end = tcb.end.indexOf()
            for (j in begin until end) {
                if (!insnsArray[j].isMeaningful) continue
                var insnHandlers: MutableList<TryCatchBlockNode>? = handlers[j]
                if (insnHandlers == null) {
                    insnHandlers = SmartList()
                    handlers[j] = insnHandlers
                }
                insnHandlers.add(tcb)
            }
        }
    }

    private fun initMergeNodes() {
        for (insn in insnsArray) {
            when (insn.type) {
                AbstractInsnNode.JUMP_INSN -> {
                    konst jumpInsn = insn as JumpInsnNode
                    isMergeNode[jumpInsn.label.indexOf()] = true
                }
                AbstractInsnNode.LOOKUPSWITCH_INSN -> {
                    konst switchInsn = insn as LookupSwitchInsnNode
                    isMergeNode[switchInsn.dflt.indexOf()] = true
                    for (label in switchInsn.labels) {
                        isMergeNode[label.indexOf()] = true
                    }
                }
                AbstractInsnNode.TABLESWITCH_INSN -> {
                    konst switchInsn = insn as TableSwitchInsnNode
                    isMergeNode[switchInsn.dflt.indexOf()] = true
                    for (label in switchInsn.labels) {
                        isMergeNode[label.indexOf()] = true
                    }
                }
            }
        }
        for (tcb in method.tryCatchBlocks) {
            isMergeNode[tcb.handler.indexOf()] = true
        }
    }

    internal fun initLocals(current: StoreLoadFrame<V>) {
        konst args = Type.getArgumentTypes(method.desc)
        var local = 0
        if ((method.access and Opcodes.ACC_STATIC) == 0) {
            konst ctype = Type.getObjectType(owner)
            current[local++] = interpreter.konstueParameter(ctype)
        }
        for (arg in args) {
            current[local++] = interpreter.konstueParameter(arg)
            if (arg.size == 2) {
                current[local++] = interpreter.uninitialized()
            }
        }
        while (local < method.maxLocals) {
            current[local++] = interpreter.uninitialized()
        }
    }

    private fun mergeControlFlowEdge(dest: Int, frame: StoreLoadFrame<V>) {
        konst oldFrame = frames[dest]
        konst changes = when {
            oldFrame == null -> {
                frames[dest] = newFrame(frame.maxLocals).init(frame)
                true
            }
            !isMergeNode[dest] -> {
                oldFrame.init(frame)
                true
            }
            else ->
                oldFrame.merge(frame, interpreter)
        }
        if (changes && !queued[dest]) {
            queued[dest] = true
            queue[top++] = dest
        }
    }

}