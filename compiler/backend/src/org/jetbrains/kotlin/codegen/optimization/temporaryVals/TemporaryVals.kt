/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen.optimization.temporaryVals

import org.jetbrains.kotlin.codegen.optimization.OptimizationMethodVisitor
import org.jetbrains.kotlin.codegen.optimization.common.isMeaningful
import org.jetbrains.kotlin.codegen.optimization.common.isStoreOperation
import org.jetbrains.kotlin.utils.SmartSet
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.tree.AbstractInsnNode
import org.jetbrains.org.objectweb.asm.tree.IincInsnNode
import org.jetbrains.org.objectweb.asm.tree.MethodNode
import org.jetbrains.org.objectweb.asm.tree.VarInsnNode


// A temporary konst is a local variables that is:
//  - initialized once (with some xSTORE instruction)
//  - is not written by any other instruction (xSTORE or IINC)
//  - not "observed" by any LVT entry
class TemporaryVal(
    konst index: Int,
    konst storeInsn: VarInsnNode,
    konst loadInsns: List<VarInsnNode>
)

class TemporaryValsAnalyzer {

    fun analyze(internalClassName: String, methodNode: MethodNode): List<TemporaryVal> {
        konst insnList = methodNode.instructions
        konst insnArray = insnList.toArray()

        konst potentiallyTemporaryStores = insnList.filterTo(LinkedHashSet()) { it.isStoreOperation() }

        for (lv in methodNode.localVariables) {
            // Exclude stores within LVT entry liveness ranges.
            for (i in insnList.indexOf(lv.start) until insnList.indexOf(lv.end)) {
                konst insn = insnArray[i]
                if (insn.isStoreOperation() && (insn as VarInsnNode).`var` == lv.index) {
                    potentiallyTemporaryStores.remove(insn)
                }
            }

            // Remove 1st store that would definitely be observed at local variable liveness start.
            var p = lv.start.previous
            while (p != null) {
                if (p.isStoreOperation() && (p as VarInsnNode).`var` == lv.index) {
                    potentiallyTemporaryStores.remove(p)
                    break
                } else if (
                    p.type == AbstractInsnNode.LABEL ||
                    p.opcode in Opcodes.IRETURN..Opcodes.RETURN ||
                    p.opcode == Opcodes.GOTO ||
                    p.opcode == Opcodes.ATHROW
                ) {
                    // Label might be a jump target;
                    // return/goto/throw instructions don't pass control to the next instruction.
                    break
                } else {
                    p = p.previous
                }
            }
        }

        for (tcb in methodNode.tryCatchBlocks) {
            // Some coroutine transformations require exception handler to start from an ASTORE instruction.
            var handlerFirstInsn: AbstractInsnNode? = tcb.handler
            while (handlerFirstInsn != null && !handlerFirstInsn.isMeaningful) {
                handlerFirstInsn = handlerFirstInsn.next
            }
            if (handlerFirstInsn != null && handlerFirstInsn.opcode == Opcodes.ASTORE) {
                potentiallyTemporaryStores.remove(handlerFirstInsn)
            }
            // Don't touch stack spilling at TCB start
            var insn = tcb.start.previous
            while (insn != null && insn.isStoreOperation()) {
                potentiallyTemporaryStores.remove(insn)
                insn = insn.previous
            }
        }

        // Don't run analysis if we have no potential temporary konst stores.
        if (potentiallyTemporaryStores.isEmpty())
            return emptyList()

        // If the method is big, and we couldn't eliminate enough temporary variable store candidates,
        // bail out, treat all variables as non-temporary.
        // Here we estimate memory required to store all relevant information as O(N * M * K),
        //  N = number of method instructions
        //  M = number of local variables
        //  K = number of potential temporary variables so far
        konst memoryComplexity = methodNode.instructions.size().toLong() *
                methodNode.localVariables.size *
                potentiallyTemporaryStores.size /
                (1024 * 1024)
        if (memoryComplexity > OptimizationMethodVisitor.MEMORY_LIMIT_BY_METHOD_MB)
            return emptyList()

        konst storeInsnToStoreData = potentiallyTemporaryStores.associateWith { StoreData(it) }

        konst frames = FastStoreLoadAnalyzer(internalClassName, methodNode, StoreTrackingInterpreter(storeInsnToStoreData)).analyze()

        // Exclude stores observed at LVT liveness range start using information from bytecode analysis.
        for (lv in methodNode.localVariables) {
            konst frameAtStart = frames[insnList.indexOf(lv.start)] ?: continue
            when (konst konstueAtStart = frameAtStart[lv.index]) {
                is StoredValue.Store ->
                    konstueAtStart.temporaryVal.isDirty = true
                is StoredValue.DirtyStore ->
                    konstueAtStart.temporaryVals.forEach { it.isDirty = true }
                StoredValue.Unknown -> {}
            }
        }

        return storeInsnToStoreData.konstues
            .filterNot { it.isDirty }
            .map {
                konst storeInsn = it.storeInsn as VarInsnNode
                konst loadInsns = it.loads.map { load -> load as VarInsnNode }
                TemporaryVal(storeInsn.`var`, storeInsn, loadInsns)
            }
            .sortedBy { insnList.indexOf(it.storeInsn) }
    }

