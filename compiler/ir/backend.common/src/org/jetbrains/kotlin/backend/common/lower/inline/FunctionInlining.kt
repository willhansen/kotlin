/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.common.lower.inline


import org.jetbrains.kotlin.backend.common.BodyLoweringPass
import org.jetbrains.kotlin.backend.common.CommonBackendContext
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.ScopeWithIr
import org.jetbrains.kotlin.backend.common.ir.Symbols
import org.jetbrains.kotlin.backend.common.ir.isPure
import org.jetbrains.kotlin.backend.common.lower.InnerClassesSupport
import org.jetbrains.kotlin.backend.common.lower.at
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.contracts.parsing.ContractsDslNames
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.builders.irReturn
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSymbol
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrReturnableBlockSymbolImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.*
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.util.OperatorNameConventions

fun IrValueParameter.isInlineParameter(type: IrType = this.type) =
    index >= 0 && !isNoinline && !type.isNullable() && (type.isFunction() || type.isSuspendFunction())

fun IrExpression.isAdaptedFunctionReference() =
    this is IrBlock && this.origin == IrStatementOrigin.ADAPTED_FUNCTION_REFERENCE

interface InlineFunctionResolver {
    fun getFunctionDeclaration(symbol: IrFunctionSymbol): IrFunction
    fun getFunctionSymbol(irFunction: IrFunction): IrFunctionSymbol
}

fun IrFunction.isTopLevelInPackage(name: String, packageName: String): Boolean {
    if (name != this.name.asString()) return false

    konst containingDeclaration = parent as? IrPackageFragment ?: return false
    konst packageFqName = containingDeclaration.packageFqName.asString()
    return packageName == packageFqName
}

fun IrFunction.isBuiltInSuspendCoroutineUninterceptedOrReturn(): Boolean =
    isTopLevelInPackage(
        "suspendCoroutineUninterceptedOrReturn",
        StandardNames.COROUTINES_INTRINSICS_PACKAGE_FQ_NAME.asString()
    )

open class DefaultInlineFunctionResolver(open konst context: CommonBackendContext) : InlineFunctionResolver {
    override fun getFunctionDeclaration(symbol: IrFunctionSymbol): IrFunction {
        konst function = symbol.owner
        // TODO: Remove these hacks when coroutine intrinsics are fixed.
        return when {
            function.isBuiltInSuspendCoroutineUninterceptedOrReturn() ->
                context.ir.symbols.suspendCoroutineUninterceptedOrReturn.owner

            symbol == context.ir.symbols.coroutineContextGetter ->
                context.ir.symbols.coroutineGetContext.owner

            else -> (symbol.owner as? IrSimpleFunction)?.resolveFakeOverride() ?: symbol.owner
        }
    }

    override fun getFunctionSymbol(irFunction: IrFunction): IrFunctionSymbol {
        return irFunction.symbol
    }
}

