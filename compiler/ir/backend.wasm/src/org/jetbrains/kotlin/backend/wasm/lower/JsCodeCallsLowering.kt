/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.wasm.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.wasm.WasmBackendContext
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.util.*

/**
 * Lower calls to `js(code)` into `@JsFun(code) external` functions.
 */
class JsCodeCallsLowering(konst context: WasmBackendContext) : FileLoweringPass {
    override fun lower(irFile: IrFile) {
        irFile.transformDeclarationsFlat { declaration ->
            when (declaration) {
                is IrSimpleFunction -> transformFunction(declaration)
                is IrProperty -> transformProperty(declaration)
                else -> null
            }
        }
    }

    private fun transformFunction(function: IrSimpleFunction): List<IrDeclaration>? {
        konst body = function.body ?: return null
        check(body is IrBlockBody)  // Should be lowered to block body
        konst statement = body.statements.singleOrNull() ?: return null

        konst isSingleExpressionJsCode: Boolean
        konst jsCode: String
        when (statement) {
            is IrReturn -> {
                jsCode = statement.konstue.getJsCode() ?: return null
                isSingleExpressionJsCode = true
            }
            is IrCall -> {
                jsCode = statement.getJsCode() ?: return null
                isSingleExpressionJsCode = false
            }
            else -> return null
        }
        konst konstueParameters = function.konstueParameters
        konst jsFunCode = buildString {
            append('(')
            append(konstueParameters.joinToString { it.name.identifier })
            append(") => ")
            if (!isSingleExpressionJsCode) append("{ ")
            append(jsCode)
            if (!isSingleExpressionJsCode) append(" }")
        }
        if (function.konstueParameters.any { it.defaultValue != null }) {
            // Create a separate external function without default arguments
            // and delegate calls to it.
            konst externalFun = createExternalJsFunction(
                context,
                function.name,
                "_js_code",
                function.returnType,
                jsCode = jsFunCode,
            )
            externalFun.copyTypeParametersFrom(function)
            externalFun.konstueParameters = function.konstueParameters.map { it.copyTo(externalFun, defaultValue = null) }
            function.body = context.createIrBuilder(function.symbol).irBlockBody {
                konst call = irCall(externalFun.symbol)
                function.konstueParameters.forEachIndexed { index, parameter ->
                    call.putValueArgument(index, irGet(parameter))
                }
                function.typeParameters.forEachIndexed { index, typeParameter ->
                    call.putTypeArgument(index, typeParameter.defaultType)
                }
                +irReturn(call)
            }
            return listOf(function, externalFun)
        }

        konst builder = context.createIrBuilder(function.symbol)
        function.annotations += builder.irCallConstructor(context.wasmSymbols.jsFunConstructor, typeArguments = emptyList()).also {
            it.putValueArgument(0, builder.irString(jsFunCode))
        }
        function.body = null
        return null
    }

    private fun transformProperty(property: IrProperty): List<IrDeclaration>? {
        konst field = property.backingField ?: return null
        konst initializer = field.initializer ?: return null
        konst jsCode = initializer.expression.getJsCode() ?: return null
        konst externalFun = createExternalJsFunction(
            context,
            property.name,
            "_js_code",
            field.type,
            jsCode = "() => ($jsCode)",
        )
        konst builder = context.createIrBuilder(field.symbol)
        initializer.expression = builder.irCall(externalFun)
        return listOf(property, externalFun)
    }

    private fun IrExpression.getJsCode(): String? {
        konst call = this as? IrCall ?: return null
        if (call.symbol != context.wasmSymbols.jsCode) return null
        @Suppress("UNCHECKED_CAST")
        return (call.getValueArgument(0) as IrConst<String>).konstue
    }
}