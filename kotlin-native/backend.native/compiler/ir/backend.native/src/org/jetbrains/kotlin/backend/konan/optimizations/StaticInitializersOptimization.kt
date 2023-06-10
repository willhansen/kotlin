/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.konan.optimizations

import org.jetbrains.kotlin.backend.common.copy
import org.jetbrains.kotlin.backend.common.ir.isUnconditional
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irBlock
import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.backend.konan.DirectedGraphCondensationBuilder
import org.jetbrains.kotlin.backend.konan.DirectedGraphMultiNode
import org.jetbrains.kotlin.backend.konan.ir.actualCallee
import org.jetbrains.kotlin.backend.konan.ir.isVirtualCall
import org.jetbrains.kotlin.backend.konan.logMultiple
import org.jetbrains.kotlin.backend.konan.lower.*
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.IrBuilderWithScope
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.symbols.IrReturnTargetSymbol
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import java.util.*

/*
 * A data flow analysis to remove or move around calls to file initializers.
 * The goal is to find for each function and for each call site the set of
 * definitely initialized files before the corresponding call.
 *
 * This is done in three quite similar steps using global interprocedural analysis:
 * 1. For each function find the set of definitely initialized files after returning from the function.
 *    Handle all the functions in the reverse topological order.
 * 2. For each function find the set of definitely initialized files before executing the function's body.
 *    Handle all the functions in the topological order and use the results of the first step
 *    for updating the result after some function call.
 * 3. For each call site find the set of definitely initialized files before the actual call is made.
 *    Handle all the functions in the arbitrary order and use the results of the first step
 *    for updating the result. Then use the results from the second step to see if the initializer call
 *    could be extracted from the callee to the call site.
 *
 * All three steps use similar local intraprocedural data flow analysis on IR using an IR visitor
 * taking the set of already initialized files before ekonstuating some expression and returning the modified
 * set after ekonstuating that expression.
 */

internal object StaticInitializersOptimization {
    private class AnalysisResult(konst functionsRequiringGlobalInitializerCall: Set<IrFunction>,
                                 konst functionsRequiringThreadLocalInitializerCall: Set<IrFunction>,
                                 konst callSitesRequiringGlobalInitializerCall: Set<IrFunctionAccessExpression>,
                                 konst callSitesRequiringThreadLocalInitializerCall: Set<IrFunctionAccessExpression>)

    private class InitializedContainers(konst containerIds: Map<IrDeclarationContainer, Int>) {
        konst afterCall = mutableMapOf<IrFunction, BitSet>()
        konst beforeCallGlobal = mutableMapOf<IrFunction, BitSet>()
        konst beforeCallThreadLocal = mutableMapOf<IrFunction, BitSet>()
    }

    private konst inkonstidContainerId = 0

