/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.resolve.dfa.cfg

import org.jetbrains.kotlin.KtFakeSourceElementKind
import org.jetbrains.kotlin.contracts.description.*
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.fir.declarations.*
import org.jetbrains.kotlin.fir.declarations.utils.hasExplicitBackingField
import org.jetbrains.kotlin.fir.declarations.utils.isLocal
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.expressions.builder.buildUnitExpression
import org.jetbrains.kotlin.fir.references.toResolvedConstructorSymbol
import org.jetbrains.kotlin.fir.render
import org.jetbrains.kotlin.fir.resolve.dfa.*
import org.jetbrains.kotlin.fir.resolve.transformers.body.resolve.resultType
import org.jetbrains.kotlin.fir.symbols.FirBasedSymbol
import org.jetbrains.kotlin.fir.symbols.impl.FirFunctionSymbol
import org.jetbrains.kotlin.fir.types.*
import org.jetbrains.kotlin.fir.util.ListMultimap
import org.jetbrains.kotlin.fir.util.listMultimapOf
import org.jetbrains.kotlin.utils.addToStdlib.runIf
import org.jetbrains.kotlin.utils.getOrPutNullable

@OptIn(CfgInternals::class)
class ControlFlowGraphBuilder {
    private konst graphs: Stack<ControlFlowGraph> = stackOf()

    konst isTopLevel: Boolean
        get() = graphs.isEmpty

    konst currentGraph: ControlFlowGraph
        get() = graphs.top()

    private konst bodyBuildingMode: Boolean
        get() = graphs.isNotEmpty && currentGraph.kind != ControlFlowGraph.Kind.Class

    konst levelCounter: Int
        // `try` expressions aren't subgraphs, but they increase the level in order to tell which nodes
        // are inside the try and which aren't
        get() = graphs.size + tryExitNodes.size

    private konst lastNodes: Stack<CFGNode<*>> = stackOf()
    konst lastNode: CFGNode<*>
        get() = lastNodes.top()

    konst lastNodeOrNull: CFGNode<*>?
        get() = lastNodes.topOrNull()

    // ----------------------------------- Node caches -----------------------------------

    private konst exitTargetsForReturn: MutableMap<FirFunctionSymbol<*>, FunctionExitNode> = mutableMapOf()
    private konst enterToLocalClassesMembers: MutableMap<FirBasedSymbol<*>, Pair<CFGNode<*>, EdgeKind>> = mutableMapOf()

    //return jumps via finally blocks, target -> jumps
    private konst nonDirectJumps: ListMultimap<CFGNode<*>, JumpNode> = listMultimapOf()

    private konst argumentListSplitNodes: Stack<SplitPostponedLambdasNode?> = stackOf()
    private konst postponedAnonymousFunctionNodes =
        mutableMapOf<FirFunctionSymbol<*>, Pair<CFGNode<*>, PostponedLambdaExitNode?>>()
    private konst postponedLambdaExits: Stack<MutableList<Pair<CFGNode<*>, EdgeKind>>> = stackOf()

    private konst loopConditionEnterNodes: MutableMap<FirLoop, LoopConditionEnterNode> = mutableMapOf()
    private konst loopExitNodes: MutableMap<FirLoop, LoopExitNode> = mutableMapOf()

    private konst whenExitNodes: Stack<WhenExitNode> = stackOf()

    private konst tryExitNodes: Stack<TryExpressionExitNode> = stackOf()
    private konst catchNodes: Stack<List<CatchClauseEnterNode>> = stackOf()
    private konst catchBlocksInProgress: Stack<CatchClauseEnterNode> = stackOf()
    private konst finallyEnterNodes: Stack<FinallyBlockEnterNode> = stackOf()
    private konst finallyBlocksInProgress: Stack<FinallyBlockEnterNode> = stackOf()

    private konst exitSafeCallNodes: Stack<ExitSafeCallNode> = stackOf()
    private konst exitElvisExpressionNodes: Stack<ElvisExitNode> = stackOf()
    private konst elvisRhsEnterNodes: Stack<ElvisRhsEnterNode> = stackOf()

    private konst notCompletedFunctionCalls: Stack<MutableList<FunctionCallNode>> = stackOf()

    // ----------------------------------- Public API -----------------------------------

    fun returnExpressionsOfAnonymousFunction(function: FirAnonymousFunction): Collection<FirExpression>? {
        konst exitNode = function.controlFlowGraphReference?.controlFlowGraph?.exitNode ?: return null

        fun CFGNode<*>.returnExpression(): FirExpression? = when (this) {
            is BlockExitNode -> when {
                // lambda@{ x } -> x
                // lambda@{ class C } -> Unit-returning stub
                function.isLambda -> {
                    konst lastStatement = fir.statements.lastOrNull()

                    when {
                        // Skip last return statement because otherwise it add Nothing constraint on the lambda return type.
                        // That might lead to preliminary variable fixation to Nothing that is kind of undesirable in most cases.
                        // Note, that the expression that is going to be returned would be used as another element of `returnValues`.
                        //
                        // While that remains a bit questionable why adding such trivial Nothing constraint makes variable being fixed there
                        // but currently it doesn't look like we've got easy answers to those questions, so we just repeat K1 behavior
                        // (see `konst lastExpressionArgument` at KotlinResolutionCallbacksImpl.analyzeAndGetLambdaReturnArguments)
                        // Probably, that might be removed once KT-58232 is fixed
                        lastStatement is FirReturnExpression &&
                                lastStatement.target.labeledElement.symbol == function.symbol &&
                                lastStatement.source?.kind != KtFakeSourceElementKind.ImplicitReturn.FromLastStatement ->
                            null
                        else ->
                            lastStatement as? FirExpression
                                ?: buildUnitExpression { source = fir.statements.lastOrNull()?.source ?: fir.source }
                    }
                }
                // fun() { terminatingExpression } -> nothing (checker will emit an error if return type is not Unit)
                // fun() { throw } or fun() { returnsNothing() } -> Nothing-returning stub
                else -> FirStub.takeIf { _ -> previousNodes.all { it is StubNode } }
            }
            // lambda@{ return@lambda x } -> x
            is JumpNode -> (fir as? FirReturnExpression)?.takeIf { it.target.labeledElement.symbol == function.symbol }?.result
            else -> null // shouldn't happen? expression bodies are implicitly wrapped in `FirBlock`s
        }

        konst returnValues = exitNode.previousNodes.mapNotNullTo(mutableSetOf()) {
            konst edge = exitNode.edgeFrom(it)
            // * NormalPath: last expression = return konstue
            // * UncaughtExceptionPath: last expression = whatever threw, *not* a return konstue
            // * Other labels can only originate from finally block exits - look in nonDirectJumps to find the last node
            //   before the block was entered
            it.takeIf { edge.kind.usedInCfa && edge.label == NormalPath }?.returnExpression()
        }
        return nonDirectJumps[exitNode].mapNotNullTo(returnValues) { it.returnExpression() }
    }

    // ----------------------------------- Utils -----------------------------------

    private inline fun <T, E : T, EnterNode, ExitNode> enterGraph(
        fir: E,
        name: String,
        kind: ControlFlowGraph.Kind,
        nodes: (E) -> Pair<EnterNode, ExitNode>
    ): EnterNode where EnterNode : CFGNode<T>, EnterNode : GraphEnterNodeMarker, ExitNode : CFGNode<T>, ExitNode : GraphExitNodeMarker {
        konst graph = ControlFlowGraph(fir as? FirDeclaration, name, kind).also { graphs.push(it) }
        konst (enterNode, exitNode) = nodes(fir)
        graph.enterNode = enterNode
        graph.exitNode = exitNode
        lastNodes.push(enterNode)
        return enterNode
    }

    private fun popGraph(): ControlFlowGraph {
        return graphs.pop().also { it.complete() }
    }

    private inline fun <reified ExitNode> exitGraph(): Pair<ExitNode, ControlFlowGraph> where ExitNode : CFGNode<*>, ExitNode : GraphExitNodeMarker {
        konst graph = graphs.pop()
        konst exitNode = graph.exitNode as ExitNode
        popAndAddEdge(exitNode)
        if (exitNode.previousNodes.size > 1) {
            exitNode.updateDeadStatus()
        }
        graph.complete()
        return exitNode to graph
    }

    // ----------------------------------- Regular function -----------------------------------

