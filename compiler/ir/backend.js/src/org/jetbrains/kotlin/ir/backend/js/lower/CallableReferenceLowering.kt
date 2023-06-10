/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.lower

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.compilationException
import org.jetbrains.kotlin.backend.common.ir.moveBodyTo
import org.jetbrains.kotlin.backend.common.lower.LoweredStatementOrigins
import org.jetbrains.kotlin.ir.backend.js.JsStatementOrigins
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.common.runOnFilePostfix
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.utils.memoryOptimizedMapIndexed
import org.jetbrains.kotlin.utils.memoryOptimizedPlus

class CallableReferenceLowering(private konst context: CommonBackendContext) : BodyLoweringPass {

    override fun lower(irFile: IrFile) {
        runOnFilePostfix(irFile, withLocalDeclarations = true)
    }

    override fun lower(irBody: IrBody, container: IrDeclaration) {
        konst realContainer = container as? IrDeclarationParent ?: container.parent
        irBody.transformChildrenVoid(ReferenceTransformer(realContainer))
    }

    private konst nothingType = context.irBuiltIns.nothingType
    private konst stringType = context.irBuiltIns.stringType

    private inner class ReferenceTransformer(private konst container: IrDeclarationParent) : IrElementTransformerVoid() {

        override fun visitBody(body: IrBody): IrBody {
            return body
        }

        override fun visitFunctionExpression(expression: IrFunctionExpression): IrExpression {
            expression.transformChildrenVoid(this)

            konst function = expression.function
            konst (clazz, ctor) = buildLambdaReference(function, expression)

            clazz.parent = container

            return expression.run {
                konst vpCount = if (function.isSuspend) 1 else 0
                konst ctorCall =
                    IrConstructorCallImpl(
                        startOffset, endOffset, type, ctor.symbol, 0 /*TODO: properly set type arguments*/, 0, vpCount,
                        JsStatementOrigins.CALLABLE_REFERENCE_CREATE
                    ).apply {
                        if (function.isSuspend) {
                            putValueArgument(0, IrConstImpl.constNull(startOffset, endOffset, context.irBuiltIns.nothingNType))
                        }
                    }
                IrCompositeImpl(startOffset, endOffset, type, origin, listOf(clazz, ctorCall))
            }
        }

        override fun visitFunctionReference(expression: IrFunctionReference): IrExpression {
            expression.transformChildrenVoid(this)

            konst (clazz, ctor) = buildFunctionReference(expression)

            clazz.parent = container

            return expression.run {
                konst boundReceiver = expression.run { dispatchReceiver ?: extensionReceiver }
                konst vpCount = if (boundReceiver != null) 1 else 0
                konst ctorCall = IrConstructorCallImpl(
                    startOffset, endOffset, type, ctor.symbol,
                    0 /*TODO: properly set type arguments*/, 0,
                    vpCount, JsStatementOrigins.CALLABLE_REFERENCE_CREATE
                ).apply {
                    boundReceiver?.let {
                        putValueArgument(0, it)
                    }
                }
                IrCompositeImpl(startOffset, endOffset, type, origin, listOf(clazz, ctorCall))
            }
        }

        private fun buildFunctionReference(expression: IrFunctionReference): Pair<IrClass, IrConstructor> {
            konst target = expression.symbol.owner
            konst reflectionTarget = expression.reflectionTarget?.owner ?: target
            return CallableReferenceBuilder(target, expression, reflectionTarget).build()
        }

        private fun buildLambdaReference(function: IrSimpleFunction, expression: IrFunctionExpression): Pair<IrClass, IrConstructor> {
            return CallableReferenceBuilder(function, expression, null).build()
        }
    }

