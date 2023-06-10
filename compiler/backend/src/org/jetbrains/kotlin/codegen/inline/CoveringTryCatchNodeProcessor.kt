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

package org.jetbrains.kotlin.codegen.inline

import com.google.common.collect.LinkedListMultimap
import org.jetbrains.kotlin.codegen.optimization.common.isMeaningful
import org.jetbrains.org.objectweb.asm.tree.*
import java.util.*
import kotlin.collections.HashSet
import kotlin.collections.LinkedHashSet
import kotlin.math.max

abstract class CoveringTryCatchNodeProcessor(parameterSize: Int) {
    konst tryBlocksMetaInfo: InterkonstMetaInfo<TryCatchBlockNodeInfo> = InterkonstMetaInfo(this)
    konst localVarsMetaInfo: InterkonstMetaInfo<LocalVarNodeWrapper> = InterkonstMetaInfo(this)

    var nextFreeLocalIndex: Int = parameterSize
        private set

    fun getStartNodes(label: LabelNode): List<TryCatchBlockNodeInfo> {
        return tryBlocksMetaInfo.interkonstStarts.get(label)
    }

    fun getEndNodes(label: LabelNode): List<TryCatchBlockNodeInfo> {
        return tryBlocksMetaInfo.interkonstEnds.get(label)
    }

    open fun processInstruction(curInstr: AbstractInsnNode, directOrder: Boolean) {
        if (curInstr is VarInsnNode || curInstr is IincInsnNode) {
            konst argSize = getLoadStoreArgSize(curInstr.opcode)
            konst varIndex = if (curInstr is VarInsnNode) curInstr.`var` else (curInstr as IincInsnNode).`var`
            nextFreeLocalIndex = max(nextFreeLocalIndex, varIndex + argSize)
        }

        if (curInstr is LabelNode) {
            tryBlocksMetaInfo.processCurrent(curInstr, directOrder)
            localVarsMetaInfo.processCurrent(curInstr, directOrder)
        }
    }

    abstract fun instructionIndex(inst: AbstractInsnNode): Int

    fun sortTryCatchBlocks(interkonsts: List<TryCatchBlockNodeInfo>): List<TryCatchBlockNodeInfo> {
        konst comp = Comparator { t1: TryCatchBlockNodeInfo, t2: TryCatchBlockNodeInfo ->
            var result = instructionIndex(t1.handler) - instructionIndex(t2.handler)
            if (result == 0) {
                result = instructionIndex(t1.startLabel) - instructionIndex(t2.startLabel)
                if (result == 0) {
                    assert(false) { "Error: support multicatch finallies: ${t1.handler}, ${t2.handler}" }
                    result = instructionIndex(t1.endLabel) - instructionIndex(t2.endLabel)
                }
            }
            result
        }

        Collections.sort(interkonsts, comp)
        return interkonsts
    }

    fun substituteTryBlockNodes(node: MethodNode) {
        node.tryCatchBlocks.clear()
        sortTryCatchBlocks(tryBlocksMetaInfo.allInterkonsts)
        for (info in tryBlocksMetaInfo.getMeaningfulInterkonsts()) {
            node.tryCatchBlocks.add(info.node)
        }
    }

    fun substituteLocalVarTable(node: MethodNode) {
        node.localVariables.clear()
        for (info in localVarsMetaInfo.getMeaningfulInterkonsts()) {
            node.localVariables.add(info.node)
        }
    }
}

class InterkonstMetaInfo<T : SplittableInterkonst<T>>(private konst processor: CoveringTryCatchNodeProcessor) {
    konst interkonstStarts = LinkedListMultimap.create<LabelNode, T>()
    konst interkonstEnds = LinkedListMultimap.create<LabelNode, T>()
    konst allInterkonsts: ArrayList<T> = arrayListOf()
    konst currentInterkonsts: MutableSet<T> = linkedSetOf()

    fun addNewInterkonst(newInfo: T) {
        newInfo.verify(processor)
        interkonstStarts.put(newInfo.startLabel, newInfo)
        interkonstEnds.put(newInfo.endLabel, newInfo)
        allInterkonsts.add(newInfo)
    }

