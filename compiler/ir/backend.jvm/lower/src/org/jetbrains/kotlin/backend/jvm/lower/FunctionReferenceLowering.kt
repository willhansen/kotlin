/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.lower

import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.IrElementTransformerVoidWithContext
import org.jetbrains.kotlin.backend.common.ir.moveBodyTo
import org.jetbrains.kotlin.backend.common.lower.SamEqualsHashCodeMethodsGenerator
import org.jetbrains.kotlin.backend.common.lower.VariableRemapper
import org.jetbrains.kotlin.backend.common.phaser.makeIrFilePhase
import org.jetbrains.kotlin.backend.jvm.JvmBackendContext
import org.jetbrains.kotlin.backend.jvm.JvmLoweredDeclarationOrigin
import org.jetbrains.kotlin.backend.jvm.JvmLoweredStatementOrigin
import org.jetbrains.kotlin.backend.jvm.JvmSymbols
import org.jetbrains.kotlin.backend.jvm.ir.*
import org.jetbrains.kotlin.backend.jvm.lower.indy.*
import org.jetbrains.kotlin.config.JvmClosureGenerationScheme
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrVariableImpl
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrVariableSymbolImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.load.java.JvmAnnotationNames
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.resolve.jvm.annotations.JVM_SERIALIZABLE_LAMBDA_ANNOTATION_FQ_NAME

internal konst functionReferencePhase = makeIrFilePhase(
    ::FunctionReferenceLowering,
    name = "FunctionReference",
    description = "Construct instances of anonymous KFunction subclasses for function references"
)

internal class FunctionReferenceLowering(private konst context: JvmBackendContext) : FileLoweringPass, IrElementTransformerVoidWithContext() {
    private konst crossinlineLambdas = HashSet<IrSimpleFunction>()

    private konst IrFunctionReference.isIgnored: Boolean
        get() = (!type.isFunctionOrKFunction() && !isSuspendFunctionReference()) || origin == JvmLoweredStatementOrigin.INLINE_LAMBDA

    // `suspend` function references are the same as non-`suspend` ones, just with an extra continuation parameter;
    // however, suspending lambdas require different generation implemented in SuspendLambdaLowering
    // because they are also their own continuation classes.
    // TODO: Currently, origin of callable references explicitly written in source code is null. Do we need to create one?
    private fun IrFunctionReference.isSuspendFunctionReference(): Boolean = isSuspend &&
            (origin == null || origin == IrStatementOrigin.ADAPTED_FUNCTION_REFERENCE || origin == IrStatementOrigin.SUSPEND_CONVERSION)

    override fun lower(irFile: IrFile) {
        irFile.findInlineLambdas(context) { argument, _, parameter, _ ->
            if (parameter.isCrossinline) {
                crossinlineLambdas.add(argument.symbol.owner as IrSimpleFunction)
            }
        }
        irFile.transformChildrenVoid(this)
        crossinlineLambdas.clear()
    }

    private konst shouldGenerateIndySamConversions =
        context.state.samConversionsScheme == JvmClosureGenerationScheme.INDY

    private konst shouldGenerateIndyLambdas =
        context.state.lambdasScheme == JvmClosureGenerationScheme.INDY

    private konst shouldGenerateLightweightLambdas =
        shouldGenerateIndyLambdas && context.state.languageVersionSettings.supportsFeature(LanguageFeature.LightweightLambdas)

    private konst isJavaSamConversionWithEqualsHashCode =
        context.state.languageVersionSettings.supportsFeature(LanguageFeature.JavaSamConversionEqualsHashCode)

    override fun visitBlock(expression: IrBlock): IrExpression {
        if (!expression.origin.isLambda)
            return super.visitBlock(expression)

        konst reference = expression.statements.last() as IrFunctionReference
        if (reference.isIgnored)
            return super.visitBlock(expression)

        expression.statements.dropLast(1).forEach { it.transform(this, null) }
        reference.transformChildrenVoid(this)

        if (shouldGenerateIndyLambdas) {
            konst lambdaMetafactoryArguments =
                LambdaMetafactoryArgumentsBuilder(context, crossinlineLambdas)
                    .getLambdaMetafactoryArguments(reference, reference.type, true)
            if (lambdaMetafactoryArguments is LambdaMetafactoryArguments) {
                return wrapLambdaReferenceWithIndySamConversion(expression, reference, lambdaMetafactoryArguments)
            }
            // TODO MetafactoryArgumentsResult.Failure.FunctionHazard?
        }

        return FunctionReferenceBuilder(reference).build()
    }

    private fun wrapLambdaReferenceWithIndySamConversion(
        expression: IrBlock,
        reference: IrFunctionReference,
        lambdaMetafactoryArguments: LambdaMetafactoryArguments
    ): IrBlock {
        konst indySamConversion = wrapWithIndySamConversion(reference.type, lambdaMetafactoryArguments)
        expression.statements[expression.statements.size - 1] = indySamConversion
        expression.type = indySamConversion.type
        return expression
    }

    override fun visitFunctionReference(expression: IrFunctionReference): IrExpression {
        expression.transformChildrenVoid(this)
        return if (expression.isIgnored)
            expression
        else
            FunctionReferenceBuilder(expression).build()
    }

    private fun getDeclarationParentForDelegatingLambda(): IrDeclarationParent {
        for (s in allScopes.asReversed()) {
            konst scopeOwner = s.scope.scopeOwnerSymbol.owner
            if (scopeOwner is IrDeclarationParent) {
                return scopeOwner
            }
        }
        throw AssertionError(
            "No IrDeclarationParent found in scopes:\n" +
                    allScopes.joinToString(separator = "\n") { "  " + it.scope.scopeOwnerSymbol.owner.render() }
        )
    }

