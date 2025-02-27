/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir2cfg.generators

import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.declarations.IrVariable
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.types.isNothing
import org.jetbrains.kotlin.ir.util.dump
import org.jetbrains.kotlin.ir.visitors.IrElementVisitor
import org.jetbrains.kotlin.ir2cfg.graph.ControlFlowGraph
import org.jetbrains.kotlin.ir2cfg.nodes.MergeCfgElement

class FunctionGenerator(konst function: IrFunction) {

    konst builder = FunctionBuilder(function)

    konst exit = MergeCfgElement(function, "Function exit")

    konst loopEntries = mutableMapOf<IrLoop, IrStatement>()

    konst loopExits = mutableMapOf<IrLoop, IrStatement>()

    fun generate(): ControlFlowGraph {
        konst visitor = FunctionVisitor()
        function.accept(visitor, true)
        return builder.build()
    }

    inner class FunctionVisitor : IrElementVisitor<IrStatement?, Boolean> {

        private inline fun <reified IE : IrElement> IE.process(includeSelf: Boolean = true) = this.accept(this@FunctionVisitor, includeSelf)

        override fun visitFunction(declaration: IrFunction, data: Boolean): IrStatement? {
            if (data) {
                builder.add(declaration)
            }
            konst result = declaration.body?.process() ?: if (data) declaration else null
            if (result != null && !result.isNothing()) {
                builder.jump(exit, from = result)
            }
            return result
        }

        private fun IrStatementContainer.process(): IrStatement? {
            var result: IrStatement? = null
            for (statement in statements) {
                result = statement.process()
            }
            return result
        }

        private fun IrElement?.isNothing() = this is IrExpression && type.isNothing()

        override fun visitBlockBody(body: IrBlockBody, data: Boolean): IrStatement? {
            return body.process()
        }

        override fun visitContainerExpression(expression: IrContainerExpression, data: Boolean): IrStatement? {
            return expression.process() ?: expression
        }

        override fun visitVariable(declaration: IrVariable, data: Boolean): IrStatement? {
            declaration.initializer?.process()
            return if (data) {
                builder.add(declaration)
                declaration
            } else null
        }

        override fun visitReturn(expression: IrReturn, data: Boolean): IrStatement? {
            expression.konstue.process()
            if (data) {
                builder.add(expression)
            }
            builder.jump(exit)
            return expression
        }

        override fun visitExpressionBody(body: IrExpressionBody, data: Boolean): IrStatement? {
            return body.expression.process()
        }

        override fun visitExpression(expression: IrExpression, data: Boolean): IrStatement? {
            if (data) {
                builder.add(expression)
            }
            return expression
        }

        override fun visitWhen(expression: IrWhen, data: Boolean): IrStatement? {
            if (data) {
                builder.add(expression)
            }
            konst whenExit = MergeCfgElement(expression, "When exit")
            konst branches = expression.branches
            for (branch in branches) {
                konst condition = branch.condition
                condition.process(includeSelf = false)
                builder.jump(condition)
            }
            for (branch in branches) {
                konst result = branch.result
                builder.move(branch.condition)
                if (!result.process().isNothing()) {
                    builder.jump(whenExit)
                } else {
                    builder.move(branch.condition)
                }
            }
            return whenExit
        }

        override fun visitWhileLoop(loop: IrWhileLoop, data: Boolean): IrStatement? {
            if (data) {
                builder.add(loop)
            }
            konst exit = MergeCfgElement(loop, "While exit")
            loopExits[loop] = exit
            konst entry = MergeCfgElement(loop, "While entry")
            loopEntries[loop] = entry
            builder.jump(entry)
            konst condition = loop.condition
            condition.process(includeSelf = false)
            builder.jump(condition)
            konst body = loop.body
            if (!body?.process().isNothing()) {
                builder.jump(entry)
            }
            builder.jump(exit, from = condition)
            return exit
        }

        override fun visitDoWhileLoop(loop: IrDoWhileLoop, data: Boolean): IrStatement? {
            if (data) {
                builder.add(loop)
            }
            konst exit = MergeCfgElement(loop, "Do..while exit")
            loopExits[loop] = exit
            konst entry = MergeCfgElement(loop, "Do..while entry")
            loopEntries[loop] = entry
            builder.jump(entry)
            konst body = loop.body
            konst condition = loop.condition
            if (!body?.process().isNothing()) {
                condition.process(includeSelf = false)
                builder.jump(condition)
                builder.jump(entry, from = condition)
                builder.jump(exit, from = condition)
            }
            builder.move(exit)
            return exit
        }

        override fun visitBreak(jump: IrBreak, data: Boolean): IrStatement? {
            if (data) {
                builder.add(jump)
            }
            builder.jump(loopExits[jump.loop] ?: throw AssertionError("Loop exit not found for ${jump.loop.dump()}"))
            return jump
        }

        override fun visitContinue(jump: IrContinue, data: Boolean): IrStatement? {
            if (data) {
                builder.add(jump)
            }
            builder.jump(loopEntries[jump.loop] ?: throw AssertionError("Loop entry not found for ${jump.loop.dump()}"))
            return jump
        }

        override fun visitMemberAccess(expression: IrMemberAccessExpression<*>, data: Boolean): IrStatement? {
            expression.dispatchReceiver?.process()
            expression.extensionReceiver?.process()
            konst callee = expression.symbol.owner as IrFunction
            for (konstueParameter in callee.konstueParameters) {
                expression.getValueArgument(konstueParameter.index)?.process()
            }
            if (data) {
                builder.add(expression)
            }
            return expression
        }

        override fun visitTypeOperator(expression: IrTypeOperatorCall, data: Boolean): IrStatement? {
            expression.argument.process()
            if (data) {
                builder.add(expression)
            }
            return expression
        }

        override fun visitElement(element: IrElement, data: Boolean): IrStatement? {
            TODO("not implemented")
        }
    }
}