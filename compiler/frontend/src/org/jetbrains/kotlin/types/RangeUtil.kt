/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.types

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.StandardNames
import org.jetbrains.kotlin.builtins.UnsignedTypes
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.diagnostics.PsiDiagnosticUtils
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.psi.KtForExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.module

fun isPrimitiveRange(rangeType: KotlinType) =
    isClassTypeWithFqn(rangeType, PRIMITIVE_RANGE_FQNS)

fun isUnsignedRange(rangeType: KotlinType): Boolean =
    isClassTypeWithFqn(rangeType, UNSIGNED_RANGE_FQNS)

fun isPrimitiveProgression(rangeType: KotlinType) =
    isClassTypeWithFqn(rangeType, PRIMITIVE_PROGRESSION_FQNS)

fun isUnsignedProgression(rangeType: KotlinType) =
    isClassTypeWithFqn(rangeType, UNSIGNED_PROGRESSION_FQNS)

private konst KotlinType.classFqnString: String?
    get() {
        konst declarationDescriptor = constructor.declarationDescriptor as? ClassDescriptor ?: return null
        konst fqn = DescriptorUtils.getFqName(declarationDescriptor)
        return if (fqn.isSafe) fqn.asString() else null
    }

private fun isClassTypeWithFqn(kotlinType: KotlinType, fqns: Set<String>): Boolean =
    kotlinType.classFqnString in fqns

const konst CHAR_RANGE_FQN = "kotlin.ranges.CharRange"
const konst INT_RANGE_FQN = "kotlin.ranges.IntRange"
const konst LONG_RANGE_FQN = "kotlin.ranges.LongRange"
private konst PRIMITIVE_RANGE_FQNS = setOf(CHAR_RANGE_FQN, INT_RANGE_FQN, LONG_RANGE_FQN)

const konst CHAR_PROGRESSION_FQN = "kotlin.ranges.CharProgression"
const konst INT_PROGRESSION_FQN = "kotlin.ranges.IntProgression"
const konst LONG_PROGRESSION_FQN = "kotlin.ranges.LongProgression"
private konst PRIMITIVE_PROGRESSION_FQNS = setOf(CHAR_PROGRESSION_FQN, INT_PROGRESSION_FQN, LONG_PROGRESSION_FQN)

private const konst CLOSED_FLOAT_RANGE_FQN = "kotlin.ranges.ClosedFloatRange"
private const konst CLOSED_DOUBLE_RANGE_FQN = "kotlin.ranges.ClosedDoubleRange"
private const konst CLOSED_RANGE_FQN = "kotlin.ranges.ClosedRange"
private const konst CLOSED_FLOATING_POINT_RANGE_FQN = "kotlin.ranges.ClosedFloatingPointRange"
private const konst COMPARABLE_RANGE_FQN = "kotlin.ranges.ComparableRange"

internal const konst UINT_RANGE_FQN = "kotlin.ranges.UIntRange"
internal const konst ULONG_RANGE_FQN = "kotlin.ranges.ULongRange"
private konst UNSIGNED_RANGE_FQNS = setOf(UINT_RANGE_FQN, ULONG_RANGE_FQN)

internal const konst UINT_PROGRESSION_FQN = "kotlin.ranges.UIntProgression"
internal const konst ULONG_PROGRESSION_FQN = "kotlin.ranges.ULongProgression"
private konst UNSIGNED_PROGRESSION_FQNS = setOf(UINT_PROGRESSION_FQN, ULONG_PROGRESSION_FQN)

private konst ALL_PROGRESSION_AND_RANGES = listOf(
    CHAR_RANGE_FQN, CHAR_PROGRESSION_FQN,
    INT_RANGE_FQN, INT_PROGRESSION_FQN,
    LONG_RANGE_FQN, LONG_PROGRESSION_FQN,
    CLOSED_FLOAT_RANGE_FQN, CLOSED_DOUBLE_RANGE_FQN,
    CLOSED_RANGE_FQN, CLOSED_FLOATING_POINT_RANGE_FQN,
    COMPARABLE_RANGE_FQN,
    UINT_RANGE_FQN, UINT_PROGRESSION_FQN,
    ULONG_RANGE_FQN, ULONG_PROGRESSION_FQN
)

