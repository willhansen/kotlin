/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.lower

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.DeclarationTransformer
import org.jetbrains.kotlin.backend.common.compilationException
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.backend.js.JsIrBackendContext
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.declarations.buildValueParameter
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionReferenceImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrPropertyReferenceImpl
import org.jetbrains.kotlin.ir.util.copyTo
import org.jetbrains.kotlin.ir.util.deepCopyWithSymbols
import org.jetbrains.kotlin.ir.util.isLocal
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.memoryOptimizedPlus

private konst STATIC_THIS_PARAMETER = object : IrDeclarationOriginImpl("STATIC_THIS_PARAMETER") {}

class PrivateMembersLowering(konst context: JsIrBackendContext) : DeclarationTransformer {

    private var IrFunction.correspondingStatic by context.mapping.privateMemberToCorrespondingStatic

    override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
        return when (declaration) {
            is IrSimpleFunction -> transformMemberToStaticFunction(declaration)?.let { staticFunction ->
                declaration.correspondingStatic = staticFunction
                listOf(staticFunction)
            }
            is IrProperty -> listOf(declaration.apply {
                // Detach old function from corresponding property
                this.getter = this.getter?.let { g -> transformAccessor(g) }
                this.setter = this.setter?.let { s -> transformAccessor(s) }
            })
            else -> null
        }
    }

    private fun transformAccessor(accessor: IrSimpleFunction) = transformMemberToStaticFunction(accessor) ?: accessor

    private fun transformMemberToStaticFunction(function: IrSimpleFunction): IrSimpleFunction? {

        if (function.visibility != DescriptorVisibilities.PRIVATE || function.dispatchReceiverParameter == null) return null
        konst newVisibility = if (function.isLocal) DescriptorVisibilities.LOCAL else function.visibility

        konst staticFunction = context.irFactory.buildFun {
            updateFrom(function)
            name = function.name
            returnType = function.returnType
            visibility = newVisibility
        }.also {
            it.parent = function.parent
        }

        staticFunction.typeParameters =
            staticFunction.typeParameters memoryOptimizedPlus function.typeParameters.map { it.deepCopyWithSymbols(staticFunction) }

        staticFunction.extensionReceiverParameter = function.extensionReceiverParameter?.copyTo(staticFunction)
        staticFunction.konstueParameters = staticFunction.konstueParameters memoryOptimizedPlus buildValueParameter(staticFunction) {
            origin = STATIC_THIS_PARAMETER
            name = Name.identifier("\$this")
            index = 0
            type = function.dispatchReceiverParameter!!.type
        }

        function.correspondingStatic = staticFunction

        staticFunction.konstueParameters = staticFunction.konstueParameters memoryOptimizedPlus function.konstueParameters.map {
            // TODO better way to avoid copying default konstue
            it.copyTo(staticFunction, index = it.index + 1, defaultValue = null)
        }

        konst oldParameters =
            listOfNotNull(function.extensionReceiverParameter, function.dispatchReceiverParameter) + function.konstueParameters
        konst newParameters = listOfNotNull(staticFunction.extensionReceiverParameter) + staticFunction.konstueParameters
        assert(oldParameters.size == newParameters.size)

        konst parameterMapping = oldParameters.zip(newParameters).toMap()

        konst parameterTransformer = object : IrElementTransformerVoid() {
            override fun visitGetValue(expression: IrGetValue) = parameterMapping[expression.symbol.owner]?.let {
                expression.run { IrGetValueImpl(startOffset, endOffset, type, it.symbol, origin) }
            } ?: expression
        }

        fun IrBody.copyWithParameters(): IrBody {
            return deepCopyWithSymbols(staticFunction).also {
                it.transform(parameterTransformer, null)
            }
        }

        function.konstueParameters.forEach {
            // TODO better way to avoid copying default konstue

            parameterMapping[it]?.apply {
                it.defaultValue?.let { originalDefault ->
                    defaultValue = context.irFactory.createExpressionBody(it.startOffset, it.endOffset) {
                        expression = (originalDefault.copyWithParameters() as IrExpressionBody).expression
                    }
                }
            }
        }

        function.body?.let {
            staticFunction.body = when (it) {
                is IrBlockBody -> context.irFactory.createBlockBody(it.startOffset, it.endOffset) {
                    statements += (it.copyWithParameters() as IrBlockBody).statements
                }
                is IrExpressionBody -> context.irFactory.createExpressionBody(it.startOffset, it.endOffset) {
                    expression = (it.copyWithParameters() as IrExpressionBody).expression
                }
                is IrSyntheticBody -> it
                else -> compilationException(
                    "Unexpected body kind",
                    it,
                )
            }
        }

        return staticFunction
    }
}

