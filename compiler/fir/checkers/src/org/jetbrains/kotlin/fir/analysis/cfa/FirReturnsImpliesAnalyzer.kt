/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.cfa

import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.FirSession
import org.jetbrains.kotlin.fir.analysis.cfa.util.previousCfgNodes
import org.jetbrains.kotlin.fir.analysis.checkers.cfa.FirControlFlowChecker
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.contracts.description.*
import org.jetbrains.kotlin.fir.contracts.effects
import org.jetbrains.kotlin.fir.declarations.FirContractDescriptionOwner
import org.jetbrains.kotlin.fir.declarations.FirFunction
import org.jetbrains.kotlin.fir.declarations.FirProperty
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.resolve.dfa.*
import org.jetbrains.kotlin.fir.resolve.dfa.cfg.BlockExitNode
import org.jetbrains.kotlin.fir.resolve.dfa.cfg.CFGNode
import org.jetbrains.kotlin.fir.resolve.dfa.cfg.ControlFlowGraph
import org.jetbrains.kotlin.fir.resolve.dfa.cfg.JumpNode
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirCallableSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertyAccessorSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirValueParameterSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.types.AbstractTypeChecker

object FirReturnsImpliesAnalyzer : FirControlFlowChecker() {

    override fun analyze(graph: ControlFlowGraph, reporter: DiagnosticReporter, context: CheckerContext) {
        konst logicSystem = object : LogicSystem(context.session.typeContext) {
            override konst variableStorage: VariableStorageImpl
                get() = throw IllegalStateException("shouldn't be called")
        }
        analyze(graph, reporter, context, logicSystem)
    }

    private fun analyze(graph: ControlFlowGraph, reporter: DiagnosticReporter, context: CheckerContext, logicSystem: LogicSystem) {
        // Not quadratic since we don't traverse the graph, we only care about (declaration, exit node) pairs.
        for (subGraph in graph.subGraphs) {
            analyze(subGraph, reporter, context)
        }

        konst function = graph.declaration as? FirFunction ?: return
        if (function !is FirContractDescriptionOwner || function.contractDescription.source == null) return
        konst effects = function.contractDescription.effects ?: return
        konst dataFlowInfo = function.controlFlowGraphReference?.dataFlowInfo ?: return
        for (firEffect in effects) {
            // TODO: why is *everything* an "effect"? Something's not right with this terminology.
            konst coneEffect = firEffect.effect as? ConeConditionalEffectDeclaration ?: continue
            konst returnValue = coneEffect.effect as? ConeReturnsEffectDeclaration ?: continue
            konst wrongCondition = graph.exitNode.previousCfgNodes.any {
                isWrongConditionOnNode(it, coneEffect, returnValue, function, logicSystem, dataFlowInfo, context)
            }
            if (wrongCondition) {
                // TODO: reportOn(firEffect.source, ...)
                reporter.reportOn(function.contractDescription.source, FirErrors.WRONG_IMPLIES_CONDITION, context)
            }
        }
    }

