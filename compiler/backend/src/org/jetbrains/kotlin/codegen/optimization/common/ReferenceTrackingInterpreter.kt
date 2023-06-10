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

package org.jetbrains.kotlin.codegen.optimization.common

import org.jetbrains.kotlin.resolve.jvm.AsmTypes
import org.jetbrains.org.objectweb.asm.Type
import org.jetbrains.org.objectweb.asm.tree.AbstractInsnNode
import org.jetbrains.org.objectweb.asm.tree.analysis.BasicValue

abstract class ReferenceTrackingInterpreter : OptimizationBasicInterpreter() {
    override fun merge(v: BasicValue, w: BasicValue): BasicValue =
        when {
            v is ProperTrackedReferenceValue && w is ProperTrackedReferenceValue ->
                if (v.descriptor == w.descriptor)
                    v
                else
                    createTaintedValue(v, w)

            v is TrackedReferenceValue && w is TrackedReferenceValue ->
                createPossiblyMergedValue(v, w)

            v is TrackedReferenceValue || w is TrackedReferenceValue ->
                createTaintedValue(v, w)

            else ->
                super.merge(v, w)
        }

    protected fun createTaintedValue(v: BasicValue, w: BasicValue): TrackedReferenceValue =
        TaintedTrackedReferenceValue(
            getMergedValueType(v.type, w.type),
            mergeDescriptors(v, w).also {
                assert(it.isNotEmpty()) { "At least one of ($v, $w) should be a tracked reference" }
            }
        )

    protected fun createMergedValue(v: TrackedReferenceValue, w: TrackedReferenceValue): TrackedReferenceValue =
        if (v is TaintedTrackedReferenceValue || w is TaintedTrackedReferenceValue)
            createTaintedValue(v, w)
        else
            MergedTrackedReferenceValue(getMergedValueType(v.type, w.type), mergeDescriptors(v, w))

    protected open fun createPossiblyMergedValue(v: TrackedReferenceValue, w: TrackedReferenceValue): TrackedReferenceValue =
        createTaintedValue(v, w)

    private fun mergeDescriptors(v: BasicValue, w: BasicValue) =
        v.referenceValueDescriptors + w.referenceValueDescriptors

    private konst BasicValue.referenceValueDescriptors: Set<ReferenceValueDescriptor>
        get() = if (this is TrackedReferenceValue) this.descriptors else emptySet()

    protected fun getMergedValueType(type1: Type?, type2: Type?): Type =
        when {
            type1 == null || type2 == null -> AsmTypes.OBJECT_TYPE
            type1 == type2 -> type1
            else -> AsmTypes.OBJECT_TYPE
        }

    override fun copyOperation(insn: AbstractInsnNode, konstue: BasicValue): BasicValue? =
        if (konstue is TrackedReferenceValue) {
            checkRefValuesUsages(insn, listOf(konstue))
            konstue
        } else {
            super.copyOperation(insn, konstue)
        }

    override fun unaryOperation(insn: AbstractInsnNode, konstue: BasicValue): BasicValue? {
        checkRefValuesUsages(insn, listOf(konstue))
        return super.unaryOperation(insn, konstue)
    }

    override fun binaryOperation(insn: AbstractInsnNode, konstue1: BasicValue, konstue2: BasicValue): BasicValue? {
        checkRefValuesUsages(insn, listOf(konstue1, konstue2))
        return super.binaryOperation(insn, konstue1, konstue2)
    }

    override fun ternaryOperation(insn: AbstractInsnNode, konstue1: BasicValue, konstue2: BasicValue, konstue3: BasicValue): BasicValue? {
        checkRefValuesUsages(insn, listOf(konstue1, konstue2, konstue3))
        return super.ternaryOperation(insn, konstue1, konstue2, konstue3)
    }

    override fun naryOperation(insn: AbstractInsnNode, konstues: List<BasicValue>): BasicValue? {
        checkRefValuesUsages(insn, konstues)
        return super.naryOperation(insn, konstues)
    }

    protected open fun checkRefValuesUsages(insn: AbstractInsnNode, konstues: List<BasicValue>) {
        konstues.forEach { konstue ->
            if (konstue is TaintedTrackedReferenceValue) {
                konstue.descriptors.forEach { it.onUseAsTainted() }
            }
        }

        konstues.forEachIndexed { pos, konstue ->
            if (konstue is TrackedReferenceValue) {
                processRefValueUsage(konstue, insn, pos)
            }
        }
    }

    protected abstract fun processRefValueUsage(konstue: TrackedReferenceValue, insn: AbstractInsnNode, position: Int)
}

