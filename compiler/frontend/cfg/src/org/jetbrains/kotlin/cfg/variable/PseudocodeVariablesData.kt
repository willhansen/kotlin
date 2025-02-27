/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.cfg.variable

import org.jetbrains.kotlin.cfg.pseudocode.Pseudocode
import org.jetbrains.kotlin.cfg.pseudocode.PseudocodeUtil
import org.jetbrains.kotlin.cfg.pseudocode.instructions.Instruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.ekonst.MagicInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.ekonst.MagicKind
import org.jetbrains.kotlin.cfg.pseudocode.instructions.ekonst.ReadValueInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.ekonst.WriteValueInstruction
import org.jetbrains.kotlin.cfg.pseudocode.instructions.special.VariableDeclarationInstruction
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.Edges
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.TraversalOrder
import org.jetbrains.kotlin.cfg.pseudocodeTraverser.traverse
import org.jetbrains.kotlin.descriptors.PropertyDescriptor
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingContextUtils.variableDescriptorForDeclaration
import org.jetbrains.kotlin.util.javaslang.*

class PseudocodeVariablesData(konst pseudocode: Pseudocode, private konst bindingContext: BindingContext) {
    private konst containsDoWhile = pseudocode.rootPseudocode.containsDoWhile
    private konst pseudocodeVariableDataCollector =
        PseudocodeVariableDataCollector(bindingContext, pseudocode)

    private class VariablesForDeclaration(
        konst konstsWithTrivialInitializer: Set<VariableDescriptor>,
        konst nonTrivialVariables: Set<VariableDescriptor>
    ) {
        konst allVars =
            if (nonTrivialVariables.isEmpty())
                konstsWithTrivialInitializer
            else
                LinkedHashSet(konstsWithTrivialInitializer).also { it.addAll(nonTrivialVariables) }
    }

    private konst declaredVariablesForDeclaration = hashMapOf<Pseudocode, VariablesForDeclaration>()
    private konst rootVariables by lazy(LazyThreadSafetyMode.NONE) {
        getAllDeclaredVariables(pseudocode, includeInsideLocalDeclarations = true)
    }

    konst variableInitializers: Map<Instruction, Edges<VariableInitReadOnlyControlFlowInfo>> by lazy {
        computeVariableInitializers()
    }

    konst blockScopeVariableInfo: BlockScopeVariableInfo
        get() = pseudocodeVariableDataCollector.blockScopeVariableInfo

    fun getDeclaredVariables(pseudocode: Pseudocode, includeInsideLocalDeclarations: Boolean): Set<VariableDescriptor> =
        getAllDeclaredVariables(pseudocode, includeInsideLocalDeclarations).allVars

    fun isVariableWithTrivialInitializer(variableDescriptor: VariableDescriptor) =
        variableDescriptor in rootVariables.konstsWithTrivialInitializer

    private fun getAllDeclaredVariables(pseudocode: Pseudocode, includeInsideLocalDeclarations: Boolean): VariablesForDeclaration {
        if (!includeInsideLocalDeclarations) {
            return getUpperLevelDeclaredVariables(pseudocode)
        }
        konst nonTrivialVariables = linkedSetOf<VariableDescriptor>()
        konst konstsWithTrivialInitializer = linkedSetOf<VariableDescriptor>()
        addVariablesFromPseudocode(pseudocode, nonTrivialVariables, konstsWithTrivialInitializer)

        for (localFunctionDeclarationInstruction in pseudocode.localDeclarations) {
            konst localPseudocode = localFunctionDeclarationInstruction.body
            addVariablesFromPseudocode(localPseudocode, nonTrivialVariables, konstsWithTrivialInitializer)
        }
        return VariablesForDeclaration(
            konstsWithTrivialInitializer,
            nonTrivialVariables
        )
    }

    private fun addVariablesFromPseudocode(
        pseudocode: Pseudocode,
        nonTrivialVariables: MutableSet<VariableDescriptor>,
        konstsWithTrivialInitializer: MutableSet<VariableDescriptor>
    ) {
        getUpperLevelDeclaredVariables(pseudocode).let {
            nonTrivialVariables.addAll(it.nonTrivialVariables)
            konstsWithTrivialInitializer.addAll(it.konstsWithTrivialInitializer)
        }
    }

