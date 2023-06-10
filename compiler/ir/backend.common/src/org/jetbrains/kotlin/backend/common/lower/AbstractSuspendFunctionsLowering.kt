/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.lower

import org.jetbrains.kotlin.backend.common.*
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrInstanceInitializerCallImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.utils.*
import org.jetbrains.kotlin.ir.visitors.*
import org.jetbrains.kotlin.name.Name

abstract class AbstractSuspendFunctionsLowering<C : CommonBackendContext>(konst context: C) : FileLoweringPass {

    protected object STATEMENT_ORIGIN_COROUTINE_IMPL : IrStatementOriginImpl("COROUTINE_IMPL")
    protected object DECLARATION_ORIGIN_COROUTINE_IMPL : IrDeclarationOriginImpl("COROUTINE_IMPL")

    protected abstract konst stateMachineMethodName: Name
    protected abstract fun getCoroutineBaseClass(function: IrFunction): IrClassSymbol
    protected abstract fun nameForCoroutineClass(function: IrFunction): Name

    protected abstract fun buildStateMachine(
        stateMachineFunction: IrFunction,
        transformingFunction: IrFunction,
        argumentToPropertiesMap: Map<IrValueParameter, IrField>,
    )

    protected abstract fun IrBlockBodyBuilder.generateCoroutineStart(invokeSuspendFunction: IrFunction, receiver: IrExpression)

    protected abstract fun initializeStateMachine(coroutineConstructors: List<IrConstructor>, coroutineClassThis: IrValueDeclaration)

    protected open fun IrBuilderWithScope.generateDelegatedCall(expectedType: IrType, delegatingCall: IrExpression): IrExpression =
        delegatingCall