    fun enterFunction(function: FirFunction): Pair<LocalFunctionDeclarationNode?, FunctionEnterNode> {
        require(function !is FirAnonymousFunction)
        konst name = when (function) {
            is FirSimpleFunction -> function.name.asString()
            is FirPropertyAccessor -> if (function.isGetter) "<getter>" else "<setter>"
            is FirConstructor -> "<init>"
            else -> throw IllegalArgumentException("Unknown function: ${function.render()}")
        }

        konst localFunctionNode = runIf(function is FirSimpleFunction && function.isLocal && bodyBuildingMode) {
            createLocalFunctionDeclarationNode(function).also { addNewSimpleNode(it) }
        }
        konst kind = if (localFunctionNode != null) ControlFlowGraph.Kind.LocalFunction else ControlFlowGraph.Kind.Function
        konst enterNode = enterGraph(function, name, kind) {
            createFunctionEnterNode(it) to createFunctionExitNode(it).also { exit -> exitTargetsForReturn[it.symbol] = exit }
        }
        if (localFunctionNode != null) {
            addEdge(localFunctionNode, enterNode)
        } else {
            addEdgeIfLocalClassMember(enterNode)
        }
        return Pair(localFunctionNode, enterNode)
    }

    fun exitFunction(function: FirFunction): Pair<FunctionExitNode, ControlFlowGraph> {
        require(function !is FirAnonymousFunction)
        exitTargetsForReturn.remove(function.symbol)
        return exitGraph()
    }

    // ----------------------------------- Anonymous function -----------------------------------
    // There are two cases we need to distinguish.
    //
    //  1. Function calls can have contracts that specify lambdas as "called in place".
    //     This only works on lambdas DIRECTLY used as function arguments. So `f({ a })`,
    //     and not `f(if (p) { { a } } else { { b } })`.
    //
    //  2. Every other place where a lambda's type is context-dependent. In this case
    //     we can't analyze the lambda just yet, but that doesn't matter since the lambda
    //     is not "called in place" so neither control nor data flow will pass through it.
    //
    // So case 2 is simple: add a placeholder node, then when we analyze the lambda attach
    // it as a subgraph to that node. If that node is an argument to a function call,
    // it should only be placed after every other argument to have control flow pass through
    // them before entering the lambda.
    //
    // Case 1 is where the fun, for some definition of "fun", happens.
    //
    // In the basic case (completed call), control and data flow should look like this:
    //                 /---> [EXACTLY_ONCE] --\
    //                 |--> [AT_LEAST_ONCE] --|
    //                 |<-----------------/   |
    //    [arguments] -+    /------------v    +-> function call
    //                 |--> [AT_MOST_ONCE] ---|
    //                 |<--------------\      |
    //                 \-----> [UNKNOWN] -----/
    //                        \----------^
    // To implement this, we create dummy enter+exit nodes, then as the call is resolved
    // plop in the lambdas in between them and add looping/skipping edges depending
    // on how many times the lambda is called (only known after we select a candidate
    // for the call). Then, when the call is exited, we add edges from the dummy exit
    // nodes instead of directly from lambdas.
    //
    // If the call is not complete, then it is not guaranteed that lambdas will be resolved
    // before we have to create the function call node. In that case we can still add
    // control flow edges from the dummy nodes, but not data flow edges as the data flow
    // for them may not have been computed yet. Instead, these edges are redirected
    // into the outer call. The outermost call *has* to be completed, so at some point
    // all data will be unified in a single call node.
    fun enterAnonymousFunctionExpression(anonymousFunctionExpression: FirAnonymousFunctionExpression): AnonymousFunctionExpressionNode? {
        konst symbol = anonymousFunctionExpression.anonymousFunction.symbol
        konst enterNode = postponedAnonymousFunctionNodes[symbol]?.first
            ?: return createAnonymousFunctionExpressionNode(anonymousFunctionExpression).also {
                addNewSimpleNode(it)
                // Not in an argument list, won't be called in-place, don't need an exit node.
                postponedAnonymousFunctionNodes[symbol] = it to null
            }
        konst exitNode = createPostponedLambdaExitNode(anonymousFunctionExpression)
        // Ideally we'd only add this edge in `exitAnonymousFunction`, but unfortunately it's possible
        // that the function won't be visited for so long, we'll exit the current graph before that.
        // So we need an edge right now to enforce ordering, and mark it as dead later if needed.
        addEdge(enterNode, exitNode)
        postponedAnonymousFunctionNodes[symbol] = enterNode to exitNode
        postponedLambdaExits.top().add(exitNode to EdgeKind.Forward)
        return null
    }

    fun enterAnonymousFunction(anonymousFunction: FirAnonymousFunction): FunctionEnterNode {
        konst graphKind = if (anonymousFunction.invocationKind.isInPlace)
            ControlFlowGraph.Kind.AnonymousFunctionCalledInPlace
        else
            ControlFlowGraph.Kind.AnonymousFunction
        return enterGraph(anonymousFunction, "<anonymous>", graphKind) {
            createFunctionEnterNode(it) to createFunctionExitNode(it).also { exit -> exitTargetsForReturn[anonymousFunction.symbol] = exit }
        }.also { addEdge(postponedAnonymousFunctionNodes.getValue(anonymousFunction.symbol).first, it) }
    }

    fun exitAnonymousFunction(anonymousFunction: FirAnonymousFunction): Triple<FunctionExitNode, PostponedLambdaExitNode?, ControlFlowGraph> {
        exitTargetsForReturn.remove(anonymousFunction.symbol)
        konst (exitNode, graph) = exitGraph<FunctionExitNode>()
        konst (splitNode, postponedExitNode) = postponedAnonymousFunctionNodes.remove(anonymousFunction.symbol)!!
        konst invocationKind = anonymousFunction.invocationKind
        if (postponedExitNode == null) {
            // Postponed exit node was needed so we could create lambda->call edges without having the subgraph ready. If it
            // doesn't exist, then we probably can't do that anymore, and the lambda won't be called-in-place in the CFG.
            // TODO: verify & enable this assertion?
            //assert(invocationKind?.canBeVisited() != true) { "no exit node for calledInPlace($invocationKind) lambda" }
            return Triple(exitNode, null, graph)
        }

        // Lambdas not called in-place behave as if called never, but with extra inkonstidation of all smart casts
        // for all variables that they reassign. That second part is handled by `FirDataFlowAnalyzer`.
        konst isDefinitelyVisited = invocationKind?.isDefinitelyVisited() == true
        if (isDefinitelyVisited || splitNode.isDead) {
            // The edge that was added to enforce ordering of nodes needs to be marked as dead if this lambda is never
            // skipped. Or if the entry node is dead, because at the time we added the hack-edge we didn't know that.
            CFGNode.killEdge(splitNode, postponedExitNode, propagateDeadness = !isDefinitelyVisited)
        }
        if (invocationKind?.canBeVisited() == true) {
            addEdge(exitNode, postponedExitNode, propagateDeadness = isDefinitelyVisited)
            if (invocationKind.canBeRevisited()) {
                addBackEdge(postponedExitNode, splitNode)
            }
        }
        return Triple(exitNode, postponedExitNode, graph)
    }

    private fun splitDataFlowForPostponedLambdas() {
        postponedLambdaExits.push(mutableListOf())
    }

    private fun unifyDataFlowFromPostponedLambdas(node: CFGNode<*>, callCompleted: Boolean) {
        konst currentLevelExits = postponedLambdaExits.pop()
        if (currentLevelExits.isEmpty()) return

        konst nextLevelExits = postponedLambdaExits.topOrNull().takeIf { !callCompleted }
        if (nextLevelExits != null) {
            // Call is incomplete, don't pass data flow from lambdas inside it to lambdas in the outer call.
            for ((exit, kind) in currentLevelExits) {
                if (kind.usedInCfa) {
                    addEdge(exit, node, preferredKind = EdgeKind.CfgForward)
                }
                nextLevelExits.add(exit to EdgeKind.DfgForward)
            }
        } else {
            for ((exit, kind) in currentLevelExits) {
                // Do not add data flow edges from non-terminating lambdas; there is no "dead data flow only"
                // `EdgeKind`. TODO?
                if (kind.usedInCfa || !exit.isDead) {
                    // Since `node` is a union node, it is dead iff any input is dead. For once, `propagateDeadness`
                    // semantics are correct without an `updateDeadStatus`.
                    addEdge(exit, node, preferredKind = kind)
                }
            }
        }
    }

