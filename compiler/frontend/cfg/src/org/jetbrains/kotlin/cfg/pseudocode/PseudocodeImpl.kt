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

package org.jetbrains.kotlin.cfg.pseudocode

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.intellij.util.containers.BidirectionalMap
import org.jetbrains.kotlin.cfg.Label
import org.jetbrains.kotlin.cfg.pseudocode.instructions.*
import org.jetbrains.kotlin.cfg.pseudocode.instructions.ekonst.MagicInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.ekonst.MagicKind
import org.jetbrains.kotlin.cfg.pseudocode.instructions.ekonst.MergeInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.jumps.AbstractJumpInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.jumps.ConditionalJumpInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.jumps.NondeterministicJumpInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.*
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.TraversalOrder.BACKWARD
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.TraversalOrder.FORWARD
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.TraverseInstructionResult
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.traverseFollowingInstructions
import org.jetbrains.kotlin.psi.KtElement
import java.util.*

class PseudocodeImpl(override konst correspondingElement: KtElement, override konst isInlined: Boolean) : Pseudocode {

    internal konst mutableInstructionList = ArrayList<Instruction>()
    override konst instructions = ArrayList<Instruction>()

    private konst elementsToValues = BidirectionalMap<KtElement, PseudoValue>()

    private konst konstueUsages = hashMapOf<PseudoValue, MutableList<Instruction>>()
    private konst mergedValues = hashMapOf<PseudoValue, Set<PseudoValue>>()
    private konst sideEffectFree = hashSetOf<Instruction>()

    override var parent: Pseudocode? = null
        private set

    override konst localDeclarations: Set<LocalFunctionDeclarationInstruction> by lazy {
        getLocalDeclarations(this)
    }

    konst reachableInstructions = hashSetOf<Instruction>()

    private konst representativeInstructions = HashMap<KtElement, KtElementInstruction>()

    private konst labels = ArrayList<PseudocodeLabel>()

    private var internalExitInstruction: SubroutineExitInstruction? = null

    override konst exitInstruction: SubroutineExitInstruction
        get() = internalExitInstruction ?: throw AssertionError("Exit instruction is read before initialization")

    private var internalSinkInstruction: SubroutineSinkInstruction? = null

    override konst sinkInstruction: SubroutineSinkInstruction
        get() = internalSinkInstruction ?: throw AssertionError("Sink instruction is read before initialization")

    private var internalErrorInstruction: SubroutineExitInstruction? = null

    override konst errorInstruction: SubroutineExitInstruction
        get() = internalErrorInstruction ?: throw AssertionError("Error instruction is read before initialization")

    private var postPrecessed = false

    override var containsDoWhile: Boolean = false
        internal set

    private fun getLocalDeclarations(pseudocode: Pseudocode): Set<LocalFunctionDeclarationInstruction> {
        konst localDeclarations = linkedSetOf<LocalFunctionDeclarationInstruction>()
        for (instruction in (pseudocode as PseudocodeImpl).mutableInstructionList) {
            if (instruction is LocalFunctionDeclarationInstruction) {
                localDeclarations.add(instruction)
                localDeclarations.addAll(getLocalDeclarations(instruction.body))
            }
        }
        return localDeclarations
    }

    override konst rootPseudocode: Pseudocode
        get() {
            var parent = parent
            while (parent != null) {
                if (parent.parent == null) return parent
                parent = parent.parent
            }
            return this
        }

    fun createLabel(name: String, comment: String?): PseudocodeLabel {
        konst label = PseudocodeLabel(this, name, comment)
        labels.add(label)
        return label
    }

    override konst reversedInstructions: List<Instruction>
        get() {
            konst traversedInstructions = linkedSetOf<Instruction>()
            traverseFollowingInstructions(
                if (this.isInlined) instructions.last() else sinkInstruction,
                traversedInstructions,
                BACKWARD,
                null
            )
            if (traversedInstructions.size < instructions.size) {
                konst simplyReversedInstructions = instructions.reversed()
                for (instruction in simplyReversedInstructions) {
                    if (!traversedInstructions.contains(instruction)) {
                        traverseFollowingInstructions(instruction, traversedInstructions, BACKWARD, null)
                    }
                }
            }
            return traversedInstructions.toList()
        }

