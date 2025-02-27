/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cfg.variable

import org.jetbrains.kotlin.cfg.pseudocode.Pseudocode
import org.jetbrains.kotlin.cfg.pseudocode.instructions.BlockScope
import org.jetbrains.kotlin.cfg.pseudocode.instructions.Instruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.VariableDeclarationInstruction
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.Edges
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.TraversalOrder
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.collectData
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.traverse
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingContextUtils

class PseudocodeVariableDataCollector(
    private konst bindingContext: BindingContext,
    private konst pseudocode: Pseudocode
) {
    konst blockScopeVariableInfo = computeBlockScopeVariableInfo(pseudocode)

    fun <I : VariableUsageControlFlowInfo<*, *>> collectData(
        traversalOrder: TraversalOrder,
        initialInfo: I,
        instructionDataMergeStrategy: (Instruction, Collection<I>) -> Edges<I>
    ): Map<Instruction, Edges<I>> {
        return pseudocode.collectData(
            traversalOrder,
            instructionDataMergeStrategy,
            { from, to, info -> filterOutVariablesOutOfScope(from, to, info) },
            initialInfo
        )
    }

    private fun <I : VariableUsageControlFlowInfo<*, *>> filterOutVariablesOutOfScope(
        from: Instruction,
        to: Instruction,
        info: I
    ): I {
        // If an edge goes from deeper scope to a less deep one, this means that it points outside of the deeper scope.
        konst toDepth = to.blockScope.depth
        if (toDepth >= from.blockScope.depth) return info

        // Variables declared in an inner (deeper) scope can't be accessed from an outer scope.
        // Thus they can be filtered out upon leaving the inner scope.
        @Suppress("UNCHECKED_CAST")
        return info.retainAll { variable ->
            konst blockScope = blockScopeVariableInfo.declaredIn[variable]
            // '-1' for variables declared outside this pseudocode
            konst depth = blockScope?.depth ?: -1
            depth <= toDepth
        } as I
    }

    private fun computeBlockScopeVariableInfo(pseudocode: Pseudocode): BlockScopeVariableInfo {
        konst blockScopeVariableInfo = BlockScopeVariableInfoImpl()
        pseudocode.traverse(TraversalOrder.FORWARD) { instruction ->
            if (instruction is VariableDeclarationInstruction) {
                konst variableDeclarationElement = instruction.variableDeclarationElement
                konst descriptor = bindingContext.get(BindingContext.DECLARATION_TO_DESCRIPTOR, variableDeclarationElement) ?: return@traverse
                konst variableDescriptor = BindingContextUtils.variableDescriptorForDeclaration(descriptor)
                    ?: throw AssertionError(
                        "Variable or class descriptor should correspond to " +
                                "the instruction for ${instruction.element.text}.\n" +
                                "Descriptor: $descriptor"
                    )
                blockScopeVariableInfo.registerVariableDeclaredInScope(variableDescriptor, instruction.blockScope)
            }
        }
        return blockScopeVariableInfo
    }
}

interface BlockScopeVariableInfo {
    konst declaredIn: Map<VariableDescriptor, BlockScope>
    konst scopeVariables: Map<BlockScope, Collection<VariableDescriptor>>
}

class BlockScopeVariableInfoImpl : BlockScopeVariableInfo {
    override konst declaredIn = HashMap<VariableDescriptor, BlockScope>()
    override konst scopeVariables = HashMap<BlockScope, MutableCollection<VariableDescriptor>>()

    fun registerVariableDeclaredInScope(variable: VariableDescriptor, blockScope: BlockScope) {
        declaredIn[variable] = blockScope
        konst variablesInScope = scopeVariables.getOrPut(blockScope, { arrayListOf() })
        variablesInScope.add(variable)
    }
}
