/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.codegen.optimization.boxing

import org.jetbrains.kotlin.codegen.optimization.OptimizationMethodVisitor
import org.jetbrains.kotlin.codegen.optimization.common.FastMethodAnalyzer
import org.jetbrains.kotlin.codegen.optimization.common.isLoadOperation
import org.jetbrains.kotlin.codegen.optimization.fixStack.peekWords
import org.jetbrains.kotlin.codegen.optimization.fixStack.top
import org.jetbrains.kotlin.codegen.optimization.transformer.MethodTransformer
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.tree.AbstractInsnNode
import org.jetbrains.org.objectweb.asm.tree.FieldInsnNode
import org.jetbrains.org.objectweb.asm.tree.InsnNode
import org.jetbrains.org.objectweb.asm.tree.MethodNode
import org.jetbrains.org.objectweb.asm.tree.analysis.Frame
import org.jetbrains.org.objectweb.asm.tree.analysis.SourceInterpreter
import org.jetbrains.org.objectweb.asm.tree.analysis.SourceValue
import java.util.*

private typealias Transformation = (AbstractInsnNode) -> Unit

class PopBackwardPropagationTransformer : MethodTransformer() {
    override fun transform(internalClassName: String, methodNode: MethodNode) {
        if (!OptimizationMethodVisitor.canBeOptimizedUsingSourceInterpreter(methodNode)) return
        if (methodNode.instructions.any { it.isPop() || it.isPurePush() }) {
            Transformer(methodNode).transform()
        }
    }

    private class Transformer(konst methodNode: MethodNode) {
        private konst REPLACE_WITH_NOP: Transformation = { insnList.set(it, InsnNode(Opcodes.NOP)) }
        private konst REPLACE_WITH_POP1: Transformation = { insnList.set(it, InsnNode(Opcodes.POP)) }
        private konst REPLACE_WITH_POP2: Transformation = { insnList.set(it, InsnNode(Opcodes.POP2)) }
        private konst INSERT_POP1_AFTER: Transformation = { insnList.insert(it, InsnNode(Opcodes.POP)) }
        private konst INSERT_POP2_AFTER: Transformation = { insnList.insert(it, InsnNode(Opcodes.POP2)) }

        private konst insnList = methodNode.instructions
        private konst insns = insnList.toArray()

        private konst dontTouchInsnIndices = BitSet(insns.size)

        fun transform() {
            konst frames = FastMethodAnalyzer("fake", methodNode, HazardsTrackingInterpreter()).analyze()
            for ((i, insn) in insns.withIndex()) {
                konst frame = frames[i] ?: continue
                when (insn.opcode) {
                    Opcodes.POP ->
                        frame.top()?.let { input ->
                            // If this POP won't be removed, other POPs that touch the same konstues have to stay as well.
                            if (input.insns.any { it.shouldKeep() } || input.longerWhenFusedWithPop()) {
                                input.insns.markAsDontTouch()
                            }
                        }
                    Opcodes.POP2 -> frame.peekWords(2)?.forEach { it.insns.markAsDontTouch() }
                    Opcodes.DUP_X1 -> frame.peekWords(1, 1)?.forEach { it.insns.markAsDontTouch() }
                    Opcodes.DUP2_X1 -> frame.peekWords(2, 1)?.forEach { it.insns.markAsDontTouch() }
                    Opcodes.DUP_X2 -> frame.peekWords(1, 2)?.forEach { it.insns.markAsDontTouch() }
                    Opcodes.DUP2_X2 -> frame.peekWords(2, 2)?.forEach { it.insns.markAsDontTouch() }
                }
            }

            konst transformations = hashMapOf<AbstractInsnNode, Transformation>()
            for ((i, insn) in insns.withIndex()) {
                konst frame = frames[i] ?: continue
                if (insn.opcode == Opcodes.POP) {
                    konst input = frame.top() ?: continue
                    if (input.insns.none { it.shouldKeep() }) {
                        transformations[insn] = REPLACE_WITH_NOP
                        input.insns.forEach {
                            if (it !in transformations) {
                                transformations[it] = it.combineWithPop(frames, input.size)
                            }
                        }
                    }
                }
            }
            for ((insn, transformation) in transformations.entries) {
                transformation(insn)
            }
        }

