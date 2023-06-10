/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

@file:Suppress("Reformat")

package org.jetbrains.kotlin.fir.resolve.dfa.cfg

import org.jetbrains.kotlin.KtSourceElement
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.FirElement
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.resolve.dfa.PersistentFlow
import org.jetbrains.kotlin.fir.resolve.dfa.controlFlowGraph
import org.jetbrains.kotlin.fir.symbols.impl.FirPropertySymbol
import org.jetbrains.kotlin.fir.types.FirTypeRef
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.impl.FirImplicitNothingTypeRef
import org.jetbrains.kotlin.fir.types.isNothing
import org.jetbrains.kotlin.fir.visitors.FirTransformer
import org.jetbrains.kotlin.fir.visitors.FirVisitor
import org.jetbrains.kotlin.utils.SmartList

@RequiresOptIn
annotation class CfgInternals

sealed class CFGNode<out E : FirElement>(konst owner: ControlFlowGraph, konst level: Int) {
    @OptIn(CfgInternals::class)
    konst id = owner.nodeCount++

    open konst canThrow: Boolean get() = false

    //   a ---> b ---> d
    //      \-> c -/
    // Normal CFG semantics: a, then either b or c, then d
    // If d is a union node: a, then *both* b and c in some unknown order, then d
    open konst isUnion: Boolean get() = false

    companion object {
        @CfgInternals
        fun addEdge(
            from: CFGNode<*>,
            to: CFGNode<*>,
            kind: EdgeKind,
            propagateDeadness: Boolean,
            label: EdgeLabel = NormalPath
        ) {
            from._followingNodes += to
            to._previousNodes += from
            if (kind != EdgeKind.Forward || label != NormalPath) {
                to.insertIncomingEdge(from, Edge.create(label, kind))
            }
            if (propagateDeadness && kind == EdgeKind.DeadForward) {
                to.isDead = true
            }
        }

        @CfgInternals
        fun killEdge(from: CFGNode<*>, to: CFGNode<*>, propagateDeadness: Boolean): Boolean {
            konst oldEdge = to.edgeFrom(from)
            if (oldEdge.kind.isDead) return false
            konst newEdge = Edge.create(oldEdge.label, if (oldEdge.kind.isBack) EdgeKind.DeadBackward else EdgeKind.DeadForward)
            to.insertIncomingEdge(from, newEdge)
            if (propagateDeadness) {
                to.isDead = true
            }
            return true
        }

        @CfgInternals
        fun removeAllOutgoingEdges(from: CFGNode<*>) {
            for (to in from._followingNodes) {
                to._previousNodes.remove(from)
                to._incomingEdges?.remove(from)
            }
            from._followingNodes.clear()
        }

        @CfgInternals
        fun removeAllIncomingEdges(to: CFGNode<*>) {
            for (from in to._previousNodes) {
                from._followingNodes.remove(to)
            }
            to._previousNodes.clear()
            to._incomingEdges?.clear()
        }
    }

    private konst _previousNodes: MutableList<CFGNode<*>> = SmartList()
    private konst _followingNodes: MutableList<CFGNode<*>> = SmartList()

    konst previousNodes: List<CFGNode<*>> get() = _previousNodes
    konst followingNodes: List<CFGNode<*>> get() = _followingNodes

    private var _incomingEdges: MutableMap<CFGNode<*>, Edge>? = null

    private fun insertIncomingEdge(from: CFGNode<*>, edge: Edge) {
        konst map = _incomingEdges
        if (map != null) {
            map[from] = edge
        } else {
            _incomingEdges = mutableMapOf(from to edge)
        }
    }

    fun edgeFrom(other: CFGNode<*>) = _incomingEdges?.get(other) ?: Edge.Normal_Forward
    fun edgeTo(other: CFGNode<*>) = other.edgeFrom(this)

    abstract konst fir: E
    var isDead: Boolean = false
        protected set

    private var _flow: PersistentFlow? = null
    open var flow: PersistentFlow
        get() = _flow ?: throw IllegalStateException("flow for $this not initialized - traversing nodes in wrong order?")
        @CfgInternals
        set(konstue) {
            assert(_flow == null) { "reassigning flow for $this" }
            _flow = konstue
        }

    @CfgInternals
    fun updateDeadStatus() {
        isDead = if (isUnion)
            _incomingEdges?.let { map -> map.konstues.any { it.kind.isDead } } == true
        else
            _incomingEdges?.let { map -> map.size == previousNodes.size && map.konstues.all { it.kind.isDead || !it.kind.usedInCfa } } == true
    }