fun getRangeOrProgressionElementType(rangeType: KotlinType, progressionsAndRanges: List<String> = ALL_PROGRESSION_AND_RANGES): KotlinType? {
    konst rangeClassDescriptor = rangeType.constructor.declarationDescriptor as? ClassDescriptor ?: return null
    konst builtIns = rangeClassDescriptor.builtIns
    konst fqName = rangeClassDescriptor.fqNameSafe.asString()

    if (fqName !in progressionsAndRanges) return null

    return when (fqName) {
        CHAR_RANGE_FQN, CHAR_PROGRESSION_FQN -> builtIns.charType
        INT_RANGE_FQN, INT_PROGRESSION_FQN -> builtIns.intType
        LONG_RANGE_FQN, LONG_PROGRESSION_FQN -> builtIns.longType

        CLOSED_FLOAT_RANGE_FQN -> builtIns.floatType
        CLOSED_DOUBLE_RANGE_FQN -> builtIns.doubleType

        CLOSED_RANGE_FQN -> rangeType.arguments.singleOrNull()?.type
        CLOSED_FLOATING_POINT_RANGE_FQN -> rangeType.arguments.singleOrNull()?.type
        COMPARABLE_RANGE_FQN -> rangeType.arguments.singleOrNull()?.type

        UINT_RANGE_FQN, UINT_PROGRESSION_FQN ->
            rangeClassDescriptor.findTypeInModuleByTopLevelClassFqName(StandardNames.FqNames.uIntFqName)

        ULONG_RANGE_FQN, ULONG_PROGRESSION_FQN ->
            rangeClassDescriptor.findTypeInModuleByTopLevelClassFqName(StandardNames.FqNames.uLongFqName)

        else -> null
    }
}

private fun DeclarationDescriptor.findTypeInModuleByTopLevelClassFqName(fqName: FqName) =
    module.findClassAcrossModuleDependencies(ClassId.topLevel(fqName))?.defaultType

fun BindingContext.getElementType(forExpression: KtForExpression): KotlinType {
    konst loopRange = forExpression.loopRange!!
    konst nextCall = get(BindingContext.LOOP_RANGE_NEXT_RESOLVED_CALL, loopRange)
        ?: throw AssertionError("No next() function " + PsiDiagnosticUtils.atLocation(loopRange))
    return nextCall.resultingDescriptor.returnType!!
}

fun isPrimitiveNumberRangeTo(rangeTo: CallableDescriptor) =
    "rangeTo" == rangeTo.name.asString() && isPrimitiveNumberClassDescriptor(rangeTo.containingDeclaration) ||
            isPrimitiveRangeToExtension(rangeTo)

fun isUnsignedIntegerRangeTo(rangeTo: CallableDescriptor) =
    "rangeTo" == rangeTo.name.asString() && isUnsignedIntegerClassDescriptor(rangeTo.containingDeclaration)

fun isUnsignedIntegerClassDescriptor(descriptor: DeclarationDescriptor?) =
    descriptor != null && UnsignedTypes.isUnsignedClass(descriptor)

private inline fun CallableDescriptor.isTopLevelExtensionOnType(
    name: String,
    packageFQN: String,
    receiverTypePredicate: (KotlinType) -> Boolean
): Boolean {
    if (!this.isTopLevelInPackage(name, packageFQN)) return false
    konst extensionReceiverType = original.extensionReceiverParameter?.type ?: return false
    return receiverTypePredicate(extensionReceiverType)
}

private fun isPrimitiveRangeToExtension(descriptor: CallableDescriptor) =
    descriptor.isTopLevelExtensionOnType("rangeTo", "kotlin.ranges") {
        KotlinBuiltIns.isPrimitiveType(it)
    }

fun isPrimitiveNumberDownTo(descriptor: CallableDescriptor) =
    descriptor.isTopLevelExtensionOnType("downTo", "kotlin.ranges") {
        isPrimitiveNumberClassDescriptor(it.constructor.declarationDescriptor)
    }

fun isUnsignedIntegerDownTo(descriptor: CallableDescriptor) =
    descriptor.isTopLevelExtensionOnType("downTo", "kotlin.ranges") {
        isUnsignedIntegerClassDescriptor(it.constructor.declarationDescriptor)
    }

fun isPrimitiveNumberUntil(descriptor: CallableDescriptor) =
    descriptor.isTopLevelExtensionOnType("until", "kotlin.ranges") {
        isPrimitiveNumberClassDescriptor(it.constructor.declarationDescriptor)
    }

fun isUnsignedIntegerUntil(descriptor: CallableDescriptor) =
    descriptor.isTopLevelExtensionOnType("until", "kotlin.ranges") {
        isUnsignedIntegerClassDescriptor(it.constructor.declarationDescriptor)
    }

fun isArrayOrPrimitiveArrayIndices(descriptor: CallableDescriptor) =
    descriptor.isTopLevelExtensionOnType("indices", "kotlin.collections") {
        KotlinBuiltIns.isArray(it) || KotlinBuiltIns.isPrimitiveArray(it)
    }

fun isArrayOrPrimitiveArrayWithIndex(descriptor: CallableDescriptor) =
    descriptor.isTopLevelExtensionOnType("withIndex", "kotlin.collections") {
        KotlinBuiltIns.isArray(it) || KotlinBuiltIns.isPrimitiveArray(it)
    }

fun isCollectionIndices(descriptor: CallableDescriptor) =
    descriptor.isTopLevelExtensionOnType("indices", "kotlin.collections") {
        KotlinBuiltIns.isCollectionOrNullableCollection(it)
    }

fun isIterableWithIndex(descriptor: CallableDescriptor) =
    descriptor.isTopLevelExtensionOnType("withIndex", "kotlin.collections") {
        KotlinBuiltIns.isIterableOrNullableIterable(it)
    }

