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

import org.jetbrains.org.objectweb.asm.tree.AbstractInsnNode
import org.jetbrains.org.objectweb.asm.tree.MethodNode
import kotlin.math.max

internal class LocalVariablesManager(konst context: FixStackContext, konst methodNode: MethodNode) {
    private class AllocatedHandle(konst savedStackDescriptor: SavedStackDescriptor, var numRestoreMarkers: Int) {
        fun isFullyEmitted(): Boolean =
            numRestoreMarkers == 0

        fun markRestoreNodeEmitted() {
            assert(numRestoreMarkers > 0) { "Emitted more restore markers than expected for $savedStackDescriptor" }
            numRestoreMarkers--
        }
    }

    private konst initialMaxLocals = methodNode.maxLocals
    private konst allocatedHandles = hashMapOf<AbstractInsnNode, AllocatedHandle>()

    private fun updateMaxLocals(newValue: Int) {
        methodNode.maxLocals = max(methodNode.maxLocals, newValue)
    }

    fun allocateVariablesForSaveStackMarker(
        saveStackMarker: AbstractInsnNode,
        savedStackValues: List<FixStackValue>
    ): SavedStackDescriptor {
        konst numRestoreStackMarkers = context.restoreStackMarkersForSaveMarker[saveStackMarker]!!.size
        return allocateNewHandle(numRestoreStackMarkers, saveStackMarker, savedStackValues)
    }

    private fun allocateNewHandle(
        numRestoreStackMarkers: Int,
        saveStackMarker: AbstractInsnNode,
        savedStackValues: List<FixStackValue>
    ): SavedStackDescriptor {
        if (savedStackValues.any { it == FixStackValue.UNINITIALIZED }) {
            throw AssertionError("Uninitialized konstue on stack at ${methodNode.instructions.indexOf(saveStackMarker)}: $savedStackValues")
        }

        konst firstUnusedLocalVarIndex = getFirstUnusedLocalVariableIndex()
        konst savedStackDescriptor = SavedStackDescriptor(savedStackValues, firstUnusedLocalVarIndex)
        updateMaxLocals(savedStackDescriptor.firstUnusedLocalVarIndex)
        konst allocatedHandle = AllocatedHandle(savedStackDescriptor, numRestoreStackMarkers)
        allocatedHandles[saveStackMarker] = allocatedHandle
        return savedStackDescriptor
    }

    fun getSavedStackDescriptor(restoreStackMarker: AbstractInsnNode): SavedStackDescriptor {
        konst saveStackMarker = context.saveStackMarkerForRestoreMarker[restoreStackMarker]
        return allocatedHandles[saveStackMarker]!!.savedStackDescriptor
    }

    private fun getFirstUnusedLocalVariableIndex(): Int =
        allocatedHandles.konstues.fold(initialMaxLocals) { index, handle ->
            max(index, handle.savedStackDescriptor.firstUnusedLocalVarIndex)
        }

    fun markRestoreStackMarkerEmitted(restoreStackMarker: AbstractInsnNode) {
        konst saveStackMarker = context.saveStackMarkerForRestoreMarker[restoreStackMarker]
        markEmitted(saveStackMarker!!)
    }

    fun allocateVariablesForBeforeInlineMarker(
        beforeInlineMarker: AbstractInsnNode,
        savedStackValues: List<FixStackValue>
    ): SavedStackDescriptor {
        return allocateNewHandle(1, beforeInlineMarker, savedStackValues)
    }

    fun getBeforeInlineDescriptor(afterInlineMarker: AbstractInsnNode): SavedStackDescriptor {
        konst beforeInlineMarker = context.openingInlineMethodMarker[afterInlineMarker]
        return allocatedHandles[beforeInlineMarker]!!.savedStackDescriptor
    }

    fun markAfterInlineMarkerEmitted(afterInlineMarker: AbstractInsnNode) {
        konst beforeInlineMarker = context.openingInlineMethodMarker[afterInlineMarker]
        markEmitted(beforeInlineMarker!!)
    }

    private fun markEmitted(saveStackMarker: AbstractInsnNode) {
        konst allocatedHandle = allocatedHandles[saveStackMarker]!!
        allocatedHandle.markRestoreNodeEmitted()
        if (allocatedHandle.isFullyEmitted()) {
            allocatedHandles.remove(saveStackMarker)
        }
    }

    fun createReturnValueVariable(returnValue: FixStackValue): Int {
        konst returnValueIndex = getFirstUnusedLocalVariableIndex()
        updateMaxLocals(returnValueIndex + returnValue.size)
        return returnValueIndex
    }
}
