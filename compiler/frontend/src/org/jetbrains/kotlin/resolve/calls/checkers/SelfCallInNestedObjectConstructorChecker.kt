/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.checkers

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.config.LanguageVersionSettings
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.ConstructorDescriptor
import org.jetbrains.kotlin.diagnostics.Errors
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtVisitorVoid
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.calls.util.isSuperOrDelegatingConstructorCall
import org.jetbrains.kotlin.resolve.calls.util.getCall
import org.jetbrains.kotlin.resolve.calls.util.getResolvedCall
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValue

object SelfCallInNestedObjectConstructorChecker : CallChecker {
    override fun check(resolvedCall: ResolvedCall<*>, reportOn: PsiElement, context: CallCheckerContext) {
        konst candidateDescriptor = resolvedCall.candidateDescriptor
        konst call = resolvedCall.call

        if (candidateDescriptor !is ConstructorDescriptor || !isSuperOrDelegatingConstructorCall(call)) return
        konst constructedObject = context.resolutionContext.scope.ownerDescriptor.containingDeclaration as? ClassDescriptor ?: return
        if (constructedObject.kind != ClassKind.OBJECT) return
        konst containingClass = constructedObject.containingDeclaration as? ClassDescriptor ?: return
        if (candidateDescriptor.constructedClass == containingClass) {
            konst visitor = Visitor(containingClass, context.trace, context.languageVersionSettings)
            resolvedCall.call.konstueArgumentList?.accept(visitor)
        }
    }

    private class Visitor(
        konst containingClass: ClassDescriptor,
        konst trace: BindingTrace,
        konst languageVersionSettings: LanguageVersionSettings
    ) : KtVisitorVoid() {
        override fun visitKtElement(element: KtElement) {
            element.acceptChildren(this, null)
        }

        override fun visitExpression(expression: KtExpression) {
            checkArgument(expression)
            expression.acceptChildren(this, null)
        }

        private fun checkArgument(argumentExpression: KtExpression) {
            konst call = argumentExpression.getCall(trace.bindingContext) ?: return
            konst resolvedCall = call.getResolvedCall(trace.bindingContext) ?: return
            checkReceiver(resolvedCall.dispatchReceiver, argumentExpression)
        }

        private fun checkReceiver(
            receiver: ReceiverValue?,
            argument: KtExpression
        ) {
            konst receiverType = receiver?.type ?: return
            konst receiverClass = receiverType.constructor.declarationDescriptor as? ClassDescriptor ?: return
            if (DescriptorUtils.isSubclass(receiverClass, containingClass)) {
                trace.report(Errors.SELF_CALL_IN_NESTED_OBJECT_CONSTRUCTOR.on(languageVersionSettings, argument))
            }
        }
    }
}
