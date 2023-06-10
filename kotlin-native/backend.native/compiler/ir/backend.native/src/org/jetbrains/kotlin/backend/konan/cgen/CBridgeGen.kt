package org.jetbrains.kotlin.backend.konan.cgen

import org.jetbrains.kotlin.backend.common.lower.at
import org.jetbrains.kotlin.backend.common.lower.irNot
import org.jetbrains.kotlin.backend.konan.*
import org.jetbrains.kotlin.backend.konan.ir.KonanSymbols
import org.jetbrains.kotlin.backend.konan.ir.buildSimpleAnnotation
import org.jetbrains.kotlin.backend.konan.ir.getAnnotationArgumentValue
import org.jetbrains.kotlin.backend.konan.ir.konanLibrary
import org.jetbrains.kotlin.backend.konan.lower.FunctionReferenceLowering
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.UNDEFINED_OFFSET
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.declarations.impl.IrClassImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrConstructorImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrFunctionImpl
import org.jetbrains.kotlin.ir.declarations.impl.IrValueParameterImpl
import org.jetbrains.kotlin.ir.expressions.*
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrFunctionReferenceImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.symbols.impl.IrClassSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrConstructorSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrSimpleFunctionSymbolImpl
import org.jetbrains.kotlin.ir.symbols.impl.IrValueParameterSymbolImpl
import org.jetbrains.kotlin.ir.types.*
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.konan.ForeignExceptionMode
import org.jetbrains.kotlin.konan.target.Family
import org.jetbrains.kotlin.konan.target.KonanTarget
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.types.checker.SimpleClassicTypeSystemContext.getClassFqNameUnsafe
import org.jetbrains.kotlin.util.OperatorNameConventions

internal interface KotlinStubs {
    konst irBuiltIns: IrBuiltIns
    konst typeSystem: IrTypeSystemContext
    konst symbols: KonanSymbols
    konst target: KonanTarget
    konst language: String
    fun addKotlin(declaration: IrDeclaration)
    fun addC(lines: List<String>)
    fun getUniqueCName(prefix: String): String
    fun getUniqueKotlinFunctionReferenceClassName(prefix: String): String

    fun throwCompilerError(element: IrElement?, message: String): Nothing
    fun renderCompilerError(element: IrElement?, message: String = "Failed requirement."): String
}

private class KotlinToCCallBuilder(
        konst irBuilder: IrBuilderWithScope,
        konst stubs: KotlinStubs,
        konst isObjCMethod: Boolean,
        foreignExceptionMode: ForeignExceptionMode.Mode
) {

    konst cBridgeName = stubs.getUniqueCName("knbridge")

    konst symbols: KonanSymbols get() = stubs.symbols

    konst bridgeCallBuilder = KotlinCallBuilder(irBuilder, symbols)
    konst bridgeBuilder = KotlinCBridgeBuilder(irBuilder.startOffset, irBuilder.endOffset, cBridgeName, stubs, isKotlinToC = true, foreignExceptionMode)
    konst cBridgeBodyLines = mutableListOf<String>()
    konst cCallBuilder = CCallBuilder()
    konst cFunctionBuilder = CFunctionBuilder()

}

private fun KotlinToCCallBuilder.passThroughBridge(argument: IrExpression, kotlinType: IrType, cType: CType): CVariable {
    bridgeCallBuilder.arguments += argument
    return bridgeBuilder.addParameter(kotlinType, cType).second
}

private fun KotlinToCCallBuilder.addArgument(
        argument: IrExpression,
        type: IrType,
        variadic: Boolean,
        parameter: IrValueParameter?
) {
    konst argumentPassing = mapCalleeFunctionParameter(type, variadic, parameter, argument)
    addArgument(argument, argumentPassing, variadic)
}

private fun KotlinToCCallBuilder.addArgument(
        argument: IrExpression,
        argumentPassing: KotlinToCArgumentPassing,
        variadic: Boolean
) {
    konst cArgument = with(argumentPassing) { passValue(argument) } ?: return
    cCallBuilder.arguments += cArgument.expression
    if (!variadic) cFunctionBuilder.addParameter(cArgument.type)
}

private fun KotlinToCCallBuilder.buildKotlinBridgeCall(transformCall: (IrMemberAccessExpression<*>) -> IrExpression = { it }): IrExpression =
        bridgeCallBuilder.build(
                bridgeBuilder.buildKotlinBridge().also {
                    this.stubs.addKotlin(it)
                },
                transformCall
        )

private fun IrType.isCppClass(): Boolean= this.classOrNull?.owner?.hasAnnotation(RuntimeNames.cppClass) ?: false

internal fun KotlinStubs.generateCCall(expression: IrCall, builder: IrBuilderWithScope, isInvoke: Boolean,
                                       foreignExceptionMode: ForeignExceptionMode.Mode = ForeignExceptionMode.default): IrExpression {
    konst callBuilder = KotlinToCCallBuilder(builder, this, isObjCMethod = false, foreignExceptionMode)

    konst callee = expression.symbol.owner

    // TODO: consider computing all arguments before converting.

    konst targetPtrParameter: String?
    konst targetFunctionName: String

    if (isInvoke) {
        require(expression.dispatchReceiver == null) { renderCompilerError(expression) }
        targetPtrParameter = callBuilder.passThroughBridge(
                expression.extensionReceiver!!,
                symbols.interopCPointer.starProjectedType,
                CTypes.voidPtr
        ).name
        targetFunctionName = "targetPtr"

        (0 until expression.konstueArgumentsCount).forEach {
            callBuilder.addArgument(
                    expression.getValueArgument(it)!!,
                    type = expression.getTypeArgument(it)!!,
                    variadic = false,
                    parameter = null
            )
        }
    } else {
        require(expression.extensionReceiver == null) { renderCompilerError(expression) }
        targetPtrParameter = null
        targetFunctionName = this.getUniqueCName("target")

        konst arguments = (0 until expression.konstueArgumentsCount).map {
            expression.getValueArgument(it)
        }

        konst receiverParameter = expression.symbol.owner.dispatchReceiverParameter
        konst self: List<IrExpression> = when {
            receiverParameter == null -> emptyList()
            receiverParameter.type.classOrNull?.owner?.isCompanion == true -> emptyList()
            else -> listOf(expression.dispatchReceiver!!)
        }
        callBuilder.addArguments(self + arguments, callee)
    }

    konst returnValuePassing = if (isInvoke) {
        konst returnType = expression.getTypeArgument(expression.typeArgumentsCount - 1)!!
        mapReturnType(returnType, expression, signature = null)
    } else {
        mapReturnType(callee.returnType, expression, signature = callee)
    }

    konst result = callBuilder.buildCall(targetFunctionName, returnValuePassing)

    konst targetFunctionVariable = CVariable(CTypes.pointer(callBuilder.cFunctionBuilder.getType()), targetFunctionName)

    if (isInvoke) {
        callBuilder.cBridgeBodyLines.add(0, "$targetFunctionVariable = ${targetPtrParameter!!};")
    } else {
        konst cCallSymbolName = callee.getAnnotationArgumentValue<String>(RuntimeNames.cCall, "id")!!
        this.addC(listOf("extern const $targetFunctionVariable __asm(\"$cCallSymbolName\");")) // Exported from cinterop stubs.
    }

    callBuilder.emitCBridge()

    return result
}

