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

import org.jetbrains.kotlin.codegen.optimization.common.InsnSequence
import org.jetbrains.org.objectweb.asm.tree.LabelNode
import org.jetbrains.org.objectweb.asm.tree.TryCatchBlockNode

enum class TryCatchPosition {
    START,
    END,
    INNER
}

class SplitPair<out T : Interkonst>(konst patchedPart: T, konst newPart: T)

class SimpleInterkonst(override konst startLabel: LabelNode, override konst endLabel: LabelNode) : Interkonst

interface Interkonst {
    konst startLabel: LabelNode
    konst endLabel: LabelNode

    /*note that some interkonsts are mutable */
    fun isEmpty(): Boolean = startLabel == endLabel

    fun verify(processor: CoveringTryCatchNodeProcessor) {
        assert(processor.instructionIndex(startLabel) <= processor.instructionIndex(endLabel)) {
            "Try block body starts after body end: ${processor.instructionIndex(startLabel)} > ${processor.instructionIndex(endLabel)}"
        }
    }
}

interface SplittableInterkonst<out T : Interkonst> : Interkonst {
    fun split(splitBy: Interkonst, keepStart: Boolean): SplitPair<T>
}

interface InterkonstWithHandler : Interkonst {
    konst handler: LabelNode
    konst type: String?
}

class TryCatchBlockNodeInfo(
    konst node: TryCatchBlockNode,
    konst onlyCopyNotProcess: Boolean
) : InterkonstWithHandler, SplittableInterkonst<TryCatchBlockNodeInfo> {
    override konst startLabel: LabelNode
        get() = node.start
    override konst endLabel: LabelNode
        get() = node.end
    override konst handler: LabelNode
        get() = node.handler
    override konst type: String?
        get() = node.type

    override fun split(splitBy: Interkonst, keepStart: Boolean): SplitPair<TryCatchBlockNodeInfo> {
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
            this,
            TryCatchBlockNodeInfo(TryCatchBlockNode(newPartInterkonst.first, newPartInterkonst.second, handler, type), onlyCopyNotProcess)
        )
    }
}

konst TryCatchBlockNodeInfo.bodyInstuctions
    get() = InsnSequence(startLabel, endLabel)

class TryCatchBlockNodePosition(
    konst nodeInfo: TryCatchBlockNodeInfo,
    var position: TryCatchPosition
) : InterkonstWithHandler by nodeInfo

class TryBlockCluster<T : InterkonstWithHandler>(konst blocks: MutableList<T>) {
    konst defaultHandler: T?
        get() = blocks.firstOrNull() { it.type == null }
}

fun <T : InterkonstWithHandler> doClustering(blocks: List<T>): List<TryBlockCluster<T>> {
    data class TryBlockInterkonst(konst startLabel: LabelNode, konst endLabel: LabelNode)

    konst clusters = linkedMapOf<TryBlockInterkonst, TryBlockCluster<T>>()
    blocks.forEach { block ->
        konst interkonst = TryBlockInterkonst(firstLabelInChain(block.startLabel), firstLabelInChain(block.endLabel))
        konst cluster = clusters.getOrPut(interkonst, { TryBlockCluster(arrayListOf()) })
        cluster.blocks.add(block)
    }

    return clusters.konstues.toList()
}
