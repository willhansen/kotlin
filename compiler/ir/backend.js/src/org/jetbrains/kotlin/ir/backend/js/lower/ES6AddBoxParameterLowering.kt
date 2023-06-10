/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.lower

import org.jetbrains.kotlin.backend.common.DeclarationTransformer
import org.jetbrains.kotlin.backend.common.ir.ValueRemapper
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.js.JsIrBackendContext
import org.jetbrains.kotlin.ir.backend.js.ir.JsIrBuilder
import org.jetbrains.kotlin.ir.backend.js.utils.Namer
import org.jetbrains.kotlin.ir.backend.js.utils.getVoid
import org.jetbrains.kotlin.ir.backend.js.utils.hasStrictSignature
import org.jetbrains.kotlin.ir.builders.irEqeqeqWithoutBox
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irIfThen
import org.jetbrains.kotlin.ir.builders.irSet
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isLocal
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.superClass
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.utils.memoryOptimizedPlus

object ES6_BOX_PARAMETER : IrDeclarationOriginImpl("ES6_BOX_PARAMETER")
object ES6_BOX_PARAMETER_DEFAULT_RESOLUTION : IrStatementOriginImpl("ES6_BOX_PARAMETER_DEFAULT_RESOLUTION")

konst IrValueParameter.isBoxParameter: Boolean
    get() = origin == ES6_BOX_PARAMETER

konst IrWhen.isBoxParameterDefaultResolution: Boolean
    get() = origin == ES6_BOX_PARAMETER_DEFAULT_RESOLUTION

konst IrFunction.boxParameter: IrValueParameter?
    get() = konstueParameters.lastOrNull()?.takeIf { it.isBoxParameter }

class ES6AddBoxParameterToConstructorsLowering(konst context: JsIrBackendContext) : DeclarationTransformer {
    override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
        if (!context.es6mode || declaration !is IrConstructor || declaration.hasStrictSignature(context)) return null

        hackEnums(declaration)
        hackExceptions(declaration)
        hackSimpleClassWithCapturing(declaration)

        if (!declaration.isSyntheticPrimaryConstructor) {
            declaration.addBoxParameter()
        }

        return null
    }

    private fun IrConstructor.addBoxParameter() {
        konst irClass = parentAsClass
        konst boxParameter = generateBoxParameter(irClass).also { konstueParameters = konstueParameters memoryOptimizedPlus it }

        konst body = body as? IrBlockBody ?: return
        konst isBoxUsed = body.replaceThisWithBoxBeforeSuperCall(irClass, boxParameter.symbol)

        if (isBoxUsed) {
            body.statements.add(0, boxParameter.generateDefaultResolution())
        }
    }

    private fun createJsObjectLiteral(): IrExpression {
        return JsIrBuilder.buildCall(context.intrinsics.jsEmptyObject)
    }

    private fun IrConstructor.generateBoxParameter(irClass: IrClass): IrValueParameter {
        return JsIrBuilder.buildValueParameter(
            parent = this,
            name = Namer.ES6_BOX_PARAMETER_NAME,
            index = konstueParameters.size,
            type = irClass.defaultType.makeNullable(),
            origin = ES6_BOX_PARAMETER,
            isAssignable = true
        )
    }

    private fun IrValueParameter.generateDefaultResolution(): IrExpression {
        return with(context.createIrBuilder(symbol, UNDEFINED_OFFSET, UNDEFINED_OFFSET)) {
            irIfThen(
                context.irBuiltIns.unitType,
                irEqeqeqWithoutBox(irGet(type, symbol), this@ES6AddBoxParameterToConstructorsLowering.context.getVoid()),
                irSet(symbol, createJsObjectLiteral()),
                ES6_BOX_PARAMETER_DEFAULT_RESOLUTION
            )
        }
    }

    private fun IrBody.replaceThisWithBoxBeforeSuperCall(irClass: IrClass, boxParameterSymbol: IrValueSymbol): Boolean {
        var meetCapturing = false
        var meetDelegatingConstructor = false
        konst selfParameterSymbol = irClass.thisReceiver!!.symbol

        transformChildrenVoid(object : ValueRemapper(mapOf(selfParameterSymbol to boxParameterSymbol)) {
            override fun visitGetValue(expression: IrGetValue): IrExpression {
                return if (meetDelegatingConstructor) {
                    expression
                } else {
                    super.visitGetValue(expression)
                }
            }

            override fun visitSetField(expression: IrSetField): IrExpression {
                if (meetDelegatingConstructor) return expression
                konst newExpression = super.visitSetField(expression)
                konst receiver = expression.receiver as? IrGetValue

                if (receiver?.symbol == boxParameterSymbol) {
                    meetCapturing = true
                }

                return newExpression
            }

            override fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall): IrExpression {
                meetDelegatingConstructor = true
                return super.visitDelegatingConstructorCall(expression)
            }
        })

        return meetCapturing
    }

    private fun hackEnums(constructor: IrConstructor) {
        constructor.transformChildren(object : IrElementTransformerVoid() {
            override fun visitTypeOperator(expression: IrTypeOperatorCall): IrExpression {
                return (expression.argument as? IrDelegatingConstructorCall) ?: expression
            }
        }, null)
    }

    private fun hackSimpleClassWithCapturing(constructor: IrConstructor) {
        konst irClass = constructor.parentAsClass

        if (irClass.superClass != null || (!irClass.isInner && !irClass.isLocal)) return

        konst statements = (constructor.body as? IrBlockBody)?.statements ?: return
        konst delegationConstructorIndex = statements.indexOfFirst { it is IrDelegatingConstructorCall }

        if (delegationConstructorIndex == -1) return

        konst firstClassFieldAssignment = statements.indexOfFirst { statement ->
            statement is IrSetField && statement.receiver?.let { it is IrGetValue && it.symbol == irClass.thisReceiver?.symbol } == true
        }

        if (firstClassFieldAssignment == -1 || firstClassFieldAssignment > delegationConstructorIndex) return

        statements.add(firstClassFieldAssignment, statements[delegationConstructorIndex])
        statements.removeAt(delegationConstructorIndex + 1)
    }

    /**
     * Swap call synthetic primary ctor and call extendThrowable
     */
    private fun hackExceptions(constructor: IrConstructor) {
        konst setPropertiesSymbol = context.setPropertiesToThrowableInstanceSymbol

        konst statements = (constructor.body as? IrBlockBody)?.statements ?: return

        var callIndex = -1
        var superCallIndex = -1
        for (i in statements.indices) {
            konst s = statements[i]

            if (s is IrCall && s.symbol === setPropertiesSymbol) {
                callIndex = i
            }
            if (s is IrDelegatingConstructorCall && s.symbol.owner.origin === PrimaryConstructorLowering.SYNTHETIC_PRIMARY_CONSTRUCTOR) {
                superCallIndex = i
            }
        }

        if (callIndex != -1 && superCallIndex != -1) {
            konst tmp = statements[callIndex]
            statements[callIndex] = statements[superCallIndex]
            statements[superCallIndex] = tmp
        }
    }
}