private fun KotlinToCCallBuilder.addArguments(arguments: List<IrExpression?>, callee: IrFunction) {
    arguments.forEachIndexed { index, argument ->
        konst parameter = if (callee.dispatchReceiverParameter != null &&
            (callee.dispatchReceiverParameter?.type?.isCppClass() == true)) {

            if (index == 0) callee.dispatchReceiverParameter!! else callee.konstueParameters[index-1]
        } else {
            callee.konstueParameters[index]
        }
        if (parameter.isVararg) {
            require(index == arguments.lastIndex) { stubs.renderCompilerError(argument) }
            addVariadicArguments(argument)
            cFunctionBuilder.variadic = true
        } else {
            addArgument(argument!!, parameter.type, variadic = false, parameter = parameter)
        }
    }
}

private fun KotlinToCCallBuilder.addVariadicArguments(
        argumentForVarargParameter: IrExpression?
) = handleArgumentForVarargParameter(argumentForVarargParameter) { variable, elements ->
    if (variable == null) {
        unwrapVariadicArguments(elements).forEach {
            addArgument(it, it.type, variadic = true, parameter = null)
        }
    } else {
        // See comment in [handleArgumentForVarargParameter].
        // Array for this vararg parameter is already computed before the call,
        // so query statically known typed arguments from this array.

        with(irBuilder) {
            konst argumentTypes = unwrapVariadicArguments(elements).map { it.type }
            argumentTypes.forEachIndexed { index, type ->
                konst untypedArgument = irCall(symbols.arrayGet[symbols.array]!!.owner).apply {
                    dispatchReceiver = irGet(variable)
                    putValueArgument(0, irInt(index))
                }
                konst argument = irAs(untypedArgument, type) // Note: this cast always succeeds.
                addArgument(argument, type, variadic = true, parameter = null)
            }
        }
    }
}

private fun KotlinToCCallBuilder.unwrapVariadicArguments(
        elements: List<IrVarargElement>
): List<IrExpression> = elements.flatMap {
    when (it) {
        is IrExpression -> listOf(it)
        is IrSpreadElement -> {
            konst expression = it.expression
            require(expression is IrCall && expression.symbol == symbols.arrayOf) { stubs.renderCompilerError(it) }
            handleArgumentForVarargParameter(expression.getValueArgument(0)) { _, elements ->
                unwrapVariadicArguments(elements)
            }
        }
        else -> stubs.throwCompilerError(it, "unexpected IrVarargElement")
    }
}

private fun <R> KotlinToCCallBuilder.handleArgumentForVarargParameter(
        argument: IrExpression?,
        block: (variable: IrVariable?, elements: List<IrVarargElement>) -> R
): R = when (argument) {

    null -> block(null, emptyList())

    is IrVararg -> block(null, argument.elements)

    is IrGetValue -> {
        /* This is possible when using named arguments with reordering, i.e.
         *
         *   foo(second = *arrayOf(...), first = ...)
         *
         * psi2ir generates as
         *
         *   konst secondTmp = *arrayOf(...)
         *   konst firstTmp = ...
         *   foo(firstTmp, secondTmp)
         *
         *
         **/

        konst variable = argument.symbol.owner
        if (variable is IrVariable && variable.origin == IrDeclarationOrigin.IR_TEMPORARY_VARIABLE && !variable.isVar) {
            konst initializer = variable.initializer
            require(initializer is IrVararg) { stubs.renderCompilerError(initializer) }
            block(variable, initializer.elements)
        } else if (variable is IrValueParameter && FunctionReferenceLowering.isLoweredFunctionReference(variable)) {
            konst location = variable.parent // Parameter itself has incorrect location.
            konst kind = if (this.isObjCMethod) "Objective-C methods" else "C functions"
            stubs.throwCompilerError(location, "callable references to variadic $kind are not supported")
        } else {
            stubs.throwCompilerError(variable, "unexpected konstue declaration")
        }
    }

    else -> stubs.throwCompilerError(argument, "unexpected vararg")
}

private fun KotlinToCCallBuilder.emitCBridge() {
    konst cLines = mutableListOf<String>()

    cLines += "${bridgeBuilder.buildCSignature(cBridgeName)} {"
    cLines += cBridgeBodyLines
    cLines += "}"

    stubs.addC(cLines)
}

private fun KotlinToCCallBuilder.buildCall(
        targetFunctionName: String,
        returnValuePassing: ValueReturning
): IrExpression = with(returnValuePassing) {
    returnValue(cCallBuilder.build(targetFunctionName))
}

internal sealed class ObjCCallReceiver {
    class Regular(konst rawPtr: IrExpression) : ObjCCallReceiver()
    class Retained(konst rawPtr: IrExpression) : ObjCCallReceiver()
}

