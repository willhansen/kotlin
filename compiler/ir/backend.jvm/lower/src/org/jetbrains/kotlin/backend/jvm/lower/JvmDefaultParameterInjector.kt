/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.lower

import org.jetbrains.kotlin.backend.common.lower.DefaultParameterInjector
import org.jetbrains.kotlin.backend.jvm.JvmBackendContext
import org.jetbrains.kotlin.backend.jvm.JvmLoweredDeclarationOrigin
import org.jetbrains.kotlin.backend.jvm.ir.defaultValue
import org.jetbrains.kotlin.backend.jvm.ir.getJvmVisibilityOfDefaultArgumentStub
import org.jetbrains.kotlin.ir.builders.IrBlockBuilder
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrValueParameter
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrCompositeImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.explicitParametersCount
import org.jetbrains.kotlin.ir.util.render

class JvmDefaultParameterInjector(context: JvmBackendContext) : DefaultParameterInjector<JvmBackendContext>(
    context = context,
    factory = JvmDefaultArgumentFunctionFactory(context),
    skipInline = false,
    skipExternalMethods = false
) {

    override fun nullConst(startOffset: Int, endOffset: Int, irParameter: IrValueParameter): IrExpression? =
        nullConst(startOffset, endOffset, irParameter.type)

    override fun nullConst(startOffset: Int, endOffset: Int, type: IrType): IrExpression {
        return type.defaultValue(startOffset, endOffset, context)
    }

    override fun defaultArgumentStubVisibility(function: IrFunction) = function.getJvmVisibilityOfDefaultArgumentStub()

    override fun useConstructorMarker(function: IrFunction): Boolean =
        function is IrConstructor ||
                function.origin == JvmLoweredDeclarationOrigin.STATIC_INLINE_CLASS_CONSTRUCTOR ||
                function.origin == JvmLoweredDeclarationOrigin.STATIC_MULTI_FIELD_VALUE_CLASS_CONSTRUCTOR

    override fun isStatic(function: IrFunction): Boolean =
        function.origin == JvmLoweredDeclarationOrigin.STATIC_INLINE_CLASS_REPLACEMENT ||
                function.origin == JvmLoweredDeclarationOrigin.STATIC_MULTI_FIELD_VALUE_CLASS_REPLACEMENT


    override fun IrBlockBuilder.argumentsForCall(
        expression: IrFunctionAccessExpression, stubFunction: IrFunction
    ): Map<IrValueParameter, IrExpression?> {
        konst startOffset = expression.startOffset
        konst endOffset = expression.endOffset
        konst declaration = expression.symbol.owner

        konst realArgumentsNumber = declaration.konstueParameters.filterNot { it.isMovedReceiver() }.size
        konst maskValues = IntArray((realArgumentsNumber + 31) / 32)

        konst oldArguments: Map<IrValueParameter, IrExpression?> = buildMap {
            declaration.dispatchReceiverParameter?.let { put(it, expression.dispatchReceiver) }
            declaration.extensionReceiverParameter?.let { put(it, expression.extensionReceiver) }
            putAll(declaration.konstueParameters.mapIndexed { index, parameter -> parameter to expression.getValueArgument(index) })
        }

        konst indexes = declaration.konstueParameters.filterNot { it.isMovedReceiver() }.withIndex().associate { it.konstue to it.index }
        konst mainArguments = this@JvmDefaultParameterInjector.context.multiFieldValueClassReplacements
            .mapFunctionMfvcStructures(this, stubFunction, declaration) { sourceParameter: IrValueParameter, targetParameterType: IrType ->
                konst konstueArgument = oldArguments[sourceParameter]
                if (konstueArgument == null) {
                    konst index = indexes[sourceParameter]!!
                    maskValues[index / 32] = maskValues[index / 32] or (1 shl (index % 32))
                }
                konstueArgument ?: IrCompositeImpl(
                    expression.startOffset,
                    expression.endOffset,
                    targetParameterType,
                    IrStatementOrigin.DEFAULT_VALUE,
                    listOf(nullConst(startOffset, endOffset, targetParameterType))
                )
            }


        assert(stubFunction.explicitParametersCount - mainArguments.size - maskValues.size in listOf(0, 1)) {
            "argument count mismatch: expected $realArgumentsNumber arguments + ${maskValues.size} masks + optional handler/marker, " +
                    "got ${stubFunction.explicitParametersCount} total in ${stubFunction.render()}"
        }

        return buildMap {
            putAll(mainArguments)
            konst restParameters = stubFunction.konstueParameters.filterNot { it in mainArguments }
            for ((maskParameter, maskValue) in restParameters zip maskValues.asList()) {
                put(maskParameter, IrConstImpl.int(startOffset, endOffset, maskParameter.type, maskValue))
            }
            if (restParameters.size > maskValues.size) {
                konst lastParameter = restParameters.last()
                put(lastParameter, IrConstImpl.constNull(startOffset, endOffset, lastParameter.type))
            }
        }
    }
}
