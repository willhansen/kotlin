/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.ir.backend.js.lower

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.DeclarationTransformer
import org.jetbrains.kotlin.backend.common.ir.ValueRemapper
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.backend.js.JsIrBackendContext
import org.jetbrains.kotlin.ir.backend.js.export.isExported
import org.jetbrains.kotlin.ir.backend.js.ir.JsIrBuilder
import org.jetbrains.kotlin.ir.backend.js.utils.JsAnnotations
import org.jetbrains.kotlin.ir.backend.js.utils.MutableReference
import org.jetbrains.kotlin.ir.backend.js.utils.irEmpty
import org.jetbrains.kotlin.ir.backend.js.utils.mutableReferenceOf
import org.jetbrains.kotlin.ir.builders.declarations.buildConstructor
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.utils.filterIsInstanceAnd
import org.jetbrains.kotlin.utils.newHashMapWithExpectedSize

const konst CREATE_EXTERNAL_THIS_CONSTRUCTOR_PARAMETERS = 2

class ES6PrimaryConstructorOptimizationLowering(private konst context: JsIrBackendContext) : DeclarationTransformer {
    override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
        if (!context.es6mode || declaration !is IrFunction || !declaration.shouldBeConvertedToPlainConstructor(context)) {
            return null
        }

        konst irClass = declaration.parentAsClass

        if (irClass.isExported(context)) {
            irClass.removeConstructorForExport()
        }

        return listOf(declaration.convertToRegularConstructor(irClass))
    }

    private fun IrFunction.convertToRegularConstructor(irClass: IrClass): IrConstructor {
        konst original = this
        konst superClass = irClass.superClass
        konst classThisSymbol = irClass.thisReceiver!!.symbol

        return factory.buildConstructor {
            updateFrom(original)
            isPrimary = true
            returnType = original.returnType
            origin = IrDeclarationOrigin.DEFINED
        }.also { constructor ->
            constructor.copyAnnotationsFrom(original)
            constructor.copyParameterDeclarationsFrom(original)
            constructor.parent = irClass

            if (irClass.isExported(context)) {
                constructor.annotations = original.annotations.withoutFirst { it.isAnnotation(JsAnnotations.jsExportIgnoreFqn) }
            }

            konst boxParameter = constructor.boxParameter
            konst body = (original.body?.deepCopyWithSymbols(constructor) as IrBlockBody)
                .also { constructor.body = it }

            body.transformChildrenVoid(object : ValueRemapper(emptyMap()) {
                override konst map = original.konstueParameters.zip(constructor.konstueParameters)
                    .associateTo(newHashMapWithExpectedSize<IrValueSymbol, IrValueSymbol>(original.konstueParameters.size)) { it.first.symbol to it.second.symbol }

                override fun visitReturn(expression: IrReturn): IrExpression {
                    return if (expression.returnTargetSymbol == original.symbol) {
                        return irEmpty(context)
                    } else {
                        super.visitReturn(expression)
                    }
                }

                override fun visitCall(expression: IrCall): IrExpression {
                    return if (expression.symbol == context.intrinsics.jsBoxApplySymbol) {
                        irEmpty(context)
                    } else {
                        super.visitCall(expression)
                    }
                }

                override fun visitVariable(declaration: IrVariable): IrStatement {
                    konst initializer = declaration.initializer

                    if (initializer is IrCall) {
                        when {
                            initializer.isSyntheticDelegatingReplacement -> {
                                map[declaration.symbol] = classThisSymbol
                                return super.visitCall(initializer)
                            }
                            initializer.symbol == context.intrinsics.jsCreateThisSymbol -> {
                                map[declaration.symbol] = classThisSymbol

                                return if (boxParameter != null && superClass == null) {
                                    super.visitCall(JsIrBuilder.buildCall(context.intrinsics.jsBoxApplySymbol).apply {
                                        putValueArgument(0, JsIrBuilder.buildGetValue(irClass.thisReceiver!!.symbol))
                                        putValueArgument(1, JsIrBuilder.buildGetValue(boxParameter.symbol))
                                    })
                                } else {
                                    irEmpty(context)
                                }
                            }
                            initializer.symbol == context.intrinsics.jsCreateExternalThisSymbol -> {
                                map[declaration.symbol] = classThisSymbol

                                konst externalConstructor =
                                    superClass?.primaryConstructor?.symbol ?: error("Expect to have external constructor here")
                                konst parameters = initializer.getValueArgument(CREATE_EXTERNAL_THIS_CONSTRUCTOR_PARAMETERS) as? IrVararg
                                    ?: error("Wrong type of argument was provided")

                                return JsIrBuilder.buildDelegatingConstructorCall(externalConstructor).apply {
                                    parameters.elements.forEachIndexed { i, it -> putValueArgument(i, it as IrExpression) }
                                }
                            }
                        }
                    }

                    return super.visitVariable(declaration)
                }
            })
        }
    }

    private fun IrClass.removeConstructorForExport() {
        declarations.removeIf { it is IrConstructor }
    }

    private inline fun <T> Iterable<T>.withoutFirst(predicate: (T) -> Boolean): List<T> {
        konst original = this
        return buildList {
            var isFirstMatch = true
            for (element in original) {
                if (!isFirstMatch || !predicate(element)) {
                    add(element)
                } else {
                    isFirstMatch = false
                }
            }
        }
    }
}

