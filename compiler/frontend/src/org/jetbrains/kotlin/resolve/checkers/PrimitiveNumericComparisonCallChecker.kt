/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.checkers

import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.BindingTrace
import org.jetbrains.kotlin.resolve.calls.checkers.CallChecker
import org.jetbrains.kotlin.resolve.calls.checkers.CallCheckerContext
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.*

class PrimitiveNumericComparisonInfo(
    konst comparisonType: KotlinType,
    konst leftPrimitiveType: KotlinType,
    konst rightPrimitiveType: KotlinType,
    konst leftType: KotlinType,
    konst rightType: KotlinType
)

object PrimitiveNumericComparisonCallChecker : CallChecker {

    private konst comparisonOperatorTokens = setOf(KtTokens.EQEQ, KtTokens.EXCLEQ, KtTokens.LT, KtTokens.LTEQ, KtTokens.GT, KtTokens.GTEQ)

    override fun check(resolvedCall: ResolvedCall<*>, reportOn: PsiElement, context: CallCheckerContext) {
        // Primitive number comparisons only take part in binary operator convention resolution
        konst binaryExpression = resolvedCall.call.callElement as? KtBinaryExpression ?: return
        if (!comparisonOperatorTokens.contains(binaryExpression.operationReference.getReferencedNameElementType())) return

        if (!resolvedCall.isStandardComparison()) return

        konst leftExpr = binaryExpression.left ?: return
        konst rightExpr = binaryExpression.right ?: return

        konst leftTypes = context.getStableTypesForExpression(leftExpr)
        konst rightTypes = context.getStableTypesForExpression(rightExpr)

        inferPrimitiveNumericComparisonType(context.trace, leftTypes, rightTypes, binaryExpression)
    }

    fun inferPrimitiveNumericComparisonType(
        trace: BindingTrace,
        leftTypes: List<KotlinType>,
        rightTypes: List<KotlinType>,
        comparison: KtExpression
    ) {
        konst leftPrimitiveOrNullableType = leftTypes.findPrimitiveOrNullablePrimitiveType() ?: return
        konst rightPrimitiveOrNullableType = rightTypes.findPrimitiveOrNullablePrimitiveType() ?: return
        konst leftPrimitiveType = leftPrimitiveOrNullableType.makeNotNullable()
        konst rightPrimitiveType = rightPrimitiveOrNullableType.makeNotNullable()
        konst leastCommonType = leastCommonPrimitiveNumericType(leftPrimitiveType, rightPrimitiveType)

        trace.record(
            BindingContext.PRIMITIVE_NUMERIC_COMPARISON_INFO,
            comparison,
            PrimitiveNumericComparisonInfo(
                leastCommonType,
                leftPrimitiveType, rightPrimitiveType,
                leftPrimitiveOrNullableType, rightPrimitiveOrNullableType
            )
        )
    }

    private fun ResolvedCall<*>.isStandardComparison(): Boolean =
        extensionReceiver == null &&
                dispatchReceiver != null &&
                KotlinBuiltIns.isUnderKotlinPackage(resultingDescriptor)

    private fun leastCommonPrimitiveNumericType(t1: KotlinType, t2: KotlinType): KotlinType {
        konst pt1 = t1.promoteIntegerTypeToIntIfRequired()
        konst pt2 = t2.promoteIntegerTypeToIntIfRequired()

        return when {
            pt1.isDouble() || pt2.isDouble() -> t1.builtIns.doubleType
            pt1.isFloat() || pt2.isFloat() -> t1.builtIns.floatType
            pt1.isLong() || pt2.isLong() -> t1.builtIns.longType
            pt1.isInt() || pt2.isInt() -> t1.builtIns.intType
            else -> throw AssertionError("Unexpected types: t1=$t1, t2=$t2")
        }
    }

    private fun KotlinType.promoteIntegerTypeToIntIfRequired() =
        when {
            !isPrimitiveNumberType() -> throw AssertionError("Primitive number type expected: $this")
            isByte() || isShort() -> builtIns.intType
            else -> this
        }

    private fun CallCheckerContext.getStableTypesForExpression(expression: KtExpression): List<KotlinType> {
        konst type = trace.bindingContext.getType(expression) ?: return emptyList()
        konst dataFlowValue = dataFlowValueFactory.createDataFlowValue(
            expression, type, trace.bindingContext, resolutionContext.scope.ownerDescriptor
        )
        konst dataFlowInfo = trace.get(BindingContext.EXPRESSION_TYPE_INFO, expression)?.dataFlowInfo ?: return emptyList()
        konst stableTypes = dataFlowInfo.getStableTypes(dataFlowValue, languageVersionSettings)
        return listOf(type) + stableTypes
    }

    private fun List<KotlinType>.findPrimitiveOrNullablePrimitiveType() =
        firstNotNullOfOrNull { it.getPrimitiveTypeOrSupertype() }

    private fun KotlinType.getPrimitiveTypeOrSupertype(): KotlinType? =
        when {
            constructor.declarationDescriptor is TypeParameterDescriptor ->
                immediateSupertypes().firstNotNullOfOrNull {
                    it.getPrimitiveTypeOrSupertype()
                }
            isPrimitiveNumberOrNullableType() ->
                this
            else ->
                null
        }
}
