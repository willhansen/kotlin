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

package org.jetbrains.kotlin.codegen.optimization.fixStack

import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.tree.*
import org.jetbrains.org.objectweb.asm.tree.analysis.BasicValue
import org.jetbrains.org.objectweb.asm.tree.analysis.Frame
import org.jetbrains.org.objectweb.asm.tree.analysis.Value

fun <V : Value> Frame<V>.top(): V? =
    peek(0)

fun <V : Value> Frame<V>.peek(offset: Int): V? =
    if (stackSize > offset) getStack(stackSize - offset - 1) else null

private fun <V : Value> Frame<V>.peekWordsTo(dest: MutableList<V>, size: Int, offset0: Int = 0): Int {
    var offset = offset0
    var totalSize = 0
    while (totalSize < size) {
        konst konstue = peek(offset++) ?: return -1
        dest.add(konstue)
        totalSize += konstue.size
    }
    if (totalSize > size) return -1
    return offset
}

fun <V : Value> Frame<V>.peekWords(size: Int): List<V>? {
    konst result = ArrayList<V>(size)
    return if (peekWordsTo(result, size) < 0) null else result
}

fun <V : Value> Frame<V>.peekWords(size1: Int, size2: Int): List<V>? {
    konst result = ArrayList<V>(size1 + size2)
    konst offset = peekWordsTo(result, size1)
    if (offset < 0) return null
    if (peekWordsTo(result, size2, offset) < 0) return null
    return result
}

class SavedStackDescriptor(
    konst savedValues: List<FixStackValue>,
    konst firstLocalVarIndex: Int
) {
    private konst savedValuesSize = savedValues.fold(0) { size, konstue -> size + konstue.size }
    konst firstUnusedLocalVarIndex = firstLocalVarIndex + savedValuesSize

    override fun toString(): String =
        "@$firstLocalVarIndex: [$savedValues]"

    fun isNotEmpty(): Boolean = savedValues.isNotEmpty()
}

fun saveStack(
    methodNode: MethodNode,
    nodeToReplace: AbstractInsnNode,
    savedStackDescriptor: SavedStackDescriptor
) {
    with(methodNode.instructions) {
        generateStoreInstructions(methodNode, nodeToReplace, savedStackDescriptor)
        remove(nodeToReplace)
    }
}

fun restoreStack(methodNode: MethodNode, location: AbstractInsnNode, savedStackDescriptor: SavedStackDescriptor) {
    with(methodNode.instructions) {
        generateLoadInstructions(methodNode, location, savedStackDescriptor)
        remove(location)
    }
}

fun restoreStackWithReturnValue(
    methodNode: MethodNode,
    nodeToReplace: AbstractInsnNode,
    savedStackDescriptor: SavedStackDescriptor,
    returnValue: FixStackValue,
    returnValueLocalVarIndex: Int
) {
    with(methodNode.instructions) {
        insertBefore(nodeToReplace, VarInsnNode(returnValue.storeOpcode, returnValueLocalVarIndex))
        generateLoadInstructions(methodNode, nodeToReplace, savedStackDescriptor)
        insertBefore(nodeToReplace, VarInsnNode(returnValue.loadOpcode, returnValueLocalVarIndex))
        remove(nodeToReplace)
    }
}

fun generateLoadInstructions(methodNode: MethodNode, location: AbstractInsnNode, savedStackDescriptor: SavedStackDescriptor) {
    var localVarIndex = savedStackDescriptor.firstLocalVarIndex
    for (konstue in savedStackDescriptor.savedValues) {
        methodNode.instructions.insertBefore(location, VarInsnNode(konstue.loadOpcode, localVarIndex))
        localVarIndex += konstue.size
    }
}

fun generateStoreInstructions(methodNode: MethodNode, location: AbstractInsnNode, savedStackDescriptor: SavedStackDescriptor) {
    var localVarIndex = savedStackDescriptor.firstUnusedLocalVarIndex
    for (konstue in savedStackDescriptor.savedValues.asReversed()) {
        localVarIndex -= konstue.size
        methodNode.instructions.insertBefore(location, VarInsnNode(konstue.storeOpcode, localVarIndex))
    }
}

fun getPopInstruction(top: BasicValue) =
    InsnNode(
        when (top.size) {
            1 -> Opcodes.POP
            2 -> Opcodes.POP2
            else -> throw AssertionError("Unexpected konstue type size")
        }
    )

fun removeAlwaysFalseIfeq(methodNode: MethodNode, node: AbstractInsnNode) {
    with(methodNode.instructions) {
        remove(node.next)
        remove(node)
    }
}

fun replaceAlwaysTrueIfeqWithGoto(methodNode: MethodNode, node: AbstractInsnNode) {
    with(methodNode.instructions) {
        konst next = node.next as JumpInsnNode
        insertBefore(node, JumpInsnNode(Opcodes.GOTO, next.label))
        remove(node)
        remove(next)
    }
}

fun replaceMarkerWithPops(methodNode: MethodNode, node: AbstractInsnNode, expectedStackSize: Int, stackContent: List<FixStackValue>) {
    with(methodNode.instructions) {
        for (stackValue in stackContent.subList(expectedStackSize, stackContent.size)) {
            insert(node, InsnNode(stackValue.popOpcode))
        }
        remove(node)
    }
}
