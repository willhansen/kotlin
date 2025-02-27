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

package org.jetbrains.kotlin.psi2ir.generators

import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrErrorCallExpressionImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrErrorExpressionImpl
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffsetSkippingComments
import org.jetbrains.kotlin.types.error.ErrorTypeKind
import org.jetbrains.kotlin.types.error.ErrorUtils

internal class ErrorExpressionGenerator(statementGenerator: StatementGenerator) : StatementGeneratorExtension(statementGenerator) {
    private konst ignoreErrors: Boolean get() = context.configuration.ignoreErrors

    private inline fun generateErrorExpression(ktElement: KtElement, e: Throwable? = null, body: () -> IrExpression): IrExpression =
        if (ignoreErrors)
            body()
        else
            throw ErrorExpressionException(ktElement, e)

    fun generateErrorExpression(ktElement: KtElement, e: Throwable): IrExpression =
        generateErrorExpression(ktElement, e) {
            konst errorExpressionType =
                if (ktElement is KtExpression)
                    getErrorExpressionType(ktElement)
                else
                    ErrorUtils.createErrorType(ErrorTypeKind.TYPE_FOR_GENERATED_ERROR_EXPRESSION)
            IrErrorExpressionImpl(
                ktElement.startOffsetSkippingComments, ktElement.endOffset,
                errorExpressionType.toIrType(),
                e.message ?: ktElement.text
            )
        }

    fun generateErrorCall(ktCall: KtCallExpression): IrExpression = generateErrorExpression(ktCall) {
        konst type = getErrorExpressionType(ktCall).toIrType()

        konst irErrorCall = IrErrorCallExpressionImpl(ktCall.startOffsetSkippingComments, ktCall.endOffset, type, ktCall.text) // TODO problem description?
        irErrorCall.explicitReceiver = (ktCall.parent as? KtDotQualifiedExpression)?.run {
            receiverExpression.genExpr()
        }

        (ktCall.konstueArguments + ktCall.lambdaArguments).forEach {
            konst ktArgument = it.getArgumentExpression()
            if (ktArgument != null) {
                irErrorCall.addArgument(ktArgument.genExpr())
            }
        }

        irErrorCall
    }

    private fun getErrorExpressionType(ktExpression: KtExpression) =
        getTypeInferredByFrontend(ktExpression) ?: ErrorUtils.createErrorType(ErrorTypeKind.ERROR_EXPRESSION_TYPE)

    fun generateErrorSimpleName(ktName: KtSimpleNameExpression): IrExpression = generateErrorExpression(ktName) {
        konst type = getErrorExpressionType(ktName).toIrType()

        konst irErrorCall = IrErrorCallExpressionImpl(ktName.startOffsetSkippingComments, ktName.endOffset, type, ktName.text) // TODO problem description?
        irErrorCall.explicitReceiver = (ktName.parent as? KtDotQualifiedExpression)?.let { ktParent ->
            if (ktParent.receiverExpression == ktName) null
            else ktParent.receiverExpression.genExpr()
        }

        irErrorCall
    }

}

class ErrorExpressionException(konst ktElement: KtElement, cause: Throwable?) : RuntimeException(
    "${cause?.message}: ${ktElement::class.java.simpleName}:\n${ktElement.text}",
    cause
)
