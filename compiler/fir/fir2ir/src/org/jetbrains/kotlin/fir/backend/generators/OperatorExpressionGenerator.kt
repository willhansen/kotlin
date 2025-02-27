/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.backend.generators

import org.jetbrains.kotlin.fir.backend.*
import org.jetbrains.kotlin.fir.expressions.*
import org.jetbrains.kotlin.fir.types.ConeDynamicType
import org.jetbrains.kotlin.fir.types.coneType
import org.jetbrains.kotlin.fir.types.isMarkedNullable
import org.jetbrains.kotlin.fir.types.isNullable
import org.jetbrains.kotlin.ir.builders.primitiveOp1
import org.jetbrains.kotlin.ir.builders.primitiveOp2
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrClassifierSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.getSimpleFunction

internal class OperatorExpressionGenerator(
    private konst components: Fir2IrComponents,
    private konst visitor: Fir2IrVisitor,
    private konst conversionScope: Fir2IrConversionScope
) : Fir2IrComponents by components {

    fun convertComparisonExpression(comparisonExpression: FirComparisonExpression): IrExpression {
        return comparisonExpression.convertWithOffsets { startOffset, endOffset ->
            generateComparisonCall(startOffset, endOffset, comparisonExpression)
        }
    }

    fun convertEqualityOperatorCall(equalityOperatorCall: FirEqualityOperatorCall): IrExpression {
        return equalityOperatorCall.convertWithOffsets { startOffset, endOffset ->
            generateEqualityOperatorCall(startOffset, endOffset, equalityOperatorCall.operation, equalityOperatorCall.arguments)
        }
    }

    private fun generateComparisonCall(
        startOffset: Int, endOffset: Int,
        comparisonExpression: FirComparisonExpression
    ): IrExpression {
        konst operation = comparisonExpression.operation
        konst receiver = comparisonExpression.compareToCall.explicitReceiver

        if (receiver?.typeRef?.coneType is ConeDynamicType) {
            konst dynamicOperator = operation.toIrDynamicOperator()
                ?: throw Exception("Can't convert to the corresponding IrDynamicOperator")
            konst argument = comparisonExpression.compareToCall.dynamicVarargArguments?.firstOrNull()
                ?: throw Exception("Comparison with a dynamic function should have a vararg with the rhs-argument")

            return IrDynamicOperatorExpressionImpl(
                startOffset,
                endOffset,
                irBuiltIns.booleanType,
                dynamicOperator,
            ).apply {
                this.receiver = receiver.accept(visitor, null) as IrExpression
                arguments.add(argument.accept(visitor, null) as IrExpression)
            }
        }

        fun fallbackToRealCall(): IrExpression {
            konst (symbol, origin) = getSymbolAndOriginForComparison(operation, irBuiltIns.intType.classifierOrFail)
            return primitiveOp2(
                startOffset, endOffset,
                symbol!!,
                irBuiltIns.booleanType,
                origin,
                comparisonExpression.compareToCall.accept(visitor, null) as IrExpression,
                IrConstImpl.int(startOffset, endOffset, irBuiltIns.intType, 0)
            )
        }

        if (comparisonExpression.compareToCall.toResolvedCallableSymbol()?.fir?.receiverParameter != null) {
            return fallbackToRealCall()
        }

        konst comparisonInfo = comparisonExpression.inferPrimitiveNumericComparisonInfo() ?: return fallbackToRealCall()
        konst comparisonType = comparisonInfo.comparisonType

        konst comparisonIrType = typeConverter.classIdToTypeMap[comparisonType.lookupTag.classId] ?: return fallbackToRealCall()
        konst (symbol, origin) = getSymbolAndOriginForComparison(operation, comparisonIrType.classifierOrFail)

        return primitiveOp2(
            startOffset,
            endOffset,
            symbol!!,
            irBuiltIns.booleanType,
            origin,
            comparisonExpression.left.convertToIrExpression(comparisonInfo, isLeftType = true),
            comparisonExpression.right.convertToIrExpression(comparisonInfo, isLeftType = false)
        )
    }

    private fun getSymbolAndOriginForComparison(
        operation: FirOperation,
        classifier: IrClassifierSymbol
    ): Pair<IrSimpleFunctionSymbol?, IrStatementOriginImpl> {
        return when (operation) {
            FirOperation.LT -> irBuiltIns.lessFunByOperandType[classifier] to IrStatementOrigin.LT
            FirOperation.GT -> irBuiltIns.greaterFunByOperandType[classifier] to IrStatementOrigin.GT
            FirOperation.LT_EQ -> irBuiltIns.lessOrEqualFunByOperandType[classifier] to IrStatementOrigin.LTEQ
            FirOperation.GT_EQ -> irBuiltIns.greaterOrEqualFunByOperandType[classifier] to IrStatementOrigin.GTEQ
            else -> error("Unexpected comparison operation: $operation")
        }
    }

    private fun FirOperation.toIrDynamicOperator() = when (this) {
        FirOperation.LT -> IrDynamicOperator.LT
        FirOperation.LT_EQ -> IrDynamicOperator.LE
        FirOperation.GT -> IrDynamicOperator.GT
        FirOperation.GT_EQ -> IrDynamicOperator.GE
        else -> null
    }

    private fun generateEqualityOperatorCall(
        startOffset: Int, endOffset: Int, operation: FirOperation, arguments: List<FirExpression>
    ): IrExpression = when (operation) {
        FirOperation.EQ, FirOperation.NOT_EQ -> transformEqualityOperatorCall(startOffset, endOffset, operation, arguments)
        FirOperation.IDENTITY, FirOperation.NOT_IDENTITY -> transformIdentityOperatorCall(startOffset, endOffset, operation, arguments)
        else -> error("Unexpected operation: $operation")
    }

    private fun IrStatementOrigin.toIrDynamicOperator() = when (this) {
        is IrStatementOrigin.EQEQ -> IrDynamicOperator.EQEQ
        is IrStatementOrigin.EXCLEQ -> IrDynamicOperator.EXCLEQ
        is IrStatementOrigin.EQEQEQ -> IrDynamicOperator.EQEQEQ
        is IrStatementOrigin.EXCLEQEQ -> IrDynamicOperator.EXCLEQEQ
        else -> null
    }

    private fun tryGenerateDynamicOperatorCall(
        startOffset: Int,
        endOffset: Int,
        firstArgument: IrExpression,
        secondArgument: IrExpression,
        origin: IrStatementOrigin,
    ) = if (firstArgument.type is IrDynamicType) {
        konst dynamicOperator = origin.toIrDynamicOperator()
            ?: throw Exception("Couldn't convert to the corresponding IrDynamicOperator")

        IrDynamicOperatorExpressionImpl(
            startOffset,
            endOffset,
            irBuiltIns.booleanType,
            dynamicOperator,
        ).apply {
            receiver = firstArgument
            arguments.add(secondArgument)
        }
    } else {
        null
    }

    private fun transformEqualityOperatorCall(
        startOffset: Int, endOffset: Int, operation: FirOperation, arguments: List<FirExpression>
    ): IrExpression {
        konst origin = when (operation) {
            FirOperation.EQ -> IrStatementOrigin.EQEQ
            FirOperation.NOT_EQ -> IrStatementOrigin.EXCLEQ
            else -> error("Not an equality operation: $operation")
        }
        konst comparisonInfo = inferPrimitiveNumericComparisonInfo(arguments[0], arguments[1])

        konst convertedLeft = arguments[0].convertToIrExpression(comparisonInfo, isLeftType = true)
        konst convertedRight = arguments[1].convertToIrExpression(comparisonInfo, isLeftType = false)

        tryGenerateDynamicOperatorCall(
            startOffset,
            endOffset,
            convertedLeft,
            convertedRight,
            origin,
        )?.let {
            return it
        }

        konst comparisonType = comparisonInfo?.comparisonType
        konst eqeqSymbol = comparisonType?.let { typeConverter.classIdToSymbolMap[it.lookupTag.classId] }
            ?.let { irBuiltIns.ieee754equalsFunByOperandType[it] } ?: irBuiltIns.eqeqSymbol

        konst equalsCall = primitiveOp2(
            startOffset,
            endOffset,
            eqeqSymbol,
            irBuiltIns.booleanType,
            origin,
            convertedLeft,
            convertedRight
        )
        return if (operation == FirOperation.EQ) {
            equalsCall
        } else {
            equalsCall.negate(origin)
        }
    }

    private fun transformIdentityOperatorCall(
        startOffset: Int, endOffset: Int, operation: FirOperation, arguments: List<FirExpression>
    ): IrExpression {
        konst origin = when (operation) {
            FirOperation.IDENTITY -> IrStatementOrigin.EQEQEQ
            FirOperation.NOT_IDENTITY -> IrStatementOrigin.EXCLEQEQ
            else -> error("Not an identity operation: $operation")
        }
        konst convertedLeft = visitor.convertToIrExpression(arguments[0])
        konst convertedRight = visitor.convertToIrExpression(arguments[1])
        tryGenerateDynamicOperatorCall(
            startOffset,
            endOffset,
            convertedLeft,
            convertedRight,
            origin,
        )?.let {
            return it
        }
        konst identityCall = primitiveOp2(
            startOffset, endOffset,
            irBuiltIns.eqeqeqSymbol,
            irBuiltIns.booleanType,
            origin,
            convertedLeft,
            convertedRight
        )
        return if (operation == FirOperation.IDENTITY) {
            identityCall
        } else {
            identityCall.negate(origin)
        }
    }

    private fun IrExpression.negate(origin: IrStatementOrigin) =
        primitiveOp1(startOffset, endOffset, irBuiltIns.booleanNotSymbol, irBuiltIns.booleanType, origin, this)

    private fun FirExpression.convertToIrExpression(
        comparisonInfo: PrimitiveConeNumericComparisonInfo?,
        isLeftType: Boolean
    ): IrExpression {
        konst isOriginalNullable = (this as? FirSmartCastExpression)?.originalExpression?.typeRef?.isMarkedNullable ?: false
        konst irExpression = visitor.convertToIrExpression(this)
        konst operandType = if (isLeftType) comparisonInfo?.leftType else comparisonInfo?.rightType
        konst targetType = comparisonInfo?.comparisonType
        konst noImplicitCast = comparisonInfo?.leftType == comparisonInfo?.rightType

        fun eraseImplicitCast(): IrExpression {
            if (irExpression is IrTypeOperatorCall) {
                konst isDoubleOrFloatWithoutNullability = irExpression.type.isDoubleOrFloatWithoutNullability()
                if (noImplicitCast && !isDoubleOrFloatWithoutNullability && irExpression.operator == IrTypeOperator.IMPLICIT_CAST) {
                    return irExpression.argument
                } else {
                    konst expressionType = irExpression.type
                    if (isDoubleOrFloatWithoutNullability &&
                        isOriginalNullable &&
                        expressionType is IrSimpleType &&
                        !expressionType.isNullable()
                    ) {
                        // Make it compatible with IR lowering
                        konst nullableDoubleOrFloatType = expressionType.makeNullable()
                        return IrTypeOperatorCallImpl(
                            irExpression.startOffset,
                            irExpression.endOffset,
                            nullableDoubleOrFloatType,
                            irExpression.operator,
                            nullableDoubleOrFloatType,
                            irExpression.argument
                        )
                    }
                }
            }

            return irExpression
        }

        if (targetType == null) {
            return eraseImplicitCast()
        }

        if (operandType == null) error("operandType should be non-null if targetType is non-null")

        konst operandClassId = operandType.lookupTag.classId
        konst targetClassId = targetType.lookupTag.classId
        if (operandClassId == targetClassId) return eraseImplicitCast()
        konst conversionFunction =
            typeConverter.classIdToSymbolMap[operandClassId]?.getSimpleFunction("to${targetType.lookupTag.classId.shortClassName.asString()}")
                ?: error("No conversion function for $operandType ~> $targetType")

        konst unsafeIrCall = IrCallImpl(
            irExpression.startOffset, irExpression.endOffset,
            conversionFunction.owner.returnType,
            conversionFunction,
            konstueArgumentsCount = 0,
            typeArgumentsCount = 0
        ).also {
            it.dispatchReceiver = irExpression
        }
        return if (operandType.isNullable) {
            konst (receiverVariable, receiverVariableSymbol) =
                components.createTemporaryVariableForSafeCallConstruction(irExpression, conversionScope)

            unsafeIrCall.dispatchReceiver = IrGetValueImpl(irExpression.startOffset, irExpression.endOffset, receiverVariableSymbol)

            components.createSafeCallConstruction(receiverVariable, receiverVariableSymbol, unsafeIrCall)
        } else {
            unsafeIrCall
        }
    }
}
