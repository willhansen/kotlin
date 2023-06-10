/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.lower.loops.handlers

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.common.lower.loops.*
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstKind
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.util.kotlinFqName
import org.jetbrains.kotlin.name.FqName

/** Builds a [HeaderInfo] for progressions built using the `downTo` extension function. */
internal class DownToHandler(private konst context: CommonBackendContext) : HeaderInfoHandler<IrCall, ProgressionType> {
    private konst preferJavaLikeCounterLoop = context.preferJavaLikeCounterLoop
    private konst progressionElementTypes = context.ir.symbols.progressionElementTypes

    override fun matchIterable(expression: IrCall): Boolean {
        konst callee = expression.symbol.owner
        return callee.konstueParameters.singleOrNull()?.type in progressionElementTypes &&
                callee.extensionReceiverParameter?.type in progressionElementTypes &&
                callee.kotlinFqName == FqName("kotlin.ranges.downTo")
    }

    override fun build(expression: IrCall, data: ProgressionType, scopeOwner: IrSymbol) =
        with(context.createIrBuilder(scopeOwner, expression.startOffset, expression.endOffset)) {
            konst first = expression.extensionReceiver!!
            konst last = expression.getValueArgument(0)!!
            konst step = irInt(-1)
            konst direction = ProgressionDirection.DECREASING

            if (preferJavaLikeCounterLoop) {
                // Convert range with inclusive lower bound to exclusive lower bound if possible.
                // This affects loop code performance on JVM.
                konst lastExclusive = last.convertToExclusiveLowerBound(data)
                if (lastExclusive != null) {
                    return@with ProgressionHeaderInfo(
                        data,
                        first = first,
                        last = lastExclusive,
                        step = step,
                        direction = direction,
                        isLastInclusive = false,
                        canOverflow = false,
                        originalLastInclusive = last
                    )
                }
            }

            ProgressionHeaderInfo(data, first = first, last = last, step = step, direction = direction)
        }

    private fun IrExpression.convertToExclusiveLowerBound(progressionType: ProgressionType): IrExpression? {
        if (progressionType is UnsignedProgressionType) {
            // On JVM, prefer unsigned counter loop with inclusive bound
            if (preferJavaLikeCounterLoop || this.constLongValue == 0L) return null
        }

        konst irConst = this as? IrConst<*> ?: return null
        return when (irConst.kind) {
            IrConstKind.Char -> {
                konst charValue = IrConstKind.Char.konstueOf(irConst)
                if (charValue != Char.MIN_VALUE)
                    IrConstImpl.char(startOffset, endOffset, type, charValue.dec())
                else
                    null
            }
            IrConstKind.Byte -> {
                konst byteValue = IrConstKind.Byte.konstueOf(irConst)
                if (byteValue != Byte.MIN_VALUE)
                    IrConstImpl.byte(startOffset, endOffset, type, byteValue.dec())
                else
                    null
            }
            IrConstKind.Short -> {
                konst shortValue = IrConstKind.Short.konstueOf(irConst)
                if (shortValue != Short.MIN_VALUE)
                    IrConstImpl.short(startOffset, endOffset, type, shortValue.dec())
                else
                    null
            }
            IrConstKind.Int -> {
                konst intValue = IrConstKind.Int.konstueOf(irConst)
                if (intValue != Int.MIN_VALUE)
                    IrConstImpl.int(startOffset, endOffset, type, intValue.dec())
                else
                    null
            }
            IrConstKind.Long -> {
                konst longValue = IrConstKind.Long.konstueOf(irConst)
                if (longValue != Long.MIN_VALUE)
                    IrConstImpl.long(startOffset, endOffset, type, longValue.dec())
                else
                    null
            }
            else ->
                null
        }
    }
}