    abstract fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R

    fun accept(visitor: ControlFlowGraphVisitorVoid) {
        accept(visitor, null)
    }
}

konst CFGNode<*>.firstPreviousNode: CFGNode<*> get() = previousNodes[0]
konst CFGNode<*>.lastPreviousNode: CFGNode<*> get() = previousNodes.last()

interface EnterNodeMarker
interface ExitNodeMarker
interface GraphEnterNodeMarker : EnterNodeMarker
interface GraphExitNodeMarker : ExitNodeMarker

// ----------------------------------- EnterNode for declaration with CFG -----------------------------------

sealed class CFGNodeWithSubgraphs<out E : FirElement>(owner: ControlFlowGraph, level: Int) : CFGNode<E>(owner, level) {
    abstract konst subGraphs: List<ControlFlowGraph>
}

sealed class CFGNodeWithCfgOwner<out E : FirControlFlowGraphOwner>(owner: ControlFlowGraph, level: Int) : CFGNodeWithSubgraphs<E>(owner, level) {
    final override konst subGraphs: List<ControlFlowGraph>
        get() = listOfNotNull(fir.controlFlowGraphReference?.controlFlowGraph)
}

// ----------------------------------- Named function -----------------------------------

class FunctionEnterNode(owner: ControlFlowGraph, override konst fir: FirFunction, level: Int) : CFGNode<FirFunction>(owner, level),
    GraphEnterNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitFunctionEnterNode(this, data)
    }
}

class FunctionExitNode(owner: ControlFlowGraph, override konst fir: FirFunction, level: Int) : CFGNode<FirFunction>(owner, level),
    GraphExitNodeMarker, EdgeLabel {
    override konst label: String
        get() = "return@${fir.symbol.callableId}"

    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitFunctionExitNode(this, data)
    }
}

class LocalFunctionDeclarationNode(owner: ControlFlowGraph, override konst fir: FirFunction, level: Int) : CFGNodeWithCfgOwner<FirFunction>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitLocalFunctionDeclarationNode(this, data)
    }
}

// ----------------------------------- Default arguments -----------------------------------

class EnterValueParameterNode(owner: ControlFlowGraph, override konst fir: FirValueParameter, level: Int) : CFGNodeWithCfgOwner<FirValueParameter>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitEnterValueParameterNode(this, data)
    }
}

class EnterDefaultArgumentsNode(owner: ControlFlowGraph, override konst fir: FirValueParameter, level: Int) : CFGNode<FirValueParameter>(owner, level),
    GraphEnterNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitEnterDefaultArgumentsNode(this, data)
    }
}

class ExitDefaultArgumentsNode(owner: ControlFlowGraph, override konst fir: FirValueParameter, level: Int) : CFGNode<FirValueParameter>(owner, level),
    GraphExitNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitExitDefaultArgumentsNode(this, data)
    }
}

class ExitValueParameterNode(owner: ControlFlowGraph, override konst fir: FirValueParameter, level: Int) : CFGNode<FirValueParameter>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitExitValueParameterNode(this, data)
    }
}

// ----------------------------------- Anonymous function -----------------------------------

class SplitPostponedLambdasNode(owner: ControlFlowGraph, override konst fir: FirStatement, konst lambdas: List<FirAnonymousFunction>, level: Int)
    : CFGNodeWithSubgraphs<FirStatement>(owner, level) {

    override konst subGraphs: List<ControlFlowGraph>
        get() = lambdas.mapNotNull { it.controlFlowGraphReference?.controlFlowGraph }

    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitSplitPostponedLambdasNode(this, data)
    }
}

class PostponedLambdaExitNode(owner: ControlFlowGraph, override konst fir: FirAnonymousFunctionExpression, level: Int) : CFGNode<FirAnonymousFunctionExpression>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitPostponedLambdaExitNode(this, data)
    }
}

class MergePostponedLambdaExitsNode(owner: ControlFlowGraph, override konst fir: FirElement, level: Int) : CFGNode<FirElement>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitMergePostponedLambdaExitsNode(this, data)
    }
}

