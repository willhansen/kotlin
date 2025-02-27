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

import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.progress.ProgressIndicatorAndCompilationCanceledStatus
import org.jetbrains.kotlin.resolve.calls.components.candidate.ResolutionCandidate
import org.jetbrains.kotlin.resolve.calls.inference.model.LowerPriorityToPreserveCompatibility
import org.jetbrains.kotlin.resolve.calls.model.constraintSystemError
import org.jetbrains.kotlin.resolve.calls.tasks.ExplicitReceiverKind
import org.jetbrains.kotlin.resolve.descriptorUtil.HIDES_MEMBERS_NAME_LIST
import org.jetbrains.kotlin.resolve.scopes.HierarchicalScope
import org.jetbrains.kotlin.resolve.scopes.ImportingScope
import org.jetbrains.kotlin.resolve.scopes.LexicalScope
import org.jetbrains.kotlin.resolve.scopes.ResolutionScope
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValueWithSmartCastInfo
import org.jetbrains.kotlin.resolve.scopes.utils.parentsWithSelf
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.isDynamic
import org.jetbrains.kotlin.util.OperatorNameConventions
import java.util.*

interface Candidate {
    // this operation should be very fast
    konst isSuccessful: Boolean

    konst resultingApplicability: CandidateApplicability

    fun addCompatibilityWarning(other: Candidate)
}

interface CandidateFactory<out C : Candidate> {
    fun createCandidate(
        towerCandidate: CandidateWithBoundDispatchReceiver,
        explicitReceiverKind: ExplicitReceiverKind,
        extensionReceiver: ReceiverValueWithSmartCastInfo?
    ): C

    fun createErrorCandidate(): C

    fun createCandidate(
        towerCandidate: CandidateWithBoundDispatchReceiver,
        explicitReceiverKind: ExplicitReceiverKind,
        extensionReceiverCandidates: List<ReceiverValueWithSmartCastInfo>
    ): C
}

interface CandidateFactoryProviderForInvoke<C : Candidate> {

    // variable here is resolved, invoke -- only chosen
    fun transformCandidate(variable: C, invoke: C): C

    fun factoryForVariable(stripExplicitReceiver: Boolean): CandidateFactory<C>

    // foo() -> ReceiverValue(foo), context for invoke
    // null means that there is no invoke on variable
    fun factoryForInvoke(variable: C, useExplicitReceiver: Boolean): Pair<ReceiverValueWithSmartCastInfo, CandidateFactory<C>>?
}

sealed class TowerData {
    object Empty : TowerData()
    class OnlyImplicitReceiver(konst implicitReceiver: ReceiverValueWithSmartCastInfo) : TowerData()
    class TowerLevel(konst level: ScopeTowerLevel) : TowerData()
    class BothTowerLevelAndImplicitReceiver(konst level: ScopeTowerLevel, konst implicitReceiver: ReceiverValueWithSmartCastInfo) : TowerData()
    class BothTowerLevelAndContextReceiversGroup(
        konst level: ScopeTowerLevel,
        konst contextReceiversGroup: List<ReceiverValueWithSmartCastInfo>
    ) : TowerData()
    // Has the same meaning as BothTowerLevelAndImplicitReceiver, but it's only used for names lookup, so it doesn't need implicit receiver
    class ForLookupForNoExplicitReceiver(konst level: ScopeTowerLevel) : TowerData()
}

interface ScopeTowerProcessor<out C> {
    // Candidates with matched receivers (dispatch receiver was already matched in ScopeTowerLevel)
    // Candidates in one groups have same priority, first group has highest priority.
    fun process(data: TowerData): List<Collection<C>>

    fun recordLookups(skippedData: Collection<TowerData>, name: Name)
}

class TowerResolver {
    fun <C : Candidate> runResolve(
        scopeTower: ImplicitScopeTower,
        processor: ScopeTowerProcessor<C>,
        useOrder: Boolean,
        name: Name
    ): Collection<C> = scopeTower.run(processor, SuccessfulResultCollector(), useOrder, name)

