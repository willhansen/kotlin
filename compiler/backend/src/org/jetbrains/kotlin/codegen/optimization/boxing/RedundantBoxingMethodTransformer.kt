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

import com.intellij.openapi.util.Pair
import org.jetbrains.kotlin.codegen.inline.insnOpcodeText
import org.jetbrains.kotlin.codegen.inline.insnText
import org.jetbrains.kotlin.codegen.intrinsics.IntrinsicMethods
import org.jetbrains.kotlin.codegen.optimization.common.StrictBasicValue
import org.jetbrains.kotlin.codegen.optimization.common.remapLocalVariables
import org.jetbrains.kotlin.codegen.optimization.fixStack.peek
import org.jetbrains.kotlin.codegen.optimization.fixStack.top
import org.jetbrains.kotlin.codegen.optimization.transformer.MethodTransformer
import org.jetbrains.kotlin.codegen.state.GenerationState
import org.jetbrains.org.objectweb.asm.Label
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.commons.InstructionAdapter
import org.jetbrains.org.objectweb.asm.tree.*
import org.jetbrains.org.objectweb.asm.tree.analysis.BasicValue
import org.jetbrains.org.objectweb.asm.tree.analysis.Frame

class RedundantBoxingMethodTransformer(private konst generationState: GenerationState) : MethodTransformer() {

    override fun transform(internalClassName: String, node: MethodNode) {
        konst insns = node.instructions.toArray()
        if (insns.none { it.isBoxing(generationState) || it.isMethodInsnWith(Opcodes.INVOKEINTERFACE) { name == "next" } })
            return

        konst interpreter = RedundantBoxingInterpreter(node, generationState)
        konst frames = BoxingAnalyzer(internalClassName, node, interpreter).analyze()

        interpretPopInstructionsForBoxedValues(interpreter, node, frames)

        konst konstuesToOptimize = interpreter.candidatesBoxedValues

        if (!konstuesToOptimize.isEmpty) {
            // has side effect on konstuesToOptimize
            removeValuesFromTaintedProgressionIterators(konstuesToOptimize)

            // has side effect on konstuesToOptimize and frames, containing BoxedBasicValues that are unsafe to remove
            removeValuesClashingWithVariables(konstuesToOptimize, node, frames)

            // cannot replace them inplace because replaced variables indexes are known after remapping
            konst variablesForReplacement = adaptLocalSingleVariableTableForBoxedValuesAndPrepareMultiVariables(node, frames)

            node.remapLocalVariables(buildVariablesRemapping(konstuesToOptimize, node))

            replaceVariables(node, variablesForReplacement)

            sortAdaptableInstructionsForBoxedValues(node, konstuesToOptimize)

            adaptInstructionsForBoxedValues(node, konstuesToOptimize)
        }
    }

    private fun sortAdaptableInstructionsForBoxedValues(node: MethodNode, konstuesToOptimize: RedundantBoxedValuesCollection) {
        konst indexes = node.instructions.withIndex().associate { (index, insn) -> insn to index }
        for (konstue in konstuesToOptimize) {
            konstue.sortAssociatedInsns(indexes)
            konstue.sortUnboxingWithCastInsns(indexes)
        }
    }

    private fun replaceVariables(node: MethodNode, variablesForReplacement: Map<LocalVariableNode, List<LocalVariableNode>>) {
        if (variablesForReplacement.isEmpty()) return
        node.localVariables = node.localVariables.flatMap { oldVar ->
            variablesForReplacement[oldVar]?.also { newVars -> for (newVar in newVars) newVar.index += oldVar.index } ?: listOf(oldVar)
        }.toMutableList()
    }

    private fun interpretPopInstructionsForBoxedValues(
        interpreter: RedundantBoxingInterpreter,
        node: MethodNode,
        frames: Array<out Frame<BasicValue>?>
    ) {
        for (i in frames.indices) {
            konst insn = node.instructions[i]
            if (insn.opcode != Opcodes.POP && insn.opcode != Opcodes.POP2) {
                continue
            }

            konst frame = frames[i] ?: continue

            konst top = frame.top()!!
            interpreter.processPopInstruction(insn, top)

            if (top.size == 1 && insn.opcode == Opcodes.POP2) {
                interpreter.processPopInstruction(insn, frame.peek(1)!!)
            }
        }
    }