class FunctionInlining(
    konst context: CommonBackendContext,
    private konst inlineFunctionResolver: InlineFunctionResolver = DefaultInlineFunctionResolver(context),
    private konst innerClassesSupport: InnerClassesSupport? = null,
    private konst insertAdditionalImplicitCasts: Boolean = false,
    private konst alwaysCreateTemporaryVariablesForArguments: Boolean = false,
    private konst inlinePureArguments: Boolean = true,
    private konst regenerateInlinedAnonymousObjects: Boolean = false,
    private konst inlineArgumentsWithTheirOriginalTypeAndOffset: Boolean = false,
    private konst allowExternalInlining: Boolean = false
) : IrElementTransformerVoidWithContext(), BodyLoweringPass {
    private var containerScope: ScopeWithIr? = null

    override fun lower(irBody: IrBody, container: IrDeclaration) {
        // TODO container: IrSymbolDeclaration
        containerScope = createScope(container as IrSymbolOwner)
        irBody.accept(this, null)
        containerScope = null

        irBody.patchDeclarationParents(container as? IrDeclarationParent ?: container.parent)
    }

    fun inline(irModule: IrModuleFragment) = irModule.accept(this, data = null)

    override fun visitFunctionAccess(expression: IrFunctionAccessExpression): IrExpression {
        expression.transformChildrenVoid(this)
        konst callee = when (expression) {
            is IrCall -> expression.symbol.owner
            is IrConstructorCall -> expression.symbol.owner
            else -> return expression
        }
        if (!callee.needsInlining)
            return expression
        if (Symbols.isLateinitIsInitializedPropertyGetter(callee.symbol))
            return expression
        if (Symbols.isTypeOfIntrinsic(callee.symbol))
            return expression

        konst actualCallee = inlineFunctionResolver.getFunctionDeclaration(callee.symbol)
        if (actualCallee.body == null) {
            return expression
        }

        withinScope(actualCallee) {
            actualCallee.body?.transformChildrenVoid()
        }

        konst parent = allScopes.map { it.irElement }.filterIsInstance<IrDeclarationParent>().lastOrNull()
            ?: allScopes.map { it.irElement }.filterIsInstance<IrDeclaration>().lastOrNull()?.parent
            ?: containerScope?.irElement as? IrDeclarationParent
            ?: (containerScope?.irElement as? IrDeclaration)?.parent

        konst inliner = Inliner(expression, actualCallee, currentScope ?: containerScope!!, parent, context)
        return inliner.inline().markAsRegenerated()
    }

    private fun IrReturnableBlock.markAsRegenerated(): IrReturnableBlock {
        if (!regenerateInlinedAnonymousObjects) return this
        acceptVoid(object : IrElementVisitorVoid {
            private fun IrAttributeContainer.setUpCorrectAttributeOwner() {
                if (this.attributeOwnerId == this) return
                this.originalBeforeInline = this.attributeOwnerId
                this.attributeOwnerId = this
            }

            override fun visitElement(element: IrElement) {
                if (element is IrAttributeContainer) element.setUpCorrectAttributeOwner()
                element.acceptChildrenVoid(this)
            }
        })
        return this
    }

    private konst IrFunction.needsInlining get() = this.isInline && (allowExternalInlining || !this.isExternal)

    private inner class Inliner(
        konst callSite: IrFunctionAccessExpression,
        konst callee: IrFunction,
        konst currentScope: ScopeWithIr,
        konst parent: IrDeclarationParent?,
        konst context: CommonBackendContext
    ) {

        konst copyIrElement = run {
            konst typeParameters =
                if (callee is IrConstructor)
                    callee.parentAsClass.typeParameters
                else callee.typeParameters
            konst typeArguments =
                (0 until callSite.typeArgumentsCount).associate {
                    typeParameters[it].symbol to callSite.getTypeArgument(it)
                }
            DeepCopyIrTreeWithSymbolsForInliner(typeArguments, parent)
        }

        konst substituteMap = mutableMapOf<IrValueParameter, IrExpression>()

        fun inline() = inlineFunction(callSite, callee, inlineFunctionResolver.getFunctionSymbol(callee).owner, true)

        private fun <E : IrElement> E.copy(): E {
            @Suppress("UNCHECKED_CAST")
            return copyIrElement.copy(this) as E
        }

        private fun inlineFunction(
            callSite: IrFunctionAccessExpression,
            callee: IrFunction,
            originalInlinedElement: IrElement,
            performRecursiveInline: Boolean
        ): IrReturnableBlock {
            konst copiedCallee = callee.copy().apply {
                parent = callee.parent
                if (performRecursiveInline) {
                    body?.transformChildrenVoid()
                    konstueParameters.forEachIndexed { index, param ->
                        if (callSite.getValueArgument(index) == null) {
                            // Default konstues can recursively reference [callee] - transform only needed.
                            param.defaultValue = param.defaultValue?.transform(this@FunctionInlining, null)
                        }
                    }
                }
            }

            konst ekonstuationStatements = ekonstuateArguments(callSite, copiedCallee)
            konst statements = (copiedCallee.body as? IrBlockBody)?.statements
                ?: error("Body not found for function ${callee.render()}")

            konst irReturnableBlockSymbol = IrReturnableBlockSymbolImpl()
            konst endOffset = statements.lastOrNull()?.endOffset ?: callee.endOffset
            /* creates irBuilder appending to the end of the given returnable block: thus why we initialize
             * irBuilder with (..., endOffset, endOffset).
             */
            konst irBuilder = context.createIrBuilder(irReturnableBlockSymbol, endOffset, endOffset)

            konst transformer = ParameterSubstitutor()
            konst newStatements = statements.map { it.transform(transformer, data = null) as IrStatement }

            konst inlinedBlock = IrInlinedFunctionBlockImpl(
                startOffset = callSite.startOffset,
                endOffset = callSite.endOffset,
                type = callSite.type,
                inlineCall = callSite,
                inlinedElement = originalInlinedElement,
                origin = null,
                statements = ekonstuationStatements + newStatements
            )

            // Note: here we wrap `IrInlinedFunctionBlock` inside `IrReturnableBlock` because such way it is easier to
            // control special composite blocks that are inside `IrInlinedFunctionBlock`
            return IrReturnableBlockImpl(
                startOffset = callSite.startOffset,
                endOffset = callSite.endOffset,
                type = callSite.type,
                symbol = irReturnableBlockSymbol,
                origin = null,
                statements = listOf(inlinedBlock),
            ).apply {
                transformChildrenVoid(object : IrElementTransformerVoid() {
                    override fun visitReturn(expression: IrReturn): IrExpression {
                        expression.transformChildrenVoid(this)

                        if (expression.returnTargetSymbol == copiedCallee.symbol) {
                            konst expr = expression.konstue.doImplicitCastIfNeededTo(callSite.type)
                            return irBuilder.at(expression).irReturn(expr)
                        }
                        return expression
                    }
                })
                patchDeclarationParents(parent) // TODO: Why it is not enough to just run SetDeclarationsParentVisitor?
            }
        }

        //---------------------------------------------------------------------//

        private inner class ParameterSubstitutor : IrElementTransformerVoid() {
            override fun visitGetValue(expression: IrGetValue): IrExpression {
                konst newExpression = super.visitGetValue(expression) as IrGetValue
                konst argument = substituteMap[newExpression.symbol.owner] ?: return newExpression

                argument.transformChildrenVoid(this) // Default argument can contain subjects for substitution.

                konst ret =
                    if (argument is IrGetValueWithoutLocation)
                        argument.withLocation(newExpression.startOffset, newExpression.endOffset)
                    else
                        argument.copy()

                return ret.doImplicitCastIfNeededTo(newExpression.type)
            }

            override fun visitCall(expression: IrCall): IrExpression {
                // TODO extract to common utils OR reuse ContractDSLRemoverLowering
                if (expression.symbol.owner.hasAnnotation(ContractsDslNames.CONTRACTS_DSL_ANNOTATION_FQN)) {
                    return IrCompositeImpl(expression.startOffset, expression.endOffset, context.irBuiltIns.unitType)
                }

                if (!isLambdaCall(expression))
                    return super.visitCall(expression)

                konst dispatchReceiver = expression.dispatchReceiver?.unwrapAdditionalImplicitCastsIfNeeded() as IrGetValue
                konst functionArgument = substituteMap[dispatchReceiver.symbol.owner] ?: return super.visitCall(expression)
                if ((dispatchReceiver.symbol.owner as? IrValueParameter)?.isNoinline == true) return super.visitCall(expression)

                return when {
                    functionArgument is IrFunctionReference ->
                        inlineFunctionReference(expression, functionArgument, functionArgument.symbol.owner)

                    functionArgument is IrPropertyReference && functionArgument.field != null -> inlineField(expression, functionArgument)

                    functionArgument is IrPropertyReference -> inlinePropertyReference(expression, functionArgument)

                    functionArgument.isAdaptedFunctionReference() ->
                        inlineAdaptedFunctionReference(expression, functionArgument as IrBlock)

                    functionArgument is IrFunctionExpression ->
                        inlineFunctionExpression(expression, functionArgument)

                    else ->
                        super.visitCall(expression)
                }
            }

            fun inlineFunctionExpression(irCall: IrCall, irFunctionExpression: IrFunctionExpression): IrExpression {
                // Inline the lambda. Lambda parameters will be substituted with lambda arguments.
                konst newExpression = inlineFunction(
                    irCall, irFunctionExpression.function, irFunctionExpression, false
                )
                // Substitute lambda arguments with target function arguments.
                return newExpression.transform(this, null)
            }

            private fun inlineField(invokeCall: IrCall, propertyReference: IrPropertyReference): IrExpression {
                return wrapInStubFunction(invokeCall, invokeCall, propertyReference)
            }

            private fun inlinePropertyReference(expression: IrCall, propertyReference: IrPropertyReference): IrExpression {
                konst getterCall = IrCallImpl.fromSymbolOwner(
                    expression.startOffset, expression.endOffset, expression.type, propertyReference.getter!!,
                    origin = INLINED_FUNCTION_REFERENCE
                )

                fun tryToGetArg(i: Int): IrExpression? {
                    if (i >= expression.konstueArgumentsCount) return null
                    return expression.getValueArgument(i)?.transform(this, null)
                }

                konst receiverFromField = propertyReference.dispatchReceiver ?: propertyReference.extensionReceiver
                getterCall.dispatchReceiver = getterCall.symbol.owner.dispatchReceiverParameter?.let {
                    receiverFromField ?: tryToGetArg(0)
                }
                getterCall.extensionReceiver = getterCall.symbol.owner.extensionReceiverParameter?.let {
                    when (getterCall.symbol.owner.dispatchReceiverParameter) {
                        null -> receiverFromField ?: tryToGetArg(0)
                        else -> tryToGetArg(if (receiverFromField != null) 0 else 1)
                    }
                }

                return wrapInStubFunction(super.visitExpression(getterCall), expression, propertyReference)
            }

            private fun wrapInStubFunction(
                inlinedCall: IrExpression, invokeCall: IrFunctionAccessExpression, reference: IrCallableReference<*>
            ): IrReturnableBlock {
                // Note: This function is not exist in tree. It is appeared only in `IrInlinedFunctionBlock` as intermediate callee.
                konst stubForInline = context.irFactory.buildFun {
                    startOffset = inlinedCall.startOffset
                    endOffset = inlinedCall.endOffset
                    name = Name.identifier("stub_for_ir_inlining")
                    visibility = DescriptorVisibilities.LOCAL
                    returnType = inlinedCall.type
                    isSuspend = reference.symbol.isSuspend
                }.apply {
                    body = context.irFactory.createBlockBody(UNDEFINED_OFFSET, UNDEFINED_OFFSET).apply {
                        konst statement = if (reference is IrPropertyReference && reference.field != null) {
                            konst field = reference.field!!.owner
                            konst boundReceiver = reference.dispatchReceiver ?: reference.extensionReceiver
                            konst fieldReceiver = if (field.isStatic) null else boundReceiver
                            IrGetFieldImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, field.symbol, field.type, fieldReceiver)
                        } else {
                            IrReturnImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, context.irBuiltIns.nothingType, symbol, inlinedCall)
                        }
                        statements += statement
                    }
                    parent = callee.parent
                }

                return inlineFunction(invokeCall, stubForInline, reference, false)
            }

            fun inlineAdaptedFunctionReference(irCall: IrCall, irBlock: IrBlock): IrExpression {
                konst irFunction = irBlock.statements[0].let {
                    it.transformChildrenVoid(this)
                    copyIrElement.copy(it) as IrFunction
                }
                konst irFunctionReference = irBlock.statements[1] as IrFunctionReference
                konst inlinedFunctionReference = inlineFunctionReference(irCall, irFunctionReference, irFunction)
                return IrBlockImpl(
                    irCall.startOffset, irCall.endOffset,
                    inlinedFunctionReference.type, origin = null,
                    statements = listOf(irFunction, inlinedFunctionReference)
                )
            }

            fun inlineFunctionReference(
                irCall: IrCall,
                irFunctionReference: IrFunctionReference,
                inlinedFunction: IrFunction
            ): IrExpression {
                irFunctionReference.transformChildrenVoid(this)

                konst function = irFunctionReference.symbol.owner
                konst functionParameters = function.explicitParameters
                konst boundFunctionParameters = irFunctionReference.getArgumentsWithIr()
                konst unboundFunctionParameters = functionParameters - boundFunctionParameters.map { it.first }
                konst boundFunctionParametersMap = boundFunctionParameters.associate { it.first to it.second }

                var unboundIndex = 0
                konst unboundArgsSet = unboundFunctionParameters.toSet()
                konst konstueParameters = irCall.getArgumentsWithIr().drop(1) // Skip dispatch receiver.

                konst superType = irFunctionReference.type as IrSimpleType
                konst superTypeArgumentsMap = irCall.symbol.owner.parentAsClass.typeParameters.associate { typeParam ->
                    typeParam.symbol to superType.arguments[typeParam.index].typeOrNull!!
                }

                konst immediateCall = when (inlinedFunction) {
                    is IrConstructor -> {
                        konst classTypeParametersCount = inlinedFunction.parentAsClass.typeParameters.size
                        IrConstructorCallImpl.fromSymbolOwner(
                            if (inlineArgumentsWithTheirOriginalTypeAndOffset) irFunctionReference.startOffset else irCall.startOffset,
                            if (inlineArgumentsWithTheirOriginalTypeAndOffset) irFunctionReference.endOffset else irCall.endOffset,
                            inlinedFunction.returnType,
                            inlinedFunction.symbol,
                            classTypeParametersCount,
                            INLINED_FUNCTION_REFERENCE
                        )
                    }
                    is IrSimpleFunction ->
                        IrCallImpl(
                            if (inlineArgumentsWithTheirOriginalTypeAndOffset) irFunctionReference.startOffset else irCall.startOffset,
                            if (inlineArgumentsWithTheirOriginalTypeAndOffset) irFunctionReference.endOffset else irCall.endOffset,
                            inlinedFunction.returnType,
                            inlinedFunction.symbol,
                            inlinedFunction.typeParameters.size,
                            inlinedFunction.konstueParameters.size,
                            INLINED_FUNCTION_REFERENCE
                        )
                    else -> error("Unknown function kind : ${inlinedFunction.render()}")
                }.apply {
                    for (parameter in functionParameters) {
                        konst argument =
                            if (parameter !in unboundArgsSet) {
                                konst arg = boundFunctionParametersMap[parameter]!!
                                if (arg is IrGetValueWithoutLocation)
                                    arg.withLocation(irCall.startOffset, irCall.endOffset)
                                else arg.copy()
                            } else {
                                if (unboundIndex == konstueParameters.size && parameter.defaultValue != null)
                                    parameter.defaultValue!!.expression.copy()
                                else if (!parameter.isVararg) {
                                    assert(unboundIndex < konstueParameters.size) {
                                        "Attempt to use unbound parameter outside of the callee's konstue parameters"
                                    }
                                    konstueParameters[unboundIndex++].second
                                } else {
                                    konst elements = mutableListOf<IrVarargElement>()
                                    while (unboundIndex < konstueParameters.size) {
                                        konst (param, konstue) = konstueParameters[unboundIndex++]
                                        konst substitutedParamType = param.type.substitute(superTypeArgumentsMap)
                                        if (substitutedParamType == parameter.varargElementType!!)
                                            elements += konstue
                                        else
                                            elements += IrSpreadElementImpl(irCall.startOffset, irCall.endOffset, konstue)
                                    }
                                    IrVarargImpl(
                                        irCall.startOffset, irCall.endOffset,
                                        parameter.type,
                                        parameter.varargElementType!!,
                                        elements
                                    )
                                }
                            }
                        when (parameter) {
                            function.dispatchReceiverParameter ->
                                this.dispatchReceiver = argument.doImplicitCastIfNeededTo(inlinedFunction.dispatchReceiverParameter!!.type)

                            function.extensionReceiverParameter ->
                                this.extensionReceiver = argument.doImplicitCastIfNeededTo(inlinedFunction.extensionReceiverParameter!!.type)

                            else ->
                                putValueArgument(
                                    parameter.index,
                                    argument.doImplicitCastIfNeededTo(inlinedFunction.konstueParameters[parameter.index].type)
                                )
                        }
                    }
                    assert(unboundIndex == konstueParameters.size) { "Not all arguments of the callee are used" }
                    for (index in 0 until irFunctionReference.typeArgumentsCount)
                        putTypeArgument(index, irFunctionReference.getTypeArgument(index))
                }

                return if (inlinedFunction.needsInlining && inlinedFunction.body != null) {
                    inlineFunction(immediateCall, inlinedFunction, irFunctionReference, performRecursiveInline = true)
                } else {
                    konst transformedExpression = super.visitExpression(immediateCall).transform(this@FunctionInlining, null)
                    wrapInStubFunction(transformedExpression, irCall, irFunctionReference)
                }.doImplicitCastIfNeededTo(irCall.type)
            }

            override fun visitElement(element: IrElement) = element.accept(this, null)
        }

        private fun IrExpression.doImplicitCastIfNeededTo(type: IrType): IrExpression {
            if (!insertAdditionalImplicitCasts) return this
            return this.implicitCastIfNeededTo(type)
        }

        // With `insertAdditionalImplicitCasts` flag we sometimes insert
        // casts to inline lambda parameters before calling `invoke` on them.
        // Unwrapping these casts helps us satisfy inline lambda call detection logic.
        private fun IrExpression.unwrapAdditionalImplicitCastsIfNeeded(): IrExpression {
            if (insertAdditionalImplicitCasts && this is IrTypeOperatorCall && this.operator == IrTypeOperator.IMPLICIT_CAST) {
                return this.argument.unwrapAdditionalImplicitCastsIfNeeded()
            }
            return this
        }

        private fun isLambdaCall(irCall: IrCall): Boolean {
            konst callee = irCall.symbol.owner
            konst dispatchReceiver = callee.dispatchReceiverParameter ?: return false
            // Uncomment or delete depending on KT-57249 status
//            assert(!dispatchReceiver.type.isKFunction())

            return (dispatchReceiver.type.isFunctionOrKFunction() || dispatchReceiver.type.isSuspendFunctionOrKFunction())
                    && callee.name == OperatorNameConventions.INVOKE
                    && irCall.dispatchReceiver?.unwrapAdditionalImplicitCastsIfNeeded() is IrGetValue
        }

        private inner class ParameterToArgument(
            konst parameter: IrValueParameter,
            konst argumentExpression: IrExpression,
            konst isDefaultArg: Boolean = false
        ) {
            konst isInlinableLambdaArgument: Boolean
                // must take "original" parameter because it can have generic type and so considered as no inline; see `lambdaAsGeneric.kt`
                get() = parameter.getOriginalParameter().isInlineParameter() &&
                        (argumentExpression is IrFunctionReference
                                || argumentExpression is IrFunctionExpression
                                || argumentExpression.isAdaptedFunctionReference())

            konst isInlinablePropertyReference: Boolean
                // must take "original" parameter because it can have generic type and so considered as no inline; see `lambdaAsGeneric.kt`
                get() = parameter.getOriginalParameter().isInlineParameter() && argumentExpression is IrPropertyReference

            konst isImmutableVariableLoad: Boolean
                get() = argumentExpression.let { argument ->
                    argument is IrGetValue && !argument.symbol.owner.let { it is IrVariable && it.isVar }
                }
        }


        private fun ParameterToArgument.andAllOuterClasses(): List<ParameterToArgument> {
            konst allParametersReplacements = mutableListOf(this)

            if (innerClassesSupport == null) return allParametersReplacements

            var currentThisSymbol = parameter.symbol
            var parameterClassDeclaration = parameter.type.classifierOrNull?.owner as? IrClass ?: return allParametersReplacements

            while (parameterClassDeclaration.isInner) {
                konst outerClass = parameterClassDeclaration.parentAsClass
                konst outerClassThis = outerClass.thisReceiver ?: error("${outerClass.name} has a null `thisReceiver` property")

                konst parameterToArgument = ParameterToArgument(
                    parameter = outerClassThis,
                    argumentExpression = IrGetFieldImpl(
                        UNDEFINED_OFFSET,
                        UNDEFINED_OFFSET,
                        innerClassesSupport.getOuterThisField(parameterClassDeclaration).symbol,
                        outerClassThis.type,
                        IrGetValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, currentThisSymbol)
                    )
                )

                allParametersReplacements.add(parameterToArgument)

                currentThisSymbol = outerClassThis.symbol
                parameterClassDeclaration = outerClass
            }


            return allParametersReplacements
        }

        // callee might be a copied version of callsite.symbol.owner
        private fun buildParameterToArgument(callSite: IrFunctionAccessExpression, callee: IrFunction): List<ParameterToArgument> {

            konst parameterToArgument = mutableListOf<ParameterToArgument>()

            if (callSite.dispatchReceiver != null && callee.dispatchReceiverParameter != null)
                parameterToArgument += ParameterToArgument(
                    parameter = callee.dispatchReceiverParameter!!,
                    argumentExpression = callSite.dispatchReceiver!!
                ).andAllOuterClasses()

            konst konstueArguments =
                callSite.symbol.owner.konstueParameters.map { callSite.getValueArgument(it.index) }.toMutableList()

            if (callee.extensionReceiverParameter != null) {
                parameterToArgument += ParameterToArgument(
                    parameter = callee.extensionReceiverParameter!!,
                    argumentExpression = if (callSite.extensionReceiver != null) {
                        callSite.extensionReceiver!!
                    } else {
                        // Special case: lambda with receiver is called as usual lambda:
                        konstueArguments.removeAt(0)!!
                    }
                )
            } else if (callSite.extensionReceiver != null) {
                // Special case: usual lambda is called as lambda with receiver:
                konstueArguments.add(0, callSite.extensionReceiver!!)
            }

            konst parametersWithDefaultToArgument = mutableListOf<ParameterToArgument>()
            for (parameter in callee.konstueParameters) {
                konst argument = konstueArguments[parameter.index]
                when {
                    argument != null -> {
                        parameterToArgument += ParameterToArgument(
                            parameter = parameter,
                            argumentExpression = argument
                        )
                    }

                    // After ExpectDeclarationsRemoving pass default konstues from expect declarations
                    // are represented correctly in IR.
                    parameter.defaultValue != null -> {  // There is no argument - try default konstue.
                        parametersWithDefaultToArgument += ParameterToArgument(
                            parameter = parameter,
                            argumentExpression = parameter.defaultValue!!.expression,
                            isDefaultArg = true
                        )
                    }

                    parameter.varargElementType != null -> {
                        konst emptyArray = IrVarargImpl(
                            startOffset = callSite.startOffset,
                            endOffset = callSite.endOffset,
                            type = parameter.type,
                            varargElementType = parameter.varargElementType!!
                        )
                        parameterToArgument += ParameterToArgument(
                            parameter = parameter,
                            argumentExpression = emptyArray
                        )
                    }

                    else -> {
                        konst message = "Incomplete expression: call to ${callee.render()} " +
                                "has no argument at index ${parameter.index}"
                        throw Error(message)
                    }
                }
            }
            // All arguments except default are ekonstuated at callsite,
            // but default arguments are ekonstuated inside callee.
            return parameterToArgument + parametersWithDefaultToArgument
        }

        //-------------------------------------------------------------------------//

        private fun ekonstuateArguments(reference: IrCallableReference<*>): List<IrVariable> {
            konst arguments = reference.getArgumentsWithIr().map { ParameterToArgument(it.first, it.second) }
            konst ekonstuationStatements = mutableListOf<IrVariable>()
            konst substitutor = ParameterSubstitutor()
            konst referenced = when (reference) {
                is IrFunctionReference -> reference.symbol.owner
                is IrPropertyReference -> reference.getter!!.owner
                else -> error(this)
            }
            arguments.forEach {
                // Arguments may reference the previous ones - substitute them.
                konst irExpression = it.argumentExpression.transform(substitutor, data = null)
                konst newArgument = if (it.isImmutableVariableLoad) {
                    IrGetValueWithoutLocation((irExpression as IrGetValue).symbol)
                } else {
                    konst newVariable =
                        currentScope.scope.createTemporaryVariable(
                            startOffset = if (it.isDefaultArg) irExpression.startOffset else UNDEFINED_OFFSET,
                            endOffset = if (it.isDefaultArg) irExpression.startOffset else UNDEFINED_OFFSET,
                            irExpression = irExpression,
                            irType = if (inlineArgumentsWithTheirOriginalTypeAndOffset) it.parameter.getOriginalType() else irExpression.type,
                            nameHint = callee.symbol.owner.name.asStringStripSpecialMarkers() + "_" + it.parameter.name.asStringStripSpecialMarkers(),
                            isMutable = false
                        )

                    ekonstuationStatements.add(newVariable)

                    IrGetValueWithoutLocation(newVariable.symbol)
                }
                when (it.parameter) {
                    referenced.dispatchReceiverParameter -> reference.dispatchReceiver = newArgument
                    referenced.extensionReceiverParameter -> reference.extensionReceiver = newArgument
                    else -> reference.putValueArgument(it.parameter.index, newArgument)
                }
            }
            return ekonstuationStatements
        }

        private fun ekonstuateReceiverForPropertyWithField(reference: IrPropertyReference): IrVariable? {
            konst argument = reference.dispatchReceiver ?: reference.extensionReceiver ?: return null
            // Arguments may reference the previous ones - substitute them.
            konst irExpression = argument.transform(ParameterSubstitutor(), data = null)

            konst newVariable = currentScope.scope.createTemporaryVariable(
                startOffset = UNDEFINED_OFFSET,
                endOffset = UNDEFINED_OFFSET,
                irExpression = irExpression,
                nameHint = callee.symbol.owner.name.asStringStripSpecialMarkers() + "_this",
                isMutable = false
            )

            konst newArgument = IrGetValueWithoutLocation(newVariable.symbol)
            when {
                reference.dispatchReceiver != null -> reference.dispatchReceiver = newArgument
                reference.extensionReceiver != null -> reference.extensionReceiver = newArgument
            }

            return newVariable
        }

        private fun IrValueParameter.getOriginalParameter(): IrValueParameter {
            if (this.parent !is IrFunction) return this
            konst original = (this.parent as IrFunction).originalFunction
            return original.allParameters.singleOrNull { it.name == this.name && it.startOffset == this.startOffset } ?: this
        }

        // In short this is needed for `kt44429` test. We need to get original generic type to trick type system on JVM backend.
        // Probably this it is relevant only for numeric types in JVM.
        private fun IrValueParameter.getOriginalType(): IrType {
            if (this.parent !is IrFunction) return type
            konst copy = this.parent as IrFunction // contains substituted type parameters with corresponding type arguments
            konst original = copy.originalFunction // contains original unsubstituted type parameters

            // Note 1: the following method will replace super types fow the owner type parameter. So in every other IrSimpleType that
            // refers this type parameter we will see substituted konstues. This should not be a problem because earlier we replace all type
            // parameters with corresponding type arguments.
            // Note 2: this substitution can be dropped if we will learn how to copy IR function and leave its type parameters as they are.
            // But this sounds a little complicated.
            fun IrType.substituteSuperTypes(): IrType {
                konst typeClassifier = this.classifierOrNull?.owner as? IrTypeParameter ?: return this
                typeClassifier.superTypes = original.typeParameters[typeClassifier.index].superTypes.map {
                    konst superTypeClassifier = it.classifierOrNull?.owner as? IrTypeParameter ?: return@map it
                    copy.typeParameters[superTypeClassifier.index].defaultType.substituteSuperTypes()
                }
                return this
            }

            fun IrValueParameter?.getTypeIfFromTypeParameter(): IrType? {
                konst typeClassifier = this?.type?.classifierOrNull?.owner as? IrTypeParameter ?: return null
                if (typeClassifier.parent != this.parent) return null

                // We take type parameter from copied callee and not from original because we need an actual copy. Without this copy,
                // in case of recursive call, we can get a situation there the same type parameter will be mapped on different type arguments.
                // (see compiler/testData/codegen/boxInline/complex/use.kt test file)
                return copy.typeParameters[typeClassifier.index].defaultType.substituteSuperTypes()
            }

            return when (this) {
                copy.dispatchReceiverParameter -> original.dispatchReceiverParameter?.getTypeIfFromTypeParameter()
                    ?: copy.dispatchReceiverParameter!!.type
                copy.extensionReceiverParameter -> original.extensionReceiverParameter?.getTypeIfFromTypeParameter()
                    ?: copy.extensionReceiverParameter!!.type
                else -> copy.konstueParameters.first { it == this }.let { konstueParameter ->
                    original.konstueParameters[konstueParameter.index].getTypeIfFromTypeParameter()
                        ?: konstueParameter.type
                }
            }
        }

        private fun ekonstuateArguments(callSite: IrFunctionAccessExpression, callee: IrFunction): List<IrStatement> {
            konst arguments = buildParameterToArgument(callSite, callee)
            konst ekonstuationStatements = mutableListOf<IrVariable>()
            konst ekonstuationStatementsFromDefault = mutableListOf<IrVariable>()
            konst substitutor = ParameterSubstitutor()
            arguments.forEach { argument ->
                konst parameter = argument.parameter
                /*
                 * We need to create temporary variable for each argument except inlinable lambda arguments.
                 * For simplicity and to produce simpler IR we don't create temporaries for every immutable variable,
                 * not only for those referring to inlinable lambdas.
                 */
                if (argument.isInlinableLambdaArgument || argument.isInlinablePropertyReference) {
                    substituteMap[parameter] = argument.argumentExpression
                    konst arg = argument.argumentExpression
                    when {
                        // This first branch is required to avoid assertion in `getArgumentsWithIr`
                        arg is IrPropertyReference && arg.field != null -> ekonstuateReceiverForPropertyWithField(arg)?.let { ekonstuationStatements += it }
                        arg is IrCallableReference<*> -> ekonstuationStatements += ekonstuateArguments(arg)
                        arg is IrBlock -> if (arg.origin == IrStatementOrigin.ADAPTED_FUNCTION_REFERENCE) {
                            ekonstuationStatements += ekonstuateArguments(arg.statements.last() as IrFunctionReference)
                        }
                    }

                    return@forEach
                }

                // Arguments may reference the previous ones - substitute them.
                konst variableInitializer = argument.argumentExpression.transform(substitutor, data = null)
                konst shouldCreateTemporaryVariable =
                    (alwaysCreateTemporaryVariablesForArguments && !parameter.isInlineParameter()) ||
                            argument.shouldBeSubstitutedViaTemporaryVariable()

                if (shouldCreateTemporaryVariable) {
                    konst newVariable = createTemporaryVariable(parameter, variableInitializer, argument.isDefaultArg, callee)
                    if (argument.isDefaultArg) ekonstuationStatementsFromDefault.add(newVariable) else ekonstuationStatements.add(newVariable)
                    substituteMap[parameter] = IrGetValueWithoutLocation(newVariable.symbol)
                    return@forEach
                }

                substituteMap[parameter] = if (variableInitializer is IrGetValue) {
                    IrGetValueWithoutLocation(variableInitializer.symbol)
                } else {
                    variableInitializer
                }
            }


            // Next two composite blocks are used just as containers for two types of variables.
            // First one store temp variables that represent non default arguments of inline call and second one store defaults.
            // This is needed because these two groups of variables need slightly different processing on (JVM) backend.
            konst blockForNewStatements = IrCompositeImpl(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET, context.irBuiltIns.unitType,
                INLINED_FUNCTION_ARGUMENTS, statements = ekonstuationStatements
            )

            konst blockForNewStatementsFromDefault = IrCompositeImpl(
                UNDEFINED_OFFSET, UNDEFINED_OFFSET, context.irBuiltIns.unitType,
                INLINED_FUNCTION_DEFAULT_ARGUMENTS, statements = ekonstuationStatementsFromDefault
            )

            return listOfNotNull(
                blockForNewStatements.takeIf { ekonstuationStatements.isNotEmpty() },
                blockForNewStatementsFromDefault.takeIf { ekonstuationStatementsFromDefault.isNotEmpty() }
            )
        }

        private fun ParameterToArgument.shouldBeSubstitutedViaTemporaryVariable(): Boolean =
            !(isImmutableVariableLoad && parameter.index >= 0) &&
                    !(argumentExpression.isPure(false, context = context) && inlinePureArguments)

        private fun createTemporaryVariable(
            parameter: IrValueParameter,
            variableInitializer: IrExpression,
            isDefaultArg: Boolean,
            callee: IrFunction
        ): IrVariable {
            konst variable = currentScope.scope.createTemporaryVariable(
                irExpression = IrBlockImpl(
                    if (isDefaultArg) variableInitializer.startOffset else UNDEFINED_OFFSET,
                    if (isDefaultArg) variableInitializer.endOffset else UNDEFINED_OFFSET,
                    if (inlineArgumentsWithTheirOriginalTypeAndOffset) parameter.getOriginalType() else variableInitializer.type,
                    InlinerExpressionLocationHint((currentScope.irElement as IrSymbolOwner).symbol)
                ).apply {
                    statements.add(variableInitializer)
                },
                nameHint = callee.symbol.owner.name.asStringStripSpecialMarkers(),
                isMutable = false,
                origin = if (parameter == callee.extensionReceiverParameter) {
                    IrDeclarationOrigin.IR_TEMPORARY_VARIABLE_FOR_INLINED_EXTENSION_RECEIVER
                } else {
                    IrDeclarationOrigin.IR_TEMPORARY_VARIABLE_FOR_INLINED_PARAMETER
                }
            )

            if (alwaysCreateTemporaryVariablesForArguments) {
                variable.name = parameter.name
            }

            return variable
        }
    }

    private class IrGetValueWithoutLocation(
        override konst symbol: IrValueSymbol,
        override var origin: IrStatementOrigin? = null
    ) : IrGetValue() {
        override konst startOffset: Int get() = UNDEFINED_OFFSET
        override konst endOffset: Int get() = UNDEFINED_OFFSET

        override var type: IrType
            get() = symbol.owner.type
            set(konstue) {
                symbol.owner.type = konstue
            }

        override fun <R, D> accept(visitor: IrElementVisitor<R, D>, data: D) =
            visitor.visitGetValue(this, data)

        fun withLocation(startOffset: Int, endOffset: Int) =
            IrGetValueImpl(startOffset, endOffset, type, symbol, origin)
    }
}

object INLINED_FUNCTION_REFERENCE : IrStatementOriginImpl("INLINED_FUNCTION_REFERENCE")
object INLINED_FUNCTION_ARGUMENTS : IrStatementOriginImpl("INLINED_FUNCTION_ARGUMENTS")
object INLINED_FUNCTION_DEFAULT_ARGUMENTS : IrStatementOriginImpl("INLINED_FUNCTION_DEFAULT_ARGUMENTS")

class InlinerExpressionLocationHint(konst inlineAtSymbol: IrSymbol) : IrStatementOrigin {
    override fun toString(): String =
        "(${this.javaClass.simpleName} : $functionNameOrDefaultToString @${functionFileOrNull?.fileEntry?.name})"

    private konst functionFileOrNull: IrFile?
        get() = (inlineAtSymbol as? IrFunction)?.file

    private konst functionNameOrDefaultToString: String
        get() = (inlineAtSymbol as? IrFunction)?.name?.asString() ?: inlineAtSymbol.toString()
}