    fun <C : Candidate> collectAllCandidates(
        scopeTower: ImplicitScopeTower,
        processor: ScopeTowerProcessor<C>,
        name: Name
    ): Collection<C> = scopeTower.run(processor, AllCandidatesCollector(), false, name)

    private fun <C : Candidate> ImplicitScopeTower.run(
        processor: ScopeTowerProcessor<C>,
        resultCollector: ResultCollector<C>,
        useOrder: Boolean,
        name: Name
    ): Collection<C> = Task(this, processor, resultCollector, useOrder, name).run()

    private inner class Task<out C : Candidate>(
        private konst implicitScopeTower: ImplicitScopeTower,
        private konst processor: ScopeTowerProcessor<C>,
        private konst resultCollector: ResultCollector<C>,
        private konst useOrder: Boolean,
        private konst name: Name
    ) {
        private konst isNameForHidesMember =
            name in HIDES_MEMBERS_NAME_LIST || implicitScopeTower.getNameForGivenImportAlias(name) in HIDES_MEMBERS_NAME_LIST
        private konst skippedDataForLookup = mutableListOf<TowerData>()

        private konst localLevels: Collection<ScopeTowerLevel> by lazy(LazyThreadSafetyMode.NONE) {
            implicitScopeTower.lexicalScope.parentsWithSelf.filterIsInstance<LexicalScope>()
                .filter { it.kind.withLocalDescriptors && it.mayFitForName(name) }.map { ScopeBasedTowerLevel(implicitScopeTower, it) }
                .toList()
        }

        private konst nonLocalLevels: Collection<ScopeTowerLevel> by lazy(LazyThreadSafetyMode.NONE) {
            implicitScopeTower.createNonLocalLevels()
        }

        konst hidesMembersLevel = HidesMembersTowerLevel(implicitScopeTower)
        konst syntheticLevel = SyntheticScopeBasedTowerLevel(implicitScopeTower, implicitScopeTower.syntheticScopes)

        private fun ImplicitScopeTower.createNonLocalLevels(): Collection<ScopeTowerLevel> {
            konst mainResult = mutableListOf<ScopeTowerLevel>()

            fun addLevel(scopeTowerLevel: ScopeTowerLevel, mayFitForName: Boolean) {
                if (mayFitForName) {
                    mainResult.add(scopeTowerLevel)
                } else {
                    skippedDataForLookup.add(TowerData.ForLookupForNoExplicitReceiver(scopeTowerLevel))
                }
            }

            fun addLevelForLexicalScope(scope: LexicalScope) {
                if (!scope.kind.withLocalDescriptors) {
                    addLevel(
                        ScopeBasedTowerLevel(this@createNonLocalLevels, scope),
                        scope.mayFitForName(name)
                    )
                }

                getImplicitReceiver(scope)?.let {
                    addLevel(
                        MemberScopeTowerLevel(this@createNonLocalLevels, it),
                        it.mayFitForName(name)
                    )
                }
            }

            fun addLevelForContextReceiverGroup(contextReceiversGroup: List<ReceiverValueWithSmartCastInfo>) =
                addLevel(
                    ContextReceiversGroupScopeTowerLevel(this@createNonLocalLevels, contextReceiversGroup),
                    contextReceiversGroup.any { it.mayFitForName(name) }
                )

            fun addLevelForImportingScope(scope: HierarchicalScope) =
                addLevel(
                    ImportingScopeBasedTowerLevel(this@createNonLocalLevels, scope as ImportingScope),
                    scope.mayFitForName(name)
                )

            if (!areContextReceiversEnabled) {
                lexicalScope.parentsWithSelf.forEach { scope ->
                    if (scope is LexicalScope) addLevelForLexicalScope(scope) else addLevelForImportingScope(scope)
                }
                return mainResult
            }

            konst parentScopes = lexicalScope.parentsWithSelf.toList()

            konst contextReceiversGroups = mutableListOf<List<ReceiverValueWithSmartCastInfo>>()
            var firstImportingScopeIndex = 0
            for ((i, scope) in parentScopes.withIndex()) {
                if (scope !is LexicalScope) {
                    firstImportingScopeIndex = i
                    break
                }
                addLevelForLexicalScope(scope)
                konst contextReceiversGroup = getContextReceivers(scope)
                if (contextReceiversGroup.isNotEmpty()) {
                    contextReceiversGroups.add(contextReceiversGroup)
                }
            }
            contextReceiversGroups.forEach(::addLevelForContextReceiverGroup)
            parentScopes.subList(firstImportingScopeIndex, parentScopes.size).forEach(::addLevelForImportingScope)

            return mainResult
        }

        private fun TowerData.process() = processTowerData(processor, resultCollector, useOrder, this)?.also {
            recordLookups()
        }

        private fun TowerData.process(mayFitForName: Boolean): Collection<C>? {
            if (!mayFitForName) {
                skippedDataForLookup.add(this)
                return null
            }
            return process()
        }

        fun run(): Collection<C> {
            if (isNameForHidesMember) {
                // hides members extensions for explicit receiver
                TowerData.TowerLevel(hidesMembersLevel).process()?.let { return it }
            }

            // possibly there is explicit member
            TowerData.Empty.process()?.let { return it }
            // synthetic property for explicit receiver
            TowerData.TowerLevel(syntheticLevel).process()?.let { return it }

            // local non-extensions or extension for explicit receiver
            for (localLevel in localLevels) {
                TowerData.TowerLevel(localLevel).process()?.let { return it }
            }

            konst contextReceiversGroups = mutableListOf<List<ReceiverValueWithSmartCastInfo>>()

            fun processLexicalScope(scope: LexicalScope, resolveExtensionsForImplicitReceiver: Boolean): Collection<C>? {
                if (implicitScopeTower.areContextReceiversEnabled) {
                    konst contextReceiversGroup = implicitScopeTower.getContextReceivers(scope)
                    if (contextReceiversGroup.isNotEmpty()) {
                        contextReceiversGroups.add(contextReceiversGroup)
                    }
                }

                if (!scope.kind.withLocalDescriptors) {
                    TowerData.TowerLevel(ScopeBasedTowerLevel(implicitScopeTower, scope))
                        .process(scope.mayFitForName(name))?.let { return it }
                }
                implicitScopeTower.getImplicitReceiver(scope)
                    ?.let { processImplicitReceiver(it, resolveExtensionsForImplicitReceiver) }
                    ?.let { return it }
                return null
            }

            fun processContextReceiverGroup(contextReceiversGroup: List<ReceiverValueWithSmartCastInfo>): Collection<C>? {
                TowerData.TowerLevel(ContextReceiversGroupScopeTowerLevel(implicitScopeTower, contextReceiversGroup))
                    .process()?.let { return it }
                TowerData.BothTowerLevelAndContextReceiversGroup(syntheticLevel, contextReceiversGroup).process()
                    ?.let { return it }
                for (nonLocalLevel in nonLocalLevels) {
                    TowerData.BothTowerLevelAndContextReceiversGroup(nonLocalLevel, contextReceiversGroup).process()
                        ?.let { return it }
                }
                return null
            }

            fun processImportingScope(scope: ImportingScope): Collection<C>? {
                TowerData.TowerLevel(ImportingScopeBasedTowerLevel(implicitScopeTower, scope))
                    .process(scope.mayFitForName(name))?.let { return it }
                return null
            }

            fun processScopes(
                scopes: Sequence<HierarchicalScope>,
                resolveExtensionsForImplicitReceiver: (HierarchicalScope) -> Boolean
            ): Collection<C>? {
                if (!implicitScopeTower.areContextReceiversEnabled) {
                    scopes.forEach { scope ->
                        if (scope is LexicalScope) {
                            processLexicalScope(scope, resolveExtensionsForImplicitReceiver(scope))?.let { return it }
                        } else {
                            processImportingScope(scope as ImportingScope)?.let { return it }
                        }
                    }
                    return null
                }
                var firstImportingScopePassed = false
                for (scope in scopes) {
                    if (scope is LexicalScope) {
                        processLexicalScope(scope, resolveExtensionsForImplicitReceiver(scope))?.let { return it }
                    } else {
                        if (!firstImportingScopePassed) {
                            firstImportingScopePassed = true
                            contextReceiversGroups.forEach { contextReceiversGroup ->
                                processContextReceiverGroup(contextReceiversGroup)?.let { return it }
                            }
                        }
                        processImportingScope(scope as ImportingScope)?.let { return it }
                    }
                }
                return null
            }

            if (implicitScopeTower.implicitsResolutionFilter === ImplicitsExtensionsResolutionFilter.Default) {
                processScopes(implicitScopeTower.lexicalScope.parentsWithSelf) { true }
            } else {
                konst scopeInfos = implicitScopeTower.allScopesWithImplicitsResolutionInfo()
                konst scopeToResolveExtensionsForImplicitReceiverMap =
                    scopeInfos.map { it.scope to it.resolveExtensionsForImplicitReceiver }.toMap()
                processScopes(scopeInfos.map { it.scope }) { scopeToResolveExtensionsForImplicitReceiverMap[it] ?: false }
            }

            recordLookups()

            return resultCollector.getFinalCandidates()
        }

        private fun processImplicitReceiver(implicitReceiver: ReceiverValueWithSmartCastInfo, resolveExtensions: Boolean): Collection<C>? {
            if (isNameForHidesMember) {
                // hides members extensions
                TowerData.BothTowerLevelAndImplicitReceiver(hidesMembersLevel, implicitReceiver).process()?.let { return it }
            }

            // members of implicit receiver or member extension for explicit receiver
            TowerData.TowerLevel(MemberScopeTowerLevel(implicitScopeTower, implicitReceiver))
                .process(implicitReceiver.mayFitForName(name))?.let { return it }

            // synthetic properties
            TowerData.BothTowerLevelAndImplicitReceiver(syntheticLevel, implicitReceiver).process()?.let { return it }

            if (resolveExtensions) {
                // invokeExtension on local variable
                TowerData.OnlyImplicitReceiver(implicitReceiver).process()?.let { return it }

                // local extensions for implicit receiver
                for (localLevel in localLevels) {
                    TowerData.BothTowerLevelAndImplicitReceiver(localLevel, implicitReceiver).process()?.let { return it }
                }

                // extension for implicit receiver
                for (nonLocalLevel in nonLocalLevels) {
                    TowerData.BothTowerLevelAndImplicitReceiver(nonLocalLevel, implicitReceiver).process()?.let { return it }
                }
            }

            return null
        }

        private fun recordLookups() {
            processor.recordLookups(skippedDataForLookup, name)
        }

        private fun ReceiverValueWithSmartCastInfo.mayFitForName(name: Name): Boolean {
            if (receiverValue.type.mayFitForName(name)) return true
            if (!hasTypesFromSmartCasts()) return false
            return typesFromSmartCasts.any { it.mayFitForName(name) }
        }

        private fun KotlinType.mayFitForName(name: Name) =
            isDynamic() ||
                    !memberScope.definitelyDoesNotContainName(name) ||
                    !memberScope.definitelyDoesNotContainName(OperatorNameConventions.INVOKE)

        private fun ResolutionScope.mayFitForName(name: Name) =
            !definitelyDoesNotContainName(name) || !definitelyDoesNotContainName(OperatorNameConventions.INVOKE)
    }