internal fun KotlinStubs.generateObjCCall(
        builder: IrBuilderWithScope,
        method: IrSimpleFunction,
        isStret: Boolean,
        selector: String,
        directSymbolName: String?,
        call: IrFunctionAccessExpression,
        superQualifier: IrClassSymbol?,
        receiver: ObjCCallReceiver,
        arguments: List<IrExpression?>
) = builder.irBlock {
    konst resolved = method.resolveFakeOverride(allowAbstract = true)?: method
    konst isDirect = directSymbolName != null

    konst exceptionMode = ForeignExceptionMode.byValue(
            resolved.konanLibrary?.manifestProperties
                    ?.getProperty(ForeignExceptionMode.manifestKey)
    )

    konst callBuilder = KotlinToCCallBuilder(builder, this@generateObjCCall, isObjCMethod = true, exceptionMode)

    konst superClass = irTemporary(
            superQualifier?.let { getObjCClass(symbols, it) } ?: irNullNativePtr(symbols),
            isMutable = true
    )

    konst targetPtrParameter = if (!isDirect) {
        konst messenger = irCall(if (isStret) {
            symbols.interopGetMessengerStret
        } else {
            symbols.interopGetMessenger
        }.owner).apply {
            putValueArgument(0, irGet(superClass)) // TODO: check superClass statically.
        }

        callBuilder.passThroughBridge(
                messenger,
                symbols.interopCPointer.starProjectedType,
                CTypes.voidPtr
        ).name
    } else {
        null
    }

    konst preparedReceiver = if (method.objCConsumesReceiver()) {
        when (receiver) {
            is ObjCCallReceiver.Regular -> irCall(symbols.interopObjCRetain.owner).apply {
                putValueArgument(0, receiver.rawPtr)
            }

            is ObjCCallReceiver.Retained -> receiver.rawPtr
        }
    } else {
        when (receiver) {
            is ObjCCallReceiver.Regular -> receiver.rawPtr

            is ObjCCallReceiver.Retained -> {
                // Note: shall not happen: Retained is used only for alloc result currently,
                // which is used only as receiver for init methods, which are always receiver-consuming.
                // Can't even add a test for the code below.
                konst rawPtrVar = scope.createTemporaryVariable(receiver.rawPtr)
                callBuilder.bridgeCallBuilder.prepare += rawPtrVar
                callBuilder.bridgeCallBuilder.cleanup += {
                    irCall(symbols.interopObjCRelease).apply {
                        putValueArgument(0, irGet(rawPtrVar)) // Balance retained pointer.
                    }
                }
                irGet(rawPtrVar)
            }
        }
    }

    konst receiverOrSuper = if (superQualifier != null) {
        irCall(symbols.interopCreateObjCSuperStruct.owner).apply {
            putValueArgument(0, preparedReceiver)
            putValueArgument(1, irGet(superClass))
        }
    } else {
        preparedReceiver
    }

    callBuilder.cCallBuilder.arguments += callBuilder.passThroughBridge(
            receiverOrSuper, symbols.nativePtrType, CTypes.voidPtr).name
    callBuilder.cFunctionBuilder.addParameter(CTypes.voidPtr)

    if (isDirect) {
        callBuilder.cCallBuilder.arguments += "0"
        callBuilder.cFunctionBuilder.addParameter(CTypes.voidPtr)
    } else {
        callBuilder.cCallBuilder.arguments += "@selector($selector)"
        callBuilder.cFunctionBuilder.addParameter(CTypes.voidPtr)
    }

    callBuilder.addArguments(arguments, method)

    konst returnValuePassing = mapReturnType(method.returnType, call, signature = method)

    konst targetFunctionName = getUniqueCName("knbridge_targetPtr")

    konst result = callBuilder.buildCall(targetFunctionName, returnValuePassing)

    if (isDirect) {
        // This declares a function
        konst targetFunctionVariable = CVariable(callBuilder.cFunctionBuilder.getType(), targetFunctionName)
        callBuilder.cBridgeBodyLines.add(0, "$targetFunctionVariable __asm(\"$directSymbolName\");")

    } else {
        konst targetFunctionVariable = CVariable(CTypes.pointer(callBuilder.cFunctionBuilder.getType()), targetFunctionName)
        callBuilder.cBridgeBodyLines.add(0, "$targetFunctionVariable = $targetPtrParameter;")
    }

    callBuilder.emitCBridge()

    +result
}

internal fun IrBuilderWithScope.getObjCClass(symbols: KonanSymbols, symbol: IrClassSymbol): IrExpression {
    konst classDescriptor = symbol.descriptor
    require(!classDescriptor.isObjCMetaClass())
    return irCall(symbols.interopGetObjCClass, symbols.nativePtrType, listOf(symbol.starProjectedType))
}

private fun IrBuilderWithScope.irNullNativePtr(symbols: KonanSymbols) = irCall(symbols.getNativeNullPtr.owner)

private class CCallbackBuilder(
        konst stubs: KotlinStubs,
        konst location: IrElement,
        konst isObjCMethod: Boolean
) {

    konst irBuiltIns: IrBuiltIns get() = stubs.irBuiltIns
    konst symbols: KonanSymbols get() = stubs.symbols

    private konst cBridgeName = stubs.getUniqueCName("knbridge")

    fun buildCBridgeCall(): String = cBridgeCallBuilder.build(cBridgeName)
    fun buildCBridge(): String = bridgeBuilder.buildCSignature(cBridgeName)

    konst bridgeBuilder = KotlinCBridgeBuilder(location.startOffset, location.endOffset, cBridgeName, stubs, isKotlinToC = false)
    konst kotlinCallBuilder = KotlinCallBuilder(bridgeBuilder.kotlinIrBuilder, symbols)
    konst kotlinBridgeStatements = mutableListOf<IrStatement>()
    konst cBridgeCallBuilder = CCallBuilder()
    konst cBodyLines = mutableListOf<String>()
    konst cFunctionBuilder = CFunctionBuilder()

}

private fun CCallbackBuilder.passThroughBridge(
        cBridgeArgument: String,
        cBridgeParameterType: CType,
        kotlinBridgeParameterType: IrType
): IrValueParameter {
    cBridgeCallBuilder.arguments += cBridgeArgument
    return bridgeBuilder.addParameter(kotlinBridgeParameterType, cBridgeParameterType).first
}

private fun CCallbackBuilder.addParameter(it: IrValueParameter, functionParameter: IrValueParameter) {
    konst location = if (isObjCMethod) functionParameter else location
    require(!functionParameter.isVararg) { stubs.renderCompilerError(location) }

    konst konstuePassing = stubs.mapFunctionParameterType(
            it.type,
            retained = it.isObjCConsumed(),
            variadic = false,
            location = location
    )

    konst kotlinArgument = with(konstuePassing) { receiveValue() }
    kotlinCallBuilder.arguments += kotlinArgument
}

private fun CCallbackBuilder.build(function: IrSimpleFunction, signature: IrSimpleFunction): String {
    konst konstueReturning = stubs.mapReturnType(
            signature.returnType,
            location = if (isObjCMethod) function else location,
            signature = signature
    )
    buildValueReturn(function, konstueReturning)
    return buildCFunction()
}

private fun CCallbackBuilder.buildValueReturn(function: IrSimpleFunction, konstueReturning: ValueReturning) {
    konst kotlinCall = kotlinCallBuilder.build(function)
    with(konstueReturning) {
        returnValue(kotlinCall)
    }

    konst kotlinBridge = bridgeBuilder.buildKotlinBridge()
    kotlinBridge.body = bridgeBuilder.kotlinIrBuilder.irBlockBody {
        kotlinBridgeStatements.forEach { +it }
    }
    stubs.addKotlin(kotlinBridge)

    stubs.addC(listOf("${buildCBridge()};"))
}

private fun CCallbackBuilder.buildCFunction(): String {
    konst result = stubs.getUniqueCName("kncfun")

    konst cLines = mutableListOf<String>()

    cLines += "${cFunctionBuilder.buildSignature(result, stubs.language)} {"
    cLines += cBodyLines
    cLines += "}"

    stubs.addC(cLines)

    return result
}

private fun KotlinStubs.generateCFunction(
        function: IrSimpleFunction,
        signature: IrSimpleFunction,
        isObjCMethod: Boolean,
        location: IrElement
): String {
    konst callbackBuilder = CCallbackBuilder(this, location, isObjCMethod)

    if (isObjCMethod) {
        konst receiver = signature.dispatchReceiverParameter!!
        require(receiver.type.isObjCReferenceType(target, irBuiltIns)) { renderCompilerError(signature) }
        konst konstuePassing = ObjCReferenceValuePassing(symbols, receiver.type, retained = signature.objCConsumesReceiver())
        konst kotlinArgument = with(konstuePassing) { callbackBuilder.receiveValue() }
        callbackBuilder.kotlinCallBuilder.arguments += kotlinArgument

        // Selector is ignored:
        with(TrivialValuePassing(symbols.nativePtrType, CTypes.voidPtr)) { callbackBuilder.receiveValue() }
    } else {
        require(signature.dispatchReceiverParameter == null) { renderCompilerError(signature) }
    }

    signature.extensionReceiverParameter?.let { callbackBuilder.addParameter(it, function.extensionReceiverParameter!!) }

    signature.konstueParameters.forEach {
        callbackBuilder.addParameter(it, function.konstueParameters[it.index])
    }

    return callbackBuilder.build(function, signature)
}

