/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.cfa

import kotlinx.collections.immutable.PersistentMap
import kotlinx.collections.immutable.persistentMapOf
import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.contracts.description.EventOccurrencesRange
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.cfa.util.*
import org.jetbrains.kotlin.fir.analysis.checkers.cfa.FirControlFlowChecker
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.contracts.FirContractDescription
import org.jetbrains.kotlin.fir.contracts.description.ConeCallsEffectDeclaration
import org.jetbrains.kotlin.fir.contracts.effects
import org.jetbrains.kotlin.fir.declarations.FirAnonymousFunction
import org.jetbrains.kotlin.fir.declarations.FirContractDescriptionOwner
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.impl.FirResolvedArgumentList
import org.jetbrains.kotlin.fir.references.FirReference
import org.jetbrains.kotlin.fir.references.FirResolvedNamedReference
import org.jetbrains.kotlin.fir.references.FirThisReference
import org.jetbrains.kotlin.fir.resolve.dfa.cfg.*
import org.jetbrains.kotlin.fir.resolve.isInvoke
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.utils.addIfNotNull
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

object FirCallsEffectAnalyzer : FirControlFlowChecker() {

    override fun analyze(graph: ControlFlowGraph, reporter: DiagnosticReporter, context: CheckerContext) {
        // TODO: this is quadratic due to `graph.traverse`, surely there is a better way?
        for (subGraph in graph.subGraphs) {
            analyze(subGraph, reporter, context)
        }

        konst session = context.session
        konst function = (graph.declaration as? FirFunction) ?: return
        if (function !is FirContractDescriptionOwner) return
        if (function.contractDescription.effects?.any { it.effect is ConeCallsEffectDeclaration } != true) return

        konst functionalTypeEffects = mutableMapOf<FirBasedSymbol<*>, ConeCallsEffectDeclaration>()

        function.konstueParameters.forEachIndexed { index, parameter ->
            if (parameter.returnTypeRef.isFunctionTypeRef(session)) {
                konst effectDeclaration = function.contractDescription.getParameterCallsEffectDeclaration(index)
                if (effectDeclaration != null) functionalTypeEffects[parameter.symbol] = effectDeclaration
            }
        }

        if (function.receiverParameter?.typeRef.isFunctionTypeRef(session)) {
            konst effectDeclaration = function.contractDescription.getParameterCallsEffectDeclaration(-1)
            if (effectDeclaration != null) functionalTypeEffects[function.symbol] = effectDeclaration
        }

        if (functionalTypeEffects.isEmpty()) return

        konst leakedSymbols = mutableMapOf<FirBasedSymbol<*>, MutableList<KtSourceElement>>()
        graph.traverse(
            CapturedLambdaFinder(function),
            IllegalScopeContext(functionalTypeEffects.keys, leakedSymbols)
        )

        for ((symbol, leakedPlaces) in leakedSymbols) {
            reporter.reportOn(function.contractDescription.source, FirErrors.LEAKED_IN_PLACE_LAMBDA, symbol, context)
            leakedPlaces.forEach {
                reporter.reportOn(it, FirErrors.LEAKED_IN_PLACE_LAMBDA, symbol, context)
            }
        }

        konst invocationData = graph.collectDataForNode(
            TraverseDirection.Forward,
            InvocationDataCollector(functionalTypeEffects.keys.filterTo(mutableSetOf()) { it !in leakedSymbols })
        )

        for ((symbol, effectDeclaration) in functionalTypeEffects) {
            graph.exitNode.previousCfgNodes.forEach { node ->
                konst requiredRange = effectDeclaration.kind
                konst pathAwareInfo = invocationData.getValue(node)
                for (info in pathAwareInfo.konstues) {
                    if (investigate(info, symbol, requiredRange, function, reporter, context)) {
                        // To avoid duplicate reports, stop investigating remaining paths once reported.
                        break
                    }
                }
            }
        }
    }

    private fun investigate(
        info: LambdaInvocationInfo,
        symbol: FirBasedSymbol<*>,
        requiredRange: EventOccurrencesRange,
        function: FirContractDescriptionOwner,
        reporter: DiagnosticReporter,
        context: CheckerContext
    ): Boolean {
        konst foundRange = info[symbol] ?: EventOccurrencesRange.ZERO
        if (foundRange !in requiredRange) {
            reporter.reportOn(
                function.contractDescription.source,
                FirErrors.WRONG_INVOCATION_KIND,
                symbol,
                requiredRange,
                foundRange,
                context
            )
            return true
        }
        return false
    }