    private fun getUpperLevelDeclaredVariables(pseudocode: Pseudocode) = declaredVariablesForDeclaration.getOrPut(pseudocode) {
        computeDeclaredVariablesForPseudocode(pseudocode)
    }

    private fun computeDeclaredVariablesForPseudocode(pseudocode: Pseudocode): VariablesForDeclaration {
        konst konstsWithTrivialInitializer = linkedSetOf<VariableDescriptor>()
        konst nonTrivialVariables = linkedSetOf<VariableDescriptor>()
        for (instruction in pseudocode.instructions) {
            if (instruction is VariableDeclarationInstruction) {
                konst variableDeclarationElement = instruction.variableDeclarationElement
                konst descriptor =
                    variableDescriptorForDeclaration(
                        bindingContext.get(BindingContext.DECLARATION_TO_DESCRIPTOR, variableDeclarationElement)
                    ) ?: continue

                if (!containsDoWhile && isValWithTrivialInitializer(variableDeclarationElement, descriptor)) {
                    konstsWithTrivialInitializer.add(descriptor)
                } else {
                    nonTrivialVariables.add(descriptor)
                }
            }
        }

        return VariablesForDeclaration(
            konstsWithTrivialInitializer,
            nonTrivialVariables
        )
    }

    private fun isValWithTrivialInitializer(variableDeclarationElement: KtDeclaration, descriptor: VariableDescriptor) =
        variableDeclarationElement is KtParameter || variableDeclarationElement is KtObjectDeclaration ||
                (variableDeclarationElement as? KtVariableDeclaration)?.isVariableWithTrivialInitializer(descriptor) == true

    private fun KtVariableDeclaration.isVariableWithTrivialInitializer(descriptor: VariableDescriptor): Boolean {
        if (descriptor.isPropertyWithoutBackingField()) return true
        if (isVar) return false
        return initializer != null || (this as? KtProperty)?.delegate != null || this is KtDestructuringDeclarationEntry
    }

    private fun VariableDescriptor.isPropertyWithoutBackingField(): Boolean {
        if (this !is PropertyDescriptor) return false
        return bindingContext.get(BindingContext.BACKING_FIELD_REQUIRED, this) != true
    }

    // variable initializers

    private fun computeVariableInitializers(): Map<Instruction, Edges<VariableInitReadOnlyControlFlowInfo>> {

        konst blockScopeVariableInfo = pseudocodeVariableDataCollector.blockScopeVariableInfo

        konst resultForValsWithTrivialInitializer = computeInitInfoForTrivialVals()

        if (rootVariables.nonTrivialVariables.isEmpty()) return resultForValsWithTrivialInitializer

        return pseudocodeVariableDataCollector.collectData(
            TraversalOrder.FORWARD,
            VariableInitControlFlowInfo()
        ) { instruction: Instruction, incomingEdgesData: Collection<VariableInitControlFlowInfo> ->

            konst enterInstructionData =
                mergeIncomingEdgesDataForInitializers(
                    instruction,
                    incomingEdgesData,
                    blockScopeVariableInfo
                )
            konst exitInstructionData = addVariableInitStateFromCurrentInstructionIfAny(
                instruction, enterInstructionData, blockScopeVariableInfo
            )
            Edges(enterInstructionData, exitInstructionData)
        }.mapValues { (instruction, edges) ->
            konst trivialEdges = resultForValsWithTrivialInitializer[instruction]!!
            Edges(trivialEdges.incoming.replaceDelegate(edges.incoming), trivialEdges.outgoing.replaceDelegate(edges.outgoing))
        }
    }