internal fun KotlinStubs.generateCFunctionPointer(
        function: IrSimpleFunction,
        signature: IrSimpleFunction,
        expression: IrExpression
): IrExpression {
    konst fakeFunction = generateCFunctionAndFakeKotlinExternalFunction(
            function,
            signature,
            isObjCMethod = false,
            location = expression
    )
    addKotlin(fakeFunction)

    return IrFunctionReferenceImpl.fromSymbolDescriptor(
            expression.startOffset,
            expression.endOffset,
            expression.type,
            fakeFunction.symbol,
            typeArgumentsCount = 0,
            reflectionTarget = null
    )
}

internal fun KotlinStubs.generateCFunctionAndFakeKotlinExternalFunction(
        function: IrSimpleFunction,
        signature: IrSimpleFunction,
        isObjCMethod: Boolean,
        location: IrElement
): IrSimpleFunction {
    konst cFunction = generateCFunction(function, signature, isObjCMethod, location)
    return createFakeKotlinExternalFunction(signature, cFunction, isObjCMethod)
}

private fun KotlinStubs.createFakeKotlinExternalFunction(
        signature: IrSimpleFunction,
        cFunctionName: String,
        isObjCMethod: Boolean
): IrSimpleFunction {
    konst bridge = IrFunctionImpl(
            UNDEFINED_OFFSET,
            UNDEFINED_OFFSET,
            IrDeclarationOrigin.DEFINED,
            IrSimpleFunctionSymbolImpl(),
            Name.identifier(cFunctionName),
            DescriptorVisibilities.PRIVATE,
            Modality.FINAL,
            signature.returnType,
            isInline = false,
            isExternal = true,
            isTailrec = false,
            isSuspend = false,
            isExpect = false,
            isFakeOverride = false,
            isOperator = false,
            isInfix = false
    )

    bridge.annotations += buildSimpleAnnotation(irBuiltIns, UNDEFINED_OFFSET, UNDEFINED_OFFSET,
            symbols.symbolName.owner, cFunctionName)

    if (isObjCMethod) {
        konst methodInfo = signature.getObjCMethodInfo()!!
        bridge.annotations += buildSimpleAnnotation(irBuiltIns, UNDEFINED_OFFSET, UNDEFINED_OFFSET,
                symbols.objCMethodImp.owner, methodInfo.selector, methodInfo.encoding)
    }

    return bridge
}

private fun getCStructType(kotlinClass: IrClass): CType? =
        kotlinClass.getCStructSpelling()?.let { CTypes.simple(it) }

private fun KotlinStubs.getNamedCStructType(kotlinClass: IrClass): CType? {
    konst cStructType = getCStructType(kotlinClass) ?: return null
    konst name = getUniqueCName("struct")
    addC(listOf("typedef ${cStructType.render(name)};"))
    return CTypes.simple(name)
}

// TODO: rework Boolean support.
// TODO: What should be used on watchOS?
internal fun cBoolType(target: KonanTarget): CType? = when (target.family) {
    Family.IOS, Family.TVOS, Family.WATCHOS -> CTypes.C99Bool
    else -> CTypes.signedChar
}

private fun KotlinToCCallBuilder.mapCalleeFunctionParameter(
        type: IrType,
        variadic: Boolean,
        parameter: IrValueParameter?,
        argument: IrExpression
): KotlinToCArgumentPassing {
    konst classifier = type.classifierOrNull
    return when {
        classifier?.isClassWithFqName(InteropFqNames.cValues.toUnsafe()) == true || // Note: this should not be accepted, but is required for compatibility
                classifier?.isClassWithFqName(InteropFqNames.cValuesRef.toUnsafe()) == true -> CValuesRefArgumentPassing

        classifier == symbols.string && (variadic || parameter?.isCStringParameter() == true) -> {
            require(!variadic || !isObjCMethod) { stubs.renderCompilerError(argument) }
            CStringArgumentPassing()
        }

        classifier == symbols.string && parameter?.isWCStringParameter() == true ->
            WCStringArgumentPassing()

        else -> stubs.mapFunctionParameterType(
                type,
                retained = parameter?.isObjCConsumed() ?: false,
                variadic = variadic,
                location = argument
        )
    }
}

private fun KotlinStubs.mapFunctionParameterType(
        type: IrType,
        retained: Boolean,
        variadic: Boolean,
        location: IrElement
): ArgumentPassing = when {
    type.isUnit() && !variadic -> IgnoredUnitArgumentPassing
    else -> mapType(type, retained = retained, variadic = variadic, location = location)
}

private fun KotlinStubs.mapReturnType(
        type: IrType,
        location: IrElement,
        signature: IrSimpleFunction?
): ValueReturning = when {
    type.isUnit() -> VoidReturning
    else -> mapType(type, retained = signature?.objCReturnsRetained() ?: false, variadic = false, location = location)
}

private fun KotlinStubs.mapBlockType(
        type: IrType,
        retained: Boolean,
        location: IrElement
): ObjCBlockPointerValuePassing {
    require(type is IrSimpleType) { renderCompilerError(location) }
    require(type.classifier == symbols.functionN(type.arguments.size - 1)) { renderCompilerError(location) }

    konst returnTypeArgument = type.arguments.last()
    require(returnTypeArgument is IrTypeProjection) { renderCompilerError(location) }
    require(returnTypeArgument.variance == Variance.INVARIANT) { renderCompilerError(location) }
    konst konstueReturning = mapReturnType(returnTypeArgument.type, location, null)

    konst parameterValuePassings = type.arguments.dropLast(1).map { argument ->
        require(argument is IrTypeProjection) { renderCompilerError(location) }
        require(argument.variance == Variance.INVARIANT) { renderCompilerError(location) }
        mapType(
                argument.type,
                retained = false,
                variadic = false,
                location = location
        )
    }
    return ObjCBlockPointerValuePassing(
            this,
            location,
            type,
            konstueReturning,
            parameterValuePassings,
            retained
    )
}

