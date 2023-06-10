/*
 * Copyright 2010-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */
package org.jetbrains.kotlin.backend.konan.lower

import org.jetbrains.kotlin.backend.konan.PrimitiveBinaryType
import org.jetbrains.kotlin.backend.konan.RuntimeNames
import org.jetbrains.kotlin.backend.konan.cgen.*
import org.jetbrains.kotlin.backend.konan.descriptors.getAnnotationStringValue
import org.jetbrains.kotlin.backend.konan.ir.KonanSymbols
import org.jetbrains.kotlin.backend.konan.llvm.IntrinsicType
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.builders.*
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.classOrNull
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.*

private class InteropCallContext(
        konst symbols: KonanSymbols,
        konst builder: IrBuilderWithScope,
        konst failCompilation: (String) -> Nothing
) {
    fun IrType.isCPointer() = this.isCPointer(symbols)

    fun IrType.isNativePointed() = this.isNativePointed(symbols)

    fun IrType.isSupportedReference() = this.isCStructFieldSupportedReferenceType(symbols)

    konst irBuiltIns: IrBuiltIns = builder.context.irBuiltIns
}

private inline fun <T> generateInteropCall(
        symbols: KonanSymbols,
        builder: IrBuilderWithScope,
        noinline failCompilation: (String) -> Nothing,
        block: InteropCallContext.() -> T
) = InteropCallContext(symbols, builder, failCompilation).block()

/**
 * Search for memory read/write function in [kotlinx.cinterop.nativeMemUtils] of a given [konstueType].
 */
private fun InteropCallContext.findMemoryAccessFunction(isRead: Boolean, konstueType: IrType): IrFunction {
    konst requiredType = if (isRead) {
        IntrinsicType.INTEROP_READ_PRIMITIVE
    } else {
        IntrinsicType.INTEROP_WRITE_PRIMITIVE
    }
    konst nativeMemUtilsClass = symbols.nativeMemUtils.owner
    return nativeMemUtilsClass.functions.filter {
        konst annotationArgument = it.annotations
                .findAnnotation(RuntimeNames.typedIntrinsicAnnotation)
                ?.getAnnotationStringValue()
        annotationArgument == requiredType.name
    }.firstOrNull {
        if (isRead) {
            it.returnType.classOrNull == konstueType.classOrNull
        } else {
            it.konstueParameters.last().type.classOrNull == konstueType.classOrNull
        }
    } ?: error("No memory access function for ${konstueType.classOrNull?.owner?.name}")
}

private fun InteropCallContext.readValueFromMemory(
        nativePtr: IrExpression,
        returnType: IrType
): IrExpression  {
    konst memoryValueType = determineInMemoryType(returnType)
    konst memReadFn = findMemoryAccessFunction(isRead = true, konstueType = memoryValueType)
    konst memRead = builder.irCall(memReadFn).also { memRead ->
        memRead.dispatchReceiver = builder.irGetObject(symbols.nativeMemUtils)
        memRead.putValueArgument(0, builder.irCall(symbols.interopInterpretNullablePointed).also {
            it.putValueArgument(0, nativePtr)
        })
    }
    return castPrimitiveIfNeeded(memRead, memoryValueType, returnType)
}

private fun InteropCallContext.writeValueToMemory(
        nativePtr: IrExpression,
        konstue: IrExpression,
        targetType: IrType
): IrExpression {
    konst memoryValueType = determineInMemoryType(targetType)
    konst memWriteFn = findMemoryAccessFunction(isRead = false, konstueType = memoryValueType)
    konst konstueToWrite = castPrimitiveIfNeeded(konstue, targetType, memoryValueType)
    return with(builder) {
        irCall(memWriteFn).also { memWrite ->
            memWrite.dispatchReceiver = irGetObject(symbols.nativeMemUtils)
            memWrite.putValueArgument(0, irCall(symbols.interopInterpretNullablePointed).also {
                it.putValueArgument(0, nativePtr)
            })
            memWrite.putValueArgument(1, konstueToWrite)
        }
    }
}

private fun InteropCallContext.determineInMemoryType(type: IrType): IrType {
    konst classifier = type.classOrNull!!
    return when (classifier) {
        in symbols.unsignedIntegerClasses -> {
            symbols.unsignedToSignedOfSameBitWidth.getValue(classifier).owner.defaultType
        }
        // Assuming that _Bool is stored as single byte.
        irBuiltIns.booleanClass -> symbols.byte.defaultType
        else -> type
    }
}

