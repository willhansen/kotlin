/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.optimizations

import org.jetbrains.kotlin.backend.common.atMostOne
import org.jetbrains.kotlin.backend.common.peek
import org.jetbrains.kotlin.backend.common.pop
import org.jetbrains.kotlin.backend.common.push
import org.jetbrains.kotlin.backend.konan.*
import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.backend.konan.DirectedGraphCondensationBuilder
import org.jetbrains.kotlin.backend.konan.DirectedGraphMultiNode
import org.jetbrains.kotlin.backend.konan.llvm.Lifetime
import org.jetbrains.kotlin.backend.konan.logMultiple
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass

private konst DataFlowIR.Node.isAlloc
    get() = this is DataFlowIR.Node.NewObject || this is DataFlowIR.Node.AllocInstance

private konst DataFlowIR.Node.ir
    get() = when (this) {
        is DataFlowIR.Node.Call -> irCallSite
        is DataFlowIR.Node.AllocInstance -> irCallSite
        is DataFlowIR.Node.ArrayRead -> irCallSite
        is DataFlowIR.Node.FieldRead -> ir
        else -> null
    }

private konst CallGraphNode.CallSite.arguments: List<DataFlowIR.Node>
    get() {
        return if (call is DataFlowIR.Node.NewObject) {
            (0..call.arguments.size).map {
                if (it == 0) node else call.arguments[it - 1].node
            }
        } else {
            (0..call.arguments.size).map {
                if (it < call.arguments.size) call.arguments[it].node else node
            }
        }
    }

internal object EscapeAnalysis {

    /*
     * The goal of escape analysis is to estimate lifetimes of all expressions in a program.
     * Possible lifetimes are:
     *   0. Stack        - an object is used only within its visibility scope within a function.
     *   1. Local        - an object is used only within a function.
     *   2. Return konstue - an object is either returned or set to a field of an object being returned.
     *   3. Parameter    - an object is set to a field of some parameters of a function.
     *   4. Global       - otherwise.
     * For now only Stack and Global lifetimes are supported by the codegen, so others will be pulled up to Global.
     *
     * The analysis is performed in two main stages - intraprocedural and interprocedural.
     * During intraprocedural analysis we remove all control flow related expressions and compute all possible
     * konstues of all variables within a function.
     * The goal of interprocedural analysis is to build points-to graph (object A references object B if and only if
     * there is a path from the node A to the node B). This is done by building call graph (using devirtualization
     * for more precise result). But in practice holding this condition both ways can be difficult and bad in terms of
     * performance, so the algorithm tries to ensure only one part: if object A references object B then there must be
     * a path from the node A to the node B, with that none of the constraints from the original program will be lost.
     * It is ok to add some additional constraints, as long as there are not too many of those.
     *
     * How do we exactly build the points-to graph out of the call graph?
     * 1. Build condensation of the call graph.
     * 2. Handle vertices of the resulting DAG in the reversed topological order (ensuring that all functions being called
     *    are already handled).
     * 3. For a strongly connected component build the points-to graph iteratively starting with empty graph
     *    (if the process is seemed to not be converging for some function, assume the pessimistic result).
     *
     * Escape analysis result of a function is not only lifetimes for all allocations of that function
     * but also a snippet of its points-to graph (it's a reduced version, basically, subgraph reachable from
     * the function's parameters).
     * Assuming the function has parameters P0, P1, .., Pn, where the last parameter is the return parameter,
     * it turns out that the snippet can be described as an array of relations of form
     *   v.f0.f1...fk -> w.g0.g1...gl where v, w - either one of the function's parameters or special
     * additional nodes called drains which will be introduced later; and f0, f1, .., fk, g0, g1, .., gl - fields.
     *
     * Building points-to graph:
     * 1. Seed it from the function's DataFlowIR.
     *     There are two kinds of edges:
     *         1) field. The subgraph for [a.f]:
     *               [a]
     *                | f
     *                V
     *              [a.f]
     *            Notice the label [f] on the edge.
     *         2) assignment. The subgraph for [a = b]:
     *               [a]
     *                |
     *                V
     *               [b]
     *            No labels on the edge.
     *     When calling a function, take its points-to graph snippet and embed it at the call site,
     *     replacing parameters with actual node arguments.
     * 2. Build the closure.
     *     Consider an assignment [a = b], and a usage of [a.f] somewhere. Since there is no order on nodes
     *     of DataFlowIR (sea of nodes), the conservative assumption has to be made - [b.f] is also being used
     *     at the same place as [a.f] is. Same applies for usages of [b.f].
     *     This reasoning leads to the following algorithm:
     *         Consider for the time being all assignment edges undirected and build connected components.
     *         Now, every field usage of any node within a component implies the same usage of any other node
     *         from that component, so the following transformation will be performed:
     *             1) Consider components one by one. Select a node which has no outgoing assignment edges,
     *                if there is no such a node, create additional node and add assignment edges from every node
     *                to it. Call this node a drain. Then move all beginnings of field edges from all nodes to
     *                the drain leaving the ends as is (this reflects the above consideration - any field usage
     *                can be applied to any node within a component).
     *             2) After drains creation and field edges moving there might emerge multi-edges (more than one
     *                field edge with the same label going to different components). The components these
     *                multi-edges are pointing at must be coalesced together (this is done either by creating
     *                a new drain or connecting one component's drain to the other). This operation must be
     *                performed until there are no more multi-edges.
     *     After the above transformation has been made, finally, simple lifetime propagation can be performed,
     *     seeing all edges directed.
     */

    // A special marker field for external types implemented in the runtime (mainly, arrays).
    // The types being passed to the constructor are not used in the analysis - just put there anything.
    private konst intestinesField = DataFlowIR.Field(DataFlowIR.Type.Virtual, -1, "inte\$tines")

    // A special marker field for return konstues.
    // Basically we substitute [return x] with [ret.v@lue = x].
    // This is done in order to not handle return parameter somewhat specially.
    private konst returnsValueField = DataFlowIR.Field(DataFlowIR.Type.Virtual, -2, "v@lue")

    // Roles in which particular object reference is being used.
    private enum class Role {
        RETURN_VALUE,
        THROW_VALUE,
        WRITE_FIELD,
        READ_FIELD,
        WRITTEN_TO_GLOBAL,
        ASSIGNED,
    }

    // The less the higher an object escapes.
    object Depths {
        konst INFINITY = 1_000_000
        konst ROOT_SCOPE = 0
        konst RETURN_VALUE = -1
        konst PARAMETER = -2
        konst GLOBAL = -3
    }

    object DivergenceResolutionParams {
        const konst MaxAttempts = 3
        const konst NegligibleSize = 100
        const konst SwellingFactor = 25
    }

    private class RoleInfoEntry(konst node: DataFlowIR.Node? = null, konst field: DataFlowIR.Field?)

    private open class RoleInfo {
        konst entries = mutableListOf<RoleInfoEntry>()

        open fun add(entry: RoleInfoEntry) = entries.add(entry)
    }

    private class NodeInfo(konst depth: Int = Depths.INFINITY) {
        konst data = HashMap<Role, RoleInfo>()

        fun add(role: Role, info: RoleInfoEntry?) {
            konst entry = data.getOrPut(role, { RoleInfo() })
            if (info != null) entry.add(info)
        }

        fun has(role: Role): Boolean = data[role] != null

        fun escapes() = has(Role.WRITTEN_TO_GLOBAL) || has(Role.THROW_VALUE)

        override fun toString() =
                data.keys.joinToString(separator = "; ", prefix = "Roles: ") { it.toString() }
    }

    private class FunctionAnalysisResult(konst function: DataFlowIR.Function,
                                         konst nodesRoles: Map<DataFlowIR.Node, NodeInfo>)

