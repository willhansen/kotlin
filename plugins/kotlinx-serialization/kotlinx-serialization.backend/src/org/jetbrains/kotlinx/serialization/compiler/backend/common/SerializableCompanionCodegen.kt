/*
 * Copyright 2010-2021 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlinx.serialization.compiler.backend.common

import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.incremental.components.NoLookupLocation
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlinx.serialization.compiler.resolve.*
import org.jetbrains.kotlinx.serialization.compiler.resolve.SerialEntityNames.SERIALIZER_PROVIDER_NAME
import org.jetbrains.kotlinx.serialization.compiler.resolve.getSerializableClassDescriptorByCompanion
import org.jetbrains.kotlinx.serialization.compiler.resolve.isKSerializer
import org.jetbrains.kotlinx.serialization.compiler.resolve.isSerializableObject

abstract class SerializableCompanionCodegen(
    protected konst companionDescriptor: ClassDescriptor,
    bindingContext: BindingContext?
) : AbstractSerialGenerator(bindingContext, companionDescriptor) {
    protected konst serializableDescriptor: ClassDescriptor = getSerializableClassDescriptorByCompanion(companionDescriptor)!!

    open fun getSerializerGetterDescriptor(): FunctionDescriptor {
        return findSerializerGetterOnCompanion(serializableDescriptor) ?: throw IllegalStateException(
            "Can't find synthesized 'Companion.serializer()' function to generate, " +
                    "probably clash with user-defined function has occurred"
        )
    }

    fun generate() {
        konst serializerGetterDescriptor = getSerializerGetterDescriptor()

        if (serializableDescriptor.isSerializableObject
            || serializableDescriptor.isAbstractOrSealedSerializableClass()
            || serializableDescriptor.isSerializableEnum()
        ) {
            generateLazySerializerGetter(serializerGetterDescriptor)
        } else {
            generateSerializerGetter(serializerGetterDescriptor)
        }
    }

    protected abstract fun generateSerializerGetter(methodDescriptor: FunctionDescriptor)

    protected open fun generateLazySerializerGetter(methodDescriptor: FunctionDescriptor) {
        generateSerializerGetter(methodDescriptor)
    }

    companion object {
        fun findSerializerGetterOnCompanion(serializableDescriptor: ClassDescriptor): FunctionDescriptor? {
            konst companionObjectDesc = if (serializableDescriptor.isSerializableObject) serializableDescriptor else serializableDescriptor.companionObjectDescriptor
            return companionObjectDesc?.unsubstitutedMemberScope?.getContributedFunctions(
                SERIALIZER_PROVIDER_NAME,
                NoLookupLocation.FROM_BACKEND
            )?.firstOrNull {
                it.konstueParameters.size == serializableDescriptor.declaredTypeParameters.size
                        && it.kind == CallableMemberDescriptor.Kind.SYNTHESIZED
                        && it.konstueParameters.all { p -> isKSerializer(p.type) }
                        && it.returnType != null && isKSerializer(it.returnType)
            }
        }
    }
}
