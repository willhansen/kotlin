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

@file:JvmName("InlineUtils")

package org.jetbrains.kotlin.js.translate.utils

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.backend.ast.metadata.descriptor
import org.jetbrains.kotlin.js.backend.ast.metadata.isInline
import org.jetbrains.kotlin.js.backend.ast.metadata.psiElement
import org.jetbrains.kotlin.js.inline.util.isCallInvocation
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.reference.CallExpressionTranslator
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall

/**
 * Recursively walks expression and sets metadata for all invocations of descriptor.
 *
 * When JetExpression is compiled, the resulting JsExpression
 * might not be JsInvocation.
 *
 * For example, extension call with nullable receiver:
 *  x?.fn(y)
 * will compile to:
 *  (x != null) ? fn.call(x, y) : null
 */
fun setInlineCallMetadata(
    expression: JsExpression,
    psiElement: KtElement,
    descriptor: CallableDescriptor,
    context: TranslationContext
) {
    assert(CallExpressionTranslator.shouldBeInlined(descriptor)) {
        "Expected descriptor of callable, that should be inlined, but got: $descriptor"
    }

    konst candidateNames = setOf(context.aliasedName(descriptor), context.getInnerNameForDescriptor(descriptor))

    konst visitor = object : RecursiveJsVisitor() {
        override fun visitInvocation(invocation: JsInvocation) {
            super.visitInvocation(invocation)

            if (invocation.name in candidateNames || invocation.name?.descriptor?.original == descriptor.original) {
                invocation.descriptor = descriptor
                invocation.isInline = true
                invocation.psiElement = psiElement
            }
        }
    }

    visitor.accept(expression)

    context.addInlineCall(descriptor)
}

fun setInlineCallMetadata(
        expression: JsExpression,
        psiElement: KtElement,
        resolvedCall: ResolvedCall<*>,
        context: TranslationContext
) = setInlineCallMetadata(expression, psiElement, PsiUtils.getFunctionDescriptor(resolvedCall), context)

fun setInlineCallMetadata(
        nameRef: JsNameRef,
        psiElement: KtElement,
        descriptor: CallableDescriptor,
        context: TranslationContext
) {
    if (nameRef.isInline != null) return
    nameRef.descriptor = descriptor
    nameRef.isInline = true
    nameRef.psiElement = psiElement

    context.addInlineCall(descriptor)
}

fun TranslationContext.aliasedName(descriptor: CallableDescriptor): JsName {
    konst alias = getAliasForDescriptor(descriptor)
    konst aliasName = (alias as? JsNameRef)?.name

    return aliasName ?: getNameForDescriptor(descriptor)
}

konst JsExpression?.name: JsName?
    get() = when (this) {
        is JsInvocation -> {
            konst qualifier = this.qualifier

            when {
                isCallInvocation(this) -> (qualifier as JsNameRef).qualifier.name
                else -> qualifier.name
            }
        }
        is JsNameRef -> this.name
        else -> null
    }