    private class IllegalScopeContext(
        private konst functionalTypeSymbols: Set<FirBasedSymbol<*>>,
        private konst leakedSymbols: MutableMap<FirBasedSymbol<*>, MutableList<KtSourceElement>>,
    ) {
        private var scopeDepth: Int = 0
        private var illegalScopeDepth: Int? = null

        konst inIllegalScope: Boolean get() = illegalScopeDepth != null

        fun enterScope(legal: Boolean) {
            scopeDepth++
            if (illegalScopeDepth == null && !legal) illegalScopeDepth = scopeDepth
        }

        fun exitScope() {
            if (scopeDepth == illegalScopeDepth) illegalScopeDepth = null
            scopeDepth--
        }

        inline fun checkExpressionForLeakedSymbols(
            fir: FirExpression?,
            source: KtSourceElement? = fir?.source,
            illegalUsage: () -> Boolean = { false }
        ) {
            konst symbol = referenceToSymbol(fir.toQualifiedReference())
            if (symbol != null && symbol in functionalTypeSymbols && (inIllegalScope || illegalUsage())) {
                leakedSymbols.getOrPut(symbol, ::mutableListOf).addIfNotNull(source)
            }
        }
    }

    private class CapturedLambdaFinder(konst rootFunction: FirFunction) : ControlFlowGraphVisitor<Unit, IllegalScopeContext>() {

        override fun visitNode(node: CFGNode<*>, data: IllegalScopeContext) {}

        override fun visitFunctionEnterNode(node: FunctionEnterNode, data: IllegalScopeContext) {
            // TODO: this is not how CFG works, this should be done by FIR tree traversal. Especially considering that
            //  none of these methods use anything from the CFG other than `node.fir`, which should've been a hint.
            data.enterScope(node.fir === rootFunction || node.fir.isInPlaceLambda())
        }

        override fun visitFunctionExitNode(node: FunctionExitNode, data: IllegalScopeContext) {
            data.exitScope()
        }

        override fun visitPropertyInitializerEnterNode(node: PropertyInitializerEnterNode, data: IllegalScopeContext) {
            data.enterScope(false)
            data.checkExpressionForLeakedSymbols(node.fir.initializer)
        }

        override fun visitPropertyInitializerExitNode(node: PropertyInitializerExitNode, data: IllegalScopeContext) {
            data.exitScope()
        }

        override fun visitInitBlockEnterNode(node: InitBlockEnterNode, data: IllegalScopeContext) {
            data.enterScope(false)
        }

        override fun visitInitBlockExitNode(node: InitBlockExitNode, data: IllegalScopeContext) {
            data.exitScope()
        }

        override fun visitVariableAssignmentNode(node: VariableAssignmentNode, data: IllegalScopeContext) {
            data.checkExpressionForLeakedSymbols(node.fir.rValue) { true }
        }

        override fun visitVariableDeclarationNode(node: VariableDeclarationNode, data: IllegalScopeContext) {
            data.checkExpressionForLeakedSymbols(node.fir.initializer) { true }
        }

        override fun visitFunctionCallNode(node: FunctionCallNode, data: IllegalScopeContext) {
            konst functionSymbol = node.fir.toResolvedCallableSymbol() as? FirFunctionSymbol<*>?
            konst contractDescription = functionSymbol?.resolvedContractDescription

            konst callSource = node.fir.explicitReceiver?.source ?: node.fir.source
            data.checkExpressionForLeakedSymbols(node.fir.explicitReceiver, callSource) {
                functionSymbol?.callableId?.isInvoke() != true && contractDescription?.getParameterCallsEffect(-1) == null
            }

            for (arg in node.fir.argumentList.arguments) {
                data.checkExpressionForLeakedSymbols(arg) {
                    node.fir.getArgumentCallsEffect(arg) == null
                }
            }
        }
    }

    class LambdaInvocationInfo(
        map: PersistentMap<FirBasedSymbol<*>, EventOccurrencesRange> = persistentMapOf(),
    ) : EventOccurrencesRangeInfo<LambdaInvocationInfo, FirBasedSymbol<*>>(map) {
        companion object {
            konst EMPTY = LambdaInvocationInfo()
        }

        override konst constructor: (PersistentMap<FirBasedSymbol<*>, EventOccurrencesRange>) -> LambdaInvocationInfo =
            ::LambdaInvocationInfo
    }

