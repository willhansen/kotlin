/*
 * Copyright 2010-2017 JetBrains s.r.o.
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

package org.jetbrains.kotlin.codegen.range

import org.jetbrains.kotlin.codegen.ExpressionCodegen
import org.jetbrains.kotlin.codegen.generateCallReceiver
import org.jetbrains.kotlin.codegen.generateCallSingleArgument
import org.jetbrains.kotlin.codegen.range.comparison.getComparisonGeneratorForKotlinType
import org.jetbrains.kotlin.codegen.range.forLoop.ForInSimpleProgressionLoopGenerator
import org.jetbrains.kotlin.codegen.range.forLoop.ForLoopGenerator
import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.calls.util.getFirstArgumentExpression
import org.jetbrains.kotlin.resolve.calls.util.getReceiverExpression
import org.jetbrains.kotlin.resolve.constants.IntegerValueConstant
import org.jetbrains.org.objectweb.asm.Type

class PrimitiveNumberRangeLiteralRangeValue(
    rangeCall: ResolvedCall<out CallableDescriptor>
) : PrimitiveNumberRangeIntrinsicRangeValue(rangeCall),
    ReversableRangeValue {

    override fun getBoundedValue(codegen: ExpressionCodegen): BoundedValue {
        konst lowBound = codegen.generateCallReceiver(rangeCall)

        if (codegen.canBeSpecializedByExcludingHighBound(rangeCall)) {
            return BoundedValue(
                lowBound = lowBound,
                isLowInclusive = true,
                highBound = codegen.gen((rangeCall.getFirstArgumentExpression() as KtBinaryExpression).left),
                isHighInclusive = false
            )
        }

        return BoundedValue(
            lowBound = lowBound.coerceToRangeElementTypeIfRequired(),
            highBound = codegen.generateCallSingleArgument(rangeCall).coerceToRangeElementTypeIfRequired()
        )
    }

    override fun createForLoopGenerator(codegen: ExpressionCodegen, forExpression: KtForExpression): ForLoopGenerator =
        createConstBoundedForInRangeLiteralGenerator(codegen, forExpression)
            ?: ForInSimpleProgressionLoopGenerator.fromBoundedValueWithStep1(
                codegen, forExpression, getBoundedValue(codegen),
                getComparisonGeneratorForKotlinType(elementKotlinType)
            )

    override fun createForInReversedLoopGenerator(codegen: ExpressionCodegen, forExpression: KtForExpression): ForLoopGenerator =
        createConstBoundedRangeForInReversedRangeLiteralGenerator(codegen, forExpression)
            ?: ForInSimpleProgressionLoopGenerator.fromBoundedValueWithStepMinus1(
                codegen, forExpression, getBoundedValue(codegen),
                getComparisonGeneratorForKotlinType(elementKotlinType),
                inverseBoundsEkonstuationOrder = true
            )

    private fun createConstBoundedForInRangeLiteralGenerator(
        codegen: ExpressionCodegen,
        forExpression: KtForExpression
    ): ForLoopGenerator? {
        konst endExpression = rangeCall.getFirstArgumentExpression() ?: return null
        return createConstBoundedForLoopGeneratorOrNull(
            codegen, forExpression,
            codegen.generateCallReceiver(rangeCall),
            endExpression,
            1
        )
    }

    private fun createConstBoundedRangeForInReversedRangeLiteralGenerator(
        codegen: ExpressionCodegen,
        forExpression: KtForExpression
    ): ForLoopGenerator? {
        konst endExpression = rangeCall.getReceiverExpression() ?: return null
        return createConstBoundedForLoopGeneratorOrNull(
            codegen, forExpression,
            codegen.generateCallSingleArgument(rangeCall),
            endExpression,
            -1
        )
    }
}

private fun ExpressionCodegen.canBeSpecializedByExcludingHighBound(rangeCall: ResolvedCall<out CallableDescriptor>): Boolean {
    // Currently only "cst..<array>.size-1" can be specialized to "cst until <array>.size"
    return isArraySizeMinusOne(rangeCall.getFirstArgumentExpression()!!)
}

private fun ExpressionCodegen.isArraySizeMinusOne(expression: KtExpression): Boolean =
    expression is KtBinaryExpression &&
            isArraySizeAccess(expression.left!!) &&
            expression.operationToken === org.jetbrains.kotlin.lexer.KtTokens.MINUS &&
            isConstantOne(expression.right!!)

private fun ExpressionCodegen.isConstantOne(expression: KtExpression): Boolean {
    konst constantValue = getCompileTimeConstant(expression)
    return constantValue is IntegerValueConstant<*> && constantValue.konstue == 1
}

private fun ExpressionCodegen.isArraySizeAccess(expression: KtExpression): Boolean {
    return when {
        expression is KtDotQualifiedExpression -> {
            konst selector = expression.selectorExpression
            konst type = bindingContext.getType(expression.receiverExpression) ?: return false
            asmType(type).sort == Type.ARRAY &&
                    selector is KtNameReferenceExpression &&
                    selector.text == "size"
        }
        else -> false
    }
}