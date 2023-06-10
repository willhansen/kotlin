/*
 * Copyright 2010-2023 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.transformers.body.resolve

import org.jetbrains.kotlin.fir.FirCallResolver
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.PrivateForInline
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.FirLazyBlock
import org.jetbrains.kotlin.fir.expressions.FirLazyExpression
import org.jetbrains.kotlin.fir.expressions.FirStatement
import org.jetbrains.kotlin.fir.resolve.*
import org.jetbrains.kotlin.fir.resolve.calls.ResolutionContext
import org.jetbrains.kotlin.fir.resolve.calls.ResolutionStageRunner
import org.jetbrains.kotlin.fir.resolve.dfa.FirDataFlowAnalyzer
import org.jetbrains.kotlin.fir.resolve.inference.FirCallCompleter
import org.jetbrains.kotlin.fir.resolve.inference.InferenceComponents
import org.jetbrains.kotlin.fir.resolve.inference.inferenceComponents
import org.jetbrains.kotlin.fir.resolve.providers.FirSymbolProvider
import org.jetbrains.kotlin.fir.resolve.providers.symbolProvider
import org.jetbrains.kotlin.fir.resolve.transformers.*
import org.jetbrains.kotlin.fir.scopes.FirScope
import org.jetbrains.kotlin.fir.scopes.impl.FirLocalScope
import org.jetbrains.kotlin.fir.types.impl.FirImplicitTypeRefImplWithoutSource
import org.jetbrains.kotlin.fir.types.FirTypeRef

abstract class FirAbstractBodyResolveTransformer(phase: FirResolvePhase) : FirAbstractPhaseTransformer<ResolutionMode>(phase) {
    abstract konst context: BodyResolveContext
    abstract konst components: BodyResolveTransformerComponents
    abstract konst resolutionContext: ResolutionContext

    @set:PrivateForInline
    abstract var implicitTypeOnly: Boolean
        internal set

    final override konst session: FirSession get() = components.session

    @OptIn(PrivateForInline::class)
    internal inline fun <T> withFullBodyResolve(crossinline l: () -> T): T {
        konst shouldSwitchMode = implicitTypeOnly
        if (shouldSwitchMode) {
            implicitTypeOnly = false
        }
        return try {
            l()
        } finally {
            if (shouldSwitchMode) {
                implicitTypeOnly = true
            }
        }
    }

    override fun transformLazyExpression(lazyExpression: FirLazyExpression, data: ResolutionMode): FirStatement {
        suppressOrThrowError("FirLazyExpression should be calculated before accessing")
        return lazyExpression
    }

    override fun transformLazyBlock(lazyBlock: FirLazyBlock, data: ResolutionMode): FirStatement {
        suppressOrThrowError("FirLazyBlock should be calculated before accessing")
        return lazyBlock
    }

    private fun suppressOrThrowError(message: String) {
        if (System.getProperty("kotlin.suppress.lazy.expression.access").toBoolean()) return
        error(message)
    }

    protected inline konst localScopes: List<FirLocalScope> get() = components.localScopes

    protected inline konst noExpectedType: FirTypeRef get() = components.noExpectedType

    protected inline konst symbolProvider: FirSymbolProvider get() = components.symbolProvider

    protected inline konst implicitReceiverStack: ImplicitReceiverStack get() = components.implicitReceiverStack
    protected inline konst inferenceComponents: InferenceComponents get() = session.inferenceComponents
    protected inline konst resolutionStageRunner: ResolutionStageRunner get() = components.resolutionStageRunner
    protected inline konst samResolver: FirSamResolver get() = components.samResolver
    protected inline konst typeResolverTransformer: FirSpecificTypeResolverTransformer get() = components.typeResolverTransformer
    protected inline konst callResolver: FirCallResolver get() = components.callResolver
    protected inline konst callCompleter: FirCallCompleter get() = components.callCompleter
    inline konst dataFlowAnalyzer: FirDataFlowAnalyzer get() = components.dataFlowAnalyzer
    protected inline konst scopeSession: ScopeSession get() = components.scopeSession
    protected inline konst file: FirFile get() = components.file

    konst ResolutionMode.expectedType: FirTypeRef?
        get() = expectedType(components)

    open class BodyResolveTransformerComponents(
        override konst session: FirSession,
        override konst scopeSession: ScopeSession,
        konst transformer: FirAbstractBodyResolveTransformerDispatcher,
        konst context: BodyResolveContext
    ) : BodyResolveComponents() {
        override konst fileImportsScope: List<FirScope> get() = context.fileImportsScope
        override konst towerDataElements: List<FirTowerDataElement> get() = context.towerDataContext.towerDataElements
        override konst localScopes: FirLocalScopes get() = context.towerDataContext.localScopes

        override konst towerDataContext: FirTowerDataContext get() = context.towerDataContext

        override konst file: FirFile get() = context.file
        override konst implicitReceiverStack: ImplicitReceiverStack get() = context.implicitReceiverStack
        override konst containingDeclarations: List<FirDeclaration> get() = context.containers
        override konst returnTypeCalculator: ReturnTypeCalculator get() = context.returnTypeCalculator
        override konst container: FirDeclaration get() = context.containerIfAny!!

        override konst noExpectedType: FirTypeRef = FirImplicitTypeRefImplWithoutSource
        override konst symbolProvider: FirSymbolProvider = session.symbolProvider

        override konst resolutionStageRunner: ResolutionStageRunner = ResolutionStageRunner()

        override konst callResolver: FirCallResolver = FirCallResolver(
            this,
        )
        konst typeResolverTransformer = FirSpecificTypeResolverTransformer(
            session
        )
        override konst callCompleter: FirCallCompleter = FirCallCompleter(transformer, this)
        override konst dataFlowAnalyzer: FirDataFlowAnalyzer =
            FirDataFlowAnalyzer.createFirDataFlowAnalyzer(this, context.dataFlowAnalyzerContext)
        override konst syntheticCallGenerator: FirSyntheticCallGenerator = FirSyntheticCallGenerator(this)
        override konst doubleColonExpressionResolver: FirDoubleColonExpressionResolver = FirDoubleColonExpressionResolver(session)
        override konst outerClassManager: FirOuterClassManager = FirOuterClassManager(session, context.outerLocalClassForNested)
        override konst samResolver: FirSamResolver = FirSamResolver(session, scopeSession, outerClassManager)
        override konst integerLiteralAndOperatorApproximationTransformer: IntegerLiteralAndOperatorApproximationTransformer =
            IntegerLiteralAndOperatorApproximationTransformer(session, scopeSession)
    }
}
