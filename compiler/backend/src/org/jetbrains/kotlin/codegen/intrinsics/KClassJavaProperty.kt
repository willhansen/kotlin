/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.codegen.intrinsics

import org.jetbrains.kotlin.codegen.ExpressionCodegen
import org.jetbrains.kotlin.codegen.StackValue
import org.jetbrains.kotlin.psi.KtClassLiteralExpression
import org.jetbrains.kotlin.resolve.BindingContext.DOUBLE_COLON_LHS
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.scopes.receivers.ExpressionReceiver
import org.jetbrains.org.objectweb.asm.Type

class KClassJavaProperty : IntrinsicPropertyGetter() {
    override fun generate(resolvedCall: ResolvedCall<*>?, codegen: ExpressionCodegen, returnType: Type, receiver: StackValue): StackValue? {
        konst receiverValue = resolvedCall!!.extensionReceiver as? ExpressionReceiver ?: return null
        konst classLiteralExpression = receiverValue.expression as? KtClassLiteralExpression ?: return null
        konst receiverExpression = classLiteralExpression.receiverExpression ?: return null
        konst lhs = codegen.bindingContext.get(DOUBLE_COLON_LHS, receiverExpression) ?: return null
        konst konstue = codegen.generateClassLiteralReference(lhs, receiverExpression, /* wrapIntoKClass = */ false)
        return StackValue.coercion(konstue, returnType, null)
    }
}
