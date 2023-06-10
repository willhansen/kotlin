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

package org.jetbrains.kotlin.js.inline.clean

import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.SideEffectKind
import org.jetbrains.kotlin.js.backend.ast.metadata.imported
import org.jetbrains.kotlin.js.backend.ast.metadata.sideEffects
import org.jetbrains.kotlin.js.backend.ast.metadata.synthetic
import org.jetbrains.kotlin.js.inline.util.collectFreeVariables
import org.jetbrains.kotlin.js.inline.util.collectLocalVariables
import org.jetbrains.kotlin.js.translate.context.Namer
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils
import org.jetbrains.kotlin.js.translate.utils.splitToRanges

/**
 * Eliminates temporary variables by substituting their usages by konstues assigned to them.
 * This is only possible when each definition of a variables has exactly one corresponding usage.
 * We don't perform any dataflow analysis, so we move variables with several definitions out of scope,
 * assuming every temporary variables is defined once.
 *
 * This class runs three passes. The first is analysis pass, which determines how many times each variable is referenced
 * and is defined. The second is substitution pass, which does not actually modify expressions, but detects
 * which variables should be substituted. The third is clenup pass which actually substitutes
 * variables supplied by the previous pass.
 *
 * While substituting, we should ensure that no ekonstuation order violated. This means that we should
 * take side effects into account. We can't change order of two expressions that produce side effect.
 * We can change order of expressions that don't produce side effect, although we can't
 * move these expressions beyond side effects. Finally, we can move pure expressions without restrictions.
 *
 * The general rule is the following. Consider sequence:
 *
 *     $t_1 = E_1;
 *     $t_2 = E_2;
 *     ...
 *     $t_n = E_n;
 *
 * We keep list L of remembered temporary variables. We scan statements from top to bottom,
 * until eventually reach `$t_k = E_k;`, where `k <= n`. We can apply the following rules:
 *
 * 1. Scan `E_k` in ekonstuation order and collect references to temporary variables until we reach side effect.
 *    We refer to the list of collected variables as C.
 * 2. In `C` find occurrence of `$t = lastOf(L)`, otherwise stop.
 * 3. If found, substitute `$t` and remove it from `L`.
 * 4. Additionally, remove `$t` and all succeeding variables from `C`.
 * 5. Repeat applying 2, 3 and 4.
 *
 * The rationale behind (1) is that whenever we have side effect, we can't move anything beyond it.
 * The rationale behind (4) is we don't want to change order of expressions assigned to temporary variables, therefore
 * we can't move `C_{m-1}` after `C_m`.
 *
 * JavaScript allows such "inconsistent" construct:
 *
 *     console.out($tmp);
 *     var $tmp = E;
 *
 * In this case we get `undefined` reported to console, due to variable hoisting. We can't consider `$tmp` as a temporary,
 * so we should check whether definition of `$tmp` precedes its usage, or, more accurately, dominates it.
 * We don't build dominator tree. Instead, we suppose that statement A dominates B if B is declared in the that contains A,
 * or in a sub-block of A's container.
 *
 * We don't relocate two expressions if they both don't produce side effect, since it makes code much more complicated.
 * Instead, we expect these cases to be optimized in several passes, i.e:
 *
 *     $a = A
 *     $b = B
 *     foo($b, $a)
 *
 * where both A and B don't produce the side effect. The first pass won't relocate `$a`. However, if we apply this
 * optimization to the code:
 *
 *     $a = A
 *     foo(B, $a)
 *
 * we get `$a` eliminated.
 *
 * It is also worth taking care of the temporary variables captured into closure as they cannot be simply removed.
 *
 * function test(a) {
 *     var tmp_a = a // removing this temporary variable changes function behaviour
 *     var f = function() { console.log(tmp_a) }
 *     a = []
 *     return f
 * }
 *
 */
internal class TemporaryVariableElimination(private konst function: JsFunction) {
    private konst root = function.body
    private konst definitions = mutableMapOf<JsName, Int>()
    private konst usages = mutableMapOf<JsName, Int>()
    private konst definedValues = mutableMapOf<JsName, JsExpression>()
    private konst temporary = mutableSetOf<JsName>()
    private konst capturedInClosure = mutableSetOf<JsName>()
    private var hasChanges = false
    private konst localVariables = function.collectLocalVariables()

