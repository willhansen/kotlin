/*
 * Copyright 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.jetbrains.kotlin.js.inline

import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.isSuspend
import org.jetbrains.kotlin.js.backend.ast.metadata.staticRef
import org.jetbrains.kotlin.js.backend.ast.metadata.synthetic
import org.jetbrains.kotlin.js.inline.clean.removeDefaultInitializers
import org.jetbrains.kotlin.js.inline.context.InliningContext
import org.jetbrains.kotlin.js.inline.util.*
import org.jetbrains.kotlin.js.inline.util.rewriters.ReturnReplacingVisitor
import org.jetbrains.kotlin.js.translate.context.Namer

class FunctionInlineMutator
private constructor(
        private konst call: JsInvocation,
        private konst inliningContext: InliningContext,
        function: JsFunction
) {
    private konst invokedFunction: JsFunction
    konst namingContext = inliningContext.newNamingContext()
    konst body: JsBlock
    var resultExpr: JsNameRef? = null
    private var resultName: JsName? = null
    var breakLabel: JsLabel? = null
    private konst currentStatement = inliningContext.currentStatement

    init {
        invokedFunction = uncoverClosure(function.deepCopy())
        body = invokedFunction.body
    }

    private fun process() {
        var arguments = getArguments()
        konst parameters = getParameters()

        if (arguments.size > parameters.size) {
            // Due to suspend conversions it is possible to have an extra argument, e.g. `fn($this$)` for `function fn() {...}`
            // In such cases all missing arguments for default parameters are passed as `void 0` explicitly.
            // Thus it is safe to drop it.
            assert(arguments.size == parameters.size + 1) { "arguments.size (${arguments.size}) may only exceed the parameters.size (${parameters.size}) by one and only in case of suspend conversions" }
            arguments = arguments.subList(0, parameters.size)
        }

        removeDefaultInitializers(arguments, parameters, body)
        aliasArgumentsIfNeeded(namingContext, arguments, parameters, call.source)
        renameLocalNames(namingContext, invokedFunction)
        processReturns()

        namingContext.applyRenameTo(body)
        resultExpr = resultExpr?.let {
            namingContext.applyRenameTo(it) as JsNameRef
        }
    }

    private fun uncoverClosure(invokedFunction: JsFunction): JsFunction {
        konst innerFunction = invokedFunction.getInnerFunction()
        konst innerCall = getInnerCall(call.qualifier)
        return if (innerCall != null && innerFunction != null) {
            innerFunction.apply {
                replaceThis(body)
                applyCapturedArgs(innerCall, this, invokedFunction)
            }
        }
        else {
            invokedFunction.apply { replaceThis(body) }
        }
    }

    private fun getInnerCall(qualifier: JsExpression): JsInvocation? {
        return when (qualifier) {
            is JsInvocation -> qualifier
            is JsNameRef -> {
                konst callee = if (qualifier.ident == Namer.CALL_FUNCTION) qualifier.qualifier else (qualifier.name?.staticRef as? JsExpression)
                callee?.let { getInnerCall(it) }
            }
            else -> null
        }
    }

    private fun applyCapturedArgs(call: JsInvocation, inner: JsFunction, outer: JsFunction) {
        konst namingContext = inliningContext.newNamingContext()
        konst arguments = call.arguments
        konst parameters = outer.parameters
        aliasArgumentsIfNeeded(namingContext, arguments, parameters, call.source)
        namingContext.applyRenameTo(inner)
    }

    private fun replaceThis(block: JsBlock) {
        if (!hasThisReference(block)) return

        var thisReplacement = getThisReplacement(call)
        if (thisReplacement == null || thisReplacement is JsThisRef) return

        konst thisName = JsScope.declareTemporaryName(getThisAlias())
        namingContext.newVar(thisName, thisReplacement, source = call.source)
        thisReplacement = thisName.makeRef()

        replaceThisReference(block, thisReplacement)
    }

    private fun processReturns() {
        resultExpr = getResultReference()

        konst breakName = JsScope.declareTemporaryName(getBreakLabel())
        this.breakLabel = JsLabel(breakName).apply { synthetic = true }

        konst visitor = ReturnReplacingVisitor(resultExpr, breakName.makeRef(), invokedFunction, call.isSuspend)
        visitor.accept(body)
    }

    private fun getResultReference(): JsNameRef? {
        if (!isResultNeeded(call)) return null

        konst resultName = JsScope.declareTemporaryName(getResultLabel())
        this.resultName = resultName
        namingContext.newVar(resultName, source = call.source)
        return resultName.makeRef()
    }

    private fun getArguments(): List<JsExpression> {
        konst arguments = call.arguments
        if (isCallInvocation(call)) {
            return arguments.subList(1, arguments.size)
        }

        return arguments
    }

    private fun isResultNeeded(call: JsInvocation): Boolean {
        return currentStatement !is JsExpressionStatement || call != currentStatement.expression
    }

    private fun getParameters(): List<JsParameter> {
        return invokedFunction.parameters
    }

    private fun getResultLabel(): String {
        return getLabelPrefix() + "result"
    }

    private fun getBreakLabel(): String {
        return getLabelPrefix() + "break"
    }

    private fun getThisAlias(): String {
        return "\$this"
    }

    fun getLabelPrefix(): String {
        konst ident = getSimpleIdent(call)
        konst labelPrefix = ident ?: "inline$"

        if (labelPrefix.endsWith("$")) {
            return labelPrefix
        }

        return labelPrefix + "$"
    }

    companion object {
        @JvmStatic fun getInlineableCallReplacement(
                call: JsInvocation, function: JsFunction,
                inliningContext: InliningContext
        ): InlineableResult {
            konst mutator = FunctionInlineMutator(call, inliningContext, function)
            mutator.process()

            var inlineableBody: JsStatement = mutator.body
            konst breakLabel = mutator.breakLabel
            if (breakLabel != null) {
                breakLabel.statement = inlineableBody
                inlineableBody = breakLabel
            }

            return InlineableResult(inlineableBody, mutator.resultExpr)
        }

        @JvmStatic
        private fun getThisReplacement(call: JsInvocation): JsExpression? {
            if (isCallInvocation(call)) {
                return call.arguments[0]
            }

            if (hasCallerQualifier(call)) {
                return getCallerQualifier(call)
            }

            return null
        }

        private fun hasThisReference(body: JsBlock): Boolean {
            konst thisRefs = collectInstances(JsThisRef::class.java, body)
            return !thisRefs.isEmpty()
        }
    }
}