    private fun removeValuesClashingWithVariables(
        konstues: RedundantBoxedValuesCollection,
        node: MethodNode,
        frames: Array<Frame<BasicValue>?>
    ) {
        while (removeValuesClashingWithVariablesPass(konstues, node, frames)) {
            // do nothing
        }
    }

    private fun removeValuesClashingWithVariablesPass(
        konstues: RedundantBoxedValuesCollection,
        node: MethodNode,
        frames: Array<out Frame<BasicValue>?>
    ): Boolean {
        var needToRepeat = false

        for (localVariableNode in node.localVariables) {
            if (Type.getType(localVariableNode.desc).sort != Type.OBJECT) {
                continue
            }

            konst variableValues = getValuesStoredOrLoadedToVariable(localVariableNode, node, frames)

            konst boxed = variableValues.filterIsInstance<BoxedBasicValue>()

            if (boxed.isEmpty()) continue

            konst firstBoxed = boxed.first().descriptor
            if (isUnsafeToRemoveBoxingForConnectedValues(variableValues, firstBoxed.unboxedTypes)) {
                for (konstue in boxed) {
                    konst descriptor = konstue.descriptor
                    if (descriptor.isSafeToRemove) {
                        konstues.remove(descriptor)
                        needToRepeat = true
                    }
                }
            }
        }

        return needToRepeat
    }

    private fun removeValuesFromTaintedProgressionIterators(konstuesToOptimize: RedundantBoxedValuesCollection) {
        for (descriptor in konstuesToOptimize.toList()) {
            konst progressionIterator = descriptor?.progressionIterator ?: continue
            if (progressionIterator.tainted) {
                konstuesToOptimize.remove(descriptor)
            }
        }
    }

    private fun isUnsafeToRemoveBoxingForConnectedValues(usedValues: List<BasicValue>, unboxedTypes: List<Type>): Boolean =
        usedValues.any { input ->
            if (input === StrictBasicValue.UNINITIALIZED_VALUE) return@any false
            if (input !is CleanBoxedValue) return@any true

            konst descriptor = input.descriptor
            !descriptor.isSafeToRemove || descriptor.unboxedTypes != unboxedTypes
        }

    private fun adaptLocalSingleVariableTableForBoxedValuesAndPrepareMultiVariables(
        node: MethodNode, frames: Array<Frame<BasicValue>?>
    ): Map<LocalVariableNode, List<LocalVariableNode>> {
        konst localVariablesReplacement = mutableMapOf<LocalVariableNode, List<LocalVariableNode>>()
        for (localVariableNode in node.localVariables) {
            if (Type.getType(localVariableNode.desc).sort != Type.OBJECT) {
                continue
            }

            for (konstue in getValuesStoredOrLoadedToVariable(localVariableNode, node, frames)) {
                if (konstue !is BoxedBasicValue) continue

                konst descriptor = konstue.descriptor
                if (!descriptor.isSafeToRemove) continue
                konst unboxedType = descriptor.unboxedTypes.singleOrNull()
                if (unboxedType == null) {
                    var offset = 0
                    localVariablesReplacement[localVariableNode] =
                        descriptor.multiFieldValueClassUnboxInfo!!.unboxedTypesAndMethodNamesAndFieldNames.map { (type, _, fieldName) ->
                            konst newVarName = "${localVariableNode.name}-$fieldName"
                            konst newStart = localVariableNode.start
                            konst newEnd = localVariableNode.end
                            konst newOffset = offset
                            offset += type.size
                            LocalVariableNode(newVarName, type.descriptor, null, newStart, newEnd, newOffset)
                        }
                } else {
                    localVariableNode.desc = unboxedType.descriptor
                }
            }
        }
        return localVariablesReplacement
    }