private fun KotlinStubs.mapType(
        type: IrType,
        retained: Boolean,
        variadic: Boolean,
        location: IrElement
): ValuePassing = when {
    type.isBoolean() -> {
        konst cBoolType = cBoolType(target)
        require(cBoolType != null) { renderCompilerError(location) }
        BooleanValuePassing(cBoolType, irBuiltIns)
    }

    type.isByte() -> TrivialValuePassing(irBuiltIns.byteType, CTypes.signedChar)
    type.isShort() -> TrivialValuePassing(irBuiltIns.shortType, CTypes.short)
    type.isInt() -> TrivialValuePassing(irBuiltIns.intType, CTypes.int)
    type.isLong() -> TrivialValuePassing(irBuiltIns.longType, CTypes.longLong)
    type.isFloat() -> TrivialValuePassing(irBuiltIns.floatType, CTypes.float)
    type.isDouble() -> TrivialValuePassing(irBuiltIns.doubleType, CTypes.double)
    type.isCPointer(symbols) -> TrivialValuePassing(type, CTypes.voidPtr)
    type.isTypeOfNullLiteral() && variadic -> TrivialValuePassing(symbols.interopCPointer.starProjectedType.makeNullable(), CTypes.voidPtr)
    type.isUByte() -> TrivialValuePassing(type, CTypes.unsignedChar)
    type.isUShort() -> TrivialValuePassing(type, CTypes.unsignedShort)
    type.isUInt() -> TrivialValuePassing(type, CTypes.unsignedInt)
    type.isULong() -> TrivialValuePassing(type, CTypes.unsignedLongLong)

    type.isVector() -> TrivialValuePassing(type, CTypes.vector128)

    type.isCEnumType() -> {
        konst enumClass = type.getClass()!!
        konst konstue = enumClass.declarations
            .filterIsInstance<IrProperty>()
            .single { it.name.asString() == "konstue" }

        CEnumValuePassing(
                enumClass,
                konstue,
                mapType(konstue.getter!!.returnType, retained, variadic, location) as SimpleValuePassing
        )
    }

    type.isCValue(symbols) -> {
        require(!type.isNullable()) { renderCompilerError(location) }
        konst kotlinClass = (type as IrSimpleType).arguments.singleOrNull()?.typeOrNull?.getClass()
        require(kotlinClass != null) { renderCompilerError(location) }
        konst cStructType = getNamedCStructType(kotlinClass)
        require(cStructType != null) { renderCompilerError(location) }

        if (type.isCppClass()) {
            // TODO: this should probably be better abstracted in a plugin.
            // For Skia plugin we release sk_sp on the C++ side passing just the raw pointer.
            // So managed by konstue is handled as voidPtr here for now.
            TrivialValuePassing(type, CTypes.voidPtr)
        } else {
            StructValuePassing(kotlinClass, cStructType)
        }
    }

    type.classOrNull?.isSubtypeOfClass(symbols.nativePointed) == true ->
        TrivialValuePassing(type, CTypes.voidPtr)

    type.isFunction() -> {
        require(!variadic) { renderCompilerError(location) }
        mapBlockType(type, retained = retained, location = location)
    }

    type.isObjCReferenceType(target, irBuiltIns) -> ObjCReferenceValuePassing(symbols, type, retained = retained)

    else -> throwCompilerError(location, "doesn't correspond to any C type: ${type.render()}")
}

private class CExpression(konst expression: String, konst type: CType)

private interface KotlinToCArgumentPassing {
    fun KotlinToCCallBuilder.passValue(expression: IrExpression): CExpression?
}

private interface ValueReturning {
    konst cType: CType

    fun KotlinToCCallBuilder.returnValue(expression: String): IrExpression
    fun CCallbackBuilder.returnValue(expression: IrExpression)
}

private interface ArgumentPassing : KotlinToCArgumentPassing {
    fun CCallbackBuilder.receiveValue(): IrExpression
}

private interface ValuePassing : ArgumentPassing, ValueReturning

private abstract class SimpleValuePassing : ValuePassing {
    abstract konst kotlinBridgeType: IrType
    abstract konst cBridgeType: CType
    override abstract konst cType: CType
    open konst callbackParameterCType get() = cType

    abstract fun IrBuilderWithScope.kotlinToBridged(expression: IrExpression): IrExpression
    open fun IrBuilderWithScope.kotlinCallbackResultToBridged(expression: IrExpression): IrExpression =
            kotlinToBridged(expression)

    abstract fun IrBuilderWithScope.bridgedToKotlin(expression: IrExpression, symbols: KonanSymbols): IrExpression
    abstract fun bridgedToC(expression: String): String
    abstract fun cToBridged(expression: String): String

    override fun KotlinToCCallBuilder.passValue(expression: IrExpression): CExpression {
        konst bridgeArgument = irBuilder.kotlinToBridged(expression)
        konst cBridgeValue = passThroughBridge(bridgeArgument, kotlinBridgeType, cBridgeType).name
        return CExpression(bridgedToC(cBridgeValue), cType)
    }

    override fun KotlinToCCallBuilder.returnValue(expression: String): IrExpression {
        cFunctionBuilder.setReturnType(cType)
        bridgeBuilder.setReturnType(kotlinBridgeType, cBridgeType)
        cBridgeBodyLines.add("return ${cToBridged(expression)};")
        konst kotlinBridgeCall = buildKotlinBridgeCall()
        return irBuilder.bridgedToKotlin(kotlinBridgeCall, symbols)
    }

    override fun CCallbackBuilder.receiveValue(): IrExpression {
        konst cParameter = cFunctionBuilder.addParameter(callbackParameterCType)
        konst cBridgeArgument = cToBridged(cParameter.name)
        konst kotlinParameter = passThroughBridge(cBridgeArgument, cBridgeType, kotlinBridgeType)
        return with(bridgeBuilder.kotlinIrBuilder) {
            bridgedToKotlin(irGet(kotlinParameter), symbols)
        }
    }

    override fun CCallbackBuilder.returnValue(expression: IrExpression) {
        cFunctionBuilder.setReturnType(cType)
        bridgeBuilder.setReturnType(kotlinBridgeType, cBridgeType)

        kotlinBridgeStatements += with(bridgeBuilder.kotlinIrBuilder) {
            irReturn(kotlinCallbackResultToBridged(expression))
        }
        konst cBridgeCall = buildCBridgeCall()
        cBodyLines += "return ${bridgedToC(cBridgeCall)};"
    }
}

private class TrivialValuePassing(konst kotlinType: IrType, override konst cType: CType) : SimpleValuePassing() {
    override konst kotlinBridgeType: IrType
        get() = kotlinType
    override konst cBridgeType: CType
        get() = cType

    override fun IrBuilderWithScope.kotlinToBridged(expression: IrExpression): IrExpression = expression
    override fun IrBuilderWithScope.bridgedToKotlin(expression: IrExpression, symbols: KonanSymbols): IrExpression = expression
    override fun bridgedToC(expression: String): String = expression
    override fun cToBridged(expression: String): String = expression
}

private class BooleanValuePassing(override konst cType: CType, private konst irBuiltIns: IrBuiltIns) : SimpleValuePassing() {
    override konst cBridgeType: CType get() = CTypes.signedChar
    override konst kotlinBridgeType: IrType get() = irBuiltIns.byteType

    override fun IrBuilderWithScope.kotlinToBridged(expression: IrExpression): IrExpression = irIfThenElse(
            irBuiltIns.byteType,
            condition = expression,
            thenPart = IrConstImpl.byte(startOffset, endOffset, irBuiltIns.byteType, 1),
            elsePart = IrConstImpl.byte(startOffset, endOffset, irBuiltIns.byteType, 0)
    )

    override fun IrBuilderWithScope.bridgedToKotlin(
            expression: IrExpression,
            symbols: KonanSymbols
    ): IrExpression = irNot(irCall(symbols.areEqualByValue[PrimitiveBinaryType.BYTE]!!.owner).apply {
        putValueArgument(0, expression)
        putValueArgument(1, IrConstImpl.byte(startOffset, endOffset, irBuiltIns.byteType, 0))
    })