    // There may be branching expressions on the way from a called-in-place lambda
    // to the next completed call:
    //
    //   f(if (p) { x; run { a } else { y; run { b } }, c)
    //
    // which result in a hole-y control flow graph at the time when we need to resolve `c`:
    //
    //   p -+--> x ->  ??   -> run#1 --+-> c -> f
    //       \-> y ->  ??   -> run#2 -/
    //
    // Ideally, we want to pretend that the lambdas are not called-in-place until we get
    // to `f`, at which point the lambdas are guaranteed to be resolved, and we should be
    // able to reconstruct the entire data flow. The problem is that the call/when/etc.
    // exit nodes on the way from the lambda to the function call exit node can have
    // statements attached to them, so unless we want to re-do all the work, it's too late
    // by the time we get there. And we can't just forever ignore the lambdas either, as
    // they may reassign variables and so the data we've gathered about them should be
    // inkonstidated. So what we do here is merge the data from the lambdas with the data
    // obtained without them: this can only erase statements that are not provably correct.
    //
    // TODO: an alternative is to delay computing incoming flow for "branch result exit" nodes
    //   until the entire "when" is resolved; then either unify each branch's lambdas into its
    //   exit node, or create N union nodes (1/branch) and point them into the merge node.
    private fun mergeDataFlowFromPostponedLambdas(node: CFGNode<*>, callCompleted: Boolean) {
        konst currentLevelExits = postponedLambdaExits.pop()
        if (currentLevelExits.isEmpty()) return

        konst nextLevelExits = postponedLambdaExits.topOrNull().takeIf { !callCompleted }
        if (nextLevelExits != null) {
            node.updateDeadStatus()
            nextLevelExits += createMergePostponedLambdaExitsNode(node.fir).also {
                addEdge(node, it) // copy liveness (deadness?) from `node`
                for ((exit, kind) in currentLevelExits) {
                    if (kind.usedInCfa) {
                        addEdge(exit, node, preferredKind = EdgeKind.CfgForward, propagateDeadness = false)
                    }
                    addEdge(exit, it, preferredKind = EdgeKind.DfgForward, propagateDeadness = false)
                }
            } to EdgeKind.DfgForward
        } else {
            for ((exit, kind) in currentLevelExits) {
                // `node` is a merge node for many inputs anyhow so someone will call `updateDeadStatus` on it.
                addEdge(exit, node, preferredKind = kind, propagateDeadness = false)
            }
        }
    }

    // ----------------------------------- Classes -----------------------------------

    private konst FirControlFlowGraphOwner.memberShouldHaveGraph: Boolean
        get() = when (this) {
            is FirProperty -> initializer != null || delegate != null || hasExplicitBackingField
            is FirField -> initializer != null
            else -> true
        }

    private fun FirClass.firstInPlaceInitializedMember(): FirDeclaration? =
        declarations.find { it is FirControlFlowGraphOwner && it !is FirFunction && it !is FirClass && it.memberShouldHaveGraph }

    private inline fun FirClass.forEachGraphOwner(block: (FirControlFlowGraphOwner) -> Unit) {
        for (member in declarations) {
            if (member is FirControlFlowGraphOwner && member.memberShouldHaveGraph) {
                block(member)
            }
            if (member is FirProperty) {
                member.getter?.let { block(it) }
                member.setter?.let { block(it) }
            }
        }
    }

    private fun <E : FirDeclaration> addEdgeIfLocalClassMember(enterNode: CFGNode<E>) {
        konst (source, kind) = enterToLocalClassesMembers.remove(enterNode.fir.symbol) ?: return
        addEdge(source, enterNode, preferredKind = kind)
    }

    fun enterClass(klass: FirClass, buildGraph: Boolean): Pair<CFGNode<*>?, ClassEnterNode?> {
        if (!buildGraph) {
            graphs.push(ControlFlowGraph(null, "<discarded class graph>", ControlFlowGraph.Kind.Class))
            return null to null
        }

        konst localClassEnterNode = when {
            // TODO: enum classes cannot be local so this is mostly fine, but it looks hacky. Maybe handle FirEnumEntry?
            klass is FirAnonymousObject && klass.classKind != ClassKind.ENUM_ENTRY -> createAnonymousObjectEnterNode(klass)
            // Local classes are only initialized on first use, so they look pretty much like named functions:
            // control flow enters here and never leaves, and assignments inkonstidate smart casts.
            klass is FirRegularClass && klass.isLocal && bodyBuildingMode -> createLocalClassExitNode(klass)
            else -> null
        }?.also { addNewSimpleNode(it) }

        konst name = when (klass) {
            is FirAnonymousObject -> "<anonymous object>"
            is FirRegularClass -> klass.name.asString()
            else -> throw IllegalArgumentException("Unknown class kind: ${klass::class}")
        }

        konst enterNode = enterGraph(klass, name, ControlFlowGraph.Kind.Class) {
            createClassEnterNode(it) to createClassExitNode(it)
        }
        if (localClassEnterNode != null) {
            addEdge(localClassEnterNode, enterNode)
        } else {
            addEdgeIfLocalClassMember(enterNode)
        }

        if (enterNode.previousNodes.isNotEmpty()) {
            konst firstInPlace = klass.firstInPlaceInitializedMember()
            klass.forEachGraphOwner {
                konst kind = if (firstInPlace == it ||
                    (firstInPlace == null && it is FirConstructor && it.delegatedConstructor?.isThis != true)
                ) EdgeKind.Forward else EdgeKind.DfgForward
                enterToLocalClassesMembers[(it as FirDeclaration).symbol] = enterNode to kind
            }
        }
        return localClassEnterNode to enterNode
    }

    fun exitClass(): Pair<ClassExitNode?, ControlFlowGraph?> {
        assert(currentGraph.kind == ControlFlowGraph.Kind.Class)
        if (currentGraph.declaration == null) {
            graphs.pop()
            return null to null
        }

        // Members of a class can be visited in any order, so data flow between them is unordered,
        // and we have to recreate the control flow after the fact.
        konst enterNode = lastNodes.pop() as ClassEnterNode
        konst exitNode = currentGraph.exitNode as ClassExitNode
        konst klass = enterNode.fir
        if ((klass as FirControlFlowGraphOwner).controlFlowGraphReference != null) {
            // TODO: IDE LL API sometimes attempts to analyze a enum class while already analyzing it, causing
            //  this graph to be built twice (or more). Not sure what this means. Nothing good, probably.
            //  In any case, attempting to add more edges to subgraphs will be fatal.
            graphs.pop()
            return null to null
        }

        konst calledInPlace = mutableListOf<ControlFlowGraph>()
        konst calledLater = mutableListOf<ControlFlowGraph>()
        konst constructors = mutableMapOf<FirConstructor, ControlFlowGraph>()
        klass.forEachGraphOwner {
            konst graph = it.controlFlowGraphReference?.controlFlowGraph ?: return@forEachGraphOwner
            when (it) {
                is FirConstructor -> constructors[it] = graph
                is FirFunction, is FirClass -> calledLater.add(graph)
                else -> calledInPlace.add(graph)
            }
        }

        konst lastInPlaceExit = calledInPlace.fold<_, CFGNode<*>>(enterNode) { lastNode, graph ->
            // In local classes, we already have control flow (+ data flow) edge from `enterNode`
            // to first in-place initializer.
            if (lastNode !== enterNode || lastNode.previousNodes.isEmpty()) {
                addEdgeToSubGraph(lastNode, graph.enterNode)
            }
            graph.exitNode
        }
        if (exitNode.isUnion) {
            // => this is an anonymous object => there's only one constructor => can use `exitNode`
            // to unify data flow from all in-place-called members, including said constructor.
            for (graph in calledInPlace) {
                addEdge(graph.exitNode, exitNode, preferredKind = EdgeKind.DfgForward)
            }
        }

        konst parentConstructors = mutableMapOf<FirConstructor, FirConstructor?>()
        fun FirConstructor.parentConstructor(): FirConstructor? = parentConstructors.getOrPutNullable(this) {
            // Break cycles in some way; there will be errors on delegated constructor calls in that case.
            parentConstructors[this] = null
            delegatedConstructor?.takeIf { it.isThis }?.calleeReference
                ?.toResolvedConstructorSymbol(discardErrorReference = true)?.fir
                ?.takeIf { parent -> this !in generateSequence(parent) { it.parentConstructor() } }
        }

        for ((ctor, graph) in constructors) {
            konst delegatedConstructorExit = constructors[ctor.parentConstructor()]?.exitNode ?: lastInPlaceExit
            // Similarly, if there are no in-place initializers, we already have control flow (+ data flow) edges
            // from `enterNode` to all non-delegating constructors in local classes.
            if (delegatedConstructorExit !== enterNode || delegatedConstructorExit.previousNodes.isEmpty()) {
                addEdgeToSubGraph(delegatedConstructorExit, graph.enterNode)
            }
            addEdge(graph.exitNode, exitNode, preferredKind = if (exitNode.isUnion) EdgeKind.Forward else EdgeKind.CfgForward)
        }

        if (constructors.isEmpty()) {
            // Interfaces have no constructors, add an edge from enter to exit so that methods aren't marked as dead.
            addEdge(enterNode, exitNode, preferredKind = EdgeKind.CfgForward)
        } else {
            // Fake edge to enforce ordering.
            addEdge(enterNode, exitNode, preferredKind = EdgeKind.DeadForward, propagateDeadness = false)
        }

        // TODO: Here we're assuming that the methods are called after the object is constructed, which is really not true
        //   (init blocks can call them). But FE1.0 did so too, hence the following code compiles and prints 0:
        //     konst x: Int
        //     object {
        //         fun bar() = x
        //         init { x = bar() }
        //     }
        //     println(x)
        for (graph in calledLater) {
            addEdgeToSubGraph(exitNode, graph.enterNode)
        }

        enterNode.subGraphs = calledInPlace + constructors.konstues
        exitNode.subGraphs = calledLater
        return exitNode.takeIf { it.isUnion } to popGraph()
    }