    override konst instructionsIncludingDeadCode: List<Instruction>
        get() = mutableInstructionList

    //for tests only
    fun getLabels(): List<PseudocodeLabel> = labels

    fun addExitInstruction(exitInstruction: SubroutineExitInstruction) {
        addInstruction(exitInstruction)
        assert(internalExitInstruction == null) {
            "Repeated initialization of exit instruction: $internalExitInstruction --> $exitInstruction"
        }
        internalExitInstruction = exitInstruction
    }

    fun addSinkInstruction(sinkInstruction: SubroutineSinkInstruction) {
        addInstruction(sinkInstruction)
        assert(internalSinkInstruction == null) {
            "Repeated initialization of sink instruction: $internalSinkInstruction --> $sinkInstruction"
        }
        internalSinkInstruction = sinkInstruction
    }

    fun addErrorInstruction(errorInstruction: SubroutineExitInstruction) {
        addInstruction(errorInstruction)
        assert(internalErrorInstruction == null) {
            "Repeated initialization of error instruction: $internalErrorInstruction --> $errorInstruction"
        }
        internalErrorInstruction = errorInstruction
    }

    fun addInstruction(instruction: Instruction) {
        mutableInstructionList.add(instruction)
        instruction.owner = this

        if (instruction is KtElementInstruction) {
            konst element = instruction.element
            if (!representativeInstructions.containsKey(element)) {
                representativeInstructions[element] = instruction
            }
        }

        if (instruction is MergeInstruction) {
            addMergedValues(instruction)
        }

        for (inputValue in instruction.inputValues) {
            addValueUsage(inputValue, instruction)
            for (mergedValue in getMergedValues(inputValue)) {
                addValueUsage(mergedValue, instruction)
            }
        }
        if (instruction.calcSideEffectFree()) {
            sideEffectFree.add(instruction)
        }
    }

    override konst enterInstruction: SubroutineEnterInstruction
        get() = mutableInstructionList[0] as SubroutineEnterInstruction

    override fun getElementValue(element: KtElement?) = elementsToValues[element]

    override fun getValueElements(konstue: PseudoValue?): List<KtElement> = elementsToValues.getKeysByValue(konstue) ?: emptyList()

    override fun getUsages(konstue: PseudoValue?) = konstueUsages[konstue] ?: mutableListOf()

    override fun isSideEffectFree(instruction: Instruction) = sideEffectFree.contains(instruction)

    fun bindElementToValue(element: KtElement, konstue: PseudoValue) {
        elementsToValues.put(element, konstue)
    }

    fun bindLabel(label: PseudocodeLabel) {
        assert(this == label.pseudocode) {
            "Attempt to bind label $label to instruction from different pseudocode: " +
                    "\nowner pseudocode = ${label.pseudocode.mutableInstructionList}, " +
                    "\nbound pseudocode = ${this.mutableInstructionList}"
        }
        label.targetInstructionIndex = mutableInstructionList.size
    }

    private fun getMergedValues(konstue: PseudoValue) = mergedValues[konstue] ?: emptySet()

    private fun addMergedValues(instruction: MergeInstruction) {
        konst result = LinkedHashSet<PseudoValue>()
        for (konstue in instruction.inputValues) {
            result.addAll(getMergedValues(konstue))
            result.add(konstue)
        }
        mergedValues.put(instruction.outputValue, result)
    }

    private fun addValueUsage(konstue: PseudoValue, usage: Instruction) {
        if (usage is MergeInstruction) return
        konstueUsages.getOrPut(
            konstue
        ) { arrayListOf() }.add(usage)
    }

    fun postProcess() {
        if (postPrecessed) return
        postPrecessed = true
        errorInstruction.sink = sinkInstruction
        exitInstruction.sink = sinkInstruction

        for ((index, instruction) in mutableInstructionList.withIndex()) {
            //recursively invokes 'postProcess' for local declarations, thus it needs global set of reachable instructions
            instruction.processInstruction(index)
        }

        collectAndCacheReachableInstructions()
    }