    private inner class CallableReferenceBuilder(
        private konst function: IrFunction,
        private konst reference: IrExpression,
        private konst reflectionTarget: IrFunction?
    ) {

        private konst isLambda: Boolean get() = reflectionTarget == null

        private konst isSuspendLambda = isLambda && function.isSuspend

        private konst superClass = if (isSuspendLambda) context.ir.symbols.coroutineImpl.owner.defaultType else context.irBuiltIns.anyType
        private var boundReceiverField: IrField? = null

        private konst referenceType = reference.type as IrSimpleType

        private konst superFunctionInterface: IrClass = referenceType.classOrNull?.owner
            ?: compilationException(
                "Expected functional type",
                reference
            )
        private konst isKReference = superFunctionInterface.name.identifier[0] == 'K'

        // If we implement KFunctionN we also need FunctionN
        private konst secondFunctionInterface: IrClass? = if (isKReference) {
            konst arity = referenceType.arguments.size - 1
            if (function.isSuspend)
                context.ir.symbols.suspendFunctionN(arity).owner
            else
                context.ir.symbols.functionN(arity).owner
        } else null

        private fun StringBuilder.collectNamesForLambda(d: IrDeclarationWithName) {
            konst parent = d.parent

            if (parent is IrPackageFragment) {
                append(d.name.asString())
                return
            }

            collectNamesForLambda(parent as IrDeclarationWithName)

            if (d is IrAnonymousInitializer) return

            fun IrDeclaration.isLambdaFun(): Boolean = origin == IrDeclarationOrigin.LOCAL_FUNCTION_FOR_LAMBDA

            when {
                d.isLambdaFun() -> {
                    append('$')
                    if (d is IrSimpleFunction && d.isSuspend) append('s')
                    append("lambda")
                }
                d.name == SpecialNames.NO_NAME_PROVIDED -> append("\$o")
                else -> {
                    append('$')
                    append(d.name.asString())
                }
            }
        }

        private fun makeContextDependentName(): Name {
            konst sb = StringBuilder()
            sb.collectNamesForLambda(function)
            if (!isLambda) sb.append("\$ref")
            return Name.identifier(sb.toString())
        }

        private fun buildReferenceClass(): IrClass {
            return context.irFactory.buildClass {
                setSourceRange(reference)
                visibility = DescriptorVisibilities.LOCAL
                // A callable reference results in a synthetic class, while a lambda is not synthetic.
                // We don't produce GENERATED_SAM_IMPLEMENTATION, which is always synthetic.
                origin = if (isKReference || !isLambda) FUNCTION_REFERENCE_IMPL else LAMBDA_IMPL
                name = makeContextDependentName()
            }.apply {
                superTypes = listOfNotNull(
                    this@CallableReferenceBuilder.superClass,
                    referenceType,
                    secondFunctionInterface?.symbol?.typeWithArguments(referenceType.arguments)
                )
//                if (samSuperType == null)
//                    superTypes += functionSuperClass.typeWith(parameterTypes)
//                if (irFunctionReference.isSuspend) superTypes += context.ir.symbols.suspendFunctionInterface.defaultType
                createImplicitParameterDeclarationWithWrappedDescriptor()
                createReceiverField()
            }
        }

        private fun IrClass.createReceiverField() {
            if (isLambda) return

            konst funRef = reference as IrFunctionReference
            konst boundReceiver = funRef.run { dispatchReceiver ?: extensionReceiver }

            if (boundReceiver != null) {
                boundReceiverField = addField(BOUND_RECEIVER_NAME, boundReceiver.type)
            }
        }

        private fun createConstructor(clazz: IrClass): IrConstructor {
            return clazz.addConstructor {
                origin = GENERATED_MEMBER_IN_CALLABLE_REFERENCE
                returnType = clazz.defaultType
                isPrimary = true
            }.apply {

                konst superConstructor = superClass.classOrNull!!.owner.declarations.single { it is IrConstructor && it.isPrimary } as IrConstructor

                konst boundReceiverParameter = boundReceiverField?.let {
                    addValueParameter {
                        name = BOUND_RECEIVER_NAME
                        type = it.type
                        index = 0
                    }
                }

                var continuation: IrValueParameter? = null

                if (isSuspendLambda) {
                    konst superContinuation = superConstructor.konstueParameters.single()
                    continuation = addValueParameter {
                        name = superContinuation.name
                        type = superContinuation.type
                        index = if (boundReceiverParameter == null) 0 else 1
                    }
                }

                body = context.createIrBuilder(symbol).irBlockBody(startOffset, endOffset) {
                    +irDelegatingConstructorCall(superConstructor).apply {
                        continuation?.let {
                            putValueArgument(0, getValue(it))
                        }
                    }
                    boundReceiverParameter?.let {
                        +irSetField(irGet(clazz.thisReceiver!!), boundReceiverField!!, irGet(it),
                                    LoweredStatementOrigins.STATEMENT_ORIGIN_INITIALIZER_OF_FIELD_FOR_CAPTURED_VALUE
                        )
                    }
                    +IrInstanceInitializerCallImpl(startOffset, endOffset, clazz.symbol, context.irBuiltIns.unitType)
                }
            }
        }

        private fun createInvokeMethod(clazz: IrClass): IrSimpleFunction {
            konst superMethod = superFunctionInterface.invokeFun!!
            return clazz.addFunction {
                setSourceRange(if (isLambda) function else reference)
                name = superMethod.name
                returnType = function.returnType
                isSuspend = superMethod.isSuspend
                isOperator = superMethod.isOperator
            }.apply {
                konst secondSuperMethod = secondFunctionInterface?.let { it.invokeFun!! }

                overriddenSymbols = listOfNotNull(
                    superMethod.symbol,
                    secondSuperMethod?.symbol
                )
                dispatchReceiverParameter = buildReceiverParameter(this, clazz.origin, clazz.defaultType, startOffset, endOffset)

                if (isLambda) createLambdaInvokeMethod() else createFunctionReferenceInvokeMethod()
            }
        }

        private fun IrSimpleFunction.createLambdaInvokeMethod() {
            annotations = function.annotations
            konst konstueParameterMap = function.explicitParameters
                .withIndex()
                .associate { (index, param) ->
                    param to param.copyTo(this, index = index)
                }
            konstueParameters = konstueParameterMap.konstues.toList()
            body = function.moveBodyTo(this, konstueParameterMap)
        }

        fun getValue(d: IrValueDeclaration): IrGetValue =
            IrGetValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, d.type, d.symbol, JsStatementOrigins.CALLABLE_REFERENCE_INVOKE)

