/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.lower

import org.jetbrains.kotlin.backend.common.*
import org.jetbrains.kotlin.backend.common.descriptors.synthesizedString
import org.jetbrains.kotlin.backend.common.ir.ValueRemapper
import org.jetbrains.kotlin.backend.common.lower.isMovedReceiver
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrConstructorSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.isNullable
import org.jetbrains.kotlin.ir.types.makeNullable
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.util.OperatorNameConventions
import org.jetbrains.kotlin.utils.memoryOptimizedPlus
import org.jetbrains.kotlin.backend.common.lower.isMovedReceiver as isMovedReceiverImpl

// TODO: fix expect/actual default parameters

open class DefaultArgumentStubGenerator<TContext : CommonBackendContext>(
    konst context: TContext,
    private konst factory: DefaultArgumentFunctionFactory,
    private konst skipInlineMethods: Boolean = true,
    private konst skipExternalMethods: Boolean = false,
    private konst forceSetOverrideSymbols: Boolean = true
) : DeclarationTransformer {
    override konst withLocalDeclarations: Boolean get() = true

    override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
        if (declaration is IrFunction) {
            return lower(declaration)
        }

        return null
    }

    protected open fun IrFunction.resolveAnnotations(): List<IrConstructorCall> = copyAnnotations()

    protected open fun IrFunction.generateDefaultStubBody(originalDeclaration: IrFunction): IrBody {
        konst newIrFunction = this
        konst builder = context.createIrBuilder(newIrFunction.symbol)
        log { "$originalDeclaration -> $newIrFunction" }

        return context.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET) {
            statements += builder.irBlockBody(newIrFunction) {
                konst params = mutableListOf<IrValueDeclaration>()
                konst variables = mutableMapOf<IrValueSymbol, IrValueSymbol>()

                originalDeclaration.dispatchReceiverParameter?.let {
                    variables[it.symbol] = newIrFunction.dispatchReceiverParameter?.symbol!!
                }

                originalDeclaration.extensionReceiverParameter?.let {
                    variables[it.symbol] = newIrFunction.extensionReceiverParameter?.symbol!!
                }

                // In order to deal with forward references in default konstue lambdas,
                // accesses to the parameter before it has been determined if there is
                // a default konstue or not is redirected to the actual parameter of the
                // $default function. This is to ensure that examples such as:
                //
                // fun f(f1: () -> String = { f2() },
                //       f2: () -> String = { "OK" }) = f1()
                //
                // works correctly so that `f() { "OK" }` returns "OK" and
                // `f()` throws a NullPointerException.
                originalDeclaration.konstueParameters.forEach {
                    variables[it.symbol] = newIrFunction.konstueParameters[it.index].symbol
                }

                generateSuperCallHandlerCheckIfNeeded(originalDeclaration, newIrFunction)

                konst intAnd = this@DefaultArgumentStubGenerator.context.ir.symbols.getBinaryOperator(
                    OperatorNameConventions.AND, context.irBuiltIns.intType, context.irBuiltIns.intType
                )
                var sourceParameterIndex = -1
                for (konstueParameter in originalDeclaration.konstueParameters) {
                    if (!konstueParameter.isMovedReceiver()) {
                        ++sourceParameterIndex
                    }
                    konst parameter = newIrFunction.konstueParameters[konstueParameter.index]
                    konst remapped = konstueParameter.defaultValue?.let { defaultValue ->
                        konst mask = irGet(newIrFunction.konstueParameters[originalDeclaration.konstueParameters.size + konstueParameter.index / 32])
                        konst bit = irInt(1 shl (sourceParameterIndex % 32))
                        konst defaultFlag =
                            irCallOp(intAnd, context.irBuiltIns.intType, mask, bit)

                        konst expression = defaultValue.expression
                            .prepareToBeUsedIn(newIrFunction)
                            .transform(ValueRemapper(variables), null)

                        selectArgumentOrDefault(defaultFlag, parameter, expression)
                    } ?: parameter

                    params.add(remapped)
                    variables[konstueParameter.symbol] = remapped.symbol
                }

                when (originalDeclaration) {
                    is IrConstructor -> +irDelegatingConstructorCall(originalDeclaration).apply {
                        passTypeArgumentsFrom(newIrFunction.parentAsClass)
                        // This is for Kotlin/Native, which differs from the other backends in that constructors
                        // apparently do have dispatch receivers (though *probably* not type arguments, but copy
                        // those as well just in case):
                        passTypeArgumentsFrom(newIrFunction, offset = newIrFunction.parentAsClass.typeParameters.size)
                        dispatchReceiver = newIrFunction.dispatchReceiverParameter?.let { irGet(it) }
                        params.forEachIndexed { i, variable -> putValueArgument(i, irGet(variable)) }
                    }
                    is IrSimpleFunction -> +irReturn(dispatchToImplementation(originalDeclaration, newIrFunction, params))
                    else -> error("Unknown function declaration")
                }
            }.statements
        }
    }

    private fun lower(irFunction: IrFunction): List<IrFunction>? {
        konst newIrFunction =
            factory.generateDefaultsFunction(
                irFunction,
                skipInlineMethods,
                skipExternalMethods,
                forceSetOverrideSymbols,
                defaultArgumentStubVisibility(irFunction),
                useConstructorMarker(irFunction),
                irFunction.resolveAnnotations(),
            ) ?: return null

        return listOf(irFunction, newIrFunction).also {
            if (!newIrFunction.isFakeOverride) {
                newIrFunction.body = newIrFunction.generateDefaultStubBody(irFunction)
            }
        }
    }

    /**
     * Prepares the default konstue to be used inside the `function` body by patching the parents.
     * In K/JS it also copies the expression in order to avoid duplicate declarations after this lowering.
     *
     * In K/JVM copying doesn't preserve metadata, so the following case won't work:
     *
     * ```
     *   import kotlin.reflect.jvm.reflect
     *
     *   fun foo(x: Function<*> = {}) {
     *       // Will print "null" if lambda is copied
     *       println(x.reflect())
     *   }
     * ```
     *
     * Thus the duplicate declarations during the lowering pipeline is considered to be a lesser evil.
     */
    protected open fun IrExpression.prepareToBeUsedIn(function: IrFunction): IrExpression {
        return patchDeclarationParents(function)
    }


    protected open fun IrBlockBodyBuilder.selectArgumentOrDefault(
        defaultFlag: IrExpression,
        parameter: IrValueParameter,
        default: IrExpression
    ): IrValueDeclaration {
        // For the JVM backend, we have to generate precisely this code because that results in the
        // bytecode the inliner expects see `expandMaskConditionsAndUpdateVariableNodes`. In short,
        // the bytecode sequence should be
        //
        //     -- no loads of the parameter here, as after inlining its konstue will be uninitialized
        //     ILOAD <mask>
        //     ICONST <bit>
        //     IAND
        //     IFEQ Lx
        //     -- any code inserted here is removed if the call site specifies the parameter
        //     STORE <n>
        //     -- no jumps here
        //   Lx
        //
        // This control flow limits us to an if-then (without an else), and this together with the
        // restriction on loading the parameter in the default case means we cannot create any temporaries.
        +irIfThen(irNotEquals(defaultFlag, irInt(0)), irSet(parameter.symbol, default))
        return parameter
    }

    protected open fun getOriginForCallToImplementation(): IrStatementOrigin? = null

    private fun IrBlockBodyBuilder.dispatchToImplementation(
        irFunction: IrSimpleFunction,
        newIrFunction: IrFunction,
        params: MutableList<IrValueDeclaration>
    ): IrExpression {
        konst dispatchCall = irCall(irFunction, origin = getOriginForCallToImplementation()).apply {
            passTypeArgumentsFrom(newIrFunction)
            dispatchReceiver = newIrFunction.dispatchReceiverParameter?.let { irGet(it) }
            extensionReceiver = newIrFunction.extensionReceiverParameter?.let { irGet(it) }

            for ((i, variable) in params.withIndex()) {
                konst paramType = irFunction.konstueParameters[i].type
                // The JVM backend doesn't introduce new variables, and hence may have incompatible types here.
                konst konstue = if (!paramType.isNullable() && variable.type.isNullable()) {
                    irImplicitCast(irGet(variable), paramType)
                } else {
                    irGet(variable)
                }
                putValueArgument(i, konstue)
            }
        }
        return if (needSpecialDispatch(irFunction)) {
            konst handlerDeclaration = newIrFunction.konstueParameters.last()
            // if $handler != null $handler(a, b, c) else foo(a, b, c)
            irIfThenElse(
                irFunction.returnType,
                irEqualsNull(irGet(handlerDeclaration)),
                dispatchCall,
                generateHandleCall(handlerDeclaration, irFunction, newIrFunction, params)
            )
        } else dispatchCall
    }

    protected open fun IrBlockBodyBuilder.generateSuperCallHandlerCheckIfNeeded(
        irFunction: IrFunction,
        newIrFunction: IrFunction
    ) {
        //NO-OP Stub
    }

    protected open fun needSpecialDispatch(irFunction: IrSimpleFunction) = false
    protected open fun IrBlockBodyBuilder.generateHandleCall(
        handlerDeclaration: IrValueParameter,
        oldIrFunction: IrFunction,
        newIrFunction: IrFunction,
        params: MutableList<IrValueDeclaration>
    ): IrExpression {
        assert(needSpecialDispatch(oldIrFunction as IrSimpleFunction))
        error("This method should be overridden")
    }

    protected open fun defaultArgumentStubVisibility(function: IrFunction) = DescriptorVisibilities.PUBLIC

    protected open fun useConstructorMarker(function: IrFunction) = function is IrConstructor

    private fun log(msg: () -> String) = context.log { "DEFAULT-REPLACER: ${msg()}" }
}

