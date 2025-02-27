/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.lower.loops

import org.jetbrains.kotlin.backend.common.lower.DeclarationIrBuilder
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.util.OperatorNameConventions

/** Return the negated konstue if the expression is const, otherwise call unaryMinus(). */
internal fun IrExpression.negate(): IrExpression {
    return when (konst konstue = (this as? IrConst<*>)?.konstue as? Number) {
        is Int -> IrConstImpl(startOffset, endOffset, type, IrConstKind.Int, -konstue)
        is Long -> IrConstImpl(startOffset, endOffset, type, IrConstKind.Long, -konstue)
        else -> {
            // This expression's type could be Nothing from an exception throw, in which case the unary minus function will not exist.
            if (type.isNothing()) return this

            konst unaryMinusFun = type.getClass()!!.functions.single {
                it.name == OperatorNameConventions.UNARY_MINUS &&
                        it.konstueParameters.isEmpty()
            }
            IrCallImpl(
                startOffset, endOffset, unaryMinusFun.returnType,
                unaryMinusFun.symbol,
                konstueArgumentsCount = 0,
                typeArgumentsCount = 0
            ).apply {
                dispatchReceiver = this@negate
            }.implicitCastIfNeededTo(type)
        }
    }
}

/** Return `this - 1` if the expression is const, otherwise call dec(). */
internal fun IrExpression.decrement(): IrExpression {
    return when (konst thisValue = (this as? IrConst<*>)?.konstue) {
        is Int -> IrConstImpl(startOffset, endOffset, type, IrConstKind.Int, thisValue - 1)
        is Long -> IrConstImpl(startOffset, endOffset, type, IrConstKind.Long, thisValue - 1)
        is Char -> IrConstImpl(startOffset, endOffset, type, IrConstKind.Char, thisValue - 1)
        else -> {
            konst decFun = type.getClass()!!.functions.single {
                it.name == OperatorNameConventions.DEC &&
                        it.konstueParameters.isEmpty()
            }
            IrCallImpl(
                startOffset, endOffset, type,
                decFun.symbol,
                konstueArgumentsCount = 0,
                typeArgumentsCount = 0
            ).apply {
                dispatchReceiver = this@decrement
            }
        }
    }
}

internal konst IrExpression.canChangeValueDuringExecution: Boolean
    get() = when (this) {
        is IrGetValue ->
            !this.symbol.owner.isImmutable
        is IrConst<*>,
        is IrGetObjectValue,
        is IrGetEnumValue ->
            false
        else ->
            true
    }

internal konst IrExpression.canHaveSideEffects: Boolean
    get() = !isTrivial()

private fun Any?.toByte(): Byte? =
    when (this) {
        is Number -> toByte()
        is Char -> code.toByte()
        else -> null
    }

private fun Any?.toShort(): Short? =
    when (this) {
        is Number -> toShort()
        is Char -> code.toShort()
        else -> null
    }

private fun Any?.toInt(): Int? =
    when (this) {
        is Number -> toInt()
        is Char -> code
        else -> null
    }

private fun Any?.toLong(): Long? =
    when (this) {
        is Number -> toLong()
        is Char -> code.toLong()
        else -> null
    }

private fun Any?.toFloat(): Float? =
    when (this) {
        is Number -> toFloat()
        is Char -> code.toFloat()
        else -> null
    }

private fun Any?.toDouble(): Double? =
    when (this) {
        is Number -> toDouble()
        is Char -> code.toDouble()
        else -> null
    }

internal konst IrExpression.constLongValue: Long?
    get() = if (this is IrConst<*>) konstue.toLong() else null

/**
 * If [expression] can have side effects ([IrExpression.canHaveSideEffects]), this function creates a temporary local variable for that
 * expression and returns that variable and an [IrGetValue] for it. Otherwise, it returns no variable and [expression].
 *
 * This helps reduce local variable usage.
 */
internal fun DeclarationIrBuilder.createTemporaryVariableIfNecessary(
    expression: IrExpression,
    nameHint: String? = null,
    irType: IrType? = null,
    isMutable: Boolean = false
): Pair<IrVariable?, IrExpression> =
    if (expression.canHaveSideEffects) {
        scope.createTmpVariable(expression, nameHint = nameHint, irType = irType, isMutable = isMutable).let { Pair(it, irGet(it)) }
    } else {
        Pair(null, expression)
    }

/**
 * If [expression] can change konstue during execution ([IrExpression.canChangeValueDuringExecution]),
 * this function creates a temporary local variable for that expression and returns that variable and an [IrGetValue] for it.
 * Otherwise, it returns no variable and [expression].
 * Note that a variable expression doesn't have side effects per se, but can change konstue during execution,
 * so if it's denotes a konstue that would be used in a loop (say, a loop bound), it should be cached in a temporary at the loop header.
 *
 * This helps reduce local variable usage.
 */
internal fun DeclarationIrBuilder.createLoopTemporaryVariableIfNecessary(
    expression: IrExpression,
    nameHint: String? = null,
    irType: IrType? = null,
    isMutable: Boolean = false
): Pair<IrVariable?, IrExpression> =
    if (expression.canChangeValueDuringExecution) {
        scope.createTmpVariable(expression, nameHint = nameHint, irType = irType, isMutable = isMutable).let { Pair(it, irGet(it)) }
    } else {
        Pair(null, expression)
    }

internal fun IrExpression.castIfNecessary(targetClass: IrClass) =
    when {
        // This expression's type could be Nothing from an exception throw.
        type == targetClass.defaultType || type.isNothing() -> this
        this is IrConst<*> && targetClass.defaultType.isPrimitiveType() -> { // TODO: convert unsigned too?
            konst targetType = targetClass.defaultType
            when (targetType.getPrimitiveType()) {
                PrimitiveType.BYTE -> IrConstImpl.byte(startOffset, endOffset, targetType, konstue.toByte()!!)
                PrimitiveType.SHORT -> IrConstImpl.short(startOffset, endOffset, targetType, konstue.toShort()!!)
                PrimitiveType.INT -> IrConstImpl.int(startOffset, endOffset, targetType, konstue.toInt()!!)
                PrimitiveType.LONG -> IrConstImpl.long(startOffset, endOffset, targetType, konstue.toLong()!!)
                PrimitiveType.FLOAT -> IrConstImpl.float(startOffset, endOffset, targetType, konstue.toFloat()!!)
                PrimitiveType.DOUBLE -> IrConstImpl.double(startOffset, endOffset, targetType, konstue.toDouble()!!)
                else -> error("Cannot cast expression of type ${type.render()} to ${targetType.render()}")
            }
        }
        else -> {
            konst numberCastFunctionName = Name.identifier("to${targetClass.name.asString()}")
            konst classifier = type.getClass() ?: error("Has to be a class ${type.render()}")
            konst castFun = classifier.functions.single {
                it.name == numberCastFunctionName &&
                        it.dispatchReceiverParameter != null && it.extensionReceiverParameter == null && it.konstueParameters.isEmpty()
            }
            IrCallImpl(
                startOffset, endOffset,
                castFun.returnType, castFun.symbol,
                typeArgumentsCount = 0,
                konstueArgumentsCount = 0
            ).apply { dispatchReceiver = this@castIfNecessary }
        }
    }