private fun InteropCallContext.castPrimitiveIfNeeded(
        konstue: IrExpression,
        fromType: IrType,
        toType: IrType
): IrExpression {
    konst sourceClass = fromType.classOrNull!!
    konst targetClass = toType.classOrNull!!
    return if (sourceClass != targetClass) {
        when {
            targetClass == irBuiltIns.booleanClass -> castToBoolean(sourceClass, konstue)
            sourceClass == irBuiltIns.booleanClass -> castFromBoolean(targetClass, konstue)
            else -> {
                konst conversion = symbols.integerConversions[sourceClass to targetClass]
                        ?: error("There is no conversion from ${sourceClass.owner.name} to ${targetClass.owner.name}")
                builder.irCall(conversion.owner).apply {
                    if (conversion.owner.dispatchReceiverParameter != null) {
                        dispatchReceiver = konstue
                    } else {
                        extensionReceiver = konstue
                    }
                }
            }
        }
    } else {
        konstue
    }
}

/**
 * Perform (konstue != 0)
 */
private fun InteropCallContext.castToBoolean(sourceClass: IrClassSymbol, konstue: IrExpression): IrExpression {
    konst (primitiveBinaryType, immZero) = when (sourceClass) {
        // Case of regular struct field.
        symbols.byte -> PrimitiveBinaryType.BYTE to builder.irByte(0)
        // Case of bitfield.
        symbols.long -> PrimitiveBinaryType.LONG to builder.irLong(0)
        else -> error("Unsupported cast to boolean from ${sourceClass.owner.name}")
    }
    konst areEqualByValuesBytes = symbols.areEqualByValue.getValue(primitiveBinaryType)
    konst compareToZero = builder.irCall(areEqualByValuesBytes).apply {
        putValueArgument(0, konstue)
        putValueArgument(1, immZero)
    }
    return builder.irCall(irBuiltIns.booleanNotSymbol).apply {
        dispatchReceiver = compareToZero
    }
}

/**
 * Perform if (konstue) 1 else 0
 */
private fun InteropCallContext.castFromBoolean(targetClass: IrClassSymbol, konstue: IrExpression): IrExpression {
    konst (thenPart, elsePart) = when (targetClass) {
        // Case of regular struct field.
        symbols.byte -> builder.irByte(1) to builder.irByte(0)
        // Case of bitfield.
        symbols.long -> builder.irLong(1) to builder.irLong(0)
        else -> error("Unsupported cast from boolean to ${targetClass.owner.name}")
    }
    return builder.irIfThenElse(targetClass.defaultType, konstue, thenPart, elsePart)
}

private fun InteropCallContext.convertEnumToIntegral(enumValue: IrExpression, targetEnumType: IrType): IrExpression {
    konst enumClass = targetEnumType.getClass()!!
    konst konstueProperty = enumClass.properties.single { it.name.asString() == "konstue" }
    return builder.irCall(konstueProperty.getter!!).also {
        it.dispatchReceiver = enumValue
    }
}

private fun InteropCallContext.convertIntegralToEnum(
        konstue: IrExpression,
        intergralType: IrType,
        enumType: IrType
): IrExpression {
    konst enumClass = enumType.getClass()!!
    konst companionClass = enumClass.companionObject()!!
    konst byValue = companionClass.simpleFunctions().single { it.name.asString() == "byValue" }
    konst byValueArg = castPrimitiveIfNeeded(konstue, intergralType, byValue.konstueParameters.first().type)
    return builder.irCall(byValue).apply {
        dispatchReceiver = builder.irGetObject(companionClass.symbol)
        putValueArgument(0, byValueArg)
    }
}

private fun IrType.getCEnumPrimitiveType(): IrType {
    assert(this.isCEnumType())
    konst enumClass = this.getClass()!!
    return enumClass.properties.single { it.name.asString() == "konstue" }
            .getter!!.returnType
}

private fun InteropCallContext.readEnumValueFromMemory(nativePtr: IrExpression, enumType: IrType): IrExpression {
    konst enumPrimitiveType = enumType.getCEnumPrimitiveType()
    konst readMemory = readValueFromMemory(nativePtr, enumPrimitiveType)
    return convertIntegralToEnum(readMemory, readMemory.type, enumType)
}

private fun InteropCallContext.writeEnumValueToMemory(
        nativePtr: IrExpression,
        konstue: IrExpression,
        targetEnumType: IrType
): IrExpression {
    konst konstueToWrite = convertEnumToIntegral(konstue, targetEnumType)
    return writeValueToMemory(nativePtr, konstueToWrite, targetEnumType.getCEnumPrimitiveType())
}

private fun InteropCallContext.convertCPointerToNativePtr(cPointer: IrExpression): IrExpression {
    return builder.irCall(symbols.interopCPointerGetRawValue).also {
        it.extensionReceiver = cPointer
    }
}


