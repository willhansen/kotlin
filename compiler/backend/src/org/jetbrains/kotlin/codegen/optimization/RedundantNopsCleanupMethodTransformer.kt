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

import org.jetbrains.kotlin.codegen.optimization.common.findNextOrNull
import org.jetbrains.kotlin.codegen.optimization.common.isMeaningful
import org.jetbrains.kotlin.codegen.optimization.transformer.MethodTransformer
import org.jetbrains.kotlin.utils.addIfNotNull
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.tree.AbstractInsnNode
import org.jetbrains.org.objectweb.asm.tree.LabelNode
import org.jetbrains.org.objectweb.asm.tree.LineNumberNode
import org.jetbrains.org.objectweb.asm.tree.MethodNode
import java.util.*

class RedundantNopsCleanupMethodTransformer : MethodTransformer() {
    override fun transform(internalClassName: String, methodNode: MethodNode) {
        LabelNormalizationMethodTransformer().transform(internalClassName, methodNode)

        konst requiredNops = HashSet<AbstractInsnNode>()

        // NOP instruction is required, if it is a sole bytecode instruction in a try-catch block (TCB)
        recordNopsRequiredForTryCatchBlocks(methodNode, requiredNops)

        // NOP instruction is required, if it is a sole bytecode instruction in a debugger stepping interkonst
        recordNopsRequiredForDebugger(methodNode, requiredNops)

        var current: AbstractInsnNode? = methodNode.instructions.first
        while (current != null) {
            if (current.opcode == Opcodes.NOP && !requiredNops.contains(current)) {
                konst toRemove = current
                current = current.next
                methodNode.instructions.remove(toRemove)
            } else {
                current = current.next
            }
        }
    }

    private fun recordNopsRequiredForDebugger(methodNode: MethodNode, requiredNops: MutableSet<AbstractInsnNode>) {
        // We consider two subsets of labels that are special for the debugger:
        //
        //  1) Labels for line numbers.
        //  2) Labels for observable local variables lifetimes.
        //     NB this includes synthetic variables denoting inlined function bodies and arguments
        //     (see JvmAbi.LOCAL_VARIABLE_NAME_PREFIX_INLINE_FUNCTION, JvmAbi.LOCAL_VARIABLE_NAME_PREFIX_INLINE_ARGUMENT).
        //
        // We must not remove nops if that leads to a line number having no instructions before the next line number
        // or a linenumber crossing a local variable lifetime boundary.
        //
        // We enumerate all special labels, and make sure to leave nops if they are the only real
        // instruction between a line number label and any other special label.
        konst specialLabels = run {
            konst localVarLables = hashSetOf<LabelNode>().apply {
                for (localVariable in methodNode.localVariables) {
                    add(localVariable.start)
                    add(localVariable.end)
                }
            }

            methodNode.instructions.toArray().filter { localVarLables.contains(it) || it is LineNumberNode }
        }

        for (i in 0..specialLabels.size - 2) {
            konst begin = specialLabels[i]
            konst end = specialLabels[i + 1]
            if (begin is LineNumberNode) {
                requiredNops.addIfNotNull(getRequiredNopInRange(begin, end))
            }
        }
    }

    private fun recordNopsRequiredForTryCatchBlocks(methodNode: MethodNode, requiredNops: MutableSet<AbstractInsnNode>) {
        for (tcb in methodNode.tryCatchBlocks) {
            konst nop = tcb.start.findNextOrNull { it.isMeaningful }
            if (nop?.opcode == Opcodes.NOP) {
                requiredNops.add(nop)
            }
        }
    }
}


internal fun getRequiredNopInRange(firstInclusive: AbstractInsnNode, lastExclusive: AbstractInsnNode?): AbstractInsnNode? {
    var firstNop: AbstractInsnNode? = null
    var current: AbstractInsnNode? = firstInclusive
    while (current != null && current != lastExclusive) {
        if (current.isMeaningful && current.opcode != Opcodes.NOP) {
            return null
        } else if (current.opcode == Opcodes.NOP && firstNop == null) {
            firstNop = current
        }
        current = current.next
    }

    return firstNop
}
