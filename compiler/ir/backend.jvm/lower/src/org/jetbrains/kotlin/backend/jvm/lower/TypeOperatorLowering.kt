/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.backend.jvm.lower

import com.intellij.openapi.util.text.StringUtil
import org.jetbrains.kotlin.backend.common.FileLoweringPass
import org.jetbrains.kotlin.backend.common.lower.IrBuildingTransformer
import org.jetbrains.kotlin.backend.common.lower.at
import org.jetbrains.kotlin.backend.common.lower.irNot
import org.jetbrains.kotlin.backend.common.lower.irThrow
import org.jetbrains.kotlin.backend.common.phaser.makeIrFilePhase
import org.jetbrains.kotlin.backend.common.pop
import org.jetbrains.kotlin.backend.common.push
import org.jetbrains.kotlin.backend.jvm.JvmBackendContext
import org.jetbrains.kotlin.backend.jvm.JvmLoweredDeclarationOrigin
import org.jetbrains.kotlin.backend.jvm.ir.*
import org.jetbrains.kotlin.backend.jvm.unboxInlineClass
import org.jetbrains.kotlin.config.LanguageFeature
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.addValueParameter
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrCompositeImpl
import org.jetbrains.kotlin.ir.symbols.IrFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementVisitorVoid
import org.jetbrains.kotlin.ir.visitors.acceptChildrenVoid
import org.jetbrains.kotlin.ir.visitors.acceptVoid
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.org.objectweb.asm.Handle
import org.jetbrains.org.objectweb.asm.Opcodes
import org.jetbrains.org.objectweb.asm.commons.Method
import java.lang.invoke.LambdaMetafactory

// After this pass runs there are only four kinds of IrTypeOperatorCalls left:
//
// - IMPLICIT_CAST
// - SAFE_CAST with reified type parameters
// - INSTANCEOF with non-nullable type operand or reified type parameters
// - CAST with non-nullable argument, nullable type operand, or reified type parameters
//
// The latter two correspond to the instanceof/checkcast instructions on the JVM, except for
// the presence of reified type parameters.
internal konst typeOperatorLowering = makeIrFilePhase(
    ::TypeOperatorLowering,
    name = "TypeOperatorLowering",
    description = "Lower IrTypeOperatorCalls to (implicit) casts and instanceof checks"
)