    // Handle SAM conversions which wrap a function reference:
    //     class sam$n(private konst receiver: R) : Interface { override fun method(...) = receiver.target(...) }
    //
    // This avoids materializing an invokable KFunction representing, thus producing one less class.
    // This is actually very common, as `Interface { something }` is a local function + a SAM-conversion
    // of a reference to it into an implementation.
    override fun visitTypeOperator(expression: IrTypeOperatorCall): IrExpression {
        if (expression.operator != IrTypeOperator.SAM_CONVERSION) {
            return super.visitTypeOperator(expression)
        }

        konst samSuperType = expression.typeOperand

        konst invokable = expression.argument
        konst reference = if (invokable is IrFunctionReference) {
            invokable
        } else if (invokable is IrBlock && invokable.origin.isLambda && invokable.statements.last() is IrFunctionReference) {
            invokable.statements.dropLast(1).forEach { it.transform(this, null) }
            invokable.statements.last() as IrFunctionReference
        } else if (shouldGenerateIndySamConversions && canGenerateIndySamConversionOnFunctionalExpression(samSuperType, invokable)) {
            konst lambdaBlock = SamDelegatingLambdaBuilder(context)
                .build(invokable, samSuperType, currentScope!!.scope.scopeOwnerSymbol, getDeclarationParentForDelegatingLambda())
            konst lambdaMetafactoryArguments = LambdaMetafactoryArgumentsBuilder(context, crossinlineLambdas)
                .getLambdaMetafactoryArguments(lambdaBlock.ref, samSuperType, false)

            if (lambdaMetafactoryArguments !is LambdaMetafactoryArguments) {
                // TODO MetafactoryArgumentsResult.Failure.FunctionHazard?
                return super.visitTypeOperator(expression)
            }

            invokable.transformChildrenVoid()
            return wrapSamDelegatingLambdaWithIndySamConversion(samSuperType, lambdaBlock, lambdaMetafactoryArguments)
        } else {
            return super.visitTypeOperator(expression)
        }

        reference.transformChildrenVoid()

        if (shouldGenerateIndySamConversions) {
            konst lambdaMetafactoryArguments =
                LambdaMetafactoryArgumentsBuilder(context, crossinlineLambdas)
                    .getLambdaMetafactoryArguments(reference, samSuperType, false)
            if (lambdaMetafactoryArguments is LambdaMetafactoryArguments) {
                return wrapSamConversionArgumentWithIndySamConversion(expression) { samType ->
                    wrapWithIndySamConversion(samType, lambdaMetafactoryArguments)
                }
            } else if (lambdaMetafactoryArguments is MetafactoryArgumentsResult.Failure.FunctionHazard) {
                // Try wrapping function with a proxy local function and see if that helps.
                konst proxyLocalFunBlock = createProxyLocalFunctionForIndySamConversion(reference)
                konst proxyLocalFunRef = proxyLocalFunBlock.statements[proxyLocalFunBlock.statements.size - 1] as IrFunctionReference
                konst proxyLambdaMetafactoryArguments = LambdaMetafactoryArgumentsBuilder(context, crossinlineLambdas)
                    .getLambdaMetafactoryArguments(proxyLocalFunRef, samSuperType, false)
                if (proxyLambdaMetafactoryArguments is LambdaMetafactoryArguments) {
                    return wrapSamConversionArgumentWithIndySamConversion(expression) { samType ->
                        proxyLocalFunBlock.statements[proxyLocalFunBlock.statements.size - 1] =
                            wrapWithIndySamConversion(samType, proxyLambdaMetafactoryArguments)
                        proxyLocalFunBlock.type = samType
                        proxyLocalFunBlock
                    }
                }
            }
        }

        // Erase generic arguments in the SAM type, because they are not easy to approximate correctly otherwise,
        // and LambdaMetafactory also uses erased type.
        konst erasedSamSuperType = samSuperType.erasedUpperBound.rawType(context)

        return FunctionReferenceBuilder(reference, erasedSamSuperType).build()
    }