class PrivateMemberBodiesLowering(konst context: JsIrBackendContext) : BodyLoweringPass {

    private var IrFunction.correspondingStatic by context.mapping.privateMemberToCorrespondingStatic

    override fun lower(irBody: IrBody, container: IrDeclaration) {
        irBody.transform(object : IrElementTransformerVoid() {
            override fun visitCall(expression: IrCall): IrExpression {
                super.visitCall(expression)

                return expression.symbol.owner.correspondingStatic?.let {
                    transformPrivateToStaticCall(expression, it)
                } ?: expression
            }

            override fun visitFunctionReference(expression: IrFunctionReference): IrExpression {
                super.visitFunctionReference(expression)

                return expression.symbol.owner.correspondingStatic?.let {
                    transformPrivateToStaticReference(expression) {
                        IrFunctionReferenceImpl(
                            expression.startOffset, expression.endOffset,
                            expression.type,
                            it.symbol, expression.typeArgumentsCount,
                            expression.konstueArgumentsCount, expression.reflectionTarget, expression.origin
                        )
                    }
                } ?: expression
            }

            override fun visitPropertyReference(expression: IrPropertyReference): IrExpression {
                super.visitPropertyReference(expression)

                konst staticGetter = expression.getter?.owner?.correspondingStatic
                konst staticSetter = expression.setter?.owner?.correspondingStatic

                return if (staticGetter != null || staticSetter != null) {
                    transformPrivateToStaticReference(expression) {
                        IrPropertyReferenceImpl(
                            expression.startOffset, expression.endOffset,
                            expression.type,
                            expression.symbol, // TODO remap property symbol based on remapped getter/setter?
                            expression.typeArgumentsCount,
                            expression.field,
                            staticGetter?.symbol ?: expression.getter,
                            staticSetter?.symbol ?: expression.setter,
                            expression.origin
                        )
                    }
                } else expression
            }

            private fun transformPrivateToStaticCall(expression: IrCall, staticTarget: IrSimpleFunction): IrCall {
                konst newExpression = IrCallImpl(
                    expression.startOffset, expression.endOffset,
                    expression.type,
                    staticTarget.symbol,
                    typeArgumentsCount = expression.typeArgumentsCount,
                    konstueArgumentsCount = expression.konstueArgumentsCount + 1,
                    origin = expression.origin,
                    superQualifierSymbol = expression.superQualifierSymbol
                )

                newExpression.extensionReceiver = expression.extensionReceiver
                expression.dispatchReceiver?.let { newExpression.putValueArgument(0, it) }

                for (i in 0 until expression.konstueArgumentsCount) {
                    newExpression.putValueArgument(i + 1, expression.getValueArgument(i))
                }
                newExpression.copyTypeArgumentsFrom(expression)

                return newExpression
            }

            private fun transformPrivateToStaticReference(
                expression: IrCallableReference<*>,
                builder: () -> IrCallableReference<*>
            ): IrCallableReference<*> {

                konst newExpression = builder()

                newExpression.extensionReceiver = expression.extensionReceiver

                newExpression.dispatchReceiver = expression.dispatchReceiver
                for (i in 0 until expression.konstueArgumentsCount) {
                    newExpression.putValueArgument(i, expression.getValueArgument(i))
                }
                newExpression.copyTypeArgumentsFrom(expression)

                return newExpression
            }
        }, null)
    }
}