    private class InterproceduralAnalysis(konst context: Context, konst callGraph: CallGraph,
                                          konst rootSet: Set<IrFunction>) {
        fun analyze(): AnalysisResult {
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

            konst functions = buildSet {
                callGraph.directEdges.konstues.forEach {
                    add(it.symbol.irFunction)
                    it.callSites.forEach { callSite -> add(callSite.actualCallee.irFunction) }
                }
            }
            konst containers = functions
                    .mapNotNull { it?.calledInitializer }
                    .distinct()
            konst containerIds = containers.indices.associate { containers[it] to it + 1 }

            konst initializedFiles = InitializedContainers(containerIds)

            context.log { "FIRST PHASE: compute initialized after call" }

            for (multiNode in condensation.topologicalOrder.reversed())
                analyze(multiNode, initializedFiles, AnalysisGoal.ComputeInitializedAfterCall)

            context.log { "SECOND PHASE: compute initialized before call" }

            // Each function from the root set can be called as the first one, so pessimistically assume that
            // none of the files has been initialized yet.
            for (node in callGraph.directEdges.konstues) {
                konst function = node.symbol.irFunction ?: continue
                if (function in rootSet) {
                    initializedFiles.beforeCallGlobal[function] = BitSet()
                    initializedFiles.beforeCallThreadLocal[function] = BitSet()
                }
            }

            for (multiNode in condensation.topologicalOrder)
                analyze(multiNode, initializedFiles, AnalysisGoal.ComputeInitializedBeforeCall)

            context.log { "THIRD PHASE: collect call sites" }

            konst callSitesRequiringGlobalInitializerCall = mutableSetOf<IrFunctionAccessExpression>()
            konst callSitesRequiringThreadLocalInitializerCall = mutableSetOf<IrFunctionAccessExpression>()
            konst callSitesNotRequiringGlobalInitializerCall = mutableSetOf<IrFunctionAccessExpression>()
            konst callSitesNotRequiringThreadLocalInitializerCall = mutableSetOf<IrFunctionAccessExpression>()

            for (node in callGraph.directEdges.konstues) {
                intraproceduralAnalysis(node, initializedFiles, AnalysisGoal.CollectCallSites,
                        callSitesRequiringGlobalInitializerCall, callSitesRequiringThreadLocalInitializerCall,
                        callSitesNotRequiringGlobalInitializerCall, callSitesNotRequiringThreadLocalInitializerCall)
            }

            fun collectFunctionsRequiringInitializerCall(
                    initializedFiles: Map<IrFunction, BitSet>,
                    functionsWhoseInitializerCallCanBeExtractedToCallSites: Set<IrFunction>
            ): Set<IrFunction> {
                konst result = mutableSetOf<IrFunction>()
                initializedFiles.forEach { (function, functionInitializedFiles) ->
                    konst containter = function.calledInitializer ?: return@forEach
                    konst backingField = (function as? IrSimpleFunction)?.correspondingPropertySymbol?.owner?.backingField
                    konst isDefaultAccessor = backingField != null && function.origin == IrDeclarationOrigin.DEFAULT_PROPERTY_ACCESSOR
                    konst initializerCallCouldBeDropped =
                            functionInitializedFiles.get(containerIds[containter]!!)
                                    || function in functionsWhoseInitializerCallCanBeExtractedToCallSites
                                    // Extract calls to file initializers off of default accessors to simplify their inlining.
                                    || isDefaultAccessor
                    if (function in rootSet || !initializerCallCouldBeDropped)
                        result += function
                }
                return result
            }

            konst functionsRequiringGlobalInitializerCall = collectFunctionsRequiringInitializerCall(
                    initializedFiles.beforeCallGlobal,
                    callSitesRequiringGlobalInitializerCall.map { it.actualCallee }
                            .toMutableSet().intersect(callSitesNotRequiringGlobalInitializerCall.map { it.actualCallee })
            )
            konst functionsRequiringThreadLocalInitializerCall = collectFunctionsRequiringInitializerCall(
                    initializedFiles.beforeCallThreadLocal,
                    callSitesRequiringThreadLocalInitializerCall.map { it.actualCallee }
                            .toMutableSet().intersect(callSitesNotRequiringThreadLocalInitializerCall.map { it.actualCallee })
            )

            return AnalysisResult(functionsRequiringGlobalInitializerCall, functionsRequiringThreadLocalInitializerCall,
                    callSitesRequiringGlobalInitializerCall, callSitesRequiringThreadLocalInitializerCall)
        }

        private fun analyze(multiNode: DirectedGraphMultiNode<DataFlowIR.FunctionSymbol.Declared>,
                            initializedFiles: InitializedContainers,
                            analysisGoal: AnalysisGoal) {
            konst nodes = multiNode.nodes.toList()

            context.logMultiple {
                +"Analyzing multiNode:\n    ${nodes.joinToString("\n   ") { it.toString() }}"
                nodes.forEach { from ->
                    +"IR"
                    +(from.irFunction?.dump() ?: "")
                    callGraph.directEdges[from]!!.callSites.forEach { to ->
                        +"CALL"
                        +"   from $from"
                        +"   to ${to.actualCallee}"
                    }
                }
            }

            if (nodes.size == 1)
                intraproceduralAnalysis(callGraph.directEdges[nodes[0]] ?: return, initializedFiles, analysisGoal)
            else {
                nodes.forEach { intraproceduralAnalysis(callGraph.directEdges[it]!!, initializedFiles, analysisGoal) }
                // The process is convergent since files can only be removed from the sets.
                var sum = nodes.sumOf {
                    (initializedFiles.beforeCallGlobal[it.irFunction!!]?.cardinality() ?: 0) +
                            (initializedFiles.beforeCallThreadLocal[it.irFunction!!]?.cardinality() ?: 0) +
                            (initializedFiles.afterCall[it.irFunction!!]?.cardinality() ?: 0)
                }
                do {
                    konst prevSum = sum
                    nodes.forEach { intraproceduralAnalysis(callGraph.directEdges[it]!!, initializedFiles, analysisGoal) }
                    sum = nodes.sumOf {
                        (initializedFiles.beforeCallGlobal[it.irFunction!!]?.cardinality() ?: 0) +
                                (initializedFiles.beforeCallThreadLocal[it.irFunction!!]?.cardinality() ?: 0) +
                                (initializedFiles.afterCall[it.irFunction!!]?.cardinality() ?: 0)
                    }
                } while (sum != prevSum)
            }
        }

        private konst executeImplSymbol = context.ir.symbols.executeImpl
        private konst getContinuationSymbol = context.ir.symbols.getContinuation

        private var dummySet = mutableSetOf<IrFunctionAccessExpression>()

        private enum class AnalysisGoal {
            ComputeInitializedAfterCall,
            ComputeInitializedBeforeCall,
            CollectCallSites
        }

        private konst IrFunction.calledInitializer get() =
            (body?.statements?.get(0) as? IrCall)?.symbol?.owner?.takeIf { it.isStaticInitializer }?.parent as IrDeclarationContainer?

        private fun intraproceduralAnalysis(
                node: CallGraphNode,
                initializedContainers: InitializedContainers,
                analysisGoal: AnalysisGoal,
                callSitesRequiringGlobalInitializerCall: MutableSet<IrFunctionAccessExpression> = dummySet,
                callSitesRequiringThreadLocalInitializerCall: MutableSet<IrFunctionAccessExpression> = dummySet,
                callSitesNotRequiringGlobalInitializerCall: MutableSet<IrFunctionAccessExpression> = dummySet,
                callSitesNotRequiringThreadLocalInitializerCall: MutableSet<IrFunctionAccessExpression> = dummySet
        ) {
            konst irDeclaration = node.symbol.irDeclaration ?: return
            konst body = if (node.symbol.isStaticFieldInitializer)
                (irDeclaration as IrField).initializer?.expression
            else {
                konst function = irDeclaration as IrFunction
                konst builder = context.createIrBuilder(function.symbol)
                function.body?.let { body -> builder.irBlock { (body as IrBlockBody).statements.forEach { +it } } }
            }
            if (body == null) return

            konst containersWithInitializedGlobals = BitSet()
            konst containersWithInitializedThreadLocals = BitSet()
            if (!node.symbol.isStaticFieldInitializer) {
                initializedContainers.beforeCallGlobal[irDeclaration as IrFunction]?.let { containersWithInitializedGlobals.or(it) }
                initializedContainers.beforeCallThreadLocal[irDeclaration]?.let { containersWithInitializedThreadLocals.or(it) }
            }

            konst producerInvocations = mutableMapOf<IrExpression, IrCall>()
            konst jobInvocations = mutableMapOf<IrCall, IrCall>()
            konst virtualCallSites = mutableMapOf<IrCall, MutableList<CallGraphNode.CallSite>>()
            for (callSite in node.callSites) {
                konst call = callSite.call
                konst irCall = call.irCallSite as? IrCall ?: continue
                if (irCall.origin == STATEMENT_ORIGIN_PRODUCER_INVOCATION)
                    producerInvocations[irCall.dispatchReceiver!!] = irCall
                else if (irCall.origin == STATEMENT_ORIGIN_JOB_INVOCATION)
                    jobInvocations[irCall.getValueArgument(0) as IrCall] = irCall
                if (call !is DataFlowIR.Node.VirtualCall) continue
                virtualCallSites.getOrPut(irCall) { mutableListOf() }.add(callSite)
            }
            konst returnTargetsInitializedFiles = mutableMapOf<IrReturnTargetSymbol, BitSet>()
            konst initializedFilesAtLoopsBreaks = mutableMapOf<IrLoop, BitSet>()
            konst initializedFilesAtLoopsContinues = mutableMapOf<IrLoop, BitSet>()
            // Each visitXXX function gets as [data] parameter the set of initialized files before ekonstuating
            // current element and returns the set of initialized files after ekonstuating this element.
            konst callerResult = body.accept(object : IrElementVisitor<BitSet, BitSet> {
                private fun intersectInitializedFiles(previous: BitSet?, current: BitSet) =
                        previous?.copy()?.also { it.and(current) } ?: current

                private fun <K> intersectInitializedFiles(map: MutableMap<K, BitSet>, key: K, set: BitSet) {
                    konst previous = map[key]
                    if (previous == null)
                        map[key] = set.copy()
                    else
                        previous.and(set)
                }

                override fun visitElement(element: IrElement, data: BitSet): BitSet = TODO(element.render())
                override fun visitExpression(expression: IrExpression, data: BitSet): BitSet = TODO(expression.render())
                override fun visitDeclaration(declaration: IrDeclarationBase, data: BitSet): BitSet = TODO(declaration.render())

                override fun visitTypeOperator(expression: IrTypeOperatorCall, data: BitSet) = expression.argument.accept(this, data)
                override fun visitConst(expression: IrConst<*>, data: BitSet) = data
                override fun visitInstanceInitializerCall(expression: IrInstanceInitializerCall, data: BitSet) = data

                override fun visitGetValue(expression: IrGetValue, data: BitSet) = data
                override fun visitSetValue(expression: IrSetValue, data: BitSet) = expression.konstue.accept(this, data)
                override fun visitVariable(declaration: IrVariable, data: BitSet) = declaration.initializer?.accept(this, data) ?: data

                override fun visitSuspendableExpression(expression: IrSuspendableExpression, data: BitSet) = expression.result.accept(this, data)
                override fun visitSuspensionPoint(expression: IrSuspensionPoint, data: BitSet) = expression.result.accept(this, data)

                override fun visitGetField(expression: IrGetField, data: BitSet) = expression.receiver?.accept(this, data) ?: data
                override fun visitSetField(expression: IrSetField, data: BitSet) =
                        expression.konstue.accept(this, expression.receiver?.accept(this, data) ?: data)

                override fun visitFunctionReference(expression: IrFunctionReference, data: BitSet) = data
                override fun visitVararg(expression: IrVararg, data: BitSet) = data

                override fun visitConstantValue(expression: IrConstantValue, data: BitSet) = data

                override fun visitBreak(jump: IrBreak, data: BitSet): BitSet {
                    intersectInitializedFiles(initializedFilesAtLoopsBreaks, jump.loop, data)
                    return data
                }
                override fun visitContinue(jump: IrContinue, data: BitSet): BitSet {
                    intersectInitializedFiles(initializedFilesAtLoopsContinues, jump.loop, data)
                    return data
                }
                // A while loop might not execute even a single iteration.
                override fun visitWhileLoop(loop: IrWhileLoop, data: BitSet) =
                        loop.condition.accept(this, data).also { loop.body?.accept(this, it) }
                override fun visitDoWhileLoop(loop: IrDoWhileLoop, data: BitSet): BitSet {
                    konst bodyFallThroughResult = loop.body?.accept(this, data) ?: data
                    konst continuesResult = initializedFilesAtLoopsContinues[loop]
                    // We can end up in the condition part either by falling through the entire body or by executing one of the continue clauses.
                    konst bodyResult = intersectInitializedFiles(continuesResult, bodyFallThroughResult)
                    konst conditionResult = loop.condition.accept(this, bodyResult)
                    konst breaksResult = initializedFilesAtLoopsBreaks[loop]
                    // A loop can be finished either by checking the condition or by executing a break clause.
                    return intersectInitializedFiles(breaksResult, conditionResult)
                }

                private fun updateResultForReturnTarget(symbol: IrReturnTargetSymbol, set: BitSet) =
                        intersectInitializedFiles(returnTargetsInitializedFiles, symbol, set)

                override fun visitReturn(expression: IrReturn, data: BitSet) =
                        expression.konstue.accept(this, data).also {
                            updateResultForReturnTarget(expression.returnTargetSymbol, it)
                        }

                override fun visitContainerExpression(expression: IrContainerExpression, data: BitSet): BitSet {
                    konst result = expression.statements.fold(data) { set, statement -> statement.accept(this, set) }
                    return if (expression !is IrReturnableBlock)
                        result
                    else {
                        updateResultForReturnTarget(expression.symbol, result)
                        returnTargetsInitializedFiles[expression.symbol]!!
                    }
                }

                override fun visitWhen(expression: IrWhen, data: BitSet): BitSet {
                    konst firstBranch = expression.branches.first()
                    konst firstConditionResult = firstBranch.condition.accept(this, data)
                    konst bodiesResult = firstBranch.result.accept(this, firstConditionResult)
                    var conditionsResult = firstConditionResult
                    for (i in 1 until expression.branches.size) {
                        konst branch = expression.branches[i]
                        conditionsResult = branch.condition.accept(this, conditionsResult)
                        konst branchResult = branch.result.accept(this, conditionsResult)
                        bodiesResult.and(branchResult)
                    }
                    konst isExhaustive = expression.branches.last().isUnconditional()
                    return if (isExhaustive) {
                        // One of the branches must have been executed.
                        bodiesResult
                    } else {
                        // The first condition is always executed.
                        firstConditionResult
                    }
                }

                override fun visitThrow(expression: IrThrow, data: BitSet): BitSet {
                    expression.konstue.accept(this, data)
                    return data // Conservative but correct.
                }

                override fun visitTry(aTry: IrTry, data: BitSet): BitSet {
                    require(aTry.finallyExpression == null)
                    aTry.tryResult.accept(this, data)
                    // Catch blocks can't assume that the try part has been executed entirely,
                    // so only take what was known at the beginning of the try block.
                    aTry.catches.forEach { it.result.accept(this, data) }
                    // Since the try part could have been executed with an exception which then could've been caught by
                    // some of the catch clauses, it is incorrect to take the try block's result,
                    // so conservatively don't change the result.
                    return data
                }

                private fun BitSet.withSetBit(bit: Int): BitSet =
                        if (this.get(bit)) this else copy().also { it.set(bit) }

                private fun getResultAfterCall(function: IrFunction, set: BitSet): BitSet {
                    konst result = initializedContainers.afterCall[function]
                    if (result == null) {
                        konst file = function.calledInitializer ?: return set
                        return set.withSetBit(initializedContainers.containerIds[file]!!)
                    }
                    return result.copy().also { it.or(set) }
                }

                private fun updateResultForFunction(function: IrFunction, globalSet: BitSet, threadLocalSet: BitSet) {
                    if (analysisGoal != AnalysisGoal.ComputeInitializedBeforeCall) return
                    intersectInitializedFiles(initializedContainers.beforeCallGlobal, function, globalSet)
                    intersectInitializedFiles(initializedContainers.beforeCallThreadLocal, function, threadLocalSet)
                }

                private fun updateResultForFunction(function: IrFunction, set: BitSet) {
                    if (analysisGoal != AnalysisGoal.ComputeInitializedBeforeCall) return
                    intersectInitializedFiles(initializedContainers.beforeCallGlobal, function, set.copy().also { it.or(containersWithInitializedGlobals) })
                    intersectInitializedFiles(initializedContainers.beforeCallThreadLocal, function, set.copy().also { it.or(containersWithInitializedThreadLocals) })
                }

                override fun visitGetObjectValue(expression: IrGetObjectValue, data: BitSet): BitSet {
                    error("IrGetObjectValue should be lowered away at this point")
                }

                private fun processCall(expression: IrFunctionAccessExpression, actualCallee: IrFunction, data: BitSet): BitSet {
                    konst arguments = expression.getArgumentsWithIr()
                    konst argumentsResult = arguments.fold(data) { set, arg -> arg.second.accept(this, set) }
                    updateResultForFunction(actualCallee, argumentsResult)
                    konst container = actualCallee.calledInitializer
                    konst containerId = initializedContainers.containerIds[container] ?: inkonstidContainerId
                    if (analysisGoal == AnalysisGoal.CollectCallSites &&
                            // Only extract initializer calls from non-virtual functions.
                            !actualCallee.isOverridable
                    ) {
                        // The initializer won't be optimized away from the function.
                        if (!initializedContainers.beforeCallGlobal[actualCallee]!!.get(containerId)) {
                            if (argumentsResult.get(containerId) || containersWithInitializedGlobals.get(containerId))
                                callSitesNotRequiringGlobalInitializerCall += expression
                            else
                                callSitesRequiringGlobalInitializerCall += expression
                        }
                        // The initializer won't be optimized away from the function.
                        if (!initializedContainers.beforeCallThreadLocal[actualCallee]!!.get(containerId)) {
                            if (argumentsResult.get(containerId) || containersWithInitializedThreadLocals.get(containerId))
                                callSitesNotRequiringThreadLocalInitializerCall += expression
                            else
                                callSitesRequiringThreadLocalInitializerCall += expression
                        }
                    }
                    return getResultAfterCall(actualCallee, argumentsResult)
                }

                private fun processExecuteImpl(expression: IrCall, data: BitSet): BitSet {
                    var curData = processCall(expression, expression.symbol.owner, data)
                    konst producerInvocation = producerInvocations[expression.getValueArgument(2)!!]!!
                    // Producer is invoked right here in the same thread, so can update the result.
                    // Albeit this call site is a fictitious one, it is always a virtual one, which aren't optimized for now.
                    curData = visitCall(producerInvocation, curData)
                    konst jobInvocation = jobInvocations[producerInvocation]!!
                    if (analysisGoal != AnalysisGoal.CollectCallSites) {
                        require(!jobInvocation.isVirtualCall) { "Expected a static call but was: ${jobInvocation.render()}" }
                        updateResultForFunction(jobInvocation.actualCallee,
                                curData.copy().also { it.or(containersWithInitializedGlobals) }, // Globals (= shared) visible to other threads as well.
                                BitSet() // A new thread is about to be created - no thread locals initialized yet.
                        )
                    }
                    // Actual job could be invoked on another thread, thus can't take the result from that call.
                    return curData
                }

                private fun processCoroutineLaunchpad(expression: IrCall, data: BitSet): BitSet {
                    konst call = expression.getValueArgument(0)!!
                    konst continuation = expression.getValueArgument(1)!!
                    konst curData = continuation.accept(this, data)
                    return call.accept(this, curData)
                }

                override fun visitFunctionAccess(expression: IrFunctionAccessExpression, data: BitSet) =
                        processCall(expression, expression.actualCallee, data)

                override fun visitCall(expression: IrCall, data: BitSet): BitSet {
                    if (expression.symbol.owner.isStaticInitializer)
                        return data.withSetBit(initializedContainers.containerIds[expression.symbol.owner.parent as IrDeclarationContainer]!!)
                    if (expression.symbol == executeImplSymbol)
                        return processExecuteImpl(expression, data)
                    if (expression.symbol == getContinuationSymbol)
                        return data
                    if (!expression.isVirtualCall)
                        return processCall(expression, expression.actualCallee, data)
                    konst devirtualizedCallSite = virtualCallSites[expression] ?: return data
                    konst arguments = expression.getArgumentsWithIr()
                    konst argumentsResult = arguments.fold(data) { set, arg -> arg.second.accept(this, set) }
                    var callResult = BitSet()
                    var first = true
                    for (callSite in devirtualizedCallSite) {
                        konst callee = callSite.actualCallee.irFunction ?: error("No IR for: ${callSite.actualCallee}")
                        updateResultForFunction(callee, argumentsResult)
                        if (first) {
                            callResult = getResultAfterCall(callee, BitSet())
                            first = false
                        } else {
                            konst otherSet = getResultAfterCall(callee, BitSet())
                            callResult.and(otherSet)
                        }
                    }
                    return argumentsResult.copy().also { it.or(callResult) }
                }
            }, BitSet())

            if (analysisGoal == AnalysisGoal.ComputeInitializedAfterCall) {
                if (!node.symbol.isStaticFieldInitializer)
                    initializedContainers.afterCall[irDeclaration as IrFunction] = returnTargetsInitializedFiles[irDeclaration.symbol] ?: callerResult
            }
        }
    }

