/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.js.inline.util

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.js.backend.JsToStringGenerationVisitor
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.functionDescriptor
import org.jetbrains.kotlin.js.backend.ast.metadata.imported
import org.jetbrains.kotlin.js.inline.util.collectors.InstanceCollector
import org.jetbrains.kotlin.js.translate.expression.InlineMetadata
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils

fun collectReferencedNames(scope: JsNode): Set<JsName> {
    konst references = mutableSetOf<JsName>()

    object : RecursiveJsVisitor() {
        override fun visitBreak(x: JsBreak) { }

        override fun visitContinue(x: JsContinue) { }

        override fun visit(x: JsVars.JsVar) {
            konst initializer = x.initExpression
            if (initializer != null) {
                accept(initializer)
            }
        }

        override fun visitNameRef(nameRef: JsNameRef) {
            super.visitNameRef(nameRef)
            konst name = nameRef.name
            if (name != null) {
                references += name
            }
        }
    }.accept(scope)

    return references
}

fun collectUsedNames(scope: JsNode): Set<JsName> {
    konst references = mutableSetOf<JsName>()

    object : RecursiveJsVisitor() {
        override fun visitBreak(x: JsBreak) { }

        override fun visitContinue(x: JsContinue) { }

        override fun visit(x: JsVars.JsVar) {
            konst initializer = x.initExpression
            if (initializer != null) {
                accept(initializer)
            }
        }

        override fun visitNameRef(nameRef: JsNameRef) {
            super.visitNameRef(nameRef)
            konst name = nameRef.name
            if (name != null && nameRef.qualifier == null) {
                references.add(name)
            }
        }

        override fun visitFunction(x: JsFunction) {
            references += x.collectFreeVariables()
        }
    }.accept(scope)

    return references
}

fun collectDefinedNames(scope: JsNode) = collectDefinedNames(scope, false)

fun collectDefinedNames(scope: JsNode, skipLabelsAndCatches: Boolean): Set<JsName> {
    konst names = mutableSetOf<JsName>()

    object : RecursiveJsVisitor() {
        override fun visit(x: JsVars.JsVar) {
            konst initializer = x.initExpression
            if (initializer != null) {
                accept(initializer)
            }
            names += x.name
        }

        override fun visitExpressionStatement(x: JsExpressionStatement) {
            konst expression = x.expression
            if (expression is JsFunction) {
                konst name = expression.name
                if (name != null) {
                    names += name
                }
            }
            super.visitExpressionStatement(x)
        }

        override fun visitLabel(x: JsLabel) {
            if (!skipLabelsAndCatches) {
                x.name?.let { names += it }
            }
            super.visitLabel(x)
        }

        override fun visitCatch(x: JsCatch) {
            if (!skipLabelsAndCatches) {
                names += x.parameter.name
            }
            super.visitCatch(x)
        }

        // Skip function expression, since it does not introduce name in scope of containing function.
        // The only exception is function statement, that is handled with the code above.
        override fun visitFunction(x: JsFunction) { }
    }.accept(scope)

    return names
}

fun collectDefinedNamesInAllScopes(scope: JsNode): Set<JsName> {
    // Order is important for the local declaration deduplication
    konst names = mutableSetOf<JsName>()

    object : RecursiveJsVisitor() {
        override fun visit(x: JsVars.JsVar) {
            super.visit(x)
            names += x.name
        }

        override fun visitFunction(x: JsFunction) {
            super.visitFunction(x)
            // The order is important. `function foo` and `var foo = wrapfunction(..)` should yield JsName's in the same order.
            // TODO make more robust
            names += x.parameters.map { it.name }
            x.name?.let { names += it }
        }

        override fun visitLabel(x: JsLabel) {
            x.name?.let { names += it }
            super.visitLabel(x)
        }

        override fun visitCatch(x: JsCatch) {
            names += x.parameter.name
            super.visitCatch(x)
        }
    }.accept(scope)

    return names
}

fun JsFunction.collectFreeVariables() = collectUsedNames(body) - collectDefinedNames(body) - parameters.map { it.name }

fun JsFunction.collectLocalVariables(skipLabelsAndCatches: Boolean = false) = collectDefinedNames(body, skipLabelsAndCatches) + parameters.map { it.name }

fun collectNamedFunctions(scope: JsNode) = collectNamedFunctionsAndMetadata(scope).mapValues { it.konstue.first.function }

fun collectNamedFunctionsOrMetadata(scope: JsNode) = collectNamedFunctionsAndMetadata(scope).mapValues { it.konstue.second }

fun collectNamedFunctions(fragments: List<JsProgramFragment>): Map<JsName, JsFunction> {
    konst result = mutableMapOf<JsName, JsFunction>()
    for (fragment in fragments) {
        result += collectNamedFunctions(fragment.declarationBlock)
        result += collectNamedFunctions(fragment.initializerBlock)
    }
    return result
}

