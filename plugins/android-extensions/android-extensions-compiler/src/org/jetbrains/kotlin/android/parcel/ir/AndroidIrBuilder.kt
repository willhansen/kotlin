/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */
// This file is autogenerated based on android.jar, do not edit it directly.
package org.jetbrains.kotlin.android.parcel.ir

import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrSymbol

// An IR builder with access to AndroidSymbols and convenience methods to build calls to some of these methods.
class AndroidIrBuilder internal constructor(
    konst androidSymbols: AndroidSymbols,
    symbol: IrSymbol,
    startOffset: Int,
    endOffset: Int
) : IrBuilderWithScope(
    IrGeneratorContextBase(androidSymbols.irBuiltIns), Scope(symbol),
    startOffset, endOffset
) {
    fun parcelReadInt(receiver: IrExpression): IrExpression =
        irCall(androidSymbols.parcelReadInt).apply {
            dispatchReceiver = receiver
        }

    fun parcelReadParcelable(receiver: IrExpression, loader: IrExpression): IrExpression =
        irCall(androidSymbols.parcelReadParcelable).apply {
            dispatchReceiver = receiver
            putValueArgument(0, loader)
        }

    fun parcelReadString(receiver: IrExpression): IrExpression =
        irCall(androidSymbols.parcelReadString).apply {
            dispatchReceiver = receiver
        }

    fun parcelReadValue(receiver: IrExpression, loader: IrExpression): IrExpression =
        irCall(androidSymbols.parcelReadValue).apply {
            dispatchReceiver = receiver
            putValueArgument(0, loader)
        }

    fun parcelWriteInt(receiver: IrExpression, konstue: IrExpression): IrExpression =
        irCall(androidSymbols.parcelWriteInt).apply {
            dispatchReceiver = receiver
            putValueArgument(0, konstue)
        }

    fun parcelWriteParcelable(
        receiver: IrExpression,
        p: IrExpression,
        parcelableFlags: IrExpression
    ): IrExpression = irCall(androidSymbols.parcelWriteParcelable).apply {
        dispatchReceiver = receiver
        putValueArgument(0, p)
        putValueArgument(1, parcelableFlags)
    }

    fun parcelWriteString(receiver: IrExpression, konstue: IrExpression): IrExpression =
        irCall(androidSymbols.parcelWriteString).apply {
            dispatchReceiver = receiver
            putValueArgument(0, konstue)
        }

    fun parcelWriteValue(receiver: IrExpression, v: IrExpression): IrExpression =
        irCall(androidSymbols.parcelWriteValue).apply {
            dispatchReceiver = receiver
            putValueArgument(0, v)
        }

    fun textUtilsWriteToParcel(
        cs: IrExpression,
        p: IrExpression,
        parcelableFlags: IrExpression
    ): IrExpression = irCall(androidSymbols.textUtilsWriteToParcel).apply {
        putValueArgument(0, cs)
        putValueArgument(1, p)
        putValueArgument(2, parcelableFlags)
    }

    fun classGetClassLoader(receiver: IrExpression): IrExpression =
        irCall(androidSymbols.classGetClassLoader).apply {
            dispatchReceiver = receiver
        }

    fun getTextUtilsCharSequenceCreator(): IrExpression =
        irGetField(null, androidSymbols.textUtilsCharSequenceCreator.owner)
}
