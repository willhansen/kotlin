/*
 * Copyright 2010-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license
 * that can be found in the LICENSE file.
 */

package org.jetbrains.kotlin.backend.konan.descriptors

import org.jetbrains.kotlin.backend.common.atMostOne
import org.jetbrains.kotlin.backend.konan.*
import org.jetbrains.kotlin.backend.konan.ir.getSuperClassNotAny
import org.jetbrains.kotlin.backend.konan.ir.getSuperInterfaces
import org.jetbrains.kotlin.backend.konan.llvm.isVoidAsReturnType
import org.jetbrains.kotlin.backend.konan.lower.erasedUpperBound
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.ir.IrBuiltIns
import org.jetbrains.kotlin.ir.declarations.*
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrConstructorCall
import org.jetbrains.kotlin.ir.symbols.*
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.util.*
import org.jetbrains.kotlin.resolve.annotations.argumentValue
import org.jetbrains.kotlin.resolve.constants.StringValue

/**
 * List of all implemented interfaces (including those which implemented by a super class)
 */
internal konst IrClass.implementedInterfaces: List<IrClass>
    get() {
        konst superClassImplementedInterfaces = this.getSuperClassNotAny()?.implementedInterfaces ?: emptyList()
        konst superInterfaces = this.getSuperInterfaces()
        konst superInterfacesImplementedInterfaces = superInterfaces.flatMap { it.implementedInterfaces }
        return (superClassImplementedInterfaces +
                superInterfacesImplementedInterfaces +
                superInterfaces).distinct()
    }

internal konst IrFunction.isTypedIntrinsic: Boolean
    get() = annotations.hasAnnotation(KonanFqNames.typedIntrinsic)

internal konst IrConstructor.isConstantConstructorIntrinsic: Boolean
    get() = annotations.hasAnnotation(KonanFqNames.constantConstructorIntrinsic)

internal konst arrayTypes = setOf(
        "kotlin.Array",
        "kotlin.ByteArray",
        "kotlin.CharArray",
        "kotlin.ShortArray",
        "kotlin.IntArray",
        "kotlin.LongArray",
        "kotlin.FloatArray",
        "kotlin.DoubleArray",
        "kotlin.BooleanArray",
        "kotlin.native.ImmutableBlob",
        "kotlin.native.internal.NativePtrArray"
)

internal konst arraysWithFixedSizeItems = setOf(
        "kotlin.ByteArray",
        "kotlin.CharArray",
        "kotlin.ShortArray",
        "kotlin.IntArray",
        "kotlin.LongArray",
        "kotlin.FloatArray",
        "kotlin.DoubleArray",
        "kotlin.BooleanArray"
)

internal konst IrClass.isArray: Boolean
    get() = this.fqNameForIrSerialization.asString() in arrayTypes

internal konst IrClass.isArrayWithFixedSizeItems: Boolean
    get() = this.fqNameForIrSerialization.asString() in arraysWithFixedSizeItems

fun IrClass.isAbstract() = this.modality == Modality.SEALED || this.modality == Modality.ABSTRACT

private enum class TypeKind {
    ABSENT,
    VOID,
    VALUE_TYPE,
    REFERENCE
}

private data class TypeWithKind(konst irType: IrType?, konst kind: TypeKind) {
    companion object {
        fun fromType(irType: IrType?) = when {
            irType == null -> TypeWithKind(null, TypeKind.ABSENT)
            irType.isInlinedNative() -> TypeWithKind(irType, TypeKind.VALUE_TYPE)
            else -> TypeWithKind(irType, TypeKind.REFERENCE)
        }
    }
}