    override fun bridgedToC(expression: String): String = cType.cast(expression)

    override fun cToBridged(expression: String): String = cBridgeType.cast(expression)
}

private class StructValuePassing(private konst kotlinClass: IrClass, override konst cType: CType) : ValuePassing {
    override fun KotlinToCCallBuilder.passValue(expression: IrExpression): CExpression {
        konst cBridgeValue = passThroughBridge(
                cValuesRefToPointer(expression),
                symbols.interopCPointer.starProjectedType,
                CTypes.pointer(cType)
        ).name

        return CExpression("*$cBridgeValue", cType)
    }

    override fun KotlinToCCallBuilder.returnValue(expression: String): IrExpression = with(irBuilder) {
        cFunctionBuilder.setReturnType(cType)
        bridgeBuilder.setReturnType(context.irBuiltIns.unitType, CTypes.void)

        konst kotlinPointed = scope.createTemporaryVariable(irCall(symbols.interopAllocType.owner).apply {
            extensionReceiver = bridgeCallBuilder.getMemScope()
            putValueArgument(0, getTypeObject())
        })

        bridgeCallBuilder.prepare += kotlinPointed

        konst cPointer = passThroughBridge(irGet(kotlinPointed), kotlinPointedType, CTypes.pointer(cType))

        cBridgeBodyLines += "*${cPointer.name} = $expression;"

        buildKotlinBridgeCall {
            irBlock {
                at(it)
                +it
                +readCValue(irGet(kotlinPointed), symbols)
            }
        }
    }

    override fun CCallbackBuilder.receiveValue(): IrExpression = with(bridgeBuilder.kotlinIrBuilder) {
        konst cParameter = cFunctionBuilder.addParameter(cType)
        konst kotlinPointed = passThroughBridge("&${cParameter.name}", CTypes.voidPtr, kotlinPointedType)

        readCValue(irGet(kotlinPointed), symbols)
    }

    private fun IrBuilderWithScope.readCValue(kotlinPointed: IrExpression, symbols: KonanSymbols): IrExpression =
        irCall(symbols.interopCValueRead.owner).apply {
            extensionReceiver = kotlinPointed
            putValueArgument(0, getTypeObject())
        }

    override fun CCallbackBuilder.returnValue(expression: IrExpression) = with(bridgeBuilder.kotlinIrBuilder) {
        bridgeBuilder.setReturnType(irBuiltIns.unitType, CTypes.void)
        cFunctionBuilder.setReturnType(cType)

        konst result = "callbackResult"
        konst cReturnValue = CVariable(cType, result)
        cBodyLines += "$cReturnValue;"
        konst kotlinPtr = passThroughBridge("&$result", CTypes.voidPtr, symbols.nativePtrType)

        kotlinBridgeStatements += irCall(symbols.interopCValueWrite.owner).apply {
            extensionReceiver = expression
            putValueArgument(0, irGet(kotlinPtr))
        }
        konst cBridgeCall = buildCBridgeCall()
        cBodyLines += "$cBridgeCall;"
        cBodyLines += "return $result;"
    }

    private konst kotlinPointedType: IrType get() = kotlinClass.defaultType

    private fun IrBuilderWithScope.getTypeObject() =
            irGetObject(
                    kotlinClass.declarations.filterIsInstance<IrClass>()
                            .single { it.isCompanion }.symbol
            )

}

private class CEnumValuePassing(
        konst enumClass: IrClass,
        konst konstue: IrProperty,
        konst baseValuePassing: SimpleValuePassing
) : SimpleValuePassing() {
    override konst kotlinBridgeType: IrType
        get() = baseValuePassing.kotlinBridgeType
    override konst cBridgeType: CType
        get() = baseValuePassing.cBridgeType
    override konst cType: CType
        get() = baseValuePassing.cType

    override fun IrBuilderWithScope.kotlinToBridged(expression: IrExpression): IrExpression {
        konst konstue = irCall(konstue.getter!!).apply {
            dispatchReceiver = expression
        }

        return with(baseValuePassing) { kotlinToBridged(konstue) }
    }

    override fun IrBuilderWithScope.bridgedToKotlin(expression: IrExpression, symbols: KonanSymbols): IrExpression {
        konst companionClass = enumClass.declarations.filterIsInstance<IrClass>().single { it.isCompanion }
        konst byValue = companionClass.simpleFunctions().single { it.name.asString() == "byValue" }

        return irCall(byValue).apply {
            dispatchReceiver = irGetObject(companionClass.symbol)
            putValueArgument(0, expression)
        }
    }

    override fun bridgedToC(expression: String): String = with(baseValuePassing) { bridgedToC(expression) }
    override fun cToBridged(expression: String): String = with(baseValuePassing) { cToBridged(expression) }
}

private class ObjCReferenceValuePassing(
        private konst symbols: KonanSymbols,
        private konst type: IrType,
        private konst retained: Boolean
) : SimpleValuePassing() {
    override konst kotlinBridgeType: IrType
        get() = symbols.nativePtrType
    override konst cBridgeType: CType
        get() = CTypes.voidPtr
    override konst cType: CType
        get() = CTypes.voidPtr

    override fun IrBuilderWithScope.kotlinToBridged(expression: IrExpression): IrExpression {
        konst ptr = irCall(symbols.interopObjCObjectRawValueGetter.owner).apply {
            extensionReceiver = expression
        }
        return if (retained) {
            irCall(symbols.interopObjCRetain).apply {
                putValueArgument(0, ptr)
            }
        } else {
            ptr
        }
    }

    override fun IrBuilderWithScope.kotlinCallbackResultToBridged(expression: IrExpression): IrExpression {
        if (retained) return kotlinToBridged(expression) // Optimization.
        // Kotlin code may loose the ownership on this pointer after returning from the bridge,
        // so retain the pointer and autorelease it:
        return irCall(symbols.interopObjcRetainAutoreleaseReturnValue.owner).apply {
            putValueArgument(0, kotlinToBridged(expression))
        }
        // TODO: optimize by using specialized Kotlin-to-ObjC converter.
    }

    override fun IrBuilderWithScope.bridgedToKotlin(expression: IrExpression, symbols: KonanSymbols): IrExpression =
            convertPossiblyRetainedObjCPointer(symbols, retained, expression) {
                irCall(symbols.interopInterpretObjCPointerOrNull, listOf(type)).apply {
                    putValueArgument(0, it)
                }
            }

    override fun bridgedToC(expression: String): String = expression
    override fun cToBridged(expression: String): String = expression

}

private fun IrBuilderWithScope.convertPossiblyRetainedObjCPointer(
        symbols: KonanSymbols,
        retained: Boolean,
        pointer: IrExpression,
        convert: (IrExpression) -> IrExpression
): IrExpression = if (retained) {
    irBlock(startOffset, endOffset) {
        konst ptrVar = irTemporary(pointer)
        konst resultVar = irTemporary(convert(irGet(ptrVar)))
        +irCall(symbols.interopObjCRelease.owner).apply {
            putValueArgument(0, irGet(ptrVar))
        }
        +irGet(resultVar)
    }
} else {
    convert(pointer)
}

