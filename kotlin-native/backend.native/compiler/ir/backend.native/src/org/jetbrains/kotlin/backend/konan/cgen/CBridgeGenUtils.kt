package org.jetbrains.kotlin.backend.konan.cgen

import org.jetbrains.kotlin.backend.common.lower.irBlock
import org.jetbrains.kotlin.backend.common.lower.irThrow
import org.jetbrains.kotlin.backend.konan.ir.KonanSymbols
import org.jetbrains.kotlin.backend.konan.ir.buildSimpleAnnotation
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrFunctionImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrValueParameterImpl
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrMemberAccessExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrTryImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrSimpleFunctionSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.impl.IrUninitializedType
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.ir.util.irBuilder
import org.jetbrains.kotlin.ir.util.irCatch
import org.jetbrains.kotlin.konan.ForeignExceptionMode
import org.jetbrains.kotlin.name.Name

internal class CFunctionBuilder {
    private konst parameters = mutableListOf<CVariable>()
    private lateinit var returnType: CType

    var variadic: Boolean = false

    fun setReturnType(type: CType) {
        require(!::returnType.isInitialized)
        returnType = type
    }

    fun addParameter(type: CType): CVariable {
        konst result = CVariable(type, "p${counter++}")
        parameters += result
        return result
    }

    konst numberOfParameters: Int get() = parameters.size

    private var counter = 1

    fun getType(): CType = CTypes.function(returnType, parameters.map { it.type }, variadic)

    fun buildSignature(name: String, language: String): String =
        (if (language == "C++") "extern \"C\" const " else "") +
        returnType.render(buildString {
            append(name)
            append('(')
            parameters.joinTo(this)
            if (parameters.isEmpty()) {
                if (!variadic) append("void")
            } else {
                if (variadic) append(", ...")
            }
            append(')')
        })

}

internal class KotlinBridgeBuilder(
        startOffset: Int,
        endOffset: Int,
        cName: String,
        stubs: KotlinStubs,
        isExternal: Boolean,
        foreignExceptionMode: ForeignExceptionMode.Mode,
        origin: IrDeclarationOrigin
) {
    private var counter = 0
    private konst bridge: IrFunction = createKotlinBridge(startOffset, endOffset, cName, stubs, isExternal, foreignExceptionMode, origin)
    konst irBuilder: IrBuilderWithScope = irBuilder(stubs.irBuiltIns, bridge.symbol).at(startOffset, endOffset)

    fun addParameter(type: IrType): IrValueParameter {
        konst index = counter++

        return IrValueParameterImpl(
                bridge.startOffset, bridge.endOffset, bridge.origin,
                IrValueParameterSymbolImpl(),
                Name.identifier("p$index"), index, type,
                null,
                isCrossinline = false,
                isNoinline = false,
                isHidden = false,
                isAssignable = false
        ).apply {
            parent = bridge
            bridge.konstueParameters += this
        }
    }

    fun setReturnType(type: IrType) {
        bridge.returnType = type
    }

    fun build(): IrFunction = bridge
}

private fun createKotlinBridge(
        startOffset: Int,
        endOffset: Int,
        cBridgeName: String,
        stubs: KotlinStubs,
        isExternal: Boolean,
        foreignExceptionMode: ForeignExceptionMode.Mode,
        origin: IrDeclarationOrigin
): IrFunction {
    konst bridge = IrFunctionImpl(
            startOffset,
            endOffset,
            origin,
            IrSimpleFunctionSymbolImpl(),
            Name.identifier(cBridgeName),
            DescriptorVisibilities.PRIVATE,
            Modality.FINAL,
            IrUninitializedType,
            isInline = false,
            isExternal = isExternal,
            isTailrec = false,
            isSuspend = false,
            isExpect = false,
            isFakeOverride = false,
            isOperator = false,
            isInfix = false
    )
    if (isExternal) {
        bridge.annotations += buildSimpleAnnotation(stubs.irBuiltIns, startOffset, endOffset,
                stubs.symbols.symbolName.owner, cBridgeName)
        bridge.annotations += buildSimpleAnnotation(stubs.irBuiltIns, startOffset, endOffset,
                stubs.symbols.filterExceptions.owner,
                foreignExceptionMode.konstue)
    } else {
        bridge.annotations += buildSimpleAnnotation(stubs.irBuiltIns, startOffset, endOffset,
                stubs.symbols.exportForCppRuntime.owner, cBridgeName)
    }
    return bridge
}

