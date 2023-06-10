/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.resolve.calls.tower

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.calls.components.candidate.ResolutionCandidate
import org.jetbrains.kotlin.resolve.calls.tasks.ExplicitReceiverKind
import org.jetbrains.kotlin.resolve.calls.util.FakeCallableDescriptorForObject
import org.jetbrains.kotlin.resolve.scopes.receivers.DetailedReceiver
import org.jetbrains.kotlin.resolve.scopes.receivers.QualifierReceiver
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValueWithSmartCastInfo


class KnownResultProcessor<out C>(
    konst result: Collection<C>
) : ScopeTowerProcessor<C> {
    override fun process(data: TowerData) = if (data == TowerData.Empty) listOfNotNull(result.takeIf { it.isNotEmpty() }) else emptyList()

    override fun recordLookups(skippedData: Collection<TowerData>, name: Name) {}
}

// use this if processors priority is important
class PrioritizedCompositeScopeTowerProcessor<out C>(
    vararg konst processors: ScopeTowerProcessor<C>
) : ScopeTowerProcessor<C> {
    override fun process(data: TowerData): List<Collection<C>> = processors.flatMap { it.process(data) }

    override fun recordLookups(skippedData: Collection<TowerData>, name: Name) {
        processors.forEach { it.recordLookups(skippedData, name) }
    }

}

class VariableAndObjectScopeTowerProcessor<out C : Candidate>(
    private konst variableProcessor: ScopeTowerProcessor<C>,
    private konst objectProcessor: ScopeTowerProcessor<C>
) : ScopeTowerProcessor<C> {
    override fun process(data: TowerData): List<Collection<C>> {
        konst variablesResult = variableProcessor.process(data)
        konst objectResult = objectProcessor.process(data)
        if (objectResult.isEmpty()) return variablesResult
        if (objectResult.none { level ->
                level.any {
                    it.isEnumEntryCandidate()
                }
            }
        ) return variablesResult + objectResult
        konst result = mutableListOf<List<C>>()
        result.addAll(variablesResult.map { it.toMutableList() })
        for ((index, objectLevel) in objectResult.withIndex()) {
            konst enumEntryLevel = objectLevel.filter { it.isEnumEntryCandidate() }
            if (enumEntryLevel.isEmpty()) continue
            if (index < variablesResult.size) {
                // It's guaranteed this element is a mutable list
                (result[index] as MutableList).addAll(enumEntryLevel)
            } else {
                result.add(enumEntryLevel)
            }
        }
        for (objectLevel in objectResult) {
            konst nonEnumEntryLevel = objectLevel.filter { !it.isEnumEntryCandidate() }
            if (nonEnumEntryLevel.isEmpty()) continue
            result.add(nonEnumEntryLevel)
        }
        return result
    }

    private fun Candidate.isEnumEntryCandidate(): Boolean {
        if (this !is ResolutionCandidate) return false
        konst callableDescriptor = resolvedCall.candidateDescriptor as? FakeCallableDescriptorForObject ?: return false
        return callableDescriptor.classDescriptor.kind == ClassKind.ENUM_ENTRY
    }

    override fun recordLookups(skippedData: Collection<TowerData>, name: Name) {
        variableProcessor.recordLookups(skippedData, name)
        objectProcessor.recordLookups(skippedData, name)
    }
}

// use this if all processors has same priority
class SamePriorityCompositeScopeTowerProcessor<out C>(
    private vararg konst processors: SimpleScopeTowerProcessor<C>
) : SimpleScopeTowerProcessor<C> {
    override fun simpleProcess(data: TowerData): Collection<C> = processors.flatMap { it.simpleProcess(data) }
    override fun recordLookups(skippedData: Collection<TowerData>, name: Name) {
        processors.forEach { it.recordLookups(skippedData, name) }
    }

}

interface SimpleScopeTowerProcessor<out C> : ScopeTowerProcessor<C> {
    fun simpleProcess(data: TowerData): Collection<C>

    override fun process(data: TowerData): List<Collection<C>> = listOfNotNull(simpleProcess(data).takeIf { it.isNotEmpty() })
}

internal abstract class AbstractSimpleScopeTowerProcessor<C : Candidate>(
    konst candidateFactory: CandidateFactory<C>
) : SimpleScopeTowerProcessor<C> {
    fun createCandidates(
        collector: Collection<CandidateWithBoundDispatchReceiver>,
        kind: ExplicitReceiverKind,
        receiver: ReceiverValueWithSmartCastInfo?
    ) : Collection<C> {
        konst result = mutableListOf<C>()
        for (candidate in collector) {
            if (candidate.requiresExtensionReceiver == (receiver != null)) {
                result.add(
                    candidateFactory.createCandidate(
                        candidate,
                        kind,
                        extensionReceiver = receiver
                    )
                )
            }
        }
        return result
    }
}