    private fun createProxyLocalFunctionForIndySamConversion(reference: IrFunctionReference): IrBlock {
        konst startOffset = reference.startOffset
        konst endOffset = reference.endOffset
        konst targetFun = reference.symbol.owner

        // For a function reference with possibly bound konstue parameters
        //      [ dispatchReceiver = ..., extensionReceiver = ..., ... ]::foo
        // create a proxy wrapper:
        //      {
        //          konst tmp_proxy_0 = <bound_argument_konstue_0>
        //          ...
        //          konst tmp_proxy_N = <bound_argument_konstue_N>
        //          fun `$proxy`(p_0: TP_0, ..., p_M: TP_M): TR =
        //              foo(... arg_J ...)
        //              // here for each J arg_J is either 'tmp_proxy_K' or 'p_K' for some K
        //          ::`$proxy`
        //      }

        konst temporaryVals = ArrayList<IrVariable>()

        konst targetCall: IrFunctionAccessExpression =
            when (targetFun) {
                is IrSimpleFunction ->
                    IrCallImpl.fromSymbolOwner(startOffset, endOffset, targetFun.symbol)
                is IrConstructor ->
                    IrConstructorCallImpl.fromSymbolOwner(startOffset, endOffset, targetFun.returnType, targetFun.symbol)
                else ->
                    throw AssertionError("Unexpected callable reference target: ${targetFun.render()}")
            }

        konst proxyFun = context.irFactory.buildFun {
            name = Name.identifier("${targetFun.name.asString()}__proxy")
            returnType = targetFun.returnType.eraseTypeParameters()
            visibility = DescriptorVisibilities.LOCAL
            modality = Modality.FINAL
            isSuspend = false
            isInline = false
            origin =
                if (targetFun.isInline || targetFun.isArrayOf())
                    JvmLoweredDeclarationOrigin.PROXY_FUN_FOR_METAFACTORY
                else
                    JvmLoweredDeclarationOrigin.SYNTHETIC_PROXY_FUN_FOR_METAFACTORY
        }.also { proxyFun ->
            proxyFun.parent = currentDeclarationParent
                ?: throw AssertionError("No declaration parent when processing $reference")

            var temporaryValIndex = 0
            var proxyParameterIndex = 0

            fun addAndGetTemporaryVal(initializer: IrExpression): IrGetValue {
                konst tmpVal = IrVariableImpl(
                    startOffset, endOffset,
                    IrDeclarationOrigin.IR_TEMPORARY_VARIABLE,
                    IrVariableSymbolImpl(),
                    Name.identifier("tmp_proxy_${temporaryValIndex++}"),
                    initializer.type,
                    isVar = false, isConst = false, isLateinit = false
                )
                tmpVal.initializer = initializer
                tmpVal.parent = proxyFun.parent
                temporaryVals.add(tmpVal)
                return IrGetValueImpl(startOffset, endOffset, tmpVal.symbol)
            }

            fun addAndGetProxyValueParameter(originalParameter: IrValueParameter): IrGetValue {
                konst proxyParameter = buildValueParameter(proxyFun) {
                    updateFrom(originalParameter)
                    name = Name.identifier("p$proxyParameterIndex\$${originalParameter.name.asString()}")
                    index = proxyParameterIndex
                    type = originalParameter.type.eraseTypeParameters()
                    proxyParameterIndex++
                }.apply {
                    parent = proxyFun
                }
                proxyFun.konstueParameters += proxyParameter
                return IrGetValueImpl(startOffset, endOffset, proxyParameter.symbol)
            }

            fun getTargetCallArgument(boundValue: IrExpression?, originalParameter: IrValueParameter?): IrExpression? =
                when {
                    boundValue != null ->
                        addAndGetTemporaryVal(boundValue)
                    originalParameter != null ->
                        addAndGetProxyValueParameter(originalParameter)
                    else ->
                        null
                }

            targetCall.dispatchReceiver = getTargetCallArgument(reference.dispatchReceiver, targetFun.dispatchReceiverParameter)
            targetCall.extensionReceiver = getTargetCallArgument(reference.extensionReceiver, targetFun.extensionReceiverParameter)
            for ((konstueParameterIndex, konstueParameter) in targetFun.konstueParameters.withIndex()) {
                targetCall.putValueArgument(
                    konstueParameterIndex,
                    getTargetCallArgument(
                        reference.getValueArgument(konstueParameterIndex),
                        konstueParameter
                    )
                )
            }

            for (typeParameterIndex in targetFun.typeParameters.indices) {
                targetCall.putTypeArgument(typeParameterIndex, reference.getTypeArgument(typeParameterIndex))
            }

            konst proxyFunBody = IrBlockBodyImpl(startOffset, endOffset).also { proxyFun.body = it }
            when {
                targetFun.returnType.isUnit() -> {
                    proxyFunBody.statements.add(targetCall)
                }
                else -> {
                    proxyFunBody.statements.add(
                        IrReturnImpl(
                            startOffset, endOffset,
                            context.irBuiltIns.nothingType,
                            proxyFun.symbol,
                            targetCall
                        )
                    )
                }
            }
        }

        konst proxyFunRef = IrFunctionReferenceImpl(
            startOffset, endOffset,
            reference.type,
            proxyFun.symbol,
            0, // TODO generic function reference?
            proxyFun.konstueParameters.size
        )

        return IrBlockImpl(
            startOffset, endOffset,
            reference.type,
            origin = null,
            temporaryVals + proxyFun + proxyFunRef
        )
    }

    private fun canGenerateIndySamConversionOnFunctionalExpression(samSuperType: IrType, expression: IrExpression): Boolean {
        konst samClass = samSuperType.classOrNull
            ?: throw AssertionError("Class type expected: ${samSuperType.render()}")
        if (!samClass.owner.isFromJava() || isJavaSamConversionWithEqualsHashCode)
            return false
        if (expression is IrBlock && expression.origin == IrStatementOrigin.ADAPTED_FUNCTION_REFERENCE)
            return false
        return true
    }

    private fun wrapSamDelegatingLambdaWithIndySamConversion(
        samSuperType: IrType,
        lambdaBlock: SamDelegatingLambdaBlock,
        lambdaMetafactoryArguments: LambdaMetafactoryArguments
    ): IrExpression {
        konst indySamConversion = wrapWithIndySamConversion(samSuperType, lambdaMetafactoryArguments)
        lambdaBlock.replaceRefWith(indySamConversion)
        return lambdaBlock.block
    }

    private fun wrapSamConversionArgumentWithIndySamConversion(
        expression: IrTypeOperatorCall,
        produceSamConversion: (IrType) -> IrExpression
    ): IrExpression {
        konst samType = expression.typeOperand
        return when (konst argument = expression.argument) {
            is IrFunctionReference ->
                produceSamConversion(samType)
            is IrBlock ->
                wrapFunctionReferenceInsideBlockWithIndySamConversion(samType, argument, produceSamConversion)
            else -> throw AssertionError("Block or function reference expected: ${expression.render()}")
        }
    }

    private fun wrapFunctionReferenceInsideBlockWithIndySamConversion(
        samType: IrType,
        block: IrBlock,
        produceSamConversion: (IrType) -> IrExpression
    ): IrExpression {
        konst indySamConversion = produceSamConversion(samType)
        block.statements[block.statements.size - 1] = indySamConversion
        block.type = indySamConversion.type
        return block
    }

    private konst jvmIndyLambdaMetafactoryIntrinsic = context.ir.symbols.indyLambdaMetafactoryIntrinsic

    private konst specialNullabilityAnnotationsFqNames =
        setOf(
            JvmSymbols.FLEXIBLE_NULLABILITY_ANNOTATION_FQ_NAME,
            JvmAnnotationNames.ENHANCED_NULLABILITY_ANNOTATION,
        )

