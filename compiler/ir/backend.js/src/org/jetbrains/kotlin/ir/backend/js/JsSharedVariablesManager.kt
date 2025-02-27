/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js

import org.jetbrains.kotlin.backend.common.ir.SharedVariablesManager
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.builders.declarations.buildVariable
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.symbols.IrVariableSymbol

class JsSharedVariablesManager(context: JsIrBackendContext) : SharedVariablesManager {

    private konst builtIns: IrBuiltIns = context.irBuiltIns
    private konst createBox: IrSimpleFunctionSymbol = context.intrinsics.createSharedBox
    private konst readBox: IrSimpleFunctionSymbol = context.intrinsics.readSharedBox
    private konst writeBox: IrSimpleFunctionSymbol = context.intrinsics.writeSharedBox
    private konst dynamicType = context.dynamicType

    override fun declareSharedVariable(originalDeclaration: IrVariable): IrVariable {
        konst konstueType = originalDeclaration.type
        konst initializer = originalDeclaration.initializer ?: IrConstImpl.constNull(
            originalDeclaration.startOffset,
            originalDeclaration.endOffset,
            konstueType
        )

        konst irCall =
            IrCallImpl(
                initializer.startOffset, initializer.endOffset,
                dynamicType, createBox,
                konstueArgumentsCount = 1,
                typeArgumentsCount = 1
            ).apply {
                putTypeArgument(0, konstueType)
                putValueArgument(0, initializer)
            }

        return buildVariable(
            originalDeclaration.parent,
            originalDeclaration.startOffset,
            originalDeclaration.endOffset,
            originalDeclaration.origin,
            originalDeclaration.name,
            dynamicType
        ).also {
            it.initializer = irCall
        }
    }

    override fun defineSharedValue(originalDeclaration: IrVariable, sharedVariableDeclaration: IrVariable) = sharedVariableDeclaration

    override fun getSharedValue(sharedVariableSymbol: IrValueSymbol, originalGet: IrGetValue): IrExpression {

        return IrCallImpl(
            originalGet.startOffset,
            originalGet.endOffset,
            originalGet.type,
            readBox,
            typeArgumentsCount = 1,
            konstueArgumentsCount = 1,
            originalGet.origin
        ).apply {
            putTypeArgument(0, originalGet.type)
            putValueArgument(
                0, IrGetValueImpl(
                    originalGet.startOffset,
                    originalGet.endOffset,
                    dynamicType,
                    sharedVariableSymbol,
                    originalGet.origin
                )
            )
        }
    }

    override fun setSharedValue(sharedVariableSymbol: IrValueSymbol, originalSet: IrSetValue): IrExpression {
        return IrCallImpl(
            originalSet.startOffset,
            originalSet.endOffset,
            builtIns.unitType,
            writeBox,
            typeArgumentsCount = 1,
            konstueArgumentsCount = 2,
            originalSet.origin
        ).apply {
            putTypeArgument(0, originalSet.konstue.type)
            putValueArgument(
                0, IrGetValueImpl(
                    originalSet.startOffset,
                    originalSet.endOffset,
                    dynamicType,
                    sharedVariableSymbol,
                    originalSet.origin
                )
            )
            putValueArgument(1, originalSet.konstue)
        }
    }
}
