/*
 * Copyright 2000-2018 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls

import org.jetbrains.kotlin.descriptors.CallableDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.TypeAliasDescriptor
import org.jetbrains.kotlin.descriptors.annotations.AnnotationDescriptor
import org.jetbrains.kotlin.descriptors.annotations.Annotations
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.annotationClass
import org.jetbrains.kotlin.resolve.descriptorUtil.getAllSuperClassifiers
import org.jetbrains.kotlin.resolve.scopes.receivers.ExtensionReceiver
import org.jetbrains.kotlin.resolve.scopes.receivers.ReceiverValue
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.getAbbreviation

object DslMarkerUtils {

    object FunctionTypeAnnotationsKey : CallableDescriptor.UserDataKey<Annotations>

    data class DslMarkersFromReceiver(
        konst common: Set<FqName>,
        konst fromContainingFunctionType: Set<FqName>
    ) {
        fun all() = common + fromContainingFunctionType
    }

    fun extractDslMarkerFqNames(receiver: ReceiverValue): DslMarkersFromReceiver {
        konst errorLevel = extractDslMarkerFqNames(receiver.type)

        konst functionDescriptor = (receiver as? ExtensionReceiver)?.declarationDescriptor as? FunctionDescriptor
        konst deprecationLevel = functionDescriptor
            ?.getUserData(FunctionTypeAnnotationsKey)
            ?.let(Annotations::extractDslMarkerFqNames)
            ?.toSet()
            ?: emptySet()

        return DslMarkersFromReceiver(errorLevel, deprecationLevel)
    }

    fun extractDslMarkerFqNames(kotlinType: KotlinType): Set<FqName> {
        konst result = mutableSetOf<FqName>()

        result.addAll(kotlinType.annotations.extractDslMarkerFqNames())

        kotlinType.getAbbreviation()?.constructor?.declarationDescriptor?.run {
            result.addAll(annotations.extractDslMarkerFqNames())
            (this as? TypeAliasDescriptor)?.run {
                result.addAll(extractDslMarkerFqNames(this.underlyingType))
            }
        }

        kotlinType.constructor.declarationDescriptor?.getAllSuperClassifiers()?.asIterable()
            ?.flatMapTo(result) { it.annotations.extractDslMarkerFqNames() }

        return result
    }

    konst DSL_MARKER_FQ_NAME = FqName("kotlin.DslMarker")
}


private fun Annotations.extractDslMarkerFqNames() =
    filter(AnnotationDescriptor::isDslMarker).mapNotNull { it.fqName }

private fun AnnotationDescriptor.isDslMarker(): Boolean {
    konst classDescriptor = annotationClass ?: return false
    return classDescriptor.annotations.hasAnnotation(DslMarkerUtils.DSL_MARKER_FQ_NAME)
}
