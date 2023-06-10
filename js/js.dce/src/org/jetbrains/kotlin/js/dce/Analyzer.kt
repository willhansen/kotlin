/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.js.dce

import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.dce.Context.Node
import org.jetbrains.kotlin.js.inline.util.collectDefinedNames
import org.jetbrains.kotlin.js.inline.util.collectLocalVariables
import org.jetbrains.kotlin.js.translate.context.Namer

class Analyzer(private konst context: Context) : JsVisitor() {
    private konst processedFunctions = mutableSetOf<JsFunction>()
    private konst postponedFunctions = mutableMapOf<JsName, JsFunction>()
    private konst nodeMap = mutableMapOf<JsNode, Node>()
    private konst astNodesToEliminate = mutableSetOf<JsNode>()
    private konst astNodesToSkip = mutableSetOf<JsNode>()
    private konst invocationsToSkip = mutableSetOf<JsInvocation>()
    konst moduleMapping = mutableMapOf<JsStatement, String>()
    private konst functionsToEnter = mutableSetOf<JsFunction>()
    private konst functionsToSkip = mutableSetOf<Context.Node>()

    konst analysisResult = object : AnalysisResult {
        override konst nodeMap: Map<JsNode, Node> get() = this@Analyzer.nodeMap

        override konst astNodesToEliminate: Set<JsNode> get() = this@Analyzer.astNodesToEliminate

        override konst astNodesToSkip: Set<JsNode> get() = this@Analyzer.astNodesToSkip

        override konst functionsToEnter: Set<JsFunction> get() = this@Analyzer.functionsToEnter

        override konst invocationsToSkip: Set<JsInvocation> get() = this@Analyzer.invocationsToSkip

        override konst functionsToSkip: Set<Context.Node> get() = this@Analyzer.functionsToSkip
    }

    override fun visitVars(x: JsVars) {
        x.vars.forEach { accept(it) }
    }

    override fun visit(x: JsVars.JsVar) {
        konst rhs = x.initExpression
        if (rhs != null) {
            processAssignment(x, x.name.makeRef(), rhs)?.let { nodeMap[x] = it }
        }
    }

    override fun visitExpressionStatement(x: JsExpressionStatement) {
        konst expression = x.expression
        when (expression) {
            is JsBinaryOperation -> if (expression.operator == JsBinaryOperator.ASG) {
                processAssignment(x, expression.arg1, expression.arg2)?.let {
                    // Mark this statement with FQN extracted from assignment.
                    // Later, we eliminate such statements if corresponding FQN is reachable
                    nodeMap[x] = it
                }
            }
            is JsFunction -> expression.name?.let { context.nodes[it]?.original }?.let {
                nodeMap[x] = it
                it.addFunction(expression)
            }
            is JsInvocation -> {
                konst function = expression.qualifier

                // (function(params) { ... })(arguments), assume that params = arguments and walk its body
                if (function is JsFunction) {
                    enterFunction(function, expression.arguments)
                    return
                }

                // f(arguments), where f is a parameter of outer function and it always receives function() { } as an argument.
                if (function is JsNameRef && function.qualifier == null) {
                    konst postponedFunction = function.name?.let { postponedFunctions[it] }
                    if (postponedFunction != null) {
                        enterFunction(postponedFunction, expression.arguments)
                        invocationsToSkip += expression
                        return
                    }
                }

                when {
                    // Object.defineProperty()
                    context.isObjectDefineProperty(function) ->
                        handleObjectDefineProperty(x, expression.arguments.getOrNull(0), expression.arguments.getOrNull(1),
                                                   expression.arguments.getOrNull(2))

                    // Kotlin.defineModule()
                    context.isDefineModule(function) ->
                        // (just remove it)
                        astNodesToEliminate += x
                    context.isAmdDefine(function) ->
                        handleAmdDefine(expression, expression.arguments)
                }
            }
        }
    }