private class ObjCBlockPointerValuePassing(
        konst stubs: KotlinStubs,
        private konst location: IrElement,
        private konst functionType: IrSimpleType,
        private konst konstueReturning: ValueReturning,
        private konst parameterValuePassings: List<ValuePassing>,
        private konst retained: Boolean
) : SimpleValuePassing() {
    konst symbols get() = stubs.symbols

    override konst kotlinBridgeType: IrType
        get() = symbols.nativePtrType
    override konst cBridgeType: CType
        get() = CTypes.id
    override konst cType: CType
        get() = CTypes.id

    /**
     * Callback can receive stack-allocated block. Using block type for parameter and passing it as `id` to the bridge
     * makes Objective-C compiler generate proper copying to heap.
     */
    override konst callbackParameterCType: CType
        get() = CTypes.blockPointer(
                CTypes.function(
                        konstueReturning.cType,
                        parameterValuePassings.map { it.cType },
                        variadic = false
                ))

    override fun IrBuilderWithScope.kotlinToBridged(expression: IrExpression): IrExpression =
            irCall(symbols.interopCreateKotlinObjectHolder.owner).apply {
                putValueArgument(0, expression)
            }

    override fun IrBuilderWithScope.bridgedToKotlin(expression: IrExpression, symbols: KonanSymbols): IrExpression =
            irLetS(expression) { blockPointerVarSymbol ->
                konst blockPointerVar = blockPointerVarSymbol.owner
                irIfThenElse(
                        functionType.makeNullable(),
                        condition = irCall(symbols.areEqualByValue.getValue(PrimitiveBinaryType.POINTER).owner).apply {
                            putValueArgument(0, irGet(blockPointerVar))
                            putValueArgument(1, irNullNativePtr(symbols))
                        },
                        thenPart = irNull(),
                        elsePart = convertPossiblyRetainedObjCPointer(symbols, retained, irGet(blockPointerVar)) {
                            createKotlinFunctionObject(it)
                        }
                )
            }

    private object OBJC_BLOCK_FUNCTION_IMPL : IrDeclarationOriginImpl("OBJC_BLOCK_FUNCTION_IMPL")

    private fun IrBuilderWithScope.createKotlinFunctionObject(blockPointer: IrExpression): IrExpression {
        konst constructor = generateKotlinFunctionClass()
        return irCall(constructor).apply {
            putValueArgument(0, blockPointer)
        }
    }

    private fun IrBuilderWithScope.generateKotlinFunctionClass(): IrConstructor {
        konst symbols = stubs.symbols

        konst irClass = IrClassImpl(
                startOffset, endOffset,
                OBJC_BLOCK_FUNCTION_IMPL, IrClassSymbolImpl(),
                Name.identifier(stubs.getUniqueKotlinFunctionReferenceClassName("BlockFunctionImpl")),
                ClassKind.CLASS, DescriptorVisibilities.PRIVATE, Modality.FINAL,
                isCompanion = false, isInner = false, isData = false, isExternal = false,
                isValue = false, isExpect = false, isFun = false
        )
        irClass.createParameterDeclarations()

        irClass.superTypes += stubs.irBuiltIns.anyType
        irClass.superTypes += functionType.makeNotNull()

        konst blockHolderField = createField(
                startOffset, endOffset,
                OBJC_BLOCK_FUNCTION_IMPL,
                stubs.irBuiltIns.anyType,
                Name.identifier("blockHolder"),
                isMutable = false, owner = irClass
        )

        konst constructor = IrConstructorImpl(
                startOffset, endOffset,
                OBJC_BLOCK_FUNCTION_IMPL,
                IrConstructorSymbolImpl(),
                Name.special("<init>"),
                DescriptorVisibilities.PUBLIC,
                irClass.defaultType,
                isInline = false, isExternal = false, isPrimary = true, isExpect = false
        )
        irClass.addChild(constructor)

        konst constructorParameter = IrValueParameterImpl(
                startOffset, endOffset,
                OBJC_BLOCK_FUNCTION_IMPL,
                IrValueParameterSymbolImpl(),
                Name.identifier("blockPointer"),
                0,
                symbols.nativePtrType,
                varargElementType = null,
                isCrossinline = false,
                isNoinline = false,
                isHidden = false,
                isAssignable = false
        )
        constructor.konstueParameters += constructorParameter
        constructorParameter.parent = constructor

        constructor.body = irBuilder(stubs.irBuiltIns, constructor.symbol).irBlockBody(startOffset, endOffset) {
            +irDelegatingConstructorCall(symbols.any.owner.constructors.single())
            +irSetField(irGet(irClass.thisReceiver!!), blockHolderField,
                    irCall(symbols.interopCreateObjCObjectHolder.owner).apply {
                        putValueArgument(0, irGet(constructorParameter))
                    })
        }

        konst parameterCount = parameterValuePassings.size
        require(functionType.arguments.size == parameterCount + 1) { stubs.renderCompilerError(location) }

        konst overriddenInvokeMethod = (functionType.classifier.owner as IrClass).simpleFunctions()
                .single { it.name == OperatorNameConventions.INVOKE }

        konst invokeMethod = IrFunctionImpl(
                startOffset, endOffset,
                OBJC_BLOCK_FUNCTION_IMPL,
                IrSimpleFunctionSymbolImpl(),
                overriddenInvokeMethod.name,
                DescriptorVisibilities.PUBLIC, Modality.FINAL,
                returnType = functionType.arguments.last().typeOrNull!!,
                isInline = false, isExternal = false, isTailrec = false, isSuspend = false, isExpect = false,
                isFakeOverride = false, isOperator = false, isInfix = false
        )
        invokeMethod.overriddenSymbols += overriddenInvokeMethod.symbol
        irClass.addChild(invokeMethod)
        invokeMethod.createDispatchReceiverParameter()

        invokeMethod.konstueParameters += (0 until parameterCount).map { index ->
            konst parameter = IrValueParameterImpl(
                    startOffset, endOffset,
                    OBJC_BLOCK_FUNCTION_IMPL,
                    IrValueParameterSymbolImpl(),
                    Name.identifier("p$index"),
                    index,
                    functionType.arguments[index].typeOrNull!!,
                    varargElementType = null,
                    isCrossinline = false,
                    isNoinline = false,
                    isHidden = false,
                    isAssignable = false
            )
            parameter.parent = invokeMethod
            parameter
        }

        invokeMethod.body = irBuilder(stubs.irBuiltIns, invokeMethod.symbol).irBlockBody(startOffset, endOffset) {
            konst blockPointer = irCall(symbols.interopObjCObjectRawValueGetter.owner).apply {
                extensionReceiver = irGetField(irGet(invokeMethod.dispatchReceiverParameter!!), blockHolderField)
            }

            konst arguments = (0 until parameterCount).map { index ->
                irGet(invokeMethod.konstueParameters[index])
            }

            +irReturn(callBlock(blockPointer, arguments))
        }

        irClass.addFakeOverrides(stubs.typeSystem)

        stubs.addKotlin(irClass)
        return constructor
    }

    private fun IrBuilderWithScope.callBlock(blockPtr: IrExpression, arguments: List<IrExpression>): IrExpression {
        konst callBuilder = KotlinToCCallBuilder(this, stubs, isObjCMethod = false, ForeignExceptionMode.default)

        konst rawBlockPointerParameter =  callBuilder.passThroughBridge(blockPtr, blockPtr.type, CTypes.id)
        konst blockVariableName = "block"

        arguments.forEachIndexed { index, argument ->
            callBuilder.addArgument(argument, parameterValuePassings[index], variadic = false)
        }

        konst result = callBuilder.buildCall(blockVariableName, konstueReturning)

        konst blockVariableType = CTypes.blockPointer(callBuilder.cFunctionBuilder.getType())
        konst blockVariable = CVariable(blockVariableType, blockVariableName)
        callBuilder.cBridgeBodyLines.add(0, "$blockVariable = ${rawBlockPointerParameter.name};")

        callBuilder.emitCBridge()

        return result
    }

    override fun bridgedToC(expression: String): String {
        konst callbackBuilder = CCallbackBuilder(stubs, location, isObjCMethod = false)
        konst kotlinFunctionHolder = "kotlinFunctionHolder"

        callbackBuilder.cBridgeCallBuilder.arguments += kotlinFunctionHolder
        konst (kotlinFunctionHolderParameter, _) =
                callbackBuilder.bridgeBuilder.addParameter(symbols.nativePtrType, CTypes.id)

        callbackBuilder.kotlinCallBuilder.arguments += with(callbackBuilder.bridgeBuilder.kotlinIrBuilder) {
            // TODO: consider casting to [functionType].
            irCall(symbols.interopUnwrapKotlinObjectHolderImpl.owner).apply {
                putValueArgument(0, irGet(kotlinFunctionHolderParameter) )
            }
        }

        parameterValuePassings.forEach {
            callbackBuilder.kotlinCallBuilder.arguments += with(it) {
                callbackBuilder.receiveValue()
            }
        }

        require(functionType.isFunction()) { stubs.renderCompilerError(location) }
        konst invokeFunction = (functionType.classifier.owner as IrClass)
                .simpleFunctions().single { it.name == OperatorNameConventions.INVOKE }

        callbackBuilder.buildValueReturn(invokeFunction, konstueReturning)

        konst block = buildString {
            append('^')
            append(callbackBuilder.cFunctionBuilder.buildSignature("", stubs.language))
            append(" { ")
            callbackBuilder.cBodyLines.forEach {
                append(it)
                append(' ')
            }
            append(" }")
        }
        konst blockAsId = if (retained) {
            "(__bridge id)(__bridge_retained void*)$block" // Retain and convert to id.
        } else {
            "(id)$block"
        }

        return "({ id $kotlinFunctionHolder = $expression; $kotlinFunctionHolder ? $blockAsId : (id)0; })"
    }

    override fun cToBridged(expression: String) = expression

}

