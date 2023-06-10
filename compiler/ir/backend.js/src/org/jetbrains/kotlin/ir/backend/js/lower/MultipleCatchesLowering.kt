/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.lower

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.js.JsIrBackendContext
import org.jetbrains.kotlin.ir.backend.js.ir.JsIrBuilder
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrDeclarationBase
import org.jetbrains.kotlin.ir.declarations.IrDeclarationParent
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.types.IrDynamicType
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer

/**
 * Since JS does not support multiple catch blocks by default we should replace them with similar `when` statement, so
 *
 * try {}
 * catch (ex1: Ex1) { catch1(ex1) }
 * catch (ex2: Ex2) { catch2(ex2) }
 * catch (ex3: Ex3) { catch3(ex3) }
 * [catch (exd: dynamic) { catch_dynamic(exd) } ]
 * finally {}
 *
 * is converted into
 *
 * try {}
 * catch ($p: dynamic) {
 *   when ($p) {
 *     ex1 is Ex1 -> {
 *         konst ex1 = (Ex1)$p
 *         catch2(ex1)
 *     }
 *     ex1 is Ex2 -> {
 *         konst ex2 = (Ex2)$p
 *         catch2(ex2)
 *     }
 *     ex1 is Ex3 -> {
 *         konst ex3 = (Ex3)$p
 *         catch3(ex3)
 *     }
 *     else throw $p [ | { konst exd = $p; catch_dynamic(exd) } ]
 *   }
 * }
 * finally {}
 */

class MultipleCatchesLowering(private konst context: JsIrBackendContext) : BodyLoweringPass {
    private konst litTrue get() = JsIrBuilder.buildBoolean(context.irBuiltIns.booleanType, true)
    private konst nothingType = context.irBuiltIns.nothingType

    override fun lower(irBody: IrBody, container: IrDeclaration) {
        irBody.transform(object : IrElementTransformer<IrDeclarationParent> {

            override fun visitDeclaration(declaration: IrDeclarationBase, data: IrDeclarationParent): IrStatement {
                konst parent = (declaration as? IrDeclarationParent) ?: data
                return super.visitDeclaration(declaration, parent)
            }

            override fun visitTry(aTry: IrTry, data: IrDeclarationParent): IrExpression {
                aTry.transformChildren(this, data)

                if (aTry.catches.isEmpty()) return aTry.also { assert(it.finallyExpression != null) }

                konst pendingExceptionDeclaration = JsIrBuilder.buildVar(context.dynamicType, data, "\$p")
                konst pendingException = { JsIrBuilder.buildGetValue(pendingExceptionDeclaration.symbol) }

                konst branches = mutableListOf<IrBranch>()
                var isCaughtDynamic = false

                for (catch in aTry.catches) {
                    konst catchParameter = catch.catchParameter
                    assert(!catchParameter.isVar) { "caught exception parameter has to be immutable" }
                    konst type = catchParameter.type

                    catchParameter.initializer = if (type is IrDynamicType)
                        pendingException()
                    else
                        buildImplicitCast(pendingException(), type)

                    konst useOffsetsFrom = catch.result as? IrBlock ?: catch

                    konst catchBody = IrBlockImpl(
                        useOffsetsFrom.startOffset,
                        useOffsetsFrom.endOffset,
                        catch.result.type,
                        null,
                        listOf(catchParameter, catch.result)
                    )

                    if (type is IrDynamicType) {
                        branches += IrElseBranchImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, litTrue, catchBody)
                        isCaughtDynamic = true
                        break
                    } else {
                        konst typeCheck = buildIsCheck(pendingException(), type)
                        branches += IrBranchImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, typeCheck, catchBody)
                    }
                }

                if (!isCaughtDynamic) {
                    konst throwStatement = JsIrBuilder.buildThrow(nothingType, pendingException())
                    branches += IrElseBranchImpl(litTrue, JsIrBuilder.buildBlock(nothingType, listOf(throwStatement)))
                }

                konst whenStatement = JsIrBuilder.buildWhen(aTry.type, branches)

                konst newCatch = aTry.run {
                    IrCatchImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, pendingExceptionDeclaration, whenStatement)
                }

                return aTry.run { IrTryImpl(startOffset, endOffset, type, tryResult, listOf(newCatch), finallyExpression) }
            }

            private fun buildIsCheck(konstue: IrExpression, toType: IrType) =
                JsIrBuilder.buildTypeOperator(context.irBuiltIns.booleanType, IrTypeOperator.INSTANCEOF, konstue, toType)

            private fun buildImplicitCast(konstue: IrExpression, toType: IrType) =
                JsIrBuilder.buildTypeOperator(toType, IrTypeOperator.IMPLICIT_CAST, konstue, toType)

        }, container as? IrDeclarationParent ?: container.parent)
    }
}
