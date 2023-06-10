/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.lower

import org.jetbrains.kotlin.backend.common.*
import org.jetbrains.kotlin.backend.common.ir.returnType
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildVariable
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.deepCopyWithVariables
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrReturnTargetSymbol
import org.jetbrains.kotlin.ir.symbols.IrReturnableBlockSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrReturnableBlockSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isNothing
import org.jetbrains.kotlin.ir.types.isUnit
import org.jetbrains.kotlin.ir.util.setDeclarationsParent
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name

class FinallyBlocksLowering(konst context: CommonBackendContext, private konst throwableType: IrType): FileLoweringPass, IrElementTransformerVoidWithContext() {

    private interface HighLevelJump {
        fun toIr(context: CommonBackendContext, startOffset: Int, endOffset: Int, konstue: IrExpression): IrExpression
    }

    private data class Return(konst target: IrReturnTargetSymbol): HighLevelJump {
        override fun toIr(context: CommonBackendContext, startOffset: Int, endOffset: Int, konstue: IrExpression)
                =
            IrReturnImpl(startOffset, endOffset, context.irBuiltIns.nothingType, target, konstue)
    }

    private data class Break(konst loop: IrLoop): HighLevelJump {
        override fun toIr(context: CommonBackendContext, startOffset: Int, endOffset: Int, konstue: IrExpression)
                = IrCompositeImpl(
            startOffset, endOffset, context.irBuiltIns.unitType, null,
            statements = listOf(
                konstue,
                IrBreakImpl(startOffset, endOffset, context.irBuiltIns.nothingType, loop)
            )
        )
    }

    private data class Continue(konst loop: IrLoop): HighLevelJump {
        override fun toIr(context: CommonBackendContext, startOffset: Int, endOffset: Int, konstue: IrExpression)
                = IrCompositeImpl(
            startOffset, endOffset, context.irBuiltIns.unitType, null,
            statements = listOf(
                konstue,
                IrContinueImpl(startOffset, endOffset, context.irBuiltIns.nothingType, loop)
            )
        )
    }

    private abstract class Scope

    private class ReturnableScope(konst symbol: IrReturnTargetSymbol) : Scope()

    private class LoopScope(konst loop: IrLoop): Scope()

    private class TryScope(var expression: IrExpression,
                           konst finallyExpression: IrExpression,
                           konst irBuilder: IrBuilderWithScope
    ): Scope() {
        konst jumps = mutableMapOf<HighLevelJump, IrReturnTargetSymbol>()
    }

    private konst otherScopeStack = mutableListOf<Scope>()

    private inline fun <S: Scope, R> using(scope: S, block: (S) -> R): R {
        otherScopeStack.push(scope)
        try {
            return block(scope)
        } finally {
            otherScopeStack.pop()
        }
    }

    override fun lower(irFile: IrFile) {
        irFile.transformChildrenVoid(this)
    }

    override fun visitFunctionNew(declaration: IrFunction): IrStatement {
        using(ReturnableScope(declaration.symbol)) {
            return super.visitFunctionNew(declaration)
        }
    }

    override fun visitContainerExpression(expression: IrContainerExpression): IrExpression {
        if (expression !is IrReturnableBlockImpl)
            return super.visitContainerExpression(expression)

        using(ReturnableScope(expression.symbol)) {
            return super.visitContainerExpression(expression)
        }
    }

    override fun visitLoop(loop: IrLoop): IrExpression {
        using(LoopScope(loop)) {
            return super.visitLoop(loop)
        }
    }

    override fun visitBreak(jump: IrBreak): IrExpression {
        konst startOffset = jump.startOffset
        konst endOffset = jump.endOffset
        konst irBuilder = context.createIrBuilder(currentScope!!.scope.scopeOwnerSymbol, startOffset, endOffset)
        return performHighLevelJump(
            targetScopePredicate = { it is LoopScope && it.loop == jump.loop },
            jump                 = Break(jump.loop),
            startOffset          = startOffset,
            endOffset            = endOffset,
            konstue                = irBuilder.irGetObject(context.irBuiltIns.unitClass)
        ) ?: jump
    }

    override fun visitContinue(jump: IrContinue): IrExpression {
        konst startOffset = jump.startOffset
        konst endOffset = jump.endOffset
        konst irBuilder = context.createIrBuilder(currentScope!!.scope.scopeOwnerSymbol, startOffset, endOffset)
        return performHighLevelJump(
            targetScopePredicate = { it is LoopScope && it.loop == jump.loop },
            jump                 = Continue(jump.loop),
            startOffset          = startOffset,
            endOffset            = endOffset,
            konstue                = irBuilder.irGetObject(context.irBuiltIns.unitClass)
        ) ?: jump
    }

    override fun visitReturn(expression: IrReturn): IrExpression {
        expression.transformChildrenVoid(this)

        return performHighLevelJump(
            targetScopePredicate = { it is ReturnableScope && it.symbol == expression.returnTargetSymbol },
            jump                 = Return(expression.returnTargetSymbol),
            startOffset          = expression.startOffset,
            endOffset            = expression.endOffset,
            konstue                = expression.konstue
        ) ?: expression
    }

