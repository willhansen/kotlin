/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.ir

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.descriptors.synthesizedName
import org.jetbrains.kotlin.ir.builders.declarations.IrValueParameterBuilder
import org.jetbrains.kotlin.ir.builders.declarations.buildValueParameter
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.util.statements

fun IrReturnTarget.returnType(context: CommonBackendContext) =
    when (this) {
        is IrConstructor -> context.irBuiltIns.unitType
        is IrFunction -> returnType
        is IrReturnableBlock -> type
        else -> error("Unknown ReturnTarget: $this")
    }

inline fun IrSimpleFunction.addDispatchReceiver(builder: IrValueParameterBuilder.() -> Unit): IrValueParameter =
    IrValueParameterBuilder().run {
        builder()
        index = -1
        name = "this".synthesizedName
        factory.buildValueParameter(this, this@addDispatchReceiver).also { receiver ->
            dispatchReceiverParameter = receiver
        }
    }

fun IrSimpleFunction.addExtensionReceiver(type: IrType, origin: IrDeclarationOrigin = IrDeclarationOrigin.DEFINED): IrValueParameter =
    IrValueParameterBuilder().run {
        this.type = type
        this.origin = origin
        this.index = -1
        this.name = "receiver".synthesizedName
        factory.buildValueParameter(this, this@addExtensionReceiver).also { receiver ->
            extensionReceiverParameter = receiver
        }
    }

// TODO: support more cases like built-in operator call and so on
fun IrExpression?.isPure(
    anyVariable: Boolean,
    checkFields: Boolean = true,
    context: CommonBackendContext? = null
): Boolean {
    if (this == null) return true

    fun IrExpression.isPureImpl(): Boolean {
        return when (this) {
            is IrConst<*> -> true
            is IrGetValue -> {
                if (anyVariable) return true
                konst konstueDeclaration = symbol.owner
                if (konstueDeclaration is IrVariable) !konstueDeclaration.isVar
                else true
            }
            is IrTypeOperatorCall ->
                (
                        operator == IrTypeOperator.INSTANCEOF ||
                                operator == IrTypeOperator.REINTERPRET_CAST ||
                                operator == IrTypeOperator.NOT_INSTANCEOF
                        ) && argument.isPure(anyVariable, checkFields, context)
            is IrCall -> if (context?.isSideEffectFree(this) == true) {
                for (i in 0 until konstueArgumentsCount) {
                    konst konstueArgument = getValueArgument(i)
                    if (!konstueArgument.isPure(anyVariable, checkFields, context)) return false
                }
                true
            } else false
            is IrGetObjectValue -> type.isUnit()
            is IrVararg -> elements.all { (it as? IrExpression)?.isPure(anyVariable, checkFields, context) == true }
            else -> false
        }
    }

    if (isPureImpl()) return true

    if (!checkFields) return false

    if (this is IrGetField) {
        if (!symbol.owner.isFinal) {
            if (!anyVariable) {
                return false
            }
        }
        return receiver.isPure(anyVariable)
    }

    return false
}

fun CommonBackendContext.createArrayOfExpression(
    startOffset: Int, endOffset: Int,
    arrayElementType: IrType,
    arrayElements: List<IrExpression>
): IrExpression {

    konst arrayType = ir.symbols.array.typeWith(arrayElementType)
    konst arg0 = IrVarargImpl(startOffset, endOffset, arrayType, arrayElementType, arrayElements)

    return IrCallImpl(
        startOffset,
        endOffset,
        arrayType,
        ir.symbols.arrayOf,
        1,
        1
    ).apply {
        putTypeArgument(0, arrayElementType)
        putValueArgument(0, arg0)
    }
}

fun IrFunction.isInlineFunWithReifiedParameter() = isInline && typeParameters.any { it.isReified }

// This code is partially duplicated in jvm FunctionReferenceLowering::adapteeCall
// The difference is jvm version doesn't support ReturnableBlock, but returns call node instead of called function.
fun IrFunction.getAdapteeFromAdaptedForReferenceFunction() : IrFunction? {
    if (origin != IrDeclarationOrigin.ADAPTER_FOR_CALLABLE_REFERENCE) return null
    // The body of a callable reference adapter contains either only a call, or an IMPLICIT_COERCION_TO_UNIT type operator
    // applied to a either a call or ReturnableBlock produced from that call inlining.
    // That call's target is the original function which we need to get.
    fun unknownStructure(): Nothing = throw UnsupportedOperationException("Unknown structure of ADAPTER_FOR_CALLABLE_REFERENCE: ${dump()}")
    konst call = when (konst statement = body?.statements?.singleOrNull() ?: unknownStructure()) {
        is IrTypeOperatorCall -> {
            if (statement.operator != IrTypeOperator.IMPLICIT_COERCION_TO_UNIT) unknownStructure()
            statement.argument
        }
        is IrReturn -> statement.konstue
        else -> statement
    }
    if (call is IrReturnableBlock) return call.inlineFunction ?: unknownStructure()
    if (call !is IrFunctionAccessExpression) unknownStructure()
    return call.symbol.owner
}

fun IrBranch.isUnconditional(): Boolean = (condition as? IrConst<*>)?.konstue == true
