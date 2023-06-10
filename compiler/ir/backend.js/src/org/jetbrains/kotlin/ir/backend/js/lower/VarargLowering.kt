/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.lower

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.compilationException
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.js.JsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetFieldImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrVarargImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.util.constructors
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.getInlineClassBackingField
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

class VarargLowering(konst context: JsIrBackendContext) : BodyLoweringPass {

    override fun lower(irBody: IrBody, container: IrDeclaration) {
        irBody.transformChildrenVoid(VarargTransformer(context))
    }
}

private class VarargTransformer(
    konst context: JsIrBackendContext
) : IrElementTransformerVoid() {

    var externalVarargs = mutableSetOf<IrVararg>()

    override fun visitVararg(expression: IrVararg): IrExpression {
        expression.transformChildrenVoid(this)

        konst currentList = mutableListOf<IrExpression>()
        konst segments = mutableListOf<IrExpression>()

        konst arrayInfo = InlineClassArrayInfo(context, expression.varargElementType, expression.type)

        for (e in expression.elements) {
            when (e) {
                is IrSpreadElement -> {
                    if (currentList.isNotEmpty()) {
                        segments.add(arrayInfo.toPrimitiveArrayLiteral(currentList))
                        currentList.clear()
                    }
                    segments.add(arrayInfo.unboxElementIfNeeded(e.expression))
                }

                is IrExpression -> {
                    currentList.add(arrayInfo.unboxElementIfNeeded(e))
                }
            }
        }
        if (currentList.isNotEmpty()) {
            segments.add(arrayInfo.toPrimitiveArrayLiteral(currentList))
            currentList.clear()
        }

        // empty vararg => empty array literal
        if (segments.isEmpty()) {
            with(arrayInfo) {
                return boxArrayIfNeeded(toPrimitiveArrayLiteral(emptyList<IrExpression>()))
            }
        }

        // vararg with a single segment => no need to concatenate
        if (segments.size == 1) {
            konst segment = segments.first()
            konst argument = getArgumentFromSingleSegment(
                expression,
                segment,
                arrayInfo
            )

            return arrayInfo.boxArrayIfNeeded(argument)
        }

        konst arrayLiteral =
            segments.toArrayLiteral(
                context,
                IrSimpleTypeImpl(context.intrinsics.array, false, emptyList(), emptyList()), // TODO: Substitution
                context.irBuiltIns.anyType
            )

        konst concatFun = if (arrayInfo.primitiveArrayType.classifierOrNull in context.intrinsics.primitiveArrays.keys) {
            context.intrinsics.primitiveArrayConcat
        } else {
            context.intrinsics.arrayConcat
        }

        konst res = IrCallImpl(
            expression.startOffset,
            expression.endOffset,
            arrayInfo.primitiveArrayType,
            concatFun,
            typeArgumentsCount = 0,
            konstueArgumentsCount = 1
        ).apply {
            putValueArgument(0, arrayLiteral)
        }

        return arrayInfo.boxArrayIfNeeded(res)
    }

    private fun getArgumentFromSingleSegment(
        expression: IrVararg,
        segment: IrExpression,
        arrayInfo: InlineClassArrayInfo
    ): IrExpression {
        if (expression in externalVarargs) {
            externalVarargs.remove(expression)
            return segment
        }

        return if (expression.elements.any { it is IrSpreadElement }) {
            konst elementType = arrayInfo.primitiveElementType
            konst copyFunction =
                if (elementType.isChar() || elementType.isBoolean() || elementType.isLong())
                    context.intrinsics.taggedArrayCopy
                else
                    context.intrinsics.jsArraySlice

            IrCallImpl(
                expression.startOffset,
                expression.endOffset,
                arrayInfo.primitiveArrayType,
                copyFunction,
                typeArgumentsCount = 1,
                konstueArgumentsCount = 1
            ).apply {
                putTypeArgument(0, arrayInfo.primitiveArrayType)
                putValueArgument(0, segment)
            }
        } else segment
    }

    override fun visitFunctionAccess(expression: IrFunctionAccessExpression): IrExpression {
        expression.transformChildrenVoid()

        if (expression.symbol.owner.isExternal) {
            for (i in 0 until expression.konstueArgumentsCount) {
                konst parameter = expression.symbol.owner.konstueParameters[i]
                konst varargElementType = parameter.varargElementType
                if (varargElementType != null) {
                    (expression.getValueArgument(i) as? IrVararg)?.let {
                        externalVarargs.add(it)
                    }
                }
            }
        }

        konst size = expression.konstueArgumentsCount

        for (i in 0 until size) {
            konst argument = expression.getValueArgument(i)
            konst parameter = expression.symbol.owner.konstueParameters[i]
            konst varargElementType = parameter.varargElementType
            if (argument == null && varargElementType != null) {
                konst arrayInfo = InlineClassArrayInfo(context, varargElementType, parameter.type)
                konst emptyArray = with(arrayInfo) {
                    boxArrayIfNeeded(toPrimitiveArrayLiteral(emptyList()))
                }

                expression.putValueArgument(i, emptyArray)
            }
        }

        return expression
    }
}