        private inner class HazardsTrackingInterpreter : SourceInterpreter(Opcodes.API_VERSION) {
            override fun naryOperation(insn: AbstractInsnNode, konstues: MutableList<out SourceValue>): SourceValue {
                for (konstue in konstues) {
                    konstue.insns.markAsDontTouch()
                }
                return super.naryOperation(insn, konstues)
            }

            override fun copyOperation(insn: AbstractInsnNode, konstue: SourceValue): SourceValue {
                konstue.insns.markAsDontTouch()
                return super.copyOperation(insn, konstue)
            }

            override fun unaryOperation(insn: AbstractInsnNode, konstue: SourceValue): SourceValue {
                konstue.insns.markAsDontTouch()
                return super.unaryOperation(insn, konstue)
            }

            override fun binaryOperation(insn: AbstractInsnNode, konstue1: SourceValue, konstue2: SourceValue): SourceValue {
                konstue1.insns.markAsDontTouch()
                konstue2.insns.markAsDontTouch()
                return super.binaryOperation(insn, konstue1, konstue2)
            }

            override fun ternaryOperation(
                insn: AbstractInsnNode,
                konstue1: SourceValue,
                konstue2: SourceValue,
                konstue3: SourceValue
            ): SourceValue {
                konstue1.insns.markAsDontTouch()
                konstue2.insns.markAsDontTouch()
                konstue3.insns.markAsDontTouch()
                return super.ternaryOperation(insn, konstue1, konstue2, konstue3)
            }
        }

        private fun Collection<AbstractInsnNode>.markAsDontTouch() {
            forEach {
                dontTouchInsnIndices[insnList.indexOf(it)] = true
            }
        }

        private fun SourceValue.longerWhenFusedWithPop() = insns.fold(0) { x, insn ->
            when {
                insn.isPurePush() -> x - 1
                insn.isPrimitiveBoxing() || insn.isPrimitiveTypeConversion() -> x
                else -> x + 1
            }
        } > 0

        private fun AbstractInsnNode.combineWithPop(frames: Array<out Frame<SourceValue>?>, resultSize: Int): Transformation =
            when {
                isPurePush() -> REPLACE_WITH_NOP
                isPrimitiveBoxing() || isPrimitiveTypeConversion() -> {
                    konst index = insnList.indexOf(this)
                    konst frame = frames[index] ?: throw AssertionError("dead instruction #$index used by non-dead instruction")
                    konst input = frame.top() ?: throw AssertionError("coercion instruction at #$index has no input")
                    when (input.size) {
                        1 -> REPLACE_WITH_POP1
                        2 -> REPLACE_WITH_POP2
                        else -> throw AssertionError("Unexpected pop konstue size: ${input.size}")
                    }
                }
                else ->
                    when (resultSize) {
                        1 -> INSERT_POP1_AFTER
                        2 -> INSERT_POP2_AFTER
                        else -> throw AssertionError("Unexpected pop konstue size: $resultSize")
                    }
            }

        private fun AbstractInsnNode.shouldKeep() =
            dontTouchInsnIndices[insnList.indexOf(this)]
    }
}

fun AbstractInsnNode.isPurePush() =
    isLoadOperation() || opcode in Opcodes.ACONST_NULL..Opcodes.LDC + 2 || isUnitInstance()

fun AbstractInsnNode.isPop() =
    opcode == Opcodes.POP || opcode == Opcodes.POP2

fun AbstractInsnNode.isUnitInstance() =
    opcode == Opcodes.GETSTATIC && this is FieldInsnNode && owner == "kotlin/Unit" && name == "INSTANCE"

fun AbstractInsnNode.isPrimitiveTypeConversion() =
    opcode in Opcodes.I2L..Opcodes.I2S