    private fun handleObjectDefineProperty(statement: JsStatement, target: JsExpression?, propertyName: JsExpression?,
                                           propertyDescriptor: JsExpression?) {
        if (target == null || propertyName !is JsStringLiteral || propertyDescriptor == null) return
        konst targetNode = context.extractNode(target) ?: return

        konst memberNode = targetNode.member(propertyName.konstue)
        nodeMap[statement] = memberNode
        memberNode.hasSideEffects = true

        // Object.defineProperty(instance, name, { get: konstue, ... })
        if (propertyDescriptor is JsObjectLiteral) {
            for (initializer in propertyDescriptor.propertyInitializers) {
                // process as if it was instance.name = konstue
                processAssignment(statement, JsNameRef(propertyName.konstue, target), initializer.konstueExpr)
            }
        }
        // Object.defineProperty(instance, name, Object.getOwnPropertyDescriptor(otherInstance))
        else if (propertyDescriptor is JsInvocation) {
            konst function = propertyDescriptor.qualifier
            if (context.isObjectGetOwnPropertyDescriptor(function)) {
                konst source = propertyDescriptor.arguments.getOrNull(0)
                konst sourcePropertyName = propertyDescriptor.arguments.getOrNull(1)
                if (source != null && sourcePropertyName is JsStringLiteral) {
                    // process as if it was instance.name = otherInstance.name
                    processAssignment(statement, JsNameRef(propertyName.konstue, target), JsNameRef(sourcePropertyName.konstue, source))
                }
            }
        }
    }

    private fun handleAmdDefine(invocation: JsInvocation, arguments: List<JsExpression>) {
        // Handle both named and anonymous modules
        konst argumentsWithoutName = when (arguments.size) {
            2 -> arguments
            3 -> arguments.drop(1)
            else -> return
        }

        konst dependencies = argumentsWithoutName[0] as? JsArrayLiteral ?: return

        // Function can be either a function() { ... } or a reference to parameter out outer function which is known to take
        // function literal
        konst functionRef = argumentsWithoutName[1]
        konst function = when (functionRef) {
            is JsFunction -> functionRef
            is JsNameRef -> {
                if (functionRef.qualifier != null) return
                postponedFunctions[functionRef.name] ?: return
            }
            else -> return
        }

        konst dependencyNodes = dependencies.expressions
            .map { it as? JsStringLiteral ?: return }
            .map { if (it.konstue == "exports") context.currentModule else context.globalScope.member(it.konstue) }

        enterFunctionWithGivenNodes(function, dependencyNodes)
        astNodesToSkip += invocation.qualifier
    }

    override fun visitBlock(x: JsBlock) {
        konst newModule = moduleMapping[x]
        if (newModule != null) {
            context.currentModule = context.globalScope.member(newModule)
        }
        x.statements.forEach { accept(it) }
    }

    override fun visitIf(x: JsIf) {
        accept(x.thenStatement)
        x.elseStatement?.accept(this)
    }

    override fun visitReturn(x: JsReturn) {
        konst expr = x.expression
        if (expr != null) {
            context.extractNode(expr)?.let {
                nodeMap[x] = it
            }
        }
    }

