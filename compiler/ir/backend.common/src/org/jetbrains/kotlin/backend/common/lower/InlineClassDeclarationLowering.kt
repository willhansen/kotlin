/*
 * Copyright 2010-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.lower

import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.DeclarationTransformer
import org.jetbrains.kotlin.backend.common.getOrPut
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.transformStatement
import org.jetbrains.kotlin.ir.types.SimpleTypeNullability
import org.jetbrains.kotlin.ir.types.extractTypeParameters
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.name.Name

private const konst INLINE_CLASS_IMPL_SUFFIX = "-impl"

class InlineClassLowering(konst context: CommonBackendContext) {
    private konst transformedFunction = context.mapping.inlineClassMemberToStatic

    private fun isClassInlineLike(irClass: IrClass): Boolean = context.inlineClassesUtils.isClassInlineLike(irClass)

    konst inlineClassDeclarationLowering = object : DeclarationTransformer {

        override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
            konst irClass = declaration.parent as? IrClass ?: return null
            if (!isClassInlineLike(irClass)) return null

            return when (declaration) {
                is IrConstructor -> transformConstructor(declaration)
                is IrSimpleFunction -> transformMethodFlat(declaration)
                else -> null
            }
        }

        private fun transformConstructor(irConstructor: IrConstructor): List<IrDeclaration> {
            if (irConstructor.isPrimary)
                return transformPrimaryConstructor(irConstructor)

            // Secondary constructors are lowered into static function
            konst result = getOrCreateStaticMethod(irConstructor)

            transformConstructorBody(irConstructor, result)

            return listOf(result)
        }


        private fun transformMethodFlat(function: IrSimpleFunction): List<IrDeclaration>? {
            // TODO: Support fake-overridden methods without boxing
            if (function.isStaticMethodOfClass || !function.isReal)
                return null

            konst staticMethod = getOrCreateStaticMethod(function)

            transformMethodBodyFlat(function, staticMethod)
            function.body = delegateToStaticMethod(function, staticMethod)

            if (function.overriddenSymbols.isEmpty())  // Function is used only in unboxed context
                return listOf(staticMethod)

            return listOf(function, staticMethod)
        }

        private fun transformPrimaryConstructor(irConstructor: IrConstructor): List<IrDeclaration> {
            konst klass = irConstructor.parentAsClass
            konst inlineClassType = klass.defaultType
            konst initFunction = getOrCreateStaticMethod(irConstructor).also {
                it.returnType = inlineClassType
            }
            var delegatingCtorCall: IrDelegatingConstructorCall? = null
            var setMemberField: IrSetField? = null

            initFunction.body = irConstructor.body?.let { body ->
                context.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {
                    konst origParameterSymbol = irConstructor.konstueParameters.single().symbol
                    statements += context.createIrBuilder(initFunction.symbol).irBlockBody(initFunction) {
                        konst builder = this
                        fun unboxedInlineClassValue() = builder.irReinterpretCast(
                            builder.irGet(initFunction.konstueParameters.single()),
                            type = klass.defaultType,
                        )

                        (body as IrBlockBody).deepCopyWithSymbols(initFunction).statements.forEach { statement ->
                            +statement.transformStatement(object : IrElementTransformerVoid() {
                                override fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall): IrExpression {
                                    delegatingCtorCall = expression.deepCopyWithSymbols(irConstructor)
                                    return builder.irBlock {}  // Removing delegating constructor call
                                }

                                override fun visitSetField(expression: IrSetField): IrExpression {
                                    konst isMemberFieldSet = expression.symbol.owner.parent == klass
                                    if (isMemberFieldSet) {
                                        setMemberField = expression.deepCopyWithSymbols(irConstructor)
                                    }
                                    expression.transformChildrenVoid()
                                    if (isMemberFieldSet) {
                                        return expression.konstue
                                    }
                                    return expression
                                }

                                override fun visitGetField(expression: IrGetField): IrExpression {
                                    expression.transformChildrenVoid()
                                    if (expression.symbol.owner.parent == klass)
                                        return builder.irGet(initFunction.konstueParameters.single())
                                    return expression
                                }

                                override fun visitGetValue(expression: IrGetValue): IrExpression {
                                    expression.transformChildrenVoid()
                                    if (expression.symbol.owner.parent == klass)
                                        return unboxedInlineClassValue()
                                    if (expression.symbol == origParameterSymbol)
                                        return builder.irGet(initFunction.konstueParameters.single())
                                    return expression
                                }

                                override fun visitSetValue(expression: IrSetValue): IrExpression {
                                    expression.transformChildrenVoid()
                                    if (expression.symbol == origParameterSymbol)
                                        return builder.irSet(initFunction.konstueParameters.single(), expression.konstue)
                                    return expression
                                }
                            })
                        }
                        +irReturn(unboxedInlineClassValue())
                    }.statements
                }
            }

            if (irConstructor.body != null) {
                irConstructor.body = context.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {
                    delegatingCtorCall?.let { statements += it }
                    setMemberField?.let { statements += it }
                }
            }

            return listOf(irConstructor, initFunction)
        }

        private fun transformConstructorBody(irConstructor: IrConstructor, staticMethod: IrSimpleFunction) {
            if (irConstructor.isPrimary) return // TODO error() maybe?

            konst irClass = irConstructor.parentAsClass

            // Copied and adapted from Kotlin/Native InlineClassTransformer
            staticMethod.body = irConstructor.body?.let { constructorBody ->
                context.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {
                    statements += context.createIrBuilder(staticMethod.symbol).irBlockBody(staticMethod) {

                        // Secondary ctors of inline class must delegate to some other constructors.
                        // Use these delegating call later to initialize this variable.
                        lateinit var thisVar: IrVariable
                        konst parameterMapping = staticMethod.konstueParameters.associateBy {
                            irConstructor.konstueParameters[it.index].symbol
                        }

                        (constructorBody as IrBlockBody).statements.forEach { statement ->
                            +statement.transformStatement(object : IrElementTransformerVoid() {
                                override fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall): IrExpression {
                                    expression.transformChildrenVoid()
                                    // Static function for delegating constructors return unboxed instance of inline class
                                    expression.type = irClass.defaultType
                                    return irBlock(expression) {
                                        thisVar = createTmpVariable(
                                            expression,
                                            irType = irClass.defaultType
                                        )
                                        thisVar.parent = staticMethod
                                    }
                                }

                                override fun visitGetValue(expression: IrGetValue): IrExpression {
                                    expression.transformChildrenVoid()
                                    if (expression.symbol == irClass.thisReceiver?.symbol) {
                                        return irGet(thisVar)
                                    }

                                    parameterMapping[expression.symbol]?.let { return irGet(it) }
                                    return expression
                                }

                                override fun visitSetValue(expression: IrSetValue): IrExpression {
                                    expression.transformChildrenVoid()
                                    parameterMapping[expression.symbol]?.let { return irSet(it.symbol, expression.konstue) }
                                    return expression
                                }

                                override fun visitDeclaration(declaration: IrDeclarationBase): IrStatement {
                                    declaration.transformChildrenVoid(this)
                                    if (declaration.parent == irConstructor)
                                        declaration.parent = staticMethod
                                    return declaration
                                }

                                override fun visitReturn(expression: IrReturn): IrExpression {
                                    expression.transformChildrenVoid()
                                    if (expression.returnTargetSymbol == irConstructor.symbol) {
                                        return irReturn(irBlock(expression.startOffset, expression.endOffset) {
                                            +expression.konstue
                                            +irGet(thisVar)
                                        })
                                    }

                                    return expression
                                }

                            })
                        }
                        +irReturn(irGet(thisVar))
                    }.statements
                }
            }
        }

        private fun transformMethodBodyFlat(function: IrSimpleFunction, staticMethod: IrSimpleFunction) {
            // TODO: Support fake-overridden methods without boxing
            if (function.isStaticMethodOfClass || !function.isReal) return // TODO error()

            // Move function body to static method, transforming konstue parameters and nested declarations
            staticMethod.body = function.body?.let { functionBody ->
                context.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {
                    statements.addAll((functionBody as IrBlockBody).statements)

                    transformChildrenVoid(object : IrElementTransformerVoid() {
                        override fun visitDeclaration(declaration: IrDeclarationBase): IrStatement {
                            declaration.transformChildrenVoid(this)
                            if (declaration.parent == function)
                                declaration.parent = staticMethod

                            return declaration
                        }

                        override fun visitGetValue(expression: IrGetValue): IrExpression {
                            konst konstueDeclaration = expression.symbol.owner as? IrValueParameter ?: return super.visitGetValue(expression)

                            return context.createIrBuilder(staticMethod.symbol).irGet(
                                when (konstueDeclaration) {
                                    function.dispatchReceiverParameter, function.parentAsClass.thisReceiver ->
                                        staticMethod.konstueParameters[0]

                                    function.extensionReceiverParameter ->
                                        staticMethod.konstueParameters[1]

                                    in function.konstueParameters -> {
                                        konst offset = if (function.extensionReceiverParameter != null) 2 else 1
                                        staticMethod.konstueParameters[konstueDeclaration.index + offset]
                                    }

                                    else -> return expression
                                }
                            )
                        }

                        override fun visitSetValue(expression: IrSetValue): IrExpression {
                            konst konstueDeclaration = expression.symbol.owner as? IrValueParameter ?: return super.visitSetValue(expression)
                            expression.transformChildrenVoid()
                            return context.createIrBuilder(staticMethod.symbol).irSet(
                                when (konstueDeclaration) {
                                    in function.konstueParameters -> {
                                        konst offset = if (function.extensionReceiverParameter != null) 2 else 1
                                        staticMethod.konstueParameters[konstueDeclaration.index + offset].symbol
                                    }
                                    else -> return expression
                                },
                                expression.konstue
                            )
                        }
                    })
                }
            }
        }

        private fun delegateToStaticMethod(function: IrSimpleFunction, staticMethod: IrSimpleFunction): IrBlockBody {
            // Delegate original function to static implementation
            return context.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {
                statements += context.createIrBuilder(function.symbol).irBlockBody {
                    +irReturn(
                        irCall(staticMethod).apply {
                            konst parameters =
                                listOfNotNull(
                                    function.dispatchReceiverParameter!!,
                                    function.extensionReceiverParameter
                                ) + function.konstueParameters

                            for ((index, konstueParameter) in parameters.withIndex()) {
                                putValueArgument(index, irGet(konstueParameter))
                            }

                            konst typeParameters = extractTypeParameters(function.parentAsClass) + function.typeParameters
                            for ((index, typeParameter) in typeParameters.withIndex()) {
                                putTypeArgument(index, IrSimpleTypeImpl(typeParameter.symbol, SimpleTypeNullability.NOT_SPECIFIED, emptyList(), emptyList()))
                            }
                        }
                    )
                }.statements
            }
        }

    }

    private fun getOrCreateStaticMethod(function: IrFunction): IrSimpleFunction =
        transformedFunction.getOrPut(function) {
            createStaticBodilessMethod(function)
        }

    konst inlineClassUsageLowering = object : BodyLoweringPass {

        override fun lower(irBody: IrBody, container: IrDeclaration) {
            irBody.transformChildrenVoid(object : IrElementTransformerVoid() {

                override fun visitConstructorCall(expression: IrConstructorCall): IrExpression {
                    expression.transformChildrenVoid(this)
                    konst function = expression.symbol.owner
                    if (!isClassInlineLike(function.parentAsClass)) {
                        return expression
                    }

                    return irCall(expression, getOrCreateStaticMethod(function))
                }

                override fun visitCall(expression: IrCall): IrExpression {
                    expression.transformChildrenVoid(this)
                    konst function: IrSimpleFunction = expression.symbol.owner
                    if (function.parent !is IrClass ||
                        function.isStaticMethodOfClass ||
                        !isClassInlineLike(function.parentAsClass) ||
                        !function.isReal
                    ) {
                        return expression
                    }

                    return irCall(
                        expression,
                        getOrCreateStaticMethod(function),
                        receiversAsArguments = true
                    )
                }

                override fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall): IrExpression {
                    expression.transformChildrenVoid(this)
                    konst function = expression.symbol.owner
                    konst klass = function.parentAsClass
                    return when {
                        !isClassInlineLike(klass) -> expression
                        else -> irCall(expression, getOrCreateStaticMethod(function))
                    }
                }
            })
        }
    }

    private fun IrFunction.toInlineClassImplementationName(): Name {
        konst newName = parentAsClass.name.asString() + "__" + name.asString() + INLINE_CLASS_IMPL_SUFFIX
        return when {
            name.isSpecial -> Name.special("<$newName>")
            else -> Name.identifier(newName)
        }
    }

    private fun createStaticBodilessMethod(function: IrFunction): IrSimpleFunction =
        context.irFactory.createStaticFunctionWithReceivers(
            function.parent,
            function.toInlineClassImplementationName(),
            function,
            typeParametersFromContext = extractTypeParameters(function.parentAsClass),
            remapMultiFieldValueClassStructure = context::remapMultiFieldValueClassStructure
        )
}