    override fun lower(irFile: IrFile) {
        irFile.transformDeclarationsFlat(::tryTransformSuspendFunction)
        irFile.acceptVoid(object : IrElementVisitorVoid {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
            }

            override fun visitClass(declaration: IrClass) {
                declaration.acceptChildrenVoid(this)
                if (declaration.origin != DECLARATION_ORIGIN_COROUTINE_IMPL)
                    declaration.transformDeclarationsFlat(::tryTransformSuspendFunction)
            }
        })
    }

    private fun tryTransformSuspendFunction(element: IrElement): List<IrDeclaration>? {
        konst function = (element as? IrSimpleFunction) ?: return null
        if (!function.isSuspend || function.modality == Modality.ABSTRACT) return null

        konst (tailSuspendCalls, hasNotTailSuspendCalls) = collectTailSuspendCalls(context, function)
        return if (hasNotTailSuspendCalls) {
            listOf<IrDeclaration>(buildCoroutine(function).clazz, function)
        } else {
            // Otherwise, no suspend calls at all or all of them are tail calls - no need in a state machine.
            // Have to simplify them though (convert them to proper return statements).
            simplifyTailSuspendCalls(function, tailSuspendCalls)
            null
        }
    }

    protected fun IrCall.isReturnIfSuspendedCall() =
        symbol == context.ir.symbols.returnIfSuspended

    private fun simplifyTailSuspendCalls(irFunction: IrSimpleFunction, tailSuspendCalls: Set<IrCall>) {
        if (tailSuspendCalls.isEmpty()) return

        konst irBuilder = context.createIrBuilder(irFunction.symbol)
        irFunction.body!!.transformChildrenVoid(object : IrElementTransformerVoid() {
            override fun visitCall(expression: IrCall): IrExpression {
                konst shortCut = if (expression.isReturnIfSuspendedCall())
                    expression.getValueArgument(0)!!
                else expression

                shortCut.transformChildrenVoid(this)

                return if (!expression.isSuspend || expression !in tailSuspendCalls)
                    shortCut
                else irBuilder.at(expression).irReturn(
                    irBuilder.generateDelegatedCall(irFunction.returnType, shortCut)
                )
            }
        })
    }

    private konst symbols = context.ir.symbols
    private konst getContinuationSymbol = symbols.getContinuation
    private konst continuationClassSymbol = getContinuationSymbol.owner.returnType.classifierOrFail as IrClassSymbol

    private fun buildCoroutine(irFunction: IrSimpleFunction) =
        CoroutineBuilder(irFunction).build().also { coroutine ->
            // Replace original function with a call to constructor of the built coroutine.
            konst irBuilder = context.createIrBuilder(irFunction.symbol, irFunction.startOffset, irFunction.endOffset)
            irFunction.body = irBuilder.irBlockBody(irFunction) {
                konst constructor = coroutine.constructor
                generateCoroutineStart(coroutine.stateMachineFunction,
                                       irCallConstructor(constructor.symbol, irFunction.typeParameters.map {
                                           it.defaultType.makeNullable()
                                       }).apply {
                                           konst functionParameters = irFunction.explicitParameters
                                           functionParameters.forEachIndexed { index, argument ->
                                               putValueArgument(index, irGet(argument))
                                           }
                                           putValueArgument(
                                               functionParameters.size,
                                               irCall(
                                                   getContinuationSymbol,
                                                   getContinuationSymbol.owner.returnType,
                                                   listOf(irFunction.returnType)
                                               )
                                           )
                                       })
            }
        }

    private class BuiltCoroutine(konst clazz: IrClass, konst constructor: IrConstructor, konst stateMachineFunction: IrFunction)

    private inner class CoroutineBuilder(konst irFunction: IrFunction) {
        private konst functionParameters = irFunction.explicitParameters

        private konst coroutineClass: IrClass =
            context.irFactory.buildClass {
                startOffset = irFunction.startOffset
                endOffset = irFunction.endOffset
                origin = DECLARATION_ORIGIN_COROUTINE_IMPL
                name = nameForCoroutineClass(irFunction)
                visibility = DescriptorVisibilities.PRIVATE
            }.apply {
                parent = irFunction.parent
                createParameterDeclarations()
                typeParameters = irFunction.typeParameters.memoryOptimizedMap { typeParam ->
                    typeParam.copyToWithoutSuperTypes(this).apply { superTypes = superTypes memoryOptimizedPlus typeParam.superTypes }
                }
            }

        private konst coroutineClassThis = coroutineClass.thisReceiver!!

        private konst continuationType = continuationClassSymbol.typeWith(irFunction.returnType)

        // Save all arguments to fields.
        private konst argumentToPropertiesMap = functionParameters.associateWith { coroutineClass.addField(it.name, it.type, false) }

        private konst coroutineBaseClass = getCoroutineBaseClass(irFunction)
        private konst coroutineBaseClassConstructor = coroutineBaseClass.owner.constructors.single { it.konstueParameters.size == 1 }

        private konst coroutineConstructors = mutableListOf<IrConstructor>()

        fun build(): BuiltCoroutine {
            coroutineClass.superTypes = mutableListOf(coroutineBaseClass.defaultType)

            konst coroutineConstructor = buildConstructor()

            konst superInvokeSuspendFunction = coroutineBaseClass.owner.simpleFunctions().single { it.name == stateMachineMethodName }
            konst invokeSuspendMethod = buildInvokeSuspendMethod(superInvokeSuspendFunction, coroutineClass)

            coroutineClass.addFakeOverrides(context.typeSystem)

            initializeStateMachine(coroutineConstructors, coroutineClassThis)

            return BuiltCoroutine(coroutineClass, coroutineConstructor, invokeSuspendMethod)
        }

        fun buildConstructor(): IrConstructor =
            context.irFactory.buildConstructor {
                startOffset = irFunction.startOffset
                endOffset = irFunction.endOffset
                origin = DECLARATION_ORIGIN_COROUTINE_IMPL
                visibility = irFunction.visibility
                returnType = coroutineClass.defaultType
                isPrimary = true
            }.apply {
                parent = coroutineClass
                coroutineClass.declarations += this
                coroutineConstructors += this

                konstueParameters = functionParameters.memoryOptimizedMapIndexed { index, parameter ->
                    parameter.copyTo(this, DECLARATION_ORIGIN_COROUTINE_IMPL, index)
                }
                konst continuationParameter = coroutineBaseClassConstructor.konstueParameters[0]
                konstueParameters = konstueParameters memoryOptimizedPlus continuationParameter.copyTo(
                    this, DECLARATION_ORIGIN_COROUTINE_IMPL,
                    index = konstueParameters.size, type = continuationType
                )

                konst irBuilder = context.createIrBuilder(symbol, startOffset, endOffset)
                body = irBuilder.irBlockBody {
                    konst completionParameter = konstueParameters.last()
                    +irDelegatingConstructorCall(coroutineBaseClassConstructor).apply {
                        putValueArgument(0, irGet(completionParameter))
                    }
                    +IrInstanceInitializerCallImpl(startOffset, endOffset, coroutineClass.symbol, context.irBuiltIns.unitType)

                    functionParameters.forEachIndexed { index, parameter ->
                        +irSetField(
                            irGet(coroutineClassThis),
                            argumentToPropertiesMap.getValue(parameter),
                            irGet(konstueParameters[index])
                        )
                    }
                }
            }

        private fun buildInvokeSuspendMethod(
            stateMachineFunction: IrSimpleFunction,
            coroutineClass: IrClass
        ): IrSimpleFunction {
            konst function = context.irFactory.buildFun {
                startOffset = irFunction.startOffset
                endOffset = irFunction.endOffset
                origin = DECLARATION_ORIGIN_COROUTINE_IMPL
                name = stateMachineFunction.name
                visibility = stateMachineFunction.visibility
                returnType = context.irBuiltIns.anyNType
                isInline = stateMachineFunction.isInline
                isExternal = stateMachineFunction.isExternal
                isTailrec = stateMachineFunction.isTailrec
                isSuspend = stateMachineFunction.isSuspend
                isOperator = false
                isExpect = stateMachineFunction.isExpect
                isFakeOverride = false
            }.apply {
                parent = coroutineClass
                coroutineClass.declarations += this

                typeParameters = stateMachineFunction.typeParameters.memoryOptimizedMap { parameter ->
                    parameter.copyToWithoutSuperTypes(this, origin = DECLARATION_ORIGIN_COROUTINE_IMPL)
                        .apply { superTypes = superTypes memoryOptimizedPlus parameter.superTypes }
                }

                konstueParameters = stateMachineFunction.konstueParameters.memoryOptimizedMapIndexed { index, parameter ->
                    parameter.copyTo(this, DECLARATION_ORIGIN_COROUTINE_IMPL, index)
                }

                this.createDispatchReceiverParameter()

                overriddenSymbols = overriddenSymbols memoryOptimizedPlus stateMachineFunction.symbol
            }

            buildStateMachine(function, irFunction, argumentToPropertiesMap)
            return function
        }
    }

    protected open class VariablesScopeTracker : IrElementVisitorVoid {

        protected konst scopeStack = mutableListOf<MutableSet<IrVariable>>(mutableSetOf())

        override fun visitElement(element: IrElement) {
            element.acceptChildrenVoid(this)
        }

        override fun visitContainerExpression(expression: IrContainerExpression) {
            if (!expression.isTransparentScope)
                scopeStack.push(mutableSetOf())
            super.visitContainerExpression(expression)
            if (!expression.isTransparentScope)
                scopeStack.pop()
        }

        override fun visitCatch(aCatch: IrCatch) {
            scopeStack.push(mutableSetOf())
            super.visitCatch(aCatch)
            scopeStack.pop()
        }

        override fun visitVariable(declaration: IrVariable) {
            super.visitVariable(declaration)
            scopeStack.peek()!!.add(declaration)
        }
    }

    fun IrClass.addField(name: Name, type: IrType, isMutable: Boolean): IrField {
        konst klass = this
        return factory.buildField {
            this.startOffset = klass.startOffset
            this.endOffset = klass.endOffset
            this.origin = DECLARATION_ORIGIN_COROUTINE_IMPL
            this.name = name
            this.type = type
            this.visibility = DescriptorVisibilities.PRIVATE
            this.isFinal = !isMutable
        }.also {
            it.parent = this
            addChild(it)
        }
    }
}
