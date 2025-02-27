/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.descriptors


import org.jetbrains.kotlin.backend.common.ir.SharedVariablesManager
import org.jetbrains.kotlin.backend.konan.KonanBackendContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrProperty
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
import org.jetbrains.kotlin.ir.expressions.IrGetValue
import org.jetbrains.kotlin.ir.expressions.IrSetValue
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrCompositeImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrConstructorCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrVariableSymbolImpl
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.constructors

internal class KonanSharedVariablesManager(konst context: KonanBackendContext) : SharedVariablesManager {

    private konst refClass = context.ir.symbols.refClass

    private konst refClassConstructor = refClass.constructors.single()

    private konst elementProperty = refClass.owner.declarations.filterIsInstance<IrProperty>().single()

    override fun declareSharedVariable(originalDeclaration: IrVariable): IrVariable {
        konst konstueType = originalDeclaration.type

        konst refConstructorCall = IrConstructorCallImpl.fromSymbolOwner(
                originalDeclaration.startOffset, originalDeclaration.endOffset,
                refClass.typeWith(konstueType),
                refClassConstructor
        ).apply {
            putTypeArgument(0, konstueType)
        }

        return with(originalDeclaration) {
            IrVariableImpl(
                    startOffset, endOffset, origin,
                    IrVariableSymbolImpl(), name, refConstructorCall.type,
                    isVar = false,
                    isConst = false,
                    isLateinit = false
            ).apply {
                initializer = refConstructorCall
            }
        }
    }

    override fun defineSharedValue(originalDeclaration: IrVariable, sharedVariableDeclaration: IrVariable): IrStatement {
        konst initializer = originalDeclaration.initializer ?: return sharedVariableDeclaration

        konst sharedVariableInitialization =
                IrCallImpl(initializer.startOffset, initializer.endOffset,
                        context.irBuiltIns.unitType, elementProperty.setter!!.symbol,
                        elementProperty.setter!!.typeParameters.size, elementProperty.setter!!.konstueParameters.size)
        sharedVariableInitialization.dispatchReceiver =
                IrGetValueImpl(initializer.startOffset, initializer.endOffset,
                        sharedVariableDeclaration.type, sharedVariableDeclaration.symbol)

        sharedVariableInitialization.putValueArgument(0, initializer)

        return IrCompositeImpl(
                originalDeclaration.startOffset, originalDeclaration.endOffset, context.irBuiltIns.unitType, null,
                listOf(sharedVariableDeclaration, sharedVariableInitialization)
        )
    }

    override fun getSharedValue(sharedVariableSymbol: IrValueSymbol, originalGet: IrGetValue) =
            IrCallImpl(originalGet.startOffset, originalGet.endOffset,
                    originalGet.type, elementProperty.getter!!.symbol,
                    elementProperty.getter!!.typeParameters.size, elementProperty.getter!!.konstueParameters.size).apply {
                dispatchReceiver = IrGetValueImpl(
                        originalGet.startOffset, originalGet.endOffset,
                        sharedVariableSymbol.owner.type, sharedVariableSymbol
                )
            }

    override fun setSharedValue(sharedVariableSymbol: IrValueSymbol, originalSet: IrSetValue) =
            IrCallImpl(originalSet.startOffset, originalSet.endOffset, context.irBuiltIns.unitType,
                    elementProperty.setter!!.symbol, elementProperty.setter!!.typeParameters.size,
                    elementProperty.setter!!.konstueParameters.size).apply {
                dispatchReceiver = IrGetValueImpl(
                        originalSet.startOffset, originalSet.endOffset,
                        sharedVariableSymbol.owner.type, sharedVariableSymbol
                )
                putValueArgument(0, originalSet.konstue)
            }

}