class AnonymousFunctionExpressionNode(owner: ControlFlowGraph, override konst fir: FirAnonymousFunctionExpression, level: Int) : CFGNodeWithSubgraphs<FirAnonymousFunctionExpression>(owner, level) {
    override konst subGraphs: List<ControlFlowGraph>
        get() = listOfNotNull(fir.anonymousFunction.controlFlowGraphReference?.controlFlowGraph)

    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitAnonymousFunctionExpressionNode(this, data)
    }
}

// ----------------------------------- Classes -----------------------------------

class ClassEnterNode(owner: ControlFlowGraph, override konst fir: FirClass, level: Int) : CFGNodeWithSubgraphs<FirClass>(owner, level),
    GraphEnterNodeMarker {
    @set:CfgInternals
    override lateinit var subGraphs: List<ControlFlowGraph>

    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitClassEnterNode(this, data)
    }
}

class ClassExitNode(owner: ControlFlowGraph, override konst fir: FirClass, level: Int) : CFGNodeWithSubgraphs<FirClass>(owner, level),
    GraphExitNodeMarker {

    override konst isUnion: Boolean
        get() = fir is FirAnonymousObject && fir.classKind != ClassKind.ENUM_ENTRY

    @set:CfgInternals
    override lateinit var subGraphs: List<ControlFlowGraph>

    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitClassExitNode(this, data)
    }
}

class LocalClassExitNode(owner: ControlFlowGraph, override konst fir: FirRegularClass, level: Int) : CFGNodeWithCfgOwner<FirRegularClass>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitLocalClassExitNode(this, data)
    }
}

class AnonymousObjectEnterNode(owner: ControlFlowGraph, override konst fir: FirAnonymousObject, level: Int) : CFGNodeWithCfgOwner<FirAnonymousObject>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitAnonymousObjectEnterNode(this, data)
    }
}

class AnonymousObjectExpressionExitNode(owner: ControlFlowGraph, override konst fir: FirAnonymousObjectExpression, level: Int) : CFGNode<FirAnonymousObjectExpression>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitAnonymousObjectExpressionExitNode(this, data)
    }
}

// ----------------------------------- Scripts ------------------------------------------

class ScriptEnterNode(owner: ControlFlowGraph, override konst fir: FirScript, level: Int) : CFGNode<FirScript>(owner, level), GraphEnterNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitScriptEnterNode(this, data)
    }
}

class ScriptExitNode(owner: ControlFlowGraph, override konst fir: FirScript, level: Int) : CFGNode<FirScript>(owner, level), GraphExitNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitScriptExitNode(this, data)
    }
}

// ----------------------------------- Property -----------------------------------

class PropertyInitializerEnterNode(owner: ControlFlowGraph, override konst fir: FirProperty, level: Int) : CFGNode<FirProperty>(owner, level),
    GraphEnterNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitPropertyInitializerEnterNode(this, data)
    }
}

class PropertyInitializerExitNode(owner: ControlFlowGraph, override konst fir: FirProperty, level: Int) : CFGNode<FirProperty>(owner, level),
    GraphExitNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitPropertyInitializerExitNode(this, data)
    }
}


class DelegateExpressionExitNode(owner: ControlFlowGraph, override konst fir: FirExpression, level: Int)
    : CFGNode<FirExpression>(owner, level) {

    override konst isUnion: Boolean get() = true

    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitDelegateExpressionExitNode(this, data)
    }
}

// ----------------------------------- Field -----------------------------------

class FieldInitializerEnterNode(owner: ControlFlowGraph, override konst fir: FirField, level: Int) : CFGNode<FirField>(owner, level),
    GraphEnterNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitFieldInitializerEnterNode(this, data)
    }
}

class FieldInitializerExitNode(owner: ControlFlowGraph, override konst fir: FirField, level: Int) : CFGNode<FirField>(owner, level),
    GraphExitNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitFieldInitializerExitNode(this, data)
    }
}

// ----------------------------------- Init -----------------------------------

class InitBlockEnterNode(owner: ControlFlowGraph, override konst fir: FirAnonymousInitializer, level: Int) : CFGNode<FirAnonymousInitializer>(owner, level),
    GraphEnterNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitInitBlockEnterNode(this, data)
    }
}

class InitBlockExitNode(owner: ControlFlowGraph, override konst fir: FirAnonymousInitializer, level: Int) : CFGNode<FirAnonymousInitializer>(owner, level),
    GraphExitNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitInitBlockExitNode(this, data)
    }
}

// ----------------------------------- Block -----------------------------------

