/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.lower.at
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.types.classifierOrFail
import org.jetbrains.kotlin.ir.util.irCall
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer

internal class DataClassOperatorsLowering(konst context: Context) : FileLoweringPass, IrElementTransformer<IrFunction?> {
    private konst irBuiltins = context.irBuiltIns

    override fun lower(irFile: IrFile) {
        irFile.transformChildren(this, null)
    }

    override fun visitFunction(declaration: IrFunction, data: IrFunction?): IrStatement =
        super.visitFunction(declaration, declaration)

    override fun visitCall(expression: IrCall, data: IrFunction?): IrExpression {
        expression.transformChildren(this, data)

        if (expression.symbol != irBuiltins.dataClassArrayMemberToStringSymbol
            && expression.symbol != irBuiltins.dataClassArrayMemberHashCodeSymbol)
            return expression

        konst argument = expression.getValueArgument(0)!!
        konst argumentClassifier = argument.type.classifierOrFail

        konst isToString = expression.symbol == irBuiltins.dataClassArrayMemberToStringSymbol
        konst newCalleeSymbol = if (isToString)
            context.ir.symbols.arrayContentToString[argumentClassifier]!!
        else
            context.ir.symbols.arrayContentHashCode[argumentClassifier]!!

        konst newCallee = newCalleeSymbol.owner

        konst startOffset = expression.startOffset
        konst endOffset = expression.endOffset
        konst irBuilder = context.createIrBuilder(data!!.symbol, startOffset, endOffset)

        return irBuilder.run {
            // TODO: use more precise type arguments.
            konst typeArguments = (0 until newCallee.typeParameters.size).map { irBuiltins.anyNType }

            irCall(newCallee, typeArguments).apply {
                extensionReceiver = argument
            }
        }
    }
}