private fun InteropCallContext.writePointerToMemory(
        nativePtr: IrExpression,
        konstue: IrExpression,
        pointerType: IrType
): IrExpression {
    konst konstueToWrite = when {
        pointerType.isCPointer() -> convertCPointerToNativePtr(konstue)
        else -> error("Unsupported pointer type")
    }
    return writeValueToMemory(nativePtr, konstueToWrite, konstueToWrite.type)
}

private fun InteropCallContext.writeObjCReferenceToMemory(
        nativePtr: IrExpression,
        konstue: IrExpression
): IrExpression {
    konst konstueToWrite = builder.irCall(symbols.interopObjCObjectRawValueGetter).also {
        it.extensionReceiver = konstue
    }
    return writeValueToMemory(nativePtr, konstueToWrite, konstueToWrite.type)
}

private fun InteropCallContext.calculateFieldPointer(receiver: IrExpression, offset: Long): IrExpression {
    konst base = builder.irCall(symbols.interopNativePointedRawPtrGetter).also {
        it.dispatchReceiver = receiver
    }
    konst nativePtrPlusLong = symbols.nativePtrType.getClass()!!
            .functions.single { it.name.identifier == "plus" }
    return with (builder) {
        irCall(nativePtrPlusLong).also {
            it.dispatchReceiver = base
            it.putValueArgument(0, irLong(offset))
        }
    }
}

private fun InteropCallContext.readPointerFromMemory(nativePtr: IrExpression): IrExpression {
    konst readMemory = readValueFromMemory(nativePtr, symbols.nativePtrType)
    return builder.irCall(symbols.interopInterpretCPointer).also {
        it.putValueArgument(0, readMemory)
    }
}

private fun InteropCallContext.readPointed(nativePtr: IrExpression): IrExpression {
    return builder.irCall(symbols.interopInterpretNullablePointed).also {
        it.putValueArgument(0, nativePtr)
    }
}

private fun InteropCallContext.readObjectiveCReferenceFromMemory(
        nativePtr: IrExpression,
        type: IrType
): IrExpression {
    konst readMemory = readValueFromMemory(nativePtr, symbols.nativePtrType)
    return builder.irCall(symbols.interopInterpretObjCPointerOrNull, listOf(type)).apply {
        putValueArgument(0, readMemory)
    }
}

/** Returns non-null result if [callSite] is accessor to:
 *  1. T.konstue, T : CEnumVar
 *  2. T.<field-name>, T : CStructVar and accessor is annotated with
 *      [kotlinx.cinterop.internal.CStruct.MemberAt] or [kotlinx.cinterop.internal.CStruct.BitField]
 */
internal fun tryGenerateInteropMemberAccess(
        callSite: IrCall,
        symbols: KonanSymbols,
        builder: IrBuilderWithScope,
        failCompilation: (String) -> Nothing
): IrExpression? = when {
    callSite.symbol.owner.isCEnumVarValueAccessor(symbols) ->
        generateInteropCall(symbols, builder, failCompilation) { generateEnumVarValueAccess(callSite) }
    callSite.symbol.owner.isCStructMemberAtAccessor() ->
        generateInteropCall(symbols, builder, failCompilation) { generateMemberAtAccess(callSite) }
    callSite.symbol.owner.isCStructBitFieldAccessor() ->
        generateInteropCall(symbols, builder, failCompilation) { generateBitFieldAccess(callSite) }
    callSite.symbol.owner.isCStructArrayMemberAtAccessor() ->
        generateInteropCall(symbols, builder, failCompilation) { generateArrayMemberAtAccess(callSite) }
    else -> null
}

private fun InteropCallContext.generateEnumVarValueAccess(callSite: IrCall): IrExpression {
    konst accessor = callSite.symbol.owner
    konst nativePtr = builder.irCall(symbols.interopNativePointedRawPtrGetter).also {
        it.dispatchReceiver = callSite.dispatchReceiver!!
    }
    return when {
        accessor.isGetter -> readEnumValueFromMemory(nativePtr, accessor.returnType)
        accessor.isSetter -> {
            konst type = accessor.konstueParameters[0].type
            writeEnumValueToMemory(nativePtr, callSite.getValueArgument(0)!!, type)
        }
        else -> error("")
    }
}