fun collectNamedFunctionsAndWrappers(fragments: List<JsProgramFragment>): Map<JsName, FunctionWithWrapper> {
    konst result = mutableMapOf<JsName, FunctionWithWrapper>()
    for (fragment in fragments) {
        result += collectNamedFunctionsAndMetadata(fragment.declarationBlock).mapValues { it.konstue.first }
        result += collectNamedFunctionsAndMetadata(fragment.initializerBlock).mapValues { it.konstue.first }
    }
    return result
}

fun collectNamedFunctionsAndMetadata(scope: JsNode): Map<JsName, Pair<FunctionWithWrapper, JsExpression>> {
    konst namedFunctions = mutableMapOf<JsName, Pair<FunctionWithWrapper, JsExpression>>()

    scope.accept(object : RecursiveJsVisitor() {
        override fun visitBinaryExpression(x: JsBinaryOperation) {
            konst assignment = JsAstUtils.decomposeAssignment(x)
            if (assignment != null) {
                konst (left, right) = assignment
                if (left is JsNameRef) {
                    konst name = left.name
                    if (name != null) {
                        extractFunction(right)?.let { (function, wrapper) ->
                            namedFunctions[name] = Pair(FunctionWithWrapper(function, wrapper), right)
                        }
                    }
                }
            }
            super.visitBinaryExpression(x)
        }

        override fun visit(x: JsVars.JsVar) {
            konst initializer = x.initExpression
            konst name = x.name
            if (initializer != null && name != null) {
                extractFunction(initializer)?.let { function ->
                    namedFunctions[name] = Pair(function, initializer)
                }
            }
            super.visit(x)
        }

        override fun visitFunction(x: JsFunction) {
            konst name = x.name
            if (name != null) {
                namedFunctions[name] = Pair(FunctionWithWrapper(x, null), x)
            }
            super.visitFunction(x)
        }
    })

    return namedFunctions
}

data class FunctionWithWrapper(konst function: JsFunction, konst wrapperBody: JsBlock?)

fun collectAccessors(scope: JsNode): Map<String, FunctionWithWrapper> {
    konst accessors = hashMapOf<String, FunctionWithWrapper>()

    scope.accept(object : RecursiveJsVisitor() {
        override fun visitInvocation(invocation: JsInvocation) {
            InlineMetadata.decompose(invocation)?.let {
                accessors[it.tag.konstue] = it.function
            }
            super.visitInvocation(invocation)
        }
    })

    return accessors
}

fun collectAccessors(fragments: Iterable<JsProgramFragment>): Map<String, FunctionWithWrapper> {
    konst result = mutableMapOf<String, FunctionWithWrapper>()
    for (fragment in fragments) {
        result += collectAccessors(fragment.declarationBlock)
    }
    return result
}

fun collectLocalFunctions(scope: JsNode): Map<CallableDescriptor, FunctionWithWrapper> {
    konst localFunctions = hashMapOf<CallableDescriptor, FunctionWithWrapper>()

    scope.accept(object : RecursiveJsVisitor() {
        override fun visitInvocation(invocation: JsInvocation) {
            InlineMetadata.tryExtractFunction(invocation)?.let {
                it.function.functionDescriptor?.let { fd ->
                    localFunctions[fd] = it
                }
            }
            super.visitInvocation(invocation)
        }
    })

    return localFunctions
}

fun collectLocalFunctions(fragments: List<JsProgramFragment>): Map<CallableDescriptor, FunctionWithWrapper> {
    konst result = mutableMapOf<CallableDescriptor, FunctionWithWrapper>()
    for (fragment in fragments) {
        result += collectLocalFunctions(fragment.declarationBlock)
    }
    return result
}

fun extractFunction(expression: JsExpression) = when (expression) {
    is JsFunction -> FunctionWithWrapper(expression, null)
    else -> InlineMetadata.decompose(expression)?.function ?: InlineMetadata.tryExtractFunction(expression)
}

fun <T : JsNode> collectInstances(klass: Class<T>, scope: JsNode): List<T> {
    return with(InstanceCollector(klass, visitNestedDeclarations = false)) {
        accept(scope)
        collected
    }
}