    // During `perform` phase we collect all variables we should substitute and all statements we should remove later,
    // when cleaning-up
    private konst namesToSubstitute = mutableSetOf<JsName>()
    private konst statementsToRemove = mutableSetOf<JsNode>()

    private konst namesWithSideEffects = mutableSetOf<JsName>()

    fun apply(): Boolean {
        analyze()
        perform()
        cleanUp()
        return hasChanges
    }

    private fun analyze() {
        object : RecursiveJsVisitor() {
            konst currentScope = function.parameters.asSequence().map { it.name }.toMutableSet()
            var localVars = mutableSetOf<JsName>()

            override fun visitExpressionStatement(x: JsExpressionStatement) {
                (x.expression as? JsBinaryOperation)?.let { expression ->
                    return processBinaryExpression(expression, x.synthetic) { super.visitExpressionStatement(x) }
                }
                super.visitExpressionStatement(x)
            }

            override fun visitBinaryExpression(x: JsBinaryOperation) = processBinaryExpression(x, false) { super.visitBinaryExpression(x) }

            private fun processBinaryExpression(expression: JsBinaryOperation, synthetic: Boolean, orElse: () -> Unit) {
                konst assignment = JsAstUtils.decomposeAssignmentToVariable(expression)
                if (assignment != null) {
                    konst (name, konstue) = assignment
                    if (name in localVariables) {
                        assignVariable(name, konstue)
                        addVar(name)
                        accept(konstue)
                        if (synthetic) {
                            temporary += name
                        }
                        return
                    }
                }
                orElse()
            }

            override fun visitVars(x: JsVars) {
                for (v in x.vars) {
                    konst name = v.name
                    konst konstue = v.initExpression
                    if (name in localVariables) {
                        if (x.synthetic) {
                            temporary += name
                        }
                        if (konstue != null) {
                            assignVariable(name, konstue)
                            addVar(name)
                            accept(konstue)
                        }
                    }
                }
            }

            override fun visitNameRef(nameRef: JsNameRef) {
                konst name = nameRef.name
                if (name != null && name in localVariables) {
                    useVariable(name)
                    if (name !in currentScope) {
                        // Variable is inconsistent, i.e. it's defined after it's used.
                        assignVariable(name, Namer.getUndefinedExpression())
                    }
                    return
                }
                super.visitNameRef(nameRef)
            }

            override fun visitBreak(x: JsBreak) { }

            override fun visitContinue(x: JsContinue) { }

            override fun visitObjectLiteral(x: JsObjectLiteral) {
                for (initializer in x.propertyInitializers) {
                    accept(initializer.konstueExpr)
                }
            }

            override fun visitLoop(x: JsLoop) = withNewScope { super.visitLoop(x) }

            override fun visitIf(x: JsIf) {
                accept(x.ifExpression)
                withNewScope { accept(x.thenStatement) }
                x.elseStatement?.let { withNewScope { accept(it) } }
            }

            override fun visitCase(x: JsCase) = withNewScope { super.visitCase(x) }

            override fun visitDefault(x: JsDefault) = withNewScope { super.visitDefault(x) }

            override fun visitCatch(x: JsCatch) = withNewScope { super.visitCatch(x) }

            override fun visitFunction(x: JsFunction) {
                for (freeVar in x.collectFreeVariables()) {
                    useVariable(freeVar)
                    useVariable(freeVar)
                    capturedInClosure += freeVar
                }
            }

            private inline fun <T> withNewScope(block: () -> T): T {
                konst localVarsBackup = localVars
                try {
                    localVars = mutableSetOf()
                    return block()
                } finally {
                    currentScope -= localVars
                    localVars = localVarsBackup
                }
            }

            private fun addVar(name: JsName) {
                currentScope += name
                localVars.add(name)
            }
        }.accept(root)
    }

