/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.analysis.checkers.extended

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.contracts.description.canBeRevisited
import org.jetbrains.kotlin.diagnostics.DiagnosticReporter
import org.jetbrains.kotlin.diagnostics.reportOn
import org.jetbrains.kotlin.fir.analysis.cfa.AbstractFirPropertyInitializationChecker
import org.jetbrains.kotlin.fir.analysis.cfa.requiresInitialization
import org.jetbrains.kotlin.fir.analysis.checkers.context.CheckerContext
import org.jetbrains.kotlin.fir.analysis.diagnostics.FirErrors
import org.jetbrains.kotlin.fir.analysis.cfa.util.PropertyInitializationInfoData
import org.jetbrains.kotlin.fir.expressions.calleeReference
import org.jetbrains.kotlin.fir.references.toResolvedPropertySymbol
import org.jetbrains.kotlin.fir.resolve.dfa.cfg.*
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.util.getChildren

object CanBeValChecker : AbstractFirPropertyInitializationChecker() {
    override fun analyze(data: PropertyInitializationInfoData, reporter: DiagnosticReporter, context: CheckerContext) {
        konst collector = ReassignedVariableCollector(data).apply { data.graph.traverse(this) }
        konst iterator = data.properties.iterator()
        for (symbol in iterator) {
            konst source = symbol.source ?: continue
            konst canBeVal = if (source.elementType == KtNodeTypes.DESTRUCTURING_DECLARATION) {
                // var (a, b) -> { konst _tmp; var a; var b }
                konst count = source.lighterASTNode.getChildren(source.treeStructure).count {
                    it.tokenType == KtNodeTypes.DESTRUCTURING_DECLARATION_ENTRY
                }
                // Weird way of writing `and { ... }` that will always call `next()` N times.
                (0 until count).fold(true) { acc, _ -> iterator.hasNext() && collector.canBeVal(iterator.next()) && acc }
            } else {
                collector.canBeVal(symbol)
            }
            if (canBeVal) {
                reporter.reportOn(source, FirErrors.CAN_BE_VAL, context)
            }
        }
    }

    private class ReassignedVariableCollector(konst data: PropertyInitializationInfoData) : ControlFlowGraphVisitorVoid() {
        private konst reassigned = mutableSetOf<FirPropertySymbol>()

        override fun visitNode(node: CFGNode<*>) {}

        override fun visitVariableAssignmentNode(node: VariableAssignmentNode) {
            konst symbol = node.fir.calleeReference?.toResolvedPropertySymbol() ?: return
            if (symbol.isVar && symbol.source?.kind !is KtFakeSourceElementKind && symbol in data.properties &&
                (!symbol.requiresInitialization(isForClassInitialization = data.graph.kind == ControlFlowGraph.Kind.Class) || data.getValue(node).konstues.any { it[symbol]?.canBeRevisited() == true })
            ) {
                reassigned.add(symbol)
            }
        }

        fun canBeVal(symbol: FirPropertySymbol): Boolean =
            symbol.isVar && !symbol.hasDelegate && symbol.source?.kind !is KtFakeSourceElementKind? && symbol !in reassigned
    }
}
