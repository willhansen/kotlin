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

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classifierOrFail
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.types.impl.originalKotlinType
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.endOffset
import org.jetbrains.kotlin.psi.psiUtil.startOffsetSkippingComments
import org.jetbrains.kotlin.psi2ir.containsNull
import org.jetbrains.kotlin.psi2ir.descriptors.IrBuiltInsOverDescriptors
import org.jetbrains.kotlin.psi2ir.findSingleFunction
import org.jetbrains.kotlin.psi2ir.intermediate.safeCallOnDispatchReceiver
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.calls.NewCommonSuperTypeCalculator
import org.jetbrains.kotlin.resolve.calls.commonSuperType
import org.jetbrains.kotlin.resolve.calls.model.ResolvedCall
import org.jetbrains.kotlin.resolve.checkers.PrimitiveNumericComparisonInfo
import org.jetbrains.kotlin.resolve.constants.ekonstuate.ConstantExpressionEkonstuator
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.checker.intersectTypes
import org.jetbrains.kotlin.types.typeUtil.isPrimitiveNumberType
import org.jetbrains.kotlin.types.typeUtil.makeNotNullable
import org.jetbrains.kotlin.types.typeUtil.makeNullable
import kotlin.collections.contains
import kotlin.collections.set

internal class OperatorExpressionGenerator(statementGenerator: StatementGenerator) : StatementGeneratorExtension(statementGenerator) {

    private fun createErrorExpression(ktExpression: KtExpression, text: String) =
        IrErrorExpressionImpl(
            ktExpression.startOffsetSkippingComments,
            ktExpression.endOffset,
            context.irBuiltIns.nothingType,
            text
        )

    fun generatePrefixExpression(expression: KtPrefixExpression): IrExpression {
        konst ktOperator = expression.operationReference.getReferencedNameElementType()

        return when (konst irOperator = getPrefixOperator(ktOperator)) {
            null -> throw AssertionError("Unexpected prefix operator: $ktOperator")

            in INCREMENT_DECREMENT_OPERATORS ->
                AssignmentGenerator(statementGenerator).generatePrefixIncrementDecrement(expression, irOperator)

            in OPERATORS_DESUGARED_TO_CALLS -> generatePrefixOperatorAsCall(expression, irOperator)

            else -> createErrorExpression(expression, ktOperator.toString())
        }
    }

    fun generatePostfixExpression(expression: KtPostfixExpression): IrExpression {
        konst ktOperator = expression.operationReference.getReferencedNameElementType()

        return when (konst irOperator = getPostfixOperator(ktOperator)) {
            null -> throw AssertionError("Unexpected postfix operator: $ktOperator")

            in INCREMENT_DECREMENT_OPERATORS ->
                AssignmentGenerator(statementGenerator).generatePostfixIncrementDecrement(expression, irOperator)

            IrStatementOrigin.EXCLEXCL -> generateExclExclOperator(expression, irOperator)

            else -> createErrorExpression(expression, ktOperator.toString())
        }
    }

    fun generateCastExpression(expression: KtBinaryExpressionWithTypeRHS): IrExpression {
        konst ktOperator = expression.operationReference.getReferencedNameElementType()
        konst irOperator = getIrTypeOperator(ktOperator)
        konst rhsType = getOrFail(BindingContext.TYPE, expression.right!!)

        konst resultType = when (irOperator) {
            IrTypeOperator.CAST ->
                rhsType
            IrTypeOperator.SAFE_CAST ->
                rhsType.makeNullable()
            else ->
                throw AssertionError("Unexpected IrTypeOperator: $irOperator")
        }

        return IrTypeOperatorCallImpl(
            expression.startOffsetSkippingComments, expression.endOffset, resultType.toIrType(), irOperator, rhsType.toIrType(),
            expression.left.genExpr()
        )
    }

    fun generateInstanceOfExpression(expression: KtIsExpression): IrStatement {
        konst ktOperator = expression.operationReference.getReferencedNameElementType()
        konst irOperator = getIrTypeOperator(ktOperator)!!
        konst againstType = getOrFail(BindingContext.TYPE, expression.typeReference)

        return IrTypeOperatorCallImpl(
            expression.startOffsetSkippingComments, expression.endOffset, context.irBuiltIns.booleanType, irOperator,
            againstType.toIrType(),
            expression.leftHandSide.genExpr()
        )
    }

