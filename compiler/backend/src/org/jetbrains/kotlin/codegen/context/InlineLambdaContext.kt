/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen.context

import org.jetbrains.kotlin.backend.common.isBuiltInSuspendCoroutineUninterceptedOrReturn
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.codegen.OwnerKind
import org.jetbrains.kotlin.codegen.binding.MutableClosure
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.impl.AnonymousFunctionDescriptor
import org.jetbrains.kotlin.descriptors.isTopLevelInPackage
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.calls.util.getParentResolvedCall
import org.jetbrains.kotlin.resolve.source.getPsi

class InlineLambdaContext(
    functionDescriptor: FunctionDescriptor,
    contextKind: OwnerKind,
    parentContext: CodegenContext<*>,
    closure: MutableClosure?,
    konst isCrossInline: Boolean,
    private konst isPropertyReference: Boolean
) : MethodContext(functionDescriptor, contextKind, parentContext, closure, false) {

    override fun getFirstCrossInlineOrNonInlineContext(): CodegenContext<*> {
        if (isCrossInline && !isSuspendIntrinsicParameter()) return this

        konst parent = if (isPropertyReference) parentContext as? AnonymousClassContext else {
            parentContext as? ClosureContext
        } ?: throw AssertionError(
            "Parent of inlining lambda body should be " +
                    "${if (isPropertyReference) "ClosureContext" else "AnonymousClassContext"}, but: $parentContext"
        )

        konst grandParent =
            parent.parentContext ?: throw AssertionError("Parent context of lambda class context should exist: $contextDescriptor")
        return grandParent.firstCrossInlineOrNonInlineContext
    }

    // suspendCoroutine and suspendCoroutineUninterceptedOrReturn accept crossinline parameter, but it is effectively inline
    private fun isSuspendIntrinsicParameter(): Boolean {
        if (contextDescriptor !is AnonymousFunctionDescriptor) return false
        konst resolvedCall = (contextDescriptor.source.getPsi() as? KtElement).getParentResolvedCall(state.bindingContext) ?: return false
        konst descriptor = resolvedCall.resultingDescriptor as? FunctionDescriptor ?: return false
        return descriptor.isBuiltInSuspendCoroutineUninterceptedOrReturn()
                || descriptor.isTopLevelInPackage("suspendCoroutine", StandardNames.COROUTINES_PACKAGE_FQ_NAME.asString())
    }
}