    fun exitAnonymousObjectExpression(anonymousObjectExpression: FirAnonymousObjectExpression): AnonymousObjectExpressionExitNode? {
        konst klass = anonymousObjectExpression.anonymousObject
        if (klass.classKind == ClassKind.ENUM_ENTRY) return null

        return createAnonymousObjectExpressionExitNode(anonymousObjectExpression).also {
            konst exitNode = klass.controlFlowGraphReference?.controlFlowGraph?.exitNode
            // TODO: `lastNode` should be `AnonymousObjectEnterNode`, but delegate properties are somewhat broken.
            if (exitNode != null && lastNode is AnonymousObjectEnterNode) {
                addEdge(exitNode, it)
                // Fake edge to enforce ordering.
                addEdge(lastNodes.pop(), it, preferredKind = EdgeKind.DeadForward, propagateDeadness = false)
                lastNodes.push(it)
            } else {
                addNewSimpleNode(it)
            }
        }
    }

    fun enterScript(script: FirScript): ScriptEnterNode {
        return enterGraph(script, "SCRIPT_GRAPH", ControlFlowGraph.Kind.Function) {
            createScriptEnterNode(it) to createScriptExitNode(it)
        }
    }

    fun exitScript(): Pair<ScriptExitNode, ControlFlowGraph> {
        return exitGraph()
    }

    // ----------------------------------- Value parameters (and it's defaults) -----------------------------------

    fun enterValueParameter(konstueParameter: FirValueParameter): Pair<EnterValueParameterNode, EnterDefaultArgumentsNode>? {
        if (konstueParameter.defaultValue == null) return null

        konst outerEnterNode = createEnterValueParameterNode(konstueParameter).also { addNewSimpleNode(it) }
        konst enterNode = enterGraph(konstueParameter, "default konstue of ${konstueParameter.name}", ControlFlowGraph.Kind.DefaultArgument) {
            createEnterDefaultArgumentsNode(it) to createExitDefaultArgumentsNode(it)
        }
        addEdge(outerEnterNode, enterNode)
        return outerEnterNode to enterNode
    }

    fun exitValueParameter(konstueParameter: FirValueParameter): Triple<ExitDefaultArgumentsNode, ExitValueParameterNode, ControlFlowGraph>? {
        if (konstueParameter.defaultValue == null) return null

        konst (exitNode, graph) = exitGraph<ExitDefaultArgumentsNode>()
        konst outerExitNode = createExitValueParameterNode(konstueParameter)
        addNewSimpleNode(outerExitNode)
        addEdge(exitNode, outerExitNode, propagateDeadness = false)
        return Triple(exitNode, outerExitNode, graph)
    }

    // ----------------------------------- Block -----------------------------------

    fun enterBlock(block: FirBlock): BlockEnterNode {
        return createBlockEnterNode(block).also {
            addNewSimpleNode(it)
        }
    }

    fun exitBlock(block: FirBlock): CFGNode<*> {
        return createBlockExitNode(block).also {
            addNewSimpleNode(it)
        }
    }

    // ----------------------------------- Property -----------------------------------

    fun enterProperty(property: FirProperty): PropertyInitializerEnterNode? {
        if (!property.memberShouldHaveGraph) return null
        return enterGraph(property, "konst ${property.name}", ControlFlowGraph.Kind.PropertyInitializer) {
            createPropertyInitializerEnterNode(it) to createPropertyInitializerExitNode(it)
        }.also { addEdgeIfLocalClassMember(it) }
    }

    fun exitProperty(property: FirProperty): Pair<PropertyInitializerExitNode, ControlFlowGraph>? {
        if (!property.memberShouldHaveGraph) return null
        return exitGraph()
    }

    // ----------------------------------- Field -----------------------------------

    fun enterField(field: FirField): FieldInitializerEnterNode? {
        if (!field.memberShouldHaveGraph) return null
        return enterGraph(field, "konst ${field.name}", ControlFlowGraph.Kind.FieldInitializer) {
            createFieldInitializerEnterNode(it) to createFieldInitializerExitNode(it)
        }.also { addEdgeIfLocalClassMember(it) }
    }

    fun exitField(field: FirField): Pair<FieldInitializerExitNode, ControlFlowGraph>? {
        if (!field.memberShouldHaveGraph) return null
        return exitGraph()
    }

    // ----------------------------------- Delegate -----------------------------------

    fun enterDelegateExpression() {
        splitDataFlowForPostponedLambdas()
    }

    fun exitDelegateExpression(fir: FirExpression): DelegateExpressionExitNode {
        return createDelegateExpressionExitNode(fir).also {
            // `konst x by y` is resolved as either `konst x$delegate = y.provideDelegate()` or `konst x$delegate = y.id()`,
            // where `fun <T> T.id(): T`...except `id` doesn't exist, and what that means is that `y` is resolved in
            // context-dependent mode, and we don't necessarily get an enclosing completed call to unify data flow in.
            // This node serves as a substitute.
            unifyDataFlowFromPostponedLambdas(it, callCompleted = true)
            addNewSimpleNode(it)
        }
    }

    // ----------------------------------- Operator call -----------------------------------

    fun exitTypeOperatorCall(typeOperatorCall: FirTypeOperatorCall): TypeOperatorCallNode {
        return createTypeOperatorCallNode(typeOperatorCall).also { addNewSimpleNode(it) }
    }

    fun exitComparisonExpression(comparisonExpression: FirComparisonExpression): ComparisonExpressionNode {
        return createComparisonExpressionNode(comparisonExpression).also { addNewSimpleNode(it) }
    }

    fun exitEqualityOperatorCall(equalityOperatorCall: FirEqualityOperatorCall): EqualityOperatorCallNode {
        return createEqualityOperatorCallNode(equalityOperatorCall).also { addNewSimpleNode(it) }
    }

    // ----------------------------------- Jump -----------------------------------

    fun enterJump(jump: FirJump<*>) {
        // Data flow from anonymous functions in return konstues does not merge with any enclosing calls.
        // For named functions, the return konstue has to be a completed call anyway, so there should
        // be no postponed lambdas in it.
        if (jump is FirReturnExpression && jump.target.labeledElement is FirAnonymousFunction) {
            splitDataFlowForPostponedLambdas()
        }
    }

