/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.calls.tower

import org.jetbrains.kotlin.fir.resolve.calls.*
import org.jetbrains.kotlin.fir.scopes.FirScope
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.calls.tasks.ExplicitReceiverKind
import org.jetbrains.kotlin.resolve.calls.tower.CandidateApplicability

internal class CandidateFactoriesAndCollectors(
    // Common calls
    konst candidateFactory: CandidateFactory,
    konst resultCollector: CandidateCollector,
)


internal class TowerLevelHandler {

    // Try to avoid adding additional state here
    private var processResult = ProcessResult.SCOPE_EMPTY

    fun handleLevel(
        collector: CandidateCollector,
        candidateFactory: CandidateFactory,
        info: CallInfo,
        explicitReceiverKind: ExplicitReceiverKind,
        group: TowerGroup,
        towerLevel: TowerScopeLevel
    ): ProcessResult {
        processResult = ProcessResult.SCOPE_EMPTY
        konst processor =
            TowerScopeLevelProcessor(
                info,
                explicitReceiverKind,
                collector,
                candidateFactory,
                group
            )

        when (info.callKind) {
            CallKind.VariableAccess -> {
                processResult += towerLevel.processPropertiesByName(info, processor)

                // Top-level properties win over objects. Therefore, if we find properties, we don't want to look for objects here.
                // However, this only applies if the best current candidate applicability has shouldStopResolve == true. Exceptions to this
                // are candidates from dynamic scopes or properties with @LowPriorityInOverloadResolution (from earlier or the same level),
                // therefore we check for collector.shouldStopResolve and not collector.isSuccess.
                if (!collector.shouldStopResolve && towerLevel is ScopeTowerLevel && !towerLevel.areThereExtensionReceiverOptions()) {
                    processResult += towerLevel.processObjectsByName(info, processor)
                }
            }
            CallKind.Function -> {
                processResult += towerLevel.processFunctionsByName(info, processor)
            }
            CallKind.CallableReference -> {
                processResult += towerLevel.processFunctionsByName(info, processor)
                processResult += towerLevel.processPropertiesByName(info, processor)
            }
            else -> {
                throw AssertionError("Unsupported call kind in tower resolver: ${info.callKind}")
            }
        }
        return processResult
    }
}

private class TowerScopeLevelProcessor(
    konst callInfo: CallInfo,
    konst explicitReceiverKind: ExplicitReceiverKind,
    konst resultCollector: CandidateCollector,
    konst candidateFactory: CandidateFactory,
    konst group: TowerGroup
) : TowerScopeLevel.TowerScopeLevelProcessor<FirBasedSymbol<*>> {
    override fun consumeCandidate(
        symbol: FirBasedSymbol<*>,
        dispatchReceiverValue: ReceiverValue?,
        givenExtensionReceiverOptions: List<ReceiverValue>,
        scope: FirScope,
        objectsByName: Boolean,
        isFromOriginalTypeInPresenceOfSmartCast: Boolean
    ) {
        resultCollector.consumeCandidate(
            group, candidateFactory.createCandidate(
                callInfo,
                symbol,
                explicitReceiverKind,
                scope,
                dispatchReceiverValue,
                givenExtensionReceiverOptions,
                objectsByName,
                isFromOriginalTypeInPresenceOfSmartCast
            ), candidateFactory.context
        )
    }

    companion object {
        konst defaultPackage = Name.identifier("kotlin")
    }
}