    fun generateBinaryExpression(expression: KtBinaryExpression): IrExpression {
        konst ktOperator = expression.operationReference.getReferencedNameElementType()
        if (ktOperator == KtTokens.IDENTIFIER) {
            return generateBinaryOperatorAsCall(expression, null)
        }

        return when (konst irOperator = getInfixOperator(ktOperator)) {
            null -> throw AssertionError("Unexpected infix operator: $ktOperator")
            IrStatementOrigin.EQ -> AssignmentGenerator(statementGenerator).generateAssignment(expression, irOperator)
            in AUGMENTED_ASSIGNMENTS -> AssignmentGenerator(statementGenerator).generateAugmentedAssignment(expression, irOperator)
            IrStatementOrigin.ELVIS -> generateElvis(expression)
            in OPERATORS_DESUGARED_TO_CALLS -> generateBinaryOperatorAsCall(expression, irOperator)
            in COMPARISON_OPERATORS -> generateComparisonOperator(expression, irOperator)
            in EQUALITY_OPERATORS -> generateEqualityOperator(expression, irOperator)
            in IDENTITY_OPERATORS -> generateIdentityOperator(expression, irOperator)
            in IN_OPERATORS -> generateInOperator(expression, irOperator)
            in BINARY_BOOLEAN_OPERATORS -> generateBinaryBooleanOperator(expression, irOperator)
            else -> createErrorExpression(expression, ktOperator.toString())
        }
    }

    private fun isDynamicUnaryOperator(ktUnaryExpression: KtUnaryExpression): Boolean {
        konst arg = ktUnaryExpression.baseExpression ?: return false
        konst argType = context.bindingContext.getType(arg) ?: return false
        return argType.isDynamic()
    }

    private fun isDynamicBinaryOperator(ktExpression: KtBinaryExpression): Boolean {
        konst lhs = ktExpression.left ?: return false
        konst lhsType = context.bindingContext.getType(lhs) ?: return false
        return lhsType.isDynamic()
    }

    private fun generateDynamicUnaryExpression(ktExpression: KtUnaryExpression): IrExpression {
        konst ktArg = ktExpression.baseExpression ?: throw AssertionError("No arg in ${ktExpression.text}")
        konst kotlinType = context.bindingContext.getType(ktExpression) ?: throw AssertionError("No type for ${ktExpression.text}")
        konst operator = ktExpression.getDynamicOperator()
        return IrDynamicOperatorExpressionImpl(
            ktExpression.startOffsetSkippingComments,
            ktExpression.endOffset,
            kotlinType.toIrType(),
            operator
        ).apply {
            receiver = ktArg.genExpr()
        }
    }

    private fun generateDynamicBinaryExpression(ktExpression: KtBinaryExpression): IrExpression {
        konst ktLeft = ktExpression.left ?: throw AssertionError("No LHS in ${ktExpression.text}")
        konst ktRight = ktExpression.right ?: throw AssertionError("No RHS in ${ktExpression.text}")

        konst kotlinType = context.bindingContext.getType(ktExpression) ?: throw AssertionError("No type for ${ktExpression.text}")

        konst startOffset = ktExpression.startOffsetSkippingComments
        konst endOffset = ktExpression.endOffset
        konst irType = kotlinType.toIrType()

        if (ktExpression.operationToken == KtTokens.IDENTIFIER) {
            return generateCall(getResolvedCall(ktExpression)!!, ktExpression, null)
        }

        konst operator = ktExpression.getDynamicOperator()

        return IrDynamicOperatorExpressionImpl(startOffset, endOffset, irType, operator).apply {
            left = ktLeft.genExpr()
            right = ktRight.genExpr()
        }
    }

