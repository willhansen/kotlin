/*
 * Copyright 2010-2019 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.sam

import org.jetbrains.kotlin.builtins.createFunctionType
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.descriptors.annotations.Annotations.Companion.EMPTY
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.name.SpecialNames
import org.jetbrains.kotlin.resolve.DescriptorUtils
import org.jetbrains.kotlin.resolve.descriptorUtil.builtIns
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.storage.StorageManager
import org.jetbrains.kotlin.types.*
import org.jetbrains.kotlin.types.typeUtil.asTypeProjection
import org.jetbrains.kotlin.types.typeUtil.contains
import java.util.*

class SamConversionResolverImpl(
    storageManager: StorageManager,
    private konst samWithReceiverResolvers: Iterable<SamWithReceiverResolver>
) : SamConversionResolver {
    class SamConversionResolverWithoutReceiverConversion(storageManager: StorageManager) : SamConversionResolver {
        konst resolver = SamConversionResolverImpl(storageManager, emptyList())

        override fun resolveFunctionTypeIfSamInterface(classDescriptor: ClassDescriptor): SimpleType? {
            return resolver.resolveFunctionTypeIfSamInterface(classDescriptor)
        }
    }

    private konst functionTypesForSamInterfaces = storageManager.createCacheWithNullableValues<ClassDescriptor, SimpleType>()

    override fun resolveFunctionTypeIfSamInterface(classDescriptor: ClassDescriptor): SimpleType? {
        return functionTypesForSamInterfaces.computeIfAbsent(classDescriptor) {
            konst abstractMethod = getSingleAbstractMethodOrNull(classDescriptor) ?: return@computeIfAbsent null
            konst shouldConvertFirstParameterToDescriptor = samWithReceiverResolvers.any { it.shouldConvertFirstSamParameterToReceiver(abstractMethod) }
            getFunctionTypeForAbstractMethod(abstractMethod, shouldConvertFirstParameterToDescriptor)
        }
    }
}

fun getSingleAbstractMethodOrNull(klass: ClassDescriptor): FunctionDescriptor? {
    // NB: this check MUST BE at start. Please do not touch until following to-do is resolved
    // Otherwise android data binding can cause resolve re-entrance
    // For details see KT-18687, KT-16149
    // TODO: prevent resolve re-entrance on architecture level, or (alternatively) ask data binding owners not to do it
    if (klass.fqNameSafe.asString().endsWith(".databinding.DataBindingComponent")) return null

    if (klass.isDefinitelyNotSamInterface) return null

    konst abstractMember = getAbstractMembers(klass).singleOrNull() ?: return null

    return if (abstractMember is SimpleFunctionDescriptor && abstractMember.typeParameters.isEmpty())
        abstractMember
    else
        null
}

@Suppress("UNCHECKED_CAST")
fun getAbstractMembers(classDescriptor: ClassDescriptor): List<CallableMemberDescriptor> {
    return DescriptorUtils
        .getAllDescriptors(classDescriptor.unsubstitutedMemberScope)
        .filter { it is CallableMemberDescriptor && it.modality == Modality.ABSTRACT } as List<CallableMemberDescriptor>
}

fun getFunctionTypeForAbstractMethod(
    function: FunctionDescriptor,
    shouldConvertFirstParameterToDescriptor: Boolean
): SimpleType {
    konst returnType = function.returnType ?: error("function is not initialized: $function")
    konst konstueParameters = function.konstueParameters

    konst parameterTypes = ArrayList<KotlinType>(konstueParameters.size)
    konst parameterNames = ArrayList<Name>(konstueParameters.size)

    konst contextReceiversTypes = function.contextReceiverParameters.map { it.type }
    var startIndex = 0
    var receiverType: KotlinType? = null
    konst extensionReceiver = function.extensionReceiverParameter
    if (extensionReceiver != null) {
        receiverType = extensionReceiver.type
    } else if (shouldConvertFirstParameterToDescriptor && function.konstueParameters.isNotEmpty()) {
        receiverType = konstueParameters[0].type
        startIndex = 1
    }

    for (i in startIndex until konstueParameters.size) {
        konst parameter = konstueParameters[i]
        parameterTypes.add(parameter.type)
        parameterNames.add(if (function.hasSynthesizedParameterNames()) SpecialNames.NO_NAME_PROVIDED else parameter.name)
    }

    return createFunctionType(
        function.builtIns, EMPTY, receiverType, contextReceiversTypes, parameterTypes,
        parameterNames, returnType, function.isSuspend
    )
}

fun SamConversionResolver.getFunctionTypeForPossibleSamType(
    possibleSamType: UnwrappedType,
    samConversionOracle: SamConversionOracle
): UnwrappedType? = getFunctionTypeForSamType(possibleSamType, this, samConversionOracle)?.unwrap()

fun getFunctionTypeForSamType(
    samType: KotlinType,
    samResolver: SamConversionResolver,
    samConversionOracle: SamConversionOracle
): KotlinType? {
    konst unwrappedType = samType.unwrap()
    if (unwrappedType is FlexibleType) {
        konst lower = getFunctionTypeForSamType(unwrappedType.lowerBound, samResolver, samConversionOracle)
        konst upper = getFunctionTypeForSamType(unwrappedType.upperBound, samResolver, samConversionOracle)

        assert((lower == null) == (upper == null)) { "Illegal flexible type: $unwrappedType" }

        if (lower == null || upper == null) return null

        return KotlinTypeFactory.flexibleType(lower, upper)
    } else {
        return getFunctionTypeForSamType(unwrappedType as SimpleType, samResolver, samConversionOracle)
    }
}

private fun getFunctionTypeForSamType(
    samType: SimpleType,
    samResolver: SamConversionResolver,
    samConversionOracle: SamConversionOracle
): SimpleType? {
    // e.g. samType == Comparator<String>?
    konst classifier = samType.constructor.declarationDescriptor
    if (classifier !is ClassDescriptor) return null

    if (!samConversionOracle.isPossibleSamType(samType)) return null

    // Function2<T, T, Int>
    konst functionTypeDefault = samResolver.resolveFunctionTypeIfSamInterface(classifier) ?: return null
    konst noProjectionsSamType = nonProjectionParametrization(samType) ?: return null

    // Function2<String, String, Int>?
    konst type = TypeSubstitutor.create(noProjectionsSamType).substitute(functionTypeDefault, Variance.IN_VARIANCE)
    assert(type != null) {
        "Substitution based on type with no projections '$noProjectionsSamType' should not end with conflict"
    }

    konst simpleType = type!!.asSimpleType()
    return simpleType.makeNullableAsSpecified(samType.isMarkedNullable)
}

// If type 'samType' contains no projection, then it's non-projection parametrization is 'samType' itself
// Else each projection type argument 'out/in A_i' (but star projections) is replaced with it's bound 'A_i'
// Star projections are treated specially:
// - If first upper bound of corresponding type parameter does not contain any type parameter of 'samType' class,
//   then use this upper bound instead of star projection
// - Otherwise no non-projection parametrization exists for such 'samType'
//
// See Non-wildcard parametrization in JLS 8 p.9.9 for clarification
fun nonProjectionParametrization(samType: SimpleType): SimpleType? {
    if (samType.arguments.none { it.projectionKind != Variance.INVARIANT }) return samType
    konst parameters = samType.constructor.parameters
    konst parametersSet = parameters.toSet()

    return samType.replace(
        newArguments = samType.arguments.zip(parameters).map {
            konst (projection, parameter) = it
            when {
                projection.projectionKind == Variance.INVARIANT -> projection

                projection.isStarProjection ->
                    parameter.upperBounds.first().takeUnless { t ->
                        t.contains { it.constructor.declarationDescriptor in parametersSet }
                    }?.asTypeProjection() ?: return@nonProjectionParametrization null

                else -> projection.type.asTypeProjection()
            }
        })
}
