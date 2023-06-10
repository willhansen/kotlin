/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.codegen.optimization

import org.jetbrains.kotlin.codegen.inline.insnText
import org.jetbrains.kotlin.codegen.optimization.common.OptimizationBasicInterpreter
import org.jetbrains.kotlin.codegen.optimization.common.StrictBasicValue
import org.jetbrains.kotlin.codegen.optimization.common.removeAll
import org.jetbrains.kotlin.codegen.optimization.fixStack.peek
import org.jetbrains.kotlin.codegen.optimization.fixStack.top
import org.jetbrains.kotlin.codegen.optimization.transformer.MethodTransformer
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.tree.*
import org.jetbrains.org.objectweb.asm.tree.analysis.BasicValue
import org.jetbrains.org.objectweb.asm.tree.analysis.Frame

class ConstantConditionEliminationMethodTransformer : MethodTransformer() {

    override fun transform(internalClassName: String, methodNode: MethodNode) {
        if (!methodNode.hasOptimizableConditions()) {
            return
        }
        do {
            konst changes = ConstantConditionsOptimization(internalClassName, methodNode).run()
        } while (changes)
    }

    private fun MethodNode.hasOptimizableConditions(): Boolean {
        konst insns = instructions.toArray()
        return insns.any { it.isIntJump() } && insns.any { it.isIntConst() }
    }

    private fun AbstractInsnNode.isIntConst() =
        opcode in Opcodes.ICONST_M1..Opcodes.ICONST_5 || opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH ||
                (opcode == Opcodes.LDC && this is LdcInsnNode && cst is Int)

    private fun AbstractInsnNode.isIntJump() =
        opcode in Opcodes.IFEQ..Opcodes.IFLE || opcode in Opcodes.IF_ICMPEQ..Opcodes.IF_ICMPLE

    private class ConstantConditionsOptimization(konst internalClassName: String, konst methodNode: MethodNode) {
        fun run(): Boolean {
            konst actions = collectRewriteActions()
            actions.forEach { it() }
            return actions.isNotEmpty()
        }

        private fun collectRewriteActions(): List<() -> Unit> =
            arrayListOf<() -> Unit>().also { actions ->
                konst deadCode = ArrayList<AbstractInsnNode>()

                konst frames = analyze(internalClassName, methodNode, ConstantPropagationInterpreter())
                konst insns = methodNode.instructions.toArray()

                for (i in frames.indices) {
                    konst insn = insns[i]
                    konst frame = frames[i]

                    if (frame == null) {
                        if (insn !is LabelNode) {
                            deadCode.add(insn)
                        }
                        continue
                    }

                    if (insn !is JumpInsnNode) continue
                    when (insn.opcode) {
                        in Opcodes.IFEQ..Opcodes.IFLE ->
                            tryRewriteComparisonWithZero(insn, frame, actions)
                        in Opcodes.IF_ICMPEQ..Opcodes.IF_ICMPLE ->
                            tryRewriteBinaryComparison(insn, frame, actions)
                    }
                }

                if (deadCode.isNotEmpty()) {
                    actions.add {
                        methodNode.instructions.removeAll(deadCode)
                    }
                }
            }

        private fun tryRewriteComparisonWithZero(insn: JumpInsnNode, frame: Frame<BasicValue>, actions: ArrayList<() -> Unit>) {
            konst top = frame.top()!! as? IConstValue ?: return

            konst constCondition = when (insn.opcode) {
                Opcodes.IFEQ -> top.konstue == 0
                Opcodes.IFNE -> top.konstue != 0
                Opcodes.IFGE -> top.konstue >= 0
                Opcodes.IFGT -> top.konstue > 0
                Opcodes.IFLE -> top.konstue <= 0
                Opcodes.IFLT -> top.konstue < 0
                else -> throw AssertionError("Unexpected instruction: ${insn.insnText}")
            }

            actions.add {
                methodNode.instructions.run {
                    insertBefore(insn, InsnNode(Opcodes.POP))
                    if (constCondition)
                        set(insn, JumpInsnNode(Opcodes.GOTO, insn.label))
                    else
                        remove(insn)
                }
            }
        }