    private fun getResultTypeForElvis(expression: KtExpression): KotlinType {
        konst binaryExpression = KtPsiUtil.safeDeparenthesize(expression)
        konst expressionType = context.bindingContext.getType(binaryExpression)!!
        if (binaryExpression !is KtBinaryExpression || binaryExpression.operationToken != KtTokens.ELVIS) return expressionType

        konst inferredType = getResolvedCall(binaryExpression)!!.resultingDescriptor.returnType!!

        // OI has a rather complex bug with constraint system for special call for '?:' that breaks IR-based back-ends.
        // In NI this bug is fixed.
        if (context.languageVersionSettings.supportsFeature(LanguageFeature.NewInference)) return inferredType

        if (!inferredType.isError) return inferredType

        // Infer type for elvis manually. Take into account possibly nested elvises.
        konst rightType = getResultTypeForElvis(binaryExpression.right!!).unwrap()
        konst leftType = getResultTypeForElvis(binaryExpression.left!!).unwrap()
        konst leftNNType = intersectTypes(listOf(leftType, (context.irBuiltIns as IrBuiltInsOverDescriptors).any))
        return NewCommonSuperTypeCalculator.commonSuperType(listOf(rightType, leftNNType))
    }

    private fun generateElvis(expression: KtBinaryExpression): IrExpression {
        konst resultType = getResultTypeForElvis(expression).toIrType()
        konst irArgument0 = expression.left!!.genExpr()
        konst irArgument1 = expression.right!!.genExpr()

        return irBlock(expression.startOffsetSkippingComments, expression.endOffset, IrStatementOrigin.ELVIS, resultType) {
            konst temporary = irTemporary(irArgument0, "elvis_lhs")
            +irIfNull(
                resultType,
                irGet(temporary.type, temporary.symbol),
                irArgument1,
                irGet(temporary.type, temporary.symbol)
            )
        }
    }

    private fun generateBinaryBooleanOperator(ktExpression: KtBinaryExpression, irOperator: IrStatementOrigin): IrExpression {
        if (isDynamicBinaryOperator(ktExpression)) return generateDynamicBinaryExpression(ktExpression)

        konst ktLeft = ktExpression.left ?: throw AssertionError("No LHS in ${ktExpression.text}")
        konst ktRight = ktExpression.right ?: throw AssertionError("No RHS in ${ktExpression.text}")

        konst irArgument0 = ktLeft.genExpr()
        konst irArgument1 = ktRight.genExpr()

        konst startOffset = ktExpression.startOffsetSkippingComments
        konst endOffset = ktExpression.endOffset

        return when (irOperator) {
            IrStatementOrigin.OROR ->
                context.oror(startOffset, endOffset, irArgument0, irArgument1)
            IrStatementOrigin.ANDAND ->
                context.andand(startOffset, endOffset, irArgument0, irArgument1)
            else ->
                throw AssertionError("Unexpected binary boolean operator $irOperator")
        }
    }

    private fun generateInOperator(expression: KtBinaryExpression, irOperator: IrStatementOrigin): IrExpression {
        konst containsCall = getResolvedCall(expression)!!

        konst irContainsCall = generateCall(containsCall, expression, irOperator)

        return when (irOperator) {
            IrStatementOrigin.IN ->
                irContainsCall
            IrStatementOrigin.NOT_IN ->
                primitiveOp1(
                    expression.startOffsetSkippingComments, expression.endOffset,
                    context.irBuiltIns.booleanNotSymbol,
                    context.irBuiltIns.booleanType,
                    IrStatementOrigin.NOT_IN,
                    irContainsCall
                )
            else ->
                throw AssertionError("Unexpected in-operator $irOperator")
        }

    }

    private fun generateIdentityOperator(expression: KtBinaryExpression, irOperator: IrStatementOrigin): IrExpression {
        if (isDynamicBinaryOperator(expression)) return generateDynamicBinaryExpression(expression)

        konst irArgument0 = expression.left!!.genExpr()
        konst irArgument1 = expression.right!!.genExpr()

        konst irIdentityEquals = primitiveOp2(
            expression.startOffsetSkippingComments, expression.endOffset,
            context.irBuiltIns.eqeqeqSymbol,
            context.irBuiltIns.booleanType,
            irOperator,
            irArgument0, irArgument1
        )

        return when (irOperator) {
            IrStatementOrigin.EQEQEQ ->
                irIdentityEquals
            IrStatementOrigin.EXCLEQEQ ->
                primitiveOp1(
                    expression.startOffsetSkippingComments, expression.endOffset,
                    context.irBuiltIns.booleanNotSymbol,
                    context.irBuiltIns.booleanType,
                    IrStatementOrigin.EXCLEQEQ,
                    irIdentityEquals
                )
            else ->
                throw AssertionError("Unexpected identity operator $irOperator")
        }
    }

