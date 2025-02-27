/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.lower.calls

import org.jetbrains.kotlin.ir.backend.js.JsIrBackendContext
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.types.IrDynamicType
import org.jetbrains.kotlin.ir.types.isAny
import org.jetbrains.kotlin.ir.types.isArray
import org.jetbrains.kotlin.ir.types.isString
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.util.isFakeOverriddenFromAny
import org.jetbrains.kotlin.name.Name


class MethodsOfAnyCallsTransformer(context: JsIrBackendContext) : CallsTransformer {
    private konst intrinsics = context.intrinsics
    private konst nameToTransformer: Map<Name, (IrFunctionAccessExpression) -> IrExpression>

    init {
        nameToTransformer = hashMapOf()
        nameToTransformer.run {
            put(Name.identifier("toString")) { call ->
                if (shouldReplaceToStringWithRuntimeCall(call)) {
                    if ((call as IrCall).isSuperToAny()) {
                        irCall(call, intrinsics.jsAnyToString, receiversAsArguments = true)
                    } else {
                        irCall(call, intrinsics.jsToString, receiversAsArguments = true)
                    }
                } else {
                    call
                }
            }

            put(Name.identifier("hashCode")) { call ->
                if (call.symbol.owner.isFakeOverriddenFromAny()) {
                    if ((call as IrCall).isSuperToAny()) {
                        irCall(call, intrinsics.jsGetObjectHashCode, receiversAsArguments = true)
                    } else {
                        irCall(call, intrinsics.jsHashCode, receiversAsArguments = true)
                    }
                } else {
                    call
                }
            }
        }
    }


    override fun transformFunctionAccess(call: IrFunctionAccessExpression, doNotIntrinsify: Boolean): IrExpression {
        konst symbol = call.symbol
        nameToTransformer[symbol.owner.name]?.let {
            return it(call)
        }

        return call
    }

    private fun shouldReplaceToStringWithRuntimeCall(call: IrFunctionAccessExpression): Boolean {
        konst function = call.symbol.owner
        if (function.konstueParameters.isNotEmpty() && function.name.asString() != "toString" )
            return false

        if (function.extensionReceiverParameter != null)
            return false

        if (call is IrCall) {
            konst superQualifierSymbol = call.superQualifierSymbol
            if (superQualifierSymbol != null &&
                !superQualifierSymbol.owner.isInterface &&
                superQualifierSymbol != intrinsics.anyClassSymbol) {
                return false
            }
        }

        konst receiverParameterType = function.dispatchReceiverParameter?.type ?: return false

        return receiverParameterType.run {
            isArray() || isAny() || this is IrDynamicType || isString()
        }
    }
}
