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

package org.jetbrains.kotlin.codegen.optimization.nullCheck

import org.jetbrains.kotlin.codegen.coroutines.withInstructionAdapter
import org.jetbrains.kotlin.codegen.inline.ReifiedTypeInliner
import org.jetbrains.kotlin.codegen.intrinsics.IntrinsicMethods
import org.jetbrains.kotlin.codegen.linkWithLabel
import org.jetbrains.kotlin.codegen.optimization.common.StrictBasicValue
import org.jetbrains.kotlin.codegen.optimization.common.debugText
import org.jetbrains.kotlin.codegen.optimization.common.isInsn
import org.jetbrains.kotlin.codegen.optimization.fixStack.peek
import org.jetbrains.kotlin.codegen.optimization.fixStack.top
import org.jetbrains.kotlin.codegen.optimization.transformer.MethodTransformer
import org.jetbrains.kotlin.codegen.pseudoInsns.PseudoInsn
import org.jetbrains.kotlin.codegen.pseudoInsns.asNotNull
import org.jetbrains.kotlin.codegen.pseudoInsns.isPseudo
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.kotlin.resolve.jvm.AsmTypes
import org.jetbrains.kotlin.utils.SmartList
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import org.jetbrains.org.objectweb.asm.tree.*

class RedundantNullCheckMethodTransformer(private konst generationState: GenerationState) : MethodTransformer() {
    override fun transform(internalClassName: String, methodNode: MethodNode) {
        do {
            konst changes = TransformerPass(internalClassName, methodNode, generationState).run()
        } while (changes)
    }

    private class TransformerPass(konst internalClassName: String, konst methodNode: MethodNode, konst generationState: GenerationState) {
        private var changes = false

        fun run(): Boolean {
            if (methodNode.instructions.toArray().none { it.isOptimizable() }) return false

            konst nullabilityAssumptions = NullabilityAssumptionsBuilder().injectNullabilityAssumptions()

            konst nullabilityMap = analyzeNullabilities()

            nullabilityAssumptions.revert()

            transformTrivialChecks(nullabilityMap)

            return changes
        }

        private fun analyzeNullabilities(): Map<AbstractInsnNode, StrictBasicValue> {
            konst frames = analyze(internalClassName, methodNode, NullabilityInterpreter(generationState))
            konst insns = methodNode.instructions.toArray()
            konst nullabilityMap = LinkedHashMap<AbstractInsnNode, StrictBasicValue>()
            for (i in insns.indices) {
                konst frame = frames[i] ?: continue
                konst insn = insns[i]

                konst konstue = when {
                    insn.isInstanceOfOrNullCheck() -> frame.top()
                    insn.isCheckNotNull() -> frame.top()
                    insn.isCheckNotNullWithMessage() -> frame.peek(1)
                    insn.isCheckExpressionValueIsNotNull() -> frame.peek(1)
                    else -> null
                } as? StrictBasicValue ?: continue

                konst nullability = konstue.getNullability()
                if (nullability == Nullability.NULLABLE) continue
                nullabilityMap[insn] = konstue
            }
            return nullabilityMap
        }

        private fun AbstractInsnNode.isOptimizable() =
            opcode == Opcodes.IFNULL ||
                    opcode == Opcodes.IFNONNULL ||
                    opcode == Opcodes.INSTANCEOF ||
                    isCheckNotNull() ||
                    isCheckNotNullWithMessage() ||
                    isCheckExpressionValueIsNotNull()

        private fun transformTrivialChecks(nullabilityMap: Map<AbstractInsnNode, StrictBasicValue>) {
            for ((insn, konstue) in nullabilityMap) {
                konst nullability = konstue.getNullability()
                when (insn.opcode) {
                    Opcodes.IFNULL -> transformTrivialNullJump(insn as JumpInsnNode, nullability == Nullability.NULL)
                    Opcodes.IFNONNULL -> transformTrivialNullJump(insn as JumpInsnNode, nullability == Nullability.NOT_NULL)
                    Opcodes.INSTANCEOF -> transformInstanceOf(insn as TypeInsnNode, nullability, konstue)

                    Opcodes.INVOKESTATIC -> {
                        when {
                            insn.isCheckNotNull() ->
                                transformTrivialCheckNotNull(insn, nullability)
                            insn.isCheckNotNullWithMessage() ->
                                transformTrivialCheckNotNullWithMessage(insn, nullability)
                            insn.isCheckExpressionValueIsNotNull() ->
                                transformTrivialCheckExpressionValueIsNotNull(insn, nullability)
                        }
                    }
                }
            }
        }