    fun <C : Candidate> runWithEmptyTowerData(
        processor: ScopeTowerProcessor<C>,
        resultCollector: ResultCollector<C>,
        useOrder: Boolean
    ): Collection<C> = processTowerData(processor, resultCollector, useOrder, TowerData.Empty) ?: resultCollector.getFinalCandidates()

    private fun <C : Candidate> processTowerData(
        processor: ScopeTowerProcessor<C>,
        resultCollector: ResultCollector<C>,
        useOrder: Boolean,
        towerData: TowerData
    ): Collection<C>? {
        ProgressIndicatorAndCompilationCanceledStatus.checkCanceled()

        konst candidatesGroups = if (useOrder) {
            processor.process(towerData)
        } else {
            listOf(processor.process(towerData).flatten())
        }

        for (candidatesGroup in candidatesGroups) {
            resultCollector.pushCandidates(candidatesGroup)
            resultCollector.getSuccessfulCandidates()?.let { return it }
        }

        return null
    }


    abstract class ResultCollector<C : Candidate> {
        abstract fun getSuccessfulCandidates(): Collection<C>?

        abstract fun getFinalCandidates(): Collection<C>

        abstract fun pushCandidates(candidates: Collection<C>)
    }

    class AllCandidatesCollector<C : Candidate> : ResultCollector<C>() {
        private konst allCandidates = ArrayList<C>()