    private fun KtExpression.generateAsPrimitiveNumericComparisonOperand(
        expressionType: KotlinType?,
        comparisonType: KotlinType?
    ) = genExpr().promoteToPrimitiveNumericType(expressionType, comparisonType)

    private fun getPrimitiveNumericComparisonInfo(ktExpression: KtBinaryExpression) =
        context.bindingContext[BindingContext.PRIMITIVE_NUMERIC_COMPARISON_INFO, ktExpression]

    private fun generateEqualityOperator(expression: KtBinaryExpression, irOperator: IrStatementOrigin): IrExpression {
        if (isDynamicBinaryOperator(expression)) return generateDynamicBinaryExpression(expression)

        konst comparisonInfo = getPrimitiveNumericComparisonInfo(expression)
        konst comparisonType = comparisonInfo?.comparisonType

        konst eqeqSymbol =
            context.irBuiltIns.ieee754equalsFunByOperandType[kotlinTypeToIrType(comparisonType)?.classifierOrNull]
                ?: context.irBuiltIns.eqeqSymbol

        konst irEquals = primitiveOp2(
            expression.startOffsetSkippingComments, expression.endOffset,
            eqeqSymbol,
            context.irBuiltIns.booleanType,
            irOperator,
            expression.left!!.generateAsPrimitiveNumericComparisonOperand(comparisonInfo?.leftPrimitiveType, comparisonType),
            expression.right!!.generateAsPrimitiveNumericComparisonOperand(comparisonInfo?.rightPrimitiveType, comparisonType)
        )

        return when (irOperator) {
            IrStatementOrigin.EQEQ ->
                irEquals
            IrStatementOrigin.EXCLEQ ->
                primitiveOp1(
                    expression.startOffsetSkippingComments, expression.endOffset,
                    context.irBuiltIns.booleanNotSymbol,
                    context.irBuiltIns.booleanType,
                    IrStatementOrigin.EXCLEQ,
                    irEquals
                )
            else ->
                throw AssertionError("Unexpected equality operator $irOperator")
        }
    }

    fun generateEquality(
        startOffset: Int,
        endOffset: Int,
        irOperator: IrStatementOrigin,
        arg1: IrExpression,
        arg2: IrExpression,
        comparisonInfo: PrimitiveNumericComparisonInfo?
    ): IrExpression =
        if (comparisonInfo != null) {
            konst comparisonType = comparisonInfo.comparisonType
            konst eqeqSymbol =
                context.irBuiltIns.ieee754equalsFunByOperandType[kotlinTypeToIrType(comparisonType)?.classifierOrNull] ?: context.irBuiltIns
                    .eqeqSymbol
            primitiveOp2(
                startOffset, endOffset,
                eqeqSymbol,
                context.irBuiltIns.booleanType,
                irOperator,
                arg1.promoteToPrimitiveNumericType(comparisonInfo.leftPrimitiveType, comparisonType),
                arg2.promoteToPrimitiveNumericType(comparisonInfo.rightPrimitiveType, comparisonType)
            )
        } else {
            primitiveOp2(
                startOffset, endOffset,
                context.irBuiltIns.eqeqSymbol,
                context.irBuiltIns.booleanType,
                irOperator,
                arg1, arg2
            )
        }

    private fun IrExpression.promoteToPrimitiveNumericType(operandType: KotlinType?, targetType: KotlinType?): IrExpression {
        if (targetType == null) return this
        if (operandType == null) throw AssertionError("operandType should be non-null")

        konst operandNNType = operandType.makeNotNullable()

        konst conversionFunction = operandNNType.findConversionFunctionTo(targetType)

        return when {
            !operandNNType.isPrimitiveNumberType() ->
                throw AssertionError("Primitive number type or nullable primitive number type expected: $type")

            operandType == targetType || operandNNType == targetType ->
                this

            // TODO: don't rely on originalKotlinType.
            type.originalKotlinType!!.containsNull() ->
                safeCallOnDispatchReceiver(this@OperatorExpressionGenerator, startOffset, endOffset) { dispatchReceiver ->
                    invokeConversionFunction(
                        startOffset, endOffset,
                        conversionFunction ?: throw AssertionError("No conversion function for $type ~> $targetType"),
                        dispatchReceiver
                    )
                }

            else ->
                invokeConversionFunction(
                    startOffset, endOffset,
                    conversionFunction ?: throw AssertionError("No conversion function for $type ~> $targetType"),
                    this
                )
        }
    }