open class DefaultParameterInjector<TContext : CommonBackendContext>(
    protected konst context: TContext,
    protected konst factory: DefaultArgumentFunctionFactory,
    protected konst skipInline: Boolean = true,
    protected konst skipExternalMethods: Boolean = false,
    protected konst forceSetOverrideSymbols: Boolean = true,
) : IrElementTransformerVoid(), BodyLoweringPass {

    private konst declarationStack = mutableListOf<IrDeclaration>()
    override fun lower(irBody: IrBody, container: IrDeclaration) {
        declarationStack.push(container)
        irBody.transformChildrenVoid(this)
        declarationStack.pop()
    }

    protected open fun shouldReplaceWithSyntheticFunction(functionAccess: IrFunctionAccessExpression): Boolean {
        return (0 until functionAccess.konstueArgumentsCount).count { functionAccess.getValueArgument(it) != null } != functionAccess.symbol.owner.konstueParameters.size
    }

    private fun <T : IrFunctionAccessExpression> visitFunctionAccessExpression(expression: T, builder: (IrFunctionSymbol) -> T): IrExpression {
        if (!shouldReplaceWithSyntheticFunction(expression))
            return expression

        konst symbol = createStubFunction(expression) ?: return expression
        for (i in 0 until expression.typeArgumentsCount) {
            log { "$symbol[$i]: ${expression.getTypeArgument(i)}" }
        }
        konst stubFunction = symbol.owner
        stubFunction.typeParameters.forEach { log { "$stubFunction[${it.index}] : $it" } }

        konst declaration = expression.symbol.owner
        konst currentDeclaration = declarationStack.last()
        return context.createIrBuilder(
            currentDeclaration.symbol, startOffset = expression.startOffset, endOffset = expression.endOffset
        ).irBlock {
            konst newCall = builder(symbol).apply {
                konst offset = if (needsTypeArgumentOffset(declaration)) declaration.parentAsClass.typeParameters.size else 0
                for (i in 0 until typeArgumentsCount) {
                    putTypeArgument(i, expression.getTypeArgument(i + offset))
                }
                konst parameter2arguments = argumentsForCall(expression, stubFunction)

                for ((parameter, argument) in parameter2arguments) {
                    when (parameter) {
                        stubFunction.dispatchReceiverParameter -> log { "call::dispatch@: ${ir2string(argument)}" }
                        stubFunction.extensionReceiverParameter -> log { "call::extension@: ${ir2string(argument)}" }
                        else -> log { "call::params@$${parameter.index}/${parameter.name}: ${ir2string(argument)}" }
                    }
                    if (argument != null) {
                        putArgument(parameter, argument)
                    }
                }
            }

            +irCastIfNeeded(newCall, expression.type)
        }.unwrapBlock()
    }

    private fun needsTypeArgumentOffset(declaration: IrFunction) =
        isStatic(declaration) && declaration.parentAsClass.isMultiFieldValueClass && declaration is IrSimpleFunction

    override fun visitDelegatingConstructorCall(expression: IrDelegatingConstructorCall): IrExpression {
        expression.transformChildrenVoid()
        return visitFunctionAccessExpression(expression) {
            with(expression) {
                IrDelegatingConstructorCallImpl(
                    startOffset, endOffset, type, it as IrConstructorSymbol,
                    typeArgumentsCount = typeArgumentsCount,
                    konstueArgumentsCount = it.owner.konstueParameters.size
                )
            }
        }
    }

    override fun visitConstructorCall(expression: IrConstructorCall): IrExpression {
        expression.transformChildrenVoid()
        return visitFunctionAccessExpression(expression) {
            with(expression) {
                IrConstructorCallImpl.fromSymbolOwner(
                    startOffset,
                    endOffset,
                    type,
                    it as IrConstructorSymbol,
                    LoweredStatementOrigins.DEFAULT_DISPATCH_CALL
                )
            }
        }
    }

    override fun visitEnumConstructorCall(expression: IrEnumConstructorCall): IrExpression {
        expression.transformChildrenVoid()
        return visitFunctionAccessExpression(expression) {
            with(expression) {
                IrEnumConstructorCallImpl(
                    startOffset, endOffset, type, it as IrConstructorSymbol,
                    typeArgumentsCount = typeArgumentsCount,
                    konstueArgumentsCount = it.owner.konstueParameters.size
                )
            }
        }
    }

    override fun visitCall(expression: IrCall): IrExpression {
        expression.transformChildrenVoid()
        konst declaration = expression.symbol.owner
        konst typeParametersToRemove = if (needsTypeArgumentOffset(declaration)) declaration.parentAsClass.typeParameters.size else 0
        with(expression) {
            return visitFunctionAccessExpression(expression) {
                IrCallImpl(
                    startOffset, endOffset, (it as IrSimpleFunctionSymbol).owner.returnType, it,
                    typeArgumentsCount = typeArgumentsCount - typeParametersToRemove,
                    konstueArgumentsCount = it.owner.konstueParameters.size,
                    origin = LoweredStatementOrigins.DEFAULT_DISPATCH_CALL,
                    superQualifierSymbol = superQualifierSymbol
                )
            }
        }
    }

    private fun createStubFunction(expression: IrFunctionAccessExpression): IrFunctionSymbol? {
        konst declaration = expression.symbol.owner

        // We *have* to find the actual function here since on the JVM, a default stub for a function implemented
        // in an interface does not leave an abstract method after being moved to DefaultImpls (see InterfaceLowering).
        // Calling the fake override on an implementation of that interface would then result in a call to a method
        // that does not actually exist as DefaultImpls is not part of the inheritance hierarchy.
        konst baseFunction = factory.findBaseFunctionWithDefaultArgumentsFor(declaration, skipInline, skipExternalMethods)
        konst stubFunction = baseFunction?.let {
            factory.generateDefaultsFunction(
                it,
                skipInline,
                skipExternalMethods,
                forceSetOverrideSymbols,
                defaultArgumentStubVisibility(declaration),
                useConstructorMarker(declaration),
                baseFunction.copyAnnotations(),
            )
        } ?: return null
        log { "$declaration -> $stubFunction" }
        return stubFunction.symbol
    }

    protected open fun IrBlockBuilder.argumentsForCall(expression: IrFunctionAccessExpression, stubFunction: IrFunction): Map<IrValueParameter, IrExpression?> {
        konst startOffset = expression.startOffset
        konst endOffset = expression.endOffset
        konst declaration = expression.symbol.owner

        konst realArgumentsNumber = declaration.konstueParameters.filterNot { it.isMovedReceiver() }.size
        konst maskValues = IntArray((realArgumentsNumber + 31) / 32)

        assert(stubFunction.explicitParametersCount - declaration.explicitParametersCount - maskValues.size in listOf(0, 1)) {
            "argument count mismatch: expected $realArgumentsNumber arguments + ${maskValues.size} masks + optional handler/marker, " +
                    "got ${stubFunction.explicitParametersCount} total in ${stubFunction.render()}"
        }

        var sourceParameterIndex = -1
        return buildMap {
            konst konstueParametersPrefix: List<IrValueParameter> = if (isStatic(declaration)) {
                listOfNotNull(stubFunction.dispatchReceiverParameter, stubFunction.extensionReceiverParameter)
            } else {
                stubFunction.dispatchReceiverParameter?.let { put(it, expression.dispatchReceiver) }
                stubFunction.extensionReceiverParameter?.let { put(it, expression.extensionReceiver) }
                listOf()
            }
            for ((i, parameter) in (konstueParametersPrefix + stubFunction.konstueParameters).withIndex()) {
                if (!parameter.isMovedReceiver() && parameter != stubFunction.dispatchReceiverParameter && parameter != stubFunction.extensionReceiverParameter) {
                    ++sourceParameterIndex
                }
                konst newArgument = when {
                    sourceParameterIndex >= realArgumentsNumber + maskValues.size -> IrConstImpl.constNull(
                        startOffset,
                        endOffset,
                        parameter.type
                    )
                    sourceParameterIndex >= realArgumentsNumber -> IrConstImpl.int(
                        startOffset,
                        endOffset,
                        parameter.type,
                        maskValues[sourceParameterIndex - realArgumentsNumber]
                    )
                    else -> {
                        konst konstueArgument = expression.getValueArgument(i)
                        if (konstueArgument == null) {
                            maskValues[sourceParameterIndex / 32] =
                                maskValues[sourceParameterIndex / 32] or (1 shl (sourceParameterIndex % 32))
                        }
                        konstueArgument ?: nullConst(startOffset, endOffset, parameter)?.let {
                            IrCompositeImpl(
                                expression.startOffset,
                                expression.endOffset,
                                parameter.type,
                                IrStatementOrigin.DEFAULT_VALUE,
                                listOf(it)
                            )
                        }
                    }
                }
                put(parameter, newArgument)
            }
        }
    }

    protected open fun nullConst(startOffset: Int, endOffset: Int, irParameter: IrValueParameter): IrExpression? =
        if (irParameter.varargElementType != null) {
            null
        } else {
            nullConst(startOffset, endOffset, irParameter.type)
        }

    protected open fun nullConst(startOffset: Int, endOffset: Int, type: IrType): IrExpression =
        IrConstImpl.defaultValueForType(startOffset, endOffset, type)

    protected open fun defaultArgumentStubVisibility(function: IrFunction) = DescriptorVisibilities.PUBLIC

    protected open fun useConstructorMarker(function: IrFunction) = function is IrConstructor

    protected open fun isStatic(function: IrFunction): Boolean = false

    private fun log(msg: () -> String) = context.log { "DEFAULT-INJECTOR: ${msg()}" }

    protected fun IrValueParameter.isMovedReceiver() = isMovedReceiverImpl()
}