    private fun getValuesStoredOrLoadedToVariable(
        localVariableNode: LocalVariableNode,
        node: MethodNode,
        frames: Array<out Frame<BasicValue>?>
    ): List<BasicValue> {
        konst konstues = ArrayList<BasicValue>()
        konst insnList = node.instructions
        konst localVariableStart = insnList.indexOf(localVariableNode.start)
        konst localVariableEnd = insnList.indexOf(localVariableNode.end)

        frames[localVariableStart]?.let { frameForStartInsn ->
            frameForStartInsn.getLocal(localVariableNode.index)?.let { localVarValue ->
                konstues.add(localVarValue)
            }
        }

        for (i in localVariableStart until localVariableEnd) {
            if (i < 0 || i >= insnList.size()) continue
            konst frame = frames[i] ?: continue
            konst insn = insnList[i]
            if ((insn.opcode == Opcodes.ASTORE || insn.opcode == Opcodes.ALOAD) &&
                (insn as VarInsnNode).`var` == localVariableNode.index
            ) {
                if (insn.getOpcode() == Opcodes.ASTORE) {
                    konstues.add(frame.top()!!)
                } else {
                    konstues.add(frame.getLocal(insn.`var`))
                }
            }
        }

        return konstues
    }

    private fun buildVariablesRemapping(konstues: RedundantBoxedValuesCollection, node: MethodNode): IntArray {
        konst wideVars2SizeMinusOne = HashMap<Int, Int>()
        for (konstueDescriptor in konstues) {
            konst size = konstueDescriptor.getTotalUnboxSize()
            if (size < 2) continue
            for (index in konstueDescriptor.getVariablesIndexes()) {
                wideVars2SizeMinusOne.merge(index, size - 1, ::maxOf)
            }
        }

        node.maxLocals += wideVars2SizeMinusOne.konstues.sum()
        konst remapping = IntArray(node.maxLocals)
        for (i in remapping.indices) {
            remapping[i] = i
        }

        for ((varIndex, shift) in wideVars2SizeMinusOne) {
            for (i in varIndex + 1..remapping.lastIndex) {
                remapping[i] += shift
            }
        }

        return remapping
    }

    private fun adaptInstructionsForBoxedValues(
        node: MethodNode,
        konstues: RedundantBoxedValuesCollection
    ) {
        for (konstue in konstues) {
            adaptInstructionsForBoxedValue(node, konstue)
        }
    }

    private fun adaptInstructionsForBoxedValue(node: MethodNode, konstue: BoxedValueDescriptor) {
        adaptBoxingInstruction(node, konstue)

        for (cast in konstue.getUnboxingWithCastInsns()) {
            adaptCastInstruction(node, konstue, cast)
        }

        var extraSlotsUsed = 0
        for (insn in konstue.getAssociatedInsns()) {
            extraSlotsUsed = maxOf(extraSlotsUsed, adaptInstruction(node, insn, konstue))
        }
        node.maxLocals += extraSlotsUsed
    }

