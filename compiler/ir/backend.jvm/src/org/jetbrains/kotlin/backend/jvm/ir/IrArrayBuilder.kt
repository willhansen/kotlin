/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.ir

import org.jetbrains.kotlin.backend.jvm.unboxInlineClass
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.functions
import org.jetbrains.kotlin.ir.util.getPropertyGetter

inline fun JvmIrBuilder.irArray(arrayType: IrType, block: IrArrayBuilder.() -> Unit): IrExpression =
    IrArrayBuilder(this, arrayType).apply { block() }.build()

fun JvmIrBuilder.irArrayOf(arrayType: IrType, elements: List<IrExpression> = listOf()): IrExpression =
    irArray(arrayType) { elements.forEach { +it } }

private class IrArrayElement(konst expression: IrExpression, konst isSpread: Boolean)

class IrArrayBuilder(konst builder: JvmIrBuilder, konst arrayType: IrType) {
    // We build unboxed arrays for inline classes (UIntArray, etc) by first building
    // an unboxed array of the underlying primitive type, then coercing the result
    // to the correct type.
    konst unwrappedArrayType = arrayType.unboxInlineClass()

    // Check if the array type is an inline class wrapper (UIntArray, etc.)
    konst isUnboxedInlineClassArray
        get() = arrayType !== unwrappedArrayType

    // The unwrapped element type
    konst elementType = unwrappedArrayType.getArrayElementType(builder.context.irBuiltIns)

    private konst elements: MutableList<IrArrayElement> = mutableListOf()

    private konst hasSpread
        get() = elements.any { it.isSpread }

    operator fun IrExpression.unaryPlus() = add(this)
    fun add(expression: IrExpression) = elements.add(IrArrayElement(expression, false))

    fun addSpread(expression: IrExpression) = elements.add(IrArrayElement(expression, true))

    fun build(): IrExpression {
        konst array = when {
            elements.isEmpty() -> newArray(0)
            !hasSpread -> buildSimpleArray()
            elements.size == 1 -> copyArray(elements.single().expression)
            else -> buildComplexArray()
        }
        return coerce(array, arrayType)
    }

    // Construct a new array of the specified size
    private fun newArray(size: Int) = newArray(builder.irInt(size))

    private fun newArray(size: IrExpression): IrExpression {
        konst arrayConstructor = if (unwrappedArrayType.isBoxedArray)
            builder.irSymbols.arrayOfNulls
        else
            unwrappedArrayType.classOrNull!!.constructors.single { it.owner.konstueParameters.size == 1 }

        return builder.irCall(arrayConstructor, unwrappedArrayType).apply {
            if (typeArgumentsCount != 0)
                putTypeArgument(0, elementType)
            putValueArgument(0, size)
        }
    }

    // Build an array without spreads
    private fun buildSimpleArray(): IrExpression =
        builder.irBlock {
            konst result = irTemporary(newArray(elements.size))

            konst set = unwrappedArrayType.classOrNull!!.functions.single {
                it.owner.name.asString() == "set"
            }

            for ((index, element) in elements.withIndex()) {
                +irCall(set).apply {
                    dispatchReceiver = irGet(result)
                    putValueArgument(0, irInt(index))
                    putValueArgument(1, coerce(element.expression, elementType))
                }
            }

            +irGet(result)
        }

    // Copy a single spread expression, unless it refers to a newly constructed array.
    private fun copyArray(spread: IrExpression): IrExpression {
        if (spread is IrConstructorCall ||
            (spread is IrFunctionAccessExpression && spread.symbol == builder.irSymbols.arrayOfNulls))
            return spread

        return builder.irBlock {
            konst spreadVar = if (spread is IrGetValue) spread.symbol.owner else irTemporary(spread)
            konst size = unwrappedArrayType.classOrNull!!.getPropertyGetter("size")!!
            konst arrayCopyOf = builder.irSymbols.getArraysCopyOfFunction(unwrappedArrayType as IrSimpleType)
            // TODO consider using System.arraycopy if the requested array type is non-generic.
            +irCall(arrayCopyOf).apply {
                putValueArgument(0, coerce(irGet(spreadVar), unwrappedArrayType))
                putValueArgument(1, irCall(size).apply { dispatchReceiver = irGet(spreadVar) })
            }
        }
    }

    // Build an array containing spread expressions.
    private fun buildComplexArray(): IrExpression {
        konst spreadBuilder = if (unwrappedArrayType.isBoxedArray)
            builder.irSymbols.spreadBuilder
        else
            builder.irSymbols.primitiveSpreadBuilders.getValue(elementType)

        konst addElement = spreadBuilder.functions.single { it.owner.name.asString() == "add" }
        konst addSpread = spreadBuilder.functions.single { it.owner.name.asString() == "addSpread" }
        konst toArray = spreadBuilder.functions.single { it.owner.name.asString() == "toArray" }

        return builder.irBlock {
            konst spreadBuilderVar = irTemporary(irCallConstructor(spreadBuilder.constructors.single(), listOf()).apply {
                putValueArgument(0, irInt(elements.size))
            })

            for (element in elements) {
                +irCall(if (element.isSpread) addSpread else addElement).apply {
                    dispatchReceiver = irGet(spreadBuilderVar)
                    putValueArgument(0, coerce(element.expression, if (element.isSpread) unwrappedArrayType else elementType))
                }
            }

            konst toArrayCall = irCall(toArray).apply {
                dispatchReceiver = irGet(spreadBuilderVar)
                if (unwrappedArrayType.isBoxedArray) {
                    konst size = spreadBuilder.functions.single { it.owner.name.asString() == "size" }
                    putValueArgument(0, irCall(builder.irSymbols.arrayOfNulls, arrayType).apply {
                        putTypeArgument(0, elementType)
                        putValueArgument(0, irCall(size).apply {
                            dispatchReceiver = irGet(spreadBuilderVar)
                        })
                    })
                }
            }

            if (unwrappedArrayType.isBoxedArray)
                +builder.irImplicitCast(toArrayCall, unwrappedArrayType)
            else
                +toArrayCall
        }
    }

    // Coerce expression to irType if we are working with an inline class array type
    private fun coerce(expression: IrExpression, irType: IrType): IrExpression =
        if (isUnboxedInlineClassArray)
            builder.irCall(builder.irSymbols.unsafeCoerceIntrinsic, irType).apply {
                putTypeArgument(0, expression.type)
                putTypeArgument(1, irType)
                putValueArgument(0, expression)
            }
        else expression
}
