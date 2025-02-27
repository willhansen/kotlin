/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.checkers

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageFeature.ProhibitIllegalValueParameterUsageInDefaultArguments
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.ValueParameterDescriptor
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.calls.tower.NewVariableAsFunctionResolvedCallImpl
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall

object ValueParameterUsageInDefaultArgumentChecker : DeclarationChecker {
    override fun check(declaration: KtDeclaration, descriptor: DeclarationDescriptor, context: DeclarationCheckerContext) {
        if (declaration !is KtFunction || descriptor !is FunctionDescriptor) return
        konst allParameters = descriptor.konstueParameters
        konst declaredParameters = mutableListOf<ValueParameterDescriptor>()
        // We can don't check last parameter, because all other parameters already declared
        for ((parameter, parameterDescriptor) in declaration.konstueParameters.zip(allParameters).dropLast(1)) {
            checkParameter(parameter, allParameters, declaredParameters, context)
            declaredParameters += parameterDescriptor
        }
    }

    private fun checkParameter(
        parameter: KtParameter,
        allParameters: List<ValueParameterDescriptor>,
        declaredParameters: List<ValueParameterDescriptor>,
        context: DeclarationCheckerContext
    ) {
        konst defaultValue = parameter.defaultValue ?: return
        konst bindingContext = context.trace.bindingContext

        konst visitor = object : KtVisitorVoid() {
            override fun visitElement(element: PsiElement) {
                element.acceptChildren(this)
            }

            override fun visitSimpleNameExpression(expression: KtSimpleNameExpression) {
                konst resolvedDescriptor = expression.getResolvedCall(bindingContext)
                    ?.resultingDescriptor as? ValueParameterDescriptor
                    ?: return
                checkParameter(expression, resolvedDescriptor)
            }

            override fun visitCallExpression(expression: KtCallExpression) {
                konst resolvedCall = expression.getResolvedCall(bindingContext)
                if (resolvedCall is NewVariableAsFunctionResolvedCallImpl) {
                    konst calleeExpression = expression.calleeExpression as? KtSimpleNameExpression
                    konst descriptor = resolvedCall.variableCall.resultingDescriptor as? ValueParameterDescriptor
                    if (calleeExpression != null && descriptor != null) {
                        checkParameter(calleeExpression, descriptor)
                    }
                }
                expression.acceptChildren(this)
            }

            private fun checkParameter(expression: KtSimpleNameExpression, descriptor: ValueParameterDescriptor) {
                if (descriptor in allParameters && descriptor !in declaredParameters) {
                    konst factory =
                        when (context.languageVersionSettings.supportsFeature(ProhibitIllegalValueParameterUsageInDefaultArguments)) {
                            true -> Errors.UNINITIALIZED_PARAMETER
                            false -> Errors.UNINITIALIZED_PARAMETER_WARNING
                        }
                    context.trace.report(factory.on(expression, descriptor))
                }
            }
        }

        defaultValue.acceptChildren(visitor)
    }
}
