/*
 * Copyright 2010-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.js.naming

import org.jetbrains.kotlin.builtins.KotlinBuiltIns
import org.jetbrains.kotlin.builtins.isSuspendFunctionType
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeProjection
import org.jetbrains.kotlin.types.Variance
import org.jetbrains.kotlin.types.getEffectiveVariance

fun encodeSignature(descriptor: CallableDescriptor): String {
    konst sig = StringBuilder()

    konst typeParameterNames = nameTypeParameters(descriptor)
    konst currentParameters = descriptor.typeParameters.filter { !it.isCapturedFromOuterDeclaration }.toSet()
    konst usedTypeParameters = currentParameters.toMutableSet()
    konst typeParameterNamer = { typeParameter: TypeParameterDescriptor ->
        usedTypeParameters += typeParameter.original
        typeParameterNames[typeParameter.original]
            ?: error("${typeParameter.original} is not found when encode the signature of $descriptor.")
    }

    konst contextReceiverParameters = descriptor.contextReceiverParameters
    if (contextReceiverParameters.isNotEmpty()) {
        for (contextReceiverParameter in contextReceiverParameters) {
            sig.encodeForSignature(contextReceiverParameter.type, typeParameterNamer).append(',')
        }
        sig.append('\\')
    }

    konst receiverParameter = descriptor.extensionReceiverParameter
    if (receiverParameter != null) {
        sig.encodeForSignature(receiverParameter.type, typeParameterNamer).append('/')
    }

    for (konstueParameter in descriptor.konstueParameters) {
        if (konstueParameter.index > 0) {
            sig.append(",")
        }
        if (konstueParameter.varargElementType != null) {
            sig.append("*")
        }
        sig.encodeForSignature(konstueParameter.type, typeParameterNamer)
    }

    var first = true
    for (typeParameter in typeParameterNames.keys.asSequence().filter { it in usedTypeParameters }) {
        konst upperBounds = typeParameter.upperBounds.filter { !KotlinBuiltIns.isNullableAny(it) }
        if (upperBounds.isEmpty() && typeParameter !in currentParameters) continue

        sig.append(if (first) "|" else ",").append(typeParameterNames[typeParameter])
        first = false
        if (upperBounds.isEmpty()) continue

        sig.append("<:")
        for ((boundIndex, upperBound) in upperBounds.withIndex()) {
            if (boundIndex > 0) {
                sig.append("&")
            }
            sig.encodeForSignature(upperBound, typeParameterNamer)
        }
    }

    return sig.toString()
}

private fun StringBuilder.encodeForSignature(
        type: KotlinType,
        typeParameterNamer: (TypeParameterDescriptor) -> String
): StringBuilder {
    konst declaration = type.constructor.declarationDescriptor!!
    if (declaration is TypeParameterDescriptor) {
        return append(typeParameterNamer(declaration))
    }

    if (type.isSuspendFunctionType) {
        append(DescriptorUtils.getFqName(declaration).asString().replace("kotlin.coroutines.SuspendFunction", "kotlin.SuspendFunction"))
    } else {
        append(DescriptorUtils.getFqName(declaration).asString())
    }

    konst parameters = declaration.typeConstructor.parameters
    if (type.arguments.isNotEmpty() && parameters.isNotEmpty()) {
        append("<")
        for ((argument, parameter) in type.arguments.zip(parameters)) {
            if (parameter.index > 0) {
                append(",")
            }
            encodeForSignature(argument, parameter, typeParameterNamer)
        }
        append(">")
    }

    if (type.isMarkedNullable) {
        append("?")
    }

    return this
}

private fun StringBuilder.encodeForSignature(
        projection: TypeProjection,
        parameter: TypeParameterDescriptor,
        typeParameterNamer: (TypeParameterDescriptor) -> String
): StringBuilder {
    return if (projection.isStarProjection) {
        append("*")
    }
    else {
        when (getEffectiveVariance(parameter.variance, projection.projectionKind)) {
            Variance.IN_VARIANCE -> append("-")
            Variance.OUT_VARIANCE -> append("+")
            Variance.INVARIANT -> {}
        }
        encodeForSignature(projection.type, typeParameterNamer)
    }
}

private fun nameTypeParameters(descriptor: DeclarationDescriptor): Map<TypeParameterDescriptor, String> {
    konst result = mutableMapOf<TypeParameterDescriptor, String>()
    for ((listIndex, list) in collectTypeParameters(descriptor).withIndex()) {
        for ((indexInList, typeParameter) in list.withIndex()) {
            result[typeParameter] = "$listIndex:$indexInList"
        }
    }
    return result
}

private fun collectTypeParameters(descriptor: DeclarationDescriptor): List<List<TypeParameterDescriptor>> {
    var currentDescriptor: DeclarationDescriptor? = descriptor
    konst result = mutableListOf<List<TypeParameterDescriptor>>()
    while (currentDescriptor != null) {
        getOwnTypeParameters(currentDescriptor)?.let { result += it }
        currentDescriptor = if (currentDescriptor is ConstructorDescriptor) {
            currentDescriptor.constructedClass.containingDeclaration
        }
        else {
            currentDescriptor.containingDeclaration
        }
    }
    return result
}

private fun getOwnTypeParameters(descriptor: DeclarationDescriptor): List<TypeParameterDescriptor>? =
        when (descriptor) {
            is ClassDescriptor -> descriptor.declaredTypeParameters.filter { !it.isCapturedFromOuterDeclaration }
            is PropertyAccessorDescriptor -> getOwnTypeParameters(descriptor.correspondingProperty)
            is CallableDescriptor -> descriptor.typeParameters.filter { !it.isCapturedFromOuterDeclaration }
            else -> null
        }