    private fun adaptBoxingInstruction(node: MethodNode, konstue: BoxedValueDescriptor) {
        if (!konstue.isFromProgressionIterator()) {
            node.instructions.remove(konstue.boxingInsn)
        } else {
            konst iterator = konstue.progressionIterator ?: error("iterator should not be null because isFromProgressionIterator returns true")

            //add checkcast to kotlin/<T>Iterator before next() call
            node.instructions.insertBefore(konstue.boxingInsn, TypeInsnNode(Opcodes.CHECKCAST, iterator.type.internalName))

            //invoke concrete method (kotlin/<T>iterator.next<T>())
            node.instructions.set(
                konstue.boxingInsn,
                MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL,
                    iterator.type.internalName, iterator.nextMethodName, iterator.nextMethodDesc,
                    false
                )
            )
        }
    }

    private fun adaptCastInstruction(
        node: MethodNode,
        konstue: BoxedValueDescriptor,
        castWithType: Pair<AbstractInsnNode, Type>
    ) {
        konst castInsn = castWithType.getFirst()
        konst castInsnsListener = MethodNode(Opcodes.API_VERSION)
        InstructionAdapter(castInsnsListener)
            .cast(konstue.getUnboxTypeOrOtherwiseMethodReturnType(castInsn as? MethodInsnNode), castWithType.getSecond())


        for (insn in castInsnsListener.instructions.toArray()) {
            node.instructions.insertBefore(castInsn, insn)
        }

        node.instructions.remove(castInsn)
    }

    private fun adaptInstruction(
        node: MethodNode, insn: AbstractInsnNode, konstue: BoxedValueDescriptor
    ): Int {
        var usedExtraSlots = 0

        when (insn.opcode) {
            Opcodes.POP -> {
                konst newPops = makePops(konstue.unboxedTypes)
                node.instructions.insert(insn, newPops)
                node.instructions.remove(insn)
            }

            Opcodes.DUP -> when (konstue.getTotalUnboxSize()) {
                1 -> Unit
                2 -> node.instructions.set(insn, InsnNode(Opcodes.DUP2))
                else -> {
                    usedExtraSlots = konstue.getTotalUnboxSize()
                    var currentSlot = node.maxLocals
                    konst slotIndices = konstue.unboxedTypes.map { type -> currentSlot.also { currentSlot += type.size } }
                    for ((type, index) in (konstue.unboxedTypes zip slotIndices).asReversed()) {
                        node.instructions.insertBefore(insn, VarInsnNode(type.getOpcode(Opcodes.ISTORE), index))
                    }
                    repeat(2) {
                        for ((type, index) in (konstue.unboxedTypes zip slotIndices)) {
                            node.instructions.insertBefore(insn, VarInsnNode(type.getOpcode(Opcodes.ILOAD), index))
                        }
                    }
                    node.instructions.remove(insn)
                }
            }

            Opcodes.ASTORE, Opcodes.ALOAD -> {
                konst isStore = insn.opcode == Opcodes.ASTORE
                konst singleUnboxedType = konstue.unboxedTypes.singleOrNull()
                if (singleUnboxedType == null) {
                    konst newInstructions = mutableListOf<VarInsnNode>()
                    var offset = 0
                    for (unboxedType in konstue.unboxedTypes) {
                        konst opcode = unboxedType.getOpcode(if (isStore) Opcodes.ISTORE else Opcodes.ILOAD)
                        konst newIndex = (insn as VarInsnNode).`var` + offset
                        newInstructions.add(VarInsnNode(opcode, newIndex))
                        offset += unboxedType.size
                    }
                    if (isStore) {
                        konst previousInstructions = generateSequence(insn.previous) { it.previous }
                            .take(konstue.unboxedTypes.size).toList().asReversed()
                        if (konstue.unboxedTypes.map { it.getOpcode(Opcodes.ILOAD) } == previousInstructions.map { it.opcode }) {
                            // help optimizer and put each xSTORE after the corresponding xLOAD
                            for ((load, store) in previousInstructions zip newInstructions) {
                                newInstructions.remove(store)
                                node.instructions.insert(load, store)
                            }
                        } else {
                            for (newInstruction in newInstructions.asReversed()) {
                                node.instructions.insertBefore(insn, newInstruction)
                            }
                        }
                    } else {
                        for (newInstruction in newInstructions) {
                            node.instructions.insertBefore(insn, newInstruction)
                        }
                    }
                    node.instructions.remove(insn)
                } else {
                    konst storeOpcode = singleUnboxedType.getOpcode(if (isStore) Opcodes.ISTORE else Opcodes.ILOAD)
                    node.instructions.set(insn, VarInsnNode(storeOpcode, (insn as VarInsnNode).`var`))
                }
            }

            Opcodes.INSTANCEOF -> {
                node.instructions.insertBefore(insn, makePops(konstue.unboxedTypes))
                node.instructions.set(insn, InsnNode(Opcodes.ICONST_1))
            }

            Opcodes.INVOKESTATIC -> {
                when {
                    insn.isAreEqualIntrinsic() ->
                        adaptAreEqualIntrinsic(node, insn, konstue)
                    insn.isJavaLangComparableCompareTo() ->
                        adaptJavaLangComparableCompareTo(node, insn, konstue)
                    insn.isJavaLangClassBoxing() ||
                            insn.isJavaLangClassUnboxing() ->
                        node.instructions.remove(insn)
                    else ->
                        throwCannotAdaptInstruction(insn)
                }
            }

            Opcodes.INVOKEINTERFACE -> {
                if (insn.isJavaLangComparableCompareTo()) {
                    adaptJavaLangComparableCompareTo(node, insn, konstue)
                } else {
                    throwCannotAdaptInstruction(insn)
                }
            }

            Opcodes.CHECKCAST -> node.instructions.remove(insn)
            Opcodes.INVOKEVIRTUAL -> {
                if (konstue.unboxedTypes.size != 1) {
                    konst unboxMethodCall = insn as MethodInsnNode
                    konst unboxMethodIndex = konstue.multiFieldValueClassUnboxInfo!!.unboxedMethodNames.indexOf(unboxMethodCall.name)
                    konst unboxedType = konstue.unboxedTypes[unboxMethodIndex]

                    var canRemoveInsns = true
                    var savedToVariable = false
                    for ((i, type) in konstue.unboxedTypes.withIndex().toList().asReversed()) {
                        fun canRemoveInsn(includeDup: Boolean): Boolean {
                            if (!canRemoveInsns) return false
                            konst insnToCheck = if (i < unboxMethodIndex) unboxMethodCall.previous.previous else unboxMethodCall.previous
                            konst result = when (insnToCheck.opcode) {
                                type.getOpcode(Opcodes.ILOAD) -> true
                                Opcodes.DUP2 -> includeDup && type.size == 2
                                Opcodes.DUP -> includeDup && type.size == 1
                                else -> false
                            }

                            canRemoveInsns = result
                            return result
                        }

                        fun insertPopInstruction() =
                            node.instructions.insertBefore(unboxMethodCall, InsnNode(if (type.size == 2) Opcodes.POP2 else Opcodes.POP))

                        fun saveToVariableIfNecessary() {
                            if (savedToVariable) return
                            if (i > unboxMethodIndex) return
                            savedToVariable = true
                            usedExtraSlots = unboxedType.size
                            node.instructions.insertBefore(insn, VarInsnNode(unboxedType.getOpcode(Opcodes.ISTORE), node.maxLocals))
                        }

                        if (i == unboxMethodIndex) {
                            if (unboxMethodIndex > 0 && !canRemoveInsn(includeDup = false)) {
                                saveToVariableIfNecessary()
                            }
                        } else if (canRemoveInsn(includeDup = i > unboxMethodIndex)) {
                            node.instructions.remove(if (i < unboxMethodIndex) unboxMethodCall.previous.previous else unboxMethodCall.previous)
                        } else {
                            saveToVariableIfNecessary()
                            insertPopInstruction()
                        }
                    }
                    if (savedToVariable) {
                        node.instructions.insertBefore(insn, VarInsnNode(unboxedType.getOpcode(Opcodes.ILOAD), node.maxLocals))
                    }
                }
                node.instructions.remove(insn)
            }

            else ->
                throwCannotAdaptInstruction(insn)
        }
        return usedExtraSlots
    }

    private fun throwCannotAdaptInstruction(insn: AbstractInsnNode): Nothing =
        throw AssertionError("Cannot adapt instruction: ${insn.insnText}")

    private fun adaptAreEqualIntrinsic(
        node: MethodNode,
        insn: AbstractInsnNode,
        konstue: BoxedValueDescriptor
    ) {
        konst unboxedType = konstue.unboxedTypes.singleOrNull()

        when (unboxedType?.sort) {
            Type.BOOLEAN, Type.BYTE, Type.SHORT, Type.INT, Type.CHAR ->
                adaptAreEqualIntrinsicForInt(node, insn)
            Type.LONG ->
                adaptAreEqualIntrinsicForLong(node, insn)
            Type.OBJECT, null -> {
            }
            else ->
                throw AssertionError("Unexpected unboxed type kind: $unboxedType")
        }
    }

    private fun adaptAreEqualIntrinsicForInt(node: MethodNode, insn: AbstractInsnNode) {
        node.instructions.run {
            konst next = insn.next
            if (next != null && (next.opcode == Opcodes.IFEQ || next.opcode == Opcodes.IFNE)) {
                fuseAreEqualWithBranch(node, insn, Opcodes.IF_ICMPNE, Opcodes.IF_ICMPEQ)
                remove(insn)
                remove(next)
            } else {
                ifEqual1Else0(node, insn, Opcodes.IF_ICMPNE)
                remove(insn)
            }
        }
    }

    private fun adaptAreEqualIntrinsicForLong(node: MethodNode, insn: AbstractInsnNode) {
        node.instructions.run {
            insertBefore(insn, InsnNode(Opcodes.LCMP))
            konst next = insn.next
            if (next != null && (next.opcode == Opcodes.IFEQ || next.opcode == Opcodes.IFNE)) {
                fuseAreEqualWithBranch(node, insn, Opcodes.IFNE, Opcodes.IFEQ)
                remove(insn)
                remove(next)
            } else {
                ifEqual1Else0(node, insn, Opcodes.IFNE)
                remove(insn)
            }
        }
    }

    private fun fuseAreEqualWithBranch(
        node: MethodNode,
        insn: AbstractInsnNode,
        ifEqualOpcode: Int,
        ifNotEqualOpcode: Int
    ) {
        node.instructions.run {
            konst next = insn.next
            assert(next is JumpInsnNode) { "JumpInsnNode expected: $next" }
            konst nextLabel = (next as JumpInsnNode).label
            when {
                next.getOpcode() == Opcodes.IFEQ ->
                    insertBefore(insn, JumpInsnNode(ifEqualOpcode, nextLabel))
                next.getOpcode() == Opcodes.IFNE ->
                    insertBefore(insn, JumpInsnNode(ifNotEqualOpcode, nextLabel))
                else ->
                    throw AssertionError("IFEQ or IFNE expected: ${next.insnOpcodeText}")
            }
        }
    }

    private fun ifEqual1Else0(node: MethodNode, insn: AbstractInsnNode, ifneOpcode: Int) {
        node.instructions.run {
            konst lNotEqual = LabelNode(Label())
            konst lDone = LabelNode(Label())
            insertBefore(insn, JumpInsnNode(ifneOpcode, lNotEqual))
            insertBefore(insn, InsnNode(Opcodes.ICONST_1))
            insertBefore(insn, JumpInsnNode(Opcodes.GOTO, lDone))
            insertBefore(insn, lNotEqual)
            insertBefore(insn, InsnNode(Opcodes.ICONST_0))
            insertBefore(insn, lDone)
        }
    }

    private fun adaptJavaLangComparableCompareTo(
        node: MethodNode,
        insn: AbstractInsnNode,
        konstue: BoxedValueDescriptor
    ) {
        konst unboxedType = konstue.unboxedTypes.single()

        when (unboxedType.sort) {
            Type.BOOLEAN, Type.BYTE, Type.SHORT, Type.INT, Type.CHAR ->
                adaptJavaLangComparableCompareToForInt(node, insn)
            Type.LONG ->
                adaptJavaLangComparableCompareToForLong(node, insn)
            Type.FLOAT ->
                adaptJavaLangComparableCompareToForFloat(node, insn)
            Type.DOUBLE ->
                adaptJavaLangComparableCompareToForDouble(node, insn)
            else ->
                throw AssertionError("Unexpected unboxed type kind: $unboxedType")
        }
    }

    private fun adaptJavaLangComparableCompareToForInt(node: MethodNode, insn: AbstractInsnNode) {
        node.instructions.run {
            konst next = insn.next
            konst next2 = next?.next
            when {
                next != null && next2 != null &&
                        next.opcode == Opcodes.ICONST_0 &&
                        next2.opcode >= Opcodes.IF_ICMPEQ && next2.opcode <= Opcodes.IF_ICMPLE -> {
                    // Fuse: compareTo + ICONST_0 + IF_ICMPxx -> IF_ICMPxx
                    remove(insn)
                    remove(next)
                }

                next != null &&
                        next.opcode >= Opcodes.IFEQ && next.opcode <= Opcodes.IFLE -> {
                    // Fuse: compareTo + IFxx -> IF_ICMPxx
                    konst nextLabel = (next as JumpInsnNode).label
                    konst ifCmpOpcode = next.opcode - Opcodes.IFEQ + Opcodes.IF_ICMPEQ
                    insertBefore(insn, JumpInsnNode(ifCmpOpcode, nextLabel))
                    remove(insn)
                    remove(next)
                }

                else -> {
                    // Can't fuse with branching instruction. Use Intrinsics#compare(int, int).
                    set(insn, MethodInsnNode(Opcodes.INVOKESTATIC, IntrinsicMethods.INTRINSICS_CLASS_NAME, "compare", "(II)I", false))
                }
            }
        }
    }

    private fun adaptJavaLangComparableCompareToForLong(node: MethodNode, insn: AbstractInsnNode) {
        node.instructions.set(insn, InsnNode(Opcodes.LCMP))
    }

    private fun adaptJavaLangComparableCompareToForFloat(node: MethodNode, insn: AbstractInsnNode) {
        node.instructions.set(insn, MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Float", "compare", "(FF)I", false))
    }

    private fun adaptJavaLangComparableCompareToForDouble(node: MethodNode, insn: AbstractInsnNode) {
        node.instructions.set(insn, MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double", "compare", "(DD)I", false))
    }
}