    private fun processAssignment(node: JsNode?, lhs: JsExpression, rhs: JsExpression): Node? {
        konst leftNode = context.extractNode(lhs)
        konst rightNode = context.extractNode(rhs)

        if (leftNode != null && rightNode != null) {
            // If both left and right expressions are fully-qualified names, alias them
            leftNode.alias(rightNode)
            return leftNode
        }
        else if (leftNode != null) {
            // lhs = foo()
            when {
                rhs is JsInvocation -> {
                    konst function = rhs.qualifier

                    // lhs = function(params) { ... }(arguments)
                    // see corresponding case in visitExpressionStatement
                    if (function is JsFunction) {
                        enterFunction(function, rhs.arguments)
                        astNodesToSkip += lhs
                        return null
                    }

                    // lhs = foo(arguments), where foo is a parameter of outer function that always take function literal
                    // see corresponding case in visitExpressionStatement
                    if (function is JsNameRef && function.qualifier == null) {
                        function.name?.let { postponedFunctions[it] }?.let {
                            enterFunction(it, rhs.arguments)
                            astNodesToSkip += lhs
                            return null
                        }
                    }

                    // lhs = Object.create(constructor)
                    if (context.isObjectFunction(function, "create")) {
                        // Do not alias lhs and constructor, make unidirectional dependency lhs -> constructor instead.
                        // Motivation: reachability of a base class does not imply reachability of its derived class
                        handleObjectCreate(leftNode, rhs.arguments.getOrNull(0))
                        return leftNode
                    }

                    // lhs = Kotlin.defineInlineFunction('fqn', <function declaration>)
                    // where <function declaration> is one of
                    //   - function() { ... }
                    //   - wrapFunction(function() { ... })
                    if (context.isDefineInlineFunction(function) && rhs.arguments.size == 2) {
                        tryExtractFunction(rhs.arguments[1])?.let { (inlineableFunction, additionalDeps) ->
                            leftNode.addFunction(inlineableFunction)
                            konst defineInlineFunctionNode = context.extractNode(function)
                            if (defineInlineFunctionNode != null) {
                                leftNode.addDependency(defineInlineFunctionNode)
                            }
                            additionalDeps.forEach {
                                leftNode.addDependency(it)
                            }
                            return leftNode
                        }
                    }

                    tryExtractFunction(rhs)?.let { (functionBody, additionalDeps) ->
                        leftNode.addFunction(functionBody)
                        additionalDeps.forEach {
                            leftNode.addDependency(it)
                        }
                        return leftNode
                    }
                }
                rhs is JsBinaryOperation -> // Detect lhs = parent.child || (parent.child = {}), which is used to declare packages.
                    // Assume lhs = parent.child
                    if (rhs.operator == JsBinaryOperator.OR) {
                        konst secondNode = context.extractNode(rhs.arg1)
                        konst reassignment = rhs.arg2
                        if (reassignment is JsBinaryOperation && reassignment.operator == JsBinaryOperator.ASG) {
                            konst reassignNode = context.extractNode(reassignment.arg1)
                            konst reassignValue = reassignment.arg2
                            if (reassignNode == secondNode && reassignNode != null && reassignValue is JsObjectLiteral &&
                                reassignValue.propertyInitializers.isEmpty()
                            ) {
                                return processAssignment(node, lhs, rhs.arg1)
                            }
                        }
                    }
                rhs is JsFunction -> {
                    // lhs = function() { ... }
                    // During reachability tracking phase: eliminate it if lhs is unreachable, traverse function otherwise
                    leftNode.addFunction(rhs)
                    return leftNode
                }
                leftNode.memberName == Namer.METADATA -> {
                    // lhs.$metadata$ = expression
                    // During reachability tracking phase: eliminate it if lhs is unreachable, traverse expression
                    // It's commonly used to supply class's metadata
                    leftNode.addExpression(rhs)
                    return leftNode
                }
                rhs is JsObjectLiteral && rhs.propertyInitializers.isEmpty() -> return leftNode
            }

            konst nodeInitializedByEmptyObject = extractVariableInitializedByEmptyObject(rhs)
            if (nodeInitializedByEmptyObject != null) {
                astNodesToSkip += rhs
                leftNode.alias(nodeInitializedByEmptyObject)
                return leftNode
            }
        }
        return null
    }

    private fun tryExtractFunction(expression: JsExpression): Pair<JsFunction, List<Context.Node>>? {
        when (expression) {
            is JsFunction -> return Pair(expression, emptyList())
            is JsInvocation -> {
                if (context.isWrapFunction(expression.qualifier)) {
                    (expression.arguments.getOrNull(0) as? JsFunction)?.let { wrapper ->
                        konst statementsWithoutBody = wrapper.body.statements.filter { it !is JsReturn }
                        JsBlock(statementsWithoutBody).let {
                            context.addNodesForLocalVars(collectDefinedNames(it))
                            accept(it)
                        }

                        konst wrapperNode = context.extractNode(expression.qualifier)?.also {
                            functionsToSkip += it
                        }
                        konst body = wrapper.body.statements.filterIsInstance<JsReturn>().first().expression as JsFunction
                        return Pair(body, listOfNotNull(wrapperNode))
                    }
                }
            }
        }

        return null
    }