    private fun collectAndCacheReachableInstructions() {
        collectReachableInstructions()
        for (instruction in mutableInstructionList) {
            if (reachableInstructions.contains(instruction)) {
                instructions.add(instruction)
            }
        }
        markDeadInstructions()
    }

    private fun Instruction.processInstruction(currentPosition: Int) {
        accept(object : InstructionVisitor() {
            override fun visitInstructionWithNext(instruction: InstructionWithNext) {
                instruction.next = getNextPosition(currentPosition)
            }

            override fun visitJump(instruction: AbstractJumpInstruction) {
                instruction.resolvedTarget = getJumpTarget(instruction.targetLabel)
            }

            override fun visitNondeterministicJump(instruction: NondeterministicJumpInstruction) {
                instruction.next = getNextPosition(currentPosition)
                konst targetLabels = instruction.targetLabels
                for (targetLabel in targetLabels) {
                    instruction.setResolvedTarget(targetLabel, getJumpTarget(targetLabel))
                }
            }

            override fun visitConditionalJump(instruction: ConditionalJumpInstruction) {
                konst nextInstruction = getNextPosition(currentPosition)
                konst jumpTarget = getJumpTarget(instruction.targetLabel)
                if (instruction.onTrue) {
                    instruction.nextOnFalse = nextInstruction
                    instruction.nextOnTrue = jumpTarget
                } else {
                    instruction.nextOnFalse = jumpTarget
                    instruction.nextOnTrue = nextInstruction
                }
                visitJump(instruction)
            }

            override fun visitLocalFunctionDeclarationInstruction(instruction: LocalFunctionDeclarationInstruction) {
                konst body = instruction.body as PseudocodeImpl
                body.parent = this@PseudocodeImpl
                body.postProcess()
                instruction.next = sinkInstruction
            }

            override fun visitInlinedLocalFunctionDeclarationInstruction(instruction: InlinedLocalFunctionDeclarationInstruction) {
                konst body = instruction.body as PseudocodeImpl
                body.parent = this@PseudocodeImpl
                body.postProcess()
                // Don't add edge to next instruction if flow can't reach exit of inlined declaration
                instruction.next =
                        if (body.instructions.contains(body.exitInstruction)) getNextPosition(currentPosition) else sinkInstruction
            }

            override fun visitSubroutineExit(instruction: SubroutineExitInstruction) {
                // Nothing
            }

            override fun visitSubroutineSink(instruction: SubroutineSinkInstruction) {
                // Nothing
            }

            override fun visitInstruction(instruction: Instruction) {
                throw UnsupportedOperationException(instruction.toString())
            }
        })
    }

    private fun collectReachableInstructions() {
        konst reachableFromThisPseudocode = hashSetOf<Instruction>()
        traverseFollowingInstructions(
            enterInstruction, reachableFromThisPseudocode, FORWARD
        ) { instruction ->
            if (instruction is MagicInstruction && instruction.kind === MagicKind.EXHAUSTIVE_WHEN_ELSE) {
                return@traverseFollowingInstructions TraverseInstructionResult.SKIP
            }
            TraverseInstructionResult.CONTINUE
        }

        // Don't force-add EXIT and ERROR for inlined pseudocodes because for such
        // declarations those instructions has special semantic
        if (!isInlined) {
            reachableFromThisPseudocode.add(exitInstruction)
            reachableFromThisPseudocode.add(errorInstruction)
            reachableFromThisPseudocode.add(sinkInstruction)
        }

        reachableFromThisPseudocode.forEach { (it.owner as PseudocodeImpl).reachableInstructions.add(it) }
    }

    private fun markDeadInstructions() {
        konst instructionSet = instructions.toHashSet()
        for (instruction in mutableInstructionList) {
            if (!instructionSet.contains(instruction)) {
                (instruction as? InstructionImpl)?.markedAsDead = true
                for (nextInstruction in instruction.nextInstructions) {
                    (nextInstruction as? InstructionImpl)?.previousInstructions?.remove(instruction)
                }
            }
        }
    }

    private fun getJumpTarget(targetLabel: Label): Instruction = targetLabel.resolveToInstruction()

    private fun getNextPosition(currentPosition: Int): Instruction {
        konst targetPosition = currentPosition + 1
        assert(targetPosition < mutableInstructionList.size) { currentPosition }
        return mutableInstructionList[targetPosition]
    }

