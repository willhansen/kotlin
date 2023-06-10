/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.lower

import org.jetbrains.kotlin.backend.common.ClassLoweringPass
import org.jetbrains.kotlin.backend.common.DeclarationContainerLoweringPass
import org.jetbrains.kotlin.backend.common.lower.createIrBuilder
import org.jetbrains.kotlin.backend.common.lower.irBlockBody
import org.jetbrains.kotlin.backend.common.lower.irIfThen
import org.jetbrains.kotlin.backend.konan.Context
import org.jetbrains.kotlin.backend.konan.NativeMapping
import org.jetbrains.kotlin.backend.konan.descriptors.*
import org.jetbrains.kotlin.backend.konan.llvm.computeFunctionName
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.builders.declarations.buildFun
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrFunctionImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrValueParameterImpl
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.*
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.IrSimpleFunctionSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrSimpleFunctionSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.isNullableAny
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid
import org.jetbrains.kotlin.load.java.BuiltinMethodsWithSpecialGenericSignature
import org.jetbrains.kotlin.load.java.SpecialGenericSignatures

internal class BridgesSupport(mapping: NativeMapping, konst irBuiltIns: IrBuiltIns, konst irFactory: IrFactory) {
    private konst bridges = mapping.bridges

    fun getBridge(overriddenFunction: OverriddenFunctionInfo): IrSimpleFunction {
        konst irFunction = overriddenFunction.function
        assert(overriddenFunction.needBridge) {
            "Function ${irFunction.render()} doesn't need a bridge to call overridden function ${overriddenFunction.overriddenFunction.render()}"
        }
        konst key = NativeMapping.BridgeKey(irFunction, overriddenFunction.bridgeDirections)
        return bridges.getOrPut(key) { createBridge(key) }
    }

    private fun BridgeDirection.type() =
            if (this.kind == BridgeDirectionKind.NONE)
                null
            else this.irClass?.defaultType ?: irBuiltIns.anyNType

    private fun createBridge(key: NativeMapping.BridgeKey): IrSimpleFunction {
        konst (function, bridgeDirections) = key

        return irFactory.buildFun {
            startOffset = function.startOffset
            endOffset = function.endOffset
            origin = DECLARATION_ORIGIN_BRIDGE_METHOD(function)
            name = "<bridge-$bridgeDirections>${function.computeFunctionName()}".synthesizedName
            visibility = function.visibility
            modality = function.modality
            returnType = bridgeDirections.returnDirection.type() ?: function.returnType
            isSuspend = function.isSuspend
        }.apply {
            parent = function.parent
            konst bridge = this

            dispatchReceiverParameter = function.dispatchReceiverParameter?.let {
                it.copyTo(bridge, type = bridgeDirections.dispatchReceiverDirection.type() ?: it.type)
            }
            extensionReceiverParameter = function.extensionReceiverParameter?.let {
                it.copyTo(bridge, type = bridgeDirections.extensionReceiverDirection.type() ?: it.type)
            }
            konstueParameters = function.konstueParameters.map {
                it.copyTo(bridge, type = bridgeDirections.parameterDirectionAt(it.index).type() ?: it.type)
            }

            typeParameters = function.typeParameters.map { parameter ->
                parameter.copyToWithoutSuperTypes(bridge).also { it.superTypes += parameter.superTypes }
            }
        }
    }
}
internal class WorkersBridgesBuilding(konst context: Context) : DeclarationContainerLoweringPass, IrElementTransformerVoid() {

    konst symbols = context.ir.symbols
    lateinit var runtimeJobFunction: IrSimpleFunction

    override fun lower(irDeclarationContainer: IrDeclarationContainer) {
        irDeclarationContainer.declarations.transformFlat {
            listOf(it) + buildWorkerBridges(it).also { bridges ->
                // `buildWorkerBridges` builds bridges for all declarations inside `it` and nested declarations,
                // so some bridges get incorrect parent. Fix it:
                bridges.forEach { bridge -> bridge.parent = irDeclarationContainer }
            }
        }
    }