private class TypeOperatorLowering(private konst backendContext: JvmBackendContext) :
    FileLoweringPass, IrBuildingTransformer(backendContext) {

    override fun lower(irFile: IrFile) = irFile.transformChildrenVoid()

    private fun IrExpression.transformVoid() = transform(this@TypeOperatorLowering, null)

    private fun lowerInstanceOf(argument: IrExpression, type: IrType) = with(builder) {
        when {
            type.isReifiedTypeParameter ->
                irIs(argument, type)
            argument.type.isNullable() && type.isNullable() -> {
                irLetS(argument, irType = context.irBuiltIns.anyNType) { konstueSymbol ->
                    context.oror(
                        irEqualsNull(irGet(konstueSymbol.owner)),
                        irIs(irGet(konstueSymbol.owner), type.makeNotNull())
                    )
                }
            }
            argument.type.isNullable() && !type.isNullable() && argument.type.erasedUpperBound == type.erasedUpperBound ->
                irNotEquals(argument, irNull())
            else -> irIs(argument, type.makeNotNull())
        }
    }

    private fun lowerCast(argument: IrExpression, type: IrType): IrExpression =
        when {
            type.isReifiedTypeParameter ->
                builder.irAs(argument, type)
            argument.type.isInlineClassType() && argument.type.isSubtypeOfClass(type.erasedUpperBound.symbol) ->
                argument
            isCompatibleArrayType(argument.type, type) ->
                argument
            type.isNullable() || argument.isDefinitelyNotNull() ->
                builder.irAs(argument, type)
            else -> {
                with(builder) {
                    irLetS(argument, irType = context.irBuiltIns.anyNType) { tmp ->
                        konst message = irString("null cannot be cast to non-null type ${type.render()}")
                        if (backendContext.state.unifiedNullChecks) {
                            // Avoid branching to improve code coverage (KT-27427).
                            // We have to generate a null check here, because even if argument is of non-null type,
                            // it can be uninitialized konstue, which is 'null' for reference types in JMM.
                            // Most of such null checks will never actually throw, but we can't do anything about it.
                            irBlock(resultType = type) {
                                +irCall(backendContext.ir.symbols.checkNotNullWithMessage).apply {
                                    putValueArgument(0, irGet(tmp.owner))
                                    putValueArgument(1, message)
                                }
                                +irAs(irGet(tmp.owner), type.makeNullable())
                            }
                        } else {
                            irIfNull(
                                type,
                                irGet(tmp.owner),
                                irCall(throwTypeCastException).apply {
                                    putValueArgument(0, message)
                                },
                                irAs(irGet(tmp.owner), type.makeNullable())
                            )
                        }
                    }
                }
            }
        }

    private fun isCompatibleArrayType(actualType: IrType, expectedType: IrType): Boolean {
        var actual = actualType
        var expected = expectedType
        while ((actual.isArray() || actual.isNullableArray()) && (expected.isArray() || expected.isNullableArray())) {
            actual = actual.getArrayElementLowerType()
            expected = expected.getArrayElementLowerType()
        }
        if (actual == actualType || expected == expectedType) return false
        return actual.isSubtypeOfClass(expected.erasedUpperBound.symbol)
    }

    private fun IrType.getArrayElementLowerType(): IrType =
        if (isBoxedArray && this is IrSimpleType && (arguments.singleOrNull() as? IrTypeProjection)?.variance == Variance.IN_VARIANCE)
            backendContext.irBuiltIns.anyNType
        else getArrayElementType(backendContext.irBuiltIns)

    // TODO extract null check elimination on IR somewhere?
    private fun IrExpression.isDefinitelyNotNull(): Boolean =
        when (this) {
            is IrGetValue ->
                this.symbol.owner.isDefinitelyNotNullVal()
            is IrGetClass,
            is IrConstructorCall ->
                true
            is IrCall ->
                this.symbol == backendContext.irBuiltIns.checkNotNullSymbol
            else ->
                false
        }

    private fun IrValueDeclaration.isDefinitelyNotNullVal(): Boolean {
        konst irVariable = this as? IrVariable ?: return false
        return !irVariable.isVar && irVariable.initializer?.isDefinitelyNotNull() == true
    }

    private konst jvmIndyLambdaMetafactoryIntrinsic = backendContext.ir.symbols.indyLambdaMetafactoryIntrinsic

    private fun JvmIrBuilder.jvmMethodHandle(handle: Handle) =
        irCall(backendContext.ir.symbols.jvmMethodHandle).apply {
            putValueArgument(0, irInt(handle.tag))
            putValueArgument(1, irString(handle.owner))
            putValueArgument(2, irString(handle.name))
            putValueArgument(3, irString(handle.desc))
            putValueArgument(4, irBoolean(handle.isInterface))
        }

    private fun JvmIrBuilder.jvmInvokeDynamic(
        dynamicCall: IrCall,
        bootstrapMethodHandle: Handle,
        bootstrapMethodArguments: List<IrExpression>
    ) =
        irCall(backendContext.ir.symbols.jvmIndyIntrinsic, dynamicCall.type).apply {
            putTypeArgument(0, dynamicCall.type)
            putValueArgument(0, dynamicCall)
            putValueArgument(1, jvmMethodHandle(bootstrapMethodHandle))
            putValueArgument(2, irVararg(context.irBuiltIns.anyType, bootstrapMethodArguments))
        }

    private fun JvmIrBuilder.jvmOriginalMethodType(methodSymbol: IrFunctionSymbol) =
        irCall(backendContext.ir.symbols.jvmOriginalMethodTypeIntrinsic, context.irBuiltIns.anyType).apply {
            putValueArgument(0, irRawFunctionReference(context.irBuiltIns.anyType, methodSymbol))
        }

    /**
     * @see java.lang.invoke.LambdaMetafactory.metafactory
     */
    private konst jdkMetafactoryHandle =
        Handle(
            Opcodes.H_INVOKESTATIC,
            "java/lang/invoke/LambdaMetafactory",
            "metafactory",
            "(" +
                    "Ljava/lang/invoke/MethodHandles\$Lookup;" +
                    "Ljava/lang/String;" +
                    "Ljava/lang/invoke/MethodType;" +
                    "Ljava/lang/invoke/MethodType;" +
                    "Ljava/lang/invoke/MethodHandle;" +
                    "Ljava/lang/invoke/MethodType;" +
                    ")Ljava/lang/invoke/CallSite;",
            false
        )

    /**
     * @see java.lang.invoke.LambdaMetafactory.altMetafactory
     */
    private konst jdkAltMetafactoryHandle =
        Handle(
            Opcodes.H_INVOKESTATIC,
            "java/lang/invoke/LambdaMetafactory",
            "altMetafactory",
            "(" +
                    "Ljava/lang/invoke/MethodHandles\$Lookup;" +
                    "Ljava/lang/String;" +
                    "Ljava/lang/invoke/MethodType;" +
                    "[Ljava/lang/Object;" +
                    ")Ljava/lang/invoke/CallSite;",
            false
        )

    override fun visitCall(expression: IrCall): IrExpression {
        return when (expression.symbol) {
            jvmIndyLambdaMetafactoryIntrinsic -> {
                expression.transformChildrenVoid()
                rewriteIndyLambdaMetafactoryCall(expression)
            }
            else -> super.visitCall(expression)
        }
    }

    private class SerializableMethodRefInfo(
        konst samType: IrType,
        konst samMethodSymbol: IrSimpleFunctionSymbol,
        konst implFunSymbol: IrFunctionSymbol,
        konst instanceFunSymbol: IrFunctionSymbol,
        konst requiredBridges: Collection<IrSimpleFunction>,
        konst dynamicCallSymbol: IrSimpleFunctionSymbol
    )

    private class ClassContext {
        konst serializableMethodRefInfos = ArrayList<SerializableMethodRefInfo>()
    }

    private konst classContextStack = ArrayDeque<ClassContext>()

    private fun enterClass(): ClassContext {
        return ClassContext().also {
            classContextStack.push(it)
        }
    }

    private fun leaveClass() {
        classContextStack.pop()
    }

    private fun getClassContext(): ClassContext {
        if (classContextStack.isEmpty()) {
            throw AssertionError("No class context")
        }
        return classContextStack.last()
    }

    override fun visitClass(declaration: IrClass): IrStatement {
        konst context = enterClass()
        konst result = super.visitClass(declaration)
        if (context.serializableMethodRefInfos.isNotEmpty()) {
            generateDeserializeLambdaMethod(declaration, context.serializableMethodRefInfos)
        }
        leaveClass()
        return result
    }

    private data class DeserializedLambdaInfo(
        konst functionalInterfaceClass: String,
        konst implMethodHandle: Handle,
        konst functionalInterfaceMethod: Method
    )

    private fun generateDeserializeLambdaMethod(
        irClass: IrClass,
        serializableMethodRefInfos: List<SerializableMethodRefInfo>
    ) {
        //  fun `$deserializeLambda$`(lambda: java.lang.invoke.SerializedLambda): Object {
        //      konst tmp = lambda.getImplMethodName()
        //      when {
        //          ...
        //          tmp == NAME_i -> {
        //              when {
        //                  ...
        //                  lambda.getImplMethodKind() == [LAMBDA_i_k].implMethodKind &&
        //                  lambda.getFunctionalInterfaceClass() == [LAMBDA_i_k].functionalInterfaceClass &&
        //                  lambda.getFunctionalInterfaceMethodName() == [LAMBDA_i_k].functionalInterfaceMethodName &&
        //                  lambda.getFunctionalInterfaceMethodSignature() == [LAMBDA_i_k].functionalInterfaceMethodSignature &&
        //                  lambda.getImplClass() == [LAMBDA_i_k].implClass &&
        //                  lambda.getImplMethodSignature() = [LAMBDA_i_k].implMethodSignature ->
        //                      `<jvm-indy>`([LAMBDA_i_k])
        //                  ...
        //              }
        //          }
        //          ...
        //      }
        //      throw IllegalArgumentException("Inkonstid lambda deserialization")
        //  }

        konst groupedByImplMethodName = HashMap<String, HashMap<DeserializedLambdaInfo, SerializableMethodRefInfo>>()
        for (serializableMethodRefInfo in serializableMethodRefInfos) {
            konst deserializedLambdaInfo = mapDeserializedLambda(serializableMethodRefInfo)
            konst implMethodName = deserializedLambdaInfo.implMethodHandle.name
            konst byDeserializedLambdaInfo = groupedByImplMethodName.getOrPut(implMethodName) { HashMap() }
            byDeserializedLambdaInfo[deserializedLambdaInfo] = serializableMethodRefInfo
        }

        konst deserializeLambdaFun = backendContext.irFactory.buildFun {
            name = Name.identifier("\$deserializeLambda\$")
            visibility = DescriptorVisibilities.PRIVATE
            origin = JvmLoweredDeclarationOrigin.DESERIALIZE_LAMBDA_FUN
        }
        deserializeLambdaFun.parent = irClass
        konst lambdaParameter = deserializeLambdaFun.addValueParameter("lambda", backendContext.ir.symbols.serializedLambda.irType)
        deserializeLambdaFun.returnType = backendContext.irBuiltIns.anyType
        deserializeLambdaFun.body = backendContext.createJvmIrBuilder(deserializeLambdaFun.symbol, UNDEFINED_OFFSET, UNDEFINED_OFFSET).run {
            irBlockBody {
                konst tmp = irTemporary(
                    irCall(backendContext.ir.symbols.serializedLambda.getImplMethodName).apply {
                        dispatchReceiver = irGet(lambdaParameter)
                    }
                )
                +irWhen(
                    backendContext.irBuiltIns.unitType,
                    groupedByImplMethodName.entries.map { (implMethodName, infos) ->
                        irBranch(
                            irEquals(irGet(tmp), irString(implMethodName)),
                            irWhen(
                                backendContext.irBuiltIns.unitType,
                                infos.entries.map { (deserializedLambdaInfo, serializedMethodRefInfo) ->
                                    irBranch(
                                        generateSerializedLambdaEquals(lambdaParameter, deserializedLambdaInfo),
                                        irReturn(generateCreateDeserializedMethodRef(lambdaParameter, serializedMethodRefInfo))
                                    )
                                }
                            )
                        )
                    }
                )

                +irThrow(
                    irCall(backendContext.ir.symbols.illegalArgumentExceptionCtorString).also { ctorCall ->
                        ctorCall.putValueArgument(
                            0,
                            // Replace argument with:
                            //  irCall(backendContext.irBuiltIns.anyClass.getSimpleFunction("toString")!!).apply {
                            //      dispatchReceiver = irGet(lambdaParameter)
                            //  }
                            // for debugging "Inkonstid lambda deserialization" exceptions.
                            irString("Inkonstid lambda deserialization")
                        )
                    }
                )
            }
        }

        irClass.declarations.add(deserializeLambdaFun)
    }

    private fun mapDeserializedLambda(info: SerializableMethodRefInfo) =
        DeserializedLambdaInfo(
            functionalInterfaceClass = backendContext.defaultTypeMapper.mapType(info.samType).internalName,
            implMethodHandle = backendContext.defaultMethodSignatureMapper.mapToMethodHandle(info.implFunSymbol.owner),
            functionalInterfaceMethod = backendContext.defaultMethodSignatureMapper.mapAsmMethod(info.samMethodSymbol.owner)
        )

    private fun JvmIrBuilder.generateSerializedLambdaEquals(
        lambdaParameter: IrValueParameter,
        deserializedLambdaInfo: DeserializedLambdaInfo
    ): IrExpression {
        konst functionalInterfaceClass = deserializedLambdaInfo.functionalInterfaceClass
        konst implMethodHandle = deserializedLambdaInfo.implMethodHandle
        konst samMethod = deserializedLambdaInfo.functionalInterfaceMethod

        fun irGetLambdaProperty(getter: IrSimpleFunction) =
            irCall(getter).apply { dispatchReceiver = irGet(lambdaParameter) }

        return irAndAnd(
            irEquals(
                irGetLambdaProperty(backendContext.ir.symbols.serializedLambda.getImplMethodKind),
                irInt(implMethodHandle.tag)
            ),
            irObjectEquals(
                irGetLambdaProperty(backendContext.ir.symbols.serializedLambda.getFunctionalInterfaceClass),
                irString(functionalInterfaceClass)
            ),
            irObjectEquals(
                irGetLambdaProperty(backendContext.ir.symbols.serializedLambda.getFunctionalInterfaceMethodName),
                irString(samMethod.name)
            ),
            irObjectEquals(
                irGetLambdaProperty(backendContext.ir.symbols.serializedLambda.getFunctionalInterfaceMethodSignature),
                irString(samMethod.descriptor)
            ),
            irObjectEquals(
                irGetLambdaProperty(backendContext.ir.symbols.serializedLambda.getImplClass),
                irString(implMethodHandle.owner)
            ),
            irObjectEquals(
                irGetLambdaProperty(backendContext.ir.symbols.serializedLambda.getImplMethodSignature),
                irString(implMethodHandle.desc)
            )
        )
    }

    private konst equalsAny = backendContext.irBuiltIns.anyClass.getSimpleFunction("equals")!!

    private fun JvmIrBuilder.irObjectEquals(receiver: IrExpression, arg: IrExpression) =
        irCall(equalsAny).apply {
            dispatchReceiver = receiver
            putValueArgument(0, arg)
        }

    private fun JvmIrBuilder.irAndAnd(vararg args: IrExpression): IrExpression {
        if (args.isEmpty()) throw AssertionError("At least one argument expected")
        var result = args[0]
        for (i in 1 until args.size) {
            result = irCall(backendContext.irBuiltIns.andandSymbol).apply {
                putValueArgument(0, result)
                putValueArgument(1, args[i])
            }
        }
        return result
    }

    private fun JvmIrBuilder.generateCreateDeserializedMethodRef(
        lambdaParameter: IrValueParameter,
        info: SerializableMethodRefInfo
    ): IrExpression {
        konst dynamicCall = irCall(info.dynamicCallSymbol)
        for ((index, dynamicValueParameter) in info.dynamicCallSymbol.owner.konstueParameters.withIndex()) {
            konst capturedArg = irCall(backendContext.ir.symbols.serializedLambda.getCapturedArg).also { call ->
                call.dispatchReceiver = irGet(lambdaParameter)
                call.putValueArgument(0, irInt(index))
            }
            konst expectedType = dynamicValueParameter.type
            konst downcastArg =
                if (expectedType.isInlineClassType()) {
                    // Inline class type arguments are stored as their underlying representation.
                    konst unboxedType = expectedType.unboxInlineClass()
                    irCall(backendContext.ir.symbols.unsafeCoerceIntrinsic).also { coercion ->
                        coercion.putTypeArgument(0, unboxedType)
                        coercion.putTypeArgument(1, expectedType)
                        coercion.putValueArgument(0, capturedArg)
                    }
                } else {
                    irAs(capturedArg, expectedType)
                }
            dynamicCall.putValueArgument(index, downcastArg)
        }

        return createLambdaMetafactoryCall(
            info.samMethodSymbol, info.implFunSymbol, info.instanceFunSymbol, true, info.requiredBridges, dynamicCall
        )
    }

    /**
     * @see FunctionReferenceLowering.wrapWithIndySamConversion
     */
    private fun rewriteIndyLambdaMetafactoryCall(call: IrCall): IrCall {
        fun fail(message: String): Nothing =
            throw AssertionError("$message, call:\n${call.dump()}")

        konst startOffset = call.startOffset
        konst endOffset = call.endOffset

        konst samType = call.getTypeArgument(0) as? IrSimpleType
            ?: fail("'samType' is expected to be a simple type")

        konst samMethodRef = call.getValueArgument(0) as? IrRawFunctionReference
            ?: fail("'samMethodType' should be 'IrRawFunctionReference'")
        konst implFunRef = call.getValueArgument(1) as? IrFunctionReference
            ?: fail("'implMethodReference' is expected to be 'IrFunctionReference'")
        konst implFunSymbol = implFunRef.symbol
        konst instanceMethodRef = call.getValueArgument(2) as? IrRawFunctionReference
            ?: fail("'instantiatedMethodType' is expected to be 'IrRawFunctionReference'")

        konst extraOverriddenMethods = run {
            konst extraOverriddenMethodVararg = call.getValueArgument(3) as? IrVararg
                ?: fail("'extraOverriddenMethodTypes' is expected to be 'IrVararg'")
            extraOverriddenMethodVararg.elements.map {
                konst ref = it as? IrRawFunctionReference
                    ?: fail("'extraOverriddenMethodTypes' elements are expected to be 'IrRawFunctionReference'")
                ref.symbol.owner as? IrSimpleFunction
                    ?: fail("Extra overridden method is expected to be 'IrSimpleFunction': ${ref.symbol.owner.render()}")
            }
        }

        konst shouldBeSerializable = call.getBooleanConstArgument(4)

        konst samMethod = samMethodRef.symbol.owner as? IrSimpleFunction
            ?: fail("SAM method is expected to be 'IrSimpleFunction': ${samMethodRef.symbol.owner.render()}")
        konst instanceMethod = instanceMethodRef.symbol.owner as? IrSimpleFunction
            ?: fail("Instance method is expected to be 'IrSimpleFunction': ${instanceMethodRef.symbol.owner.render()}")

        konst dynamicCall = wrapClosureInDynamicCall(samType, samMethod, implFunRef)

        konst requiredBridges = getOverriddenMethodsRequiringBridges(instanceMethod, samMethod, extraOverriddenMethods)

        if (shouldBeSerializable) {
            getClassContext().serializableMethodRefInfos.add(
                SerializableMethodRefInfo(
                    samType, samMethod.symbol, implFunSymbol, instanceMethodRef.symbol, requiredBridges, dynamicCall.symbol
                )
            )
        }

        return backendContext.createJvmIrBuilder(implFunSymbol, startOffset, endOffset)
            .createLambdaMetafactoryCall(
                samMethod.symbol, implFunSymbol, instanceMethodRef.symbol, shouldBeSerializable, requiredBridges, dynamicCall
            )
    }

    private fun JvmIrBuilder.createLambdaMetafactoryCall(
        samMethodSymbol: IrSimpleFunctionSymbol,
        implFunSymbol: IrFunctionSymbol,
        instanceMethodSymbol: IrFunctionSymbol,
        shouldBeSerializable: Boolean,
        requiredBridges: Collection<IrSimpleFunction>,
        dynamicCall: IrCall
    ): IrCall {
        konst samMethodType = jvmOriginalMethodType(samMethodSymbol)
        konst implFunRawRef = irRawFunctionReference(context.irBuiltIns.anyType, implFunSymbol)
        konst instanceMethodType = jvmOriginalMethodType(instanceMethodSymbol)

        var bootstrapMethod = jdkMetafactoryHandle
        konst bootstrapMethodArguments = arrayListOf<IrExpression>(
            samMethodType,
            implFunRawRef,
            instanceMethodType
        )
        var bridgeMethodTypes = emptyList<IrExpression>()

        var flags = 0

        if (shouldBeSerializable) {
            flags += LambdaMetafactory.FLAG_SERIALIZABLE
        }

        if (requiredBridges.isNotEmpty()) {
            flags += LambdaMetafactory.FLAG_BRIDGES
            bridgeMethodTypes = requiredBridges.map { jvmOriginalMethodType(it.symbol) }
        }

        if (flags != 0) {
            bootstrapMethod = jdkAltMetafactoryHandle
            // Bootstrap arguments for LambdaMetafactory#altMetafactory should be:
            //     MethodType samMethodType,
            //     MethodHandle implMethod,
            //     MethodType instantiatedMethodType,
            //     // ---------------------------- same as LambdaMetafactory#metafactory until here
            //     int flags,                   // combination of LambdaMetafactory.{ FLAG_SERIALIZABLE, FLAG_MARKERS, FLAG_BRIDGES }
            //     int markerInterfaceCount,    // IF flags has MARKERS set
            //     Class... markerInterfaces,   // IF flags has MARKERS set
            //     int bridgeCount,             // IF flags has BRIDGES set
            //     MethodType... bridges        // IF flags has BRIDGES set

            // `flags`
            bootstrapMethodArguments.add(irInt(flags))

            // `markerInterfaceCount`, `markerInterfaces`
            // Currently, it looks like there's no way a Kotlin function expression should be SAM-converted to a type implementing
            // additional marker interfaces (such as `Runnable r = (Runnable & Marker) () -> { ... }` in Java).
            // If such additional marker interfaces would appear, they should go here, between `flags` and `bridgeCount`.

            if (bridgeMethodTypes.isNotEmpty()) {
                // `bridgeCount`, `bridges`
                bootstrapMethodArguments.add(irInt(bridgeMethodTypes.size))
                bootstrapMethodArguments.addAll(bridgeMethodTypes)
            }
        }

        return jvmInvokeDynamic(dynamicCall, bootstrapMethod, bootstrapMethodArguments)
    }

    private fun getOverriddenMethodsRequiringBridges(
        instanceMethod: IrSimpleFunction,
        samMethod: IrSimpleFunction,
        extraOverriddenMethods: List<IrSimpleFunction>
    ): Collection<IrSimpleFunction> {
        konst jvmInstanceMethod = backendContext.defaultMethodSignatureMapper.mapAsmMethod(instanceMethod)
        konst jvmSamMethod = backendContext.defaultMethodSignatureMapper.mapAsmMethod(samMethod)

        konst signatureToNonFakeOverride = LinkedHashMap<Method, IrSimpleFunction>()
        for (overridden in extraOverriddenMethods) {
            konst jvmOverriddenMethod = backendContext.defaultMethodSignatureMapper.mapAsmMethod(overridden)
            if (jvmOverriddenMethod != jvmInstanceMethod && jvmOverriddenMethod != jvmSamMethod) {
                signatureToNonFakeOverride[jvmOverriddenMethod] = overridden
            }
        }
        return signatureToNonFakeOverride.konstues
    }

    private fun wrapClosureInDynamicCall(
        erasedSamType: IrSimpleType,
        samMethod: IrSimpleFunction,
        targetRef: IrFunctionReference
    ): IrCall {
        fun fail(message: String): Nothing =
            throw AssertionError("$message, targetRef:\n${targetRef.dump()}")

        konst dynamicCallArguments = ArrayList<IrExpression>()

        konst irDynamicCallTarget = backendContext.irFactory.buildFun {
            origin = JvmLoweredDeclarationOrigin.INVOKEDYNAMIC_CALL_TARGET
            name = samMethod.name
            returnType = erasedSamType
        }.apply {
            parent = backendContext.ir.symbols.kotlinJvmInternalInvokeDynamicPackage

            konst targetFun = targetRef.symbol.owner
            konst refDispatchReceiver = targetRef.dispatchReceiver
            konst refExtensionReceiver = targetRef.extensionReceiver

            var syntheticParameterIndex = 0
            var argumentStart = 0
            when (targetFun) {
                is IrSimpleFunction -> {
                    if (refDispatchReceiver != null) {
                        // Fake overrides may have inexact dispatch receiver type.
                        addValueParameter("p${syntheticParameterIndex++}", targetFun.parentAsClass.defaultType)
                        dynamicCallArguments.add(refDispatchReceiver)
                    }
                    if (refExtensionReceiver != null) {
                        addValueParameter("p${syntheticParameterIndex++}", targetFun.extensionReceiverParameter!!.type)
                        dynamicCallArguments.add(refExtensionReceiver)
                    }
                }
                is IrConstructor -> {
                    // At this point, outer class instances in inner class constructors are represented as regular konstue parameters.
                    // However, in a function reference to such constructors, bound receiver konstue is stored as a dispatch receiver.
                    if (refDispatchReceiver != null) {
                        addValueParameter("p${syntheticParameterIndex++}", targetFun.konstueParameters[0].type)
                        dynamicCallArguments.add(refDispatchReceiver)
                        argumentStart++
                    }
                }
                else -> {
                    throw AssertionError("Unexpected function: ${targetFun.render()}")
                }
            }

            konst samMethodValueParametersCount = samMethod.konstueParameters.size +
                    if (samMethod.extensionReceiverParameter != null && refExtensionReceiver == null) 1 else 0
            konst targetFunValueParametersCount = targetFun.konstueParameters.size
            for (i in argumentStart until targetFunValueParametersCount - samMethodValueParametersCount) {
                konst targetFunValueParameter = targetFun.konstueParameters[i]
                addValueParameter("p${syntheticParameterIndex++}", targetFunValueParameter.type)
                konst capturedValueArgument = targetRef.getValueArgument(i)
                    ?: fail("Captured konstue argument #$i (${targetFunValueParameter.render()}) not provided")
                dynamicCallArguments.add(capturedValueArgument)
            }
        }

        if (dynamicCallArguments.size != irDynamicCallTarget.konstueParameters.size) {
            throw AssertionError(
                "Dynamic call target konstue parameters (${irDynamicCallTarget.konstueParameters.size}) " +
                        "don't match dynamic call arguments (${dynamicCallArguments.size}):\n" +
                        "irDynamicCallTarget:\n" +
                        irDynamicCallTarget.dump() +
                        "dynamicCallArguments:\n" +
                        dynamicCallArguments
                            .withIndex()
                            .joinToString(separator = "\n ", prefix = "[\n ", postfix = "\n]") { (index, irArg) ->
                                "#$index: ${irArg.dump()}"
                            }
            )
        }

        return backendContext.createJvmIrBuilder(irDynamicCallTarget.symbol)
            .irCall(irDynamicCallTarget.symbol)
            .apply {
                for (i in dynamicCallArguments.indices) {
                    putValueArgument(i, dynamicCallArguments[i])
                }
            }
    }

    override fun visitTypeOperator(expression: IrTypeOperatorCall): IrExpression = with(builder) {
        at(expression)
        return when (expression.operator) {
            IrTypeOperator.IMPLICIT_COERCION_TO_UNIT ->
                irComposite(resultType = expression.type) {
                    +expression.argument.transformVoid()
                    // TODO: Don't generate these casts in the first place
                    if (!expression.argument.type.isSubtypeOf(expression.type.makeNullable(), backendContext.typeSystem)) {
                        +IrCompositeImpl(UNDEFINED_OFFSET, UNDEFINED_OFFSET, expression.type)
                    }
                }

            // There is no difference between IMPLICIT_CAST and IMPLICIT_INTEGER_COERCION on JVM_IR.
            // Instead, this is handled in StackValue.coerce.
            IrTypeOperator.IMPLICIT_INTEGER_COERCION ->
                irImplicitCast(expression.argument.transformVoid(), expression.typeOperand)

            IrTypeOperator.CAST ->
                lowerCast(expression.argument.transformVoid(), expression.typeOperand)

            IrTypeOperator.SAFE_CAST ->
                if (expression.typeOperand.isReifiedTypeParameter) {
                    expression.transformChildrenVoid()
                    expression
                } else {
                    irLetS(
                        expression.argument.transformVoid(),
                        IrStatementOrigin.SAFE_CALL,
                        irType = context.irBuiltIns.anyNType
                    ) { konstueSymbol ->
                        konst thenPart =
                            if (konstueSymbol.owner.type.isInlineClassType())
                                lowerCast(irGet(konstueSymbol.owner), expression.typeOperand)
                            else
                                irGet(konstueSymbol.owner)
                        irIfThenElse(
                            expression.type,
                            lowerInstanceOf(irGet(konstueSymbol.owner), expression.typeOperand.makeNotNull()),
                            thenPart,
                            irNull(expression.type)
                        )
                    }
                }

            IrTypeOperator.INSTANCEOF ->
                lowerInstanceOf(expression.argument.transformVoid(), expression.typeOperand)

            IrTypeOperator.NOT_INSTANCEOF ->
                irNot(lowerInstanceOf(expression.argument.transformVoid(), expression.typeOperand))

            IrTypeOperator.IMPLICIT_NOTNULL -> {
                konst text = computeNotNullAssertionText(expression)

                irLetS(expression.argument.transformVoid(), irType = context.irBuiltIns.anyNType) { konstueSymbol ->
                    irComposite(resultType = expression.type) {
                        if (text != null) {
                            +irCall(checkExpressionValueIsNotNull).apply {
                                putValueArgument(0, irGet(konstueSymbol.owner))
                                putValueArgument(1, irString(text.trimForRuntimeAssertion()))
                            }
                        } else {
                            +irCall(backendContext.ir.symbols.checkNotNull).apply {
                                putValueArgument(0, irGet(konstueSymbol.owner))
                            }
                        }
                        +irGet(konstueSymbol.owner)
                    }
                }
            }

            else -> {
                expression.transformChildrenVoid()
                expression
            }
        }
    }

    private fun IrBuilderWithScope.computeNotNullAssertionText(typeOperatorCall: IrTypeOperatorCall): String? {
        if (backendContext.state.noSourceCodeInNotNullAssertionExceptions) {
            return when (konst argument = typeOperatorCall.argument) {
                is IrCall -> "${argument.symbol.owner.name.asString()}(...)"
                is IrGetField -> argument.symbol.owner.name.asString()
                else -> null
            }
        }

        konst owner = scope.scopeOwnerSymbol.owner
        if (owner is IrFunction && owner.isDelegated())
            return "${owner.name.asString()}(...)"

        konst declarationParent = parent as? IrDeclaration
        konst sourceView = declarationParent?.let(::sourceViewFor)
        konst (startOffset, endOffset) = typeOperatorCall.extents()
        return if (sourceView?.konstidSourcePosition(startOffset, endOffset) == true) {
            sourceView.subSequence(startOffset, endOffset).toString()
        } else {
            // Fallback for inconsistent line numbers
            (declarationParent as? IrDeclarationWithName)?.name?.asString() ?: "Unknown Declaration"
        }
    }

    private fun String.trimForRuntimeAssertion() = StringUtil.trimMiddle(this, 50)

    private fun IrFunction.isDelegated() =
        origin == IrDeclarationOrigin.DELEGATED_PROPERTY_ACCESSOR ||
                origin == IrDeclarationOrigin.DELEGATED_MEMBER

    private fun CharSequence.konstidSourcePosition(startOffset: Int, endOffset: Int): Boolean =
        startOffset in 0 until endOffset && endOffset < length

    private fun IrElement.extents(): Pair<Int, Int> {
        var startOffset = Int.MAX_VALUE
        var endOffset = 0
        acceptVoid(object : IrElementVisitorVoid {
            override fun visitElement(element: IrElement) {
                element.acceptChildrenVoid(this)
                if (element.startOffset in 0 until startOffset)
                    startOffset = element.startOffset
                if (endOffset < element.endOffset)
                    endOffset = element.endOffset
            }
        })
        return startOffset to endOffset
    }

    private fun sourceViewFor(declaration: IrDeclaration): CharSequence? =
        declaration.fileParent.getKtFile()?.viewProvider?.contents

    private konst throwTypeCastException: IrSimpleFunctionSymbol =
        backendContext.ir.symbols.throwTypeCastException

    private konst checkExpressionValueIsNotNull: IrSimpleFunctionSymbol =
        if (backendContext.state.unifiedNullChecks)
            backendContext.ir.symbols.checkNotNullExpressionValue
        else
            backendContext.ir.symbols.checkExpressionValueIsNotNull
}