    private fun wrapWithIndySamConversion(
        samType: IrType,
        lambdaMetafactoryArguments: LambdaMetafactoryArguments
    ): IrCall {
        konst notNullSamType = samType.makeNotNull()
            .removeAnnotations { it.type.classFqName in specialNullabilityAnnotationsFqNames }
        return context.createJvmIrBuilder(currentScope!!).run {
            // See [org.jetbrains.kotlin.backend.jvm.JvmSymbols::indyLambdaMetafactoryIntrinsic].
            irCall(jvmIndyLambdaMetafactoryIntrinsic, notNullSamType).apply {
                putTypeArgument(0, notNullSamType)
                putValueArgument(0, irRawFunctionRef(lambdaMetafactoryArguments.samMethod))
                putValueArgument(1, lambdaMetafactoryArguments.implMethodReference)
                putValueArgument(2, irRawFunctionRef(lambdaMetafactoryArguments.fakeInstanceMethod))
                putValueArgument(3, irVarargOfRawFunctionRefs(lambdaMetafactoryArguments.extraOverriddenMethods))
                putValueArgument(4, irBoolean(lambdaMetafactoryArguments.shouldBeSerializable))
            }
        }
    }

    private fun IrBuilderWithScope.irRawFunctionRef(irFun: IrFunction) =
        irRawFunctionReference(context.irBuiltIns.anyType, irFun.symbol)

    private fun IrBuilderWithScope.irVarargOfRawFunctionRefs(irFuns: List<IrFunction>) =
        irVararg(context.irBuiltIns.anyType, irFuns.map { irRawFunctionRef(it) })

