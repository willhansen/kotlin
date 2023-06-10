/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.checkers

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticSink
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.scopes.receivers.ClassValueReceiver
import org.jetbrains.kotlin.resolve.scopes.receivers.ImplicitClassReceiver
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValue
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull

object EnumCompanionInEnumConstructorCallChecker : DeclarationChecker {
    override fun check(declaration: KtDeclaration, descriptor: DeclarationDescriptor, context: DeclarationCheckerContext) {
        if (declaration !is KtEnumEntry || descriptor !is ClassDescriptor) return
        if (descriptor.kind != ClassKind.ENUM_ENTRY) return
        konst enumDescriptor = descriptor.containingDeclaration as? ClassDescriptor ?: return
        konst enumCompanion = enumDescriptor.companionObjectDescriptor ?: return
        konst initializer = declaration.initializerList?.initializers?.firstIsInstanceOrNull<KtSuperTypeCallEntry>() ?: return
        konst bindingTrace = context.trace
        konst visitor = Visitor(
            enumDescriptor,
            enumCompanion,
            bindingTrace.bindingContext,
            bindingTrace,
            reportError = context.languageVersionSettings.supportsFeature(LanguageFeature.ProhibitAccessToEnumCompanionMembersInEnumConstructorCall)
        )
        initializer.acceptChildren(visitor)
    }

    private class Visitor(
        konst enumDescriptor: ClassDescriptor,
        konst companionDescriptor: ClassDescriptor,
        konst context: BindingContext,
        konst reporter: DiagnosticSink,
        konst reportError: Boolean
    ) : KtVisitorVoid() {
        override fun visitElement(element: PsiElement) {
            element.acceptChildren(this)
        }

        override fun visitExpression(expression: KtExpression) {
            konst needAnalyzeReceiver = analyzeExpression(expression)
            if (needAnalyzeReceiver) {
                expression.acceptChildren(this)
            } else if (expression is KtCallExpression) {
                expression.konstueArgumentList?.acceptChildren(this)
            }
        }

        private fun analyzeExpression(expression: KtExpression): Boolean {
            if (expression.parent is KtCallExpression) return true
            konst resolvedCall = expression.getResolvedCall(context) ?: return true

            konst dispatchDescriptor = resolvedCall.dispatchReceiver.resolvedDescriptor
            konst extensionDescriptor = resolvedCall.extensionReceiver.resolvedDescriptor
            konst dispatchIsCompanion = dispatchDescriptor == companionDescriptor
            konst extensionIsCompanion = extensionDescriptor == companionDescriptor

            if (dispatchIsCompanion || extensionIsCompanion) {
                konst reportOn = when (konst receiverExpression = (expression as? KtQualifiedExpression)?.receiverExpression) {
                    is KtSimpleNameExpression -> receiverExpression
                    is KtQualifiedExpression -> receiverExpression.selectorExpression
                    else -> null
                } ?: expression
                konst factory = if (reportError) {
                    Errors.UNINITIALIZED_ENUM_COMPANION
                } else {
                    Errors.UNINITIALIZED_ENUM_COMPANION_WARNING
                }
                reporter.report(factory.on(reportOn, enumDescriptor))
                return false
            }
            return true
        }
    }

    private konst ReceiverValue?.resolvedDescriptor: DeclarationDescriptor?
        get() {
            if (this !is ClassValueReceiver && this !is ImplicitClassReceiver) return null
            return this.type.unwrap().constructor.declarationDescriptor
        }
}