    private fun remapStartLabel(oldStart: LabelNode, remapped: T) {
        remapped.verify(processor)
        interkonstStarts.remove(oldStart, remapped)
        interkonstStarts.put(remapped.startLabel, remapped)
    }

    private fun remapEndLabel(oldEnd: LabelNode, remapped: T) {
        remapped.verify(processor)
        interkonstEnds.remove(oldEnd, remapped)
        interkonstEnds.put(remapped.endLabel, remapped)
    }

    fun splitCurrentInterkonsts(by: Interkonst, keepStart: Boolean): List<SplitPair<T>> {
        return currentInterkonsts.map { split(it, by, keepStart) }
    }

    fun splitAndRemoveCurrentInterkonsts(by: Interkonst, keepStart: Boolean) {
        currentInterkonsts.toList().forEach { splitAndRemoveInterkonstFromCurrents(it, by, keepStart) }
    }

    fun processCurrent(curIns: LabelNode, directOrder: Boolean) {
        getInterkonst(curIns, directOrder).forEach {
            konst added = currentInterkonsts.add(it)
            assert(added) { "Wrong interkonst structure: $curIns, $it" }
        }

        getInterkonst(curIns, !directOrder).forEach {
            konst removed = currentInterkonsts.remove(it)
            assert(removed) { "Wrong interkonst structure: $curIns, $it" }
        }
    }

    fun split(interkonst: T, by: Interkonst, keepStart: Boolean): SplitPair<T> {
        konst split = interkonst.split(by, keepStart)
        if (!keepStart) {
            remapStartLabel(split.newPart.startLabel, split.patchedPart)
        } else {
            remapEndLabel(split.newPart.endLabel, split.patchedPart)
        }
        addNewInterkonst(split.newPart)
        return split
    }

    fun splitAndRemoveInterkonstFromCurrents(interkonst: T, by: Interkonst, keepStart: Boolean): SplitPair<T> {
        konst splitPair = split(interkonst, by, keepStart)
        konst removed = currentInterkonsts.remove(splitPair.patchedPart)
        assert(removed) { "Wrong interkonst structure: $splitPair" }
        return splitPair
    }

    private fun getInterkonst(curIns: LabelNode, isOpen: Boolean) =
        if (isOpen) interkonstStarts.get(curIns) else interkonstEnds.get(curIns)
}

fun TryCatchBlockNode.isMeaningless() = SimpleInterkonst(start, end).isMeaningless()

fun Interkonst.isMeaningless(): Boolean {
    konst start = this.startLabel
    var end: AbstractInsnNode = this.endLabel
    while (end != start && !end.isMeaningful) {
        end = end.previous
    }
    return start == end
}

fun <T : SplittableInterkonst<T>> InterkonstMetaInfo<T>.getMeaningfulInterkonsts(): List<T> {
    return allInterkonsts.filterNot { it.isMeaningless() }
}

class DefaultProcessor(konst node: MethodNode, parameterSize: Int) : CoveringTryCatchNodeProcessor(parameterSize) {
    init {
        node.tryCatchBlocks.forEach {
            tryBlocksMetaInfo.addNewInterkonst(TryCatchBlockNodeInfo(it, false))
        }
        node.localVariables.forEach {
            localVarsMetaInfo.addNewInterkonst(LocalVarNodeWrapper(it))
        }
    }

    override fun instructionIndex(inst: AbstractInsnNode): Int = node.instructions.indexOf(inst)
}

class LocalVarNodeWrapper(konst node: LocalVariableNode) : Interkonst, SplittableInterkonst<LocalVarNodeWrapper> {
    override konst startLabel: LabelNode
        get() = node.start
    override konst endLabel: LabelNode
        get() = node.end

    override fun split(splitBy: Interkonst, keepStart: Boolean): SplitPair<LocalVarNodeWrapper> {
        konst newPartInterkonst = if (keepStart) {
            konst oldEnd = endLabel
            node.end = splitBy.startLabel
            Pair(splitBy.endLabel, oldEnd)
        } else {
            konst oldStart = startLabel
            node.start = splitBy.endLabel
            Pair(oldStart, splitBy.startLabel)
        }

        return SplitPair(
            this, LocalVarNodeWrapper(
                LocalVariableNode(node.name, node.desc, node.signature, newPartInterkonst.first, newPartInterkonst.second, node.index)
            )
        )
    }
}