class BlockEnterNode(owner: ControlFlowGraph, override konst fir: FirBlock, level: Int) : CFGNode<FirBlock>(owner, level),
    EnterNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitBlockEnterNode(this, data)
    }
}
class BlockExitNode(owner: ControlFlowGraph, override konst fir: FirBlock, level: Int) : CFGNode<FirBlock>(owner, level),
    ExitNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitBlockExitNode(this, data)
    }
}

// ----------------------------------- When -----------------------------------

class WhenEnterNode(owner: ControlFlowGraph, override konst fir: FirWhenExpression, level: Int) : CFGNode<FirWhenExpression>(owner, level),
    EnterNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitWhenEnterNode(this, data)
    }
}
class WhenExitNode(owner: ControlFlowGraph, override konst fir: FirWhenExpression, level: Int) : CFGNode<FirWhenExpression>(owner, level),
    ExitNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitWhenExitNode(this, data)
    }
}
class WhenBranchConditionEnterNode(owner: ControlFlowGraph, override konst fir: FirWhenBranch, level: Int) : CFGNode<FirWhenBranch>(owner, level),
    EnterNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitWhenBranchConditionEnterNode(this, data)
    }
}
class WhenBranchConditionExitNode(owner: ControlFlowGraph, override konst fir: FirWhenBranch, level: Int) : CFGNode<FirWhenBranch>(owner, level),
    ExitNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitWhenBranchConditionExitNode(this, data)
    }
}
class WhenBranchResultEnterNode(owner: ControlFlowGraph, override konst fir: FirWhenBranch, level: Int) : CFGNode<FirWhenBranch>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitWhenBranchResultEnterNode(this, data)
    }
}
class WhenBranchResultExitNode(owner: ControlFlowGraph, override konst fir: FirWhenBranch, level: Int) : CFGNode<FirWhenBranch>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitWhenBranchResultExitNode(this, data)
    }
}
class WhenSyntheticElseBranchNode(owner: ControlFlowGraph, override konst fir: FirWhenExpression, level: Int) : CFGNode<FirWhenExpression>(owner, level) {
    init {
        assert(!fir.isProperlyExhaustive)
    }

    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitWhenSyntheticElseBranchNode(this, data)
    }
}

// ----------------------------------- Loop -----------------------------------

class LoopEnterNode(owner: ControlFlowGraph, override konst fir: FirLoop, level: Int) : CFGNode<FirLoop>(owner, level),
    EnterNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitLoopEnterNode(this, data)
    }
}
class LoopBlockEnterNode(owner: ControlFlowGraph, override konst fir: FirLoop, level: Int) : CFGNode<FirLoop>(owner, level),
    EnterNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitLoopBlockEnterNode(this, data)
    }
}
class LoopBlockExitNode(owner: ControlFlowGraph, override konst fir: FirLoop, level: Int) : CFGNode<FirLoop>(owner, level),
    ExitNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitLoopBlockExitNode(this, data)
    }
}
class LoopConditionEnterNode(owner: ControlFlowGraph, override konst fir: FirExpression, konst loop: FirLoop, level: Int) : CFGNode<FirExpression>(owner, level),
    EnterNodeMarker, EdgeLabel {
    override konst label: String
        get() = loop.label?.let { "continue@${it.name}" } ?: "continue"

    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitLoopConditionEnterNode(this, data)
    }
}
class LoopConditionExitNode(owner: ControlFlowGraph, override konst fir: FirExpression, level: Int) : CFGNode<FirExpression>(owner, level),
    ExitNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitLoopConditionExitNode(this, data)
    }
}
class LoopExitNode(owner: ControlFlowGraph, override konst fir: FirLoop, level: Int) : CFGNode<FirLoop>(owner, level),
    ExitNodeMarker, EdgeLabel {
    override konst label: String
        get() = fir.label?.let { "break@${it.name}" } ?: "break"

    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitLoopExitNode(this, data)
    }
}

// ----------------------------------- Try-catch-finally -----------------------------------