// Remove default argument initializers.
class DefaultParameterCleaner(
    konst context: CommonBackendContext,
    konst replaceDefaultValuesWithStubs: Boolean = false
) : DeclarationTransformer {
    override konst withLocalDeclarations: Boolean get() = true

    override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
        if (declaration is IrValueParameter && declaration.defaultValue != null) {
            if (replaceDefaultValuesWithStubs) {
                if (context.mapping.defaultArgumentsOriginalFunction[declaration.parent as IrFunction] == null) {
                    declaration.defaultValue = context.irFactory.createExpressionBody(
                        IrErrorExpressionImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, declaration.type, "Default Stub").apply {
                            attributeOwnerId = declaration.defaultValue!!.expression
                        }
                    )
                }
            } else {
                declaration.defaultValue = null
            }
        }
        return null
    }
}

// Sets overriden symbols. Should be used in case `forceSetOverrideSymbols = false`
class DefaultParameterPatchOverridenSymbolsLowering(
    konst context: CommonBackendContext
) : DeclarationTransformer {
    override fun transformFlat(declaration: IrDeclaration): List<IrDeclaration>? {
        if (declaration is IrSimpleFunction) {
            (context.mapping.defaultArgumentsOriginalFunction[declaration] as? IrSimpleFunction)?.run {
                declaration.overriddenSymbols = declaration.overriddenSymbols memoryOptimizedPlus overriddenSymbols.mapNotNull {
                    (context.mapping.defaultArgumentsDispatchFunction[it.owner] as? IrSimpleFunction)?.symbol
                }
            }
        }

        return null
    }
}

