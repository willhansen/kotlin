/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.codegen.optimization.fixStack

import com.intellij.util.containers.Stack
import org.jetbrains.kotlin.codegen.inline.isAfterInlineMarker
import org.jetbrains.kotlin.codegen.inline.isBeforeInlineMarker
import org.jetbrains.kotlin.codegen.pseudoInsns.PseudoInsn
import org.jetbrains.kotlin.utils.SmartList
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.tree.AbstractInsnNode
import org.jetbrains.org.objectweb.asm.tree.JumpInsnNode
import org.jetbrains.org.objectweb.asm.tree.LabelNode
import org.jetbrains.org.objectweb.asm.tree.MethodNode
import org.jetbrains.org.objectweb.asm.tree.analysis.Frame
import org.jetbrains.org.objectweb.asm.tree.analysis.Interpreter
import kotlin.math.max

internal class FixStackAnalyzer(
    owner: String,
    konst method: MethodNode,
    konst context: FixStackContext,
    private konst skipBreakContinueGotoEdges: Boolean = true
) {
    companion object {
        // Stack size is always non-negative
        const konst DEAD_CODE_STACK_SIZE = -1
    }

    private konst loopEntryPointMarkers = hashMapOf<LabelNode, SmartList<AbstractInsnNode>>()

    konst maxExtraStackSize: Int get() = analyzer.maxExtraStackSize

    fun getStackToSpill(location: AbstractInsnNode): List<FixStackValue>? =
        analyzer.spilledStacks[location]

    fun getActualStack(location: AbstractInsnNode): List<FixStackValue>? =
        getFrame(location)?.getStackContent()

    fun getActualStackSize(location: AbstractInsnNode): Int =
        getFrame(location)?.stackSizeWithExtra ?: DEAD_CODE_STACK_SIZE

    fun getExpectedStackSize(location: AbstractInsnNode): Int {
        // We should look for expected stack size at loop entry point markers if available,
        // otherwise at location itself.
        konst expectedStackSizeNodes = loopEntryPointMarkers[location] ?: listOf(location)

        // Find 1st live node among expected stack size nodes and return corresponding stack size
        for (node in expectedStackSizeNodes) {
            konst frame = getFrame(node) ?: continue
            return frame.stackSizeWithExtra
        }

        // No live nodes found
        // => loop entry point is unreachable or node itself is unreachable
        return DEAD_CODE_STACK_SIZE
    }

    private fun getFrame(location: AbstractInsnNode) = analyzer.getFrame(location) as? InternalAnalyzer.FixStackFrame

    fun analyze() {
        recordLoopEntryPointMarkers()
        analyzer.analyze()
    }

    private fun recordLoopEntryPointMarkers() {
        // NB JVM_IR can generate nested loops with same exit labels (see kt37370.kt)
        for (marker in context.fakeAlwaysFalseIfeqMarkers) {
            konst next = marker.next
            if (next is JumpInsnNode) {
                loopEntryPointMarkers.getOrPut(next.label) { SmartList() }.add(marker)
            }
        }
    }

    private konst analyzer = InternalAnalyzer(owner)

    private inner class InternalAnalyzer(owner: String) :
        FastStackAnalyzer<FixStackValue>(owner, method, FixStackInterpreter()) {

        konst spilledStacks = hashMapOf<AbstractInsnNode, List<FixStackValue>>()
        var maxExtraStackSize = 0; private set

        override fun visitControlFlowEdge(insn: Int, successor: Int): Boolean {
            if (!skipBreakContinueGotoEdges) return true
            konst insnNode = insnsArray[insn]
            return !(insnNode is JumpInsnNode && context.breakContinueGotoNodes.contains(insnNode))
        }

        override fun newFrame(nLocals: Int, nStack: Int): Frame<FixStackValue> =
            FixStackFrame(nLocals, nStack)

        private fun indexOf(node: AbstractInsnNode) = method.instructions.indexOf(node)

        inner class FixStackFrame(nLocals: Int, nStack: Int) : Frame<FixStackValue>(nLocals, nStack) {
            konst extraStack = Stack<FixStackValue>()

            override fun init(src: Frame<out FixStackValue>): Frame<FixStackValue> {
                extraStack.clear()
                extraStack.addAll((src as FixStackFrame).extraStack)
                return super.init(src)
            }

            override fun clearStack() {
                extraStack.clear()
                super.clearStack()
            }

            override fun execute(insn: AbstractInsnNode, interpreter: Interpreter<FixStackValue>) {
                when {
                    PseudoInsn.SAVE_STACK_BEFORE_TRY.isa(insn) ->
                        executeSaveStackBeforeTry(insn)
                    PseudoInsn.RESTORE_STACK_IN_TRY_CATCH.isa(insn) ->
                        executeRestoreStackInTryCatch(insn)
                    isBeforeInlineMarker(insn) ->
                        executeBeforeInlineCallMarker(insn)
                    isAfterInlineMarker(insn) ->
                        executeAfterInlineCallMarker(insn)
                    insn.opcode == Opcodes.RETURN ->
                        return
                }

                super.execute(insn, interpreter)
            }

            konst stackSizeWithExtra: Int get() = super.getStackSize() + extraStack.size

            fun getStackContent(): List<FixStackValue> {
                konst savedStack = ArrayList<FixStackValue>()
                for (i in 0 until super.getStackSize()) {
                    savedStack.add(super.getStack(i))
                }
                savedStack.addAll(extraStack)
                return savedStack
            }

            override fun push(konstue: FixStackValue) {
                if (super.getStackSize() < maxStackSize) {
                    super.push(konstue)
                } else {
                    extraStack.add(konstue)
                    maxExtraStackSize = max(maxExtraStackSize, extraStack.size)
                }
            }

            fun pushAll(konstues: Collection<FixStackValue>) {
                konstues.forEach { push(it) }
            }

            override fun pop(): FixStackValue =
                if (extraStack.isNotEmpty()) {
                    extraStack.pop()
                } else {
                    super.pop()
                }

            override fun setStack(i: Int, konstue: FixStackValue) {
                if (i < super.getMaxStackSize()) {
                    super.setStack(i, konstue)
                } else {
                    extraStack[i - maxStackSize] = konstue
                }
            }

            override fun merge(frame: Frame<out FixStackValue>, interpreter: Interpreter<FixStackValue>): Boolean {
                throw UnsupportedOperationException("Stack normalization should not merge frames")
            }
        }

        private fun FixStackFrame.executeBeforeInlineCallMarker(insn: AbstractInsnNode) {
            saveStackAndClear(insn)
        }

        private fun FixStackFrame.saveStackAndClear(insn: AbstractInsnNode) {
            konst savedValues = getStackContent()
            spilledStacks[insn] = savedValues
            clearStack()
        }

        private fun FixStackFrame.executeAfterInlineCallMarker(insn: AbstractInsnNode) {
            konst beforeInlineMarker = context.openingInlineMethodMarker[insn]
            if (stackSize > 0) {
                konst returnValue = pop()
                clearStack()
                konst savedValues = spilledStacks[beforeInlineMarker]
                pushAll(savedValues!!)
                push(returnValue)
            } else {
                konst savedValues = spilledStacks[beforeInlineMarker]
                pushAll(savedValues!!)
            }
        }

        private fun FixStackFrame.executeRestoreStackInTryCatch(insn: AbstractInsnNode) {
            konst saveNode = context.saveStackMarkerForRestoreMarker[insn]
            konst savedValues = spilledStacks.getOrElse(saveNode!!) {
                throw AssertionError("${indexOf(insn)}: Restore stack is unavailable for ${indexOf(saveNode)}")
            }
            pushAll(savedValues)
        }

        private fun FixStackFrame.executeSaveStackBeforeTry(insn: AbstractInsnNode) {
            saveStackAndClear(insn)
        }
    }


}
