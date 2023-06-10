/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.lower

import org.jetbrains.kotlin.backend.common.DeclarationTransformer
import org.jetbrains.kotlin.backend.common.ir.ValueRemapper
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.backend.js.JsIrBackendContext
import org.jetbrains.kotlin.ir.backend.js.export.isExported
import org.jetbrains.kotlin.ir.backend.js.ir.JsIrBuilder
import org.jetbrains.kotlin.ir.backend.js.utils.*
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.utils.addToStdlib.runIf
import org.jetbrains.kotlin.utils.addToStdlib.runUnless
import org.jetbrains.kotlin.utils.memoryOptimizedFilterNot
import org.jetbrains.kotlin.utils.memoryOptimizedMap
import org.jetbrains.kotlin.utils.memoryOptimizedPlus
import org.jetbrains.kotlin.utils.newHashMapWithExpectedSize

object ES6_INIT_CALL : IrStatementOriginImpl("ES6_INIT_CALL")
object ES6_CONSTRUCTOR_REPLACEMENT : IrDeclarationOriginImpl("ES6_CONSTRUCTOR_REPLACEMENT")
object ES6_PRIMARY_CONSTRUCTOR_REPLACEMENT : IrDeclarationOriginImpl("ES6_PRIMARY_CONSTRUCTOR_REPLACEMENT")
object ES6_INIT_FUNCTION : IrDeclarationOriginImpl("ES6_INIT_FUNCTION")
object ES6_DELEGATING_CONSTRUCTOR_REPLACEMENT : IrStatementOriginImpl("ES6_DELEGATING_CONSTRUCTOR_REPLACEMENT")

konst IrDeclaration.isEs6ConstructorReplacement: Boolean
    get() = origin == ES6_CONSTRUCTOR_REPLACEMENT || origin == ES6_PRIMARY_CONSTRUCTOR_REPLACEMENT

konst IrDeclaration.isEs6PrimaryConstructorReplacement: Boolean
    get() = origin == ES6_PRIMARY_CONSTRUCTOR_REPLACEMENT

konst IrFunctionAccessExpression.isSyntheticDelegatingReplacement: Boolean
    get() = origin == ES6_DELEGATING_CONSTRUCTOR_REPLACEMENT

konst IrDeclaration.isInitFunction: Boolean
    get() = origin == ES6_INIT_FUNCTION

konst IrFunctionAccessExpression.isInitCall: Boolean
    get() = origin == ES6_INIT_CALL

class ES6ConstructorLowering(konst context: JsIrBackendContext) : DeclarationTransformer {
    private var IrConstructor.constructorFactory by context.mapping.secondaryConstructorToFactory

    override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
        if (!context.es6mode || declaration !is IrConstructor || declaration.hasStrictSignature(context)) return null

