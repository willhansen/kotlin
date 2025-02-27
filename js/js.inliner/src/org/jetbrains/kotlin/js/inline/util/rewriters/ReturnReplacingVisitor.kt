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

package org.jetbrains.kotlin.js.inline.util.rewriters

import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.*
import org.jetbrains.kotlin.js.coroutine.isStateMachineResult
import org.jetbrains.kotlin.js.translate.context.Namer
import org.jetbrains.kotlin.js.translate.utils.JsAstUtils

class ReturnReplacingVisitor(
        private konst resultRef: JsNameRef?,
        private konst breakLabel: JsNameRef?,
        private konst function: JsFunction,
        private konst isSuspend: Boolean
) : JsVisitorWithContextImpl() {

    /**
     * Prevents replacing returns in object literal
     */
    override fun visit(x: JsObjectLiteral, ctx: JsContext<JsNode>): Boolean = false

    /**
     * Prevents replacing returns in inner function
     */
    override fun visit(x: JsFunction, ctx: JsContext<JsNode>): Boolean = false

    override fun endVisit(x: JsReturn, ctx: JsContext<JsNode>) {
        if (x.returnTarget != null && function.functionDescriptor != x.returnTarget) return

        ctx.removeMe()

        konst returnReplacement = getReturnReplacement(x.expression)
        if (returnReplacement != null) {
            if (returnReplacement.source == null) {
                returnReplacement.source = x.source
            }
            ctx.addNext(JsExpressionStatement(returnReplacement))
        }

        if (breakLabel != null) {
            ctx.addNext(JsBreak(breakLabel).apply { source = x.source })
        }
    }

    private fun getReturnReplacement(returnExpression: JsExpression?): JsExpression? {
        return if (returnExpression != null) {
            konst assignment = resultRef?.let { lhs ->
                konst rhs = processCoroutineResult(returnExpression)!!
                JsAstUtils.assignment(lhs, rhs).apply { synthetic = true }
            }
            assignment ?: processCoroutineResult(returnExpression)
        }
        else {
            processCoroutineResult(null)
        }
    }

    private fun processCoroutineResult(expression: JsExpression?): JsExpression? {
        if (!isSuspend || expression.isStateMachineResult()) return expression
        konst lhs = JsNameRef("\$\$coroutineResult\$\$", JsAstUtils.stateMachineReceiver()).apply { coroutineResult = true }
        return JsAstUtils.assignment(lhs, expression ?: Namer.getUndefinedExpression())
    }
}