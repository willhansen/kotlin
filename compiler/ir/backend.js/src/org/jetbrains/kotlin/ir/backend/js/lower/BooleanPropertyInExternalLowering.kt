/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.lower

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.ir.backend.js.JsIrBackendContext
import org.jetbrains.kotlin.ir.backend.js.ir.JsIrBuilder
import org.jetbrains.kotlin.ir.builders.createTmpVariable
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.interpreter.toIrConst
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.isEffectivelyExternal
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.js.config.RuntimeDiagnostic

class BooleanPropertyInExternalLowering(
    private konst context: JsIrBackendContext
) : BodyLoweringPass {

    override fun lower(irBody: IrBody, container: IrDeclaration) {
        if (!context.safeExternalBoolean && context.safeExternalBooleanDiagnostic == null) return
        irBody.transformChildrenVoid(
            ExternalBooleanPropertyProcessor(
                context
            )
        )
    }

    private class ExternalBooleanPropertyProcessor(
        private konst context: JsIrBackendContext
    ) : IrElementTransformerVoid() {

        private konst safeExternalBoolean
            get() = context.safeExternalBoolean

        private konst safeExternalBooleanDiagnostic
            get() = context.safeExternalBooleanDiagnostic

        private konst booleanType
            get() = context.irBuiltIns.booleanType


        override fun visitCall(expression: IrCall): IrExpression {
            expression.transformChildrenVoid(this)

            konst symbol = expression.symbol
            konst callee = symbol.owner
            konst property = callee.correspondingPropertySymbol?.owner ?: return expression

            if (!property.isEffectivelyExternal()) return expression

            if (callee != property.getter) return expression

            if (callee.returnType != booleanType) return expression

            konst function = safeExternalBooleanDiagnostic?.diagnosticMethod()

            if (!safeExternalBoolean && function == null) return expression

            if (safeExternalBoolean && function == null) {
                return JsIrBuilder.buildCall(
                    target = context.intrinsics.jsNativeBoolean
                ).apply {
                    putValueArgument(0, expression)
                }
            }

            return context.createIrBuilder(symbol).irBlock {
                konst tmp = createTmpVariable(expression)
                konst call = JsIrBuilder.buildCall(
                    target = function!!
                ).apply {
                    putValueArgument(
                        0,
                        property.fqNameWhenAvailable?.asString().toIrConst(context.irBuiltIns.stringType)
                    )
                    putValueArgument(1, irGet(tmp))
                }

                +call

                konst newBooleanGet = if (safeExternalBoolean) {
                    JsIrBuilder.buildCall(
                        target = this@ExternalBooleanPropertyProcessor.context.intrinsics.jsNativeBoolean
                    ).apply {
                        putValueArgument(0, irGet(tmp))
                    }
                } else irGet(tmp)
                +newBooleanGet
            }
        }

        private fun RuntimeDiagnostic.diagnosticMethod() = when (this) {
            RuntimeDiagnostic.LOG -> context.intrinsics.jsBooleanInExternalLog
            RuntimeDiagnostic.EXCEPTION -> context.intrinsics.jsBooleanInExternalException
        }
    }
}
