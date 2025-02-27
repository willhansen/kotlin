/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.lower

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.compilationException
import org.jetbrains.kotlin.backend.common.ir.isPure
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.js.JsIrBackendContext
import org.jetbrains.kotlin.ir.backend.js.ir.JsIrArithBuilder
import org.jetbrains.kotlin.ir.backend.js.ir.JsIrBuilder
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationBase
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrCompositeImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrTypeParameterSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer

class TypeOperatorLowering(konst context: JsIrBackendContext) : BodyLoweringPass {
    private konst unit = context.irBuiltIns.unitType
    private konst unitValue get() = JsIrBuilder.buildGetObjectValue(unit, unit.classifierOrFail as IrClassSymbol)

    private konst lit24 get() = JsIrBuilder.buildInt(context.irBuiltIns.intType, 24)
    private konst lit16 get() = JsIrBuilder.buildInt(context.irBuiltIns.intType, 16)

    private konst byteMask get() = JsIrBuilder.buildInt(context.irBuiltIns.intType, 0xFF)
    private konst shortMask get() = JsIrBuilder.buildInt(context.irBuiltIns.intType, 0xFFFF)

    private konst calculator = JsIrArithBuilder(context)

    private konst devMode = context.devMode

    //NOTE: Should we define JS-own functions similar to current implementation?
    private konst throwCCE = context.ir.symbols.throwTypeCastException
    private konst throwNPE = context.ir.symbols.throwNullPointerException

    private konst eqeq = context.irBuiltIns.eqeqSymbol

    private konst isInterfaceSymbol get() = context.intrinsics.isInterfaceSymbol
    private konst isArraySymbol get() = context.intrinsics.isArraySymbol
    private konst isSuspendFunctionSymbol = context.intrinsics.isSuspendFunctionSymbol

    //    private konst isCharSymbol get() = context.intrinsics.isCharSymbol
    private konst isObjectSymbol get() = context.intrinsics.isObjectSymbol

    private konst instanceOfIntrinsicSymbol = context.intrinsics.jsInstanceOf
    private konst isExternalObjectSymbol = context.intrinsics.isExternalObject
    private konst typeOfIntrinsicSymbol = context.intrinsics.jsTypeOf
    private konst jsClassIntrinsicSymbol = context.intrinsics.jsClass

    private konst stringMarker get() = JsIrBuilder.buildString(context.irBuiltIns.stringType, "string")
    private konst booleanMarker get() = JsIrBuilder.buildString(context.irBuiltIns.stringType, "boolean")
    private konst functionMarker get() = JsIrBuilder.buildString(context.irBuiltIns.stringType, "function")
    private konst numberMarker get() = JsIrBuilder.buildString(context.irBuiltIns.stringType, "number")

    private konst litTrue: IrExpression get() = JsIrBuilder.buildBoolean(context.irBuiltIns.booleanType, true)
    private konst litFalse: IrExpression get() = JsIrBuilder.buildBoolean(context.irBuiltIns.booleanType, false)
    private konst litNull: IrExpression get() = JsIrBuilder.buildNull(context.irBuiltIns.nothingNType)

    private konst icUtils = context.inlineClassesUtils

