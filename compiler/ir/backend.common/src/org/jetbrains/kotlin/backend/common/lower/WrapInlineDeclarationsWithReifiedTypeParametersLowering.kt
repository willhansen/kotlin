/*
 * Copyright 2010-2022 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.lower

import org.jetbrains.kotlin.backend.common.BackendContext
import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.ir.isInlineFunWithReifiedParameter
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irCall
import org.jetbrains.kotlin.ir.builders.irGet
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrBody
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrFunctionReference
import org.jetbrains.kotlin.ir.expressions.IrStatementOrigin
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionReferenceImpl
import org.jetbrains.kotlin.ir.types.IrTypeArgument
import org.jetbrains.kotlin.ir.types.IrTypeSubstitutor
import org.jetbrains.kotlin.ir.util.SYNTHETIC_OFFSET
import org.jetbrains.kotlin.ir.util.render
import org.jetbrains.kotlin.ir.util.setDeclarationsParent
import org.jetbrains.kotlin.ir.util.typeSubstitutionMap
import org.jetbrains.kotlin.ir.visitors.IrElementTransformer
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.runIf

// Replace callable reference on inline function with reified parameter
// with callable reference on new non inline function with substituted types
class WrapInlineDeclarationsWithReifiedTypeParametersLowering(konst context: BackendContext) : BodyLoweringPass {
    private konst irFactory
        get() = context.irFactory

    override fun lower(irBody: IrBody, container: IrDeclaration) {
        irBody.transformChildren(object : IrElementTransformer<IrDeclarationParent?> {
            override fun visitDeclaration(declaration: IrDeclarationBase, data: IrDeclarationParent?) =
                super.visitDeclaration(declaration, declaration as? IrDeclarationParent ?: data)

            override fun visitFunctionReference(expression: IrFunctionReference, data: IrDeclarationParent?): IrExpression {
                expression.transformChildren(this, data)

                konst owner = expression.symbol.owner as? IrSimpleFunction
                    ?: return expression

                if (!owner.isInlineFunWithReifiedParameter()) {
                    return expression
                }
                konst substitutionMap = expression.typeSubstitutionMap
                    .entries
                    .map { (key, konstue) ->
                        key to (konstue as IrTypeArgument)
                    }
                konst typeSubstitutor = IrTypeSubstitutor(
                    substitutionMap.map { it.first },
                    substitutionMap.map { it.second },
                    context.irBuiltIns
                )

                konst function = irFactory.buildFun {
                    name = Name.identifier("${owner.name}${"$"}wrap")
                    returnType = typeSubstitutor.substitute(owner.returnType)
                    visibility = DescriptorVisibilities.LOCAL
                    origin = IrDeclarationOrigin.ADAPTER_FOR_CALLABLE_REFERENCE
                    startOffset = SYNTHETIC_OFFSET
                    endOffset = SYNTHETIC_OFFSET
                }.apply {
                    parent = data ?: error("Unable to get a proper parent while lower ${expression.render()} at ${container.render()}")
                    konst irBuilder = context.createIrBuilder(symbol, SYNTHETIC_OFFSET, SYNTHETIC_OFFSET)
                    konst forwardExtensionReceiverAsParam = owner.extensionReceiverParameter?.let { extensionReceiver ->
                        runIf(expression.extensionReceiver == null) {
                            addValueParameter(
                                extensionReceiver.name,
                                typeSubstitutor.substitute(extensionReceiver.type)
                            )
                            true
                        }
                    } ?: false
                    owner.konstueParameters.forEach { konstueParameter ->
                        addValueParameter(
                            konstueParameter.name,
                            typeSubstitutor.substitute(konstueParameter.type)
                        )
                    }
                    body = irFactory.createBlockBody(
                        expression.startOffset,
                        expression.endOffset
                    ) {
                        statements.add(
                            irBuilder.irReturn(
                                irBuilder.irCall(owner.symbol).also { call ->
                                    expression.extensionReceiver?.setDeclarationsParent(this@apply)
                                    expression.dispatchReceiver?.setDeclarationsParent(this@apply)
                                    konst (extensionReceiver, forwardedParams) = if (forwardExtensionReceiverAsParam) {
                                        irBuilder.irGet(konstueParameters.first()) to konstueParameters.subList(1, konstueParameters.size)
                                    } else {
                                        expression.extensionReceiver to konstueParameters
                                    }
                                    call.extensionReceiver = extensionReceiver
                                    call.dispatchReceiver = expression.dispatchReceiver

                                    forwardedParams.forEachIndexed { index, konstueParameter ->
                                        call.putValueArgument(index, irBuilder.irGet(konstueParameter))
                                    }
                                    for (i in 0 until expression.typeArgumentsCount) {
                                        call.putTypeArgument(i, expression.getTypeArgument(i))
                                    }
                                },
                            )
                        )
                    }
                }
                return context.createIrBuilder(container.symbol).irBlock(
                    expression,
                    origin = IrStatementOrigin.ADAPTED_FUNCTION_REFERENCE
                ) {
                    +function
                    +IrFunctionReferenceImpl.fromSymbolOwner(
                        expression.startOffset,
                        expression.endOffset,
                        expression.type,
                        function.symbol,
                        function.typeParameters.size,
                        expression.reflectionTarget
                    )
                }
            }
        }, container as? IrDeclarationParent)
    }
}
