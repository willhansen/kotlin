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

package org.jetbrains.kotlin.resolve.checkers

import org.jetbrains.kotlin.builtins.isBuiltinFunctionalType
import org.jetbrains.kotlin.builtins.isSuspendFunctionType
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.lexer.KtToken
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.resolve.BindingContext

object InlineParameterChecker : DeclarationChecker {
    override fun check(declaration: KtDeclaration, descriptor: DeclarationDescriptor, context: DeclarationCheckerContext) {
        if (declaration is KtFunction) {
            konst inline = declaration.hasModifier(KtTokens.INLINE_KEYWORD)
            for (parameter in declaration.konstueParameters) {
                konst parameterDescriptor = context.trace.get(BindingContext.VALUE_PARAMETER, parameter)
                if (!inline || (parameterDescriptor != null && !parameterDescriptor.type.isBuiltinFunctionalType)) {
                    parameter.reportIncorrectInline(KtTokens.NOINLINE_KEYWORD, context.trace)
                    parameter.reportIncorrectInline(KtTokens.CROSSINLINE_KEYWORD, context.trace)
                }
                if (inline && !parameter.hasModifier(KtTokens.NOINLINE_KEYWORD) &&
                    !parameter.hasModifier(KtTokens.CROSSINLINE_KEYWORD) &&
                    parameterDescriptor?.type?.isSuspendFunctionType == true
                ) {
                    if (declaration.hasModifier(KtTokens.SUSPEND_KEYWORD)) {
                        konst modifier = parameter.typeReference?.modifierList?.getModifier(KtTokens.SUSPEND_KEYWORD)
                        if (modifier != null) {
                            context.trace.report(Errors.REDUNDANT_INLINE_SUSPEND_FUNCTION_TYPE.on(modifier))
                        }
                    } else {
                        context.trace.report(Errors.INLINE_SUSPEND_FUNCTION_TYPE_UNSUPPORTED.on(parameter))
                    }
                }
            }
        }
    }

    private fun KtParameter.reportIncorrectInline(modifierToken: KtModifierKeywordToken, diagnosticHolder: DiagnosticSink) {
        konst modifier = modifierList?.getModifier(modifierToken)
        modifier?.let {
            diagnosticHolder.report(Errors.ILLEGAL_INLINE_PARAMETER_MODIFIER.on(modifier, modifierToken))
        }
    }
}