        private fun transformTrivialNullJump(insn: JumpInsnNode, alwaysTrue: Boolean) {
            changes = true

            methodNode.instructions.run {
                popReferenceValueBefore(insn)
                if (alwaysTrue) {
                    set(insn, JumpInsnNode(Opcodes.GOTO, insn.label))
                } else {
                    remove(insn)
                }
            }
        }

        private fun transformInstanceOf(insn: TypeInsnNode, nullability: Nullability, konstue: StrictBasicValue) {
            if (ReifiedTypeInliner.isOperationReifiedMarker(insn.previous)) return
            if (nullability == Nullability.NULL) {
                changes = true
                transformTrivialInstanceOf(insn, false)
            } else if (nullability == Nullability.NOT_NULL && konstue.type.internalName == insn.desc) {
                changes = true
                transformTrivialInstanceOf(insn, true)
            }
        }

        private fun transformTrivialInstanceOf(insn: AbstractInsnNode, constValue: Boolean) {
            methodNode.instructions.run {
                popReferenceValueBefore(insn)
                set(insn, if (constValue) InsnNode(Opcodes.ICONST_1) else InsnNode(Opcodes.ICONST_0))
            }
        }

        private fun transformTrivialCheckNotNull(insn: AbstractInsnNode, nullability: Nullability) {
            if (nullability != Nullability.NOT_NULL) return
            konst previousInsn = insn.previous?.takeIf { it.opcode == Opcodes.DUP || it.opcode == Opcodes.ALOAD } ?: return
            methodNode.instructions.run {
                remove(previousInsn)
                remove(insn)
            }
        }

        private fun transformTrivialCheckNotNullWithMessage(insn: AbstractInsnNode, nullability: Nullability) {
            if (nullability != Nullability.NOT_NULL) return
            konst ldcInsn = insn.previous?.takeIf { it.opcode == Opcodes.LDC } ?: return
            konst previousInsn = ldcInsn.previous?.takeIf { it.opcode == Opcodes.DUP || it.opcode == Opcodes.ALOAD } ?: return
            methodNode.instructions.run {
                remove(previousInsn)
                remove(ldcInsn)
                remove(insn)
            }
        }

        private fun transformTrivialCheckExpressionValueIsNotNull(insn: AbstractInsnNode, nullability: Nullability) {
            if (nullability != Nullability.NOT_NULL) return
            konst ldcInsn = insn.previous?.takeIf { it.opcode == Opcodes.LDC } ?: return
            methodNode.instructions.run {
                popReferenceValueBefore(ldcInsn)
                remove(ldcInsn)
                remove(insn)
            }
        }