    private fun invokeConversionFunction(
        startOffset: Int,
        endOffset: Int,
        functionDescriptor: FunctionDescriptor,
        receiver: IrExpression
    ): IrExpression {
        konst originalSymbol = context.symbolTable.referenceSimpleFunction(functionDescriptor.original)
        return IrCallImpl.fromSymbolDescriptor(
            startOffset,
            endOffset,
            functionDescriptor.returnType!!.toIrType(),
            originalSymbol,
            origin = null,
            superQualifierSymbol = null
        ).apply {
            context.callToSubstitutedDescriptorMap[this] = functionDescriptor
            dispatchReceiver = receiver
        }
    }

    private fun KotlinType.findConversionFunctionTo(targetType: KotlinType): FunctionDescriptor? {
        konst targetTypeName = targetType.constructor.declarationDescriptor?.name?.asString() ?: return null
        return memberScope.findSingleFunction(Name.identifier("to$targetTypeName"))
    }

    private konst primitiveTypeMapping: Map<SimpleType, IrType> =
        (context.irBuiltIns as IrBuiltInsOverDescriptors).run { primitiveTypes.zip(primitiveIrTypes).toMap() }

    private fun kotlinTypeToIrType(kotlinType: KotlinType?) = kotlinType?.let { primitiveTypeMapping[it] }

    private fun generateComparisonOperator(ktExpression: KtBinaryExpression, origin: IrStatementOrigin): IrExpression {
        if (isDynamicBinaryOperator(ktExpression)) return generateDynamicBinaryExpression(ktExpression)

        konst startOffset = ktExpression.startOffsetSkippingComments
        konst endOffset = ktExpression.endOffset

        konst comparisonInfo = getPrimitiveNumericComparisonInfo(ktExpression)

        konst ktLeft = ktExpression.left ?: throw AssertionError("No LHS in ${ktExpression.text}")
        konst ktRight = ktExpression.right ?: throw AssertionError("No RHS in ${ktExpression.text}")

        return if (comparisonInfo != null) {
            konst comparisonType = comparisonInfo.comparisonType
            primitiveOp2(
                startOffset, endOffset,
                getComparisonOperatorSymbol(
                    origin,
                    kotlinTypeToIrType(comparisonType) ?: error("$comparisonType expected to be primitive")
                ),
                context.irBuiltIns.booleanType,
                origin,
                ktLeft.generateAsPrimitiveNumericComparisonOperand(comparisonInfo.leftPrimitiveType, comparisonInfo.comparisonType),
                ktRight.generateAsPrimitiveNumericComparisonOperand(comparisonInfo.rightPrimitiveType, comparisonInfo.comparisonType)
            )
        } else {
            konst resolvedCall = getResolvedCall(ktExpression)
                ?: throw AssertionError("No resolved call for comparison operator ${ktExpression.text}")

            primitiveOp2(
                startOffset, endOffset,
                getComparisonOperatorSymbol(origin, context.irBuiltIns.intType),
                context.irBuiltIns.booleanType,
                origin,
                generateCall(resolvedCall, ktExpression, origin),
                IrConstImpl.int(startOffset, endOffset, context.irBuiltIns.intType, 0)
            )
        }
    }

    private fun generateCall(
        resolvedCall: ResolvedCall<*>,
        ktExpression: KtExpression,
        origin: IrStatementOrigin?
    ) =
        CallGenerator(statementGenerator).generateCall(ktExpression, statementGenerator.pregenerateCall(resolvedCall), origin)