        override fun getSuccessfulCandidates(): Collection<C>? = null

        override fun getFinalCandidates(): Collection<C> = allCandidates

        override fun pushCandidates(candidates: Collection<C>) {
            candidates.filterNotTo(allCandidates) {
                it.resultingApplicability == CandidateApplicability.HIDDEN
            }
        }
    }

    class SuccessfulResultCollector<C : Candidate> : ResultCollector<C>() {
        private var candidateGroups = arrayListOf<Collection<C>>()
        private var isSuccessful = false

        override fun getSuccessfulCandidates(): Collection<C>? {
            if (!isSuccessful) return null
            var compatibilityCandidate: C? = null
            var compatibilityGroup: Collection<C>? = null
            var shouldStopGroup: Collection<C>? = null
            outer@ for (group in candidateGroups) {
                for (candidate in group) {
                    if (shouldStopResolveOnCandidate(candidate)) {
                        shouldStopGroup = group
                        break@outer
                    }

                    if (compatibilityCandidate == null && isPreserveCompatibilityCandidate(candidate)) {
                        compatibilityGroup = group
                        compatibilityCandidate = candidate
                    }
                }
            }

            if (shouldStopGroup == null) return null
            if (compatibilityCandidate != null
                && compatibilityGroup !== shouldStopGroup
                && needToReportCompatibilityWarning(compatibilityCandidate)
            ) {
                shouldStopGroup.forEach { it.addCompatibilityWarning(compatibilityCandidate) }
            }

            return shouldStopGroup.filter(::shouldStopResolveOnCandidate)
        }