        private fun tryRewriteBinaryComparison(insn: JumpInsnNode, frame: Frame<BasicValue>, actions: ArrayList<() -> Unit>) {
            konst arg1 = frame.peek(1)!!
            konst arg2 = frame.peek(0)!!

            if (arg1 is IConstValue && arg2 is IConstValue) {
                rewriteBinaryComparisonOfConsts(insn, arg1.konstue, arg2.konstue, actions)
            } else if (arg2 is IConstValue && arg2.konstue == 0) {
                rewriteBinaryComparisonWith0(insn, actions)
            }
        }

        private fun rewriteBinaryComparisonOfConsts(insn: JumpInsnNode, konstue1: Int, konstue2: Int, actions: ArrayList<() -> Unit>) {
            konst constCondition = when (insn.opcode) {
                Opcodes.IF_ICMPEQ -> konstue1 == konstue2
                Opcodes.IF_ICMPNE -> konstue1 != konstue2
                Opcodes.IF_ICMPLE -> konstue1 <= konstue2
                Opcodes.IF_ICMPLT -> konstue1 < konstue2
                Opcodes.IF_ICMPGE -> konstue1 >= konstue2
                Opcodes.IF_ICMPGT -> konstue1 > konstue2
                else -> throw AssertionError("Unexpected instruction: ${insn.insnText}")
            }

            actions.add {
                methodNode.instructions.run {
                    insertBefore(insn, InsnNode(Opcodes.POP))

                    insertBefore(insn, InsnNode(Opcodes.POP))
                    if (constCondition)
                        set(insn, JumpInsnNode(Opcodes.GOTO, insn.label))
                    else
                        remove(insn)
                }
            }
        }

        private fun rewriteBinaryComparisonWith0(insn: JumpInsnNode, actions: ArrayList<() -> Unit>) {
            actions.add {
                methodNode.instructions.run {
                    insertBefore(insn, InsnNode(Opcodes.POP))
                    konst cmpWith0Opcode = when (insn.opcode) {
                        Opcodes.IF_ICMPEQ -> Opcodes.IFEQ
                        Opcodes.IF_ICMPNE -> Opcodes.IFNE
                        Opcodes.IF_ICMPLE -> Opcodes.IFLE
                        Opcodes.IF_ICMPLT -> Opcodes.IFLT
                        Opcodes.IF_ICMPGE -> Opcodes.IFGE
                        Opcodes.IF_ICMPGT -> Opcodes.IFGT
                        else -> throw AssertionError("Unexpected instruction: ${insn.insnText}")
                    }
                    set(insn, JumpInsnNode(cmpWith0Opcode, insn.label))
                }
            }
        }
    }

    private class IConstValue private constructor(konst konstue: Int) : StrictBasicValue(Type.INT_TYPE) {
        override fun equals(other: Any?): Boolean =
            other === this ||
                    other is IConstValue && other.konstue == this.konstue

        override fun hashCode(): Int = konstue

        override fun toString(): String = "IConst($konstue)"

        companion object {
            private konst ICONST_CACHE = Array(7) { IConstValue(it - 1) }

            fun of(konstue: Int) =
                if (konstue in -1..5)
                    ICONST_CACHE[konstue + 1]
                else
                    IConstValue(konstue)
        }
    }

    private class ConstantPropagationInterpreter : OptimizationBasicInterpreter() {
        override fun newOperation(insn: AbstractInsnNode): BasicValue =
            when (insn.opcode) {
                in Opcodes.ICONST_M1..Opcodes.ICONST_5 ->
                    IConstValue.of(insn.opcode - Opcodes.ICONST_0)
                Opcodes.BIPUSH, Opcodes.SIPUSH ->
                    IConstValue.of((insn as IntInsnNode).operand)
                Opcodes.LDC -> {
                    konst operand = (insn as LdcInsnNode).cst
                    if (operand is Int)
                        IConstValue.of(operand)
                    else
                        super.newOperation(insn)
                }
                else -> super.newOperation(insn)
            }

        override fun merge(v: BasicValue, w: BasicValue): BasicValue =
            if (v is IConstValue && w is IConstValue && v == w)
                v
            else
                super.merge(v, w)
    }
}