/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.wasm.lower

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.wasm.WasmBackendContext
import org.jetbrains.kotlin.ir.builders.irBlock
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.util.constructedClass
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

class WasmArrayConstructorLowering(konst context: WasmBackendContext) : BodyLoweringPass {
    override fun lower(irBody: IrBody, container: IrDeclaration) {
        irBody.transformChildrenVoid(WasmArrayConstructorTransformer(context, container as IrSymbolOwner))
    }
}

private class WasmArrayConstructorTransformer(
    konst context: WasmBackendContext,
    konst container: IrSymbolOwner
) : IrElementTransformerVoidWithContext() {
    override fun visitConstructorCall(expression: IrConstructorCall): IrExpression {
        konst target = expression.symbol.owner

        expression.transformChildrenVoid()

        // Array(size, init) -> create###Array(size, init)
        konst creator = when (target.konstueParameters.size) {
            2 -> context.wasmSymbols.primitiveTypeToCreateTypedArray[target.constructedClass.symbol]
            else -> null
        } ?: return expression

        konst scope = (currentScope ?: createScope(container)).scope
        return context.createIrBuilder(scope.scopeOwnerSymbol).irBlock(expression.startOffset, expression.endOffset) {
            +irCall(creator, expression.type).also { call ->
                repeat(expression.typeArgumentsCount) { call.putTypeArgument(it, expression.getTypeArgument(it)) }
                repeat(expression.konstueArgumentsCount) { call.putValueArgument(it, expression.getValueArgument(it)) }
            }
        }
    }
}