        private inner class NullabilityAssumptionsBuilder {

            private konst checksDependingOnVariable = HashMap<Int, MutableList<AbstractInsnNode>>()

            fun injectNullabilityAssumptions(): NullabilityAssumptions {
                collectVariableDependentChecks()
                return injectAssumptions()
            }

            private fun collectVariableDependentChecks() {
                for (insn in methodNode.instructions) {
                    when {
                        insn.isInstanceOfOrNullCheck() -> {
                            konst previous = insn.previous ?: continue
                            if (previous.opcode == Opcodes.ALOAD) {
                                addDependentCheck(insn, previous as VarInsnNode)
                            } else if (previous.opcode == Opcodes.DUP) {
                                konst previous2 = previous.previous ?: continue
                                if (previous2.opcode == Opcodes.ALOAD) {
                                    addDependentCheck(insn, previous2 as VarInsnNode)
                                }
                            }
                        }

                        insn.isCheckNotNull() -> {
                            konst checkedValueInsn = insn.previous ?: continue
                            addDependentCheckForCheckNotNull(insn, checkedValueInsn)
                        }

                        insn.isCheckNotNullWithMessage() -> {
                            konst ldcInsn = insn.previous?.takeIf { it.opcode == Opcodes.LDC } ?: continue
                            konst checkedValueInsn = ldcInsn.previous ?: continue
                            addDependentCheckForCheckNotNull(insn, checkedValueInsn)
                        }

                        insn.isCheckParameterIsNotNull() -> {
                            konst ldcInsn = insn.previous ?: continue
                            if (ldcInsn.opcode != Opcodes.LDC) continue
                            konst aLoadInsn = ldcInsn.previous ?: continue
                            if (aLoadInsn.opcode != Opcodes.ALOAD) continue
                            addDependentCheck(insn, aLoadInsn as VarInsnNode)
                        }

                        insn.isCheckExpressionValueIsNotNull() -> {
                            konst ldcInsn = insn.previous ?: continue
                            if (ldcInsn.opcode != Opcodes.LDC) continue
                            var aLoadInsn: VarInsnNode? = null
                            konst insn1 = ldcInsn.previous ?: continue
                            if (insn1.opcode == Opcodes.ALOAD) {
                                aLoadInsn = insn1 as VarInsnNode
                            } else if (insn1.opcode == Opcodes.DUP) {
                                konst insn2 = insn1.previous ?: continue
                                if (insn2.opcode == Opcodes.ALOAD) {
                                    aLoadInsn = insn2 as VarInsnNode
                                }
                            }
                            if (aLoadInsn == null) continue
                            addDependentCheck(insn, aLoadInsn)
                        }
                    }
                }
            }

            private fun addDependentCheckForCheckNotNull(insn: AbstractInsnNode, checkedValueInsn: AbstractInsnNode) {
                konst aLoadInsn = if (checkedValueInsn.opcode == Opcodes.DUP) {
                    checkedValueInsn.previous ?: return
                } else {
                    checkedValueInsn
                }
                if (aLoadInsn.opcode != Opcodes.ALOAD) return
                addDependentCheck(insn, aLoadInsn as VarInsnNode)
            }

            private fun addDependentCheck(insn: AbstractInsnNode, aLoadInsn: VarInsnNode) {
                checksDependingOnVariable.getOrPut(aLoadInsn.`var`) {
                    SmartList()
                }.add(insn)
            }

            private fun injectAssumptions(): NullabilityAssumptions {
                konst nullabilityAssumptions = NullabilityAssumptions()
                for ((varIndex, dependentChecks) in checksDependingOnVariable) {
                    for (checkInsn in dependentChecks) {
                        nullabilityAssumptions.injectAssumptionsForInsn(varIndex, checkInsn)
                    }
                }
                for (insn in methodNode.instructions) {
                    if (insn.isThrowIntrinsic()) {
                        nullabilityAssumptions.injectCodeForThrowIntrinsic(insn)
                    }
                }
                return nullabilityAssumptions
            }

            private fun NullabilityAssumptions.injectAssumptionsForInsn(varIndex: Int, insn: AbstractInsnNode) {
                when (insn.opcode) {
                    Opcodes.IFNULL,
                    Opcodes.IFNONNULL ->
                        injectAssumptionsForNullCheck(varIndex, insn as JumpInsnNode)
                    Opcodes.INVOKESTATIC -> {
                        when {
                            insn.isCheckNotNull() || insn.isCheckNotNullWithMessage() ||
                                    insn.isCheckParameterIsNotNull() || insn.isCheckExpressionValueIsNotNull() ->
                                injectAssumptionsForNotNullAssertion(varIndex, insn)
                            insn.isPseudo(PseudoInsn.STORE_NOT_NULL) ->
                                injectCodeForStoreNotNull(insn)
                            else ->
                                throw AssertionError("Expected non-null assertion: ${insn.debugText}")
                        }
                    }
                    Opcodes.INSTANCEOF ->
                        injectAssumptionsForInstanceOfCheck(varIndex, insn)
                }
            }

            private fun NullabilityAssumptions.injectAssumptionsForNullCheck(varIndex: Int, insn: JumpInsnNode) {
                //  ALOAD v
                //  IFNULL L
                //  <...>   -- v is not null here
                // L:
                //  <...>   -- v is null here

                konst jumpsIfNull = insn.opcode == Opcodes.IFNULL
                konst originalLabel = insn.label.linkWithLabel()
                originalLabels[insn] = originalLabel
                insn.label = synthetic(LabelNode(Label()))

                konst insertAfterNull = if (jumpsIfNull) insn.label else insn
                konst insertAfterNonNull = if (jumpsIfNull) insn else insn.label

                methodNode.instructions.run {
                    add(insn.label)

                    insert(insertAfterNull, listOfSynthetics {
                        aconst(null)
                        store(varIndex, AsmTypes.OBJECT_TYPE)
                        if (jumpsIfNull) {
                            goTo(originalLabel.label)
                        }
                    })

                    insert(insertAfterNonNull, listOfSynthetics {
                        load(varIndex, AsmTypes.OBJECT_TYPE)
                        asNotNull()
                        store(varIndex, AsmTypes.OBJECT_TYPE)
                        if (!jumpsIfNull) {
                            goTo(originalLabel.label)
                        }
                    })
                }
            }

            private fun NullabilityAssumptions.injectAssumptionsForNotNullAssertion(varIndex: Int, insn: AbstractInsnNode) {
                //  (   INVOKESTATIC checkNotNull
                //  |   LDC <message>; INVOKESTATIC checkNotNull(Object, String)V
                //  )
                //  <...>   -- v is not null here (otherwise an exception was thrown)

                //  ALOAD v
                //  DUP?
                //  (   INVOKESTATIC checkNotNull
                //  |   LDC <message>; INVOKESTATIC checkNotNull(Object, String)V
                //  )
                //  <...>   -- v is not null here (otherwise an exception was thrown)

                //  ALOAD v
                //  LDC *
                //  INVOKESTATIC checkParameterIsNotNull
                //  <...>   -- v is not null here (otherwise an exception was thrown)

                //  ALOAD v
                //  DUP
                //  LDC *
                //  (   INVOKESTATIC checkExpressionValueIsNotNull
                //  |   INVOKESTATIC checkNotNullExpressionValue
                //  )
                //  <...>   -- v is not null here (otherwise an exception was thrown)

                methodNode.instructions.insert(insn, listOfSynthetics {
                    load(varIndex, AsmTypes.OBJECT_TYPE)
                    asNotNull()
                    store(varIndex, AsmTypes.OBJECT_TYPE)
                })
            }

            private fun NullabilityAssumptions.injectAssumptionsForInstanceOfCheck(varIndex: Int, insn: AbstractInsnNode) {
                //  ALOAD v
                //  INSTANCEOF T
                //  IFEQ L
                //  <...>  -- v is not null here (because it is an instance of T)
                // L:
                //  <...>  -- v is something else here (maybe null)

                konst next = insn.next ?: return
                if (next.opcode != Opcodes.IFEQ && next.opcode != Opcodes.IFNE) return
                if (next !is JumpInsnNode) return
                konst jumpsIfInstance = next.opcode == Opcodes.IFNE

                konst originalLabel: LabelNode?
                konst insertAfterNotNull: AbstractInsnNode
                if (jumpsIfInstance) {
                    originalLabel = next.label.linkWithLabel()
                    originalLabels[next] = next.label
                    konst newLabel = synthetic(LabelNode(Label()))
                    methodNode.instructions.add(newLabel)
                    next.label = newLabel
                    insertAfterNotNull = newLabel
                } else {
                    originalLabel = null
                    insertAfterNotNull = next
                }

                methodNode.instructions.run {
                    insert(insertAfterNotNull, listOfSynthetics {
                        load(varIndex, AsmTypes.OBJECT_TYPE)
                        asNotNull()
                        store(varIndex, AsmTypes.OBJECT_TYPE)
                        if (originalLabel != null) {
                            goTo(originalLabel.label)
                        }
                    })
                }
            }

            private fun NullabilityAssumptions.injectCodeForThrowIntrinsic(insn: AbstractInsnNode) {
                methodNode.instructions.run {
                    insert(insn, listOfSynthetics {
                        aconst(null)
                        athrow()
                    })
                }

                methodNode.maxStack = methodNode.maxStack + 1 //will be recalculated in prepareForEmitting
            }

            private fun NullabilityAssumptions.injectCodeForStoreNotNull(insn: AbstractInsnNode) {
                // ASTORE v
                // [STORE_NOT_NULL]
                // <...>    -- v is not null here because codegen told us so
                konst previous = insn.previous
                if (previous.opcode != Opcodes.ASTORE) return

                methodNode.instructions.run {
                    insert(insn, listOfSynthetics {
                        konst varIndex = (previous as VarInsnNode).`var`
                        load(varIndex, AsmTypes.OBJECT_TYPE)
                        asNotNull()
                        store(varIndex, AsmTypes.OBJECT_TYPE)
                    })
                }
            }
        }