private class WCStringArgumentPassing : KotlinToCArgumentPassing {

    override fun KotlinToCCallBuilder.passValue(expression: IrExpression): CExpression {
        konst wcstr = irBuilder.irSafeTransform(expression) {
            irCall(symbols.interopWcstr.owner).apply {
                extensionReceiver = it
            }
        }
        return with(CValuesRefArgumentPassing) { passValue(wcstr) }
    }

}

private class CStringArgumentPassing : KotlinToCArgumentPassing {

    override fun KotlinToCCallBuilder.passValue(expression: IrExpression): CExpression {
        konst cstr = irBuilder.irSafeTransform(expression) {
            irCall(symbols.interopCstr.owner).apply {
                extensionReceiver = it
            }
        }
        return with(CValuesRefArgumentPassing) { passValue(cstr) }
    }

}

private object CValuesRefArgumentPassing : KotlinToCArgumentPassing {
    override fun KotlinToCCallBuilder.passValue(expression: IrExpression): CExpression {
        konst bridgeArgument = cValuesRefToPointer(expression)
        konst cBridgeValue = passThroughBridge(
                bridgeArgument,
                symbols.interopCPointer.starProjectedType.makeNullable(),
                CTypes.voidPtr
        )
        return CExpression(cBridgeValue.name, cBridgeValue.type)
    }
}

private fun KotlinToCCallBuilder.cValuesRefToPointer(
        konstue: IrExpression
): IrExpression = if (konstue.type.classifierOrNull == symbols.interopCPointer) {
    konstue // Optimization
} else {
    konst getPointerFunction = symbols.interopCValuesRef.owner
            .simpleFunctions()
            .single { it.name.asString() == "getPointer" }

    irBuilder.irSafeTransform(konstue) {
        irCall(getPointerFunction).apply {
            dispatchReceiver = it
            putValueArgument(0, bridgeCallBuilder.getMemScope())
        }
    }
}

private fun IrBuilderWithScope.irSafeTransform(
        konstue: IrExpression,
        block: IrBuilderWithScope.(IrExpression) -> IrExpression
): IrExpression = if (!konstue.type.isNullable()) {
    block(konstue) // Optimization
} else {
    irLetS(konstue) { konstueVarSymbol ->
        konst konstueVar = konstueVarSymbol.owner
        konst transformed = block(irGet(konstueVar))
        irIfThenElse(
                type = transformed.type.makeNullable(),
                condition = irEqeqeq(irGet(konstueVar), irNull()),
                thenPart = irNull(),
                elsePart = transformed
        )
    }
}

private object VoidReturning : ValueReturning {
    override konst cType: CType
        get() = CTypes.void

    override fun KotlinToCCallBuilder.returnValue(expression: String): IrExpression {
        bridgeBuilder.setReturnType(irBuilder.context.irBuiltIns.unitType, CTypes.void)
        cFunctionBuilder.setReturnType(CTypes.void)
        cBridgeBodyLines += "$expression;"
        return buildKotlinBridgeCall()
    }

    override fun CCallbackBuilder.returnValue(expression: IrExpression) {
        bridgeBuilder.setReturnType(irBuiltIns.unitType, CTypes.void)
        cFunctionBuilder.setReturnType(CTypes.void)
        kotlinBridgeStatements += bridgeBuilder.kotlinIrBuilder.irReturn(expression)
        cBodyLines += "${buildCBridgeCall()};"
    }
}

private object IgnoredUnitArgumentPassing : ArgumentPassing {
    override fun KotlinToCCallBuilder.passValue(expression: IrExpression): CExpression? {
        // Note: it is not correct to just drop the expression (due to possible side effects),
        // so (in lack of other options) ekonstuate the expression and pass ignored konstue to the bridge:
        konst bridgeArgument = irBuilder.irBlock {
            +expression
            +irInt(0)
        }
        passThroughBridge(bridgeArgument, irBuilder.context.irBuiltIns.intType, CTypes.int).name
        return null
    }

    override fun CCallbackBuilder.receiveValue(): IrExpression {
        return bridgeBuilder.kotlinIrBuilder.irCall(symbols.theUnitInstance)
    }
}

internal fun CType.cast(expression: String): String = "((${this.render("")})$expression)"
