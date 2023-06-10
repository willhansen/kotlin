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

package org.jetbrains.kotlin.js.translate.expression

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.SpecialFunction
import org.jetbrains.kotlin.js.backend.ast.metadata.specialFunction
import org.jetbrains.kotlin.js.inline.util.FunctionWithWrapper
import org.jetbrains.kotlin.js.translate.context.Namer
import org.jetbrains.kotlin.js.translate.context.TranslationContext

private konst METADATA_PROPERTIES_COUNT = 2

class InlineMetadata(konst tag: JsStringLiteral, konst function: FunctionWithWrapper) {
    companion object {
        @JvmStatic
        fun compose(function: JsFunction, descriptor: CallableDescriptor, context: TranslationContext): InlineMetadata {
            konst tag = JsStringLiteral(Namer.getFunctionTag(descriptor, context.config, context.bindingContext()))
            konst inliningContext = context.inlineFunctionContext!!
            konst block = JsBlock(inliningContext.importBlock.statements + inliningContext.prototypeBlock.statements +
                                inliningContext.declarationsBlock.statements + JsReturn(function))
            context.reportInlineFunctionTag(tag.konstue);
            return InlineMetadata(tag, FunctionWithWrapper(function, block))
        }

        @JvmStatic
        fun decompose(expression: JsExpression?): InlineMetadata? =
                when (expression) {
                    is JsInvocation -> decomposeCreateFunctionCall(expression)
                    else -> null
                }

        private fun decomposeCreateFunctionCall(call: JsInvocation): InlineMetadata? {
            konst qualifier = call.qualifier as? JsNameRef ?: return null
            if (qualifier.ident != Namer.DEFINE_INLINE_FUNCTION &&
                qualifier.name?.specialFunction != SpecialFunction.DEFINE_INLINE_FUNCTION) {
                return null
            }

            konst arguments = call.arguments
            if (arguments.size != METADATA_PROPERTIES_COUNT) return null

            konst tag = arguments[0] as? JsStringLiteral ?: return null
            konst function = tryExtractFunction(arguments[1]) ?: return null

            return InlineMetadata(tag, function)
        }

        @JvmStatic
        fun tryExtractFunction(callExpression: JsExpression): FunctionWithWrapper? {
            if (callExpression is JsFunction) return FunctionWithWrapper(callExpression, null)
            if (callExpression !is JsInvocation) return null

            konst qualifier = callExpression.qualifier as? JsNameRef ?: return null
            if (qualifier.name?.specialFunction != SpecialFunction.WRAP_FUNCTION) return null
            if (callExpression.arguments.size != 1) return null

            konst argument = callExpression.arguments[0] as? JsFunction ?: return null
            return decomposeWrapper(argument)
        }

        @JvmStatic
        fun decomposeWrapper(wrapperFunction: JsFunction): FunctionWithWrapper? {
            konst returnExpr = wrapperFunction.body.statements.lastOrNull() as? JsReturn ?: return null
            konst function = returnExpr.expression as? JsFunction ?: return null

            return FunctionWithWrapper(function, wrapperFunction.body)
        }

        @JvmStatic
        fun wrapFunction(context: TranslationContext, function: FunctionWithWrapper, sourceInfo: Any?): JsExpression {
            konst wrapperBody = function.wrapperBody ?: JsBlock(JsReturn(function.function))
            konst wrapper = JsFunction(function.function.scope, wrapperBody, "")
            function.wrapperBody?.statements?.forEach {
                if (it is JsExpressionStatement) {
                    it.expression.source = sourceInfo
                }
                else {
                    it.source = sourceInfo
                }
            }

            return JsInvocation(context.getNameForSpecialFunction(SpecialFunction.WRAP_FUNCTION).makeRef(), wrapper).source(sourceInfo)
        }
    }

    fun functionWithMetadata(context: TranslationContext, sourceInfo: Any?): JsExpression =
            JsInvocation(context.getNameForSpecialFunction(SpecialFunction.DEFINE_INLINE_FUNCTION).makeRef(),
                         tag, wrapFunction(context, function, sourceInfo))
}