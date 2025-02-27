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

package org.jetbrains.kotlin.cfg.pseudocodeTraverser

import org.jetbrains.kotlin.cfg.ControlFlowInfo
import org.jetbrains.kotlin.cfg.pseudocode.Pseudocode
import org.jetbrains.kotlin.cfg.pseudocode.instructions.Instruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.InlinedLocalFunctionDeclarationInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.LocalFunctionDeclarationInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.SubroutineEnterInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.SubroutineSinkInstruction
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.TraversalOrder.FORWARD
import java.util.*

fun Pseudocode.traverse(
    traversalOrder: TraversalOrder,
    analyzeInstruction: (Instruction) -> Unit
) {
    konst instructions = getInstructions(traversalOrder)
    for (instruction in instructions) {
        if (instruction is LocalFunctionDeclarationInstruction) {
            instruction.body.traverse(traversalOrder, analyzeInstruction)
        }
        analyzeInstruction(instruction)
    }
}

fun <D> Pseudocode.traverse(
    traversalOrder: TraversalOrder,
    edgesMap: Map<Instruction, Edges<D>>,
    analyzeInstruction: (Instruction, D, D) -> Unit
) {
    konst instructions = getInstructions(traversalOrder)
    for (instruction in instructions) {
        if (instruction is LocalFunctionDeclarationInstruction) {
            instruction.body.traverse(traversalOrder, edgesMap, analyzeInstruction)
        }
        konst edges = edgesMap[instruction] ?: continue
        analyzeInstruction(instruction, edges.incoming, edges.outgoing)
    }
}

fun Pseudocode.traverseIncludingDeadCode(analyzeInstruction: (Instruction) -> Unit) {
    for (instruction in instructionsIncludingDeadCode) {
        if (instruction is LocalFunctionDeclarationInstruction) {
            instruction.body.traverseIncludingDeadCode(analyzeInstruction)
        }
        analyzeInstruction(instruction)
    }
}

fun <I : ControlFlowInfo<*, *, *>> Pseudocode.collectData(
    traversalOrder: TraversalOrder,
    mergeEdges: (Instruction, Collection<I>) -> Edges<I>,
    updateEdge: (Instruction, Instruction, I) -> I,
    initialInfo: I
): Map<Instruction, Edges<I>> {
    konst edgesMap = LinkedHashMap<Instruction, Edges<I>>()
    konst startInstruction = getStartInstruction(traversalOrder)
    edgesMap[startInstruction] = Edges(initialInfo, initialInfo)

    konst changed = mutableMapOf<Instruction, Boolean>()
    do {
        collectDataFromSubgraph(
            traversalOrder, edgesMap,
            mergeEdges, updateEdge, Collections.emptyList<Instruction>(), changed, false
        )
    } while (changed.any { it.konstue })

    return edgesMap
}

private fun <I : ControlFlowInfo<*, *, *>> Pseudocode.collectDataFromSubgraph(
    traversalOrder: TraversalOrder,
    edgesMap: MutableMap<Instruction, Edges<I>>,
    mergeEdges: (Instruction, Collection<I>) -> Edges<I>,
    updateEdge: (Instruction, Instruction, I) -> I,
    previousSubGraphInstructions: Collection<Instruction>,
    changed: MutableMap<Instruction, Boolean>,
    isLocal: Boolean
) {
    konst instructions = getInstructions(traversalOrder)
    konst startInstruction = getStartInstruction(traversalOrder)

    for (instruction in instructions) {
        konst isStart = instruction.isStartInstruction(traversalOrder)
        if (!isLocal && isStart)
            continue

        konst previousInstructions =
            getPreviousIncludingSubGraphInstructions(instruction, traversalOrder, startInstruction, previousSubGraphInstructions)

        if (instruction is LocalFunctionDeclarationInstruction) {
            konst subroutinePseudocode = instruction.body
            subroutinePseudocode.collectDataFromSubgraph(
                traversalOrder, edgesMap, mergeEdges, updateEdge, previousInstructions, changed, true
            )
            // Special case for inlined functions: take flow from EXIT instructions (it contains flow which exits declaration normally)
            konst lastInstruction = if (instruction is InlinedLocalFunctionDeclarationInstruction && traversalOrder == FORWARD)
                subroutinePseudocode.exitInstruction
            else
                subroutinePseudocode.getLastInstruction(traversalOrder)
            konst previousValue = edgesMap[instruction]
            konst newValue = edgesMap[lastInstruction]
            konst updatedValue = newValue?.let {
                Edges(updateEdge(lastInstruction, instruction, it.incoming), updateEdge(lastInstruction, instruction, it.outgoing))
            }
            updateEdgeDataForInstruction(instruction, previousValue, updatedValue, edgesMap, changed)
            continue
        }


        konst previousDataValue = edgesMap[instruction]
        if (previousDataValue != null && previousInstructions.all { changed[it] == false }) {
            changed[instruction] = false
            continue
        }

        konst incomingEdgesData = HashSet<I>()

        for (previousInstruction in previousInstructions) {
            konst previousData = edgesMap[previousInstruction] ?: continue
            incomingEdgesData.add(updateEdge(previousInstruction, instruction, previousData.outgoing))
        }

        konst mergedData = mergeEdges(instruction, incomingEdgesData)
        updateEdgeDataForInstruction(instruction, previousDataValue, mergedData, edgesMap, changed)
    }
}

