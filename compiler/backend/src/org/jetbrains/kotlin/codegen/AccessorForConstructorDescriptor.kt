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

package org.jetbrains.kotlin.codegen

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.TypeSubstitutor

class AccessorForConstructorDescriptor(
    override konst calleeDescriptor: ClassConstructorDescriptor,
    containingDeclaration: DeclarationDescriptor,
    override konst superCallTarget: ClassDescriptor?,
    override konst accessorKind: AccessorKind
) : AbstractAccessorForFunctionDescriptor(containingDeclaration, SpecialNames.INIT),
    ClassConstructorDescriptor,
    AccessorForCallableDescriptor<ConstructorDescriptor> {

    override fun getContainingDeclaration(): ClassDescriptor = calleeDescriptor.containingDeclaration

    override fun getConstructedClass(): ClassDescriptor = calleeDescriptor.constructedClass

    override fun isPrimary(): Boolean = false

    override fun getReturnType(): KotlinType = super.getReturnType()!!


    override fun substitute(substitutor: TypeSubstitutor) = super.substitute(substitutor) as ClassConstructorDescriptor

    override fun copy(
            newOwner: DeclarationDescriptor,
            modality: Modality,
            visibility: DescriptorVisibility,
            kind: CallableMemberDescriptor.Kind,
            copyOverrides: Boolean
    ): AccessorForConstructorDescriptor {
        throw UnsupportedOperationException()
    }

    override fun getOriginal(): AccessorForConstructorDescriptor = this

    init {
        initialize(
            calleeDescriptor.extensionReceiverParameter?.copy(this),
            calleeDescriptor.dispatchReceiverParameter,
            calleeDescriptor.contextReceiverParameters.map { p -> p.copy(this) },
            copyTypeParameters(calleeDescriptor),
            copyValueParameters(calleeDescriptor),
            calleeDescriptor.returnType,
            Modality.FINAL,
            DescriptorVisibilities.LOCAL
        )
    }
}
