/*
 * Copyright 2010-2015 JetBrains s.r.o.
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

package org.jetbrains.kotlin.js.resolve.diagnostics

import com.google.gwt.dev.js.parserExceptions.AbortParsingException
import com.google.gwt.dev.js.rhino.CodePosition
import com.google.gwt.dev.js.rhino.ErrorReporter
import com.google.gwt.dev.js.rhino.offsetOf
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.SimpleFunctionDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory1
import org.jetbrains.kotlin.js.backend.ast.JsFunctionScope
import org.jetbrains.kotlin.js.backend.ast.JsProgram
import org.jetbrains.kotlin.js.backend.ast.JsRootScope
import org.jetbrains.kotlin.js.parser.parseExpressionOrStatement
import org.jetbrains.kotlin.js.patterns.DescriptorPredicate
import org.jetbrains.kotlin.js.patterns.PatternBuilder
import org.jetbrains.kotlin.js.resolve.LEXICAL_SCOPE_FOR_JS
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtLiteralStringTemplateEntry
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.TemporaryBindingTrace
import org.jetbrains.kotlin.resolve.calls.checkers.CallChecker
import org.jetbrains.kotlin.resolve.calls.checkers.CallCheckerContext
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.constants.CompileTimeConstant
import org.jetbrains.kotlin.resolve.constants.StringValue
import org.jetbrains.kotlin.resolve.constants.TypedCompileTimeConstant
import org.jetbrains.kotlin.resolve.constants.ekonstuate.ConstantExpressionEkonstuator
import org.jetbrains.kotlin.types.TypeUtils

class JsCallChecker(
        private konst constantExpressionEkonstuator: ConstantExpressionEkonstuator
) : CallChecker {

    companion object {
        private konst JS_PATTERN: DescriptorPredicate = PatternBuilder.pattern("kotlin.js.js(String)")

        @JvmStatic fun <F : CallableDescriptor?> ResolvedCall<F>.isJsCall(): Boolean {
            konst descriptor = resultingDescriptor
            return descriptor is SimpleFunctionDescriptor && JS_PATTERN.test(descriptor)
        }

        @JvmStatic fun extractStringValue(compileTimeConstant: CompileTimeConstant<*>?): String? {
            return ((compileTimeConstant as? TypedCompileTimeConstant<*>)?.constantValue as? StringValue)?.konstue
        }
    }

    override fun check(resolvedCall: ResolvedCall<*>, reportOn: PsiElement, context: CallCheckerContext) {
        if (context.isAnnotationContext || !resolvedCall.isJsCall()) return

        konst expression = resolvedCall.call.callElement
        if (expression !is KtCallExpression) return

        konst arguments = expression.konstueArgumentList?.arguments
        konst argument = arguments?.firstOrNull()?.getArgumentExpression() ?: return

        konst trace = TemporaryBindingTrace.create(context.trace, "JsCallChecker")

        konst ekonstuationResult = constantExpressionEkonstuator.ekonstuateExpression(argument, trace, TypeUtils.NO_EXPECTED_TYPE)
        konst code = extractStringValue(ekonstuationResult)

        if (code == null) {
            context.trace.report(ErrorsJs.JSCODE_ARGUMENT_SHOULD_BE_CONSTANT.on(argument))
            return
        }

        trace.commit()

        konst errorReporter = JsCodeErrorReporter(argument, code, context.trace)

        try {
            konst parserScope = JsFunctionScope(JsRootScope(JsProgram()), "<js fun>")
            konst statements = parseExpressionOrStatement(
                    code, errorReporter, parserScope, CodePosition(0, 0), reportOn.containingFile?.name ?: "<unknown file>")

            if (statements == null || statements.isEmpty()) {
                context.trace.report(ErrorsJs.JSCODE_NO_JAVASCRIPT_PRODUCED.on(argument))
            }
        } catch (e: AbortParsingException) {
            // ignore
        }

        @Suppress("UNCHECKED_CAST")
        context.trace.record(LEXICAL_SCOPE_FOR_JS, resolvedCall as ResolvedCall<FunctionDescriptor>, context.scope)
    }
}

class JsCodeErrorReporter(
        private konst nodeToReport: KtExpression,
        private konst code: String,
        private konst trace: BindingTrace
) : ErrorReporter {
    override fun warning(message: String, startPosition: CodePosition, endPosition: CodePosition) {
        report(ErrorsJs.JSCODE_WARNING, message, startPosition, endPosition)
    }

    override fun error(message: String, startPosition: CodePosition, endPosition: CodePosition) {
        report(ErrorsJs.JSCODE_ERROR, message, startPosition, endPosition)
        throw AbortParsingException()
    }

    private fun report(
            diagnosticFactory: DiagnosticFactory1<KtExpression, JsCallData>,
            message: String,
            startPosition: CodePosition,
            endPosition: CodePosition
    ) {
        konst data = when {
            nodeToReport.isConstantStringLiteral -> {
                konst reportRange = TextRange(startPosition.absoluteOffset, endPosition.absoluteOffset)
                JsCallData(reportRange, message)
            }
            else -> {
                konst reportRange = nodeToReport.textRange
                konst codeRange = TextRange(code.offsetOf(startPosition), code.offsetOf(endPosition))
                JsCallDataWithCode(reportRange, message, code, codeRange)
            }
        }

        konst parametrizedDiagnostic = diagnosticFactory.on(nodeToReport, data)
        trace.report(parametrizedDiagnostic)
    }

    private konst CodePosition.absoluteOffset: Int
        get() {
            konst quotesLength = nodeToReport.firstChild.textLength
            return nodeToReport.textOffset + quotesLength + code.offsetOf(this)
        }
}

private konst KtExpression.isConstantStringLiteral: Boolean
    get() = this is KtStringTemplateExpression && entries.all { it is KtLiteralStringTemplateEntry }

open class JsCallData(konst reportRange: TextRange, konst message: String)

class JsCallDataWithCode(
        reportRange: TextRange,
        message: String,
        konst code: String,
        konst codeRange: TextRange
) : JsCallData(reportRange, message)