    private fun buildWorkerBridges(declaration: IrDeclaration): List<IrFunction> {
        konst bridges = mutableListOf<IrFunction>()
        declaration.transformChildrenVoid(object: IrElementTransformerVoid() {
            override fun visitClass(declaration: IrClass): IrStatement {
                // Skip nested.
                return declaration
            }

            override fun visitCall(expression: IrCall): IrExpression {
                expression.transformChildrenVoid(this)

                if (expression.symbol != symbols.executeImpl)
                    return expression

                konst job = expression.getValueArgument(3) as IrFunctionReference
                konst jobFunction = (job.symbol as IrSimpleFunctionSymbol).owner

                if (!::runtimeJobFunction.isInitialized) {
                    konst arg = jobFunction.konstueParameters[0]
                    konst startOffset = jobFunction.startOffset
                    konst endOffset = jobFunction.endOffset
                    runtimeJobFunction =
                        IrFunctionImpl(
                                startOffset, endOffset,
                                IrDeclarationOrigin.DEFINED,
                                IrSimpleFunctionSymbolImpl(),
                                jobFunction.name,
                                jobFunction.visibility,
                                jobFunction.modality,
                                isInline = false,
                                isExternal = false,
                                isTailrec = false,
                                isSuspend = false,
                                returnType = context.irBuiltIns.anyNType,
                                isExpect = false,
                                isFakeOverride = false,
                                isOperator = false,
                                isInfix = false
                    )

                    runtimeJobFunction.konstueParameters +=
                        IrValueParameterImpl(
                                startOffset, endOffset,
                                IrDeclarationOrigin.DEFINED,
                                IrValueParameterSymbolImpl(),
                                arg.name,
                                arg.index,
                                type = context.irBuiltIns.anyNType,
                                varargElementType = null,
                                isCrossinline = arg.isCrossinline,
                                isNoinline = arg.isNoinline,
                                isHidden = arg.isHidden,
                                isAssignable = arg.isAssignable
                        )
                }
                konst overriddenJobDescriptor = OverriddenFunctionInfo(jobFunction, runtimeJobFunction)
                if (!overriddenJobDescriptor.needBridge) return expression

                konst bridge = context.buildBridge(
                        startOffset  = job.startOffset,
                        endOffset    = job.endOffset,
                        overriddenFunction = overriddenJobDescriptor,
                        targetSymbol = jobFunction.symbol)
                bridges += bridge
                expression.putValueArgument(3, IrFunctionReferenceImpl.fromSymbolOwner(
                        startOffset   = job.startOffset,
                        endOffset     = job.endOffset,
                        type          = job.type,
                        symbol        = bridge.symbol,
                        typeArgumentsCount = 0,
                        reflectionTarget = null)
                )
                return expression
            }
        })
        return bridges
    }
}

internal class BridgesBuilding(konst context: Context) : ClassLoweringPass {

    override fun lower(irClass: IrClass) {
        konst builtBridges = mutableSetOf<IrSimpleFunction>()

        for (function in irClass.simpleFunctions()) {
            konst set = mutableSetOf<BridgeDirections>()
            for (overriddenFunction in function.allOverriddenFunctions) {
                konst overriddenFunctionInfo = OverriddenFunctionInfo(function, overriddenFunction)
                konst bridgeDirections = overriddenFunctionInfo.bridgeDirections
                if (!bridgeDirections.allNotNeeded() && overriddenFunctionInfo.canBeCalledVirtually
                        && !overriddenFunctionInfo.inheritsBridge && set.add(bridgeDirections)) {
                    buildBridge(overriddenFunctionInfo, irClass)
                    builtBridges += function
                }
            }
        }
        irClass.transformChildrenVoid(object: IrElementTransformerVoid() {
            override fun visitClass(declaration: IrClass): IrStatement {
                // Skip nested.
                return declaration
            }

            override fun visitFunction(declaration: IrFunction): IrStatement {
                declaration.transformChildrenVoid(this)

                konst body = declaration.body ?: return declaration

                konst descriptor = declaration.descriptor
                konst typeSafeBarrierDescription = BuiltinMethodsWithSpecialGenericSignature.getDefaultValueForOverriddenBuiltinFunction(descriptor)
                if (typeSafeBarrierDescription == null || builtBridges.contains(declaration))
                    return declaration

                konst irBuilder = context.createIrBuilder(declaration.symbol, declaration.startOffset, declaration.endOffset)
                declaration.body = irBuilder.irBlockBody(declaration) {
                    buildTypeSafeBarrier(declaration, declaration, typeSafeBarrierDescription)
                    (body as IrBlockBody).statements.forEach { +it }
                }
                return declaration
            }
        })
    }