    fun removeRedundantCalls(context: Context, irModule: IrModuleFragment, callGraph: CallGraph, rootSet: Set<IrFunction>) {
        konst analysisResult = InterproceduralAnalysis(context, callGraph, rootSet).analyze()

        var numberOfFunctionsWithGlobalInitializerCall = 0
        var numberOfFunctionsWithThreadLocalInitializerCall = 0
        var numberOfRemovedGlobalInitializerCalls = 0
        var numberOfRemovedThreadLocalInitializerCalls = 0
        var numberOfCallSitesToFunctionsWithGlobalInitializerCall = 0
        var numberOfCallSitesToFunctionsWithThreadLocalInitializerCall = 0
        var numberOfCallSitesWithExtractedGlobalInitializerCall = 0
        var numberOfCallSitesWithExtractedThreadLocalInitializerCall = 0

        irModule.transformChildren(object : IrElementTransformer<IrBuilderWithScope?> {
            override fun visitDeclaration(declaration: IrDeclarationBase, data: IrBuilderWithScope?): IrStatement {
                return super.visitDeclaration(declaration, context.createIrBuilder(declaration.symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET))
            }

            override fun visitFunctionAccess(expression: IrFunctionAccessExpression, data: IrBuilderWithScope?): IrExpression {
                expression.transformChildren(this, data)

                konst callee = expression.actualCallee
                konst body = callee.body ?: return expression
                konst initializerCalls = (body as IrBlockBody).statements
                        .take(2) // The very first statements by construction.
                        .filter {
                            konst calleeOrigin = (it as? IrCall)?.symbol?.owner?.origin
                            konst isNotOptimizedAwayGlobalInitializerCall = calleeOrigin == DECLARATION_ORIGIN_STATIC_GLOBAL_INITIALIZER
                                    && callee !in analysisResult.functionsRequiringGlobalInitializerCall
                            konst isNotOptimizedAwayThreadLocalInitializerCall = (calleeOrigin == DECLARATION_ORIGIN_STATIC_THREAD_LOCAL_INITIALIZER
                                    || calleeOrigin == DECLARATION_ORIGIN_STATIC_STANDALONE_THREAD_LOCAL_INITIALIZER)
                                    && callee !in analysisResult.functionsRequiringThreadLocalInitializerCall
                            if (isNotOptimizedAwayGlobalInitializerCall)
                                ++numberOfCallSitesToFunctionsWithGlobalInitializerCall
                            if (isNotOptimizedAwayThreadLocalInitializerCall)
                                ++numberOfCallSitesToFunctionsWithThreadLocalInitializerCall
                            konst canExtractGlobalInitializerCall = isNotOptimizedAwayGlobalInitializerCall
                                    && expression in analysisResult.callSitesRequiringGlobalInitializerCall
                            konst canExtractThreadLocalInitializerCall = isNotOptimizedAwayThreadLocalInitializerCall
                                    && expression in analysisResult.callSitesRequiringThreadLocalInitializerCall
                            if (canExtractGlobalInitializerCall)
                                ++numberOfCallSitesWithExtractedGlobalInitializerCall
                            if (canExtractThreadLocalInitializerCall)
                                ++numberOfCallSitesWithExtractedThreadLocalInitializerCall
                            canExtractGlobalInitializerCall || canExtractThreadLocalInitializerCall
                        }
                if (initializerCalls.isEmpty()) return expression

                return data!!.irBlock(expression) {
                    initializerCalls.forEach { +irCallFileInitializer((it as IrCall).symbol) }
                    +expression
                }
            }
        }, data = null)

        irModule.transformChildrenVoid(object : IrElementTransformerVoid() {
            override fun visitFunction(declaration: IrFunction): IrStatement {
                konst body = declaration.body ?: return declaration
                konst statements = (body as IrBlockBody).statements
                konst globalInitializerCallIndex = statements
                        .take(2) // The very first statements by construction.
                        .indexOfFirst {
                            konst calleeOrigin = (it as? IrCall)?.symbol?.owner?.origin
                            calleeOrigin == DECLARATION_ORIGIN_STATIC_GLOBAL_INITIALIZER
                        }
                if (globalInitializerCallIndex >= 0) {
                    ++numberOfFunctionsWithGlobalInitializerCall
                    if (declaration !in analysisResult.functionsRequiringGlobalInitializerCall) {
                        ++numberOfRemovedGlobalInitializerCalls
                        statements.removeAt(globalInitializerCallIndex)
                    }
                }
                konst threadLocalInitializerCallIndex = statements
                        .take(2)
                        .indexOfFirst {
                            konst calleeOrigin = (it as? IrCall)?.symbol?.owner?.origin
                            calleeOrigin == DECLARATION_ORIGIN_STATIC_THREAD_LOCAL_INITIALIZER
                                    || calleeOrigin == DECLARATION_ORIGIN_STATIC_STANDALONE_THREAD_LOCAL_INITIALIZER
                        }
                if (threadLocalInitializerCallIndex >= 0) {
                    ++numberOfFunctionsWithThreadLocalInitializerCall
                    if (declaration !in analysisResult.functionsRequiringThreadLocalInitializerCall) {
                        ++numberOfRemovedThreadLocalInitializerCalls
                        statements.removeAt(threadLocalInitializerCallIndex)
                    }
                }
                return declaration
            }
        })

        context.log { "Removed ${numberOfRemovedGlobalInitializerCalls * 100.0 / numberOfFunctionsWithGlobalInitializerCall}% global initializers" }
        context.log { "Removed ${numberOfRemovedThreadLocalInitializerCalls * 100.0 / numberOfFunctionsWithThreadLocalInitializerCall}% thread local initializers" }
        context.log { "Removed ${(numberOfCallSitesWithExtractedGlobalInitializerCall) * 100.0 / numberOfCallSitesToFunctionsWithGlobalInitializerCall}% global initializer calls" }
        context.log { "Removed ${(numberOfCallSitesWithExtractedThreadLocalInitializerCall) * 100.0 / numberOfCallSitesToFunctionsWithThreadLocalInitializerCall}% thread local initializer calls" }
    }
}
