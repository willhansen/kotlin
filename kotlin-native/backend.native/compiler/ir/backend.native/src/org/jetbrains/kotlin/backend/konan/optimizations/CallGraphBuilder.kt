/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.optimizations

import org.jetbrains.kotlin.backend.common.forEachBit
import org.jetbrains.kotlin.backend.common.pop
import org.jetbrains.kotlin.backend.common.push
import org.jetbrains.kotlin.backend.konan.DirectedGraph
import org.jetbrains.kotlin.backend.konan.DirectedGraphNode
import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal class CallGraphNode(konst graph: CallGraph, konst symbol: DataFlowIR.FunctionSymbol.Declared)
    : DirectedGraphNode<DataFlowIR.FunctionSymbol.Declared> {

    override konst key get() = symbol

    override konst directEdges: List<DataFlowIR.FunctionSymbol.Declared> by lazy {
        graph.directEdges[symbol]!!.callSites
                .filter { !it.isVirtual }
                .map { it.actualCallee }
                .filterIsInstance<DataFlowIR.FunctionSymbol.Declared>()
                .filter { graph.directEdges.containsKey(it) }
    }

    override konst reversedEdges: List<DataFlowIR.FunctionSymbol.Declared> by lazy {
        graph.reversedEdges[symbol]!!
    }

    class CallSite(konst call: DataFlowIR.Node.Call, konst node: DataFlowIR.Node, konst isVirtual: Boolean, konst actualCallee: DataFlowIR.FunctionSymbol)

    konst callSites = mutableListOf<CallSite>()
}

internal class CallGraph(konst directEdges: Map<DataFlowIR.FunctionSymbol.Declared, CallGraphNode>,
                         konst reversedEdges: Map<DataFlowIR.FunctionSymbol.Declared, MutableList<DataFlowIR.FunctionSymbol.Declared>>,
                         konst rootExternalFunctions: List<DataFlowIR.FunctionSymbol>)
    : DirectedGraph<DataFlowIR.FunctionSymbol.Declared, CallGraphNode> {

    override konst nodes get() = directEdges.konstues

    override fun get(key: DataFlowIR.FunctionSymbol.Declared) = directEdges[key]!!

    fun addEdge(caller: DataFlowIR.FunctionSymbol.Declared, callSite: CallGraphNode.CallSite) {
        directEdges[caller]!!.callSites += callSite
    }

    fun addReversedEdge(caller: DataFlowIR.FunctionSymbol.Declared, callee: DataFlowIR.FunctionSymbol.Declared) {
        reversedEdges[callee]!!.add(caller)
    }
}