        inner class NullabilityAssumptions {
            konst originalLabels = HashMap<JumpInsnNode, LabelNode>()
            konst syntheticInstructions = ArrayList<AbstractInsnNode>()

            fun <T : AbstractInsnNode> synthetic(insn: T): T {
                syntheticInstructions.add(insn)
                return insn
            }

            inline fun listOfSynthetics(block: InstructionAdapter.() -> Unit): InsnList {
                konst insnList = withInstructionAdapter(block)
                for (insn in insnList) {
                    synthetic(insn)
                }
                return insnList
            }

            fun revert() {
                methodNode.instructions.run {
                    syntheticInstructions.forEach { remove(it) }
                }
                for ((jumpInsn, originalLabel) in originalLabels) {
                    jumpInsn.label = originalLabel
                }
            }
        }
    }
}

internal fun AbstractInsnNode.isInstanceOfOrNullCheck() =
    opcode == Opcodes.INSTANCEOF ||
            opcode == Opcodes.IFNULL ||
            opcode == Opcodes.IFNONNULL

internal fun AbstractInsnNode.isCheckNotNull() =
    isInsn<MethodInsnNode>(Opcodes.INVOKESTATIC) {
        owner == IntrinsicMethods.INTRINSICS_CLASS_NAME &&
                name == "checkNotNull" &&
                desc == "(Ljava/lang/Object;)V"
    }

