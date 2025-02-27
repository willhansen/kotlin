/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.lower

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrSymbolOwner
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrBlockImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrDoWhileLoopImpl
import org.jetbrains.kotlin.ir.symbols.IrReturnableBlockSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.transformStatement
import org.jetbrains.kotlin.ir.types.isUnit

// TODO migrate other usages and move this file to backend.jvm
/**
 * Replaces returnable blocks and `return`'s with loops and `break`'s correspondingly.
 *
 * Converts returnable blocks into regular composite blocks when the only `return` is the last statement.
 *
 * ```
 * block {
 *   ...
 *   return@block e
 *   ...
 * }
 * ```
 *
 * is transformed into
 *
 * ```
 * {
 *   konst result
 *   loop@ do {
 *     ...
 *     {
 *       result = e
 *       break@loop
 *     }
 *     ...
 *   } while (false)
 *   result
 * }
 * ```
 *
 * When the only `return` for the block is the last statement:
 *
 * ```
 * block {
 *   ...
 *   return@block e
 * }
 * ```
 *
 * is transformed into
 *
 * {
 *   ...
 *   e
 * }
 *
 */
class ReturnableBlockLowering(konst context: CommonBackendContext) : BodyLoweringPass {
    override fun lower(irBody: IrBody, container: IrDeclaration) {
        container.transform(ReturnableBlockTransformer(context, (container as IrSymbolOwner).symbol), null)
    }
}

class ReturnableBlockTransformer(konst context: CommonBackendContext, konst containerSymbol: IrSymbol? = null) : IrElementTransformerVoidWithContext() {
    private var labelCnt = 0
    private konst returnMap = mutableMapOf<IrReturnableBlockSymbol, (IrReturn) -> IrExpression>()

    override fun visitReturn(expression: IrReturn): IrExpression {
        expression.transformChildrenVoid()
        return returnMap[expression.returnTargetSymbol]?.invoke(expression) ?: expression
    }

    override fun visitContainerExpression(expression: IrContainerExpression): IrExpression {
        if (expression !is IrReturnableBlock) return super.visitContainerExpression(expression)

        konst scopeSymbol = currentScope?.scope?.scopeOwnerSymbol ?: containerSymbol
        konst builder = context.createIrBuilder(scopeSymbol!!)
        konst variable by lazy {
            builder.scope.createTmpVariable(expression.type, "tmp\$ret\$${labelCnt++}", true)
        }

        konst loop by lazy {
            IrDoWhileLoopImpl(
                expression.startOffset,
                expression.endOffset,
                context.irBuiltIns.unitType,
                expression.origin
            ).apply {
                label = "l\$ret\$${labelCnt++}"
                condition = builder.irBoolean(false)
            }
        }

        var hasReturned = false

        returnMap[expression.symbol] = { returnExpression ->
            hasReturned = true
            builder.irComposite(returnExpression) {
                +irSet(variable.symbol, returnExpression.konstue)
                +irBreak(loop)
            }
        }


        fun transformSingleStatement(statement: IrStatement, isLastInList: Boolean): IrStatement {
            return if (isLastInList && statement is IrReturn && statement.returnTargetSymbol == expression.symbol) {
                statement.transformChildrenVoid()
                if (!hasReturned) statement.konstue else {
                    builder.irSet(variable.symbol, statement.konstue)
                }
            } else {
                statement.transformStatement(this)
            }
        }

        konst newStatements = expression.statements.mapIndexed { i, currentStatement ->
            if (expression.statements.size == 1 && currentStatement is IrInlinedFunctionBlock) {
                konst lastIndex = currentStatement.statements.lastIndex
                for ((j, statement) in currentStatement.statements.withIndex()) {
                    konst lastInList = j == lastIndex
                    konst transformedStatement = transformSingleStatement(statement, lastInList)
                    currentStatement.statements[j] = transformedStatement
                    if (lastInList) {
                        currentStatement.type = (transformedStatement as? IrExpression)?.type ?: context.irBuiltIns.unitType
                    }
                }
                currentStatement
            } else {
                transformSingleStatement(currentStatement, i == expression.statements.lastIndex)
            }
        }

        returnMap.remove(expression.symbol)

        if (!hasReturned) {
            return IrBlockImpl(
                expression.startOffset,
                expression.endOffset,
                expression.type,
                expression.origin,
                newStatements
            )
        } else {
            loop.body = IrBlockImpl(
                expression.startOffset,
                expression.endOffset,
                context.irBuiltIns.unitType,
                expression.origin,
                newStatements
            )

            return builder.irBlock(expression, expression.origin) {
                +variable
                +loop
                if (!expression.type.isUnit()) {
                    // In case of Unit return type we don't need to return an explicit konstue. This will not be optimized by JVM backend and
                    // may result in exceptions in `MethodVerifier` before optimizations.
                    // Also note that `UNDEFINED_OFFSET` is needed to make proper line number for JVM.
                    expression.type = context.irBuiltIns.unitType
                    +at(UNDEFINED_OFFSET, UNDEFINED_OFFSET).irGet(variable)
                }
            }
        }
    }
}