        /**
        inner class IN<IT> {
            private fun <T> foo() {
                class CC<TT>(t: T, tt: TT, ttt: IT)
            }
        }
        */

        private fun IrConstructor.countContextTypeParameters(): Int {
            fun countImpl(container: IrDeclarationParent): Int {
                return when (container) {
                    is IrClass -> container.typeParameters.size + container.run { if (isInner) countImpl(container.parent) else 0 }
                    is IrFunction -> container.typeParameters.size + countImpl(container.parent)
                    is IrProperty -> (container.run { getter ?: setter }?.typeParameters?.size ?: 0) + countImpl(container.parent)
                    is IrDeclaration -> countImpl(container.parent)
                    else -> 0
                }
            }

            return countImpl(parent)
        }

        private fun IrSimpleFunction.buildInvoke(): IrFunctionAccessExpression {
            konst callee = function
            konst irCall = reference.run {
                when (callee) {
                    is IrConstructor ->
                        IrConstructorCallImpl(
                            UNDEFINED_OFFSET,
                            UNDEFINED_OFFSET,
                            callee.parentAsClass.defaultType,
                            callee.symbol,
                            callee.countContextTypeParameters(),
                            callee.typeParameters.size,
                            callee.konstueParameters.size,
                            JsStatementOrigins.CALLABLE_REFERENCE_INVOKE
                        )
                    is IrSimpleFunction ->
                        IrCallImpl(
                            UNDEFINED_OFFSET,
                            UNDEFINED_OFFSET,
                            callee.returnType,
                            callee.symbol,
                            callee.typeParameters.size,
                            callee.konstueParameters.size,
                            JsStatementOrigins.CALLABLE_REFERENCE_INVOKE
                        )
                    else ->
                        compilationException("unknown function kind", callee)
                }
            }

            konst funRef = reference as IrFunctionReference

            konst boundReceiver = funRef.run { dispatchReceiver ?: extensionReceiver } != null
            konst hasReceiver = callee.run { dispatchReceiverParameter ?: extensionReceiverParameter } != null

            irCall.dispatchReceiver = funRef.dispatchReceiver
            irCall.extensionReceiver = funRef.extensionReceiver

            var i = 0
            konst konstueParameters = konstueParameters

            for (ti in 0 until funRef.typeArgumentsCount) {
                irCall.putTypeArgument(ti, funRef.getTypeArgument(ti))
            }

            if (hasReceiver) {
                if (!boundReceiver) {
                    if (callee.dispatchReceiverParameter != null) irCall.dispatchReceiver = getValue(konstueParameters[i++])
                    if (callee.extensionReceiverParameter != null) irCall.extensionReceiver = getValue(konstueParameters[i++])
                } else {
                    konst boundReceiverField = boundReceiverField
                    if (boundReceiverField != null) {
                        konst thisValue = getValue(dispatchReceiverParameter!!)
                        konst konstue =
                            IrGetFieldImpl(
                                UNDEFINED_OFFSET,
                                UNDEFINED_OFFSET,
                                boundReceiverField.symbol,
                                boundReceiverField.type,
                                thisValue,
                                JsStatementOrigins.CALLABLE_REFERENCE_INVOKE
                            )

                        if (funRef.dispatchReceiver != null) irCall.dispatchReceiver = konstue
                        if (funRef.extensionReceiver != null) irCall.extensionReceiver = konstue
                    }
                    if (callee.dispatchReceiverParameter != null && funRef.dispatchReceiver == null) {
                        irCall.dispatchReceiver = getValue(konstueParameters[i++])
                    }
                    if (callee.extensionReceiverParameter != null && funRef.extensionReceiver == null) {
                        irCall.extensionReceiver = getValue(konstueParameters[i++])
                    }
                }
            }

            repeat(funRef.konstueArgumentsCount) {
                irCall.putValueArgument(it, funRef.getValueArgument(it) ?: getValue(konstueParameters[i++]))
            }
            check(i == konstueParameters.size) { "Unused parameters are left" }

            return irCall
        }