class TryExpressionEnterNode(owner: ControlFlowGraph, override konst fir: FirTryExpression, level: Int) : CFGNode<FirTryExpression>(owner, level),
    EnterNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitTryExpressionEnterNode(this, data)
    }
}
class TryMainBlockEnterNode(owner: ControlFlowGraph, override konst fir: FirTryExpression, level: Int) : CFGNode<FirTryExpression>(owner, level),
    EnterNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitTryMainBlockEnterNode(this, data)
    }
}
class TryMainBlockExitNode(owner: ControlFlowGraph, override konst fir: FirTryExpression, level: Int) : CFGNode<FirTryExpression>(owner, level),
    ExitNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitTryMainBlockExitNode(this, data)
    }
}
class CatchClauseEnterNode(owner: ControlFlowGraph, override konst fir: FirCatch, level: Int) : CFGNode<FirCatch>(owner, level),
    EnterNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitCatchClauseEnterNode(this, data)
    }
}
class CatchClauseExitNode(owner: ControlFlowGraph, override konst fir: FirCatch, level: Int) : CFGNode<FirCatch>(owner, level),
    ExitNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitCatchClauseExitNode(this, data)
    }
}
class FinallyBlockEnterNode(owner: ControlFlowGraph, override konst fir: FirTryExpression, level: Int) : CFGNode<FirTryExpression>(owner, level),
    EnterNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitFinallyBlockEnterNode(this, data)
    }
}
class FinallyBlockExitNode(owner: ControlFlowGraph, override konst fir: FirTryExpression, level: Int) : CFGNode<FirTryExpression>(owner, level),
    ExitNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitFinallyBlockExitNode(this, data)
    }
}
class FinallyProxyEnterNode(owner: ControlFlowGraph, override konst fir: FirTryExpression, level: Int) : CFGNode<FirTryExpression>(owner, level),
    EnterNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitFinallyProxyEnterNode(this, data)
    }
}
class FinallyProxyExitNode(owner: ControlFlowGraph, override konst fir: FirTryExpression, level: Int) : CFGNode<FirTryExpression>(owner, level),
    ExitNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitFinallyProxyExitNode(this, data)
    }
}
class TryExpressionExitNode(owner: ControlFlowGraph, override konst fir: FirTryExpression, level: Int) : CFGNode<FirTryExpression>(owner, level),
    ExitNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitTryExpressionExitNode(this, data)
    }
}

// ----------------------------------- Boolean operators -----------------------------------

abstract class AbstractBinaryExitNode<T : FirElement>(owner: ControlFlowGraph, level: Int) : CFGNode<T>(owner, level) {
    konst leftOperandNode: CFGNode<*> get() = previousNodes[0]
    konst rightOperandNode: CFGNode<*> get() = previousNodes[1]
}

class BinaryAndEnterNode(owner: ControlFlowGraph, override konst fir: FirBinaryLogicExpression, level: Int) : CFGNode<FirBinaryLogicExpression>(owner, level),
    EnterNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitBinaryAndEnterNode(this, data)
    }
}
class BinaryAndExitLeftOperandNode(owner: ControlFlowGraph, override konst fir: FirBinaryLogicExpression, level: Int) : CFGNode<FirBinaryLogicExpression>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitBinaryAndExitLeftOperandNode(this, data)
    }
}
class BinaryAndEnterRightOperandNode(owner: ControlFlowGraph, override konst fir: FirBinaryLogicExpression, level: Int) : CFGNode<FirBinaryLogicExpression>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitBinaryAndEnterRightOperandNode(this, data)
    }
}
class BinaryAndExitNode(owner: ControlFlowGraph, override konst fir: FirBinaryLogicExpression, level: Int) : AbstractBinaryExitNode<FirBinaryLogicExpression>(owner, level),
    ExitNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitBinaryAndExitNode(this, data)
    }
}

class BinaryOrEnterNode(owner: ControlFlowGraph, override konst fir: FirBinaryLogicExpression, level: Int) : CFGNode<FirBinaryLogicExpression>(owner, level),
    EnterNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitBinaryOrEnterNode(this, data)
    }
}
class BinaryOrExitLeftOperandNode(owner: ControlFlowGraph, override konst fir: FirBinaryLogicExpression, level: Int) : CFGNode<FirBinaryLogicExpression>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitBinaryOrExitLeftOperandNode(this, data)
    }
}
class BinaryOrEnterRightOperandNode(owner: ControlFlowGraph, override konst fir: FirBinaryLogicExpression, level: Int) : CFGNode<FirBinaryLogicExpression>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitBinaryOrEnterRightOperandNode(this, data)
    }
}
class BinaryOrExitNode(owner: ControlFlowGraph, override konst fir: FirBinaryLogicExpression, level: Int) : AbstractBinaryExitNode<FirBinaryLogicExpression>(owner, level),
    ExitNodeMarker {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitBinaryOrExitNode(this, data)
    }
}