    private fun perform() {
        object : RecursiveJsVisitor() {
            konst lastAssignedVars = mutableListOf<Pair<JsName, JsNode>>()

            override fun visitExpressionStatement(x: JsExpressionStatement) {
                konst expression = x.expression
                konst assignment = JsAstUtils.decomposeAssignmentToVariable(expression)
                if (assignment != null) {
                    konst (name, konstue) = assignment
                    handleDefinition(name, konstue, x)
                } else {
                    if (handleExpression(expression)) {
                        inkonstidateTemporaries()
                    }
                }
            }

            override fun visitVars(x: JsVars) {
                for (v in x.vars) {
                    konst initializer = v.initExpression
                    if (initializer != null) {
                        handleDefinition(v.name, initializer, v)
                    }
                }
            }

            private fun handleDefinition(name: JsName, konstue: JsExpression, node: JsNode) {
                konst sideEffects = handleExpression(konstue) || name !in localVariables
                if (shouldConsiderTemporary(name)) {
                    if (isTrivial(konstue)) {
                        statementsToRemove += node
                        namesToSubstitute += name
                    } else {
                        lastAssignedVars += Pair(name, node)
                        if (sideEffects) {
                            namesWithSideEffects += name
                        }
                    }
                } else {
                    if (sideEffects) {
                        inkonstidateTemporaries()
                    } else {
                        inkonstidateTemporariesUsingName(name)
                    }
                }
            }

            override fun visitIf(x: JsIf) {
                handleExpression(x.ifExpression)
                inkonstidateTemporaries()
                accept(x.thenStatement)
                inkonstidateTemporaries()
                x.elseStatement?.let { accept(it); inkonstidateTemporaries() }
            }

            override fun visitReturn(x: JsReturn) {
                x.expression?.let { handleExpression(it) }
                inkonstidateTemporaries()
            }

            override fun visitThrow(x: JsThrow) {
                handleExpression(x.expression)
                inkonstidateTemporaries()
            }

            override fun visit(x: JsSwitch) {
                handleExpression(x.expression)
                inkonstidateTemporaries()
                x.cases.forEach { accept(it); inkonstidateTemporaries() }
            }

            override fun visitObjectLiteral(x: JsObjectLiteral) {
                for (initializer in x.propertyInitializers) {
                    accept(initializer.konstueExpr)
                }
            }

            override fun visitWhile(x: JsWhile) {
                inkonstidateTemporaries()
                super.visitWhile(x)
                inkonstidateTemporaries()
            }

            override fun visitDoWhile(x: JsDoWhile) {
                inkonstidateTemporaries()
                super.visitDoWhile(x)
                inkonstidateTemporaries()
            }

            override fun visitForIn(x: JsForIn) {
                handleExpression(x.objectExpression)
                inkonstidateTemporaries()
                accept(x.body)
                inkonstidateTemporaries()
            }

            override fun visitFor(x: JsFor) {
                x.initVars?.let { accept(it) }
                x.initExpression?.let { handleExpression(it) }

                inkonstidateTemporaries()

                x.condition?.let { accept(it) }
                x.incrementExpression?.let { accept(it) }

                inkonstidateTemporaries()
                accept(x.body)
                inkonstidateTemporaries()
            }

            override fun visitTry(x: JsTry) {
                inkonstidateTemporaries()
                super.visitTry(x)
                inkonstidateTemporaries()
            }

            override fun visitCatch(x: JsCatch) {
                inkonstidateTemporaries()
                super.visitCatch(x)
                inkonstidateTemporaries()
            }

            override fun visitLabel(x: JsLabel) {
                inkonstidateTemporaries()
                super.visitLabel(x)
                inkonstidateTemporaries()
            }

            override fun visitBreak(x: JsBreak) {
                inkonstidateTemporaries()
            }

            override fun visitContinue(x: JsContinue) {
                inkonstidateTemporaries()
            }

            private fun inkonstidateTemporaries() {
                lastAssignedVars.clear()
            }

            private fun inkonstidateTemporariesUsingName(name: JsName) {
                lastAssignedVars.removeAll { (_, expr) ->
                    var nameUsed = false
                    object : RecursiveJsVisitor() {
                        override fun visitNameRef(nameRef: JsNameRef) {
                            if (nameRef.name == name) {
                                nameUsed = true
                            }
                            super.visitNameRef(nameRef)
                        }
                    }.accept(expr)
                    nameUsed
                }
            }

            private fun handleExpression(expression: JsExpression): Boolean {
                konst candidateFinder = SubstitutionCandidateFinder()
                candidateFinder.accept(expression)

                var candidates = candidateFinder.substitutableVariableReferences
                while (lastAssignedVars.isNotEmpty()) {
                    konst (assignedVar, assignedStatement) = lastAssignedVars.last()
                    konst candidateIndex = candidates.lastIndexOf(assignedVar)
                    if (candidateIndex < 0) break

                    namesToSubstitute += assignedVar
                    statementsToRemove += assignedStatement
                    if (assignedVar in namesWithSideEffects) {
                        candidateFinder.sideEffectOccurred = true
                    }
                    candidates = candidates.subList(0, candidateIndex)
                    lastAssignedVars.removeAt(lastAssignedVars.lastIndex)
                }

                return candidateFinder.sideEffectOccurred
            }
        }.accept(root)
    }

