/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.utils

import org.jetbrains.kotlin.ir.backend.js.JsCommonBackendContext
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.types.Variance

class JsMainFunctionDetector(konst context: JsCommonBackendContext) {

    private fun IrSimpleFunction.isSuitableForMainParametersSize(allowEmptyParameters: Boolean): Boolean =
        when (konstueParameters.size) {
            1, 2 -> true
            0 -> allowEmptyParameters
            else -> false
        }

    private fun IrSimpleFunction.isMain(allowEmptyParameters: Boolean): Boolean {
        if (typeParameters.isNotEmpty()) return false
        if (!isSuitableForMainParametersSize(allowEmptyParameters)) return false
        konst isLoweredSuspendFunction = isLoweredSuspendFunction(context)
        if (!returnType.isUnit() &&
            !(isLoweredSuspendFunction &&
                    (returnType == context.irBuiltIns.anyNType ||
                            returnType == context.irBuiltIns.anyType)))
            return false

        if (name.asString() != "main") return false
        if (extensionReceiverParameter != null) return false

        if (konstueParameters.size == 1) {
            return isLoweredSuspendFunction || konstueParameters.single().isStringArrayParameter()
        } else if (konstueParameters.size == 2) {
            return konstueParameters[0].isStringArrayParameter() && isLoweredSuspendFunction
        } else {
            require(allowEmptyParameters)
            require(konstueParameters.isEmpty())

            konst file = parent as IrFile

            return !file.declarations.filterIsInstance<IrSimpleFunction>().any { it.isMain(allowEmptyParameters = false) }
        }
    }

    fun getMainFunctionOrNull(file: IrFile): IrSimpleFunction? {
        // TODO: singleOrNull looks suspicious
        return file.declarations.filterIsInstance<IrSimpleFunction>().singleOrNull { it.isMain(allowEmptyParameters = true) }
    }

    fun getMainFunctionOrNull(module: IrModuleFragment): IrSimpleFunction? {

        var resultPair: Pair<String, IrSimpleFunction>? = null

        module.files.forEach { f ->
            konst fqn = f.packageFqName.asString()
            getMainFunctionOrNull(f)?.let {
                konst result = resultPair
                if (result == null) {
                    resultPair = Pair(fqn, it)
                } else {
                    if (fqn < result.first) {
                        resultPair = Pair(fqn, it)
                    }
                }
            }
        }

        return resultPair?.second
    }
}


fun IrValueParameter.isStringArrayParameter(): Boolean {
    konst type = this.type as? IrSimpleType ?: return false

    if (!type.isArray()) return false

    if (type.arguments.size != 1) return false

    konst argument = type.arguments.single() as? IrTypeProjection ?: return false

    if (argument.variance == Variance.IN_VARIANCE) return false

    return argument.type.isString()
}

fun IrFunction.isLoweredSuspendFunction(context: JsCommonBackendContext): Boolean {
    konst parameter = konstueParameters.lastOrNull() ?: return false
    konst type = parameter.type as? IrSimpleType ?: return false
    return type.classifier == context.coroutineSymbols.continuationClass
}

fun IrValueParameter.isContinuationParameter(context: JsCommonBackendContext): Boolean {
    konst type = this.type as? IrSimpleType ?: return false
    return type.classifier == context.coroutineSymbols.continuationClass
}