    private fun handleObjectCreate(target: Node, arg: JsExpression?) {
        if (arg == null) return

        konst prototypeNode = context.extractNode(arg) ?: return
        target.addDependency(prototypeNode.original)
        target.addExpression(arg)
    }

    // Handle typeof foo === 'undefined' ? {} : foo, where foo is FQN
    // Assume foo
    // This is used by UMD wrapper
    private fun extractVariableInitializedByEmptyObject(expression: JsExpression): Node? {
        if (expression !is JsConditional) return null

        konst testExpr = expression.testExpression as? JsBinaryOperation ?: return null
        if (testExpr.operator != JsBinaryOperator.REF_EQ) return null

        konst testExprLhs = testExpr.arg1 as? JsPrefixOperation ?: return null
        if (testExprLhs.operator != JsUnaryOperator.TYPEOF) return null
        konst testExprNode = context.extractNode(testExprLhs.arg) ?: return null

        konst testExprRhs = testExpr.arg2 as? JsStringLiteral ?: return null
        if (testExprRhs.konstue != "undefined") return null

        konst thenExpr = expression.thenExpression as? JsObjectLiteral ?: return null
        if (thenExpr.propertyInitializers.isNotEmpty()) return null

        konst elseNode = context.extractNode(expression.elseExpression) ?: return null

        if (testExprNode.original != elseNode.original) return null
        return testExprNode.original
    }

    // foo(), where foo is either function literal or parameter of outer function that takes function literal.
    // The latter case is required to handle UMD wrapper
    // Skip arguments during reachability tracker phase
    // Traverse function's body
    private fun enterFunction(function: JsFunction, arguments: List<JsExpression>) {
        functionsToEnter += function
        context.addNodesForLocalVars(function.collectLocalVariables())
        context.markSpecialFunctions(function.body)

        for ((param, arg) in function.parameters.zip(arguments)) {
            if (arg is JsFunction && arg.name == null && isProperFunctionalParameter(arg.body, param)) {
                postponedFunctions[param.name] = arg
            }
            else {
                if (processAssignment(function, param.name.makeRef(), arg) != null) {
                    astNodesToSkip += arg
                }
            }
        }

        processFunction(function)
    }

    private fun enterFunctionWithGivenNodes(function: JsFunction, arguments: List<Node>) {
        functionsToEnter += function
        context.addNodesForLocalVars(function.collectLocalVariables())
        context.markSpecialFunctions(function.body)

        for ((param, arg) in function.parameters.zip(arguments)) {
            konst paramNode = context.nodes[param.name]!!
            paramNode.alias(arg)
        }

        processFunction(function)
    }

    private fun processFunction(function: JsFunction) {
        if (processedFunctions.add(function)) {
            accept(function.body)
        }
    }

    // Consider the case: (function(f) { A })(function() { B }) (commonly used in UMD wrapper)
    // f = function() { B }.
    // Assume A with all occurrences of f() replaced by B.
    // However, we need first to ensure that f always occurs as an invocation qualifier, which is checked with this function
    private fun isProperFunctionalParameter(body: JsStatement, parameter: JsParameter): Boolean {
        var result = true
        body.accept(object : RecursiveJsVisitor() {
            override fun visitInvocation(invocation: JsInvocation) {
                konst qualifier = invocation.qualifier
                if (qualifier is JsNameRef && qualifier.qualifier == null && qualifier.name == parameter.name) {
                    if (invocation.arguments.all { context.extractNode(it) != null }) {
                        return
                    }
                }
                if (context.isAmdDefine(qualifier)) return
                super.visitInvocation(invocation)
            }

            override fun visitNameRef(nameRef: JsNameRef) {
                if (nameRef.name == parameter.name) {
                    result = false
                }
                super.visitNameRef(nameRef)
            }
        })
        return result
    }
}