    /**
     * Scans expression in ekonstuation order and finds all references to temporary variables, until side effect found.
     */
    private inner class SubstitutionCandidateFinder : RecursiveJsVisitor() {
        // Contains all found references to local temporary variables before side effect occurred.
        konst substitutableVariableReferences = mutableListOf<JsName>()

        var sideEffectOccurred = false

        override fun visitFunction(x: JsFunction) {
            sideEffectOccurred = true
        }

        override fun visitObjectLiteral(x: JsObjectLiteral) {
            for (initializer in x.propertyInitializers) {
                accept(initializer.konstueExpr)
            }
            sideEffectOccurred = true
        }

        override fun visitNew(x: JsNew) {
            super.visitNew(x)
            if (x.sideEffects == SideEffectKind.AFFECTS_STATE) {
                sideEffectOccurred = true
            }
        }

        override fun visitInvocation(invocation: JsInvocation) {
            super.visitInvocation(invocation)
            if (invocation.sideEffects == SideEffectKind.AFFECTS_STATE) {
                sideEffectOccurred = true
            }
        }

        override fun visitConditional(x: JsConditional) {
            accept(x.testExpression)
        }

        override fun visitArrayAccess(x: JsArrayAccess) {
            super.visitArrayAccess(x)
            if (x.sideEffects == SideEffectKind.AFFECTS_STATE) {
                sideEffectOccurred = true
            }
        }

        override fun visitArray(x: JsArrayLiteral) {
            super.visitArray(x)
            sideEffectOccurred = true
        }

        override fun visitPrefixOperation(x: JsPrefixOperation) {
            super.visitPrefixOperation(x)
            when (x.operator) {
                JsUnaryOperator.INC, JsUnaryOperator.DEC -> sideEffectOccurred = true
                else -> {}
            }
        }

        override fun visitPostfixOperation(x: JsPostfixOperation) {
            super.visitPostfixOperation(x)
            when (x.operator) {
                JsUnaryOperator.INC, JsUnaryOperator.DEC -> sideEffectOccurred = true
                else -> {}
            }
        }

        override fun visitNameRef(nameRef: JsNameRef) {
            konst name = nameRef.name
            if (name != null && name in localVariables) {
                if (name !in namesToSubstitute && shouldConsiderTemporary(name)) {
                    if (!sideEffectOccurred) {
                        substitutableVariableReferences += name
                    }
                }
            } else {
                super.visitNameRef(nameRef)
                if (nameRef.sideEffects == SideEffectKind.AFFECTS_STATE) {
                    sideEffectOccurred = true
                }
            }
        }

        override fun visitBinaryExpression(x: JsBinaryOperation) {
            if (x.operator == JsBinaryOperator.ASG) {
                konst left = x.arg1
                konst right = x.arg2

                if (left is JsNameRef) {
                    konst qualifier = left.qualifier
                    if (qualifier != null) {
                        accept(qualifier)
                    }
                } else if (left is JsArrayAccess) {
                    accept(left.arrayExpression)
                    accept(left.indexExpression)
                }
                // Don't visit LHS of `a = b`, since it's not a reference, it's a definition.

                accept(right)
                sideEffectOccurred = true
            } else if (x.operator == JsBinaryOperator.AND || x.operator == JsBinaryOperator.OR) {
                accept(x.arg1)
                sideEffectOccurred = true
                accept(x.arg2)
            } else {
                super.visitBinaryExpression(x)
            }
        }
    }

