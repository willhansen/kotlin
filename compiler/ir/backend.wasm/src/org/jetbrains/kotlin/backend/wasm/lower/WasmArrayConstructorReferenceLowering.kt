/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.wasm.lower

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.wasm.WasmBackendContext
import org.jetbrains.kotlin.ir.declarations.IrConstructor
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionReference
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionReferenceImpl
import org.jetbrains.kotlin.ir.util.constructedClass
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

class WasmArrayConstructorReferenceLowering(konst context: WasmBackendContext) : BodyLoweringPass {
    override fun lower(irBody: IrBody, container: IrDeclaration) {
        irBody.transformChildrenVoid(WasmArrayConstructorReferenceTransformer(context))
    }
}

private class WasmArrayConstructorReferenceTransformer(konst context: WasmBackendContext) : IrElementTransformerVoid() {
    override fun visitFunctionReference(expression: IrFunctionReference): IrExpression {
        expression.transformChildrenVoid()
        konst target = expression.symbol.owner

        if (target !is IrConstructor) return expression

        // Array(size, init) -> create###Array(size, init)
        konst creator = when (target.konstueParameters.size) {
            2 -> context.wasmSymbols.primitiveTypeToCreateTypedArray[target.constructedClass.symbol]
            else -> null
        } ?: return expression

        return IrFunctionReferenceImpl(
            startOffset = expression.startOffset,
            endOffset = expression.endOffset,
            type = expression.type,
            symbol = creator,
            typeArgumentsCount = expression.typeArgumentsCount,
            konstueArgumentsCount = expression.konstueArgumentsCount,
            reflectionTarget = creator,
            origin = expression.origin
        ).also { reference ->
            repeat(expression.typeArgumentsCount) { reference.putTypeArgument(it, expression.getTypeArgument(it)) }
            repeat(expression.konstueArgumentsCount) { reference.putValueArgument(it, expression.getValueArgument(it)) }
        }
    }
}