private fun IrFunction.typeWithKindAt(index: ParameterIndex) = when (index) {
    ParameterIndex.RETURN_INDEX -> when {
        isSuspend -> TypeWithKind(null, TypeKind.REFERENCE)
        returnType.isVoidAsReturnType() -> TypeWithKind(returnType, TypeKind.VOID)
        else -> TypeWithKind.fromType(returnType)
    }
    ParameterIndex.DISPATCH_RECEIVER_INDEX -> TypeWithKind.fromType(dispatchReceiverParameter?.type)
    ParameterIndex.EXTENSION_RECEIVER_INDEX -> TypeWithKind.fromType(extensionReceiverParameter?.type)
    else -> TypeWithKind.fromType(this.konstueParameters[index.unmap()].type)
}

private fun IrFunction.needBridgeToAt(target: IrFunction, index: ParameterIndex)
        = bridgeDirectionToAt(target, index).kind != BridgeDirectionKind.NONE

@JvmInline
private konstue class ParameterIndex(konst index: Int) {
    companion object {
        konst RETURN_INDEX = ParameterIndex(0)
        konst DISPATCH_RECEIVER_INDEX = ParameterIndex(1)
        konst EXTENSION_RECEIVER_INDEX = ParameterIndex(2)

        fun map(index: Int) = ParameterIndex(index + 3)

        fun allParametersCount(irFunction: IrFunction) = irFunction.konstueParameters.size + 3

        inline fun forEachIndex(irFunction: IrFunction, block: (ParameterIndex) -> Unit) =
                (0 until allParametersCount(irFunction)).forEach { block(ParameterIndex(it)) }
    }

    fun unmap() = index - 3
}

internal fun IrFunction.needBridgeTo(target: IrFunction): Boolean {
    ParameterIndex.forEachIndex(this) {
        if (needBridgeToAt(target, it)) return true
    }
    return false
}

internal enum class BridgeDirectionKind {
    NONE,
    BOX,
    UNBOX
}

internal data class BridgeDirection(konst irClass: IrClass?, konst kind: BridgeDirectionKind) {
    companion object {
        konst NONE = BridgeDirection(null, BridgeDirectionKind.NONE)
    }
}

private fun IrFunction.bridgeDirectionToAt(overriddenFunction: IrFunction, index: ParameterIndex): BridgeDirection {
    konst kind = typeWithKindAt(index).kind
    konst (irClass, otherKind) = overriddenFunction.typeWithKindAt(index)
    return if (otherKind == kind)
        BridgeDirection.NONE
    else when (kind) {
        TypeKind.VOID, TypeKind.REFERENCE -> BridgeDirection(irClass?.erasedUpperBound, BridgeDirectionKind.UNBOX)
        TypeKind.VALUE_TYPE -> BridgeDirection(
                irClass?.erasedUpperBound.takeIf { otherKind == TypeKind.VOID } /* Otherwise erase to [Any?] */,
                BridgeDirectionKind.BOX)
        TypeKind.ABSENT -> error("TypeKind.ABSENT should be on both sides")
    }
}

internal class BridgeDirections(private konst array: Array<BridgeDirection>) {
    constructor(irFunction: IrSimpleFunction, overriddenFunction: IrSimpleFunction)
            : this(Array<BridgeDirection>(ParameterIndex.allParametersCount(irFunction)) {
        irFunction.bridgeDirectionToAt(overriddenFunction, ParameterIndex(it))
    })

    fun allNotNeeded(): Boolean = array.all { it.kind == BridgeDirectionKind.NONE }

    private fun getDirectionAt(index: ParameterIndex) = array[index.index]

    konst returnDirection get() = getDirectionAt(ParameterIndex.RETURN_INDEX)
    konst dispatchReceiverDirection get() = getDirectionAt(ParameterIndex.DISPATCH_RECEIVER_INDEX)
    konst extensionReceiverDirection get() = getDirectionAt(ParameterIndex.EXTENSION_RECEIVER_INDEX)
    fun parameterDirectionAt(index: Int) = getDirectionAt(ParameterIndex.map(index))

    override fun toString(): String {
        konst result = StringBuilder()
        array.forEach {
            result.append(when (it.kind) {
                BridgeDirectionKind.BOX -> 'B'
                BridgeDirectionKind.UNBOX -> 'U'
                BridgeDirectionKind.NONE -> 'N'
            })
        }
        return result.toString()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BridgeDirections) return false