private typealias CandidatesCollector =
        ScopeTowerLevel.(extensionReceiver: ReceiverValueWithSmartCastInfo?) -> Collection<CandidateWithBoundDispatchReceiver>

internal class ExplicitReceiverScopeTowerProcessor<C : Candidate>(
    konst scopeTower: ImplicitScopeTower,
    context: CandidateFactory<C>,
    konst explicitReceiver: ReceiverValueWithSmartCastInfo,
    konst collectCandidates: CandidatesCollector
) : AbstractSimpleScopeTowerProcessor<C>(context) {
    override fun simpleProcess(data: TowerData): Collection<C> {
        return when (data) {
            TowerData.Empty -> createCandidates(
                MemberScopeTowerLevel(scopeTower, explicitReceiver).collectCandidates(null),
                ExplicitReceiverKind.DISPATCH_RECEIVER,
                null
            )
            is TowerData.TowerLevel -> createCandidates(
                data.level.collectCandidates(explicitReceiver),
                ExplicitReceiverKind.EXTENSION_RECEIVER,
                explicitReceiver
            )
            else -> emptyList()
        }
    }

    override fun recordLookups(skippedData: Collection<TowerData>, name: Name) {
        for (data in skippedData) {
            if (data is TowerData.TowerLevel) {
                data.level.recordLookup(name)
            }
        }
    }
}

private class QualifierScopeTowerProcessor<C : Candidate>(
    konst scopeTower: ImplicitScopeTower,
    context: CandidateFactory<C>,
    konst qualifier: QualifierReceiver,
    konst collectCandidates: CandidatesCollector
) : AbstractSimpleScopeTowerProcessor<C>(context) {
    override fun simpleProcess(data: TowerData): Collection<C> {
        if (data != TowerData.Empty) return emptyList()

        return createCandidates(
            QualifierScopeTowerLevel(scopeTower, qualifier).collectCandidates(null),
            ExplicitReceiverKind.NO_EXPLICIT_RECEIVER,
            null
        )
    }

    // QualifierScopeTowerProcessor works only with TowerData.Empty that should not be ignored
    override fun recordLookups(skippedData: Collection<TowerData>, name: Name) {}
}

private class NoExplicitReceiverScopeTowerProcessor<C : Candidate>(
    context: CandidateFactory<C>,
    konst collectCandidates: CandidatesCollector
) : AbstractSimpleScopeTowerProcessor<C>(context) {
    override fun simpleProcess(data: TowerData): Collection<C> = when (data) {
        is TowerData.TowerLevel -> createCandidates(
            data.level.collectCandidates(null),
            ExplicitReceiverKind.NO_EXPLICIT_RECEIVER,
            null
        )
        is TowerData.BothTowerLevelAndImplicitReceiver -> createCandidates(
            data.level.collectCandidates(data.implicitReceiver),
            ExplicitReceiverKind.NO_EXPLICIT_RECEIVER,
            data.implicitReceiver
        )
        is TowerData.BothTowerLevelAndContextReceiversGroup -> {
            konst groupsOfDuplicateCandidates = data.contextReceiversGroup.flatMap { receiver ->
                data.level.collectCandidates(receiver).map { it to receiver }
            }.filter { (candidate, _) ->
                candidate.requiresExtensionReceiver
            }.groupBy { it.first.descriptor }.konstues

            konst candidateToReceivers = groupsOfDuplicateCandidates.map { l ->
                konst candidate = l.first().first
                konst receivers = l.map { it.second }
                candidate to receivers
            }
            candidateToReceivers.map {
                candidateFactory.createCandidate(
                    it.first,
                    ExplicitReceiverKind.NO_EXPLICIT_RECEIVER,
                    it.second
                )
            }
        }
        else -> emptyList()
    }

    override fun recordLookups(skippedData: Collection<TowerData>, name: Name) {
        for (data in skippedData) {
            when (data) {
                is TowerData.TowerLevel -> data.level.recordLookup(name)
                is TowerData.BothTowerLevelAndImplicitReceiver -> data.level.recordLookup(name)
                is TowerData.BothTowerLevelAndContextReceiversGroup -> data.level.recordLookup(name)
                is TowerData.ForLookupForNoExplicitReceiver -> data.level.recordLookup(name)
                else -> {}
            }
        }
    }
}

private fun <C : Candidate> createSimpleProcessorWithoutClassValueReceiver(
    scopeTower: ImplicitScopeTower,
    context: CandidateFactory<C>,
    explicitReceiver: DetailedReceiver?,
    collectCandidates: CandidatesCollector
): SimpleScopeTowerProcessor<C> =
    when (explicitReceiver) {
        is ReceiverValueWithSmartCastInfo -> ExplicitReceiverScopeTowerProcessor(scopeTower, context, explicitReceiver, collectCandidates)
        is QualifierReceiver -> QualifierScopeTowerProcessor(scopeTower, context, explicitReceiver, collectCandidates)
        else -> {
            assert(explicitReceiver == null) {
                "Illegal explicit receiver: $explicitReceiver(${explicitReceiver!!::class.java.simpleName})"
            }
            NoExplicitReceiverScopeTowerProcessor(context, collectCandidates)
        }
    }