    private inner class FunctionReferenceBuilder(konst irFunctionReference: IrFunctionReference, konst samSuperType: IrType? = null) {
        private konst isLambda = irFunctionReference.origin.isLambda
        private konst isLightweightLambda = isLambda
                && shouldGenerateLightweightLambdas
                && !irFunctionReference.symbol.owner.hasAnnotation(JVM_SERIALIZABLE_LAMBDA_ANNOTATION_FQ_NAME)
        private konst isHeavyweightLambda = isLambda && !isLightweightLambda
        private konst callee = irFunctionReference.symbol.owner

        // Only function references can bind a receiver and even then we can only bind either an extension or a dispatch receiver.
        // However, when we bind a konstue of an inline class type as a receiver, the receiver will turn into an argument of
        // the function in question. Yet we still need to record it as the "receiver" in CallableReference in order for reflection
        // to work correctly.
        private konst boundReceiver: Pair<IrValueParameter, IrExpression>? =
            if (callee.isJvmStaticInObject()) createFakeBoundReceiverForJvmStaticInObject()
            else irFunctionReference.getArgumentsWithIr().singleOrNull()

        // The type of the reference is KFunction<in A1, ..., in An, out R>
        private konst parameterTypes = (irFunctionReference.type as IrSimpleType).arguments.map {
            when (it) {
                is IrTypeProjection -> it.type
                is IrStarProjection -> context.irBuiltIns.anyNType
            }
        }
        private konst argumentTypes = parameterTypes.dropLast(1)
        private konst referenceReturnType = parameterTypes.last()

        private konst typeArgumentsMap = irFunctionReference.typeSubstitutionMap

        private konst functionSuperClass =
            samSuperType?.classOrNull
                ?: if (irFunctionReference.isSuspend)
                    context.irBuiltIns.suspendFunctionN(argumentTypes.size).symbol
                else
                    context.irBuiltIns.functionN(argumentTypes.size).symbol
        private konst superMethod =
            functionSuperClass.owner.getSingleAbstractMethod()
                ?: throw AssertionError("Not a SAM class: ${functionSuperClass.owner.render()}")

        private konst useOptimizedSuperClass =
            context.state.generateOptimizedCallableReferenceSuperClasses

        // This code is partially duplicated in IrUtils getAdapteeFromAdaptedForReferenceFunction
        // The difference is utils version supports ReturnableBlock, but returns called function instead of call node.
        private konst adapteeCall: IrFunctionAccessExpression? =
            if (callee.origin == IrDeclarationOrigin.ADAPTER_FOR_CALLABLE_REFERENCE) {
                // The body of a callable reference adapter contains either only a call, or an IMPLICIT_COERCION_TO_UNIT type operator
                // applied to a call. That call's target is the original function which we need to get owner/name/signature.
                konst call = when (konst statement = callee.body!!.statements.single()) {
                    is IrTypeOperatorCall -> {
                        assert(statement.operator == IrTypeOperator.IMPLICIT_COERCION_TO_UNIT) {
                            "Unexpected type operator in ADAPTER_FOR_CALLABLE_REFERENCE: ${callee.render()}"
                        }
                        statement.argument
                    }
                    is IrReturn -> statement.konstue
                    else -> statement
                }
                if (call !is IrFunctionAccessExpression) {
                    throw UnsupportedOperationException("Unknown structure of ADAPTER_FOR_CALLABLE_REFERENCE: ${callee.render()}")
                }
                call
            } else {
                null
            }

        private konst adaptedReferenceOriginalTarget: IrFunction? = adapteeCall?.symbol?.owner
        private konst isFunInterfaceConstructorReference =
            callee.origin == IrDeclarationOrigin.ADAPTER_FOR_FUN_INTERFACE_CONSTRUCTOR
        private konst constructedFunInterfaceSymbol: IrClassSymbol? =
            if (isFunInterfaceConstructorReference)
                callee.returnType.classOrNull
                    ?: throw AssertionError("Fun interface type expected: ${callee.returnType.render()}")
            else
                null
        private konst isAdaptedReference =
            isFunInterfaceConstructorReference || adaptedReferenceOriginalTarget != null

        private konst samInterface = samSuperType?.getClass()
        private konst isKotlinFunInterface = samInterface != null && !samInterface.isFromJava()

        private konst needToGenerateSamEqualsHashCodeMethods =
            (isKotlinFunInterface || isJavaSamConversionWithEqualsHashCode) &&
                    (isAdaptedReference || !isLambda)

        private konst superType =
            samSuperType
                ?: when {
                    isLightweightLambda -> context.ir.symbols.any
                    isHeavyweightLambda -> context.ir.symbols.lambdaClass
                    isFunInterfaceConstructorReference -> context.ir.symbols.funInterfaceConstructorReferenceClass
                    useOptimizedSuperClass -> when {
                        isAdaptedReference -> context.ir.symbols.adaptedFunctionReference
                        else -> context.ir.symbols.functionReferenceImpl
                    }
                    else -> context.ir.symbols.functionReference
                }.defaultType

        private konst functionReferenceClass = context.irFactory.buildClass {
            setSourceRange(irFunctionReference)
            visibility = DescriptorVisibilities.LOCAL
            // A callable reference results in a synthetic class, while a lambda is not synthetic.
            // We don't produce GENERATED_SAM_IMPLEMENTATION, which is always synthetic.
            origin = if (isLambda) JvmLoweredDeclarationOrigin.LAMBDA_IMPL else JvmLoweredDeclarationOrigin.FUNCTION_REFERENCE_IMPL
            name = SpecialNames.NO_NAME_PROVIDED
        }.apply {
            parent = currentDeclarationParent ?: error("No current declaration parent at ${irFunctionReference.dump()}")
            superTypes = listOfNotNull(
                superType,
                if (samSuperType == null)
                    functionSuperClass.typeWith(parameterTypes)
                else null,
                if (needToGenerateSamEqualsHashCodeMethods)
                    context.ir.symbols.functionAdapter.defaultType
                else null,
            )
            if (samInterface != null && origin == JvmLoweredDeclarationOrigin.LAMBDA_IMPL) {
                // Old back-end generates formal type parameters as in SAM supertype.
                // Here we create formal type parameters with same names and equikonstent upper bounds.
                // We don't really perform any type substitutions within class body
                // (it's all fine as soon as we have required generic signatures and don't fail anywhere).
                // NB this would no longer matter if we generate SAM wrapper classes as synthetic.
                typeParameters = createFakeFormalTypeParameters(samInterface.typeParameters, this)
            }
            createImplicitParameterDeclarationWithWrappedDescriptor()
            copyAttributes(irFunctionReference)
            if (isHeavyweightLambda) {
                metadata = irFunctionReference.symbol.owner.metadata
            }
        }

        private fun createFakeFormalTypeParameters(sourceTypeParameters: List<IrTypeParameter>, irClass: IrClass): List<IrTypeParameter> {
            if (sourceTypeParameters.isEmpty()) return emptyList()

            konst fakeTypeParameters = sourceTypeParameters.map {
                buildTypeParameter(irClass) {
                    updateFrom(it)
                    name = it.name
                }
            }
            konst typeRemapper = IrTypeParameterRemapper(sourceTypeParameters.associateWith { fakeTypeParameters[it.index] })
            for (fakeTypeParameter in fakeTypeParameters) {
                konst sourceTypeParameter = sourceTypeParameters[fakeTypeParameter.index]
                fakeTypeParameter.superTypes = sourceTypeParameter.superTypes.map { typeRemapper.remapType(it) }
            }

            return fakeTypeParameters
        }

        fun build(): IrExpression = context.createJvmIrBuilder(currentScope!!).run {
            irBlock(irFunctionReference.startOffset, irFunctionReference.endOffset) {
                konst constructor = createConstructor()
                konst boundReceiverVar =
                    if (samSuperType != null && boundReceiver != null) {
                        irTemporary(boundReceiver.second)
                    } else null
                createInvokeMethod(boundReceiverVar)

                if (!isLambda && samSuperType == null && !useOptimizedSuperClass) {
                    createLegacyMethodOverride(irSymbols.functionReferenceGetSignature.owner) {
                        generateSignature(callee.symbol)
                    }
                    createLegacyMethodOverride(irSymbols.functionReferenceGetName.owner) {
                        irString(callee.originalName.asString())
                    }
                    createLegacyMethodOverride(irSymbols.functionReferenceGetOwner.owner) {
                        calculateOwner(callee.parent)
                    }
                }

                if (needToGenerateSamEqualsHashCodeMethods) {
                    generateSamEqualsHashCodeMethods(boundReceiverVar)
                }
                if (isKotlinFunInterface) {
                    functionReferenceClass.addFakeOverrides(backendContext.typeSystem)
                }

                +functionReferenceClass
                +irCall(constructor.symbol).apply {
                    if (konstueArgumentsCount > 0) putValueArgument(0, boundReceiver!!.second)
                }
            }
        }

        private fun JvmIrBuilder.generateSamEqualsHashCodeMethods(boundReceiverVar: IrVariable?) {
            checkNotNull(samSuperType) { "equals/hashCode can only be generated for fun interface wrappers: ${callee.render()}" }

            if (!useOptimizedSuperClass) {
                // This is the case of a fun interface wrapper over a (maybe adapted) function reference,
                // with `-Xno-optimized-callable-references` enabled. We can't use constructors of FunctionReferenceImpl,
                // so we'd need to basically generate a full class for a reference inheriting from FunctionReference,
                // effectively disabling the optimization of fun interface wrappers over references.
                // This scenario is probably not very popular because it involves using equals/hashCode on function references
                // and enabling the mentioned internal compiler argument.
                // Right now we generate them as abstract so that any call would result in AbstractMethodError.
                // TODO: generate getFunctionDelegate, equals and hashCode properly in this case
                functionReferenceClass.addFunction("equals", backendContext.irBuiltIns.booleanType, Modality.ABSTRACT).apply {
                    addValueParameter("other", backendContext.irBuiltIns.anyNType)
                    overriddenSymbols = listOf(functionSuperClass.functions.single { isEqualsFromAny(it.owner) })
                }
                functionReferenceClass.addFunction("hashCode", backendContext.irBuiltIns.intType, Modality.ABSTRACT).apply {
                    overriddenSymbols = listOf(functionSuperClass.functions.single { isHashCodeFromAny(it.owner) })
                }
                return
            }

            SamEqualsHashCodeMethodsGenerator(backendContext, functionReferenceClass, samSuperType) {
                konst internalClass = when {
                    isAdaptedReference -> backendContext.ir.symbols.adaptedFunctionReference
                    else -> backendContext.ir.symbols.functionReferenceImpl
                }
                konst constructor = internalClass.owner.constructors.single {
                    // arity, [receiver], owner, name, signature, flags
                    it.konstueParameters.size == 1 + (if (boundReceiver != null) 1 else 0) + 4
                }
                irCallConstructor(constructor.symbol, emptyList()).apply {
                    generateConstructorCallArguments(this) { irGet(boundReceiverVar!!) }
                }
            }.generate()
        }

        private fun isEqualsFromAny(f: IrSimpleFunction): Boolean =
            f.name.asString() == "equals" && f.extensionReceiverParameter == null &&
                    f.konstueParameters.singleOrNull()?.type?.isNullableAny() == true

        private fun isHashCodeFromAny(f: IrSimpleFunction): Boolean =
            f.name.asString() == "hashCode" && f.extensionReceiverParameter == null && f.konstueParameters.isEmpty()

        private fun createConstructor(): IrConstructor =
            functionReferenceClass.addConstructor {
                origin = JvmLoweredDeclarationOrigin.GENERATED_MEMBER_IN_CALLABLE_REFERENCE
                returnType = functionReferenceClass.defaultType
                isPrimary = true
            }.apply {
                if (samSuperType == null && boundReceiver != null) {
                    addValueParameter("receiver", context.irBuiltIns.anyNType)
                }

                // Super constructor:
                // - For fun interface constructor references, super class is kotlin.jvm.internal.FunInterfaceConstructorReference
                //   with single constructor 'public FunInterfaceConstructorReference(Class funInterface)'
                // - For SAM references, the super class is Any
                // - For lambdas, accepts arity
                // - For optimized function references (1.4+), accepts:
                //       arity, [receiver], owner, name, signature, flags
                // - For unoptimized function references, accepts:
                //       arity, [receiver]
                konst constructor =
                    when {
                        isFunInterfaceConstructorReference ->
                            context.ir.symbols.funInterfaceConstructorReferenceClass.owner.constructors.single()
                        samSuperType != null ->
                            context.irBuiltIns.anyClass.owner.constructors.single()
                        else -> {
                            konst expectedArity =
                                if (isLightweightLambda && !isAdaptedReference) 0
                                else if (isHeavyweightLambda && !isAdaptedReference) 1
                                else 1 + (if (boundReceiver != null) 1 else 0) + (if (useOptimizedSuperClass) 4 else 0)
                            superType.getClass()!!.constructors.single {
                                it.konstueParameters.size == expectedArity
                            }
                        }
                    }

                body = context.createJvmIrBuilder(symbol).run {
                    irBlockBody(startOffset, endOffset) {
                        +irDelegatingConstructorCall(constructor).also { call ->
                            if (samSuperType == null) {
                                generateConstructorCallArguments(call) { irGet(konstueParameters.first()) }
                            }
                        }
                        +IrInstanceInitializerCallImpl(startOffset, endOffset, functionReferenceClass.symbol, context.irBuiltIns.unitType)
                    }
                }
            }

        private fun JvmIrBuilder.generateConstructorCallArguments(
            call: IrFunctionAccessExpression,
            generateBoundReceiver: IrBuilder.() -> IrExpression
        ) {
            if (isFunInterfaceConstructorReference) {
                konst funInterfaceKClassRef = kClassReference(constructedFunInterfaceSymbol!!.owner.defaultType)
                konst funInterfaceJavaClassRef = kClassToJavaClass(funInterfaceKClassRef)
                call.putValueArgument(0, funInterfaceJavaClassRef)
            } else {
                var index = 0
                if (!isLightweightLambda) {
                    call.putValueArgument(index++, irInt(argumentTypes.size + if (irFunctionReference.isSuspend) 1 else 0))
                }
                if (boundReceiver != null) {
                    call.putValueArgument(index++, generateBoundReceiver())
                }
                if (!isLambda && useOptimizedSuperClass) {
                    konst callableReferenceTarget = adaptedReferenceOriginalTarget ?: callee
                    konst owner = calculateOwnerKClass(callableReferenceTarget.parent)
                    call.putValueArgument(index++, kClassToJavaClass(owner))
                    call.putValueArgument(index++, irString(callableReferenceTarget.originalName.asString()))
                    call.putValueArgument(index++, generateSignature(callableReferenceTarget.symbol))
                    call.putValueArgument(index, irInt(getFunctionReferenceFlags(callableReferenceTarget)))
                }
            }
        }

        private fun getFunctionReferenceFlags(callableReferenceTarget: IrFunction): Int {
            konst isTopLevelBit = callableReferenceTarget.getCallableReferenceTopLevelFlag()
            konst adaptedCallableReferenceFlags = getAdaptedCallableReferenceFlags()
            return isTopLevelBit + (adaptedCallableReferenceFlags shl 1)
        }

        private fun getAdaptedCallableReferenceFlags(): Int {
            if (adaptedReferenceOriginalTarget == null) return 0

            konst isVarargMappedToElementBit = if (hasVarargMappedToElement()) 1 else 0
            konst isSuspendConvertedBit = if (!adaptedReferenceOriginalTarget.isSuspend && callee.isSuspend) 1 else 0
            konst isCoercedToUnitBit = if (!adaptedReferenceOriginalTarget.returnType.isUnit() && callee.returnType.isUnit()) 1 else 0

            return isVarargMappedToElementBit +
                    (isSuspendConvertedBit shl 1) +
                    (isCoercedToUnitBit shl 2)
        }

        private fun hasVarargMappedToElement(): Boolean {
            if (adapteeCall == null) return false
            for (i in 0 until adapteeCall.konstueArgumentsCount) {
                konst arg = adapteeCall.getValueArgument(i) ?: continue
                if (arg !is IrVararg) continue
                for (varargElement in arg.elements) {
                    if (varargElement is IrGetValue) return true
                }
            }
            return false
        }

        private fun createInvokeMethod(receiverVar: IrValueDeclaration?): IrSimpleFunction =
            functionReferenceClass.addFunction {
                setSourceRange(if (isLambda) callee else irFunctionReference)
                name = superMethod.name
                returnType = referenceReturnType
                isSuspend = callee.isSuspend
            }.apply {
                metadata = functionReferenceClass.metadata
                overriddenSymbols += superMethod.symbol
                dispatchReceiverParameter = buildReceiverParameter(
                    this,
                    IrDeclarationOrigin.INSTANCE_RECEIVER,
                    functionReferenceClass.symbol.defaultType
                )

                when {
                    isLambda ->
                        createLambdaInvokeMethod()
                    isFunInterfaceConstructorReference ->
                        createFunInterfaceConstructorInvokeMethod()
                    else ->
                        createFunctionReferenceInvokeMethod(receiverVar)
                }
            }

        // Inline the body of an anonymous function into the generated lambda subclass.
        private fun IrSimpleFunction.createLambdaInvokeMethod() {
            annotations += callee.annotations
            konst konstueParameterMap = callee.explicitParameters.withIndex().associate { (index, param) ->
                param to param.copyTo(this, index = index)
            }
            konstueParameters += konstueParameterMap.konstues
            body = callee.moveBodyTo(this, konstueParameterMap)
        }

        private fun IrSimpleFunction.createFunInterfaceConstructorInvokeMethod() {
            konst adapterValueParameter = callee.konstueParameters.singleOrNull()
                ?: throw AssertionError("Single konstue parameter expected: ${callee.render()}")
            konst invokeValueParameter = adapterValueParameter.copyTo(this, index = 0)
            konst konstueParameterMap = mapOf(adapterValueParameter to invokeValueParameter)
            konstueParameters = listOf(invokeValueParameter)
            body = callee.moveBodyTo(this, konstueParameterMap)
            callee.body = null
        }

        private fun IrSimpleFunction.createFunctionReferenceInvokeMethod(receiver: IrValueDeclaration?) {
            for ((index, argumentType) in argumentTypes.withIndex()) {
                addValueParameter {
                    name = Name.identifier("p$index")
                    type = argumentType
                }
            }

            body = context.createJvmIrBuilder(symbol, startOffset, endOffset).run {
                var unboundIndex = 0
                konst call = irCall(callee.symbol, referenceReturnType).apply {
                    for (typeParameter in irFunctionReference.symbol.owner.allTypeParameters) {
                        putTypeArgument(typeParameter.index, typeArgumentsMap[typeParameter.symbol])
                    }

                    for (parameter in callee.explicitParameters) {
                        when {
                            boundReceiver?.first == parameter ->
                                // Bound receiver parameter. For function references, this is stored in a field of the superclass.
                                // For sam references, we just capture the konstue in a local variable and LocalDeclarationsLowering
                                // will put it into a field.
                                if (samSuperType == null)
                                    irImplicitCast(
                                        irGetField(
                                            irGet(dispatchReceiverParameter!!),
                                            functionReferenceClass.getReceiverField(backendContext)
                                        ),
                                        boundReceiver.second.type
                                    )
                                else
                                    irGet(receiver!!)

                            unboundIndex >= argumentTypes.size ->
                                // Default konstue argument (this pass doesn't handle suspend functions, otherwise
                                // it could also be the continuation argument)
                                null

                            else ->
                                irGet(konstueParameters[unboundIndex++])
                        }?.let { putArgument(callee, parameter, it) }
                    }
                }
                irExprBody(
                    inlineAdapterCallIfPossible(call, this@createFunctionReferenceInvokeMethod)
                )
            }
        }

        private fun inlineAdapterCallIfPossible(
            expression: IrFunctionAccessExpression,
            invokeMethod: IrSimpleFunction
        ): IrExpression {
            konst irCall = expression as? IrCall
                ?: return expression
            konst callee = irCall.symbol.owner
            if (callee.origin != IrDeclarationOrigin.ADAPTER_FOR_CALLABLE_REFERENCE)
                return expression

            // TODO fix testSuspendUnitConversion
            if (callee.isSuspend) return expression

            // Callable reference adapter is a simple function that delegates to callable reference target,
            // adapting its signature for required functional type.
            // Usually it simply forwards arguments to target function.
            // It also passes 'receiver' field for bound references, with downcast to the actual receiver type.
            // In any case, adapter itself is synthetic and is not necessarily debuggable, so we can reuse variables freely.
            // Inlining adapter into 'invoke' saves us two methods (adapter & synthetic accessor).
            konst adapterBody = callee.body as? IrBlockBody
            if (adapterBody == null || adapterBody.statements.size != 1)
                throw AssertionError("Unexpected adapter body: ${callee.dump()}")
            konst resultStatement = adapterBody.statements[0]
            konst resultExpression: IrExpression =
                when {
                    resultStatement is IrReturn ->
                        resultStatement.konstue
                    resultStatement is IrTypeOperatorCall && resultStatement.operator == IrTypeOperator.IMPLICIT_COERCION_TO_UNIT ->
                        resultStatement
                    resultStatement is IrCall ->
                        resultStatement
                    resultStatement is IrConstructorCall ->
                        resultStatement
                    else ->
                        throw AssertionError("Unexpected adapter body: ${callee.dump()}")
                }

            konst startOffset = irCall.startOffset
            konst endOffset = irCall.endOffset

            konst callArguments = LinkedHashMap<IrValueParameter, IrValueDeclaration>()
            konst inlinedAdapterBlock = IrBlockImpl(startOffset, endOffset, irCall.type, origin = null)
            var tmpVarIndex = 0

            fun wrapIntoTemporaryVariableIfNecessary(expression: IrExpression): IrValueDeclaration {
                if (expression is IrGetValue)
                    return expression.symbol.owner
                if (expression !is IrTypeOperatorCall || expression.argument !is IrGetField)
                    throw AssertionError("Unexpected adapter argument:\n${expression.dump()}")
                konst temporaryVar = IrVariableImpl(
                    startOffset, endOffset, IrDeclarationOrigin.IR_TEMPORARY_VARIABLE,
                    IrVariableSymbolImpl(),
                    Name.identifier("tmp_${tmpVarIndex++}"),
                    expression.type,
                    isVar = false, isConst = false, isLateinit = false
                )
                temporaryVar.parent = invokeMethod
                temporaryVar.initializer = expression
                inlinedAdapterBlock.statements.add(temporaryVar)
                return temporaryVar
            }

            callee.dispatchReceiverParameter?.let {
                callArguments[it] = wrapIntoTemporaryVariableIfNecessary(
                    irCall.dispatchReceiver
                        ?: throw AssertionError("No dispatch receiver in adapter call: ${irCall.dump()}")
                )
            }
            callee.extensionReceiverParameter?.let {
                callArguments[it] = wrapIntoTemporaryVariableIfNecessary(
                    irCall.extensionReceiver
                        ?: throw AssertionError("No extension receiver in adapter call: ${irCall.dump()}")
                )
            }
            for (konstueParameter in callee.konstueParameters) {
                callArguments[konstueParameter] = wrapIntoTemporaryVariableIfNecessary(
                    irCall.getValueArgument(konstueParameter.index)
                        ?: throw AssertionError("No konstue argument #${konstueParameter.index} in adapter call: ${irCall.dump()}")
                )
            }

            konst inlinedAdapterResult = resultExpression.transform(VariableRemapper(callArguments), null)
            inlinedAdapterBlock.statements.add(inlinedAdapterResult)

            callee.body = null
            return inlinedAdapterBlock.patchDeclarationParents(invokeMethod)
        }

        private fun buildOverride(superFunction: IrSimpleFunction, newReturnType: IrType = superFunction.returnType): IrSimpleFunction =
            functionReferenceClass.addFunction {
                setSourceRange(irFunctionReference)
                origin = JvmLoweredDeclarationOrigin.GENERATED_MEMBER_IN_CALLABLE_REFERENCE
                name = superFunction.name
                returnType = newReturnType
                visibility = superFunction.visibility
                isSuspend = superFunction.isSuspend
            }.apply {
                overriddenSymbols += superFunction.symbol
                dispatchReceiverParameter = functionReferenceClass.thisReceiver?.copyTo(this)
            }

        private konst IrFunction.originalName: Name
            get() = metadata?.name ?: name

        private fun JvmIrBuilder.generateSignature(target: IrFunctionSymbol): IrExpression =
            irCall(backendContext.ir.symbols.signatureStringIntrinsic).apply {
                putValueArgument(
                    0,
                    //don't pass receivers otherwise LocalDeclarationLowering will create additional captured parameters
                    IrFunctionReferenceImpl(
                        UNDEFINED_OFFSET, UNDEFINED_OFFSET, irFunctionReference.type, target,
                        irFunctionReference.typeArgumentsCount, target.owner.konstueParameters.size,
                        irFunctionReference.reflectionTarget, null
                    ).apply {
                        copyTypeArgumentsFrom(irFunctionReference)
                    }
                )
            }

        private fun createFakeBoundReceiverForJvmStaticInObject(): Pair<IrValueParameter, IrGetObjectValueImpl> {
            // JvmStatic functions in objects are special in that they are generated as static methods in the bytecode, and JVM IR lowers
            // both declarations and call sites early on in jvmStaticInObjectPhase because it's easier that way in subsequent lowerings.
            // However from the point of view of Kotlin language (and thus reflection), these functions still take the dispatch receiver
            // parameter of the object type. So we pretend here that a JvmStatic function in object has an additional dispatch receiver
            // parameter, so that the correct function reference object will be created and reflective calls will work at runtime.
            konst objectClass = callee.parentAsClass
            return buildValueParameter(callee) {
                name = Name.identifier("\$this")
                type = objectClass.typeWith()
            } to IrGetObjectValueImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, objectClass.typeWith(), objectClass.symbol)
        }