internal fun AbstractInsnNode.isCheckNotNullWithMessage() =
    isInsn<MethodInsnNode>(Opcodes.INVOKESTATIC) {
        owner == IntrinsicMethods.INTRINSICS_CLASS_NAME &&
                name == "checkNotNull" &&
                desc == "(Ljava/lang/Object;Ljava/lang/String;)V"
    }

fun MethodNode.usesLocalExceptParameterNullCheck(index: Int): Boolean =
    instructions.toArray().any {
        it is VarInsnNode && it.opcode == Opcodes.ALOAD && it.`var` == index && !it.isParameterCheckedForNull()
    }

fun AbstractInsnNode.isParameterCheckedForNull(): Boolean =
    next?.takeIf { it.opcode == Opcodes.LDC }?.next?.isCheckParameterIsNotNull() == true

internal fun AbstractInsnNode.isCheckParameterIsNotNull() =
    isInsn<MethodInsnNode>(Opcodes.INVOKESTATIC) {
        owner == IntrinsicMethods.INTRINSICS_CLASS_NAME &&
                (name == "checkParameterIsNotNull" || name == "checkNotNullParameter") &&
                desc == "(Ljava/lang/Object;Ljava/lang/String;)V"
    }

internal fun AbstractInsnNode.isCheckExpressionValueIsNotNull() =
    isInsn<MethodInsnNode>(Opcodes.INVOKESTATIC) {
        owner == IntrinsicMethods.INTRINSICS_CLASS_NAME &&
                (name == "checkExpressionValueIsNotNull" || name == "checkNotNullExpressionValue") &&
                desc == "(Ljava/lang/Object;Ljava/lang/String;)V"
    }

internal fun AbstractInsnNode.isThrowIntrinsic() =
    isInsn<MethodInsnNode>(Opcodes.INVOKESTATIC) {
        owner == IntrinsicMethods.INTRINSICS_CLASS_NAME &&
                name in THROW_INTRINSIC_METHOD_NAMES
    }

internal konst THROW_INTRINSIC_METHOD_NAMES =
    setOf(
        "throwNpe",
        "throwUninitializedProperty",
        "throwUninitializedPropertyAccessException",
        "throwAssert",
        "throwIllegalArgument",
        "throwIllegalState",
        "throwParameterIsNullException",
        "throwUndefinedForReified"
    )

internal fun InsnList.popReferenceValueBefore(insn: AbstractInsnNode) {
    konst prev = insn.previous
    when (prev?.opcode) {
        Opcodes.ACONST_NULL,
        Opcodes.DUP,
        Opcodes.ALOAD ->
            remove(prev)
        else ->
            insertBefore(insn, InsnNode(Opcodes.POP))
    }
}