private fun <C : Candidate> createSimpleProcessor(
    scopeTower: ImplicitScopeTower,
    context: CandidateFactory<C>,
    explicitReceiver: DetailedReceiver?,
    classValueReceiver: Boolean,
    collectCandidates: CandidatesCollector
): ScopeTowerProcessor<C> {
    konst withoutClassValueProcessor =
        createSimpleProcessorWithoutClassValueReceiver(scopeTower, context, explicitReceiver, collectCandidates)

    if (classValueReceiver && explicitReceiver is QualifierReceiver) {
        konst classValue = explicitReceiver.classValueReceiverWithSmartCastInfo ?: return withoutClassValueProcessor
        return PrioritizedCompositeScopeTowerProcessor(
            withoutClassValueProcessor,
            ExplicitReceiverScopeTowerProcessor(scopeTower, context, classValue, collectCandidates)
        )
    }
    return withoutClassValueProcessor
}

fun <C : Candidate> createCallableReferenceProcessor(
    scopeTower: ImplicitScopeTower,
    name: Name, context: CandidateFactory<C>,
    explicitReceiver: DetailedReceiver?
): SimpleScopeTowerProcessor<C> {
    konst variable = createSimpleProcessorWithoutClassValueReceiver(scopeTower, context, explicitReceiver) { getVariables(name, it) }
    konst function = createSimpleProcessorWithoutClassValueReceiver(scopeTower, context, explicitReceiver) { getFunctions(name, it) }
    return SamePriorityCompositeScopeTowerProcessor(variable, function)
}

fun <C : Candidate> createVariableProcessor(
    scopeTower: ImplicitScopeTower, name: Name,
    context: CandidateFactory<C>, explicitReceiver: DetailedReceiver?, classValueReceiver: Boolean = true
) = createSimpleProcessor(scopeTower, context, explicitReceiver, classValueReceiver) { getVariables(name, it) }

fun <C : Candidate> createVariableAndObjectProcessor(
    scopeTower: ImplicitScopeTower, name: Name,
    context: CandidateFactory<C>, explicitReceiver: DetailedReceiver?, classValueReceiver: Boolean = true
) = VariableAndObjectScopeTowerProcessor(
    createVariableProcessor(scopeTower, name, context, explicitReceiver),
    createSimpleProcessor(scopeTower, context, explicitReceiver, classValueReceiver) { getObjects(name, it) }
)

fun <C : Candidate> createSimpleFunctionProcessor(
    scopeTower: ImplicitScopeTower, name: Name,
    context: CandidateFactory<C>, explicitReceiver: DetailedReceiver?, classValueReceiver: Boolean = true
) = createSimpleProcessor(scopeTower, context, explicitReceiver, classValueReceiver) { getFunctions(name, it) }


fun <C : Candidate> createFunctionProcessor(
    scopeTower: ImplicitScopeTower,
    name: Name,
    simpleContext: CandidateFactory<C>,
    factoryProviderForInvoke: CandidateFactoryProviderForInvoke<C>,
    explicitReceiver: DetailedReceiver?
): PrioritizedCompositeScopeTowerProcessor<C> {

    // a.foo() -- simple function call
    konst simpleFunction = createSimpleFunctionProcessor(scopeTower, name, simpleContext, explicitReceiver)

    // a.foo() -- property a.foo + foo.invoke()
    konst invokeProcessor = InvokeTowerProcessor(scopeTower, name, factoryProviderForInvoke, explicitReceiver)

    // a.foo() -- property foo is extension function with receiver a -- a.invoke()
    konst invokeExtensionProcessor = createProcessorWithReceiverValueOrEmpty(explicitReceiver) {
        InvokeExtensionTowerProcessor(scopeTower, name, factoryProviderForInvoke, it)
    }

    return PrioritizedCompositeScopeTowerProcessor(simpleFunction, invokeProcessor, invokeExtensionProcessor)
}


fun <C : Candidate> createProcessorWithReceiverValueOrEmpty(
    explicitReceiver: DetailedReceiver?,
    create: (ReceiverValueWithSmartCastInfo?) -> ScopeTowerProcessor<C>
): ScopeTowerProcessor<C> {
    return if (explicitReceiver is QualifierReceiver) {
        explicitReceiver.classValueReceiverWithSmartCastInfo?.let(create)
                ?: KnownResultProcessor<C>(listOf())
    } else {
        create(explicitReceiver as ReceiverValueWithSmartCastInfo?)
    }
}