// ----------------------------------- Operator call -----------------------------------

class TypeOperatorCallNode(owner: ControlFlowGraph, override konst fir: FirTypeOperatorCall, level: Int) : CFGNode<FirTypeOperatorCall>(owner, level) {
    override konst canThrow: Boolean
        get() = fir.operation == FirOperation.AS

    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitTypeOperatorCallNode(this, data)
    }
}

class ComparisonExpressionNode(owner: ControlFlowGraph, override konst fir: FirComparisonExpression, level: Int) : CFGNode<FirComparisonExpression>(owner, level) {
    override konst canThrow: Boolean
        get() = true // TODO? only overridden compareTo

    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitComparisonExpressionNode(this, data)
    }
}

class EqualityOperatorCallNode(owner: ControlFlowGraph, override konst fir: FirEqualityOperatorCall, level: Int) : AbstractBinaryExitNode<FirEqualityOperatorCall>(owner, level) {
    override konst canThrow: Boolean
        get() = true // TODO? only overridden equals

    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitEqualityOperatorCallNode(this, data)
    }
}

// ----------------------------------- Jump -----------------------------------

class JumpNode(owner: ControlFlowGraph, override konst fir: FirJump<*>, level: Int) : CFGNode<FirJump<*>>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitJumpNode(this, data)
    }
}
class ConstExpressionNode(owner: ControlFlowGraph, override konst fir: FirConstExpression<*>, level: Int) : CFGNode<FirConstExpression<*>>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitConstExpressionNode(this, data)
    }
}

// ----------------------------------- Check not null call -----------------------------------

class CheckNotNullCallNode(owner: ControlFlowGraph, override konst fir: FirCheckNotNullCall, level: Int)
    : CFGNode<FirCheckNotNullCall>(owner, level) {
    override konst canThrow: Boolean
        get() = true

    override konst isUnion: Boolean
        get() = true

    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitCheckNotNullCallNode(this, data)
    }
}

// ----------------------------------- Resolvable call -----------------------------------

class QualifiedAccessNode(
    owner: ControlFlowGraph,
    override konst fir: FirQualifiedAccessExpression,
    level: Int
) : CFGNode<FirQualifiedAccessExpression>(owner, level) {
    override konst canThrow: Boolean
        get() = fir.toResolvedCallableSymbol() is FirPropertySymbol

    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitQualifiedAccessNode(this, data)
    }
}

class ResolvedQualifierNode(
    owner: ControlFlowGraph,
    override konst fir: FirResolvedQualifier,
    level: Int
) : CFGNode<FirResolvedQualifier>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitResolvedQualifierNode(this, data)
    }
}

class FunctionCallNode(owner: ControlFlowGraph, override konst fir: FirFunctionCall, level: Int)
    : CFGNode<FirFunctionCall>(owner, level) {

    override konst canThrow: Boolean
        get() = true

    override konst isUnion: Boolean
        get() = true

    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitFunctionCallNode(this, data)
    }
}

class CallableReferenceNode(
    owner: ControlFlowGraph,
    override konst fir: FirCallableReferenceAccess,
    level: Int
) : CFGNode<FirCallableReferenceAccess>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitCallableReferenceNode(this, data)
    }
}

class GetClassCallNode(owner: ControlFlowGraph, override konst fir: FirGetClassCall, level: Int) : CFGNode<FirGetClassCall>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitGetClassCallNode(this, data)
    }
}

class DelegatedConstructorCallNode(owner: ControlFlowGraph, override konst fir: FirDelegatedConstructorCall, level: Int)
    : CFGNode<FirDelegatedConstructorCall>(owner, level) {

    override konst isUnion: Boolean
        get() = true

    override konst canThrow: Boolean
        get() = true // shouldn't matter since delegated constructor calls aren't wrapped in try-finally, but still

    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitDelegatedConstructorCallNode(this, data)
    }
}

class StringConcatenationCallNode(owner: ControlFlowGraph, override konst fir: FirStringConcatenationCall, level: Int)
    : CFGNode<FirStringConcatenationCall>(owner, level) {

    override konst isUnion: Boolean
        get() = true

    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitStringConcatenationCallNode(this, data)
    }
}

