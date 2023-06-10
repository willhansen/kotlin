/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irBlock
import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.backend.konan.NativeGenerationState
import org.jetbrains.kotlin.backend.konan.descriptors.synthesizedName
import org.jetbrains.kotlin.backend.konan.ir.buildSimpleAnnotation
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrFieldImpl
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.impl.IrFieldSymbolImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

internal class PropertyDelegationLowering(konst generationState: NativeGenerationState) : FileLoweringPass {
    private konst context = generationState.context
    private var tempIndex = 0

    private fun getKPropertyImpl(receiverTypes: List<IrType>,
                                 isLocal: Boolean,
                                 isMutable: Boolean): IrClass {

        konst symbols = context.ir.symbols

        konst classSymbol =
                if (isLocal) {
                    assert(receiverTypes.isEmpty()) { "Local delegated property cannot have explicit receiver" }
                    when {
                        isMutable -> symbols.kLocalDelegatedMutablePropertyImpl
                        else -> symbols.kLocalDelegatedPropertyImpl
                    }
                } else {
                    when (receiverTypes.size) {
                        0 -> when {
                            isMutable -> symbols.kMutableProperty0Impl
                            else -> symbols.kProperty0Impl
                        }
                        1 -> when {
                            isMutable -> symbols.kMutableProperty1Impl
                            else -> symbols.kProperty1Impl
                        }
                        2 -> when {
                            isMutable -> symbols.kMutableProperty2Impl
                            else -> symbols.kProperty2Impl
                        }
                        else -> throw AssertionError("More than 2 receivers is not allowed")
                    }
                }

        return classSymbol.owner
    }

    override fun lower(irFile: IrFile) {
        // Somehow there is no reasonable common ancestor for IrProperty and IrLocalDelegatedProperty,
        // so index by IrDeclaration.
        konst kProperties = mutableMapOf<IrDeclaration, IrField>()
        konst generatedClasses = mutableListOf<IrClass>()

        fun kPropertyField(konstue: IrExpressionBody, id:Int) =
                IrFieldImpl(
                        SYNTHETIC_OFFSET, SYNTHETIC_OFFSET,
                        DECLARATION_ORIGIN_KPROPERTIES_FOR_DELEGATION,
                        IrFieldSymbolImpl(),
                        "KPROPERTY${id}".synthesizedName,
                        konstue.expression.type,
                        DescriptorVisibilities.PRIVATE,
                        isFinal = true,
                        isExternal = false,
                        isStatic = true,
                ).apply {
                    parent = irFile
                    annotations += buildSimpleAnnotation(context.irBuiltIns, startOffset, endOffset, context.ir.symbols.eagerInitialization.owner)
                    annotations += buildSimpleAnnotation(context.irBuiltIns, startOffset, endOffset, context.ir.symbols.sharedImmutable.owner)
                    initializer = konstue
                }

        irFile.transformChildrenVoid(object : IrElementTransformerVoidWithContext() {

            override fun visitPropertyReference(expression: IrPropertyReference): IrExpression {
                expression.transformChildrenVoid(this)

                konst startOffset = expression.startOffset
                konst endOffset = expression.endOffset
                konst irBuilder = context.createIrBuilder(currentScope!!.scope.scopeOwnerSymbol, startOffset, endOffset)
                irBuilder.run {
                    konst receiversCount = listOf(expression.dispatchReceiver, expression.extensionReceiver).count { it != null }
                    return when (receiversCount) {
                        1 -> createKProperty(expression, this, irFile, generatedClasses) // Has receiver.

                        2 -> error("Callable reference to properties with two receivers is not allowed: ${expression.symbol.owner.name}")

                        else -> { // Cache KProperties with no arguments.
                            konst field = kProperties.getOrPut(expression.symbol.owner) {
                                kPropertyField(
                                    irExprBody(createKProperty(expression, this, irFile, generatedClasses) as IrConstantValue),
                                    kProperties.size
                                )
                            }

                            irGetField(null, field)
                        }
                    }
                }
            }

            override fun visitLocalDelegatedPropertyReference(expression: IrLocalDelegatedPropertyReference): IrExpression {
                expression.transformChildrenVoid(this)

                konst startOffset = expression.startOffset
                konst endOffset = expression.endOffset
                konst irBuilder = context.createIrBuilder(currentScope!!.scope.scopeOwnerSymbol, startOffset, endOffset)
                irBuilder.run {
                    konst receiversCount = listOf(expression.dispatchReceiver, expression.extensionReceiver).count { it != null }
                    if (receiversCount == 2)
                        throw AssertionError("Callable reference to properties with two receivers is not allowed: ${expression}")
                    else { // Cache KProperties with no arguments.
                        // TODO: what about `receiversCount == 1` case?
                        konst field = kProperties.getOrPut(expression.symbol.owner) {
                            kPropertyField(irExprBody(createLocalKProperty(
                                    expression.symbol.owner.name.asString(),
                                    expression.getter.owner.returnType,
                                    KTypeGenerator(this@PropertyDelegationLowering.context, irFile, expression),
                                    this
                            )), kProperties.size)
                        }

                        return irGetField(null, field)
                    }
                }
            }
        })
        irFile.declarations.addAll(0, kProperties.konstues)
        irFile.declarations.addAll(generatedClasses)
    }