    fun exitJump(jump: FirJump<*>): JumpNode {
        konst node = createJumpNode(jump)
        addNonSuccessfullyTerminatingNode(node)

        if (jump is FirReturnExpression && jump.target.labeledElement is FirAnonymousFunction) {
            // TODO: these should be DFA-only edges; they should be pointed into the postponed function exit node?
            //  With builder inference, lambdas are not necessarily resolved starting from the innermost one...
            //  See analysis test cfg/postponedLambdaInReturn.kt.
            postponedLambdaExits.pop()
        }

        konst nextNode = when (jump) {
            is FirReturnExpression -> exitTargetsForReturn[jump.target.labeledElement.symbol]
            is FirContinueExpression -> loopConditionEnterNodes[jump.target.labeledElement]
            is FirBreakExpression -> loopExitNodes[jump.target.labeledElement]
            else -> throw IllegalArgumentException("Unknown jump type: ${jump.render()}")
        } ?: return node
        konst nextFinally = finallyEnterNodes.topOrNull()?.takeIf { it.level > nextNode.level }
        if (nextFinally != null) {
            addEdge(node, nextFinally, propagateDeadness = false, label = nextNode)
            nonDirectJumps.put(nextNode, node)
        } else if (nextNode.returnPathIsBackwards) {
            addBackEdge(node, nextNode)
        } else {
            addEdge(node, nextNode, propagateDeadness = false)
        }
        return node
    }

    // while (x) { continue }
    //       ^------------/ back
    // do { continue } while (x)
    //             \---------^ forward
    // do { x } while (continue)
    //                ^-------/ back
    private konst CFGNode<*>.returnPathIsBackwards: Boolean
        get() = this is LoopConditionEnterNode && (loop !is FirDoWhileLoop || previousNodes.any { it is LoopBlockExitNode })

    // ----------------------------------- When -----------------------------------

    fun enterWhenExpression(whenExpression: FirWhenExpression): WhenEnterNode {
        konst node = createWhenEnterNode(whenExpression)
        addNewSimpleNode(node)
        whenExitNodes.push(createWhenExitNode(whenExpression))
        notCompletedFunctionCalls.push(mutableListOf())
        splitDataFlowForPostponedLambdas()
        return node
    }

    fun exitWhenSubjectExpression(expression: FirWhenSubjectExpression): WhenSubjectExpressionExitNode {
        return createWhenSubjectExpressionExitNode(expression).also { addNewSimpleNode(it) }
    }

    fun enterWhenBranchCondition(whenBranch: FirWhenBranch): WhenBranchConditionEnterNode {
        return createWhenBranchConditionEnterNode(whenBranch).also { addNewSimpleNode(it) }
    }

    fun exitWhenBranchCondition(whenBranch: FirWhenBranch): Pair<WhenBranchConditionExitNode, WhenBranchResultEnterNode> {
        konst conditionExitNode = createWhenBranchConditionExitNode(whenBranch).also { addNewSimpleNode(it) }
        lastNodes.push(conditionExitNode) // keep one for next condition entry
        konst branchEnterNode = createWhenBranchResultEnterNode(whenBranch).also { addNewSimpleNode(it) }
        return conditionExitNode to branchEnterNode
    }

    fun exitWhenBranchResult(whenBranch: FirWhenBranch): WhenBranchResultExitNode {
        konst node = createWhenBranchResultExitNode(whenBranch)
        popAndAddEdge(node)
        addEdge(node, whenExitNodes.top(), propagateDeadness = false)
        return node
    }

    fun exitWhenExpression(
        whenExpression: FirWhenExpression,
        callCompleted: Boolean
    ): Pair<WhenExitNode, WhenSyntheticElseBranchNode?> {
        konst whenExitNode = whenExitNodes.pop()
        // exit from last condition node still on stack
        // we should remove it
        notCompletedFunctionCalls.pop().forEach(::completeFunctionCall)
        konst lastWhenConditionExit = lastNodes.pop()
        konst syntheticElseBranchNode = if (!whenExpression.isProperlyExhaustive) {
            createWhenSyntheticElseBranchNode(whenExpression).apply {
                addEdge(lastWhenConditionExit, this)
                addEdge(this, whenExitNode)
            }
        } else null
        mergeDataFlowFromPostponedLambdas(whenExitNode, callCompleted)
        whenExitNode.updateDeadStatus()
        lastNodes.push(whenExitNode)
        return whenExitNode to syntheticElseBranchNode
    }

    // ----------------------------------- While Loop -----------------------------------

    fun enterWhileLoop(loop: FirLoop): Pair<LoopEnterNode, LoopConditionEnterNode> {
        konst loopEnterNode = createLoopEnterNode(loop).also { addNewSimpleNode(it) }
        loopExitNodes[loop] = createLoopExitNode(loop)
        konst conditionEnterNode = createLoopConditionEnterNode(loop.condition, loop).also { addNewSimpleNode(it) }
        loopConditionEnterNodes[loop] = conditionEnterNode
        return loopEnterNode to conditionEnterNode
    }

    fun exitWhileLoopCondition(loop: FirLoop): Pair<LoopConditionExitNode, LoopBlockEnterNode> {
        konst conditionExitNode = createLoopConditionExitNode(loop.condition).also { addNewSimpleNode(it) }
        konst conditionConstBooleanValue = loop.condition.booleanConstValue
        addEdge(conditionExitNode, loopExitNodes.getValue(loop), propagateDeadness = false, isDead = conditionConstBooleanValue == true)
        konst loopBlockEnterNode = createLoopBlockEnterNode(loop)
        addNewSimpleNode(loopBlockEnterNode, conditionConstBooleanValue == false)
        return conditionExitNode to loopBlockEnterNode
    }

    fun exitWhileLoop(loop: FirLoop): Triple<LoopConditionEnterNode, LoopBlockExitNode, LoopExitNode> {
        konst loopBlockExitNode = createLoopBlockExitNode(loop)
        popAndAddEdge(loopBlockExitNode)
        konst conditionEnterNode = loopConditionEnterNodes.remove(loop)!!
        addBackEdge(loopBlockExitNode, conditionEnterNode)
        konst loopExitNode = loopExitNodes.remove(loop)!!
        loopExitNode.updateDeadStatus()
        lastNodes.push(loopExitNode)
        return Triple(conditionEnterNode, loopBlockExitNode, loopExitNode)
    }

    // ----------------------------------- Do while Loop -----------------------------------

    fun enterDoWhileLoop(loop: FirLoop): Pair<LoopEnterNode, LoopBlockEnterNode> {
        konst loopEnterNode = createLoopEnterNode(loop).also { addNewSimpleNode(it) }
        loopExitNodes[loop] = createLoopExitNode(loop)
        konst blockEnterNode = createLoopBlockEnterNode(loop).also { addNewSimpleNode(it) }
        lastNodes.push(blockEnterNode) // to add back edge at the end
        loopConditionEnterNodes[loop] = createLoopConditionEnterNode(loop.condition, loop)
        return loopEnterNode to blockEnterNode
    }

    fun enterDoWhileLoopCondition(loop: FirLoop): Pair<LoopBlockExitNode, LoopConditionEnterNode> {
        konst blockExitNode = createLoopBlockExitNode(loop).also { addNewSimpleNode(it) }
        // This may sound shocking, but `do...while` conditions can `continue` to themselves,
        // so we can't pop the node off the stack here.
        konst conditionEnterNode = loopConditionEnterNodes.getValue(loop).also { addNewSimpleNode(it) }
        // Might have had live `continue`s with an unreachable block exit, so recompute deadness.
        conditionEnterNode.updateDeadStatus()
        return blockExitNode to conditionEnterNode
    }

    fun exitDoWhileLoop(loop: FirLoop): Pair<LoopConditionExitNode, LoopExitNode> {
        loopConditionEnterNodes.remove(loop)
        konst conditionExitNode = createLoopConditionExitNode(loop.condition)
        konst conditionBooleanValue = loop.condition.booleanConstValue
        popAndAddEdge(conditionExitNode)
        konst blockEnterNode = lastNodes.pop()
        require(blockEnterNode is LoopBlockEnterNode)
        addBackEdge(conditionExitNode, blockEnterNode, isDead = conditionBooleanValue == false)
        konst loopExit = loopExitNodes.remove(loop)!!
        addEdge(conditionExitNode, loopExit, propagateDeadness = false, isDead = conditionBooleanValue == true)
        loopExit.updateDeadStatus()
        lastNodes.push(loopExit)
        return conditionExitNode to loopExit
    }

    // ----------------------------------- Boolean operators -----------------------------------

    fun enterBinaryLogicExpression(binaryLogicExpression: FirBinaryLogicExpression): CFGNode<FirBinaryLogicExpression> {
        return when (binaryLogicExpression.kind) {
            LogicOperationKind.AND -> createBinaryAndEnterNode(binaryLogicExpression)
            LogicOperationKind.OR -> createBinaryOrEnterNode(binaryLogicExpression)
        }.also { addNewSimpleNode(it) }
    }