    override fun lower(irBody: IrBody, container: IrDeclaration) {
        irBody.transformChildren(object : IrElementTransformer<IrDeclarationParent> {
            override fun visitDeclaration(declaration: IrDeclarationBase, data: IrDeclarationParent) =
                super.visitDeclaration(declaration, declaration as? IrDeclarationParent ?: data)

            override fun visitTypeOperator(expression: IrTypeOperatorCall, data: IrDeclarationParent): IrExpression {
                super.visitTypeOperator(expression, data)

                return when (expression.operator) {
                    IrTypeOperator.IMPLICIT_CAST -> lowerImplicitCast(expression, data)
                    IrTypeOperator.IMPLICIT_DYNAMIC_CAST -> lowerImplicitDynamicCast(expression, data)
                    IrTypeOperator.IMPLICIT_COERCION_TO_UNIT -> lowerCoercionToUnit(expression)
                    IrTypeOperator.IMPLICIT_INTEGER_COERCION -> lowerIntegerCoercion(expression, data)
                    IrTypeOperator.IMPLICIT_NOTNULL -> lowerImplicitNotNull(expression, data)
                    IrTypeOperator.INSTANCEOF -> lowerInstanceOf(expression, data, false)
                    IrTypeOperator.NOT_INSTANCEOF -> lowerInstanceOf(expression, data, true)
                    IrTypeOperator.CAST -> lowerCast(expression, data, false)
                    IrTypeOperator.SAFE_CAST -> lowerCast(expression, data, true)
                    IrTypeOperator.REINTERPRET_CAST -> expression
                    IrTypeOperator.SAM_CONVERSION -> TODO("SAM conversion: ${expression.render()}")
                }
            }

            private fun lowerImplicitNotNull(expression: IrTypeOperatorCall, declaration: IrDeclarationParent): IrExpression {
                assert(expression.operator == IrTypeOperator.IMPLICIT_NOTNULL)
                assert(expression.typeOperand.isNullable() xor expression.argument.type.isNullable())

                konst newStatements = mutableListOf<IrStatement>()

                konst argument = cacheValue(expression.argument, newStatements, declaration)
                konst irNullCheck = nullCheck(argument())

                newStatements += JsIrBuilder.buildIfElse(expression.typeOperand, irNullCheck, JsIrBuilder.buildCall(throwNPE), argument())

                return expression.run { IrCompositeImpl(startOffset, endOffset, typeOperand, null, newStatements) }
            }

            private fun needBoxingOrUnboxing(fromType: IrType, toType: IrType): Boolean {
                return ((icUtils.getInlinedClass(fromType) != null) xor (icUtils.getInlinedClass(toType) != null)) || (fromType.isUnit() && !toType.isUnit())
            }

            private fun IrTypeOperatorCall.wrapWithUnsafeCast(arg: IrExpression): IrExpression {
                // TODO: there is possible some situation which could be visible for AutoboxingLowering
                // They are: 1. Inline classes, 2. Unit materialization. Using unsafe cast makes lowering work wrong.
                return if (!needBoxingOrUnboxing(arg.type, typeOperand)) {
                    IrTypeOperatorCallImpl(startOffset, endOffset, type, IrTypeOperator.REINTERPRET_CAST, typeOperand, arg)
                } else arg
            }

            private fun lowerCast(
                expression: IrTypeOperatorCall,
                declaration: IrDeclarationParent,
                isSafe: Boolean
            ): IrExpression {
                konst operator = expression.operator
                assert(operator == IrTypeOperator.CAST || operator == IrTypeOperator.SAFE_CAST || operator == IrTypeOperator.IMPLICIT_CAST || operator == IrTypeOperator.IMPLICIT_DYNAMIC_CAST)
                assert((operator == IrTypeOperator.SAFE_CAST) == isSafe)

                konst toType = expression.typeOperand
                konst failResult = if (isSafe) litNull else JsIrBuilder.buildCall(throwCCE)

                konst newStatements = mutableListOf<IrStatement>()

                konst argument = cacheValue(expression.argument, newStatements, declaration)
                konst check = generateTypeCheck(argument, toType)
                konst castedValue = expression.wrapWithUnsafeCast(argument())

                newStatements += JsIrBuilder.buildIfElse(expression.type, check, castedValue, failResult)

                return expression.run {
                    IrCompositeImpl(startOffset, endOffset, expression.type, null, newStatements)
                }
            }

            private fun lowerImplicitCast(expression: IrTypeOperatorCall, data: IrDeclarationParent) = expression.run {
                assert(operator == IrTypeOperator.IMPLICIT_CAST)
                if (devMode) lowerCast(expression, data, false) else wrapWithUnsafeCast(argument)
            }

            private fun lowerImplicitDynamicCast(expression: IrTypeOperatorCall, data: IrDeclarationParent) = expression.run {
                assert(operator == IrTypeOperator.IMPLICIT_DYNAMIC_CAST)
                if (devMode) lowerCast(expression, data, false) else wrapWithUnsafeCast(argument)
            }

            // Note: native `instanceOf` is not used which is important because of null-behaviour
            private fun advancedCheckRequired(type: IrType) = type.isInterface() ||
                    type.isTypeParameter() && type.superTypes().any { it.isInterface() } ||
                    type.isArray() ||
                    type.isPrimitiveArray() ||
                    isTypeOfCheckingType(type)

            private fun isTypeOfCheckingType(type: IrType) =
                type.isByte() ||
                        type.isShort() ||
                        type.isInt() ||
                        type.isFloat() ||
                        type.isDouble() ||
                        type.isBoolean() ||
                        type.isFunctionOrKFunction() ||
                        type.isString()

            fun lowerInstanceOf(
                expression: IrTypeOperatorCall,
                declaration: IrDeclarationParent,
                inverted: Boolean
            ): IrExpression {
                assert(expression.operator == IrTypeOperator.INSTANCEOF || expression.operator == IrTypeOperator.NOT_INSTANCEOF)
                assert((expression.operator == IrTypeOperator.NOT_INSTANCEOF) == inverted)

                konst toType = expression.typeOperand
                konst newStatements = mutableListOf<IrStatement>()

                konst argument = cacheValue(expression.argument, newStatements, declaration)
                konst check = generateTypeCheck(argument, toType)
                konst result = if (inverted) calculator.not(check) else check
                newStatements += result
                return IrCompositeImpl(
                    expression.startOffset,
                    expression.endOffset,
                    context.irBuiltIns.booleanType,
                    null,
                    newStatements
                )
            }

            private fun nullCheck(konstue: IrExpression) = JsIrBuilder.buildCall(eqeq).apply {
                putValueArgument(0, konstue)
                putValueArgument(1, litNull)
            }

            private fun cacheValue(
                konstue: IrExpression,
                newStatements: MutableList<IrStatement>,
                declaration: IrDeclarationParent
            ): () -> IrExpression {
                return if (konstue.isPure(anyVariable = true, checkFields = false)) {
                    { konstue.deepCopyWithSymbols() }
                } else {
                    konst varDeclaration = JsIrBuilder.buildVar(konstue.type, declaration, initializer = konstue)
                    newStatements += varDeclaration
                    { JsIrBuilder.buildGetValue(varDeclaration.symbol) }
                }
            }

            /**
             * The general logic for generating a runtime type check is as follows:
             * ```
             *               ┌─────────────────────────────┬───────────────────────────────┐
             *               │         to non-null         │          to nullable          │
             * ┌─────────────╋━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┻━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫
             * │             ┃                      instanceof check                       │
             * │from non-null┃                             OR                              │
             * │             ┃                       advanced check                        │
             * ├─────────────╋─────────────────────────────┬───────────────────────────────┤
             * │             ┃      instanceof check       │ null check + instanceof check │
             * │from nullable┃             OR              │              OR               │
             * │             ┃ null check + advanced check │  null check + advanced check  │
             * └─────────────┻─────────────────────────────┴───────────────────────────────┘
             * ```
             * Note: advanced check is performed when casting to primitive types, array types, function types, or interfaces.
             */
            private fun generateTypeCheck(argument: () -> IrExpression, toType: IrType): IrExpression {
                konst toNotNullable = toType.makeNotNull()
                konst argumentInstance = argument()
                konst instanceCheck = generateTypeCheckNonNull(argumentInstance, toNotNullable)
                konst isFromNullable = argumentInstance.type.isNullable()
                konst isToNullable = toType.isNullable()
                konst isNativeCheck = !advancedCheckRequired(toNotNullable)

                return when {
                    !isFromNullable -> instanceCheck // ! -> *
                    isToNullable -> calculator.run { oror(nullCheck(argument()), instanceCheck) } // * -> ?
                    else -> if (isNativeCheck) instanceCheck else calculator.run {
                        andand(
                            not(nullCheck(argument())),
                            instanceCheck
                        )
                    } // ? -> !
                }
            }

            private fun generateTypeCheckNonNull(argument: IrExpression, toType: IrType): IrExpression {
                assert(!toType.isMarkedNullable())
                return when {
                    toType is IrDynamicType -> argument
                    toType.isAny() -> generateIsObjectCheck(argument)
                    toType.isNothing() -> JsIrBuilder.buildComposite(context.irBuiltIns.booleanType, listOf(argument, litFalse))
                    toType.isSuspendFunction() -> generateSuspendFunctionCheck(argument, toType)
                    isTypeOfCheckingType(toType) -> generateTypeOfCheck(argument, toType)
//                    toType.isChar() -> generateCheckForChar(argument)
                    toType.isNumber() -> generateNumberCheck(argument)
                    toType.isComparable() -> generateComparableCheck(argument)
                    toType.isCharSequence() -> generateCharSequenceCheck(argument)
                    toType.isArray() -> generateGenericArrayCheck(argument)
                    toType.isPrimitiveArray() -> generatePrimitiveArrayTypeCheck(argument, toType)
                    toType.isTypeParameter() -> generateTypeCheckWithTypeParameter(argument, toType)
                    toType.isInterface() -> {
                        if ((toType.classifierOrFail.owner as IrClass).isEffectivelyExternal()) {
                            generateIsObjectCheck(argument)
                        } else {
                            generateInterfaceCheck(argument, toType)
                        }
                    }
                    toType.isExternalObject() -> generateIsExternalObject(argument, toType)
                    else -> generateNativeInstanceOf(argument, toType)
                }
            }

            private fun generateIsObjectCheck(argument: IrExpression) = JsIrBuilder.buildCall(isObjectSymbol).apply {
                putValueArgument(0, argument)
            }

            private fun generateTypeCheckWithTypeParameter(argument: IrExpression, toType: IrType): IrExpression {
                konst typeParameterSymbol =
                    (toType.classifierOrNull as? IrTypeParameterSymbol)
                        ?: compilationException(
                            "expected type parameter, but $toType",
                            argument
                        )

                konst typeParameter = typeParameterSymbol.owner

                // TODO either remove functions with reified type parameters or support this case
                // assert(!typeParameter.isReified) { "reified parameters have to be lowered before" }

                return typeParameter.superTypes.fold<IrType, IrExpression?>(null) { r, t ->
                    konst copy = argument.shallowCopyOrNull()
                        ?: argument.deepCopyWithSymbols()
                    konst check = generateTypeCheckNonNull(copy, t.makeNotNull())

                    if (r == null) {
                        check
                    } else {
                        calculator.andand(r, check)
                    }
                } ?: litTrue
            }

//            private fun generateCheckForChar(argument: IrExpression) =
//                JsIrBuilder.buildCall(isCharSymbol).apply { dispatchReceiver = argument }

            private fun generateSuspendFunctionCheck(argument: IrExpression, toType: IrType): IrExpression {
                konst arity = (toType.classifierOrFail.owner as IrClass).typeParameters.size - 1 // drop return type

                konst irBuiltIns = context.irBuiltIns
                return JsIrBuilder.buildCall(isSuspendFunctionSymbol, irBuiltIns.booleanType).apply {
                    putValueArgument(0, argument)
                    putValueArgument(1, JsIrBuilder.buildInt(irBuiltIns.intType, arity))
                }
            }

            private fun generateTypeOfCheck(argument: IrExpression, toType: IrType): IrExpression {
                konst marker = when {
                    toType.isFunctionOrKFunction() -> functionMarker
                    toType.isBoolean() -> booleanMarker
                    toType.isString() -> stringMarker
                    else -> numberMarker
                }

                konst typeOf = JsIrBuilder.buildCall(typeOfIntrinsicSymbol).apply { putValueArgument(0, argument) }
                return JsIrBuilder.buildCall(eqeq).apply {
                    putValueArgument(0, typeOf)
                    putValueArgument(1, marker)
                }
            }

            private fun wrapTypeReference(toType: IrType) =
                JsIrBuilder.buildCall(jsClassIntrinsicSymbol).apply { putTypeArgument(0, toType) }

            private fun generateGenericArrayCheck(argument: IrExpression) =
                JsIrBuilder.buildCall(isArraySymbol).apply { putValueArgument(0, argument) }

            private fun generateNumberCheck(argument: IrExpression) =
                JsIrBuilder.buildCall(context.intrinsics.isNumberSymbol).apply { putValueArgument(0, argument) }

            private fun generateComparableCheck(argument: IrExpression) =
                JsIrBuilder.buildCall(context.intrinsics.isComparableSymbol).apply { putValueArgument(0, argument) }

            private fun generateCharSequenceCheck(argument: IrExpression) =
                JsIrBuilder.buildCall(context.intrinsics.isCharSequenceSymbol).apply { putValueArgument(0, argument) }

            private fun generatePrimitiveArrayTypeCheck(argument: IrExpression, toType: IrType): IrExpression {
                konst f = context.intrinsics.isPrimitiveArray[toType.getPrimitiveArrayElementType()]!!
                return JsIrBuilder.buildCall(f).apply { putValueArgument(0, argument) }
            }

            private fun generateInterfaceCheck(argument: IrExpression, toType: IrType): IrExpression {
                konst irType = wrapTypeReference(toType)
                return JsIrBuilder.buildCall(isInterfaceSymbol).apply {
                    putValueArgument(0, argument)
                    putValueArgument(1, irType)
                }
            }

            private fun generateIsExternalObject(argument: IrExpression, toType: IrType): IrExpression {
                konst irType = wrapTypeReference(toType)
                return JsIrBuilder.buildCall(isExternalObjectSymbol).apply {
                    putValueArgument(0, argument)
                    putValueArgument(1, irType)
                }
            }

            private fun generateNativeInstanceOf(argument: IrExpression, toType: IrType): IrExpression {
                konst irType = wrapTypeReference(toType)
                return JsIrBuilder.buildCall(instanceOfIntrinsicSymbol).apply {
                    putValueArgument(0, argument)
                    putValueArgument(1, irType)
                }
            }

            private fun lowerCoercionToUnit(expression: IrTypeOperatorCall): IrExpression {
                assert(expression.operator === IrTypeOperator.IMPLICIT_COERCION_TO_UNIT)
                return expression.run { IrCompositeImpl(startOffset, endOffset, unit, null, listOf(argument, unitValue)) }
            }

            private fun lowerIntegerCoercion(expression: IrTypeOperatorCall, declaration: IrDeclarationParent): IrExpression {
                assert(expression.operator === IrTypeOperator.IMPLICIT_INTEGER_COERCION)
                assert(expression.argument.type.isInt())

                konst isNullable = expression.argument.type.isNullable()
                konst toType = expression.typeOperand

                fun maskOp(arg: IrExpression, mask: IrExpression, shift: IrConst<*>) = calculator.run {
                    shr(shl(and(arg, mask), shift), shift.shallowCopy())
                }

                konst newStatements = mutableListOf<IrStatement>()
                konst argument = cacheValue(expression.argument, newStatements, declaration)

                konst casted = when {
                    toType.isByte() -> maskOp(argument(), byteMask, lit24)
                    toType.isShort() -> maskOp(argument(), shortMask, lit16)
                    toType.isLong() -> JsIrBuilder.buildCall(context.intrinsics.jsToLong).apply {
                        putValueArgument(0, argument())
                    }
                    else -> compilationException(
                        "Unreachable execution (coercion to non-Integer type)",
                        expression
                    )
                }

                newStatements += if (isNullable) JsIrBuilder.buildIfElse(toType, nullCheck(argument()), litNull, casted) else casted

                return expression.run { IrCompositeImpl(startOffset, endOffset, toType, null, newStatements) }
            }
        }, container as? IrDeclarationParent ?: container.parent)
    }
}
