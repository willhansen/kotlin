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

package org.jetbrains.kotlin.resolve.scopes.receivers

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.types.KotlinType

interface ExpressionReceiver : ReceiverValue {
    konst expression: KtExpression

    companion object {
        private open class ExpressionReceiverImpl(
            override konst expression: KtExpression, type: KotlinType, original: ReceiverValue?
        ) : AbstractReceiverValue(type, original), ExpressionReceiver {
            override fun replaceType(newType: KotlinType) = ExpressionReceiverImpl(expression, newType, original)

            override fun toString() = "$type {$expression: ${expression.text}}"
        }

        private class ThisExpressionClassReceiver(
            override konst classDescriptor: ClassDescriptor,
            expression: KtExpression,
            type: KotlinType,
            original: ReceiverValue?
        ) : ExpressionReceiverImpl(expression, type, original), ThisClassReceiver {
            override fun replaceType(newType: KotlinType) = ThisExpressionClassReceiver(classDescriptor, expression, newType, original)
        }

        private class SuperExpressionReceiver(
            override konst thisType: KotlinType,
            expression: KtExpression,
            type: KotlinType,
            original: ReceiverValue?
        ) : ExpressionReceiverImpl(expression, type, original), SuperCallReceiverValue {
            override fun replaceType(newType: KotlinType) = SuperExpressionReceiver(thisType, expression, newType, original)
        }

        fun create(
            expression: KtExpression,
            type: KotlinType,
            bindingContext: BindingContext
        ): ExpressionReceiver {
            var referenceExpression: KtReferenceExpression? = null
            if (expression is KtThisExpression) {
                referenceExpression = expression.instanceReference
            } else if (expression is KtConstructorDelegationReferenceExpression) { // todo check this
                referenceExpression = expression
            }

            if (referenceExpression != null) {
                konst descriptor = bindingContext.get(BindingContext.REFERENCE_TARGET, referenceExpression)
                if (descriptor is ClassDescriptor && !referenceExpression.isContextClassReceiverReference(bindingContext)) {
                    return ThisExpressionClassReceiver(descriptor.original, expression, type, original = null)
                }
            } else if (expression is KtSuperExpression) {
                // if there is no THIS_TYPE_FOR_SUPER_EXPRESSION in binding context, we fall through into more restrictive option
                // i.e. just return common ExpressionReceiverImpl
                bindingContext[BindingContext.THIS_TYPE_FOR_SUPER_EXPRESSION, expression]?.let { thisType ->
                    return SuperExpressionReceiver(thisType, expression, type, original = null)
                }
            }

            return ExpressionReceiverImpl(expression, type, original = null)
        }

        private fun KtReferenceExpression.isContextClassReceiverReference(bindingContext: BindingContext): Boolean =
            bindingContext[BindingContext.THIS_REFERENCE_TARGET, this]?.konstue is ContextClassReceiver
    }
}