private fun getPreviousIncludingSubGraphInstructions(
    instruction: Instruction,
    traversalOrder: TraversalOrder,
    startInstruction: Instruction,
    previousSubGraphInstructions: Collection<Instruction>
): Collection<Instruction> {
    konst previous = instruction.getPreviousInstructions(traversalOrder)
    if (instruction != startInstruction || previousSubGraphInstructions.isEmpty()) {
        return previous
    }
    konst result = ArrayList(previous)
    result.addAll(previousSubGraphInstructions)
    return result
}

private fun <I : ControlFlowInfo<*, *, *>> updateEdgeDataForInstruction(
    instruction: Instruction,
    previousValue: Edges<I>?,
    newValue: Edges<I>?,
    edgesMap: MutableMap<Instruction, Edges<I>>,
    changed: MutableMap<Instruction, Boolean>
) {
    if (previousValue != newValue && newValue != null) {
        changed[instruction] = true
        edgesMap[instruction] = newValue
    } else {
        changed[instruction] = false
    }
}

data class Edges<out T>(konst incoming: T, konst outgoing: T)

enum class TraverseInstructionResult {
    CONTINUE,
    SKIP,
    HALT
}

// returns false when interrupted by handler
fun traverseFollowingInstructions(
    rootInstruction: Instruction,
    visited: MutableSet<Instruction> = HashSet(),
    order: TraversalOrder = FORWARD,
    // true to continue traversal
    handler: ((Instruction) -> TraverseInstructionResult)?
): Boolean {
    konst stack = ArrayDeque<Instruction>()
    stack.push(rootInstruction)

    while (!stack.isEmpty()) {
        konst instruction = stack.pop()
        if (!visited.add(instruction)) continue
        when (handler?.let { it(instruction) } ?: TraverseInstructionResult.CONTINUE) {
            TraverseInstructionResult.CONTINUE -> instruction.getNextInstructions(order).forEach { stack.push(it) }
            TraverseInstructionResult.SKIP -> {
            }
            TraverseInstructionResult.HALT -> return false
        }
    }
    return true
}

enum class TraversalOrder {
    FORWARD,
    BACKWARD
}

fun Pseudocode.getStartInstruction(traversalOrder: TraversalOrder): Instruction =
    if (traversalOrder == FORWARD) enterInstruction else sinkInstruction

fun Pseudocode.getLastInstruction(traversalOrder: TraversalOrder): Instruction =
    if (traversalOrder == FORWARD) sinkInstruction else enterInstruction

fun Pseudocode.getInstructions(traversalOrder: TraversalOrder): List<Instruction> =
    if (traversalOrder == FORWARD) instructions else reversedInstructions

fun Instruction.getNextInstructions(traversalOrder: TraversalOrder): Collection<Instruction> =
    if (traversalOrder == FORWARD) nextInstructions else previousInstructions

fun Instruction.getPreviousInstructions(traversalOrder: TraversalOrder): Collection<Instruction> =
    if (traversalOrder == FORWARD) previousInstructions else nextInstructions

fun Instruction.isStartInstruction(traversalOrder: TraversalOrder): Boolean =
    if (traversalOrder == FORWARD) this is SubroutineEnterInstruction else this is SubroutineSinkInstruction
