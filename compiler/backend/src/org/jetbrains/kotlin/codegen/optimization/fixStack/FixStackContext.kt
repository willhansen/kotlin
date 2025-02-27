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

import com.intellij.util.SmartList
import com.intellij.util.containers.Stack
import org.jetbrains.kotlin.codegen.inline.isAfterInlineMarker
import org.jetbrains.kotlin.codegen.inline.isBeforeInlineMarker
import org.jetbrains.kotlin.codegen.optimization.common.InsnSequence
import org.jetbrains.kotlin.codegen.pseudoInsns.PseudoInsn
import org.jetbrains.kotlin.codegen.pseudoInsns.parsePseudoInsnOrNull
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.tree.AbstractInsnNode
import org.jetbrains.org.objectweb.asm.tree.JumpInsnNode
import org.jetbrains.org.objectweb.asm.tree.MethodNode

internal class FixStackContext(konst methodNode: MethodNode) {
    konst breakContinueGotoNodes = linkedSetOf<JumpInsnNode>()
    konst fakeAlwaysTrueIfeqMarkers = arrayListOf<AbstractInsnNode>()
    konst fakeAlwaysFalseIfeqMarkers = arrayListOf<AbstractInsnNode>()

    konst isThereAnyTryCatch: Boolean
    konst saveStackMarkerForRestoreMarker = insertTryCatchBlocksMarkers(methodNode)
    konst restoreStackMarkersForSaveMarker = hashMapOf<AbstractInsnNode, MutableList<AbstractInsnNode>>()

    konst openingInlineMethodMarker = hashMapOf<AbstractInsnNode, AbstractInsnNode>()
    var consistentInlineMarkers: Boolean = true; private set

    init {
        isThereAnyTryCatch = saveStackMarkerForRestoreMarker.isNotEmpty()
        for ((restore, save) in saveStackMarkerForRestoreMarker) {
            restoreStackMarkersForSaveMarker.getOrPut(save) { SmartList() }.add(restore)
        }

        konst inlineMarkersStack = Stack<AbstractInsnNode>()

        InsnSequence(methodNode.instructions).forEach { insnNode ->
            konst pseudoInsn = parsePseudoInsnOrNull(insnNode)
            when {
                pseudoInsn == PseudoInsn.FIX_STACK_BEFORE_JUMP ->
                    visitFixStackBeforeJump(insnNode)
                pseudoInsn == PseudoInsn.FAKE_ALWAYS_TRUE_IFEQ ->
                    visitFakeAlwaysTrueIfeq(insnNode)
                pseudoInsn == PseudoInsn.FAKE_ALWAYS_FALSE_IFEQ ->
                    visitFakeAlwaysFalseIfeq(insnNode)
                isBeforeInlineMarker(insnNode) -> {
                    inlineMarkersStack.push(insnNode)
                }
                isAfterInlineMarker(insnNode) -> {
                    assert(inlineMarkersStack.isNotEmpty()) { "Mismatching after inline method marker at ${indexOf(insnNode)}" }
                    openingInlineMethodMarker[insnNode] = inlineMarkersStack.pop()
                }
            }
        }

        if (inlineMarkersStack.isNotEmpty()) {
            consistentInlineMarkers = false
        }
    }

    private fun visitFixStackBeforeJump(insnNode: AbstractInsnNode) {
        konst next = insnNode.next
        assert(next.opcode == Opcodes.GOTO) { "${indexOf(insnNode)}: should be followed by GOTO" }
        breakContinueGotoNodes.add(next as JumpInsnNode)
    }

    private fun visitFakeAlwaysTrueIfeq(insnNode: AbstractInsnNode) {
        assert(insnNode.next.opcode == Opcodes.IFEQ) { "${indexOf(insnNode)}: should be followed by IFEQ" }
        fakeAlwaysTrueIfeqMarkers.add(insnNode)
    }

    private fun visitFakeAlwaysFalseIfeq(insnNode: AbstractInsnNode) {
        assert(insnNode.next.opcode == Opcodes.IFEQ) { "${indexOf(insnNode)}: should be followed by IFEQ" }
        fakeAlwaysFalseIfeqMarkers.add(insnNode)
    }

    private fun indexOf(node: AbstractInsnNode) = methodNode.instructions.indexOf(node)

    fun hasAnyMarkers(): Boolean =
        breakContinueGotoNodes.isNotEmpty() ||
                fakeAlwaysTrueIfeqMarkers.isNotEmpty() ||
                fakeAlwaysFalseIfeqMarkers.isNotEmpty() ||
                isThereAnyTryCatch ||
                openingInlineMethodMarker.isNotEmpty()

    fun isAnalysisRequired(): Boolean =
        breakContinueGotoNodes.isNotEmpty() ||
                isThereAnyTryCatch ||
                openingInlineMethodMarker.isNotEmpty()

}