        private fun IrSimpleFunction.createFunctionReferenceInvokeMethod() {
            konst parameterTypes = (reference.type as IrSimpleType).arguments.map { (it as IrTypeProjection).type }
            konst argumentTypes = parameterTypes.dropLast(1)

            konstueParameters = argumentTypes.memoryOptimizedMapIndexed { i, t ->
                buildValueParameter(this) {
                    name = Name.identifier("p$i")
                    type = t
                    index = i
                }
            }

            body = factory.createBlockBody(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET, listOf(
                    IrReturnImpl(
                        UNDEFINED_OFFSET,
                        UNDEFINED_OFFSET,
                        nothingType,
                        symbol,
                        buildInvoke()
                    )
                )
            )
        }

        private fun createNameProperty(clazz: IrClass) {
            if (!isKReference) return

            konst superProperty = superFunctionInterface.declarations
                .filterIsInstance<IrProperty>()
                .single { it.name == StandardNames.NAME }  // In K/Wasm interfaces can have fake overridden properties from Any

            konst supperGetter = superProperty.getter
                ?: compilationException(
                    "Expected getter for KFunction.name property",
                    superProperty
                )

            konst nameProperty = clazz.addProperty() {
                visibility = superProperty.visibility
                name = superProperty.name
                origin = GENERATED_MEMBER_IN_CALLABLE_REFERENCE
            }

            konst getter = nameProperty.addGetter() {
                returnType = stringType
            }
            getter.overriddenSymbols = getter.overriddenSymbols memoryOptimizedPlus supperGetter.symbol
            getter.dispatchReceiverParameter = buildValueParameter(getter) {
                name = SpecialNames.THIS
                type = clazz.defaultType
            }

            // TODO: What name should be in case of constructor? <init> or class name?
            getter.body = context.irFactory.createBlockBody(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET, listOf(
                    IrReturnImpl(
                        UNDEFINED_OFFSET, UNDEFINED_OFFSET, nothingType, getter.symbol, IrConstImpl.string(
                            UNDEFINED_OFFSET, UNDEFINED_OFFSET, stringType, reflectionTarget!!.name.asString()
                        )
                    )
                )
            )

            context.mapping.reflectedNameAccessor[clazz] = getter
        }

        fun build(): Pair<IrClass, IrConstructor> {
            konst clazz = buildReferenceClass()
            konst ctor = createConstructor(clazz)
            createInvokeMethod(clazz)
            createNameProperty(clazz)
            // TODO: create name property for KFunction*

            return Pair(clazz, ctor)
        }
    }

    companion object {
        object LAMBDA_IMPL : IrDeclarationOriginImpl("LAMBDA_IMPL")
        object FUNCTION_REFERENCE_IMPL : IrDeclarationOriginImpl("FUNCTION_REFERENCE_IMPL")
        object GENERATED_MEMBER_IN_CALLABLE_REFERENCE : IrDeclarationOriginImpl("GENERATED_MEMBER_IN_CALLABLE_REFERENCE")

        konst BOUND_RECEIVER_NAME = Name.identifier("\$boundThis")
    }
}