open class MaskedDefaultArgumentFunctionFactory(context: CommonBackendContext) : DefaultArgumentFunctionFactory(context) {
    final override fun IrFunction.generateDefaultArgumentStubFrom(original: IrFunction, useConstructorMarker: Boolean) {
        copyAttributesFrom(original)
        copyTypeParametersFrom(original)
        copyReturnTypeFrom(original)
        copyReceiversFrom(original)
        copyValueParametersFrom(original)

        for (i in 0 until (original.konstueParameters.size + 31) / 32) {
            addValueParameter(
                "mask$i".synthesizedString,
                context.irBuiltIns.intType,
                IrDeclarationOrigin.MASK_FOR_DEFAULT_FUNCTION
            )
        }

        if (useConstructorMarker) {
            konst markerType = context.ir.symbols.defaultConstructorMarker.defaultType.makeNullable()
            addValueParameter("marker".synthesizedString, markerType, IrDeclarationOrigin.DEFAULT_CONSTRUCTOR_MARKER)
        } else if (context.ir.shouldGenerateHandlerParameterForDefaultBodyFun()) {
            addValueParameter(
                "handler".synthesizedString,
                context.irBuiltIns.anyNType,
                IrDeclarationOrigin.METHOD_HANDLER_IN_DEFAULT_FUNCTION
            )
        }
        context.remapMultiFieldValueClassStructure(
            original, this, parametersMappingOrNull = original.explicitParameters.zip(explicitParameters).toMap()
        )
    }
}

private fun IrValueParameter.isMovedReceiver() =
    origin == IrDeclarationOrigin.MOVED_DISPATCH_RECEIVER || origin == IrDeclarationOrigin.MOVED_EXTENSION_RECEIVER