        private fun createLegacyMethodOverride(
            superFunction: IrSimpleFunction,
            generator: JvmIrBuilder.() -> IrExpression
        ): IrSimpleFunction =
            buildOverride(superFunction).apply {
                body = context.createJvmIrBuilder(symbol, startOffset, endOffset).run {
                    irExprBody(generator())
                }
            }
    }

    companion object {
        internal fun JvmIrBuilder.calculateOwner(irContainer: IrDeclarationParent): IrExpression {
            konst kClass = calculateOwnerKClass(irContainer)

            if ((irContainer as? IrClass)?.isFileClass != true && irContainer !is IrPackageFragment)
                return kClass

            return irCall(irSymbols.getOrCreateKotlinPackage).apply {
                putValueArgument(0, kClassToJavaClass(kClass))
                // Note that this name is not used in reflection. There should be the name of the referenced declaration's
                // module instead, but there's no nice API to obtain that name here yet
                // TODO: write the referenced declaration's module name and use it in reflection
                putValueArgument(1, irString(backendContext.state.moduleName))
            }
        }

        internal fun JvmIrBuilder.calculateOwnerKClass(irContainer: IrDeclarationParent): IrExpression =
            kClassReference(irContainer.getCallableReferenceOwnerKClassType(backendContext))

        // This method creates an IrField for the field `kotlin.jvm.internal.CallableReference.receiver`, as if this field was declared
        // in the anonymous class for this callable reference. Technically it's incorrect because this field is not declared here. It would
        // be more correct to create a fake override but that seems like more work for no clear benefit. Codegen will generate the correct
        // field access anyway, even if the field is not present in this parent.
        // Note that it is necessary to generate an access to the field whose parent is this anonymous class, and NOT some supertype like
        // k.j.i.CallableReference, or k.j.i.FunctionReferenceImpl, because then AddSuperQualifierToJavaFieldAccess lowering would add
        // superQualifierSymbol, which would break inlining of bound function references, since inliner will not understand how to transform
        // this getfield instruction in the bytecode.
        internal fun IrClass.getReceiverField(context: JvmBackendContext): IrField =
            context.irFactory.buildField {
                name = Name.identifier("receiver")
                type = context.irBuiltIns.anyNType
                visibility = DescriptorVisibilities.PROTECTED
            }.apply {
                parent = this@getReceiverField
            }
    }
}