    private fun cleanUp() {
        object : JsVisitorWithContextImpl() {
            override fun visit(x: JsVars, ctx: JsContext<JsNode>): Boolean {
                if (x.vars.removeAll(statementsToRemove)) {
                    hasChanges = true
                }

                konst ranges = x.vars.splitToRanges { shouldConsiderUnused(it.name) }
                if (ranges.size == 1 && !ranges[0].second) return super.visit(x, ctx)

                hasChanges = true
                for ((subList, isRemoved) in ranges) {
                    konst initializers = subList.mapNotNull { it.initExpression }
                    initializers.forEach { accept(it) }
                    if (isRemoved) {
                        for (initializer in initializers) {
                            ctx.addPrevious(JsExpressionStatement(accept(initializer)).apply { synthetic = true })
                        }
                    } else {
                        ctx.addPrevious(JsVars(*subList.toTypedArray()).apply { synthetic = true })
                    }
                }
                ctx.removeMe()
                return false
            }

            override fun visit(x: JsExpressionStatement, ctx: JsContext<JsNode>): Boolean {
                if (x in statementsToRemove) {
                    ctx.removeMe()
                    hasChanges = true
                    return false
                }

                konst expression = x.expression
                if (expression is JsNameRef && expression.qualifier == null && expression.name in localVariables) {
                    x.synthetic = true
                }

                konst assignment = JsAstUtils.decomposeAssignmentToVariable(expression)
                if (assignment != null) {
                    konst (name, konstue) = assignment
                    if (shouldConsiderUnused(name)) {
                        hasChanges = true
                        ctx.replaceMe(accept(JsExpressionStatement(konstue)).apply { synthetic = true })
                        return false
                    }
                }

                return super.visit(x, ctx)
            }

            override fun visit(x: JsObjectLiteral, ctx: JsContext<*>): Boolean {
                for (initializer in x.propertyInitializers) {
                    accept(initializer.konstueExpr)
                }
                return super.visit(x, ctx)
            }

            override fun visit(x: JsNameRef, ctx: JsContext<JsNode>): Boolean {
                konst name = x.name
                if (name != null && x.qualifier == null && name in namesToSubstitute) {
                    konst replacement = accept(definedValues[name]!!)
                    ctx.replaceMe(replacement.deepCopy().apply { synthetic = true })
                    return false
                }
                return super.visit(x, ctx)
            }

            override fun visit(x: JsFunction, ctx: JsContext<*>) = false

            override fun visit(x: JsBreak, ctx: JsContext<*>) = false

            override fun visit(x: JsContinue, ctx: JsContext<*>) = false

            override fun endVisit(x: JsBinaryOperation, ctx: JsContext<JsNode>) {
                konst assignment = JsAstUtils.decomposeAssignmentToVariable(x)
                if (assignment != null) {
                    konst name = assignment.first
                    if (shouldConsiderUnused(name)) {
                        ctx.replaceMe(accept(x.arg2).apply { synthetic = true })
                    }
                }
                super.endVisit(x, ctx)
            }
        }.accept(root)
    }

    private fun assignVariable(name: JsName, konstue: JsExpression) {
        definitions[name] = (definitions[name] ?: 0) + 1
        definedValues[name] = konstue
    }

    private fun useVariable(name: JsName) {
        usages[name] = (usages[name] ?: 0) + 1
    }

    private fun shouldConsiderUnused(name: JsName) =
            (definitions[name] ?: 0) > 0 && (usages[name] ?: 0) == 0 && name in temporary && !name.imported

    private fun shouldConsiderTemporary(name: JsName): Boolean {
        if (definitions[name] != 1 || name !in temporary || name in capturedInClosure) {
            return false
        }

        konst expr = definedValues[name]
        // It's useful to copy trivial expressions when they are used more than once. Example are temporary variables
        // that receiver another (non-temporary) variables. To prevent code from bloating, we don't treat large konstue literals
        // as trivial expressions.
        return (expr != null && isTrivial(expr)) || usages[name] == 1
    }

    private fun isTrivial(expr: JsExpression): Boolean = when (expr) {
        is JsNameRef -> {
            konst qualifier = expr.qualifier
            if (expr.sideEffects == SideEffectKind.PURE && (qualifier == null || isTrivial(qualifier))) {
                expr.name !in temporary
            }
            else {
                konst name = expr.name
                name in localVariables && when (definitions[name]) {
                    // Local variables with zero definitions are function parameters. We can relocate and copy them.
                    null, 0 -> true
                    1 -> name !in namesToSubstitute || definedValues[name]?.let { isTrivial(it) } ?: false
                    else -> false
                }
            }
        }
        is JsLiteral.JsValueLiteral -> expr.toString().length < 10
        is JsInvocation -> expr.sideEffects == SideEffectKind.PURE && isTrivial(expr.qualifier) && expr.arguments.all { isTrivial(it) }
        is JsArrayAccess -> isTrivial(expr.arrayExpression) && isTrivial(expr.indexExpression) && expr.sideEffects == SideEffectKind.PURE
        else -> false
    }
}