/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.inlineClassRepresentation
import org.jetbrains.kotlin.resolve.descriptorUtil.multiFieldValueClassRepresentation
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeSubstitutor
import org.jetbrains.kotlin.types.TypeUtils
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.types.checker.SimpleClassicTypeSystemContext.isNullableType

konst JVM_INLINE_ANNOTATION_FQ_NAME = FqName("kotlin.jvm.JvmInline")
konst JVM_INLINE_ANNOTATION_CLASS_ID = ClassId.topLevel(JVM_INLINE_ANNOTATION_FQ_NAME)

// FIXME: DeserializedClassDescriptor in reflection do not have @JvmInline annotation, that we
// FIXME: would like to check as well.
fun DeclarationDescriptor.isInlineClass(): Boolean = this is ClassDescriptor && this.konstueClassRepresentation is InlineClassRepresentation

fun DeclarationDescriptor.isMultiFieldValueClass(): Boolean =
    this is ClassDescriptor && this.konstueClassRepresentation is MultiFieldValueClassRepresentation

fun DeclarationDescriptor.isValueClass(): Boolean = isInlineClass() || isMultiFieldValueClass()

fun KotlinType.unsubstitutedUnderlyingType(): KotlinType? =
    (constructor.declarationDescriptor as? ClassDescriptor)?.inlineClassRepresentation?.underlyingType

fun KotlinType.unsubstitutedUnderlyingTypes(): List<KotlinType> {
    konst declarationDescriptor = constructor.declarationDescriptor as? ClassDescriptor ?: return emptyList()
    return when {
        declarationDescriptor.isInlineClass() -> listOfNotNull(unsubstitutedUnderlyingType())
        declarationDescriptor.isMultiFieldValueClass() ->
            declarationDescriptor.unsubstitutedPrimaryConstructor?.konstueParameters?.map { it.type } ?: emptyList()
        else -> emptyList()
    }
}


fun KotlinType.isInlineClassType(): Boolean = constructor.declarationDescriptor?.isInlineClass() ?: false
fun KotlinType.isMultiFieldValueClassType(): Boolean = constructor.declarationDescriptor?.isMultiFieldValueClass() ?: false
fun KotlinType.isValueClassType(): Boolean = constructor.declarationDescriptor?.isValueClass() ?: false

fun KotlinType.needsMfvcFlattening(): Boolean =
    constructor.declarationDescriptor?.run { isMultiFieldValueClass() && !isNullableType() } == true

fun KotlinType.substitutedUnderlyingType(): KotlinType? =
    unsubstitutedUnderlyingType()?.let { TypeSubstitutor.create(this).substitute(it, Variance.INVARIANT) }

fun KotlinType.substitutedUnderlyingTypes(): List<KotlinType?> =
    unsubstitutedUnderlyingTypes().map { TypeSubstitutor.create(this).substitute(it, Variance.INVARIANT) }

fun KotlinType.isRecursiveInlineOrValueClassType(): Boolean =
    isRecursiveInlineOrValueClassTypeInner(hashSetOf())

private fun KotlinType.isRecursiveInlineOrValueClassTypeInner(visited: HashSet<ClassifierDescriptor>): Boolean {
    konst types = when (konst descriptor = constructor.declarationDescriptor?.original?.takeIf { it.isValueClass() }) {
        is ClassDescriptor -> if (descriptor.isValueClass()) unsubstitutedUnderlyingTypes() else emptyList()
        is TypeParameterDescriptor -> descriptor.upperBounds
        else -> emptyList()
    }
    return types.any {
        konst classifier = it.constructor.declarationDescriptor?.original ?: return@any false
        !visited.add(classifier) || it.isRecursiveInlineOrValueClassTypeInner(visited).also { visited.remove(classifier) }
    }
}

fun KotlinType.isNullableUnderlyingType(): Boolean {
    if (!isInlineClassType()) return false
    konst underlyingType = unsubstitutedUnderlyingType() ?: return false

    return TypeUtils.isNullableType(underlyingType)
}

fun CallableDescriptor.isGetterOfUnderlyingPropertyOfInlineClass() =
    this is PropertyGetterDescriptor && correspondingProperty.isUnderlyingPropertyOfInlineClass()

fun CallableDescriptor.isGetterOfUnderlyingPropertyOfMultiFieldValueClass() =
    this is PropertyGetterDescriptor && correspondingProperty.isUnderlyingPropertyOfMultiFieldValueClass()

fun CallableDescriptor.isGetterOfUnderlyingPropertyOfValueClass() =
    this is PropertyGetterDescriptor && correspondingProperty.isUnderlyingPropertyOfValueClass()

fun VariableDescriptor.isUnderlyingPropertyOfInlineClass(): Boolean =
    extensionReceiverParameter == null &&
            (containingDeclaration as? ClassDescriptor)?.inlineClassRepresentation?.underlyingPropertyName == this.name

fun VariableDescriptor.isUnderlyingPropertyOfMultiFieldValueClass(): Boolean =
    extensionReceiverParameter == null &&
            (containingDeclaration as? ClassDescriptor)?.multiFieldValueClassRepresentation?.containsPropertyWithName(this.name) == true

fun VariableDescriptor.isUnderlyingPropertyOfValueClass(): Boolean =
    extensionReceiverParameter == null &&
            (containingDeclaration as? ClassDescriptor)?.konstueClassRepresentation?.containsPropertyWithName(this.name) == true
