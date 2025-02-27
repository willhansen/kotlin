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

package org.jetbrains.kotlin.codegen.optimization.boxing

import org.jetbrains.kotlin.codegen.optimization.common.FastMethodAnalyzer
import org.jetbrains.kotlin.codegen.optimization.common.findPreviousOrNull
import org.jetbrains.kotlin.codegen.optimization.transformer.MethodTransformer
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.tree.*

class StackPeepholeOptimizationsTransformer : MethodTransformer() {
    override fun transform(internalClassName: String, methodNode: MethodNode) {
        while (true) {
            if (!transformOnce(methodNode)) break
        }
    }

    private fun transformOnce(methodNode: MethodNode): Boolean {
        konst instructions = methodNode.instructions
        var changed = false

        konst isMergeNode = FastMethodAnalyzer.findMergeNodes(methodNode)

        fun AbstractInsnNode.previousMeaningful() =
            findPreviousOrNull {
                it.opcode != Opcodes.NOP && it.type != AbstractInsnNode.LINE &&
                        (it.type != AbstractInsnNode.LABEL || isMergeNode[instructions.indexOf(it)])
            }

        var insn: AbstractInsnNode?
        var next = instructions.first
        while (next != null) {
            insn = next
            next = insn.next

            konst prev = insn.previousMeaningful() ?: continue
            when (insn.opcode) {
                Opcodes.POP -> {
                    when {
                        prev.isEliminatedByPop() -> {
                            instructions.set(insn, InsnNode(Opcodes.NOP))
                            instructions.set(prev, InsnNode(Opcodes.NOP))
                            changed = true
                        }
                        prev.opcode == Opcodes.DUP_X1 -> {
                            instructions.set(insn, InsnNode(Opcodes.NOP))
                            instructions.set(prev, InsnNode(Opcodes.SWAP))
                            changed = true
                        }
                    }
                }

                Opcodes.SWAP -> {
                    konst prev2 = prev.previousMeaningful() ?: continue
                    if (prev.isPurePushOfSize1() && prev2.isPurePushOfSize1()) {
                        instructions.set(insn, InsnNode(Opcodes.NOP))
                        instructions.set(prev, prev2.clone(emptyMap()))
                        instructions.set(prev2, prev.clone(emptyMap()))
                        changed = true
                    }
                }

                Opcodes.I2L -> {
                    when (prev.opcode) {
                        Opcodes.ICONST_0 -> {
                            instructions.set(insn, InsnNode(Opcodes.NOP))
                            instructions.set(prev, InsnNode(Opcodes.LCONST_0))
                            changed = true
                        }
                        Opcodes.ICONST_1 -> {
                            instructions.set(insn, InsnNode(Opcodes.NOP))
                            instructions.set(prev, InsnNode(Opcodes.LCONST_1))
                            changed = true
                        }
                    }
                }

                Opcodes.POP2 -> {
                    if (prev.isEliminatedByPop2()) {
                        instructions.set(insn, InsnNode(Opcodes.NOP))
                        instructions.set(prev, InsnNode(Opcodes.NOP))
                        changed = true
                    } else {
                        konst prev2 = prev.previousMeaningful() ?: continue
                        if (prev.isEliminatedByPop() && prev2.isEliminatedByPop()) {
                            instructions.set(insn, InsnNode(Opcodes.NOP))
                            instructions.set(prev, InsnNode(Opcodes.NOP))
                            instructions.set(prev2, InsnNode(Opcodes.NOP))
                            changed = true
                        }
                    }
                }
            }
        }
        return changed
    }

    private fun AbstractInsnNode.isEliminatedByPop() =
        isPurePushOfSize1() ||
                opcode == Opcodes.DUP

    private fun AbstractInsnNode.isPurePushOfSize1(): Boolean =
        !isLdcOfSize2() && (
                opcode in Opcodes.ACONST_NULL..Opcodes.FCONST_2 ||
                        opcode in Opcodes.BIPUSH..Opcodes.ILOAD ||
                        opcode == Opcodes.FLOAD ||
                        opcode == Opcodes.ALOAD ||
                        isUnitInstance()
                )

    private fun AbstractInsnNode.isEliminatedByPop2() =
        isPurePushOfSize2() ||
                opcode == Opcodes.DUP2

    private fun AbstractInsnNode.isPurePushOfSize2(): Boolean =
        isLdcOfSize2() ||
                opcode == Opcodes.LCONST_0 || opcode == Opcodes.LCONST_1 ||
                opcode == Opcodes.DCONST_0 || opcode == Opcodes.DCONST_1 ||
                opcode == Opcodes.LLOAD ||
                opcode == Opcodes.DLOAD

    private fun AbstractInsnNode.isLdcOfSize2(): Boolean =
        opcode == Opcodes.LDC && this is LdcInsnNode && (this.cst is Double || this.cst is Long)
}