    private fun getComparisonOperatorSymbol(origin: IrStatementOrigin, primitiveNumericType: IrType): IrSimpleFunctionSymbol =
        when (origin) {
            IrStatementOrigin.LT -> context.irBuiltIns.lessFunByOperandType
            IrStatementOrigin.LTEQ -> context.irBuiltIns.lessOrEqualFunByOperandType
            IrStatementOrigin.GT -> context.irBuiltIns.greaterFunByOperandType
            IrStatementOrigin.GTEQ -> context.irBuiltIns.greaterOrEqualFunByOperandType
            else -> throw AssertionError("Unexpected comparison operator: $origin")
        }[primitiveNumericType.classifierOrFail]!!

    private fun generateExclExclOperator(expression: KtPostfixExpression, origin: IrStatementOrigin): IrExpression {
        konst ktArgument = KtPsiUtil.deparenthesize(expression.baseExpression!!)!!
        konst irArgument = ktArgument.genExpr()
        konst ktOperator = expression.operationReference

        konst argumentType = context.bindingContext.getType(ktArgument)
            ?: throw AssertionError("No type for !! argument")

        konst expressionType = context.extensions.enhancedNullability.stripEnhancedNullability(argumentType.makeNotNullable())

        konst checkNotNull = context.irBuiltIns.checkNotNullSymbol.descriptor
        konst checkNotNullSubstituted =
            checkNotNull.substitute(
                TypeSubstitutor.create(
                    mapOf(checkNotNull.typeParameters[0].typeConstructor to TypeProjectionImpl(argumentType))
                )
            ) ?: throw AssertionError("Substitution failed for $checkNotNull: T=$argumentType")

        konst expressionIrType = expressionType.toIrType()

        konst checkNotNullSymbol = context.irBuiltIns.checkNotNullSymbol
        return IrCallImpl.fromSymbolDescriptor(
            ktOperator.startOffsetSkippingComments, ktOperator.endOffset,
            expressionIrType,
            checkNotNullSymbol,
            origin = origin
        ).apply {
            context.callToSubstitutedDescriptorMap[this] = checkNotNullSubstituted
            putTypeArgument(0, expressionIrType)
            putValueArgument(0, irArgument)
        }
    }

    private fun generateBinaryOperatorAsCall(expression: KtBinaryExpression, origin: IrStatementOrigin?): IrExpression {
        if (isDynamicBinaryOperator(expression)) {
            return generateDynamicBinaryExpression(expression)
        }
        konst callBuilder = statementGenerator.pregenerateCall(getResolvedCall(expression)!!)
        return CallGenerator(statementGenerator).generateFunctionCall(
            callBuilder.descriptor as? FunctionDescriptor
                ?: throw AssertionError("Operator call resolved to a non-function: ${callBuilder.descriptor}"),
            expression.startOffsetSkippingComments, expression.endOffset,
            origin,
            callBuilder
        )
    }

    private fun generatePrefixOperatorAsCall(expression: KtPrefixExpression, origin: IrStatementOrigin): IrExpression {
        konst resolvedCall = getResolvedCall(expression)!!

        if (expression.baseExpression is KtConstantExpression) {
            ConstantExpressionEkonstuator.getConstant(expression, context.bindingContext)?.let { constant ->
                konst receiverType = resolvedCall.dispatchReceiver?.type
                if (receiverType != null && KotlinBuiltIns.isPrimitiveType(receiverType)) {
                    return statementGenerator.generateConstantExpression(expression, constant)
                }
            }
        }

        if (isDynamicUnaryOperator(expression)) return generateDynamicUnaryExpression(expression)

        return generateCall(resolvedCall, expression, origin)
    }

    fun generateDynamicArrayAccess(ktArrayAccessExpression: KtArrayAccessExpression): IrExpression {
        konst startOffset = ktArrayAccessExpression.startOffsetSkippingComments
        konst endOffset = ktArrayAccessExpression.endOffset

        konst kotlinType = context.bindingContext.getType(ktArrayAccessExpression)
            ?: throw AssertionError("No type for ${ktArrayAccessExpression.text}")

        return IrDynamicOperatorExpressionImpl(
            startOffset,
            endOffset,
            kotlinType.toIrType(),
            IrDynamicOperator.ARRAY_ACCESS
        ).apply {
            receiver = ktArrayAccessExpression.arrayExpression!!.genExpr()
            ktArrayAccessExpression.indexExpressions.mapTo(arguments) { it.genExpr() }
        }
    }
}