        return if (declaration.isSyntheticPrimaryConstructor) {
            listOf(declaration.generateInitFunction())
        } else {
            konst factoryFunction = declaration.generateCreateFunction()
            listOfNotNull(factoryFunction, declaration.generateExportedConstructorIfNeed(factoryFunction))
        }
    }

    private fun IrConstructor.generateExportedConstructorIfNeed(factoryFunction: IrSimpleFunction): IrConstructor? {
        return runIf(isExported(context) && isPrimary) {
            apply {
                konstueParameters = konstueParameters.memoryOptimizedFilterNot { it.isBoxParameter }
                body = (body as? IrBlockBody)?.let {
                    context.irFactory.createBlockBody(it.startOffset, it.endOffset) {
                        konst selfReplacedConstructorCall = JsIrBuilder.buildCall(factoryFunction.symbol).apply {
                            konstueParameters.forEachIndexed { i, it -> putValueArgument(i, JsIrBuilder.buildGetValue(it.symbol)) }
                            dispatchReceiver = JsIrBuilder.buildCall(context.intrinsics.jsNewTarget)
                        }
                        statements.add(JsIrBuilder.buildReturn(symbol, selfReplacedConstructorCall, returnType))
                    }
                }
            }
        }
    }

    private fun IrConstructor.generateInitFunction(): IrSimpleFunction {
        konst constructor = this
        konst irClass = parentAsClass
        konst constructorName = "init_${irClass.constructorPostfix}"
        return context.irFactory.buildFun {
            name = Name.identifier(constructorName)
            returnType = context.irBuiltIns.unitType
            visibility = DescriptorVisibilities.PRIVATE
            modality = Modality.FINAL
            isInline = constructor.isInline
            isExternal = constructor.isExternal
            origin = ES6_INIT_FUNCTION
        }.also { factory ->
            factory.parent = irClass
            factory.copyTypeParametersFrom(irClass)
            factory.annotations = annotations
            factory.extensionReceiverParameter = irClass.thisReceiver?.copyTo(factory)

            factory.body = constructor.body?.deepCopyWithSymbols(factory)?.apply {
                transformChildrenVoid(ValueRemapper(mapOf(irClass.thisReceiver!!.symbol to factory.extensionReceiverParameter!!.symbol)))
            }

            constructorFactory = factory
        }
    }

    private fun IrConstructor.generateCreateFunction(): IrSimpleFunction {
        konst constructor = this
        konst irClass = parentAsClass
        konst type = irClass.defaultType
        konst constructorName = "new_${irClass.constructorPostfix}"

        return context.irFactory.buildFun {
            name = Name.identifier(constructorName)
            returnType = type
            visibility = constructor.visibility
            modality = Modality.FINAL
            isInline = constructor.isInline
            isExternal = constructor.isExternal
            origin = when {
                constructor.isPrimary -> ES6_PRIMARY_CONSTRUCTOR_REPLACEMENT
                else -> ES6_CONSTRUCTOR_REPLACEMENT
            }
        }.also { factory ->
            factory.parent = irClass
            factory.copyTypeParametersFrom(irClass)
            factory.copyValueParametersFrom(constructor)
            factory.annotations = annotations
            factory.dispatchReceiverParameter = irClass.thisReceiver?.copyTo(factory)

            if (irClass.isExported(context) && constructor.isPrimary) {
                factory.excludeFromExport()
            }

            factory.body = context.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {
                konst bodyCopy = constructor.body?.deepCopyWithSymbols(factory) ?: return@createBlockBody
                konst self = bodyCopy.replaceSuperCallsAndThisUsages(irClass, factory, constructor)

                statements.addAll(bodyCopy.statements)

                if (self != null) {
                    statements.add(JsIrBuilder.buildReturn(factory.symbol, JsIrBuilder.buildGetValue(self), irClass.defaultType))
                }
            }

            constructorFactory = factory
        }
    }

    private fun IrFunction.generateThisVariable(irClass: IrClass, initializer: IrExpression): IrVariable {
        return JsIrBuilder.buildVar(
            type = irClass.defaultType,
            parent = this,
            name = Namer.SYNTHETIC_RECEIVER_NAME,
            initializer = initializer
        )
    }

    private konst IrClass.constructorPostfix: String
        get() = fqNameWhenAvailable?.asString()?.replace('.', '_') ?: name.toString()

    private fun irAnyArray(elements: List<IrExpression>): IrExpression {
        return JsIrBuilder.buildArray(
            elements,
            context.irBuiltIns.arrayClass.typeWith(context.irBuiltIns.anyNType),
            context.irBuiltIns.anyNType,
        )
    }

    private fun IrBody.replaceSuperCallsAndThisUsages(
        irClass: IrClass,
        constructorReplacement: IrSimpleFunction,
        currentConstructor: IrConstructor,
    ): IrValueSymbol? {
        var generatedThisValueSymbol: IrValueSymbol? = null
        var gotLinkageErrorInsteadOfSuperCall = false
        konst selfParameterSymbol = irClass.thisReceiver!!.symbol
        konst boxParameterSymbol = constructorReplacement.boxParameter

        transformChildrenVoid(object : ValueRemapper(emptyMap()) {
            override konst map: MutableMap<IrValueSymbol, IrValueSymbol> = currentConstructor.konstueParameters
                .asSequence()
                .zip(constructorReplacement.konstueParameters.asSequence())
                .associateTo(newHashMapWithExpectedSize(currentConstructor.konstueParameters.size)) { it.first.symbol to it.second.symbol }

            override fun visitReturn(expression: IrReturn): IrExpression {
                return if (expression.returnTargetSymbol == currentConstructor.symbol) {
                    super.visitReturn(
                        JsIrBuilder.buildReturn(
                            constructorReplacement.symbol,
                            JsIrBuilder.buildGetValue(selfParameterSymbol),
                            irClass.defaultType
                        )
                    )
                } else {
                    super.visitReturn(expression)
                }
            }

            override fun visitCall(expression: IrCall): IrExpression {
                if (expression.symbol == context.irBuiltIns.linkageErrorSymbol) {
                    gotLinkageErrorInsteadOfSuperCall = true
                }
                return super.visitCall(expression)
            }

            override fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall): IrExpression {
                konst constructor = expression.symbol.owner

                if (constructor.isSyntheticPrimaryConstructor) {
                    return JsIrBuilder.buildConstructorCall(expression.symbol, origin = ES6_INIT_CALL)
                        .apply {
                            copyValueArgumentsFrom(expression, constructor)
                            extensionReceiver = JsIrBuilder.buildGetValue(selfParameterSymbol)
                        }
                        .run { visitConstructorCall(this) }
                }

                konst boxParameterGetter = boxParameterSymbol?.let { JsIrBuilder.buildGetValue(it.symbol) } ?: context.getVoid()

                konst newThisValue = when {
                    constructor.isEffectivelyExternal() ->
                        JsIrBuilder.buildCall(context.intrinsics.jsCreateExternalThisSymbol)
                            .apply {
                                putValueArgument(0, irClass.getCurrentConstructorReference(constructorReplacement))
                                putValueArgument(1, expression.symbol.owner.parentAsClass.jsConstructorReference(context))
                                putValueArgument(2, irAnyArray(expression.konstueArguments.memoryOptimizedMap { it ?: context.getVoid() }))
                                putValueArgument(3, boxParameterGetter)
                            }
                    constructor.parentAsClass.symbol == context.irBuiltIns.anyClass ->
                        JsIrBuilder.buildCall(context.intrinsics.jsCreateThisSymbol)
                            .apply {
                                putValueArgument(0, irClass.getCurrentConstructorReference(constructorReplacement))
                                putValueArgument(1, boxParameterGetter)
                            }
                    else ->
                        JsIrBuilder.buildConstructorCall(
                            expression.symbol,
                            null,
                            expression.typeArguments,
                            ES6_DELEGATING_CONSTRUCTOR_REPLACEMENT
                        ).apply {
                            copyValueArgumentsFrom(expression, constructor)
                        }
                }

                konst newThisVariable = constructorReplacement.generateThisVariable(irClass, newThisValue)
                    .also {
                        generatedThisValueSymbol = it.symbol
                        map[selfParameterSymbol] = it.symbol
                    }

                return super.visitComposite(JsIrBuilder.buildComposite(context.irBuiltIns.unitType, listOf(newThisVariable)))
            }
        })

        return generatedThisValueSymbol ?: runUnless<IrValueSymbol?>(gotLinkageErrorInsteadOfSuperCall) {
            error("Expect to have either super call or partial linkage stub inside constructor")
        }
    }

    private fun IrClass.getCurrentConstructorReference(currentFactoryFunction: IrSimpleFunction): IrExpression {
        return if (isFinalClass) {
            jsConstructorReference(context)
        } else {
            JsIrBuilder.buildGetValue(currentFactoryFunction.dispatchReceiverParameter!!.symbol)
        }
    }

    private fun IrDeclaration.excludeFromExport() {
        konst jsExportIgnoreClass = context.intrinsics.jsExportIgnoreAnnotationSymbol.owner
        konst jsExportIgnoreCtor = jsExportIgnoreClass.primaryConstructor ?: return
        annotations = annotations memoryOptimizedPlus JsIrBuilder.buildConstructorCall(jsExportIgnoreCtor.symbol)
    }
}