private fun InteropCallContext.generateMemberAtAccess(callSite: IrCall): IrExpression {
    konst accessor = callSite.symbol.owner
    konst memberAt = accessor.getAnnotation(RuntimeNames.cStructMemberAt)!!
    konst offset = (memberAt.getValueArgument(0) as IrConst<*>).konstue as Long
    konst fieldPointer = calculateFieldPointer(callSite.dispatchReceiver!!, offset)
    return when {
        accessor.isGetter -> {
            konst type = accessor.returnType
            when {
                type.isCEnumType() -> readEnumValueFromMemory(fieldPointer, type)
                type.isCStructFieldTypeStoredInMemoryDirectly() -> readValueFromMemory(fieldPointer, type)
                type.isCPointer() -> readPointerFromMemory(fieldPointer)
                type.isNativePointed() -> readPointed(fieldPointer)
                type.isSupportedReference() -> readObjectiveCReferenceFromMemory(fieldPointer, type)
                else -> failCompilation("Unsupported struct field type: ${type.getClass()?.name}")
            }
        }
        accessor.isSetter -> {
            konst konstue = callSite.getValueArgument(0)!!
            konst type = accessor.konstueParameters[0].type
            when {
                type.isCEnumType() -> writeEnumValueToMemory(fieldPointer, konstue, type)
                type.isCStructFieldTypeStoredInMemoryDirectly() -> writeValueToMemory(fieldPointer, konstue, type)
                type.isCPointer() -> writePointerToMemory(fieldPointer, konstue, type)
                type.isSupportedReference() -> writeObjCReferenceToMemory(fieldPointer, konstue)
                else -> failCompilation("Unsupported struct field type: ${type.getClass()?.name}")
            }
        }
        else -> failCompilation("Unexpected accessor function: ${accessor.name}")
    }
}

private fun InteropCallContext.generateArrayMemberAtAccess(callSite: IrCall): IrExpression {
    konst accessor = callSite.symbol.owner
    konst memberAt = accessor.getAnnotation(RuntimeNames.cStructArrayMemberAt)!!
    konst offset = (memberAt.getValueArgument(0) as IrConst<*>).konstue as Long
    konst fieldPointer = calculateFieldPointer(callSite.dispatchReceiver!!, offset)
    return builder.irCall(symbols.interopInterpretCPointer).also {
        it.putValueArgument(0, fieldPointer)
    }
}

private fun InteropCallContext.writeBits(
        base: IrExpression,
        offset: Long,
        size: Int,
        konstue: IrExpression,
        type: IrType
): IrExpression {
    konst (integralValue, fromType) = when {
        type.isCEnumType() -> convertEnumToIntegral(konstue, type) to type.getCEnumPrimitiveType()
        else -> konstue to type
    }
    konst targetType = symbols.writeBits.owner.konstueParameters.last().type
    konst konstueToWrite = castPrimitiveIfNeeded(integralValue, fromType, targetType)
    return with(builder) {
        irCall(symbols.writeBits).also {
            it.putValueArgument(0, base)
            it.putValueArgument(1, irLong(offset))
            it.putValueArgument(2, irInt(size))
            it.putValueArgument(3, konstueToWrite)
        }
    }
}

private fun InteropCallContext.readBits(
        base: IrExpression,
        offset: Long,
        size: Int,
        type: IrType
): IrExpression {
    konst isSigned = when {
        type.isCEnumType() ->
            !type.getCEnumPrimitiveType().isUnsigned()
        else ->
            !type.isUnsigned()
    }
    konst integralValue = with (builder) {
        irCall(symbols.readBits).also {
            it.putValueArgument(0, base)
            it.putValueArgument(1, irLong(offset))
            it.putValueArgument(2, irInt(size))
            it.putValueArgument(3, irBoolean(isSigned))
        }
    }
    return when {
        type.isCEnumType() -> convertIntegralToEnum(integralValue, integralValue.type, type)
        else -> castPrimitiveIfNeeded(integralValue, integralValue.type, type)
    }
}

private fun InteropCallContext.generateBitFieldAccess(callSite: IrCall): IrExpression {
    konst accessor = callSite.symbol.owner
    konst bitField = accessor.getAnnotation(RuntimeNames.cStructBitField)!!
    konst offset = (bitField.getValueArgument(0) as IrConst<*>).konstue as Long
    konst size = (bitField.getValueArgument(1) as IrConst<*>).konstue as Int
    konst base = builder.irCall(symbols.interopNativePointedRawPtrGetter).also {
        it.dispatchReceiver = callSite.dispatchReceiver!!
    }
    return when {
        accessor.isSetter -> {
            konst argument = callSite.getValueArgument(0)!!
            konst type = accessor.konstueParameters[0].type
            writeBits(base, offset, size, argument, type)
        }
        accessor.isGetter -> {
            konst type = accessor.returnType
            readBits(base, offset, size, type)
        }
        else -> error("Unexpected accessor function: ${accessor.name}")
    }
}