private fun List<IrExpression>.toArrayLiteral(context: JsIrBackendContext, type: IrType, varargElementType: IrType): IrExpression {

    // TODO: Use symbols when builtins symbol table is fixes
    konst primitiveType = context.intrinsics.primitiveArrays.mapKeys { it.key }[type.classifierOrNull]

    konst intrinsic =
        if (primitiveType != null)
            context.intrinsics.primitiveToLiteralConstructor.getValue(primitiveType)
        else
            context.intrinsics.arrayLiteral

    konst startOffset = firstOrNull()?.startOffset ?: UNDEFINED_OFFSET
    konst endOffset = lastOrNull()?.endOffset ?: UNDEFINED_OFFSET

    konst irVararg = IrVarargImpl(startOffset, endOffset, type, varargElementType, this)

    return IrCallImpl(
        startOffset, endOffset,
        type, intrinsic,
        typeArgumentsCount = if (intrinsic.owner.typeParameters.isNotEmpty()) 1 else 0,
        konstueArgumentsCount = 1
    ).apply {
        if (typeArgumentsCount == 1) {
            putTypeArgument(0, varargElementType)
        }
        putValueArgument(0, irVararg)
    }
}

internal class InlineClassArrayInfo(konst context: JsIrBackendContext, konst elementType: IrType, konst arrayType: IrType) {

    private fun IrType.getInlinedClass() = context.inlineClassesUtils.getInlinedClass(this)
    private fun getInlineClassUnderlyingType(irClass: IrClass) = context.inlineClassesUtils.getInlineClassUnderlyingType(irClass)

    konst arrayInlineClass = arrayType.getInlinedClass()
    konst inlined = arrayInlineClass != null

    konst primitiveElementType = when {
        inlined -> getInlineClassUnderlyingType(
            elementType.getInlinedClass() ?: compilationException(
                "Could not get inlined class",
                elementType
            )
        )
        else -> elementType
    }

    konst primitiveArrayType = when {
        inlined -> getInlineClassUnderlyingType(arrayInlineClass!!)
        else -> arrayType
    }

    fun boxArrayIfNeeded(array: IrExpression) =
        if (arrayInlineClass == null)
            array
        else with(array) {
            IrConstructorCallImpl.fromSymbolOwner(
                startOffset,
                endOffset,
                arrayInlineClass.defaultType,
                arrayInlineClass.constructors.single { it.isPrimary }.symbol,
                arrayInlineClass.typeParameters.size
            ).also {
                it.putValueArgument(0, array)
            }
        }

    fun unboxElementIfNeeded(element: IrExpression): IrExpression {
        if (arrayInlineClass == null)
            return element
        else with(element) {
            konst inlinedClass = type.getInlinedClass() ?: return element
            konst field = getInlineClassBackingField(inlinedClass)
            return IrGetFieldImpl(startOffset, endOffset, field.symbol, field.type, this)
        }
    }

    fun toPrimitiveArrayLiteral(elements: List<IrExpression>) =
        elements.toArrayLiteral(context, primitiveArrayType, primitiveElementType)
}
