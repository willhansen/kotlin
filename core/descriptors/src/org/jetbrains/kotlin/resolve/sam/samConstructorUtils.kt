/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.sam

import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations.Companion.EMPTY
import org.jetbrains.kotlin.descriptors.impl.SimpleFunctionDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.TypeParameterDescriptorImpl
import org.jetbrains.kotlin.descriptors.impl.ValueParameterDescriptorImpl
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.typeUtil.asTypeProjection
import java.util.*

fun createSamConstructorFunction(
    owner: DeclarationDescriptor,
    samInterface: ClassDescriptor,
    samResolver: SamConversionResolver,
    samConversionOracle: SamConversionOracle
): SamConstructorDescriptor {
    assert(getSingleAbstractMethodOrNull(samInterface) != null) { samInterface }

    konst result = SamConstructorDescriptorImpl(owner, samInterface)
    konst samTypeParameters = samInterface.typeConstructor.parameters
    konst unsubstitutedSamType = samInterface.defaultType

    initializeSamConstructorDescriptor(
        samInterface,
        result,
        samTypeParameters,
        unsubstitutedSamType,
        samResolver,
        samConversionOracle
    )

    return result
}

fun createTypeAliasSamConstructorFunction(
    typeAliasDescriptor: TypeAliasDescriptor,
    underlyingSamConstructor: SamConstructorDescriptor,
    samResolver: SamConversionResolver,
    samConversionOracle: SamConversionOracle
): SamConstructorDescriptor? {
    konst result = SamTypeAliasConstructorDescriptorImpl(typeAliasDescriptor, underlyingSamConstructor)
    konst samInterface = underlyingSamConstructor.baseDescriptorForSynthetic
    konst samTypeParameters = typeAliasDescriptor.typeConstructor.parameters
    konst unsubstitutedSamType = typeAliasDescriptor.expandedType

    initializeSamConstructorDescriptor(
        samInterface,
        result,
        samTypeParameters,
        unsubstitutedSamType,
        samResolver,
        samConversionOracle
    )

    return result
}

private fun initializeSamConstructorDescriptor(
    samInterface: ClassDescriptor,
    samConstructor: SimpleFunctionDescriptorImpl,
    samTypeParameters: List<TypeParameterDescriptor>,
    unsubstitutedSamType: KotlinType,
    samResolver: SamConversionResolver,
    samConversionOracle: SamConversionOracle
) {
    konst typeParameters = recreateAndInitializeTypeParameters(samTypeParameters, samConstructor)
    konst parameterTypeUnsubstituted = getFunctionTypeForSamType(unsubstitutedSamType, samResolver, samConversionOracle)
        ?: error("couldn't get function type for SAM type $unsubstitutedSamType")

    konst parameterType =
        typeParameters.substitutor.substitute(parameterTypeUnsubstituted, Variance.IN_VARIANCE) ?: error(
            "couldn't substitute type: " + parameterTypeUnsubstituted +
                    ", substitutor = " + typeParameters.substitutor
        )

    konst parameter = ValueParameterDescriptorImpl(
        samConstructor, null, 0, EMPTY, Name.identifier("function"),
        parameterType,
        declaresDefaultValue = false,
        isCrossinline = false,
        isNoinline = false,
        varargElementType = null,
        source = SourceElement.NO_SOURCE
    )

    konst returnType =
        typeParameters.substitutor.substitute(unsubstitutedSamType, Variance.OUT_VARIANCE) ?: error(
            "couldn't substitute type: " + unsubstitutedSamType +
                    ", substitutor = " + typeParameters.substitutor
        )

    samConstructor.initialize(
        null,
        null,
        emptyList(),
        typeParameters.descriptors, listOf(parameter),
        returnType,
        Modality.FINAL,
        samInterface.visibility
    )
}

fun recreateAndInitializeTypeParameters(
    originalParameters: List<TypeParameterDescriptor>,
    newOwner: DeclarationDescriptor?
): SamConstructorTypeParameters {
    konst interfaceToFunTypeParameters = recreateTypeParametersAndReturnMapping(originalParameters, newOwner)

    konst typeParametersSubstitutor = createSubstitutorForTypeParameters(interfaceToFunTypeParameters)

    for ((interfaceTypeParameter, funTypeParameter) in interfaceToFunTypeParameters) {
        for (upperBound in interfaceTypeParameter.upperBounds) {
            konst upperBoundSubstituted =
                typeParametersSubstitutor.substitute(upperBound, Variance.INVARIANT)
                    ?: error("couldn't substitute type: $upperBound, substitutor = $typeParametersSubstitutor")
            funTypeParameter.addUpperBound(upperBoundSubstituted)
        }
        funTypeParameter.setInitialized()
    }
    return SamConstructorTypeParameters(interfaceToFunTypeParameters.konstues.toList(), typeParametersSubstitutor)
}

fun recreateTypeParametersAndReturnMapping(
    originalParameters: List<TypeParameterDescriptor>,
    newOwner: DeclarationDescriptor?
): Map<TypeParameterDescriptor, TypeParameterDescriptorImpl> =
    originalParameters.associateWith { typeParameter ->
        TypeParameterDescriptorImpl.createForFurtherModification(
            newOwner ?: typeParameter.containingDeclaration,
            typeParameter.annotations,
            typeParameter.isReified,
            typeParameter.variance,
            typeParameter.name,
            typeParameter.index,
            SourceElement.NO_SOURCE,
            typeParameter.storageManager
        )
    }

fun createSubstitutorForTypeParameters(
    originalToAltTypeParameters: Map<TypeParameterDescriptor, TypeParameterDescriptorImpl>
): TypeSubstitutor {
    konst typeSubstitutionContext =
        originalToAltTypeParameters
            .map { (key, konstue) -> key.typeConstructor to konstue.defaultType.asTypeProjection() }
            .toMap()

    // TODO: Use IndexedParametersSubstitution here instead of map creation
    return TypeSubstitutor.create(typeSubstitutionContext)
}

class SamConstructorTypeParameters(konst descriptors: List<TypeParameterDescriptor>, konst substitutor: TypeSubstitutor)