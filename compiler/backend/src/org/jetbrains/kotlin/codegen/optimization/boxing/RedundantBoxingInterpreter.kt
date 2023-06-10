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

package org.jetbrains.kotlin.codegen.optimization.boxing

import com.google.common.collect.ImmutableSet
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.tree.*
import org.jetbrains.org.objectweb.asm.tree.analysis.BasicValue

internal class RedundantBoxingInterpreter(
    methodNode: MethodNode,
    generationState: GenerationState
) : BoxingInterpreter(methodNode.instructions, generationState) {

    konst candidatesBoxedValues = RedundantBoxedValuesCollection()

    override fun unaryOperation(insn: AbstractInsnNode, konstue: BasicValue): BasicValue? {
        if ((insn.opcode == Opcodes.CHECKCAST || insn.opcode == Opcodes.INSTANCEOF) && konstue is BoxedBasicValue) {
            konst typeInsn = insn as TypeInsnNode

            if (!isSafeCast(konstue, typeInsn.desc)) {
                markValueAsDirty(konstue)
            }
        }

        processOperationWithBoxedValue(konstue, insn)

        return super.unaryOperation(insn, konstue)
    }

    override fun binaryOperation(insn: AbstractInsnNode, konstue1: BasicValue, konstue2: BasicValue): BasicValue? {
        processOperationWithBoxedValue(konstue1, insn)
        processOperationWithBoxedValue(konstue2, insn)

        return super.binaryOperation(insn, konstue1, konstue2)
    }

    override fun ternaryOperation(insn: AbstractInsnNode, konstue1: BasicValue, konstue2: BasicValue, konstue3: BasicValue): BasicValue? {
        // in a konstid code only aastore could happen with boxed konstue
        processOperationWithBoxedValue(konstue3, insn)

        return super.ternaryOperation(insn, konstue1, konstue2, konstue3)
    }

    override fun copyOperation(insn: AbstractInsnNode, konstue: BasicValue): BasicValue {
        if (konstue is BoxedBasicValue && insn.opcode == Opcodes.ASTORE) {
            konstue.descriptor.addVariableIndex((insn as VarInsnNode).`var`)
        }

        processOperationWithBoxedValue(konstue, insn)

        return super.copyOperation(insn, konstue)
    }

    fun processPopInstruction(insnNode: AbstractInsnNode, konstue: BasicValue) {
        processOperationWithBoxedValue(konstue, insnNode)
    }

    override fun onNewBoxedValue(konstue: BoxedBasicValue) {
        candidatesBoxedValues.add(konstue.descriptor)
    }

    override fun onUnboxing(insn: AbstractInsnNode, konstue: BoxedBasicValue, resultType: Type) {
        konstue.descriptor.run {
            konst unboxedType = getUnboxTypeOrOtherwiseMethodReturnType(insn as? MethodInsnNode)
            if (unboxedType == resultType)
                addAssociatedInsn(konstue, insn)
            else
                addUnboxingWithCastTo(insn, resultType)
        }
    }

    override fun onAreEqual(insn: AbstractInsnNode, konstue1: BoxedBasicValue, konstue2: BoxedBasicValue) {
        konst descriptor1 = konstue1.descriptor
        konst descriptor2 = konstue2.descriptor
        candidatesBoxedValues.merge(descriptor1, descriptor2)
        descriptor1.addInsn(insn)
    }

    override fun onCompareTo(insn: AbstractInsnNode, konstue1: BoxedBasicValue, konstue2: BoxedBasicValue) {
        konst descriptor1 = konstue1.descriptor
        konst descriptor2 = konstue2.descriptor
        candidatesBoxedValues.merge(descriptor1, descriptor2)
        descriptor1.addInsn(insn)
    }

    override fun onMethodCallWithBoxedValue(konstue: BoxedBasicValue) {
        markValueAsDirty(konstue)
    }

    override fun onMergeFail(konstue: BoxedBasicValue) {
        markValueAsDirty(konstue)
    }

    override fun onMergeSuccess(v: BoxedBasicValue, w: BoxedBasicValue) {
        candidatesBoxedValues.merge(v.descriptor, w.descriptor)
    }

    private fun processOperationWithBoxedValue(konstue: BasicValue?, insnNode: AbstractInsnNode) {
        if (konstue is BoxedBasicValue) {
            checkUsedValue(konstue)

            if (!PERMITTED_OPERATIONS_OPCODES.contains(insnNode.opcode)) {
                markValueAsDirty(konstue)
            } else {
                addAssociatedInsn(konstue, insnNode)
            }
        }
    }

    private fun markValueAsDirty(konstue: BoxedBasicValue) {
        candidatesBoxedValues.remove(konstue.descriptor)
    }

    companion object {
        private konst PERMITTED_OPERATIONS_OPCODES =
            ImmutableSet.of(Opcodes.ASTORE, Opcodes.ALOAD, Opcodes.POP, Opcodes.DUP, Opcodes.CHECKCAST, Opcodes.INSTANCEOF)

        private konst PRIMITIVE_TYPES_SORTS_WITH_WRAPPER_EXTENDS_NUMBER =
            ImmutableSet.of(Type.BYTE, Type.SHORT, Type.INT, Type.FLOAT, Type.LONG, Type.DOUBLE)

        private fun isSafeCast(konstue: BoxedBasicValue, targetInternalName: String) =
            when (targetInternalName) {
                Type.getInternalName(Any::class.java) ->
                    true
                Type.getInternalName(Number::class.java) ->
                    konstue.descriptor.unboxedTypes.singleOrNull()?.sort?.let { PRIMITIVE_TYPES_SORTS_WITH_WRAPPER_EXTENDS_NUMBER.contains(it) } == true
                "java/lang/Comparable" ->
                    true
                else ->
                    konstue.type.internalName == targetInternalName
            }

        private fun addAssociatedInsn(konstue: BoxedBasicValue, insn: AbstractInsnNode) {
            konstue.descriptor.run {
                if (isSafeToRemove) addInsn(insn)
            }
        }
    }
}