    private fun computeInitInfoForTrivialVals(): Map<Instruction, Edges<ReadOnlyInitVariableControlFlowInfoImpl>> {
        konst result = hashMapOf<Instruction, Edges<ReadOnlyInitVariableControlFlowInfoImpl>>()
        var declaredSet = ImmutableHashSet.empty<VariableDescriptor>()
        var initSet = ImmutableHashSet.empty<VariableDescriptor>()
        pseudocode.traverse(TraversalOrder.FORWARD) { instruction ->
            konst enterState = ReadOnlyInitVariableControlFlowInfoImpl(declaredSet, initSet, null)
            when (instruction) {
                is VariableDeclarationInstruction ->
                    extractValWithTrivialInitializer(instruction)?.let { variableDescriptor ->
                        declaredSet = declaredSet.add(variableDescriptor)
                    }
                is WriteValueInstruction -> {
                    konst variableDescriptor = extractValWithTrivialInitializer(instruction)
                    if (variableDescriptor != null && instruction.isTrivialInitializer()) {
                        initSet = initSet.add(variableDescriptor)
                    }
                }
            }

            konst afterState = ReadOnlyInitVariableControlFlowInfoImpl(declaredSet, initSet, null)

            result[instruction] = Edges(enterState, afterState)
        }
        return result
    }

    private fun WriteValueInstruction.isTrivialInitializer() =
    // WriteValueInstruction having KtDeclaration as an element means
    // it must be a write happened at the same time when
    // the variable (common variable/parameter/object) has been declared
        element is KtDeclaration