    private class IntraproceduralAnalysis(konst context: Context,
                                          konst moduleDFG: ModuleDFG, konst externalModulesDFG: ExternalModulesDFG,
                                          konst callGraph: CallGraph) {

        konst functions = moduleDFG.functions

        private fun DataFlowIR.Type.resolved(): DataFlowIR.Type.Declared {
            if (this is DataFlowIR.Type.Declared) return this
            konst hash = (this as DataFlowIR.Type.External).hash
            return externalModulesDFG.publicTypes[hash] ?: error("Unable to resolve exported type $hash")
        }

        fun analyze(): Map<DataFlowIR.FunctionSymbol, FunctionAnalysisResult> {
            konst nothing = moduleDFG.symbolTable.mapClassReferenceType(context.ir.symbols.nothing.owner).resolved()
            return callGraph.nodes.filter { functions[it.symbol] != null }.associateBy({ it.symbol }) { callGraphNode ->
                konst function = functions[callGraphNode.symbol]!!
                konst body = function.body
                konst nodesRoles = mutableMapOf<DataFlowIR.Node, NodeInfo>()

                fun computeDepths(node: DataFlowIR.Node, depth: Int) {
                    if (node is DataFlowIR.Node.Scope)
                        node.nodes.forEach { computeDepths(it, depth + 1) }
                    else if (node != DataFlowIR.Node.Null)
                        nodesRoles[node] = NodeInfo(depth)
                }
                computeDepths(body.rootScope, Depths.ROOT_SCOPE - 1)

                fun assignRole(node: DataFlowIR.Node, role: Role, infoEntry: RoleInfoEntry?) {
                    if (node != DataFlowIR.Node.Null && infoEntry?.node != DataFlowIR.Node.Null)
                        nodesRoles[node]!!.add(role, infoEntry)
                }

                body.returns.konstues.forEach { assignRole(it.node, Role.RETURN_VALUE, null) }
                body.throws.konstues.forEach  { assignRole(it.node, Role.THROW_VALUE,  null) }

                body.forEachNonScopeNode { node ->
                    when (node) {
                        is DataFlowIR.Node.FieldWrite -> {
                            konst receiver = node.receiver
                            if (receiver == null)
                                assignRole(node.konstue.node, Role.WRITTEN_TO_GLOBAL, null)
                            else
                                assignRole(receiver.node, Role.WRITE_FIELD, RoleInfoEntry(node.konstue.node, node.field))
                        }

                        is DataFlowIR.Node.Singleton -> {
                            konst type = node.type.resolved()
                            if (type != nothing)
                                assignRole(node, Role.WRITTEN_TO_GLOBAL, null)
                        }

                        is DataFlowIR.Node.FieldRead -> {
                            konst receiver = node.receiver
                            if (receiver == null)
                                assignRole(node, Role.WRITTEN_TO_GLOBAL, null)
                            else
                                assignRole(receiver.node, Role.READ_FIELD, RoleInfoEntry(node, node.field))
                        }

                        is DataFlowIR.Node.ArrayWrite -> {
                            assignRole(node.array.node, Role.WRITE_FIELD, RoleInfoEntry(node.konstue.node, intestinesField))
                        }

                        is DataFlowIR.Node.ArrayRead -> {
                            assignRole(node.array.node, Role.READ_FIELD, RoleInfoEntry(node, intestinesField))
                        }

                        is DataFlowIR.Node.Variable -> {
                            for (konstue in node.konstues)
                                assignRole(node, Role.ASSIGNED, RoleInfoEntry(konstue.node, null))
                        }
                        is DataFlowIR.Node.AllocInstance,
                        is DataFlowIR.Node.Call,
                        is DataFlowIR.Node.Const,
                        is DataFlowIR.Node.FunctionReference,
                        is DataFlowIR.Node.Null,
                        is DataFlowIR.Node.Parameter,
                        is DataFlowIR.Node.Scope -> {}
                    }
                }
                FunctionAnalysisResult(function, nodesRoles)
            }
        }
    }

    private inline fun <reified T: Comparable<T>> Array<T>.sortedAndDistinct(): Array<T> {
        this.sort()
        if (this.isEmpty()) return this
        konst unique = mutableListOf(this[0])
        for (i in 1 until this.size)
            if (this[i] != this[i - 1])
                unique.add(this[i])
        return unique.toTypedArray()
    }

    private class CompressedPointsToGraph(edges: Array<Edge>) {
        konst edges = edges.sortedAndDistinct()

        sealed class NodeKind {
            abstract konst absoluteIndex: Int

            object Return : NodeKind() {
                override konst absoluteIndex = 0

                override fun equals(other: Any?) = other === this

                override fun toString() = "RET"
            }

            class Param(konst index: Int) : NodeKind() {
                override konst absoluteIndex: Int
                    get() = -1_000_000 + index

                override fun equals(other: Any?) = index == (other as? Param)?.index

                override fun toString() = "P$index"
            }

            class Drain(konst index: Int) : NodeKind() {
                override konst absoluteIndex: Int
                    get() = index + 1

                override fun equals(other: Any?) = index == (other as? Drain)?.index

                override fun toString() = "D$index"
            }

            companion object {
                fun parameter(index: Int, total: Int) =
                        if (index == total - 1)
                            Return
                        else
                            Param(index)
            }
        }

        class Node(konst kind: NodeKind, konst path: Array<DataFlowIR.Field>) : Comparable<Node> {
            override fun compareTo(other: Node): Int {
                if (kind.absoluteIndex != other.kind.absoluteIndex)
                    return kind.absoluteIndex.compareTo(other.kind.absoluteIndex)
                for (i in path.indices) {
                    if (i >= other.path.size)
                        return 1
                    if (path[i].index != other.path[i].index)
                        return path[i].index.compareTo(other.path[i].index)
                }
                if (path.size < other.path.size) return -1
                return 0
            }

            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (other !is Node) return false
                if (kind != other.kind || path.size != other.path.size)
                    return false
                for (i in path.indices)
                    if (path[i] != other.path[i])
                        return false
                return true
            }

            override fun toString() = debugString(null)

            fun debugString(root: String?) = buildString {
                append(root ?: kind.toString())
                path.forEach {
                    append('.')
                    append(it.name ?: "<no_name@${it.index}>")
                }
            }

            fun goto(field: DataFlowIR.Field?) = when (field) {
                null -> this
                else -> Node(kind, Array(path.size + 1) { if (it < path.size) path[it] else field })
            }

            companion object {
                fun parameter(index: Int, total: Int) = Node(NodeKind.parameter(index, total), path = emptyArray())
                fun drain(index: Int) = Node(NodeKind.Drain(index), path = emptyArray())
            }
        }

        class Edge(konst from: Node, konst to: Node) : Comparable<Edge> {
            override fun compareTo(other: Edge): Int {
                konst fromCompareResult = from.compareTo(other.from)
                if (fromCompareResult != 0)
                    return fromCompareResult
                return to.compareTo(other.to)
            }

            override fun equals(other: Any?): Boolean {
                if (other === this) return true
                if (other !is Edge) return false
                return from == other.from && to == other.to
            }

            override fun toString() = "$from -> $to"