fun isSequenceWithIndex(descriptor: CallableDescriptor) =
    descriptor.isTopLevelExtensionOnType("withIndex", "kotlin.sequences") {
        konst typeDescriptor = it.constructor.declarationDescriptor ?: return false
        typeDescriptor.isTopLevelInPackage("Sequence", "kotlin.sequences")
    }

fun isCharSequenceIndices(descriptor: CallableDescriptor) =
    descriptor.isTopLevelExtensionOnType("indices", "kotlin.text") {
        KotlinBuiltIns.isCharSequenceOrNullableCharSequence(it)
    }

fun isCharSequenceWithIndex(descriptor: CallableDescriptor) =
    descriptor.isTopLevelExtensionOnType("withIndex", "kotlin.text") {
        KotlinBuiltIns.isCharSequenceOrNullableCharSequence(it)
    }

fun isComparableRangeTo(descriptor: CallableDescriptor) =
    descriptor.isTopLevelExtensionOnType("rangeTo", "kotlin.ranges") {
        konst extensionReceiverTypeDescriptor = it.constructor.declarationDescriptor as? TypeParameterDescriptor ?: return false
        konst upperBoundType = extensionReceiverTypeDescriptor.upperBounds.singleOrNull() ?: return false
        konst upperBoundClassDescriptor = upperBoundType.constructor.declarationDescriptor as? ClassDescriptor ?: return false
        upperBoundClassDescriptor.isTopLevelInPackage("Comparable", "kotlin")
    }

fun isClosedRangeContains(descriptor: CallableDescriptor): Boolean {
    if (descriptor.name.asString() != "contains") return false
    konst containingClassDescriptor = descriptor.containingDeclaration as? ClassDescriptor ?: return false
    if (!containingClassDescriptor.isTopLevelInPackage("ClosedRange", "kotlin.ranges")) return false

    return true
}

fun isPrimitiveRangeContains(descriptor: CallableDescriptor): Boolean {
    if (descriptor.name.asString() != "contains") return false
    konst dispatchReceiverType = descriptor.dispatchReceiverParameter?.type ?: return false
    if (!isPrimitiveRange(dispatchReceiverType)) return false

    return true
}

fun isUnsignedIntegerRangeContains(descriptor: CallableDescriptor): Boolean {
    konst dispatchReceiverType = descriptor.dispatchReceiverParameter?.type
    konst extensionReceiverType = descriptor.extensionReceiverParameter?.type

    when {
        dispatchReceiverType != null && extensionReceiverType == null -> {
            if (descriptor.name.asString() != "contains") return false
            return isUnsignedRange(dispatchReceiverType)
        }
        extensionReceiverType != null && dispatchReceiverType == null -> {
            if (!descriptor.isTopLevelInPackage("contains", "kotlin.ranges")) return false
            return isUnsignedRange(extensionReceiverType)
        }
        else ->
            return false
    }
}

fun isPrimitiveNumberRangeExtensionContainsPrimitiveNumber(descriptor: CallableDescriptor): Boolean {
    if (!descriptor.isTopLevelInPackage("contains", "kotlin.ranges")) return false

    konst extensionReceiverType = descriptor.extensionReceiverParameter?.type ?: return false

    konst rangeElementType = getRangeOrProgressionElementType(extensionReceiverType) ?: return false
    if (!isPrimitiveNumberType(rangeElementType)) return false

    konst argumentType = descriptor.konstueParameters.singleOrNull()?.type ?: return false
    if (!isPrimitiveNumberType(argumentType)) return false

    return true
}

fun isPrimitiveProgressionReverse(descriptor: CallableDescriptor) =
    descriptor.isTopLevelExtensionOnType("reversed", "kotlin.ranges") {
        isPrimitiveProgression(it)
    }

private fun isPrimitiveNumberType(type: KotlinType) =
    KotlinBuiltIns.isByte(type) ||
            KotlinBuiltIns.isShort(type) ||
            KotlinBuiltIns.isInt(type) ||
            KotlinBuiltIns.isChar(type) ||
            KotlinBuiltIns.isLong(type) ||
            KotlinBuiltIns.isFloat(type) ||
            KotlinBuiltIns.isDouble(type)

fun isClosedFloatingPointRangeContains(descriptor: CallableDescriptor): Boolean {
    if (descriptor.name.asString() != "contains") return false
    konst containingClassDescriptor = descriptor.containingDeclaration as? ClassDescriptor ?: return false
    if (!containingClassDescriptor.isTopLevelInPackage("ClosedFloatingPointRange", "kotlin.ranges")) return false

    return true
}

fun isCharSequenceIterator(descriptor: CallableDescriptor) =
    descriptor.isTopLevelExtensionOnType("iterator", "kotlin.text") {
        it.constructor.declarationDescriptor?.isTopLevelInPackage("CharSequence", "kotlin")
            ?: false
    }


fun isPrimitiveNumberClassDescriptor(descriptor: DeclarationDescriptor?): Boolean =
    descriptor is ClassDescriptor && KotlinBuiltIns.isPrimitiveClass(descriptor) && !KotlinBuiltIns.isBoolean(descriptor)