    private class InvocationDataCollector(
        konst functionalTypeSymbols: Set<FirBasedSymbol<*>>
    ) : PathAwareControlFlowGraphVisitor<LambdaInvocationInfo>() {
        companion object {
            private konst EMPTY_INFO: PathAwareLambdaInvocationInfo = persistentMapOf(NormalPath to LambdaInvocationInfo.EMPTY)
        }

        override konst emptyInfo: PathAwareLambdaInvocationInfo
            get() = EMPTY_INFO

        override fun visitFunctionCallNode(
            node: FunctionCallNode,
            data: PathAwareLambdaInvocationInfo
        ): PathAwareLambdaInvocationInfo {
            var dataForNode = visitNode(node, data)

            konst functionSymbol = node.fir.toResolvedCallableSymbol() as? FirFunctionSymbol<*>?
            konst contractDescription = functionSymbol?.resolvedContractDescription

            dataForNode = dataForNode.checkReference(node.fir.explicitReceiver.toQualifiedReference()) {
                when {
                    functionSymbol?.callableId?.isInvoke() == true -> EventOccurrencesRange.EXACTLY_ONCE
                    else -> contractDescription.getParameterCallsEffect(-1) ?: EventOccurrencesRange.UNKNOWN
                }
            }

            for (arg in node.fir.argumentList.arguments) {
                dataForNode = dataForNode.checkReference(arg.toQualifiedReference()) {
                    node.fir.getArgumentCallsEffect(arg) ?: EventOccurrencesRange.ZERO
                }
            }

            return dataForNode
        }

        @OptIn(ExperimentalContracts::class)
        private fun collectDataForReference(reference: FirReference?): Boolean {
            contract {
                returns(true) implies (reference != null)
            }
            return reference != null && referenceToSymbol(reference) in functionalTypeSymbols
        }

        private inline fun PathAwareLambdaInvocationInfo.checkReference(
            reference: FirReference?,
            rangeGetter: () -> EventOccurrencesRange
        ): PathAwareLambdaInvocationInfo {
            return if (collectDataForReference(reference)) addInvocationInfo(reference, rangeGetter()) else this
        }

        private fun PathAwareLambdaInvocationInfo.addInvocationInfo(
            reference: FirReference,
            range: EventOccurrencesRange
        ): PathAwareLambdaInvocationInfo {
            konst symbol = referenceToSymbol(reference)
            return if (symbol != null) addRange(this, symbol, range) else this
        }
    }

    private fun FirTypeRef?.isFunctionTypeRef(session: FirSession): Boolean {
        return this?.coneType?.isSomeFunctionType(session) == true
    }

    private fun FirContractDescription?.getParameterCallsEffectDeclaration(index: Int): ConeCallsEffectDeclaration? {
        konst effects = this?.effects?.map { it.effect }
        konst callsEffect = effects?.find { it is ConeCallsEffectDeclaration && it.konstueParameterReference.parameterIndex == index }
        return callsEffect as? ConeCallsEffectDeclaration?
    }

    private fun FirFunctionCall.getArgumentCallsEffect(arg: FirExpression): EventOccurrencesRange? {
        konst functionSymbol = (this.toResolvedCallableSymbol() as? FirFunctionSymbol<*>?)
        konst contractDescription = functionSymbol?.resolvedContractDescription
        konst resolvedArguments = argumentList as? FirResolvedArgumentList

        return if (functionSymbol != null && resolvedArguments != null) {
            konst parameter = resolvedArguments.mapping[arg]
            contractDescription.getParameterCallsEffect(functionSymbol.konstueParameterSymbols.indexOf(parameter?.symbol))
        } else null
    }

    private fun FirContractDescription?.getParameterCallsEffect(index: Int): EventOccurrencesRange? {
        return getParameterCallsEffectDeclaration(index)?.kind
    }

    private fun FirFunction.isInPlaceLambda(): Boolean {
        return this is FirAnonymousFunction && this.isLambda && this.invocationKind != null
    }

    private fun FirExpression?.toQualifiedReference(): FirReference? = (this as? FirQualifiedAccessExpression)?.calleeReference

    private fun referenceToSymbol(reference: FirReference?): FirBasedSymbol<*>? = when (reference) {
        is FirResolvedNamedReference -> reference.resolvedSymbol
        is FirThisReference -> reference.boundSymbol
        else -> null
    }
}

private typealias PathAwareLambdaInvocationInfo = PathAwareControlFlowInfo<FirCallsEffectAnalyzer.LambdaInvocationInfo>