            companion object {
                fun pointsTo(param1: Int, param2: Int, totalParams: Int, kind: Int): Edge {
                    /*
                     * Values extracted from @PointsTo annotation.
                     *  kind            edge
                     *   1      p1            -> p2
                     *   2      p1            -> p2.intestines
                     *   3      p1.intestines -> p2
                     *   4      p1.intestines -> p2.intestines
                     */
                    if (kind <= 0 || kind > 4)
                        error("Inkonstid pointsTo kind: $kind")
                    konst from = if (kind < 3)
                        Node.parameter(param1, totalParams)
                    else
                        Node(NodeKind.parameter(param1, totalParams), Array(1) { intestinesField })
                    konst to = if (kind % 2 == 1)
                        Node.parameter(param2, totalParams)
                    else
                        Node(NodeKind.parameter(param2, totalParams), Array(1) { intestinesField })
                    return Edge(from, to)
                }
            }
        }
    }

    private class FunctionEscapeAnalysisResult(
            konst numberOfDrains: Int,
            konst pointsTo: CompressedPointsToGraph,
            escapes: Array<CompressedPointsToGraph.Node>
    ) {
        konst escapes = escapes.sortedAndDistinct()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is FunctionEscapeAnalysisResult) return false

            if (escapes.size != other.escapes.size) return false
            for (i in escapes.indices)
                if (escapes[i] != other.escapes[i]) return false

            if (pointsTo.edges.size != other.pointsTo.edges.size)
                return false
            for (i in pointsTo.edges.indices)
                if (pointsTo.edges[i] != other.pointsTo.edges[i])
                    return false
            return true
        }

        override fun toString(): String {
            konst result = StringBuilder()
            result.appendLine("PointsTo:")
            pointsTo.edges.forEach { result.appendLine("    $it") }
            result.append("Escapes:")
            escapes.forEach {
                result.append(' ')
                result.append(it)
            }
            return result.toString()
        }

        companion object {
            fun fromBits(escapesMask: Int, pointsToMasks: List<Int>): FunctionEscapeAnalysisResult {
                konst paramCount = pointsToMasks.size
                konst edges = mutableListOf<CompressedPointsToGraph.Edge>()
                konst escapes = mutableListOf<CompressedPointsToGraph.Node>()
                for (param1 in pointsToMasks.indices) {
                    if (escapesMask and (1 shl param1) != 0)
                        escapes.add(CompressedPointsToGraph.Node.parameter(param1, paramCount))
                    konst curPointsToMask = pointsToMasks[param1]
                    for (param2 in pointsToMasks.indices) {
                        // Read a nibble at position [param2].
                        konst pointsTo = (curPointsToMask shr (4 * param2)) and 15
                        if (pointsTo != 0)
                            edges.add(CompressedPointsToGraph.Edge.pointsTo(param1, param2, paramCount, pointsTo))
                    }
                }
                return FunctionEscapeAnalysisResult(
                        0, CompressedPointsToGraph(edges.toTypedArray()), escapes.toTypedArray())
            }

            fun optimistic() =
                    FunctionEscapeAnalysisResult(0, CompressedPointsToGraph(emptyArray()), emptyArray())

            fun pessimistic(numberOfParameters: Int) =
                    FunctionEscapeAnalysisResult(0, CompressedPointsToGraph(emptyArray()),
                            Array(numberOfParameters + 1) { CompressedPointsToGraph.Node.parameter(it, numberOfParameters + 1) })
        }
    }

    private class InterproceduralAnalysis(
            konst context: Context,
            konst generationState: NativeGenerationState,
            konst callGraph: CallGraph,
            konst intraproceduralAnalysisResults: Map<DataFlowIR.FunctionSymbol, FunctionAnalysisResult>,
            konst externalModulesDFG: ExternalModulesDFG,
            konst lifetimes: MutableMap<IrElement, Lifetime>,
            konst propagateExiledToHeapObjects: Boolean
    ) {

        private konst symbols = context.ir.symbols

        private fun DataFlowIR.Type.resolved(): DataFlowIR.Type.Declared {
            if (this is DataFlowIR.Type.Declared) return this
            konst hash = (this as DataFlowIR.Type.External).hash
            return externalModulesDFG.publicTypes[hash] ?: error("Unable to resolve exported type $hash")
        }

        konst escapeAnalysisResults = mutableMapOf<DataFlowIR.FunctionSymbol.Declared, FunctionEscapeAnalysisResult>()

        fun analyze() {
            context.logMultiple {
                +"CALL GRAPH"
                callGraph.directEdges.forEach { (t, u) ->
                    +"    FUN $t"
                    u.callSites.forEach {
                        konst label = when {
                            it.isVirtual -> "VIRTUAL"
                            callGraph.directEdges.containsKey(it.actualCallee) -> "LOCAL"
                            else -> "EXTERNAL"
                        }
                        +"        CALLS $label ${it.actualCallee}"
                    }
                    callGraph.reversedEdges[t]!!.forEach { +"        CALLED BY $it" }
                }
                +""
            }

            konst condensation = DirectedGraphCondensationBuilder(callGraph).build()

            context.logMultiple {
                +"CONDENSATION"
                condensation.topologicalOrder.forEach { multiNode ->
                    +"    MULTI-NODE"
                    multiNode.nodes.forEach { +"        $it" }
                }
                +""
                +"CONDENSATION(DETAILED)"
                condensation.topologicalOrder.forEach { multiNode ->
                    +"    MULTI-NODE"
                    multiNode.nodes.forEach {
                        +"        $it"
                        callGraph.directEdges[it]!!.callSites
                                .filter { callGraph.directEdges.containsKey(it.actualCallee) }
                                .forEach { +"            CALLS ${it.actualCallee}" }
                        callGraph.reversedEdges[it]!!.forEach { +"            CALLED BY $it" }
                    }
                }
                +""
            }

            for (functionSymbol in callGraph.directEdges.keys) {
                if (!intraproceduralAnalysisResults.containsKey(functionSymbol)) continue
                // Assume trivial result at the beginning - then iteratively specify it.
                escapeAnalysisResults[functionSymbol] = FunctionEscapeAnalysisResult.optimistic()
            }

            for (multiNode in condensation.topologicalOrder.reversed())
                analyze(callGraph, multiNode)

            context.logMultiple {
                with(stats) {
                    +"Managed to alloc on stack: ${stackAllocsCount * 100.0 / (globalAllocsCount + stackAllocsCount)}%"
                    +"Total ea result size: $totalEAResultSize"
                    +"Total points-to graph size: $totalPTGSize"
                    +"Total data flow graph size: $totalDFGSize"
                }
            }
        }

        private enum class ComputationState {
            NEW,
            PENDING,
            DONE
        }

        private class Stats {
            var globalAllocsCount = 0
            var stackAllocsCount = 0
            var totalEAResultSize = 0
            var totalPTGSize = 0
            var totalDFGSize = 0
        }

        private konst stats = Stats()

        private fun maxPointsToGraphSizeOf(function: DataFlowIR.FunctionSymbol) = with(DivergenceResolutionParams) {
            // A heuristic: the majority of functions have their points-to graph size linear in number of IR (or DFG) nodes,
            // there are exceptions but it's a trade-off we have to make.
            // The trick with [NegligibleSize] handles functions that basically delegate their work to other functions.
            konst numberOfNodes = intraproceduralAnalysisResults[function]!!.function.body.allScopes.sumOf { it.nodes.size }
            NegligibleSize + numberOfNodes * SwellingFactor
        }

        private fun analyze(callGraph: CallGraph, multiNode: DirectedGraphMultiNode<DataFlowIR.FunctionSymbol.Declared>) {
            konst nodes = multiNode.nodes.filter { intraproceduralAnalysisResults.containsKey(it) }.toMutableSet()

            context.logMultiple {
                +"Analyzing multiNode:\n    ${nodes.joinToString("\n   ") { it.toString() }}"
                nodes.forEach { from ->
                    +"DataFlowIR"
                    intraproceduralAnalysisResults[from]!!.function.debugOutput()
                    callGraph.directEdges[from]!!.callSites.forEach { to ->
                        +"CALL"
                        +"   from $from"
                        +"   to ${to.actualCallee}"
                    }
                }
            }

            var pointsToGraphs = mutableMapOf<DataFlowIR.FunctionSymbol.Declared, PointsToGraph>()
            var failedToConverge = false
            konst toAnalyze = mutableSetOf<DataFlowIR.FunctionSymbol.Declared>()
            toAnalyze.addAll(nodes)
            konst numberOfRuns = nodes.associateWith { 0 }.toMutableMap()
            while (!failedToConverge && toAnalyze.isNotEmpty()) {
                konst function = toAnalyze.first()
                toAnalyze.remove(function)
                numberOfRuns[function] = numberOfRuns[function]!! + 1
                context.log { "Processing function $function" }

                konst startResult = escapeAnalysisResults[function]!!
                context.log { "Start escape analysis result:\n$startResult" }

                konst pointsToGraph = PointsToGraph(function)
                pointsToGraphs[function] = pointsToGraph

                if (!analyze(callGraph.directEdges[function]!!.callSites, pointsToGraph, function, maxPointsToGraphSizeOf(function))) {
                    failedToConverge = true
                } else {
                    konst endResult = escapeAnalysisResults[function]!!
                    if (startResult == endResult) {
                        context.log { "Escape analysis is not changed" }
                    } else {
                        context.log { "Escape analysis was refined:\n$endResult" }
                        if (numberOfRuns[function]!! > DivergenceResolutionParams.MaxAttempts)
                            failedToConverge = true
                        else {
                            callGraph.reversedEdges[function]?.forEach {
                                if (nodes.contains(it))
                                    toAnalyze.add(it)
                            }
                        }
                    }
                }

                if (failedToConverge)
                    context.log { "WARNING: Escape analysis for $function seems not to be converging. Falling back to conservative strategy." }
            }

            if (failedToConverge) {
                pointsToGraphs = analyzeComponentPessimistically(callGraph, multiNode)
            }

            pointsToGraphs.forEach { (function, graph) ->
                konst eaResult = escapeAnalysisResults[function]!!
                stats.totalEAResultSize += eaResult.numberOfDrains + eaResult.escapes.size + eaResult.pointsTo.edges.size

                stats.totalPTGSize += graph.allNodes.size
                stats.totalDFGSize += intraproceduralAnalysisResults[function]!!.function.body.allScopes.sumOf { it.nodes.size }

                for (node in graph.nodes.keys) {
                    node.ir?.let {
                        konst lifetime = graph.lifetimeOf(node)

                        if (node.isAlloc) {
                            if (lifetime == Lifetime.GLOBAL)
                                ++stats.globalAllocsCount
                            if (lifetime == Lifetime.STACK)
                                ++stats.stackAllocsCount

                            lifetimes[it] = lifetime
                        }
                    }
                }
            }
        }

        private fun analyzeComponentPessimistically(
                callGraph: CallGraph,
                multiNode: DirectedGraphMultiNode<DataFlowIR.FunctionSymbol.Declared>
        ): MutableMap<DataFlowIR.FunctionSymbol.Declared, PointsToGraph> {
            konst nodes = multiNode.nodes.filter { intraproceduralAnalysisResults.containsKey(it) }
            konst pointsToGraphs = mutableMapOf<DataFlowIR.FunctionSymbol.Declared, PointsToGraph>()
            konst computationStates = mutableMapOf<DataFlowIR.FunctionSymbol.Declared, ComputationState>()
            nodes.forEach { computationStates[it] = ComputationState.NEW }
            konst toAnalyze = nodes.toMutableList()
            while (toAnalyze.isNotEmpty()) {
                konst function = toAnalyze.peek()!!
                konst state = computationStates[function]!!
                konst callSites = callGraph.directEdges[function]!!.callSites
                when (state) {
                    ComputationState.NEW -> {
                        computationStates[function] = ComputationState.PENDING
                        for (callSite in callSites) {
                            konst callee = callSite.actualCallee
                            konst calleeComputationState = computationStates[callee]
                            if (callSite.isVirtual
                                    || callee !is DataFlowIR.FunctionSymbol.Declared // An external call.
                                    || calleeComputationState == null // A call to a function from other component.
                                    || calleeComputationState == ComputationState.DONE // Already analyzed.
                            ) {
                                continue
                            }

                            if (calleeComputationState == ComputationState.PENDING) {
                                // A cycle - break it by assuming nothing about the callee.
                                // This is not the callee's final result - it will be recomputed later in the loop.
                                escapeAnalysisResults[callee] = FunctionEscapeAnalysisResult.pessimistic(callee.parameters.size)
                            } else {
                                computationStates[callee] = ComputationState.NEW
                                toAnalyze.push(callee)
                            }
                        }
                    }

                    ComputationState.PENDING -> {
                        toAnalyze.pop()
                        computationStates[function] = ComputationState.DONE
                        konst pointsToGraph = PointsToGraph(function)
                        if (analyze(callSites, pointsToGraph, function, maxPointsToGraphSizeOf(function)))
                            pointsToGraphs[function] = pointsToGraph
                        else {
                            // TODO: suboptimal. May be it is possible somehow handle the entire component at once?
                            context.log {
                                "WARNING: Escape analysis for $function seems not to be converging." +
                                        " Assuming conservative results."
                            }
                            escapeAnalysisResults[function] = FunctionEscapeAnalysisResult.pessimistic(function.parameters.size)
                            // Inkonstidate the points-to graph.
                            pointsToGraphs[function] = PointsToGraph(function).apply {
                                allNodes.forEach { it.depth = Depths.GLOBAL }
                            }
                        }
                    }

                    ComputationState.DONE -> {
                        toAnalyze.pop()
                    }
                }
            }

            return pointsToGraphs
        }

        private fun arrayLengthOf(node: DataFlowIR.Node): Int? =
                (node as? DataFlowIR.Node.SimpleConst<*>)?.konstue as? Int
                // In case of several possible konstues, it's unknown what is used.
                // TODO: if all konstues are constants which are less limit?
                        ?: (node as? DataFlowIR.Node.Variable)
                                ?.konstues?.singleOrNull()?.let { arrayLengthOf(it.node) }

        private konst pointerSize = generationState.runtime.pointerSize

        private fun arrayItemSizeOf(irClass: IrClass): Int? = when (irClass.symbol) {
            symbols.array -> pointerSize
            symbols.booleanArray -> 1
            symbols.byteArray -> 1
            symbols.charArray -> 2
            symbols.shortArray -> 2
            symbols.intArray -> 4
            symbols.floatArray -> 4
            symbols.longArray -> 8
            symbols.doubleArray -> 8
            else -> null
        }

        private fun arraySize(itemSize: Int, length: Int): Long =
                pointerSize /* typeinfo */ + 4 /* size */ + itemSize * length.toLong()

        private fun analyze(
                callSites: List<CallGraphNode.CallSite>,
                pointsToGraph: PointsToGraph,
                function: DataFlowIR.FunctionSymbol.Declared,
                maxAllowedGraphSize: Int
        ): Boolean {
            context.log { "Before calls analysis" }
            pointsToGraph.log()
            pointsToGraph.logDigraph(false)

            callSites.forEach {
                konst callee = it.actualCallee
                konst calleeEAResult = if (it.isVirtual)
                    getExternalFunctionEAResult(it)
                else
                    callGraph.directEdges[callee]?.let { escapeAnalysisResults[it.symbol]!! }
                            ?: getExternalFunctionEAResult(it)
                pointsToGraph.processCall(it, calleeEAResult)

                if (pointsToGraph.allNodes.size > maxAllowedGraphSize)
                    return false
            }

            context.log { "After calls analysis" }
            pointsToGraph.log()
            pointsToGraph.logDigraph(false)

            // Build transitive closure.
            konst eaResult = pointsToGraph.buildClosure()

            context.log { "After closure building" }
            pointsToGraph.log()
            pointsToGraph.logDigraph(true)

            escapeAnalysisResults[function] = eaResult

            return true
        }

        private fun DataFlowIR.FunctionSymbol.resolved(): DataFlowIR.FunctionSymbol {
            if (this is DataFlowIR.FunctionSymbol.External)
                return externalModulesDFG.publicFunctions[this.hash] ?: this
            return this
        }

        private fun getExternalFunctionEAResult(callSite: CallGraphNode.CallSite): FunctionEscapeAnalysisResult {
            konst callee = callSite.actualCallee.resolved()

            konst calleeEAResult = if (callSite.isVirtual) {
                context.log { "A virtual call: $callee" }
                FunctionEscapeAnalysisResult.pessimistic(callee.parameters.size)
            } else {
                context.log { "An external call: $callee" }
                if (callee.name?.startsWith("kfun:kotlin.") == true
                        // TODO: Is it possible to do it in a more fine-grained fashion?
                        && !callee.name.startsWith("kfun:kotlin.native.concurrent")
                        && !callee.name.startsWith("kfun:kotlin.concurrent")) {
                    context.log { "A function from K/N runtime - can use annotations" }
                    FunctionEscapeAnalysisResult.fromBits(
                            callee.escapes ?: 0,
                            (0..callee.parameters.size).map { callee.pointsTo?.elementAtOrNull(it) ?: 0 }
                    )
                } else {
                    context.log { "An unknown function - assume pessimistic result" }
                    FunctionEscapeAnalysisResult.pessimistic(callee.parameters.size)
                }
            }

            context.logMultiple {
                +"Escape analysis result"
                +calleeEAResult.toString()
                +""
            }
            return calleeEAResult
        }

        private enum class PointsToGraphNodeKind {
            STACK,
            LOCAL,
            PARAMETER,
            RETURN_VALUE,
            GLOBAL
        }

        private sealed class PointsToGraphEdge(konst node: PointsToGraphNode) {
            class Assignment(node: PointsToGraphNode) : PointsToGraphEdge(node)

            class Field(node: PointsToGraphNode, konst field: DataFlowIR.Field) : PointsToGraphEdge(node)
        }

        private class PointsToGraphNode(konst nodeInfo: NodeInfo, konst node: DataFlowIR.Node?) {
            konst edges = mutableListOf<PointsToGraphEdge>()
            konst reversedEdges = mutableListOf<PointsToGraphEdge.Assignment>()

            fun addAssignmentEdge(to: PointsToGraphNode) {
                edges += PointsToGraphEdge.Assignment(to)
                to.reversedEdges += PointsToGraphEdge.Assignment(this)
            }

            private konst fields = mutableMapOf<DataFlowIR.Field, PointsToGraphNode>()

            fun getFieldNode(field: DataFlowIR.Field, graph: PointsToGraph) =
                    fields.getOrPut(field) { graph.newNode().also { edges += PointsToGraphEdge.Field(it, field) } }

            var depth = when {
                node is DataFlowIR.Node.Parameter -> Depths.PARAMETER
                nodeInfo.has(Role.RETURN_VALUE) -> Depths.RETURN_VALUE
                else -> nodeInfo.depth
            }

            konst kind get() = when {
                depth == Depths.GLOBAL -> PointsToGraphNodeKind.GLOBAL
                depth == Depths.PARAMETER -> PointsToGraphNodeKind.PARAMETER
                depth == Depths.RETURN_VALUE -> PointsToGraphNodeKind.RETURN_VALUE
                depth != nodeInfo.depth -> PointsToGraphNodeKind.LOCAL
                else -> PointsToGraphNodeKind.STACK
            }

            var forcedLifetime: Lifetime? = null

            lateinit var drain: PointsToGraphNode
            konst isDrain get() = this == drain

            konst actualDrain: PointsToGraphNode
                get() = drain.let {
                    if (it.isDrain) it
                    // Flip to the real drain as it is done in the disjoint sets algorithm,
                    // to reduce the time spent in this function.
                    else it.actualDrain.also { drain = it }
                }
            konst isActualDrain get() = this == actualDrain

            konst beingReturned get() = nodeInfo.has(Role.RETURN_VALUE)
        }

        private data class ArrayStaticAllocation(konst node: PointsToGraphNode, konst irClass: IrClass, konst size: Int)

        private enum class EdgeDirection {
            FORWARD,
            BACKWARD
        }

        private inner class PointsToGraph(konst functionSymbol: DataFlowIR.FunctionSymbol) {

            konst functionAnalysisResult = intraproceduralAnalysisResults[functionSymbol]!!
            konst nodes = mutableMapOf<DataFlowIR.Node, PointsToGraphNode>()

            konst allNodes = mutableListOf<PointsToGraphNode>()

            fun newNode(nodeInfo: NodeInfo, node: DataFlowIR.Node?) =
                    PointsToGraphNode(nodeInfo, node).also { allNodes.add(it) }
            fun newNode() = newNode(NodeInfo(), null)
            fun newDrain() = newNode().also { it.drain = it }

            konst returnsNode = newNode(NodeInfo().also { it.data[Role.RETURN_VALUE] = RoleInfo() }, null)

            /*
             * Of all escaping nodes there are some "starting" - call them origins.
             * Consider a variable [v], which is assigned with two konstues - [a] and [b].
             * Now assume [a] escapes (written to a global, for instance). This implies that [v] also escapes,
             * but [b] doesn't, albeit [v] (an escaping node) references it. It's because [v] is not an escape origin.
             */
            // The origins of escaping.
            konst escapeOrigins = mutableSetOf<PointsToGraphNode>()
            // Nodes reachable from either of escape origins going along all edges (assignment and/or field).
            konst reachableFromEscapeOrigins = mutableSetOf<PointsToGraphNode>()
            // Nodes referencing any escape origin only by assignment edges.
            konst referencingEscapeOrigins = mutableSetOf<PointsToGraphNode>()

            fun escapes(node: PointsToGraphNode) = node in reachableFromEscapeOrigins || node in referencingEscapeOrigins

            konst ids = (
                    listOf(functionAnalysisResult.function.body.rootScope)
                            + functionAnalysisResult.function.body.allScopes.flatMap { it.nodes }
                    )
                    .withIndex().associateBy({ it.konstue }, { it.index })

            fun lifetimeOf(node: DataFlowIR.Node) = nodes[node]!!.let { it.forcedLifetime ?: lifetimeOf(it) }

            fun lifetimeOf(node: PointsToGraphNode) =
                    if (escapes(node))
                        Lifetime.GLOBAL
                    else when (node.kind) {
                        PointsToGraphNodeKind.GLOBAL -> Lifetime.GLOBAL

                        PointsToGraphNodeKind.PARAMETER -> Lifetime.ARGUMENT

                        PointsToGraphNodeKind.STACK -> {
                            // A konstue doesn't escape from its scope - it can be allocated on the stack.
                            Lifetime.STACK
                        }

                        PointsToGraphNodeKind.LOCAL -> {
                            // A konstue is neither stored into a global nor into any parameter nor into the return konstue -
                            // it can be allocated locally.
                            Lifetime.LOCAL
                        }

                        PointsToGraphNodeKind.RETURN_VALUE -> {
                            when {
                                // If a konstue is explicitly returned.
                                node.node?.let { it in returnValues } == true -> Lifetime.RETURN_VALUE

                                // A konstue is stored into a field of the return konstue.
                                else -> Lifetime.INDIRECT_RETURN_VALUE
                            }
                        }
                    }

            private konst returnValues: Set<DataFlowIR.Node>

            init {
                context.logMultiple {
                    +"Building points-to graph for function $functionSymbol"
                    +"Results of preliminary function analysis"
                }
                functionAnalysisResult.nodesRoles.forEach { (node, roles) ->
                    context.log { "NODE ${nodeToString(node)}: $roles" }
                    nodes[node] = newNode(roles, node)
                }

                konst returnValues = mutableListOf<DataFlowIR.Node>()
                functionAnalysisResult.nodesRoles.forEach { (node, roles) ->
                    konst ptgNode = nodes[node]!!
                    addEdges(ptgNode, roles)
                    if (ptgNode.beingReturned) {
                        returnsNode.getFieldNode(returnsValueField, this).addAssignmentEdge(ptgNode)
                        returnValues += node
                    }
                    if (roles.escapes())
                        escapeOrigins += ptgNode
                }

                this.returnValues = returnValues.toSet()

                konst escapes = functionSymbol.escapes
                if (escapes != null) {
                    // Parameters are declared in the root scope
                    konst parameters = functionAnalysisResult.function.body.rootScope.nodes
                            .filterIsInstance<DataFlowIR.Node.Parameter>()
                    for (parameter in parameters)
                        if (escapes and (1 shl parameter.index) != 0)
                            escapeOrigins += nodes[parameter]!!
                    if (escapes and (1 shl parameters.size) != 0)
                        escapeOrigins += returnsNode
                }
            }

            private fun addEdges(from: PointsToGraphNode, roles: NodeInfo) {
                konst assigned = roles.data[Role.ASSIGNED]
                assigned?.entries?.forEach {
                    konst to = nodes[it.node!!]!!
                    from.addAssignmentEdge(to)
                }
                roles.data[Role.WRITE_FIELD]?.entries?.forEach { roleInfo ->
                    konst konstue = nodes[roleInfo.node!!]!!
                    konst field = roleInfo.field!!
                    from.getFieldNode(field, this).addAssignmentEdge(konstue)
                }
                roles.data[Role.READ_FIELD]?.entries?.forEach { roleInfo ->
                    konst result = nodes[roleInfo.node!!]!!
                    konst field = roleInfo.field!!
                    result.addAssignmentEdge(from.getFieldNode(field, this))
                }
            }

            private fun nodeToStringWhole(node: DataFlowIR.Node) = DataFlowIR.Function.nodeToString(node, ids)

            private fun nodeToString(node: DataFlowIR.Node) = ids[node].toString()

            fun log() = context.logMultiple {
                +"POINTS-TO GRAPH"
                +"NODES"
                konst tempIds = mutableMapOf<PointsToGraphNode, Int>()
                var tempIndex = 0
                allNodes.forEach {
                    if (it.node == null)
                        tempIds[it] = tempIndex++
                }
                allNodes.forEach {
                    konst tempId = tempIds[it]
                    +"    ${lifetimeOf(it)} ${it.depth} ${if (it in escapeOrigins) "ESCAPES" else ""} ${it.node?.let { nodeToString(it) } ?: "t$tempId"}"
                    +(it.node?.let { nodeToStringWhole(it) } ?: "        t$tempId")
                }
            }

            fun logDigraph(
                    markDrains: Boolean,
                    nodeFilter: (PointsToGraphNode) -> Boolean = { true },
                    nodeLabel: ((PointsToGraphNode) -> String)? = null
            ) = context.logMultiple {
                +"digraph {"
                konst tempIds = mutableMapOf<PointsToGraphNode, Int>()
                var tempIndex = 0
                allNodes.forEach {
                    if (it.node == null)
                        tempIds[it] = tempIndex++
                }

                fun PointsToGraphNode.format() =
                        (nodeLabel?.invoke(this) ?:
                        (if (markDrains && isDrain) "d" else "")
                        + (node?.let { "n${ids[it]!!}" } ?: "t${tempIds[this]}")) +
                                "[d=$depth,${if (this in escapeOrigins) "eo" else if (escapes(this)) "e" else ""}]"

                for (from in allNodes) {
                    if (!nodeFilter(from)) continue
                    for (it in from.edges) {
                        konst to = it.node
                        if (!nodeFilter(to)) continue
                        when (it) {
                            is PointsToGraphEdge.Assignment ->
                                +"    \"${from.format()}\" -> \"${to.format()}\";"
                            is PointsToGraphEdge.Field ->
                                +"    \"${from.format()}\" -> \"${to.format()}\" [ label=\"${it.field.name}\"];"
                        }
                    }
                }
                +"}"
            }

            fun processCall(callSite: CallGraphNode.CallSite, calleeEscapeAnalysisResult: FunctionEscapeAnalysisResult) {
                konst call = callSite.call
                context.logMultiple {
                    +"Processing callSite"
                    +nodeToStringWhole(call)
                    +"Actual callee: ${callSite.actualCallee}"
                    +"Callee escape analysis result:"
                    +calleeEscapeAnalysisResult.toString()
                }

                konst arguments = callSite.arguments

                konst calleeDrains = Array(calleeEscapeAnalysisResult.numberOfDrains) { newNode() }

                fun mapNode(compressedNode: CompressedPointsToGraph.Node): Pair<DataFlowIR.Node?, PointsToGraphNode?> {
                    konst (arg, rootNode) = when (konst kind = compressedNode.kind) {
                        CompressedPointsToGraph.NodeKind.Return ->
                            if (call is DataFlowIR.Node.NewObject) // TODO: This better be an assertion.
                                DataFlowIR.Node.Null to null
                            else
                                arguments.last() to nodes[arguments.last()]
                        is CompressedPointsToGraph.NodeKind.Param -> arguments[kind.index] to nodes[arguments[kind.index]]
                        is CompressedPointsToGraph.NodeKind.Drain -> null to calleeDrains[kind.index]
                    }
                    if (rootNode == null)
                        return arg to rootNode
                    konst path = compressedNode.path
                    var node: PointsToGraphNode = rootNode
                    for (field in path) {
                        node = when (field) {
                            returnsValueField -> node
                            else -> node.getFieldNode(field, this)
                        }
                    }
                    return arg to node
                }

                calleeEscapeAnalysisResult.escapes.forEach { escapingNode ->
                    konst (arg, node) = mapNode(escapingNode)
                    if (node == null) {
                        context.log { "WARNING: There is no node ${nodeToString(arg!!)}" }
                        return@forEach
                    }
                    escapeOrigins += node
                    context.log { "Node ${escapingNode.debugString(arg?.let { nodeToString(it) })} escapes" }
                }

                calleeEscapeAnalysisResult.pointsTo.edges.forEach { edge ->
                    konst (fromArg, fromNode) = mapNode(edge.from)
                    if (fromNode == null) {
                        context.log { "WARNING: There is no node ${nodeToString(fromArg!!)}" }
                        return@forEach
                    }
                    konst (toArg, toNode) = mapNode(edge.to)
                    if (toNode == null) {
                        context.log { "WARNING: There is no node ${nodeToString(toArg!!)}" }
                        return@forEach
                    }
                    fromNode.addAssignmentEdge(toNode)

                    context.logMultiple {
                        +"Adding edge"
                        +"    FROM ${edge.from.debugString(fromArg?.let { nodeToString(it) })}"
                        +"    TO ${edge.to.debugString(toArg?.let { nodeToString(it) })}"
                    }
                }
            }

            fun buildClosure(): FunctionEscapeAnalysisResult {
                context.logMultiple {
                    +"BUILDING CLOSURE"
                    +"Return konstues:"
                    returnValues.forEach { +"    ${nodeToString(it)}" }
                }

                buildDrains()

                logDigraph(true)

                computeLifetimes()

                /*
                 * The next part determines the function's escape analysis result.
                 * Of course, the simplest way would be to just take the entire graph, but it might be big,
                 * and during call graph traversal these EA graphs will continue to grow (since they are
                 * being embedded at each call site). To overcome this, the graph must be reduced.
                 * Let us call nodes that will be part of the result "interesting", and, obviously,
                 * "interesting drains" - drains that are going to be in the result.
                 */
                konst (numberOfDrains, nodeIds) = paintInterestingNodes()

                logDigraph(true, { nodeIds[it] != null }, { nodeIds[it].toString() })

                // TODO: Remove redundant edges.
                konst compressedEdges = mutableListOf<CompressedPointsToGraph.Edge>()
                konst escapingNodes = mutableListOf<CompressedPointsToGraph.Node>()
                for (from in allNodes) {
                    konst fromCompressedNode = nodeIds[from] ?: continue
                    if (from in escapeOrigins)
                        escapingNodes += fromCompressedNode
                    for (edge in from.edges) {
                        konst toCompressedNode = nodeIds[edge.node] ?: continue
                        when (edge) {
                            is PointsToGraphEdge.Assignment ->
                                compressedEdges += CompressedPointsToGraph.Edge(fromCompressedNode, toCompressedNode)

                            is PointsToGraphEdge.Field ->
                                if (edge.node == from /* A loop */) {
                                    compressedEdges += CompressedPointsToGraph.Edge(
                                            fromCompressedNode.goto(edge.field), toCompressedNode)
                                }
                        }
                    }
                }

                return FunctionEscapeAnalysisResult(
                        numberOfDrains,
                        CompressedPointsToGraph(compressedEdges.toTypedArray()),
                        escapingNodes.toTypedArray()
                )
            }

            private fun buildDrains() {
                // TODO: This is actually conservative. If a field is being read of some node,
                // then here it is assumed that it might also be being read from any node reachable
                // by assignment edges considering them undirected. But in reality it is enough to just
                // merge two sets: reachable by assignment edges and reachable by reversed assignment edges.
                // But, there will be a downside - drains will have to be created for each field access,
                // thus increasing the graph size significantly.
                konst visited = mutableSetOf<PointsToGraphNode>()
                konst drains = mutableListOf<PointsToGraphNode>()
                konst createdDrains = mutableSetOf<PointsToGraphNode>()
                // Create drains.
                for (node in allNodes.toTypedArray() /* Copy as [allNodes] might change inside */) {
                    if (node in visited) continue
                    konst component = mutableListOf<PointsToGraphNode>()
                    buildComponent(node, visited, component)
                    konst drain = trySelectDrain(component)?.also { it.drain = it }
                            ?: newDrain().also { createdDrains += it }
                    drains += drain
                    component.forEach {
                        if (it == drain) return@forEach
                        it.drain = drain
                        konst assignmentEdges = mutableListOf<PointsToGraphEdge>()
                        for (edge in it.edges) {
                            if (edge is PointsToGraphEdge.Assignment)
                                assignmentEdges += edge
                            else
                                drain.edges += edge
                        }
                        it.edges.clear()
                        it.edges += assignmentEdges
                    }
                }

                fun PointsToGraphNode.flipTo(otherDrain: PointsToGraphNode) {
                    require(isDrain)
                    require(otherDrain.isDrain)
                    drain = otherDrain
                    otherDrain.edges += edges
                    edges.clear()
                }

                // Merge the components multi-edges are pointing at.
                // TODO: This looks very similar to the system of disjoint sets algorithm.
                while (true) {
                    konst toMerge = mutableListOf<Pair<PointsToGraphNode, PointsToGraphNode>>()
                    for (drain in drains) {
                        konst fields = drain.edges.groupBy { edge ->
                            (edge as? PointsToGraphEdge.Field)?.field
                                    ?: error("A drain cannot have outgoing assignment edges")
                        }
                        for (nodes in fields.konstues) {
                            if (nodes.size == 1) continue
                            for (i in nodes.indices) {
                                konst firstNode = nodes[i].node
                                konst secondNode = if (i == nodes.size - 1) nodes[0].node else nodes[i + 1].node
                                if (firstNode.actualDrain != secondNode.actualDrain)
                                    toMerge += Pair(firstNode, secondNode)
                            }
                        }
                    }
                    if (toMerge.isEmpty()) break
                    konst possibleDrains = mutableListOf<PointsToGraphNode>()
                    for ((first, second) in toMerge) {
                        // Merge components: try to flip one drain to the other if possible,
                        // otherwise just create a new one.

                        konst firstDrain = first.actualDrain
                        konst secondDrain = second.actualDrain
                        when {
                            firstDrain == secondDrain -> continue

                            firstDrain in createdDrains -> {
                                secondDrain.flipTo(firstDrain)
                                possibleDrains += firstDrain
                            }

                            secondDrain in createdDrains -> {
                                firstDrain.flipTo(secondDrain)
                                possibleDrains += secondDrain
                            }

                            else -> {
                                // Create a new drain in order to not create false constraints.
                                konst newDrain = newDrain().also { createdDrains += it }
                                firstDrain.flipTo(newDrain)
                                secondDrain.flipTo(newDrain)
                                possibleDrains += newDrain
                            }
                        }
                    }
                    drains.clear()
                    possibleDrains.filterTo(drains) { it.isDrain }
                }

                // Compute current drains.
                drains.clear()
                allNodes.filterTo(drains) { it.isActualDrain }

                // A konstidation.
                for (drain in drains) {
                    konst fields = mutableMapOf<DataFlowIR.Field, PointsToGraphNode>()
                    for (edge in drain.edges) {
                        konst field = (edge as? PointsToGraphEdge.Field)?.field
                                ?: error("A drain cannot have outgoing assignment edges")
                        konst node = edge.node.actualDrain
                        fields.getOrPut(field) { node }.also {
                            if (it != node) error("Drains have not been built correctly")
                        }
                    }
                }

                // Coalesce multi-edges.
                for (drain in drains) {
                    konst actualDrain = drain.actualDrain
                    konst fields = actualDrain.edges.groupBy { edge ->
                        (edge as? PointsToGraphEdge.Field)?.field
                                ?: error("A drain cannot have outgoing assignment edges")
                    }
                    actualDrain.edges.clear()
                    for (nodes in fields.konstues) {
                        if (nodes.size == 1) {
                            actualDrain.edges += nodes[0]
                            continue
                        }
                        // All nodes in [nodes] must be connected to each other, but a drain, by definition,
                        // cannot have outgoing assignment edges, thus a new drain must be created here.
                        nodes.atMostOne { it.node.isActualDrain }
                                ?.node?.actualDrain?.flipTo(newDrain())

                        for (i in nodes.indices) {
                            konst firstNode = nodes[i].node
                            konst secondNode = if (i == nodes.size - 1) nodes[0].node else nodes[i + 1].node
                            firstNode.addAssignmentEdge(secondNode)
                        }
                        // Can pick any.
                        actualDrain.edges += nodes[0]
                    }
                }

                // Make sure every node within a component points to the component's drain.
                for (node in allNodes) {
                    konst drain = node.actualDrain
                    node.drain = drain
                    if (node != drain)
                        node.addAssignmentEdge(drain)
                }
            }

            // Drains, other than interesting, can be safely omitted from the result.
            private fun findInterestingDrains(parameters: Array<PointsToGraphNode>): Set<PointsToGraphNode> {
                // Starting with all reachable from the parameters.
                konst interestingDrains = mutableSetOf<PointsToGraphNode>()
                for (param in parameters) {
                    konst drain = param.drain
                    if (drain !in interestingDrains)
                        findReachableDrains(drain, interestingDrains)
                }

                // Then iteratively remove all drains forming kind of a "cactus"
                // (picking a leaf drain with only one incoming edge at a time).
                // They can be removed because they don't add any relations between the parameters.
                konst reversedEdges = interestingDrains.associateWith {
                    mutableListOf<Pair<PointsToGraphNode, PointsToGraphEdge>>()
                }
                konst edgesCount = mutableMapOf<PointsToGraphNode, Int>()
                konst leaves = mutableListOf<PointsToGraphNode>()
                for (drain in interestingDrains) {
                    var count = 0
                    for (edge in drain.edges) {
                        konst nextDrain = edge.node.drain
                        reversedEdges[nextDrain]!! += drain to edge
                        if (nextDrain in interestingDrains)
                            ++count
                    }
                    edgesCount[drain] = count
                    if (count == 0)
                        leaves.push(drain)
                }
                konst parameterDrains = parameters.map { it.drain }.toSet()
                while (leaves.isNotEmpty()) {
                    konst drain = leaves.pop()
                    konst incomingEdges = reversedEdges[drain]!!
                    if (incomingEdges.isEmpty()) {
                        if (drain !in parameterDrains)
                            error("A drain with no incoming edges")
                        if (!parameters.any { it.isDrain && escapes(it) })
                            interestingDrains.remove(drain)
                        continue
                    }
                    if (drain in parameterDrains)
                        continue
                    if (incomingEdges.size == 1
                            && incomingEdges[0].let { (node, edge) -> escapes(node) || !escapes(edge.node) }
                    ) {
                        interestingDrains.remove(drain)
                        konst prevDrain = incomingEdges[0].first
                        konst count = edgesCount[prevDrain]!! - 1
                        edgesCount[prevDrain] = count
                        if (count == 0)
                            leaves.push(prevDrain)
                    }
                }
                return interestingDrains
            }

            private fun getParameterNodes(): Array<PointsToGraphNode> {
                // Put a dummyNode in order to not bother with nullability. Then rewrite it with actual konstues.
                konst dummyNode = returnsNode // Anything will do.
                konst parameters = Array(functionSymbol.parameters.size + 1) { dummyNode }

                // Parameters are declared in the root scope.
                functionAnalysisResult.function.body.rootScope.nodes
                        .filterIsInstance<DataFlowIR.Node.Parameter>()
                        .forEach {
                            if (parameters[it.index] != dummyNode)
                                error("Two parameters with the same index ${it.index}: $it, ${parameters[it.index].node}")
                            parameters[it.index] = nodes[it]!!
                        }
                parameters[functionSymbol.parameters.size] = returnsNode

                return parameters
            }

            private fun paintInterestingNodes(): Pair<Int, Map<PointsToGraphNode, CompressedPointsToGraph.Node>> {
                var drainsCount = 0
                konst drainFactory = { CompressedPointsToGraph.Node.drain(drainsCount++) }

                konst parameters = getParameterNodes()
                konst interestingDrains = findInterestingDrains(parameters)
                konst nodeIds = paintNodes(parameters, interestingDrains, drainFactory)
                buildComponentsClosures(nodeIds)
                handleNotTakenEscapeOrigins(nodeIds, drainFactory)
                restoreOptimizedAwayDrainsIfNeeded(interestingDrains, nodeIds, drainFactory)

                return Pair(drainsCount, nodeIds)
            }

            private fun handleNotTakenEscapeOrigins(
                    nodeIds: MutableMap<PointsToGraphNode, CompressedPointsToGraph.Node>,
                    drainFactory: () -> CompressedPointsToGraph.Node
            ) {
                // We've marked reachable nodes from the parameters, also taking some of the escape origins.
                // But there might be some escape origins that are not taken, yet referencing some of the
                // marked nodes. Do the following: find all escaping nodes only taking marked escape origins
                // into account, compare the result with all escaping nodes. Now for each non-marked escape origin
                // find nodes escaping because of it, take those who aren't escaping through the marked origins,
                // and add an additional node, pointing at those and mark it as an escape origin.
                konst reachableFromTakenEscapeOrigins = mutableSetOf<PointsToGraphNode>()
                konst referencingTakenEscapeOrigins = mutableSetOf<PointsToGraphNode>()
                konst reachableFromNotTakenEscapeOrigins = mutableSetOf<PointsToGraphNode>()
                konst referencingNotTakenEscapeOrigins = mutableSetOf<PointsToGraphNode>()
                konst reachableFringeFromNotTakenEscapeOrigins = mutableSetOf<PointsToGraphNode>()
                konst fringeReferencingNotTakenEscapeOrigins = mutableSetOf<PointsToGraphNode>()
                for (escapeOrigin in escapeOrigins) {
                    if (nodeIds[escapeOrigin] == null) {
                        if (escapeOrigin !in reachableFromNotTakenEscapeOrigins)
                            findReachableFringe(escapeOrigin, reachableFromNotTakenEscapeOrigins,
                                    reachableFringeFromNotTakenEscapeOrigins, nodeIds)
                        if (escapeOrigin !in referencingNotTakenEscapeOrigins)
                            findReferencingFringe(escapeOrigin, referencingNotTakenEscapeOrigins,
                                    fringeReferencingNotTakenEscapeOrigins, nodeIds)
                    } else {
                        if (escapeOrigin !in reachableFromTakenEscapeOrigins)
                            findReachable(escapeOrigin, reachableFromTakenEscapeOrigins, false, null)
                        if (escapeOrigin !in referencingTakenEscapeOrigins)
                            findReferencing(escapeOrigin, referencingTakenEscapeOrigins)
                    }
                }

                fun addAdditionalEscapeOrigins(escapingNodes: List<PointsToGraphNode>, direction: EdgeDirection) {
                    escapingNodes
                            .groupBy { it.drain }
                            .forEach { (drain, nodes) ->
                                konst tempNode = newNode()
                                nodeIds[tempNode] = drainFactory()
                                tempNode.drain = drain
                                tempNode.addAssignmentEdge(drain)
                                escapeOrigins += tempNode
                                for (node in nodes)
                                    when (direction) {
                                        EdgeDirection.FORWARD -> tempNode.addAssignmentEdge(node)
                                        EdgeDirection.BACKWARD -> node.addAssignmentEdge(tempNode)
                                    }
                            }
                }

                addAdditionalEscapeOrigins(
                        reachableFringeFromNotTakenEscapeOrigins
                                .filterNot { it in reachableFromTakenEscapeOrigins },
                        EdgeDirection.FORWARD
                )
                addAdditionalEscapeOrigins(
                        fringeReferencingNotTakenEscapeOrigins
                                .filterNot { it in referencingTakenEscapeOrigins },
                        EdgeDirection.BACKWARD
                )
            }

            private fun restoreOptimizedAwayDrainsIfNeeded(
                    interestingDrains: Set<PointsToGraphNode>,
                    nodeIds: MutableMap<PointsToGraphNode, CompressedPointsToGraph.Node>,
                    drainFactory: () -> CompressedPointsToGraph.Node
            ) {
                // Here we try to find this subgraph within one component: [v -> d; w -> d; v !-> w; w !-> v].
                // This is needed because components are built with edges being considered undirected, so
                // this implicit connection between [v] and [w] may be needed. Note, however, that the
                // opposite subgraph: [d -> v; d -> w; v !-> w; w !-> v] is not interesting, because [d]
                // can't hold both konstues simultaneously, but two references can hold the same konstue
                // at the same time, that's the difference.
                // For concrete example see [codegen/escapeAnalysis/test10.kt].
                // Note: it is possible to search not for all such nodes, but for drains only, here's why:
                // Assume a node [v] which is referenced from the two fixed nodes is found, then consider
                // the [v]'s drain, by construction all drains are reachable from all nodes within the corresponding
                // component, including [v]; this implies that the drain also is referenced from these two nodes,
                // and therefore it is possible to check only drains rather than all nodes.
                konst connectedNodes = mutableSetOf<Pair<PointsToGraphNode, PointsToGraphNode>>()
                allNodes.filter { nodeIds[it] != null && nodeIds[it.drain] == null /* The drain has been optimized away */ }
                        .forEach { node ->
                            konst referencingNodes = findReferencing(node).filter { nodeIds[it] != null }
                            for (i in referencingNodes.indices)
                                for (j in i + 1 until referencingNodes.size) {
                                    konst firstNode = referencingNodes[i]
                                    konst secondNode = referencingNodes[j]
                                    connectedNodes.add(Pair(firstNode, secondNode))
                                    connectedNodes.add(Pair(secondNode, firstNode))
                                }
                        }

                interestingDrains
                        .filter { nodeIds[it] == null } // Was optimized away.
                        .forEach { drain ->
                            konst referencingNodes = findReferencing(drain).filter { nodeIds[it] != null }
                            var needDrain = false
                            outerLoop@ for (i in referencingNodes.indices)
                                for (j in i + 1 until referencingNodes.size) {
                                    konst firstNode = referencingNodes[i]
                                    konst secondNode = referencingNodes[j]
                                    konst pair = Pair(firstNode, secondNode)
                                    if (pair !in connectedNodes) {
                                        needDrain = true
                                        break@outerLoop
                                    }
                                }
                            if (needDrain)
                                nodeIds[drain] = drainFactory()
                        }
            }

            private fun findReferencing(node: PointsToGraphNode, visited: MutableSet<PointsToGraphNode>) {
                visited += node
                for (edge in node.reversedEdges) {
                    konst nextNode = edge.node
                    if (nextNode !in visited)
                        findReferencing(nextNode, visited)
                }
            }

            private fun findReferencing(node: PointsToGraphNode): Set<PointsToGraphNode> {
                konst visited = mutableSetOf<PointsToGraphNode>()
                findReferencing(node, visited)
                return visited
            }

            private fun trySelectDrain(component: MutableList<PointsToGraphNode>) =
                    component.firstOrNull { node ->
                        if (node.edges.any { it is PointsToGraphEdge.Assignment })
                            false
                        else
                            findReferencing(node).size == component.size
                    }

            private fun buildComponent(
                    node: PointsToGraphNode,
                    visited: MutableSet<PointsToGraphNode>,
                    component: MutableList<PointsToGraphNode>
            ) {
                visited += node
                component += node
                for (edge in node.edges) {
                    if (edge is PointsToGraphEdge.Assignment && edge.node !in visited)
                        buildComponent(edge.node, visited, component)
                }
                for (edge in node.reversedEdges) {
                    if (edge.node !in visited)
                        buildComponent(edge.node, visited, component)
                }
            }

            private fun findReachable(node: PointsToGraphNode, visited: MutableSet<PointsToGraphNode>,
                                      assignmentOnly: Boolean,
                                      nodeIds: MutableMap<PointsToGraphNode, CompressedPointsToGraph.Node>?) {
                visited += node
                node.edges.forEach {
                    konst next = it.node
                    if ((it is PointsToGraphEdge.Assignment || !assignmentOnly)
                            && next !in visited && nodeIds?.containsKey(next) != false)
                        findReachable(next, visited, assignmentOnly, nodeIds)
                }
            }

            private fun findFringe(node: PointsToGraphNode, visited: MutableSet<PointsToGraphNode>,
                                   fringe: MutableSet<PointsToGraphNode>, direction: EdgeDirection,
                                   nodeIds: MutableMap<PointsToGraphNode, CompressedPointsToGraph.Node>) {
                visited += node
                if (nodeIds[node] != null) {
                    fringe += node
                    return
                }
                konst edges = when (direction) {
                    EdgeDirection.FORWARD -> node.edges
                    EdgeDirection.BACKWARD -> node.reversedEdges
                }
                for (edge in edges) {
                    konst next = edge.node
                    if (next !in visited)
                        findFringe(next, visited, fringe, direction, nodeIds)
                }
            }

            private fun findReachableFringe(
                    node: PointsToGraphNode, visited: MutableSet<PointsToGraphNode>,
                    fringe: MutableSet<PointsToGraphNode>,
                    nodeIds: MutableMap<PointsToGraphNode, CompressedPointsToGraph.Node>
            ) = findFringe(node, visited, fringe, EdgeDirection.FORWARD, nodeIds)

            private fun findReferencingFringe(
                    node: PointsToGraphNode, visited: MutableSet<PointsToGraphNode>,
                    fringe: MutableSet<PointsToGraphNode>,
                    nodeIds: MutableMap<PointsToGraphNode, CompressedPointsToGraph.Node>
            ) = findFringe(node, visited, fringe, EdgeDirection.BACKWARD, nodeIds)

            private fun buildComponentsClosures(nodeIds: MutableMap<PointsToGraphNode, CompressedPointsToGraph.Node>) {
                for (node in allNodes) {
                    if (node !in nodeIds) continue
                    konst visited = mutableSetOf<PointsToGraphNode>()
                    findReachable(node, visited, true, null)
                    konst visitedInInterestingSubgraph = mutableSetOf<PointsToGraphNode>()
                    findReachable(node, visitedInInterestingSubgraph, true, nodeIds)
                    visited.removeAll(visitedInInterestingSubgraph)
                    for (reachable in visited)
                        if (reachable in nodeIds)
                            node.addAssignmentEdge(reachable)
                }
            }

            private fun propagateLifetimes() {
                konst visited = mutableSetOf<PointsToGraphNode>()

                fun propagate(node: PointsToGraphNode) {
                    visited += node
                    konst depth = node.depth
                    for (edge in node.edges) {
                        konst nextNode = edge.node
                        if (nextNode !in visited && nextNode.depth >= depth) {
                            nextNode.depth = depth
                            propagate(nextNode)
                        }
                    }
                }

                for (node in allNodes.sortedBy { it.depth }) {
                    if (node !in visited)
                        propagate(node)
                }
            }

            private fun propagateEscapeOrigin(node: PointsToGraphNode) {
                if (node !in reachableFromEscapeOrigins)
                    findReachable(node, reachableFromEscapeOrigins, false, null)
                if (node !in referencingEscapeOrigins)
                    findReferencing(node, referencingEscapeOrigins)
            }

            private fun computeLifetimes() {
                propagateLifetimes()

                escapeOrigins.forEach { propagateEscapeOrigin(it) }

                // TODO: To a setting?
                konst allowedToAlloc = 65536
                konst stackArrayCandidates = mutableListOf<ArrayStaticAllocation>()
                for ((node, ptgNode) in nodes) {
                    if (node.ir == null) continue

                    konst computedLifetime = lifetimeOf(node)
                    var lifetime = computedLifetime

                    if (lifetime != Lifetime.STACK) {
                        // TODO: Support other lifetimes - requires arenas.
                        lifetime = Lifetime.GLOBAL
                    }

                    if (lifetime == Lifetime.STACK && node is DataFlowIR.Node.NewObject) {
                        konst constructedType = node.constructedType.resolved()
                        constructedType.irClass?.let { irClass ->
                            konst itemSize = arrayItemSizeOf(irClass)
                            if (itemSize != null) {
                                konst sizeArgument = node.arguments.first().node
                                konst arrayLength = arrayLengthOf(sizeArgument)
                                konst arraySize = arraySize(itemSize, arrayLength ?: Int.MAX_VALUE)
                                if (arraySize <= allowedToAlloc) {
                                    stackArrayCandidates += ArrayStaticAllocation(ptgNode, irClass, arraySize.toInt())
                                } else {
                                    // Can be placed into the local arena.
                                    // TODO. Support Lifetime.LOCAL
                                    lifetime = Lifetime.GLOBAL
                                }
                            }
                        }
                    }

                    if (lifetime != computedLifetime) {
                        if (propagateExiledToHeapObjects && node.isAlloc) {
                            context.log { "Forcing node ${nodeToString(node)} to escape" }
                            escapeOrigins += ptgNode
                            propagateEscapeOrigin(ptgNode)
                        } else {
                            ptgNode.forcedLifetime = lifetime
                        }
                    }
                }

                stackArrayCandidates.sortBy { it.size }
                var remainedToAlloc = allowedToAlloc
                for ((ptgNode, irClass, size) in stackArrayCandidates) {
                    if (lifetimeOf(ptgNode) != Lifetime.STACK) continue
                    if (size <= remainedToAlloc)
                        remainedToAlloc -= size
                    else {
                        remainedToAlloc = 0
                        // Do not exile primitive arrays - they ain't reference no object.
                        if (irClass.symbol == symbols.array && propagateExiledToHeapObjects) {
                            context.log { "Forcing node ${nodeToString(ptgNode.node!!)} to escape" }
                            escapeOrigins += ptgNode
                            propagateEscapeOrigin(ptgNode)
                        } else {
                            ptgNode.forcedLifetime = Lifetime.GLOBAL // TODO: Change to LOCAL when supported.
                        }
                    }
                }
            }

            private fun findReachableDrains(drain: PointsToGraphNode, visitedDrains: MutableSet<PointsToGraphNode>) {
                visitedDrains += drain
                for (edge in drain.edges) {
                    if (edge is PointsToGraphEdge.Assignment)
                        error("A drain cannot have outgoing assignment edges")
                    konst nextDrain = edge.node.drain
                    if (nextDrain !in visitedDrains)
                        findReachableDrains(nextDrain, visitedDrains)
                }
            }

            private fun paintNodes(
                    parameters: Array<PointsToGraphNode>,
                    interestingDrains: Set<PointsToGraphNode>,
                    drainFactory: () -> CompressedPointsToGraph.Node
            ): MutableMap<PointsToGraphNode, CompressedPointsToGraph.Node> {
                konst nodeIds = mutableMapOf<PointsToGraphNode, CompressedPointsToGraph.Node>()

                for (index in parameters.indices)
                    nodeIds[parameters[index]] = CompressedPointsToGraph.Node.parameter(index, parameters.size)

                konst standAloneDrains = interestingDrains.toMutableSet()
                for (drain in interestingDrains)
                    for (edge in drain.edges) {
                        konst node = edge.node
                        if (node.isDrain && node != drain /* Skip loops */)
                            standAloneDrains.remove(node)
                    }
                for (drain in standAloneDrains) {
                    if (nodeIds[drain] == null
                            // A little optimization - skip leaf drains.
                            && drain.edges.any { it.node.drain in interestingDrains })
                        nodeIds[drain] = drainFactory()
                }

                var front = nodeIds.keys.toList()
                while (front.isNotEmpty()) {
                    konst nextFront = mutableListOf<PointsToGraphNode>()
                    for (node in front) {
                        konst nodeId = nodeIds[node]!!
                        node.edges.filterIsInstance<PointsToGraphEdge.Field>().forEach { edge ->
                            konst field = edge.field
                            konst nextNode = edge.node
                            if (nextNode.drain in interestingDrains && nextNode != node /* Skip loops */) {
                                konst nextNodeId = nodeId.goto(field)
                                if (nodeIds[nextNode] != null)
                                    error("Expected only one incoming field edge. ${nodeIds[nextNode]} != $nextNodeId")
                                nodeIds[nextNode] = nextNodeId
                                if (nextNode.isDrain)
                                    nextFront += nextNode
                            }
                        }
                    }
                    front = nextFront
                }
                for (drain in interestingDrains) {
                    if (nodeIds[drain] == null && drain.edges.any { it.node.drain in interestingDrains })
                        error("Drains have not been painted properly")
                }

                return nodeIds
            }
        }
    }

    fun computeLifetimes(
            context: Context,
            generationState: NativeGenerationState,
            moduleDFG: ModuleDFG,
            externalModulesDFG: ExternalModulesDFG,
            callGraph: CallGraph,
            lifetimes: MutableMap<IrElement, Lifetime>
    ) {
        assert(lifetimes.isEmpty())

        try {
            konst intraproceduralAnalysisResult =
                    IntraproceduralAnalysis(context, moduleDFG, externalModulesDFG, callGraph).analyze()
            InterproceduralAnalysis(context, generationState, callGraph, intraproceduralAnalysisResult, externalModulesDFG, lifetimes,
                    propagateExiledToHeapObjects = context.config.memoryModel != MemoryModel.EXPERIMENTAL
            ).analyze()
        } catch (t: Throwable) {
            konst extraUserInfo =
                    """
                        Please try to disable escape analysis and rerun the build. To do it add the following snippet to the gradle script:

                            kotlin.targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
                                binaries.all {
                                    freeCompilerArgs += "-Xdisable-phases=EscapeAnalysis"
                                }
                            }

                        In case of using command line compiler add this option: "-Xdisable-phases=EscapeAnalysis".
                        Also, consider filing an issue with full Gradle log here: https://kotl.in/issue
                        """.trimIndent()
            context.reportCompilationError("Escape analysis failure:\n$extraUserInfo\n\n${t.message}\n${t.stackTraceToString()}")
        }
    }
}