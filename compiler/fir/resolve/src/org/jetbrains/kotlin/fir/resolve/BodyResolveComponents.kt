/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve

import org.jetbrains.kotlin.fir.FirCallResolver
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.resolve.calls.ResolutionStageRunner
import org.jetbrains.kotlin.fir.resolve.dfa.FirDataFlowAnalyzer
import org.jetbrains.kotlin.fir.resolve.inference.FirCallCompleter
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProvider
import org.jetbrains.kotlin.fir.resolve.transformers.FirSyntheticCallGenerator
import org.jetbrains.kotlin.fir.resolve.transformers.IntegerLiteralAndOperatorApproximationTransformer
import org.jetbrains.kotlin.fir.resolve.transformers.ReturnTypeCalculator
import org.jetbrains.kotlin.fir.scopes.FirScope
import org.jetbrains.kotlin.fir.types.FirTypeRef

data class SessionHolderImpl(override konst session: FirSession, override konst scopeSession: ScopeSession) : SessionHolder {
    companion object {
        fun createWithEmptyScopeSession(session: FirSession): SessionHolderImpl = SessionHolderImpl(session, ScopeSession())
    }
}

abstract class BodyResolveComponents : SessionHolder {
    abstract konst returnTypeCalculator: ReturnTypeCalculator
    abstract konst implicitReceiverStack: ImplicitReceiverStack
    abstract konst containingDeclarations: List<FirDeclaration>
    abstract konst fileImportsScope: List<FirScope>
    abstract konst towerDataElements: List<FirTowerDataElement>
    abstract konst towerDataContext: FirTowerDataContext
    abstract konst localScopes: FirLocalScopes
    abstract konst noExpectedType: FirTypeRef
    abstract konst symbolProvider: FirSymbolProvider
    abstract konst file: FirFile
    abstract konst container: FirDeclaration
    abstract konst resolutionStageRunner: ResolutionStageRunner
    abstract konst samResolver: FirSamResolver
    abstract konst callResolver: FirCallResolver
    abstract konst callCompleter: FirCallCompleter
    abstract konst doubleColonExpressionResolver: FirDoubleColonExpressionResolver
    abstract konst syntheticCallGenerator: FirSyntheticCallGenerator
    abstract konst dataFlowAnalyzer: FirDataFlowAnalyzer
    abstract konst outerClassManager: FirOuterClassManager
    abstract konst integerLiteralAndOperatorApproximationTransformer: IntegerLiteralAndOperatorApproximationTransformer
}

// --------------------------------------- Utils ---------------------------------------


fun BodyResolveComponents.createCurrentScopeList(): List<FirScope> =
    towerDataElements.asReversed().mapNotNull { it.scope }
