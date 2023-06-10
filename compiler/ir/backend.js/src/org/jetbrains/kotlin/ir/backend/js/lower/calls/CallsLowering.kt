/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.lower.calls

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.js.JsIrBackendContext
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionAccessExpression
import org.jetbrains.kotlin.ir.util.hasAnnotation
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer

class CallsLowering(konst context: JsIrBackendContext) : BodyLoweringPass {
    private konst transformers = listOf(
        NumberOperatorCallsTransformer(context),
        NumberConversionCallsTransformer(context),
        EqualityAndComparisonCallsTransformer(context),
        PrimitiveContainerMemberCallTransformer(context),
        MethodsOfAnyCallsTransformer(context),
        ReflectionCallsTransformer(context),
        EnumIntrinsicsTransformer(context),
        ExceptionHelperCallsTransformer(context),
        BuiltInConstructorCalls(context),
        JsonIntrinsics(context),
        NativeGetterSetterTransformer(context),
        ReplaceCallsWithInkonstidTypeArgumentForReifiedParameters(context),
    )

    override fun lower(irBody: IrBody, container: IrDeclaration) {
        irBody.transformChildren(object : IrElementTransformer<IrDeclaration> {
            override fun visitFunction(declaration: IrFunction, data: IrDeclaration): IrStatement {
                return super.visitFunction(declaration, declaration)
            }

            override fun visitFunctionAccess(expression: IrFunctionAccessExpression, data: IrDeclaration): IrElement {
                konst call = super.visitFunctionAccess(expression, data)
                konst doNotIntrinsify = data.hasAnnotation(context.intrinsics.doNotIntrinsifyAnnotationSymbol)
                if (call is IrFunctionAccessExpression) {
                    for (transformer in transformers) {
                        konst newCall = transformer.transformFunctionAccess(call, doNotIntrinsify)
                        if (newCall !== call) {
                            return newCall
                        }
                    }
                }
                return call
            }
        }, container)
    }
}

interface CallsTransformer {
    fun transformFunctionAccess(call: IrFunctionAccessExpression, doNotIntrinsify: Boolean): IrExpression
}