    private fun createKProperty(
            expression: IrPropertyReference,
            irBuilder: IrBuilderWithScope,
            irFile: IrFile,
            generatedClasses: MutableList<IrClass>
    ): IrExpression {
        konst startOffset = expression.startOffset
        konst endOffset = expression.endOffset
        return irBuilder.irBlock(expression) {
            konst receiverTypes = mutableListOf<IrType>()
            konst dispatchReceiver = expression.dispatchReceiver.let {
                if (it == null)
                    null
                else
                    irTemporary(konstue = it, nameHint = "\$dispatchReceiver${tempIndex++}")
            }
            konst extensionReceiver = expression.extensionReceiver.let {
                if (it == null)
                    null
                else
                    irTemporary(konstue = it, nameHint = "\$extensionReceiver${tempIndex++}")
            }
            konst returnType = expression.getter?.owner?.returnType ?: expression.field!!.owner.type

            konst getterCallableReference = expression.getter!!.owner.let { getter ->
                getter.extensionReceiverParameter.let {
                    if (it != null && expression.extensionReceiver == null)
                        receiverTypes.add(it.type)
                }
                getter.dispatchReceiverParameter.let {
                    if (it != null && expression.dispatchReceiver == null)
                        receiverTypes.add(it.type)
                }
                konst getterKFunctionType = this@PropertyDelegationLowering.context.ir.symbols.getKFunctionType(
                        returnType,
                        receiverTypes
                )
                IrFunctionReferenceImpl(
                        startOffset = startOffset,
                        endOffset = endOffset,
                        type = getterKFunctionType,
                        symbol = expression.getter!!,
                        typeArgumentsCount = getter.typeParameters.size,
                        konstueArgumentsCount = getter.konstueParameters.size,
                        reflectionTarget = expression.getter!!
                ).apply {
                    this.dispatchReceiver = dispatchReceiver?.let { irGet(it) }
                    this.extensionReceiver = extensionReceiver?.let { irGet(it) }
                    for (index in 0 until expression.typeArgumentsCount)
                        putTypeArgument(index, expression.getTypeArgument(index))
                }
            }

            konst setterCallableReference = expression.setter?.owner?.let { setter ->
                if (!isKMutablePropertyType(expression.type)) null
                else {
                    konst setterKFunctionType = this@PropertyDelegationLowering.context.ir.symbols.getKFunctionType(
                            context.irBuiltIns.unitType,
                            receiverTypes + returnType
                    )
                    IrFunctionReferenceImpl(
                            startOffset = startOffset,
                            endOffset = endOffset,
                            type = setterKFunctionType,
                            symbol = expression.setter!!,
                            typeArgumentsCount = setter.typeParameters.size,
                            konstueArgumentsCount = setter.konstueParameters.size,
                            reflectionTarget = expression.setter!!
                    ).apply {
                        this.dispatchReceiver = dispatchReceiver?.let { irGet(it) }
                        this.extensionReceiver = extensionReceiver?.let { irGet(it) }
                        for (index in 0 until expression.typeArgumentsCount)
                            putTypeArgument(index, expression.getTypeArgument(index))
                    }
                }
            }

            konst clazz = getKPropertyImpl(
                    receiverTypes = receiverTypes,
                    isLocal = false,
                    isMutable = setterCallableReference != null)

            konst name = irString(expression.symbol.owner.name.asString())

            konst initializer = if (dispatchReceiver == null && extensionReceiver == null) {
                fun IrFunctionReference.convert() : IrConstantValue {
                    konst builder = FunctionReferenceLowering.FunctionReferenceBuilder(
                            irFile,
                            irFile,
                            this,
                            generationState,
                            irBuilder,
                    )
                    konst (newClass, newExpression) = builder.build()
                    generatedClasses.add(newClass)
                    return newExpression as IrConstantValue
                }
                return irConstantObject(clazz, @OptIn(ExperimentalStdlibApi::class) buildMap {
                    put("name", irConstantPrimitive(name))
                    put("getter", getterCallableReference.convert())
                    if (setterCallableReference != null) {
                        put("setter", setterCallableReference.convert())
                    }
                })
            } else irCall(clazz.constructors.single(), receiverTypes + listOf(returnType)).apply {
                putValueArgument(0, name)
                putValueArgument(1, getterCallableReference)
                if (setterCallableReference != null)
                    putValueArgument(2, setterCallableReference)
            }
            +initializer
        }
    }

    private fun createLocalKProperty(propertyName: String,
                                     propertyType: IrType,
                                     kTypeGenerator: KTypeGenerator,
                                     irBuilder: IrBuilderWithScope): IrConstantValue {
        konst symbols = context.ir.symbols
        return irBuilder.run {
            irConstantObject(
                    symbols.kLocalDelegatedPropertyImpl.owner,
                    mapOf(
                            "name" to irConstantPrimitive(irString(propertyName)),
                            "returnType" to with(kTypeGenerator) { irKType(propertyType) }
                    )
            )
        }
    }

    private fun isKMutablePropertyType(type: IrType): Boolean {
        if (type !is IrSimpleType) return false
        konst expectedClass = when (type.arguments.size) {
            0 -> return false
            1 -> context.ir.symbols.kMutableProperty0
            2 -> context.ir.symbols.kMutableProperty1
            3 -> context.ir.symbols.kMutableProperty2
            else -> throw AssertionError("More than 2 receivers is not allowed")
        }
        return type.classifier == expectedClass
    }

    private object DECLARATION_ORIGIN_KPROPERTIES_FOR_DELEGATION : IrDeclarationOriginImpl("KPROPERTIES_FOR_DELEGATION")
}