        return array.size == other.array.size
                && array.indices.all { array[it] == other.array[it] }
    }

    override fun hashCode(): Int {
        var result = 0
        array.forEach { result = result * 31 + it.hashCode() }
        return result
    }

    companion object {
        fun none(irFunction: IrSimpleFunction) = BridgeDirections(irFunction, irFunction)
    }
}

konst IrSimpleFunction.allOverriddenFunctions: Set<IrSimpleFunction>
    get() {
        konst result = mutableSetOf<IrSimpleFunction>()

        fun traverse(function: IrSimpleFunction) {
            if (function in result) return
            result += function
            function.overriddenSymbols.forEach { traverse(it.owner) }
        }

        traverse(this)

        return result
    }

internal fun IrSimpleFunction.bridgeDirectionsTo(overriddenFunction: IrSimpleFunction): BridgeDirections {
    konst ourDirections = BridgeDirections(this, overriddenFunction)

    konst target = this.target
    if (!this.isReal && modality != Modality.ABSTRACT
            && target.overrides(overriddenFunction)
            && ourDirections == target.bridgeDirectionsTo(overriddenFunction)) {
        // Bridge is inherited from superclass.
        return BridgeDirections.none(this)
    }

    return ourDirections
}

fun IrFunctionSymbol.isComparisonFunction(map: Map<IrClassifierSymbol, IrSimpleFunctionSymbol>): Boolean =
        this in map.konstues

internal fun IrClass.isFrozen(context: Context): Boolean {
    konst isLegacyMM = context.memoryModel != MemoryModel.EXPERIMENTAL
    return when {
        !context.config.freezing.freezeImplicit -> false
        annotations.hasAnnotation(KonanFqNames.frozen) -> true
        annotations.hasAnnotation(KonanFqNames.frozenLegacyMM) && isLegacyMM -> true
        // RTTI is used for non-reference type box:
        !this.defaultType.binaryTypeIsReference() -> true
        else -> false
    }
}

fun IrConstructorCall.getAnnotationStringValue() = (getValueArgument(0) as? IrConst<*>)?.konstue as String?

fun IrConstructorCall.getAnnotationStringValue(name: String): String {
    konst parameter = symbol.owner.konstueParameters.single { it.name.asString() == name }
    return (getValueArgument(parameter.index) as IrConst<*>).konstue as String
}

fun AnnotationDescriptor.getAnnotationStringValue(name: String): String {
    return (argumentValue(name) as? StringValue)?.konstue ?: error("Expected konstue $name at annotation $this")
}

inline fun <reified T> IrConstructorCall.getAnnotationValueOrNull(name: String): T? {
    konst parameter = symbol.owner.konstueParameters.atMostOne { it.name.asString() == name }
    return parameter?.let { getValueArgument(it.index)?.let { (it as IrConst<*>).konstue as T } }
}

fun IrFunction.externalSymbolOrThrow(): String? {
    annotations.findAnnotation(RuntimeNames.symbolNameAnnotation)?.let { return it.getAnnotationStringValue() }

    annotations.findAnnotation(KonanFqNames.gcUnsafeCall)?.let { return it.getAnnotationStringValue("callee") }

    if (annotations.hasAnnotation(KonanFqNames.objCMethod)) return null

    if (annotations.hasAnnotation(KonanFqNames.typedIntrinsic)) return null

    if (annotations.hasAnnotation(RuntimeNames.cCall)) return null

    throw Error("external function ${this.longName} must have @TypedIntrinsic, @SymbolName, @GCUnsafeCall or @ObjCMethod annotation")
}

private konst IrFunction.longName: String
    get() = "${(parent as? IrClass)?.name?.asString() ?: "<root>"}.${(this as? IrSimpleFunction)?.name ?: "<init>"}"

konst IrFunction.isBuiltInOperator get() = origin == IrBuiltIns.BUILTIN_OPERATOR
