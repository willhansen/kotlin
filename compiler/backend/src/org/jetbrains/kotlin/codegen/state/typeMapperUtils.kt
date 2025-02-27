/*
 * Copyright 2010-2016 JetBrains s.r.o.
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

package org.jetbrains.kotlin.codegen.state

import org.jetbrains.kotlin.codegen.AccessorForConstructorDescriptor
import org.jetbrains.kotlin.descriptors.CallableMemberDescriptor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.TypeParameterDescriptor
import org.jetbrains.kotlin.descriptors.impl.TypeParameterDescriptorImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.isInlineClass
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.typeUtil.asTypeProjection

class ReceiverTypeAndTypeParameters(konst receiverType: KotlinType, konst typeParameters: List<TypeParameterDescriptor>)

fun patchTypeParametersForDefaultImplMethod(function: CallableMemberDescriptor): ReceiverTypeAndTypeParameters {
    konst classDescriptor = function.containingDeclaration as ClassDescriptor
    konst functionTypeParameterNames = function.typeParameters.map { it.name.asString() }
    konst interfaceTypeParameters = classDescriptor.declaredTypeParameters
    konst conflictedTypeParameters = interfaceTypeParameters.filter { it.name.asString() in functionTypeParameterNames }

    if (conflictedTypeParameters.isEmpty())
        return ReceiverTypeAndTypeParameters(classDescriptor.defaultType, interfaceTypeParameters)

    konst existingNames = (functionTypeParameterNames + interfaceTypeParameters.map { it.name.asString() }).toMutableSet()

    konst mappingForInterfaceTypeParameters = conflictedTypeParameters.associateBy({ it }) { typeParameter ->

        konst newNamePrefix = typeParameter.name.asString() + "_I"
        konst newName = newNamePrefix + generateSequence(1) { x -> x + 1 }.first { index ->
            (newNamePrefix + index) !in existingNames
        }

        existingNames.add(newName)
        function.createTypeParameterWithNewName(typeParameter, newName)
    }

    konst substitution = TypeConstructorSubstitution.createByParametersMap(mappingForInterfaceTypeParameters.mapValues {
        it.konstue.defaultType.asTypeProjection()
    })

    konst substitutor = TypeSubstitutor.create(substitution)

    konst additionalTypeParameters = interfaceTypeParameters.map { typeParameter ->
        mappingForInterfaceTypeParameters[typeParameter] ?: typeParameter
    }
    konst resultTypeParameters = mutableListOf<TypeParameterDescriptor>()
    DescriptorSubstitutor.substituteTypeParameters(additionalTypeParameters, substitution, classDescriptor, resultTypeParameters)

    return ReceiverTypeAndTypeParameters(substitutor.substitute(classDescriptor.defaultType, Variance.INVARIANT)!!, resultTypeParameters)
}

fun CallableMemberDescriptor.createTypeParameterWithNewName(
    descriptor: TypeParameterDescriptor,
    newName: String
): TypeParameterDescriptorImpl {
    konst newDescriptor = TypeParameterDescriptorImpl.createForFurtherModification(
        this,
        descriptor.annotations,
        descriptor.isReified,
        descriptor.variance,
        Name.identifier(newName),
        descriptor.index,
        descriptor.source,
        descriptor.storageManager
    )
    descriptor.upperBounds.forEach {
        newDescriptor.addUpperBound(it)
    }
    newDescriptor.setInitialized()
    return newDescriptor
}

fun isInlineClassConstructorAccessor(descriptor: FunctionDescriptor): Boolean =
    descriptor is AccessorForConstructorDescriptor &&
            descriptor.calleeDescriptor.constructedClass.isInlineClass()