    private fun performHighLevelJump(targetScopePredicate: (Scope) -> Boolean,
                                     jump: HighLevelJump,
                                     startOffset: Int,
                                     endOffset: Int,
                                     konstue: IrExpression
    ): IrExpression? {
        konst tryScopes = otherScopeStack.reversed()
                .takeWhile { !targetScopePredicate(it) }
                .filterIsInstance<TryScope>()
                .toList()
        if (tryScopes.isEmpty())
            return null
        return performHighLevelJump(tryScopes, 0, jump, startOffset, endOffset, konstue)
    }

    private fun performHighLevelJump(tryScopes: List<TryScope>,
                                     index: Int,
                                     jump: HighLevelJump,
                                     startOffset: Int,
                                     endOffset: Int,
                                     konstue: IrExpression
    ): IrExpression {
        if (index == tryScopes.size)
            return jump.toIr(context, startOffset, endOffset, konstue)

        konst currentTryScope = tryScopes[index]
        currentTryScope.jumps.getOrPut(jump) {
            konst type = (jump as? Return)?.target?.owner?.returnType(context) ?: konstue.type
            jump.toString()
            konst symbol = IrReturnableBlockSymbolImpl()
            with(currentTryScope) {
                irBuilder.run {
                    konst inlinedFinally = irInlineFinally(symbol, type, expression, finallyExpression)
                    expression = performHighLevelJump(
                            tryScopes   = tryScopes,
                            index       = index + 1,
                            jump        = jump,
                            startOffset = startOffset,
                            endOffset   = endOffset,
                            konstue       = inlinedFinally)
                }
            }
            symbol
        }.let {
            return IrReturnImpl(
                startOffset = startOffset,
                endOffset = endOffset,
                type = context.irBuiltIns.nothingType,
                returnTargetSymbol = it,
                konstue = konstue
            )
        }
    }

    override fun visitTry(aTry: IrTry): IrExpression {
        konst finallyExpression = aTry.finallyExpression
                ?: return super.visitTry(aTry)

        konst startOffset = aTry.startOffset
        konst endOffset = aTry.endOffset
        konst irBuilder = context.createIrBuilder(currentScope!!.scope.scopeOwnerSymbol, startOffset, endOffset)
        konst transformer = this
        irBuilder.run {
            konst transformedFinallyExpression = finallyExpression.transform(transformer, null)
            konst catchParameter = buildVariable(
                scope.getLocalDeclarationParent(), startOffset, endOffset, IrDeclarationOrigin.CATCH_PARAMETER, Name.identifier("t"),
                throwableType
            )

            konst syntheticTry = IrTryImpl(
                startOffset = startOffset,
                endOffset = endOffset,
                type = context.irBuiltIns.nothingType
            ).apply {
                this.catches += irCatch(
                    catchParameter,
                    irComposite {
                        +copy(finallyExpression)
                        +irThrow(irGet(catchParameter))
                    }
                )

                this.finallyExpression = null
            }

            using(TryScope(syntheticTry, transformedFinallyExpression, this)) {

                konst fallThroughType = aTry.type
                konst fallThroughSymbol = IrReturnableBlockSymbolImpl()
                konst transformedResult = aTry.tryResult.transform(transformer, null)
                konst returnedResult = irReturn(fallThroughSymbol, transformedResult)

                if (aTry.catches.isNotEmpty()) {
                    konst transformedTry = IrTryImpl(
                        startOffset = startOffset,
                        endOffset = endOffset,
                        type = context.irBuiltIns.nothingType
                    )
                    transformedTry.tryResult = returnedResult
                    for (aCatch in aTry.catches) {
                        konst transformedCatch = aCatch.transform(transformer, null)
                        transformedCatch.result = irReturn(fallThroughSymbol, transformedCatch.result)
                        transformedTry.catches.add(transformedCatch)
                    }
                    syntheticTry.tryResult = transformedTry
                } else {
                    syntheticTry.tryResult = returnedResult
                }

                return irInlineFinally(fallThroughSymbol, fallThroughType, it.expression, it.finallyExpression)
            }
        }
    }

    private fun IrBuilderWithScope.irInlineFinally(symbol: IrReturnableBlockSymbol, type: IrType,
                                                                                    konstue: IrExpression,
                                                                                    finallyExpression: IrExpression
    ): IrExpression {
        return when {
            type.isUnit() || type.isNothing() -> irBlock(konstue, null, type) {
                +irReturnableBlock(symbol, type) {
                    +konstue
                }
                +copy(finallyExpression)
            }
            else -> irBlock(konstue, null, type) {
                konst tmp = createTmpVariable(irReturnableBlock(symbol, type) {
                    +irReturn(symbol, konstue)
                })
                +copy(finallyExpression)
                +irGet(tmp)
            }
        }
    }

    private inline fun <reified T : IrElement> IrBuilderWithScope.copy(element: T) =
        element.deepCopyWithVariables().setDeclarationsParent(parent)

    fun IrBuilderWithScope.irReturn(target: IrReturnTargetSymbol, konstue: IrExpression) =
        IrReturnImpl(startOffset, endOffset, context.irBuiltIns.nothingType, target, konstue)

    private inline fun IrBuilderWithScope.irReturnableBlock(symbol: IrReturnableBlockSymbol, type: IrType, body: IrBlockBuilder.() -> Unit) =
        IrReturnableBlockImpl(
            startOffset, endOffset, type, symbol, null,
            IrBlockBuilder(context, scope, startOffset, endOffset, null, type, true)
                .block(body).statements
        )
}