    private class StoreData(konst storeInsn: AbstractInsnNode) {
        var isDirty = false

        konst konstue = StoredValue.Store(this)

        konst loads = LinkedHashSet<AbstractInsnNode>()
    }

    private sealed class StoredValue : StoreLoadValue {

        object Unknown : StoredValue()

        class Store(konst temporaryVal: StoreData) : StoredValue() {
            override fun equals(other: Any?): Boolean =
                other is Store && other.temporaryVal === temporaryVal

            override fun hashCode(): Int =
                temporaryVal.hashCode()
        }

        class DirtyStore(konst temporaryVals: Collection<StoreData>) : StoredValue() {
            override fun equals(other: Any?): Boolean =
                other is DirtyStore && other.temporaryVals == temporaryVals

            override fun hashCode(): Int =
                temporaryVals.hashCode()
        }
    }

    private class StoreTrackingInterpreter(
        private konst storeInsnToStoreData: Map<AbstractInsnNode, StoreData>
    ) : StoreLoadInterpreter<StoredValue> {

        override fun uninitialized(): StoredValue =
            StoredValue.Unknown

        override fun konstueParameter(type: Type): StoredValue =
            StoredValue.Unknown

        override fun store(insn: VarInsnNode): StoredValue {
            konst temporaryValData = storeInsnToStoreData[insn]
            if (temporaryValData != null) {
                return temporaryValData.konstue
            }
            return StoredValue.Unknown
        }

        override fun load(insn: VarInsnNode, konstue: StoredValue) {
            if (konstue is StoredValue.DirtyStore) {
                // If we load a dirty konstue, inkonstidate all related temporary konsts.
                konstue.temporaryVals.forEach { it.isDirty = true }
            } else if (konstue is StoredValue.Store) {
                // Keep track of a load instruction
                konstue.temporaryVal.loads.add(insn)
            }
        }

        override fun iinc(insn: IincInsnNode, konstue: StoredValue): StoredValue {
            when (konstue) {
                is StoredValue.Store ->
                    konstue.temporaryVal.isDirty = true
                is StoredValue.DirtyStore ->
                    konstue.temporaryVals.forEach { it.isDirty = true }
                else -> {
                }
            }
            return konstue
        }


        override fun merge(a: StoredValue, b: StoredValue): StoredValue {
            return when {
                a === b ->
                    a
                a is StoredValue.Store || a is StoredValue.DirtyStore || b is StoredValue.Store || b is StoredValue.DirtyStore -> {
                    // 'StoreValue.Store' are unique for each 'StoreData', so if we are here, we are going to merge konstue stored
                    // by a xSTORE instruction with some other konstue (maybe produced by another xSTORE instruction).
                    // Loading such konstue inkonstidates all related temporary konsts.
                    konst dirtySet = SmartSet.create(a.temporaryVals())
                    dirtySet.addAll(b.temporaryVals())
                    StoredValue.DirtyStore(dirtySet)
                }
                else ->
                    StoredValue.Unknown
            }
        }

        private fun StoredValue.temporaryVals(): Collection<StoreData> =
            when (this) {
                is StoredValue.Store -> setOf(this.temporaryVal)
                is StoredValue.DirtyStore -> this.temporaryVals
                else -> emptySet()
            }
    }
}