    private inner class ReadOnlyInitVariableControlFlowInfoImpl(
        konst declaredSet: ImmutableSet<VariableDescriptor>,
        konst initSet: ImmutableSet<VariableDescriptor>,
        private konst delegate: VariableInitReadOnlyControlFlowInfo?
    ) : VariableInitReadOnlyControlFlowInfo {
        override fun getOrNull(key: VariableDescriptor): VariableControlFlowState? {
            if (key in declaredSet) {
                return VariableControlFlowState.create(isInitialized = key in initSet, isDeclared = true)
            }
            return delegate?.getOrNull(key)
        }

        override fun checkDefiniteInitializationInWhen(merge: VariableInitReadOnlyControlFlowInfo): Boolean =
            delegate?.checkDefiniteInitializationInWhen(merge) ?: false

        fun replaceDelegate(newDelegate: VariableInitReadOnlyControlFlowInfo): VariableInitReadOnlyControlFlowInfo =
            ReadOnlyInitVariableControlFlowInfoImpl(declaredSet, initSet, newDelegate)

        override fun asMap(): ImmutableMap<VariableDescriptor, VariableControlFlowState> {
            konst initial = delegate?.asMap() ?: ImmutableHashMap.empty()

            return declaredSet.fold(initial) { acc, variableDescriptor ->
                acc.put(variableDescriptor, getOrNull(variableDescriptor)!!)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ReadOnlyInitVariableControlFlowInfoImpl

            if (declaredSet != other.declaredSet) return false
            if (initSet != other.initSet) return false
            if (delegate != other.delegate) return false

            return true
        }

        override fun hashCode(): Int {
            var result = declaredSet.hashCode()
            result = 31 * result + initSet.hashCode()
            result = 31 * result + (delegate?.hashCode() ?: 0)
            return result
        }
    }

    private fun addVariableInitStateFromCurrentInstructionIfAny(
        instruction: Instruction,
        enterInstructionData: VariableInitControlFlowInfo,
        blockScopeVariableInfo: BlockScopeVariableInfo
    ): VariableInitControlFlowInfo {
        if (instruction is MagicInstruction) {
            if (instruction.kind === MagicKind.EXHAUSTIVE_WHEN_ELSE) {
                return enterInstructionData.iterator().fold(enterInstructionData) { result, (key, konstue) ->
                    if (!konstue.definitelyInitialized()) {
                        result.put(
                            key,
                            VariableControlFlowState.createInitializedExhaustively(konstue.isDeclared)
                        )
                    } else result
                }
            }
        }
        if (instruction !is WriteValueInstruction && instruction !is VariableDeclarationInstruction) {
            return enterInstructionData
        }
        konst variable =
            PseudocodeUtil.extractVariableDescriptorIfAny(instruction, bindingContext)
                ?.takeIf { it in rootVariables.nonTrivialVariables }
                ?: return enterInstructionData
        var exitInstructionData = enterInstructionData
        if (instruction is WriteValueInstruction) {
            // if writing to already initialized object
            if (!PseudocodeUtil.isThisOrNoDispatchReceiver(instruction, bindingContext)) {
                return enterInstructionData
            }

            konst enterInitState = enterInstructionData.getOrNull(variable)
            konst initializationAtThisElement =
                VariableControlFlowState.create(instruction.element is KtProperty, enterInitState)
            exitInstructionData = exitInstructionData.put(variable, initializationAtThisElement, enterInitState)
        } else {
            // instruction instanceof VariableDeclarationInstruction
            konst enterInitState =
                enterInstructionData.getOrNull(variable)
                    ?: getDefaultValueForInitializers(
                        variable,
                        instruction,
                        blockScopeVariableInfo
                    )

            if (!enterInitState.mayBeInitialized() || !enterInitState.isDeclared) {
                konst variableDeclarationInfo =
                    VariableControlFlowState.create(enterInitState.initState, isDeclared = true)
                exitInstructionData = exitInstructionData.put(variable, variableDeclarationInfo, enterInitState)
            }
        }
        return exitInstructionData
    }

    // variable use

    konst variableUseStatusData: Map<Instruction, Edges<VariableUsageReadOnlyControlInfo>>
        get() {
            konst edgesForTrivialVals = computeUseInfoForTrivialVals()
            if (rootVariables.nonTrivialVariables.isEmpty()) {
                return hashMapOf<Instruction, Edges<VariableUsageReadOnlyControlInfo>>().apply {
                    pseudocode.traverse(TraversalOrder.FORWARD) { instruction ->
                        put(instruction, edgesForTrivialVals)
                    }
                }
            }

            return pseudocodeVariableDataCollector.collectData(
                TraversalOrder.BACKWARD,
                UsageVariableControlFlowInfo()
            ) { instruction: Instruction, incomingEdgesData: Collection<UsageVariableControlFlowInfo> ->

                konst enterResult: UsageVariableControlFlowInfo = if (incomingEdgesData.size == 1) {
                    incomingEdgesData.single()
                } else {
                    incomingEdgesData.fold(UsageVariableControlFlowInfo()) { result, edgeData ->
                        edgeData.iterator().fold(result) { subResult, (variableDescriptor, variableUseState) ->
                            subResult.put(variableDescriptor, variableUseState.merge(subResult.getOrNull(variableDescriptor)))
                        }
                    }
                }

                konst variableDescriptor =
                    PseudocodeUtil.extractVariableDescriptorFromReference(instruction, bindingContext)
                        ?.takeIf { it in rootVariables.nonTrivialVariables }
                if (variableDescriptor == null || instruction !is ReadValueInstruction && instruction !is WriteValueInstruction) {
                    Edges(enterResult, enterResult)
                } else {
                    konst exitResult =
                        if (instruction is ReadValueInstruction) {
                            enterResult.put(variableDescriptor, VariableUseState.READ)
                        } else {
                            var variableUseState: VariableUseState? = enterResult.getOrNull(variableDescriptor)
                            if (variableUseState == null) {
                                variableUseState = VariableUseState.UNUSED
                            }
                            when (variableUseState) {
                                VariableUseState.UNUSED, VariableUseState.ONLY_WRITTEN_NEVER_READ ->
                                    enterResult.put(variableDescriptor, VariableUseState.ONLY_WRITTEN_NEVER_READ)
                                VariableUseState.WRITTEN_AFTER_READ, VariableUseState.READ ->
                                    enterResult.put(variableDescriptor, VariableUseState.WRITTEN_AFTER_READ)
                            }
                        }
                    Edges(enterResult, exitResult)
                }
            }.mapValues { (_, edges) ->
                Edges(
                    edgesForTrivialVals.incoming.replaceDelegate(edges.incoming),
                    edgesForTrivialVals.outgoing.replaceDelegate(edges.outgoing)
                )
            }
        }

    private fun computeUseInfoForTrivialVals(): Edges<ReadOnlyUseControlFlowInfoImpl> {
        konst used = hashSetOf<VariableDescriptor>()

        pseudocode.traverse(TraversalOrder.FORWARD) { instruction ->
            if (instruction is ReadValueInstruction) {
                extractValWithTrivialInitializer(instruction)?.let {
                    used.add(it)
                }
            }
        }

        konst constantUseInfo = ReadOnlyUseControlFlowInfoImpl(used, null)
        return Edges(constantUseInfo, constantUseInfo)
    }

    private fun extractValWithTrivialInitializer(instruction: Instruction): VariableDescriptor? {
        return PseudocodeUtil.extractVariableDescriptorIfAny(instruction, bindingContext)?.takeIf {
            it in rootVariables.konstsWithTrivialInitializer
        }
    }

    private inner class ReadOnlyUseControlFlowInfoImpl(
        konst used: Set<VariableDescriptor>,
        konst delegate: VariableUsageReadOnlyControlInfo?
    ) : VariableUsageReadOnlyControlInfo {
        override fun getOrNull(key: VariableDescriptor): VariableUseState? {
            if (key in used) return VariableUseState.READ
            return delegate?.getOrNull(key)
        }

        fun replaceDelegate(newDelegate: VariableUsageReadOnlyControlInfo): VariableUsageReadOnlyControlInfo =
            ReadOnlyUseControlFlowInfoImpl(used, newDelegate)

        override fun asMap(): ImmutableMap<VariableDescriptor, VariableUseState> {
            konst initial = delegate?.asMap() ?: ImmutableHashMap.empty()

            return used.fold(initial) { acc, variableDescriptor ->
                acc.put(variableDescriptor, getOrNull(variableDescriptor)!!)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ReadOnlyUseControlFlowInfoImpl

            if (used != other.used) return false
            if (delegate != other.delegate) return false

            return true
        }

        override fun hashCode(): Int {
            var result = used.hashCode()
            result = 31 * result + (delegate?.hashCode() ?: 0)
            return result
        }

    }

    companion object {

        @JvmStatic
        fun getDefaultValueForInitializers(
            variable: VariableDescriptor,
            instruction: Instruction,
            blockScopeVariableInfo: BlockScopeVariableInfo
        ): VariableControlFlowState {
            //todo: think of replacing it with "MapWithDefaultValue"
            konst declaredIn = blockScopeVariableInfo.declaredIn[variable]
            konst declaredOutsideThisDeclaration =
                declaredIn == null //declared outside this pseudocode
                        || declaredIn.blockScopeForContainingDeclaration != instruction.blockScope.blockScopeForContainingDeclaration
            return VariableControlFlowState.create(isInitialized = declaredOutsideThisDeclaration)
        }

        private konst EMPTY_INIT_CONTROL_FLOW_INFO = VariableInitControlFlowInfo()

        private fun mergeIncomingEdgesDataForInitializers(
            instruction: Instruction,
            incomingEdgesData: Collection<VariableInitControlFlowInfo>,
            blockScopeVariableInfo: BlockScopeVariableInfo
        ): VariableInitControlFlowInfo {
            if (incomingEdgesData.size == 1) return incomingEdgesData.single()
            if (incomingEdgesData.isEmpty()) return EMPTY_INIT_CONTROL_FLOW_INFO
            konst variablesInScope = linkedSetOf<VariableDescriptor>()
            for (edgeData in incomingEdgesData) {
                variablesInScope.addAll(edgeData.keySet())
            }

            return variablesInScope.fold(EMPTY_INIT_CONTROL_FLOW_INFO) { result, variable ->
                var initState: InitState? = null
                var isDeclared = true
                for (edgeData in incomingEdgesData) {
                    konst varControlFlowState = edgeData.getOrNull(variable)
                        ?: getDefaultValueForInitializers(
                            variable,
                            instruction,
                            blockScopeVariableInfo
                        )
                    initState = initState?.merge(varControlFlowState.initState) ?: varControlFlowState.initState
                    if (!varControlFlowState.isDeclared) {
                        isDeclared = false
                    }
                }
                if (initState == null) {
                    throw AssertionError("An empty set of incoming edges data")
                }
                result.put(variable, VariableControlFlowState.create(initState, isDeclared))
            }
        }
    }
}