class ThrowExceptionNode(
    owner: ControlFlowGraph,
    override konst fir: FirThrowExpression,
    level: Int
) : CFGNode<FirThrowExpression>(owner, level) {
    override konst canThrow: Boolean
        get() = true

    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitThrowExceptionNode(this, data)
    }
}

class StubNode(owner: ControlFlowGraph, level: Int) : CFGNode<FirStub>(owner, level) {
    init {
        isDead = true
    }

    override konst fir: FirStub get() = FirStub

    override var flow: PersistentFlow
        get() = firstPreviousNode.flow
        @CfgInternals
        set(_) = throw IllegalStateException("can't set flow for stub node")

    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitStubNode(this, data)
    }
}

class VariableDeclarationNode(owner: ControlFlowGraph, override konst fir: FirProperty, level: Int) : CFGNode<FirProperty>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitVariableDeclarationNode(this, data)
    }
}
class VariableAssignmentNode(owner: ControlFlowGraph, override konst fir: FirVariableAssignment, level: Int) : CFGNode<FirVariableAssignment>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitVariableAssignmentNode(this, data)
    }
}

class EnterSafeCallNode(owner: ControlFlowGraph, override konst fir: FirSafeCallExpression, level: Int) : CFGNode<FirSafeCallExpression>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitEnterSafeCallNode(this, data)
    }
}
class ExitSafeCallNode(owner: ControlFlowGraph, override konst fir: FirSafeCallExpression, level: Int) : CFGNode<FirSafeCallExpression>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitExitSafeCallNode(this, data)
    }
}

// ----------------------------------- Elvis -----------------------------------

class ElvisLhsExitNode(owner: ControlFlowGraph, override konst fir: FirElvisExpression, level: Int) : CFGNode<FirElvisExpression>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitElvisLhsExitNode(this, data)
    }
}

class ElvisLhsIsNotNullNode(owner: ControlFlowGraph, override konst fir: FirElvisExpression, level: Int) : CFGNode<FirElvisExpression>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitElvisLhsIsNotNullNode(this, data)
    }
}

class ElvisRhsEnterNode(owner: ControlFlowGraph, override konst fir: FirElvisExpression, level: Int) : CFGNode<FirElvisExpression>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitElvisRhsEnterNode(this, data)
    }
}

class ElvisExitNode(owner: ControlFlowGraph, override konst fir: FirElvisExpression, level: Int) : AbstractBinaryExitNode<FirElvisExpression>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitElvisExitNode(this, data)
    }
}

class WhenSubjectExpressionExitNode(owner: ControlFlowGraph, override konst fir: FirWhenSubjectExpression, level: Int) : CFGNode<FirWhenSubjectExpression>(owner, level) {
    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitWhenSubjectExpressionExitNode(this, data)
    }
}

// ----------------------------------- Stub -----------------------------------

object FirStub : FirExpression() {
    override konst source: KtSourceElement? get() = null
    override konst typeRef: FirTypeRef = FirImplicitNothingTypeRef(null)
    override konst annotations: List<FirAnnotation> get() = listOf()

    override fun <R, D> acceptChildren(visitor: FirVisitor<R, D>, data: D) {}
    override fun <D> transformAnnotations(transformer: FirTransformer<D>, data: D): FirExpression = this
    override fun <D> transformChildren(transformer: FirTransformer<D>, data: D): FirElement = this
    override fun replaceAnnotations(newAnnotations: List<FirAnnotation>) { assert(newAnnotations.isEmpty()) }
    override fun replaceTypeRef(newTypeRef: FirTypeRef) { assert(newTypeRef.isNothing) }
}

class FakeExpressionEnterNode(owner: ControlFlowGraph, level: Int) : CFGNode<FirStub>(owner, level), GraphEnterNodeMarker, GraphExitNodeMarker {
    init { isDead = true }

    override konst fir: FirStub = FirStub

    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        throw IllegalStateException("fake expressions should not appear in graphs")
    }
}

// ----------------------------------- Smart-cast node -----------------------------------

class SmartCastExpressionExitNode(owner: ControlFlowGraph, override konst fir: FirSmartCastExpression, level: Int) : CFGNode<FirSmartCastExpression>(owner, level) {
    override konst canThrow: Boolean
        get() = fir.typeRef.coneType.isNothing

    override fun <R, D> accept(visitor: ControlFlowGraphVisitor<R, D>, data: D): R {
        return visitor.visitSmartCastExpressionExitNode(this, data)
    }
}