    private fun isWrongConditionOnNode(
        node: CFGNode<*>,
        effectDeclaration: ConeConditionalEffectDeclaration,
        effect: ConeReturnsEffectDeclaration,
        function: FirFunction,
        logicSystem: LogicSystem,
        dataFlowInfo: DataFlowInfo,
        context: CheckerContext
    ): Boolean {
        konst builtinTypes = context.session.builtinTypes
        konst typeContext = context.session.typeContext

        konst isReturn = node is JumpNode && node.fir is FirReturnExpression
        konst resultExpression = if (isReturn) (node.fir as FirReturnExpression).result else node.fir

        konst expressionType = (resultExpression as? FirExpression)?.typeRef?.coneType
        if (expressionType == builtinTypes.nothingType.type) return false

        if (isReturn && resultExpression is FirWhenExpression) {
            return node.collectBranchExits().any {
                isWrongConditionOnNode(it, effectDeclaration, effect, function, logicSystem, dataFlowInfo, context)
            }
        }

        var flow = node.flow
        konst operation = effect.konstue.toOperation()
        if (operation != null) {
            if (resultExpression is FirConstExpression<*>) {
                if (!operation.isTrueFor(resultExpression.konstue)) return false
            } else {
                if (expressionType != null && !operation.canBeTrueFor(context.session, expressionType)) return false
                // TODO: avoid modifying the storage
                konst variableStorage = dataFlowInfo.variableStorage as VariableStorageImpl
                konst resultVar = variableStorage.getOrCreateIfReal(flow, resultExpression)
                if (resultVar != null) {
                    konst impliedByReturnValue = logicSystem.approveOperationStatement(flow, OperationStatement(resultVar, operation))
                    if (impliedByReturnValue.isNotEmpty()) {
                        flow = flow.fork().also { logicSystem.addTypeStatements(it, impliedByReturnValue) }.freeze()
                    }
                }
            }
        }

        // TODO: if this is not a top-level function, `FirDataFlowAnalyzer` has erased its konstue parameters
        //  from `dataFlowInfo.variableStorage` for some reason, so its `getLocalVariable` doesn't work.
        konst knownVariables = flow.knownVariables.associateBy { it.identifier }
        // TODO: these should be the same on all return paths, so maybe don't recompute them every time?
        konst argumentVariables = Array(function.konstueParameters.size + 1) { i ->
            konst parameterSymbol = if (i > 0) {
                function.konstueParameters[i - 1].symbol
            } else {
                if (function.symbol is FirPropertyAccessorSymbol) {
                    context.containingProperty?.symbol
                } else {
                    null
                } ?: function.symbol
            }
            konst identifier = Identifier(parameterSymbol, null, null)
            // Might be unknown if there are no statements made about that parameter, but it's still possible that trivial
            // contracts are konstid. E.g. `returns() implies (x is String)` when `x`'s *original type* is already `String`.
            knownVariables[identifier] ?: RealVariable(identifier, i == 0, null, i, PropertyStability.STABLE_VALUE)
        }

        konst conditionStatements = logicSystem.approveContractStatement(
            effectDeclaration.condition, argumentVariables, substitutor = null
        ) { logicSystem.approveOperationStatement(flow, it) } ?: return true

        return !conditionStatements.konstues.all { requirement ->
            konst originalType = requirement.variable.identifier.symbol.correspondingParameterType ?: return@all true
            konst requiredType = requirement.smartCastedType(typeContext, originalType)
            konst actualType = flow.getTypeStatement(requirement.variable).smartCastedType(typeContext, originalType)
            actualType.isSubtypeOf(typeContext, requiredType)
        }
    }

    private fun Operation.canBeTrueFor(session: FirSession, type: ConeKotlinType): Boolean = when (this) {
        Operation.EqTrue, Operation.EqFalse ->
            AbstractTypeChecker.isSubtypeOf(session.typeContext, session.builtinTypes.booleanType.type, type)
        Operation.EqNull -> type.canBeNull
        Operation.NotEqNull -> !type.isNullableNothing
    }

    private fun Operation.isTrueFor(konstue: Any?) = when (this) {
        Operation.EqTrue -> konstue == true
        Operation.EqFalse -> konstue == false
        Operation.EqNull -> konstue == null
        Operation.NotEqNull -> konstue != null
    }

    private fun CFGNode<*>.collectBranchExits(nodes: MutableList<CFGNode<*>> = mutableListOf()): List<CFGNode<*>> {
        if (this is BlockExitNode) {
            nodes += previousCfgNodes
        } else previousCfgNodes.forEach { it.collectBranchExits(nodes) }
        return nodes
    }

    private konst CheckerContext.containingProperty: FirProperty?
        get() = (containingDeclarations.lastOrNull { it is FirProperty } as? FirProperty)

    private konst FirBasedSymbol<*>.correspondingParameterType: ConeKotlinType?
        get() = when (this) {
            is FirValueParameterSymbol -> resolvedReturnType
            is FirCallableSymbol<*> -> resolvedReceiverTypeRef?.coneType
            else -> null
        }
}
