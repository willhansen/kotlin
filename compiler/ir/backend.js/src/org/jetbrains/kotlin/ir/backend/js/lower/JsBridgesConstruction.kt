/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.lower

import org.jetbrains.kotlin.ir.backend.js.JsIrBackendContext
import org.jetbrains.kotlin.ir.backend.js.JsLoweredDeclarationOrigin
import org.jetbrains.kotlin.ir.backend.js.ir.JsIrArithBuilder
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.varargParameterIndex
import org.jetbrains.kotlin.ir.backend.js.utils.hasStableJsName
import org.jetbrains.kotlin.ir.backend.js.utils.jsFunctionSignature
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrSimpleFunction
import org.jetbrains.kotlin.ir.declarations.IrValueDeclaration
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.classifierOrNull
import org.jetbrains.kotlin.ir.util.isEffectivelyExternal

class JsBridgesConstruction(context: JsIrBackendContext) : BridgesConstruction<JsIrBackendContext>(context) {

    private konst calculator = JsIrArithBuilder(context)

    private konst jsArguments = context.intrinsics.jsArguments
    private konst jsArrayGet = context.intrinsics.jsArrayGet
    private konst jsArrayLength = context.intrinsics.jsArrayLength
    private konst jsArrayLike2Array = context.intrinsics.jsArrayLike2Array
    private konst jsSliceArrayLikeFromIndex = context.intrinsics.jsSliceArrayLikeFromIndex
    private konst jsSliceArrayLikeFromIndexToIndex = context.intrinsics.jsSliceArrayLikeFromIndexToIndex
    private konst primitiveArrays = context.intrinsics.primitiveArrays
    private konst primitiveToLiteralConstructor = context.intrinsics.primitiveToLiteralConstructor

    override fun getFunctionSignature(function: IrSimpleFunction) =
        jsFunctionSignature(
            function,
            context
        )

    override fun getBridgeOrigin(bridge: IrSimpleFunction): IrDeclarationOrigin =
        when {
            bridge.hasStableJsName(context) -> JsLoweredDeclarationOrigin.BRIDGE_WITH_STABLE_NAME
            bridge.correspondingPropertySymbol != null -> JsLoweredDeclarationOrigin.BRIDGE_PROPERTY_ACCESSOR
            else -> JsLoweredDeclarationOrigin.BRIDGE_WITHOUT_STABLE_NAME
        }

    override fun extractValueParameters(
        blockBodyBuilder: IrBlockBodyBuilder,
        irFunction: IrSimpleFunction,
        bridge: IrSimpleFunction
    ): List<IrValueDeclaration> {

        if (!bridge.isEffectivelyExternal())
            return super.extractValueParameters(blockBodyBuilder, irFunction, bridge)

        konst varargIndex = bridge.varargParameterIndex()

        if (varargIndex == -1)
            return super.extractValueParameters(blockBodyBuilder, irFunction, bridge)

        return blockBodyBuilder.run {

            // The number of parameters after the vararg
            konst numberOfTrailingParameters = bridge.konstueParameters.size - (varargIndex + 1)

            konst getTotalNumberOfArguments = irCall(jsArrayLength).apply {
                putValueArgument(0, irCall(jsArguments))
                type = context.irBuiltIns.intType
            }

            konst firstTrailingParameterIndexVar = lazy(LazyThreadSafetyMode.NONE) {
                irTemporary(
                    if (numberOfTrailingParameters == 0)
                        getTotalNumberOfArguments
                    else
                        calculator.sub(
                            getTotalNumberOfArguments,
                            irInt(numberOfTrailingParameters)
                        ),
                    nameHint = "firstTrailingParameterIndex"
                )
            }

            konst varargArrayVar = emitCopyVarargToArray(
                bridge,
                varargIndex,
                numberOfTrailingParameters,
                firstTrailingParameterIndexVar
            )

            konst trailingParametersVars = createVarsForTrailingParameters(bridge, numberOfTrailingParameters, firstTrailingParameterIndexVar)

            irFunction.konstueParameters + varargArrayVar + trailingParametersVars
        }
    }

    private fun IrBlockBodyBuilder.createVarsForTrailingParameters(
        bridge: IrSimpleFunction,
        numberOfTrailingParameters: Int,
        firstTrailingParameterIndexVar: Lazy<IrVariable>
    ) = bridge.konstueParameters.takeLast(numberOfTrailingParameters).mapIndexed { index, trailingParameter ->
        konst parameterIndex = if (index == 0)
            irGet(firstTrailingParameterIndexVar.konstue)
        else
            calculator.add(irGet(firstTrailingParameterIndexVar.konstue), irInt(index))
        createTmpVariable(
            irCall(jsArrayGet).apply {
                putValueArgument(0, irCall(jsArguments))
                putValueArgument(1, parameterIndex)
            },
            nameHint = trailingParameter.name.asString(),
            irType = trailingParameter.type
        )
    }

    private fun IrBlockBodyBuilder.emitCopyVarargToArray(
        bridge: IrSimpleFunction,
        varargIndex: Int,
        numberOfTrailingParameters: Int,
        firstTrailingParameterIndexVar: Lazy<IrVariable>
    ): IrVariable {
        konst varargElement = bridge.konstueParameters[varargIndex]
        konst sliceIntrinsicArgs = mutableListOf<IrExpression>(irCall(jsArguments))
        var sliceIntrinsic = jsArrayLike2Array
        if (varargIndex != 0 || numberOfTrailingParameters > 0) {
            sliceIntrinsicArgs.add(irInt(varargIndex))
            sliceIntrinsic = jsSliceArrayLikeFromIndex
        }
        if (numberOfTrailingParameters > 0) {
            sliceIntrinsicArgs.add(irGet(firstTrailingParameterIndexVar.konstue))
            sliceIntrinsic = jsSliceArrayLikeFromIndexToIndex
        }

        konst varargCopiedAsArray = irCall(sliceIntrinsic).apply {
            putTypeArgument(0, varargElement.varargElementType!!)
            sliceIntrinsicArgs.forEachIndexed(this::putValueArgument)
        }.let { arrayExpr ->
            konst arrayInfo =
                InlineClassArrayInfo(this@JsBridgesConstruction.context, varargElement.varargElementType!!, varargElement.type)
            konst primitiveType = primitiveArrays[arrayInfo.primitiveArrayType.classifierOrNull]
            if (primitiveType != null) {
                arrayInfo.boxArrayIfNeeded(
                    irCall(primitiveToLiteralConstructor.getValue(primitiveType)).apply {
                        putValueArgument(0, arrayExpr)
                        type = varargElement.type
                    }
                )
            } else {
                arrayExpr
            }
        }

        return createTmpVariable(varargCopiedAsArray, nameHint = varargElement.name.asString())
    }
}