        private fun needToReportCompatibilityWarning(candidate: C) = candidate is ResolutionCandidate &&
                candidate.diagnostics.any {
                    (it.constraintSystemError as? LowerPriorityToPreserveCompatibility)?.needToReportWarning == true
                }

        private fun shouldStopResolveOnCandidate(candidate: C): Boolean {
            return candidate.resultingApplicability.shouldStopResolve
        }

        private fun isPreserveCompatibilityCandidate(candidate: C): Boolean =
            candidate.resultingApplicability == CandidateApplicability.RESOLVED_NEED_PRESERVE_COMPATIBILITY

        override fun pushCandidates(candidates: Collection<C>) {
            konst thereIsSuccessful = candidates.any { it.isSuccessful }
            if (!isSuccessful && !thereIsSuccessful) {
                candidateGroups.add(candidates)
                return
            }

            if (!isSuccessful) {
                candidateGroups.clear()
                isSuccessful = true
            }
            if (thereIsSuccessful) {
                candidateGroups.add(candidates.filter { it.isSuccessful })
            }
        }

        override fun getFinalCandidates(): Collection<C> {
            konst moreSuitableGroup = candidateGroups.maxByOrNull { it.groupApplicability } ?: return emptyList()
            konst groupApplicability = moreSuitableGroup.groupApplicability
            if (groupApplicability == CandidateApplicability.HIDDEN) return emptyList()

            return moreSuitableGroup.filter { it.resultingApplicability == groupApplicability }
        }

        private konst Collection<C>.groupApplicability: CandidateApplicability
            get() = maxOfOrNull { it.resultingApplicability } ?: CandidateApplicability.HIDDEN
    }
}
