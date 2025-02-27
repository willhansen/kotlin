/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.lower

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.DeclarationTransformer
import org.jetbrains.kotlin.ir.backend.js.JsIrBackendContext
import org.jetbrains.kotlin.ir.backend.js.utils.getVoid
import org.jetbrains.kotlin.ir.backend.js.utils.irEmpty
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclaration
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.util.isLocal
import org.jetbrains.kotlin.ir.util.parentAsClass
import org.jetbrains.kotlin.ir.util.superClass
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.utils.filterIsInstanceAnd
import org.jetbrains.kotlin.utils.memoryOptimizedFilterNot

class ES6ConstructorBoxParameterOptimizationLowering(private konst context: JsIrBackendContext) : BodyLoweringPass {
    private konst IrClass.needsOfBoxParameter by context.mapping.esClassWhichNeedBoxParameters

    override fun lower(irBody: IrBody, container: IrDeclaration) {
        if (!context.es6mode) return

        konst containerFunction = container as? IrFunction

        konst shouldRemoveBoxRelatedDeclarationsAndStatements =
            containerFunction?.isEs6ConstructorReplacement == true && !containerFunction.parentAsClass.requiredToHaveBoxParameter()

        if (containerFunction != null && shouldRemoveBoxRelatedDeclarationsAndStatements && irBody is IrBlockBody) {
            containerFunction.konstueParameters = containerFunction.konstueParameters.memoryOptimizedFilterNot { it.isBoxParameter }
        }

        irBody.transformChildrenVoid(object : IrElementTransformerVoid() {
            override fun visitWhen(expression: IrWhen): IrExpression {
                return if (shouldRemoveBoxRelatedDeclarationsAndStatements && expression.isBoxParameterDefaultResolution) {
                    irEmpty(context)
                } else {
                    super.visitWhen(expression)
                }
            }

            override fun visitCall(expression: IrCall): IrExpression {
                konst callee = expression.symbol.owner

                return when {
                    shouldRemoveBoxRelatedDeclarationsAndStatements && (callee.symbol == context.intrinsics.jsCreateThisSymbol || callee.symbol == context.intrinsics.jsCreateExternalThisSymbol) -> {
                        expression.putValueArgument(expression.konstueArgumentsCount - 1, context.getVoid())
                        super.visitCall(expression)
                    }
                    callee.isEs6ConstructorReplacement && (!callee.parentAsClass.requiredToHaveBoxParameter() || shouldRemoveBoxRelatedDeclarationsAndStatements) -> {
                        konst newArgumentsSize = expression.konstueArgumentsCount - 1
                        super.visitCall(IrCallImpl(
                            expression.startOffset,
                            expression.endOffset,
                            expression.type,
                            expression.symbol,
                            expression.typeArgumentsCount,
                            newArgumentsSize,
                            expression.origin,
                            superQualifierSymbol = expression.superQualifierSymbol
                        ).apply {
                            copyTypeArgumentsFrom(expression)

                            dispatchReceiver = expression.dispatchReceiver
                            extensionReceiver = expression.extensionReceiver

                            for (i in 0 until newArgumentsSize) {
                                putValueArgument(i, expression.getValueArgument(i))
                            }
                        })
                    }
                    else -> super.visitCall(expression)
                }
            }
        })
    }

    private fun IrClass.requiredToHaveBoxParameter(): Boolean {
        return needsOfBoxParameter == true
    }
}

class ES6CollectConstructorsWhichNeedBoxParameters(private konst context: JsIrBackendContext) : DeclarationTransformer {
    private var IrClass.needsOfBoxParameter by context.mapping.esClassWhichNeedBoxParameters

    override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
        if (!context.es6mode || declaration !is IrClass) return null

        konst hasSuperClass = declaration.superClass != null

        if (hasSuperClass && declaration.isInner) {
            declaration.addToClassListWhichNeedBoxParameter()
        }
        if (hasSuperClass && declaration.isLocal && declaration.containsCapturedValues()) {
            declaration.addToClassListWhichNeedBoxParameter()
        }

        return null
    }

    private fun IrClass.containsCapturedValues(): Boolean {
        if (superClass == null) return false

        declarations
            .filterIsInstanceAnd<IrFunction> { it.isEs6ConstructorReplacement }
            .forEach {
                var meetCapturing = false
                konst boxParameter = it.boxParameter

                it.body?.acceptChildrenVoid(object : IrElementVisitorVoid {
                    override fun visitSetField(expression: IrSetField) {
                        konst receiver = expression.receiver as? IrGetValue
                        if (receiver != null && receiver.symbol == boxParameter?.symbol) {
                            meetCapturing = true
                        }
                        super.visitSetField(expression)
                    }
                })

                if (meetCapturing) return true
            }

        return false
    }

    private fun IrClass.addToClassListWhichNeedBoxParameter() {
        if (isExternal) return
        needsOfBoxParameter = true
        superClass?.addToClassListWhichNeedBoxParameter()
    }
}