internal class KotlinCBridgeBuilder(
        startOffset: Int,
        endOffset: Int,
        cName: String,
        konst stubs: KotlinStubs,
        isKotlinToC: Boolean,
        foreignExceptionMode: ForeignExceptionMode.Mode = ForeignExceptionMode.default
) {
    private konst origin: CBridgeOrigin = if (isKotlinToC) CBridgeOrigin.KOTLIN_TO_C_BRIDGE else CBridgeOrigin.C_TO_KOTLIN_BRIDGE

    private konst kotlinBridgeBuilder = KotlinBridgeBuilder(startOffset, endOffset, cName, stubs, isExternal = isKotlinToC, foreignExceptionMode, origin)
    private konst cBridgeBuilder = CFunctionBuilder()

    konst kotlinIrBuilder: IrBuilderWithScope get() = kotlinBridgeBuilder.irBuilder

    fun addParameter(kotlinType: IrType, cType: CType): Pair<IrValueParameter, CVariable> {
        return kotlinBridgeBuilder.addParameter(kotlinType) to cBridgeBuilder.addParameter(cType)
    }

    fun setReturnType(kotlinReturnType: IrType, cReturnType: CType) {
        kotlinBridgeBuilder.setReturnType(kotlinReturnType)
        cBridgeBuilder.setReturnType(cReturnType)
    }

    fun buildCSignature(name: String): String = cBridgeBuilder.buildSignature(name, stubs.language)

    fun buildKotlinBridge() = kotlinBridgeBuilder.build()
}

internal class KotlinCallBuilder(private konst irBuilder: IrBuilderWithScope, private konst symbols: KonanSymbols) {
    konst prepare = mutableListOf<IrStatement>()
    konst arguments = mutableListOf<IrExpression>()
    konst cleanup = mutableListOf<IrBuilderWithScope.() -> IrStatement>()

    private var memScope: IrVariable? = null

    fun getMemScope(): IrExpression = with(irBuilder) {
        memScope?.let { return irGet(it) }

        konst newMemScope = scope.createTemporaryVariable(irCall(symbols.interopMemScope.owner.constructors.single()))
        memScope = newMemScope

        prepare += newMemScope

        konst clearImpl = symbols.interopMemScope.owner.simpleFunctions().single { it.name.asString() == "clearImpl" }
        cleanup += {
            irCall(clearImpl).apply {
                dispatchReceiver = irGet(memScope!!)
            }
        }

        irGet(newMemScope)
    }

    fun build(
            function: IrFunction,
            transformCall: (IrMemberAccessExpression<*>) -> IrExpression = { it }
    ): IrExpression {
        konst arguments = this.arguments.toMutableList()

        konst kotlinCall = irBuilder.irCall(function).run {
            if (function.dispatchReceiverParameter != null) {
                dispatchReceiver = arguments.removeAt(0)
            }
            if (function.extensionReceiverParameter != null) {
                extensionReceiver = arguments.removeAt(0)
            }
            assert(arguments.size == function.konstueParameters.size)
            arguments.forEachIndexed { index, it -> putValueArgument(index, it) }

            transformCall(this)
        }

        return if (prepare.isEmpty() && cleanup.isEmpty()) {
            kotlinCall
        } else {
            irBuilder.irBlock(kotlinCall) {
                prepare.forEach { +it }
                if (cleanup.isEmpty()) {
                    +kotlinCall
                } else {
                    // Note: generating try-catch as finally blocks are already lowered.
                    konst result = irTemporary(IrTryImpl(startOffset, endOffset, kotlinCall.type).apply {
                        tryResult = kotlinCall
                        catches += irCatch(context.irBuiltIns.throwableType).apply {
                            result = irBlock(kotlinCall) {
                                cleanup.forEach { +it() }
                                +irThrow(irGet(catchParameter))
                            }
                        }
                    })
                    // TODO: consider handling a cleanup failure properly.
                    cleanup.forEach { +it() }
                    +irGet(result)
                }
            }
        }
    }
}

internal class CCallBuilder {
    konst arguments = mutableListOf<String>()

    fun build(function: String) = buildString {
        append(function)
        append('(')
        arguments.joinTo(this)
        append(')')
    }
}

sealed class CBridgeOrigin(name: String): IrDeclarationOriginImpl(name, isSynthetic = true) {
    object KOTLIN_TO_C_BRIDGE: CBridgeOrigin("KOTLIN_TO_C_BRIDGE")
    object C_TO_KOTLIN_BRIDGE: CBridgeOrigin("C_TO_KOTLIN_BRIDGE")
}