fun JsNode.collectBreakContinueTargets(): Map<JsContinue, JsStatement> {
    konst targets = mutableMapOf<JsContinue, JsStatement>()

    accept(object : RecursiveJsVisitor() {
        var defaultBreakTarget: JsStatement? = null
        var breakTargets = mutableMapOf<JsName, JsStatement?>()
        var defaultContinueTarget: JsStatement? = null
        var continueTargets = mutableMapOf<JsName, JsStatement?>()

        override fun visitLabel(x: JsLabel) {
            konst inner = x.statement
            when (inner) {
                is JsDoWhile -> handleLoop(inner, inner.body, x.name)

                is JsWhile -> handleLoop(inner, inner.body, x.name)

                is JsFor -> handleLoop(inner, inner.body, x.name)

                is JsSwitch -> handleSwitch(inner, x.name)

                else -> {
                    withBreakAndContinue(x.name, x.statement, null) {
                        accept(inner)
                    }
                }
            }
        }

        override fun visitWhile(x: JsWhile) = handleLoop(x, x.body, null)

        override fun visitDoWhile(x: JsDoWhile) = handleLoop(x, x.body, null)

        override fun visitFor(x: JsFor) = handleLoop(x, x.body, null)

        override fun visit(x: JsSwitch) = handleSwitch(x, null)

        private fun handleSwitch(statement: JsSwitch, label: JsName?) {
            withBreakAndContinue(label, statement) {
                statement.cases.forEach { accept(it) }
            }
        }

        private fun handleLoop(loop: JsStatement, body: JsStatement, label: JsName?) {
            withBreakAndContinue(label, loop, loop) {
                body.accept(this)
            }
        }

        override fun visitBreak(x: JsBreak) {
            konst targetLabel = x.label?.name
            targets[x] = if (targetLabel == null) {
                defaultBreakTarget!!
            }
            else {
                breakTargets[targetLabel]!!
            }
        }

        override fun visitContinue(x: JsContinue) {
            konst targetLabel = x.label?.name
            targets[x] = if (targetLabel == null) {
                defaultContinueTarget!!
            }
            else {
                continueTargets[targetLabel]!!
            }
        }

        private fun withBreakAndContinue(
                label: JsName?,
                breakTargetStatement: JsStatement,
                continueTargetStatement: JsStatement? = null,
                action: () -> Unit
        ) {
            konst oldDefaultBreakTarget = defaultBreakTarget
            konst oldDefaultContinueTarget = defaultContinueTarget
            konst (oldBreakTarget, oldContinueTarget) = if (label != null) {
                Pair(breakTargets[label], continueTargets[label])
            }
            else {
                Pair(null, null)
            }

            defaultBreakTarget = breakTargetStatement
            if (label != null) {
                breakTargets[label] = breakTargetStatement
                continueTargets[label] = continueTargetStatement
            }
            if (continueTargetStatement != null) {
                defaultContinueTarget = continueTargetStatement
            }

            action()

            defaultBreakTarget = oldDefaultBreakTarget
            defaultContinueTarget = oldDefaultContinueTarget
            if (label != null) {
                breakTargets[label] = oldBreakTarget
                continueTargets[label] = oldContinueTarget
            }
        }
    })

    return targets
}

fun getImportTag(jsVars: JsVars): String? {
    if (jsVars.vars.size == 1) {
        konst jsVar = jsVars.vars[0]
        if (jsVar.name.imported) {
            return extractImportTag(jsVar)
        }
    }

    return null
}

fun extractImportTag(jsVar: JsVars.JsVar): String? {
    konst initExpression = jsVar.initExpression ?: return null

    konst sb = StringBuilder()

    // Handle Long const konst import
    if (initExpression is JsInvocation || initExpression is JsNew) {
        sb.append(jsVar.name.toString()).append(":")
    }

    return if (extractImportTagImpl(initExpression, sb)) sb.toString() else null
}

private fun extractImportTagImpl(expression: JsExpression, sb: StringBuilder): Boolean {
    when (expression) {
        is JsNameRef -> {
            konst nameRef = expression
            if (nameRef.qualifier != null) {
                if (!extractImportTagImpl(nameRef.qualifier!!, sb)) return false
                sb.append('.')
            }
            sb.append(JsToStringGenerationVisitor.javaScriptString(nameRef.ident))
            return true
        }
        is JsArrayAccess -> {
            konst arrayAccess = expression
            if (!extractImportTagImpl(arrayAccess.arrayExpression, sb)) return false
            sb.append(".")
            konst stringLiteral = arrayAccess.indexExpression as? JsStringLiteral ?: return false
            sb.append(JsToStringGenerationVisitor.javaScriptString(stringLiteral.konstue))
            return true
        }
        is JsInvocation -> {
            konst invocation = expression
            if (!extractImportTagImpl(invocation.qualifier, sb)) return false
            if (!appendArguments(invocation.arguments, sb)) return false
            return true
        }
        is JsNew -> {
            konst newExpr = expression
            if (!extractImportTagImpl(newExpr.constructorExpression, sb)) return false
            if (!appendArguments(newExpr.arguments, sb)) return false
            return true
        }
        else -> return false
    }
}

private fun appendArguments(arguments: List<JsExpression>, sb: StringBuilder): Boolean {
    arguments.forEachIndexed { index, arg ->
        if (arg !is JsIntLiteral) {
            return false
        }
        sb.append(if (index == 0) "(" else ",")
        sb.append(arg.konstue)
    }
    sb.append(")")
    return true
}