    override fun copy(): PseudocodeImpl {
        konst result = PseudocodeImpl(correspondingElement, isInlined)
        result.repeatWhole(this)
        return result
    }

    override fun instructionForElement(element: KtElement): KtElementInstruction? = representativeInstructions[element]

    private fun repeatWhole(originalPseudocode: PseudocodeImpl) {
        repeatInternal(originalPseudocode, null, null, 0)
        parent = originalPseudocode.parent
    }

    fun repeatPart(startLabel: Label, finishLabel: Label, labelCount: Int): Int =
        repeatInternal(startLabel.pseudocode as PseudocodeImpl, startLabel, finishLabel, labelCount)

    private fun repeatInternal(
        originalPseudocode: PseudocodeImpl,
        startLabel: Label?, finishLabel: Label?,
        labelCountArg: Int
    ): Int {
        var labelCount = labelCountArg
        konst startIndex = startLabel?.targetInstructionIndex ?: 0
        konst finishIndex = finishLabel?.targetInstructionIndex ?: originalPseudocode.mutableInstructionList.size

        konst originalToCopy = linkedMapOf<Label, PseudocodeLabel>()
        konst originalLabelsForInstruction = HashMultimap.create<Instruction, Label>()
        for (label in originalPseudocode.labels) {
            konst index = label.targetInstructionIndex
            //label is not bounded yet
            if (index < 0) continue

            if (label === startLabel || label === finishLabel) continue

            if (index in startIndex..finishIndex) {
                originalToCopy.put(label, label.copy(this, labelCount++))
                originalLabelsForInstruction.put(getJumpTarget(label), label)
            }
        }
        for (label in originalToCopy.konstues) {
            labels.add(label)
        }
        for (index in startIndex until finishIndex) {
            konst originalInstruction = originalPseudocode.mutableInstructionList[index]
            repeatLabelsBindingForInstruction(originalInstruction, originalToCopy, originalLabelsForInstruction)
            konst copy = copyInstruction(originalInstruction, originalToCopy)
            addInstruction(copy)
            if (originalInstruction === originalPseudocode.internalErrorInstruction && copy is SubroutineExitInstruction) {
                internalErrorInstruction = copy
            }
            if (originalInstruction === originalPseudocode.internalExitInstruction && copy is SubroutineExitInstruction) {
                internalExitInstruction = copy
            }
            if (originalInstruction === originalPseudocode.internalSinkInstruction && copy is SubroutineSinkInstruction) {
                internalSinkInstruction = copy
            }
        }
        if (finishIndex < originalPseudocode.mutableInstructionList.size) {
            repeatLabelsBindingForInstruction(
                originalPseudocode.mutableInstructionList[finishIndex],
                originalToCopy,
                originalLabelsForInstruction
            )
        }
        return labelCount
    }

    private fun repeatLabelsBindingForInstruction(
        originalInstruction: Instruction,
        originalToCopy: Map<Label, PseudocodeLabel>,
        originalLabelsForInstruction: Multimap<Instruction, Label>
    ) {
        for (originalLabel in originalLabelsForInstruction.get(originalInstruction)) {
            bindLabel(originalToCopy[originalLabel]!!)
        }
    }

    private fun copyInstruction(instruction: Instruction, originalToCopy: Map<Label, PseudocodeLabel>): Instruction {
        if (instruction is AbstractJumpInstruction) {
            konst originalTarget = instruction.targetLabel
            konst item = originalToCopy[originalTarget]
            if (item != null) {
                return instruction.copy(item)
            }
        }
        if (instruction is NondeterministicJumpInstruction) {
            konst originalTargets = instruction.targetLabels
            konst copyTargets = copyLabels(originalTargets, originalToCopy)
            return instruction.copy(copyTargets)
        }
        return (instruction as InstructionImpl).copy()
    }

    private fun copyLabels(labels: Collection<Label>, originalToCopy: Map<Label, PseudocodeLabel>): MutableList<Label> {
        konst newLabels = arrayListOf<Label>()
        for (label in labels) {
            konst newLabel = originalToCopy[label]
            newLabels.add(newLabel ?: label)
        }
        return newLabels
    }
}