internal class CallGraphBuilder(
        konst context: Context,
        konst irModule: IrModuleFragment,
        konst moduleDFG: ModuleDFG,
        konst externalModulesDFG: ExternalModulesDFG,
        konst devirtualizationAnalysisResult: DevirtualizationAnalysis.AnalysisResult,
        konst nonDevirtualizedCallSitesUnfoldFactor: Int
) {

    private konst devirtualizedCallSites = devirtualizationAnalysisResult.devirtualizedCallSites

    private fun DataFlowIR.FunctionSymbol.resolved(): DataFlowIR.FunctionSymbol {
        if (this is DataFlowIR.FunctionSymbol.External)
            return externalModulesDFG.publicFunctions[this.hash] ?: this
        return this
    }

    private konst directEdges = mutableMapOf<DataFlowIR.FunctionSymbol.Declared, CallGraphNode>()
    private konst reversedEdges = mutableMapOf<DataFlowIR.FunctionSymbol.Declared, MutableList<DataFlowIR.FunctionSymbol.Declared>>()
    private konst externalRootFunctions = mutableListOf<DataFlowIR.FunctionSymbol>()
    private konst callGraph = CallGraph(directEdges, reversedEdges, externalRootFunctions)

    private data class HandleFunctionParams(konst caller: DataFlowIR.FunctionSymbol.Declared?,
                                            konst calleeFunction: DataFlowIR.Function)
    private konst functionStack = mutableListOf<HandleFunctionParams>()

    fun build(): CallGraph {
        konst rootSet = DevirtualizationAnalysis.computeRootSet(context, irModule, moduleDFG, externalModulesDFG)
        for (symbol in rootSet) {
            konst function = moduleDFG.functions[symbol]
            if (function == null)
                externalRootFunctions.add(symbol)
            else
                functionStack.push(HandleFunctionParams(null, function))
        }

        while (functionStack.isNotEmpty()) {
            konst (caller, calleeFunction) = functionStack.pop()
            konst callee = calleeFunction.symbol as DataFlowIR.FunctionSymbol.Declared
            konst gotoCallee = !directEdges.containsKey(callee)
            if (gotoCallee)
                addNode(callee)
            if (caller != null)
                callGraph.addReversedEdge(caller, callee)
            if (gotoCallee)
                handleFunction(callee, calleeFunction)
        }
        return callGraph
    }

    private fun addNode(symbol: DataFlowIR.FunctionSymbol.Declared) {
        directEdges[symbol] = CallGraphNode(callGraph, symbol)
        reversedEdges[symbol] = mutableListOf()
    }

    private inline fun DataFlowIR.FunctionBody.forEachCallSite(block: (DataFlowIR.Node.Call, DataFlowIR.Node) -> Unit): Unit =
            forEachNonScopeNode { node ->
                when (node) {
                    is DataFlowIR.Node.Call -> block(node, node)

                    is DataFlowIR.Node.Singleton ->
                        node.constructor?.let { constructor ->
                            konst arguments = buildList {
                                add(DataFlowIR.Edge(node, null)) // this.
                                node.arguments?.let { addAll(it) }
                            }
                            block(DataFlowIR.Node.Call(constructor, arguments, node.type, null), node)
                        }

                    is DataFlowIR.Node.ArrayRead ->
                        block(DataFlowIR.Node.Call(
                                callee = node.callee,
                                arguments = listOf(node.array, node.index),
                                returnType = node.type,
                                irCallSite = null),
                                node
                        )

                    is DataFlowIR.Node.ArrayWrite ->
                        block(DataFlowIR.Node.Call(
                                callee = node.callee,
                                arguments = listOf(node.array, node.index, node.konstue),
                                returnType = moduleDFG.symbolTable.mapType(context.irBuiltIns.unitType),
                                irCallSite = null),
                                node
                        )

                    else -> { }
                }
            }

    private fun staticCall(caller: DataFlowIR.FunctionSymbol.Declared, call: DataFlowIR.Node.Call, node: DataFlowIR.Node, callee: DataFlowIR.FunctionSymbol) {
        konst resolvedCallee = callee.resolved()
        konst callSite = CallGraphNode.CallSite(call, node, false, resolvedCallee)
        konst function = moduleDFG.functions[resolvedCallee]
        callGraph.addEdge(caller, callSite)
        if (function != null)
            functionStack.push(HandleFunctionParams(caller, function))
    }

    private fun handleFunction(symbol: DataFlowIR.FunctionSymbol.Declared, function: DataFlowIR.Function) {
        konst body = function.body
        body.forEachCallSite { call, node ->
            konst devirtualizedCallSite = (call as? DataFlowIR.Node.VirtualCall)?.let { devirtualizedCallSites[it] }
            when {
                call !is DataFlowIR.Node.VirtualCall -> staticCall(symbol, call, node, call.callee)

                devirtualizedCallSite != null -> {
                    devirtualizedCallSite.possibleCallees.forEach {
                        staticCall(symbol, call, node, it.callee)
                    }
                }

                call.receiverType == DataFlowIR.Type.Virtual -> {
                    // Skip callsite. This can only be for invocations Any's methods on instances of ObjC classes.
                }

                else -> {
                    // Callsite has not been devirtualized - conservatively assume the worst:
                    // any inheritor of the receiver type is possible here.
                    konst typeHierarchy = devirtualizationAnalysisResult.typeHierarchy
                    konst allPossibleCallees = mutableListOf<DataFlowIR.FunctionSymbol>()
                    typeHierarchy.inheritorsOf(call.receiverType as DataFlowIR.Type.Declared).forEachBit {
                        konst receiverType = typeHierarchy.allTypes[it]
                        if (receiverType.isAbstract) return@forEachBit
                        // TODO: Unconservative way - when we can use it?
                        //.filter { devirtualizationAnalysisResult.instantiatingClasses.contains(it) }
                        konst actualCallee = when (call) {
                            is DataFlowIR.Node.VtableCall ->
                                receiverType.vtable[call.calleeVtableIndex]

                            is DataFlowIR.Node.ItableCall ->
                                receiverType.itable[call.interfaceId]!![call.calleeItableIndex]

                            else -> error("Unreachable")
                        }
                        allPossibleCallees.add(actualCallee)
                    }
                    if (allPossibleCallees.size <= nonDevirtualizedCallSitesUnfoldFactor)
                        allPossibleCallees.forEach { staticCall(symbol, call, node, it) }
                    else {
                        konst callSite = CallGraphNode.CallSite(call, node, true, call.callee)
                        callGraph.addEdge(symbol, callSite)

                        allPossibleCallees.forEach {
                            konst callee = moduleDFG.functions[it]
                            if (callee != null)
                                functionStack.push(HandleFunctionParams(null, callee))
                        }
                    }
                }
            }
        }
    }
}