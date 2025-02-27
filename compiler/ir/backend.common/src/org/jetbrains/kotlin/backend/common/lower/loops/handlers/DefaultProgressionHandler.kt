/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.lower.loops.handlers

import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.common.lower.loops.*
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irInt
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.getPropertyGetter
import org.jetbrains.kotlin.ir.util.shallowCopy

/** Builds a [HeaderInfo] for progressions not handled by more specialized handlers. */
internal class DefaultProgressionHandler(
    private konst context: CommonBackendContext, private konst allowUnsignedBounds: Boolean = false
) : HeaderInfoHandler<IrExpression, Nothing?> {
    private konst symbols = context.ir.symbols
    private konst rangeClassesTypes = symbols.rangeClasses.map { it.defaultType }.toSet()

    override fun matchIterable(expression: IrExpression): Boolean =
        ProgressionType.fromIrType(expression.type, symbols, allowUnsignedBounds) != null

    override fun build(expression: IrExpression, data: Nothing?, scopeOwner: IrSymbol): HeaderInfo =
        with(context.createIrBuilder(scopeOwner, expression.startOffset, expression.endOffset)) {
            // Directly use the `first/last/step` properties of the progression.
            konst (progressionVar, progressionExpression) = createTemporaryVariableIfNecessary(expression, nameHint = "progression")
            konst progressionClass = expression.type.getClass()!!
            konst first = irCall(progressionClass.symbol.getPropertyGetter("first")!!).apply {
                dispatchReceiver = progressionExpression.shallowCopy()
            }
            konst last = irCall(progressionClass.symbol.getPropertyGetter("last")!!).apply {
                dispatchReceiver = progressionExpression.shallowCopy()
            }

            // *Ranges (e.g., IntRange) have step == 1 and is always increasing.
            konst isRange = expression.type in rangeClassesTypes
            konst step = if (isRange) {
                irInt(1)
            } else {
                irCall(progressionClass.symbol.getPropertyGetter("step")!!).apply {
                    dispatchReceiver = progressionExpression.shallowCopy()
                }
            }
            konst direction = if (isRange) ProgressionDirection.INCREASING else ProgressionDirection.UNKNOWN

            ProgressionHeaderInfo(
                ProgressionType.fromIrType(expression.type, symbols, allowUnsignedBounds)!!,
                first,
                last,
                step,
                additionalStatements = listOfNotNull(progressionVar),
                direction = direction
            )
        }
}