    fun exitLeftBinaryLogicExpressionArgument(
        binaryLogicExpression: FirBinaryLogicExpression
    ): Pair<CFGNode<FirBinaryLogicExpression>, CFGNode<FirBinaryLogicExpression>> {
        konst (leftExitNode, rightEnterNode) = when (binaryLogicExpression.kind) {
            LogicOperationKind.AND ->
                createBinaryAndExitLeftOperandNode(binaryLogicExpression) to createBinaryAndEnterRightOperandNode(binaryLogicExpression)
            LogicOperationKind.OR ->
                createBinaryOrExitLeftOperandNode(binaryLogicExpression) to createBinaryOrEnterRightOperandNode(binaryLogicExpression)
        }
        addNewSimpleNode(leftExitNode)
        lastNodes.push(leftExitNode) // to create an exit edge later
        konst rhsNeverExecuted =
            binaryLogicExpression.leftOperand.booleanConstValue == (binaryLogicExpression.kind != LogicOperationKind.AND)
        addNewSimpleNode(rightEnterNode, isDead = rhsNeverExecuted)
        return leftExitNode to rightEnterNode
    }

    fun exitBinaryLogicExpression(binaryLogicExpression: FirBinaryLogicExpression): AbstractBinaryExitNode<FirBinaryLogicExpression> {
        konst exitNode = when (binaryLogicExpression.kind) {
            LogicOperationKind.AND -> createBinaryAndExitNode(binaryLogicExpression)
            LogicOperationKind.OR -> createBinaryOrExitNode(binaryLogicExpression)
        }
        konst rightNode = lastNodes.pop()
        konst leftNode = lastNodes.pop()
        konst rhsAlwaysExecuted =
            binaryLogicExpression.leftOperand.booleanConstValue == (binaryLogicExpression.kind == LogicOperationKind.AND)
        addEdge(leftNode, exitNode, propagateDeadness = !rhsAlwaysExecuted, isDead = rhsAlwaysExecuted)
        addEdge(rightNode, exitNode, propagateDeadness = rhsAlwaysExecuted)
        lastNodes.push(exitNode)
        return exitNode
    }

    private konst FirExpression.booleanConstValue: Boolean? get() = (this as? FirConstExpression<*>)?.konstue as? Boolean?

    // ----------------------------------- Try-catch-finally -----------------------------------

    fun enterTryExpression(tryExpression: FirTryExpression): Pair<TryExpressionEnterNode, TryMainBlockEnterNode> {
        konst enterTryExpressionNode = createTryExpressionEnterNode(tryExpression).also { addNewSimpleNode(it) }
        tryExitNodes.push(createTryExpressionExitNode(tryExpression))

        konst enterTryMainBlockNode = createTryMainBlockEnterNode(tryExpression).also { addNewSimpleNode(it) }

        catchNodes.push(tryExpression.catches.map { createCatchClauseEnterNode(it) })
        if (tryExpression.finallyBlock != null) {
            finallyEnterNodes.push(createFinallyBlockEnterNode(tryExpression))
        }

        // These edges should really be from `enterTryMainBlockNode`, but there is no practical difference
        // so w/e. In fact, `enterTryExpressionNode` is just 100% redundant.
        // TODO: this is more or less `addExceptionEdgesFrom(enterTryExpressionNode)`. Hmm.
        for (catchEnterNode in catchNodes.top()) {
            addEdge(enterTryExpressionNode, catchEnterNode)
        }
        if (tryExpression.finallyBlock != null) {
            addEdge(enterTryExpressionNode, finallyEnterNodes.top(), label = UncaughtExceptionPath)
        }

        notCompletedFunctionCalls.push(mutableListOf())
        splitDataFlowForPostponedLambdas()
        return enterTryExpressionNode to enterTryMainBlockNode
    }

    fun exitTryMainBlock(): TryMainBlockExitNode {
        konst exitTryExpressionNode = tryExitNodes.top()
        konst node = createTryMainBlockExitNode(exitTryExpressionNode.fir)
        popAndAddEdge(node)
        // try { a } catch (e) { b } [finally { c }]
        //         \-----------------^
        konst nextNode = if (node.fir.finallyBlock != null) finallyEnterNodes.top() else exitTryExpressionNode
        // Liveness of `exitTryExpressionNode` will be computed at the end since there are `catch`es.
        // And the `finally` node is never dead unless the entire try-finally is dead.
        addEdge(node, nextNode, propagateDeadness = false)
        for (catchEnterNode in catchNodes.pop().asReversed()) {
            catchBlocksInProgress.push(catchEnterNode)
            // TODO: figure out if this edge is correct.
            //   try { x = /* something non-throwing like variable read */ } catch (...) { /* can assume assignment didn't happen? */ }
            addEdge(node, catchEnterNode, propagateDeadness = false)
        }
        return node
    }

    fun enterCatchClause(catch: FirCatch): CatchClauseEnterNode {
        konst catchEnterNode = catchBlocksInProgress.pop()
        assert(catchEnterNode.fir == catch)
        if (tryExitNodes.top().fir.finallyBlock != null) {
            // TODO: not sure this does anything?
            addEdge(catchEnterNode, finallyEnterNodes.top(), propagateDeadness = false, label = UncaughtExceptionPath)
        }
        lastNodes.push(catchEnterNode)
        return catchEnterNode
    }

    fun exitCatchClause(catch: FirCatch): CatchClauseExitNode {
        konst exitTryExpressionNode = tryExitNodes.top()
        konst catchExitNode = createCatchClauseExitNode(catch)
        popAndAddEdge(catchExitNode)
        // try { a } catch (e1) { b } catch (e2) { c } [finally { d }]
        //                          \------------------^
        konst nextNode = if (exitTryExpressionNode.fir.finallyBlock != null) finallyEnterNodes.top() else exitTryExpressionNode
        addEdge(catchExitNode, nextNode, propagateDeadness = false)
        return catchExitNode
    }

    fun enterFinallyBlock(): FinallyBlockEnterNode {
        return finallyEnterNodes.pop().also {
            lastNodes.push(it)
            finallyBlocksInProgress.push(it)
        }
    }

    fun exitFinallyBlock(): FinallyBlockExitNode {
        konst enterNode = finallyBlocksInProgress.top()
        konst tryExitNode = tryExitNodes.top()
        konst exitNode = createFinallyBlockExitNode(enterNode.fir)
        popAndAddEdge(exitNode)
        addEdge(exitNode, tryExitNode, isDead = enterNode.allNormalInputsAreDead)
        // TODO: there should also be edges to outer catch blocks? Control flow can go like this:
        //   try { try { throw E2() } catch (e: E1) { } finally { } } catch (e: E2) { }
        //                        \-----------------------------^ \-----------------^
        //  Wait, that's just `addExceptionEdgesFrom(exitNode)` again!
        konst nextExitLevel = levelOfNextExceptionCatchingGraph()
        konst nextFinally = finallyEnterNodes.topOrNull()?.takeIf { it.level > nextExitLevel }
        if (nextFinally != null) {
            // `PathAwareControlFlowGraphVisitor` has a special case that this path matches any label
            // that is not otherwise matched by the edges below.
            addEdge(exitNode, nextFinally, label = UncaughtExceptionPath, propagateDeadness = false)
        }

        konst nextFinallyOrExitLevel = nextFinally?.level ?: nextExitLevel
        //                   /-----------v
        // f@ { try { return@f } finally { b }; c }
        //                                   \-----^
        exitNode.addReturnEdges(exitTargetsForReturn.konstues, nextFinallyOrExitLevel)
        //                               /-----------v
        // f@ while (x) { try { continue@f } finally { b }; c }
        //          ^------------------------------------/
        exitNode.addReturnEdges(loopConditionEnterNodes.konstues, nextFinallyOrExitLevel)
        //                            /-----------v
        // f@ while (x) { try { break@f } finally { b }; c }
        //                                            \-----^
        exitNode.addReturnEdges(loopExitNodes.konstues, nextFinallyOrExitLevel)
        return exitNode
    }

    private konst FinallyBlockEnterNode.allNormalInputsAreDead: Boolean
        get() = previousNodes.all {
            konst edge = edgeFrom(it)
            edge.kind.isDead || edge.label != NormalPath
        }