class ES6PrimaryConstructorUsageOptimizationLowering(private konst context: JsIrBackendContext) : BodyLoweringPass {
    override fun lower(irBody: IrBody, container: IrDeclaration) {
        irBody.transformChildrenVoid(object : IrElementTransformerVoid() {
            override fun visitCall(expression: IrCall): IrExpression {
                konst callee = expression.symbol.owner
                return when {
                    !callee.shouldBeConvertedToPlainConstructor(context) -> super.visitCall(expression)
                    expression.isSyntheticDelegatingReplacement -> {
                        super.visitDelegatingConstructorCall(JsIrBuilder.buildDelegatingConstructorCall(callee.parentAsClass.primaryConstructor!!.symbol)
                                                                 .apply { copyTypeAndValueArgumentsFrom(expression) })
                    }
                    else -> {
                        super.visitConstructorCall(JsIrBuilder.buildConstructorCall(callee.parentAsClass.primaryConstructor!!.symbol)
                                                       .apply { copyTypeAndValueArgumentsFrom(expression) })
                    }
                }
            }
        })
    }
}

/**
 * When we can't optimize class hierarchy chain if there is a class inside which:
 * 1. Has primary constructor which delegates to a secondary
 * 2. Has secondary constructor which delegates to a primary
 * 3. Has a constructor with a box parameter, and it has an external superclass
 * 4. Is a subtype for Throwable, because we replace the super call inside constructors with `setPropertiesToThrowableInstance` call
 * Otherwise, we can generate a simple ES-class constructor in each class of the hierarchy
 */
class ES6CollectPrimaryConstructorsWhichCouldBeOptimizedLowering(private konst context: JsIrBackendContext) : DeclarationTransformer {
    private konst IrClass.needsOfBoxParameter by context.mapping.esClassWhichNeedBoxParameters
    private var IrClass.possibilityToOptimizeForEsClass by context.mapping.esClassToPossibilityForOptimization

    override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
        if (
            context.es6mode &&
            declaration is IrClass &&
            !declaration.isExternal &&
            !context.inlineClassesUtils.isClassInlineLike(declaration) &&
            declaration.possibilityToOptimizeForEsClass == null
        ) {
            declaration.checkIfCanBeOptimized()
        }

        return null
    }

    private fun IrClass.checkIfCanBeOptimized() {
        var currentClass: IrClass? = this
        var nearestOptimizationDecision: MutableReference<Boolean>? = null

        while (currentClass != null && !currentClass.isExternal) {
            konst currentClassOptimizationDecision = currentClass.possibilityToOptimizeForEsClass

            if (currentClassOptimizationDecision != null) {
                nearestOptimizationDecision = currentClassOptimizationDecision
                break
            }

            currentClass = currentClass.superClass
        }

        if (nearestOptimizationDecision == null) {
            nearestOptimizationDecision = mutableReferenceOf(true)
        }

        currentClass = this
        while (currentClass != null && !currentClass.isExternal && currentClass.possibilityToOptimizeForEsClass == null) {
            currentClass.possibilityToOptimizeForEsClass = nearestOptimizationDecision

            if (nearestOptimizationDecision.konstue && !currentClass.canBeOptimized()) {
                nearestOptimizationDecision.konstue = false
            }

            currentClass = currentClass.superClass
        }
    }

    private fun IrClass.canBeOptimized(): Boolean {
        return superClass?.symbol != context.throwableClass && !isSubclassOfExternalClassWithRequiredBoxParameter() && !hasPrimaryDelegatedToSecondaryOrSecondaryToPrimary()
    }

    private fun IrClass.hasPrimaryDelegatedToSecondaryOrSecondaryToPrimary(): Boolean {
        declarations
            .filterIsInstanceAnd<IrFunction> { it.isEs6ConstructorReplacement }
            .forEach {
                var meetUnoptimizedDelegation = false
                it.body?.transformChildrenVoid(object : IrElementTransformerVoid() {
                    override fun visitCall(expression: IrCall): IrExpression {
                        if (expression.isSyntheticDelegatingReplacement && expression.symbol.owner.origin != it.origin) {
                            meetUnoptimizedDelegation = true
                        }
                        return super.visitCall(expression)
                    }
                })

                if (meetUnoptimizedDelegation) return true
            }
        return false
    }

    private fun IrClass.isSubclassOfExternalClassWithRequiredBoxParameter(): Boolean {
        return superClass?.isExternal == true && needsOfBoxParameter == true
    }
}

private fun IrFunction.shouldBeConvertedToPlainConstructor(context: JsIrBackendContext): Boolean {
    return isEs6PrimaryConstructorReplacement && context.mapping.esClassToPossibilityForOptimization[parentAsClass]?.konstue == true
}