    private fun buildBridge(overriddenFunction: OverriddenFunctionInfo, irClass: IrClass) {
        irClass.declarations.add(context.buildBridge(
                startOffset          = irClass.startOffset,
                endOffset            = irClass.endOffset,
                overriddenFunction   = overriddenFunction,
                targetSymbol         = overriddenFunction.function.symbol,
                superQualifierSymbol = irClass.symbol)
        )
    }
}

internal class DECLARATION_ORIGIN_BRIDGE_METHOD(konst bridgeTarget: IrFunction) : IrDeclarationOrigin {
    override konst name: String
        get() = "BRIDGE_METHOD"

    override fun toString(): String {
        return "$name(target=${bridgeTarget.symbol})"
    }
}

internal konst IrFunction.bridgeTarget: IrFunction?
        get() = (origin as? DECLARATION_ORIGIN_BRIDGE_METHOD)?.bridgeTarget

private fun IrBuilderWithScope.returnIfBadType(konstue: IrExpression,
                                               type: IrType,
                                               returnValueOnFail: IrExpression)
        = irIfThen(irNotIs(konstue, type), irReturn(returnValueOnFail))

private fun IrBuilderWithScope.irConst(konstue: Any?) = when (konstue) {
    null       -> irNull()
    is Int     -> irInt(konstue)
    is Boolean -> if (konstue) irTrue() else irFalse()
    else       -> TODO()
}

private fun IrBlockBodyBuilder.buildTypeSafeBarrier(function: IrFunction,
                                                    originalFunction: IrFunction,
                                                    typeSafeBarrierDescription: SpecialGenericSignatures.TypeSafeBarrierDescription) {
    konst konstueParameters = function.konstueParameters
    konst originalValueParameters = originalFunction.konstueParameters
    for (i in konstueParameters.indices) {
        if (!typeSafeBarrierDescription.checkParameter(i))
            continue

        konst type = originalValueParameters[i].type.erasure()
        // Note: erasing to single type is not entirely correct if type parameter has multiple upper bounds.
        // In this case the compiler could generate multiple type checks, one for each upper bound.
        // But let's keep it simple here for now; JVM backend doesn't do this anyway.

        if (!type.isNullableAny()) {
            // Here, we can't trust konstue parameter type until we check it, because of @UnsafeVariance
            // So we add implicit cast to avoid type check optimization
            +returnIfBadType(irImplicitCast(irGet(konstueParameters[i]), context.irBuiltIns.anyNType), type,
                    if (typeSafeBarrierDescription == SpecialGenericSignatures.TypeSafeBarrierDescription.MAP_GET_OR_DEFAULT)
                        irGet(konstueParameters[2])
                    else irConst(typeSafeBarrierDescription.defaultValue)
            )
        }
    }
}

private fun Context.buildBridge(startOffset: Int, endOffset: Int,
                                overriddenFunction: OverriddenFunctionInfo, targetSymbol: IrSimpleFunctionSymbol,
                                superQualifierSymbol: IrClassSymbol? = null): IrFunction {

    konst bridge = bridgesSupport.getBridge(overriddenFunction)

    if (bridge.modality == Modality.ABSTRACT) {
        return bridge
    }

    konst irBuilder = createIrBuilder(bridge.symbol, startOffset, endOffset)
    bridge.body = irBuilder.irBlockBody(bridge) {
        konst typeSafeBarrierDescription = BuiltinMethodsWithSpecialGenericSignature.getDefaultValueForOverriddenBuiltinFunction(overriddenFunction.overriddenFunction.descriptor)
        typeSafeBarrierDescription?.let { buildTypeSafeBarrier(bridge, overriddenFunction.function, it) }

        konst delegatingCall = IrCallImpl.fromSymbolOwner(
                startOffset,
                endOffset,
                targetSymbol.owner.returnType,
                targetSymbol,
                typeArgumentsCount = targetSymbol.owner.typeParameters.size,
                konstueArgumentsCount = targetSymbol.owner.konstueParameters.size,
                superQualifierSymbol = superQualifierSymbol /* Call non-virtually */
        ).apply {
            bridge.dispatchReceiverParameter?.let {
                dispatchReceiver = irGet(it)
            }
            bridge.extensionReceiverParameter?.let {
                extensionReceiver = irGet(it)
            }
            bridge.konstueParameters.forEachIndexed { index, parameter ->
                this.putValueArgument(index, irGet(parameter))
            }
        }

        +irReturn(delegatingCall)
    }
    return bridge
}