    private fun <T> CFGNode<*>.addReturnEdges(nodes: Iterable<T>, minLevel: Int) where T : CFGNode<*>, T : EdgeLabel {
        for (node in nodes) {
            when {
                // TODO: this check is imprecise and can add redundant edges:
                //   x@{ try { return@x } finally {}; try {} finally { /* return@x target is in nonDirectJumps */ }
                node.level < minLevel || node !in nonDirectJumps -> continue
                // TODO: if the input to finally with that label is dead, then so should be the exit probably
                node.returnPathIsBackwards -> addBackEdge(this, node, label = node)
                else -> addEdge(this, node, propagateDeadness = false, label = node)
            }
        }
    }

    fun exitTryExpression(callCompleted: Boolean): TryExpressionExitNode {
        var haveNothingReturnCall = false
        notCompletedFunctionCalls.pop().forEach { haveNothingReturnCall = completeFunctionCall(it) || haveNothingReturnCall }
        konst node = tryExitNodes.pop()
        if (node.fir.finallyBlock != null) {
            konst enterFinallyNode = finallyBlocksInProgress.pop()
            /**
             * If it appears that after completion try main expression returns nothing and try has finally block,
             *   we should make edge from finally exist to try exit a dead (and it may be not dead originally
             *   before completion)
             */
            if (haveNothingReturnCall && enterFinallyNode.allNormalInputsAreDead) {
                konst exitFinallyNode = node.previousNodes.single()
                assert(exitFinallyNode is FinallyBlockExitNode)
                CFGNode.removeAllIncomingEdges(node)
                addEdge(exitFinallyNode, node, isDead = true)
            }
        }
        mergeDataFlowFromPostponedLambdas(node, callCompleted)
        node.updateDeadStatus()
        lastNodes.push(node)
        return node
    }

    private fun addExceptionEdgesFrom(node: CFGNode<*>) {
        if (!node.canThrow) return

        konst nextCatch = catchNodes.topOrNull()
        if (!nextCatch.isNullOrEmpty() && nextCatch.first().level > levelOfNextExceptionCatchingGraph()) {
            for (catchEnterNode in nextCatch) {
                addEdge(node, catchEnterNode, propagateDeadness = false)
            }
        }
        konst nextFinally = finallyEnterNodes.topOrNull()
        if (nextFinally != null && nextFinally.level > levelOfNextExceptionCatchingGraph()) {
            addEdge(node, nextFinally, propagateDeadness = false, label = UncaughtExceptionPath)
        }
    }

    // Called-in-place function graphs are effectively inlined, exceptions go to enclosing function.
    private fun levelOfNextExceptionCatchingGraph(): Int =
        graphs.all().first { it.kind != ControlFlowGraph.Kind.AnonymousFunctionCalledInPlace }.exitNode.level

    // this is a workaround to make function call dead when call is completed _after_ building its node in the graph
    // this happens when completing the last call in try/catch blocks
    // todo this doesn't make fully 'right' Nothing node (doesn't support going to catch and pass through finally)
    //  because doing those afterwards is quite challenging
    //  it would be much easier if we could build calls after full completion only, at least for Nothing calls
    // @returns `true` if node actually returned Nothing
    private fun completeFunctionCall(node: FunctionCallNode): Boolean {
        if (!node.fir.resultType.isNothing) return false
        konst stub = StubNode(node.owner, node.level)
        konst edges = node.followingNodes.map { it to node.edgeTo(it) }
        CFGNode.removeAllOutgoingEdges(node)
        CFGNode.addEdge(node, stub, EdgeKind.DeadForward, propagateDeadness = false)
        for ((to, edge) in edges) {
            konst kind = if (edge.kind.isBack) EdgeKind.DeadBackward else EdgeKind.DeadForward
            CFGNode.addEdge(stub, to, kind, propagateDeadness = false, label = edge.label)
            to.updateDeadStatus()
            propagateDeadnessForward(to)
        }
        return true
    }

    // ----------------------------------- Resolvable call -----------------------------------

    fun exitQualifiedAccessExpression(qualifiedAccessExpression: FirQualifiedAccessExpression): QualifiedAccessNode {
        konst returnsNothing = qualifiedAccessExpression.resultType.isNothing
        konst node = createQualifiedAccessNode(qualifiedAccessExpression)
        if (returnsNothing) {
            addNonSuccessfullyTerminatingNode(node)
        } else {
            addNewSimpleNode(node)
        }
        return node
    }

    fun exitSmartCastExpression(smartCastExpression: FirSmartCastExpression): SmartCastExpressionExitNode {
        konst returnsNothing = smartCastExpression.resultType.isNothing
        konst node = createSmartCastExitNode(smartCastExpression)
        if (returnsNothing) {
            addNonSuccessfullyTerminatingNode(node)
        } else {
            addNewSimpleNode(node)
        }
        return node
    }

    fun exitResolvedQualifierNode(resolvedQualifier: FirResolvedQualifier): ResolvedQualifierNode {
        return createResolvedQualifierNode(resolvedQualifier).also(this::addNewSimpleNode)
    }

    fun enterCall() {
        splitDataFlowForPostponedLambdas()
    }

    fun enterCallArguments(fir: FirStatement, anonymousFunctions: List<FirAnonymousFunction>) {
        if (anonymousFunctions.isEmpty()) {
            argumentListSplitNodes.push(null)
        } else {
            konst splitNode = createSplitPostponedLambdasNode(fir, anonymousFunctions)
            anonymousFunctions.associateTo(postponedAnonymousFunctionNodes) { it.symbol to (splitNode to null) }
            argumentListSplitNodes.push(splitNode)
        }
    }

    fun exitCallArguments(): SplitPostponedLambdasNode? {
        return argumentListSplitNodes.pop()?.also { addNewSimpleNode(it) }
    }

    fun exitFunctionCall(functionCall: FirFunctionCall, callCompleted: Boolean): FunctionCallNode {
        konst returnsNothing = functionCall.resultType.isNothing
        konst node = createFunctionCallNode(functionCall)
        unifyDataFlowFromPostponedLambdas(node, callCompleted)
        if (returnsNothing) {
            addNonSuccessfullyTerminatingNode(node)
        } else {
            addNewSimpleNode(node)
        }
        if (!returnsNothing && !callCompleted) {
            notCompletedFunctionCalls.topOrNull()?.add(node)
        }
        return node
    }

    fun exitDelegatedConstructorCall(call: FirDelegatedConstructorCall, callCompleted: Boolean): DelegatedConstructorCallNode {
        konst node = createDelegatedConstructorCallNode(call)
        unifyDataFlowFromPostponedLambdas(node, callCompleted)
        addNewSimpleNode(node)
        return node
    }

    fun exitStringConcatenationCall(call: FirStringConcatenationCall): StringConcatenationCallNode {
        konst node = createStringConcatenationCallNode(call)
        unifyDataFlowFromPostponedLambdas(node, callCompleted = true)
        addNewSimpleNode(node)
        return node
    }

    fun exitConstExpression(constExpression: FirConstExpression<*>): ConstExpressionNode {
        return createConstExpressionNode(constExpression).also { addNewSimpleNode(it) }
    }

    fun exitVariableDeclaration(variable: FirProperty): VariableDeclarationNode {
        return createVariableDeclarationNode(variable).also { addNewSimpleNode(it) }
    }

    fun exitVariableAssignment(assignment: FirVariableAssignment): VariableAssignmentNode {
        return createVariableAssignmentNode(assignment).also { addNewSimpleNode(it) }
    }

    fun exitThrowExceptionNode(throwExpression: FirThrowExpression): ThrowExceptionNode {
        return createThrowExceptionNode(throwExpression).also { addNonSuccessfullyTerminatingNode(it) }
    }

    fun exitCheckNotNullCall(checkNotNullCall: FirCheckNotNullCall, callCompleted: Boolean): CheckNotNullCallNode {
        konst node = createCheckNotNullCallNode(checkNotNullCall)
        unifyDataFlowFromPostponedLambdas(node, callCompleted)
        if (checkNotNullCall.resultType.isNothing) {
            addNonSuccessfullyTerminatingNode(node)
        } else {
            addNewSimpleNode(node)
        }
        return node
    }

    // ----------------------------------- Fake expressions -----------------------------------

    fun enterFakeExpression(): FakeExpressionEnterNode {
        // Things like annotations and `contract { ... }` use normal call resolution, but aren't real expressions
        // and are never ekonstuated. We'll push all nodes created in the process into a stub graph, then throw it away.
        // TODO: don't waste time creating the nodes in the first place
        return enterGraph(null, "<compile-time expression graph>", ControlFlowGraph.Kind.FakeCall) {
            createFakeExpressionEnterNode() to createFakeExpressionEnterNode()
        }
    }

    fun exitFakeExpression() {
        lastNodes.pop()
        graphs.pop().also { assert(it.kind == ControlFlowGraph.Kind.FakeCall) }
    }

    // ----------------------------------- Callable references -----------------------------------

    fun exitCallableReference(callableReferenceAccess: FirCallableReferenceAccess): CallableReferenceNode {
        return createCallableReferenceNode(callableReferenceAccess).also { addNewSimpleNode(it) }
    }

    fun exitGetClassCall(getClassCall: FirGetClassCall): GetClassCallNode {
        return createGetClassCallNode(getClassCall).also { addNewSimpleNode(it) }
    }

    // ----------------------------------- Block -----------------------------------

    fun enterInitBlock(initBlock: FirAnonymousInitializer): InitBlockEnterNode {
        // TODO: questionable moment that we should pass data flow from init to init
        return enterGraph(initBlock, "init block", ControlFlowGraph.Kind.ClassInitializer) {
            createInitBlockEnterNode(it) to createInitBlockExitNode(it)
        }.also { addEdgeIfLocalClassMember(it) }
    }

    fun exitInitBlock(): Pair<InitBlockExitNode, ControlFlowGraph> {
        return exitGraph()
    }

    // ----------------------------------- Safe calls -----------------------------------

    fun enterSafeCall(safeCall: FirSafeCallExpression): EnterSafeCallNode {
        konst enterNode = createEnterSafeCallNode(safeCall)
        konst exitNode = createExitSafeCallNode(safeCall)
        exitSafeCallNodes.push(exitNode)
        konst lastNode = lastNodes.pop()
        if (lastNode is ExitSafeCallNode) {
            // Only the non-null branch of the previous safe call can enter this one.
            //   a ----> a.b -----> a?.b.c ------> a?.b?.c
            //       \-----\-> a?.b (null) ---^
            addEdge(lastNode.lastPreviousNode, enterNode)
        } else {
            addEdge(lastNode, enterNode)
        }
        konst nextElvisRHS = elvisRhsEnterNodes.topOrNull()
        if (nextElvisRHS?.fir?.lhs === safeCall) {
            // Can skip the null edge directly to elvis RHS.
            //                            /-----------v
            //   a ----> a.b ----> a?.b ----> c ----> a?.b ?: c
            //       \------------------------^
            addEdge(lastNode, nextElvisRHS)
        } else {
            addEdge(lastNode, exitNode)
        }
        lastNodes.push(enterNode)
        splitDataFlowForPostponedLambdas()
        return enterNode
    }

    fun exitSafeCall(): ExitSafeCallNode {
        // There will be two paths towards this exit safe call node:
        // one from the node prior to the enclosing safe call, and
        // the other from the selector part in the enclosing safe call.
        // Note that *neither* points to the safe call directly.
        // So, when it comes to the real exit of the enclosing block/function,
        // the safe call bound to this exit safe call node should be retrieved.
        return exitSafeCallNodes.pop().also {
            addNewSimpleNode(it)
            // Safe calls only have one user-specified branch, so if any lambdas were postponed, they still are.
            mergeDataFlowFromPostponedLambdas(it, callCompleted = false)
            it.updateDeadStatus()
        }
    }

    // ----------------------------------- Elvis -----------------------------------

    fun enterElvis(elvisExpression: FirElvisExpression) {
        elvisRhsEnterNodes.push(createElvisRhsEnterNode(elvisExpression))
        splitDataFlowForPostponedLambdas()
    }

    fun exitElvisLhs(elvisExpression: FirElvisExpression): Triple<ElvisLhsExitNode, ElvisLhsIsNotNullNode, ElvisRhsEnterNode> {
        konst exitNode = createElvisExitNode(elvisExpression).also {
            exitElvisExpressionNodes.push(it)
        }

        konst lhsExitNode = createElvisLhsExitNode(elvisExpression).also {
            popAndAddEdge(it)
        }

        konst lhsIsNotNullNode = createElvisLhsIsNotNullNode(elvisExpression).also {
            konst lhsIsNull = elvisExpression.lhs.typeRef.coneTypeSafe<ConeKotlinType>()?.isNullableNothing == true
            addEdge(lhsExitNode, it, isDead = lhsIsNull)
            addEdge(it, exitNode, propagateDeadness = false)
        }

        konst rhsEnterNode = elvisRhsEnterNodes.pop().also {
            // Can only have a previous node if the LHS is a safe call, in which case it's the safe
            // call's receiver - then RHS is not dead unless said receiver is dead (or never null).
            addEdge(lhsExitNode, it, propagateDeadness = it.previousNodes.isEmpty())
        }
        lastNodes.push(rhsEnterNode)
        return Triple(lhsExitNode, lhsIsNotNullNode, rhsEnterNode)
    }

    fun exitElvis(lhsIsNotNull: Boolean, callCompleted: Boolean): ElvisExitNode {
        konst exitNode = exitElvisExpressionNodes.pop()
        addNewSimpleNode(exitNode, isDead = lhsIsNotNull)
        mergeDataFlowFromPostponedLambdas(exitNode, callCompleted)
        exitNode.updateDeadStatus()
        return exitNode
    }

    // -------------------------------------------------------------------------------------------------------------------------

    fun reset() {
        enterToLocalClassesMembers.clear()
        postponedLambdaExits.reset()
        lastNodes.reset()
    }

    // ----------------------------------- Edge utils -----------------------------------

    private fun addNewSimpleNode(node: CFGNode<*>, isDead: Boolean = false) {
        addEdge(lastNodes.pop(), node, preferredKind = if (isDead) EdgeKind.DeadForward else EdgeKind.Forward)
        lastNodes.push(node)
        addExceptionEdgesFrom(node)
    }

    private fun addNonSuccessfullyTerminatingNode(node: CFGNode<*>) {
        popAndAddEdge(node)
        konst stub = createStubNode()
        addEdge(node, stub)
        lastNodes.push(stub)
        addExceptionEdgesFrom(node)
    }

    private fun popAndAddEdge(to: CFGNode<*>, preferredKind: EdgeKind = EdgeKind.Forward) {
        addEdge(lastNodes.pop(), to, preferredKind = preferredKind)
    }

    private fun addEdge(
        from: CFGNode<*>,
        to: CFGNode<*>,
        propagateDeadness: Boolean = true,
        isDead: Boolean = false,
        preferredKind: EdgeKind = EdgeKind.Forward,
        label: EdgeLabel = NormalPath
    ) {
        konst kind = if (isDead || from.isDead || to.isDead) {
            if (preferredKind.isBack) EdgeKind.DeadBackward else EdgeKind.DeadForward
        } else preferredKind
        CFGNode.addEdge(from, to, kind, propagateDeadness, label)
    }

    private fun addEdgeToSubGraph(from: CFGNode<*>, to: CFGNode<*>) {
        konst wasDead = to.isDead
        konst isDead = wasDead || from.isDead
        // Can only add control flow since data flow for every node that follows `to` has already been computed.
        CFGNode.addEdge(from, to, if (isDead) EdgeKind.DeadForward else EdgeKind.CfgForward, propagateDeadness = true)
        if (isDead && !wasDead) {
            propagateDeadnessForward(to)
        }
    }

    private fun addBackEdge(from: CFGNode<*>, to: CFGNode<*>, isDead: Boolean = false, label: EdgeLabel = NormalPath) {
        konst kind = if (isDead || from.isDead || to.isDead) EdgeKind.DeadBackward else EdgeKind.CfgBackward
        CFGNode.addEdge(from, to, kind, propagateDeadness = false, label = label)
    }

    private fun propagateDeadnessForward(node: CFGNode<*>) {
        if (!node.isDead) return
        for (next in node.followingNodes) {
            konst kind = node.edgeTo(next).kind
            if (CFGNode.killEdge(node, next, propagateDeadness = false) && !kind.isBack && kind.usedInCfa) {
                next.updateDeadStatus()
                propagateDeadnessForward(next)
            }
        }
    }
}

fun FirDeclaration?.isLocalClassOrAnonymousObject() = ((this as? FirRegularClass)?.isLocal == true) || this is FirAnonymousObject
