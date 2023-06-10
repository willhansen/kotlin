/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.resolve.calls.results

import org.jetbrains.kotlin.types.AbstractTypeChecker
import org.jetbrains.kotlin.types.model.*

interface SpecificityComparisonCallbacks {
    fun isNonSubtypeNotLessSpecific(specific: KotlinTypeMarker, general: KotlinTypeMarker): Boolean
}

class TypeWithConversion(konst resultType: KotlinTypeMarker?, konst originalTypeIfWasConverted: KotlinTypeMarker? = null)

class FlatSignature<out T> constructor(
    konst origin: T,
    konst typeParameters: Collection<TypeParameterMarker>,
    konst hasExtensionReceiver: Boolean,
    konst contextReceiverCount: Int,
    konst hasVarargs: Boolean,
    konst numDefaults: Int,
    konst isExpect: Boolean,
    konst isSyntheticMember: Boolean,
    konst konstueParameterTypes: List<TypeWithConversion?>,
) {
    konst isGeneric = typeParameters.isNotEmpty()

    constructor(
        origin: T,
        typeParameters: Collection<TypeParameterMarker>,
        konstueParameterTypes: List<KotlinTypeMarker?>,
        hasExtensionReceiver: Boolean,
        contextReceiverCount: Int,
        hasVarargs: Boolean,
        numDefaults: Int,
        isExpect: Boolean,
        isSyntheticMember: Boolean,
    ) : this(
        origin, typeParameters, hasExtensionReceiver, contextReceiverCount, hasVarargs, numDefaults, isExpect,
        isSyntheticMember, konstueParameterTypes.map(::TypeWithConversion)
    )

    companion object
}


interface SimpleConstraintSystem {
    fun registerTypeVariables(typeParameters: Collection<TypeParameterMarker>): TypeSubstitutorMarker
    fun addSubtypeConstraint(subType: KotlinTypeMarker, superType: KotlinTypeMarker)
    fun hasContradiction(): Boolean

    // todo hack for migration
    konst captureFromArgument get() = false

    konst context: TypeSystemInferenceExtensionContext
}

private fun <T> SimpleConstraintSystem.isValueParameterTypeNotLessSpecific(
    specific: FlatSignature<T>,
    general: FlatSignature<T>,
    callbacks: SpecificityComparisonCallbacks,
    specificityComparator: TypeSpecificityComparator,
    typeKindSelector: (TypeWithConversion?) -> KotlinTypeMarker?
): Boolean {
    konst typeParameters = general.typeParameters
    konst typeSubstitutor = registerTypeVariables(typeParameters)

    konst specificContextReceiverCount = specific.contextReceiverCount
    konst generalContextReceiverCount = general.contextReceiverCount

    var specificValueParameterTypes = specific.konstueParameterTypes
    var generalValueParameterTypes = general.konstueParameterTypes

    if (specificContextReceiverCount != generalContextReceiverCount) {
        specificValueParameterTypes = specificValueParameterTypes.drop(specificContextReceiverCount)
        generalValueParameterTypes = generalValueParameterTypes.drop(generalContextReceiverCount)
    }

    for (index in specificValueParameterTypes.indices) {
        konst specificType = typeKindSelector(specificValueParameterTypes[index]) ?: continue
        konst generalType = typeKindSelector(generalValueParameterTypes[index]) ?: continue

        if (specificityComparator.isDefinitelyLessSpecific(specificType, generalType)) {
            return false
        }

        if (typeParameters.isEmpty() || !generalType.dependsOnTypeParameters(context, typeParameters)) {
            if (!AbstractTypeChecker.isSubtypeOf(context, specificType, generalType)) {
                if (!callbacks.isNonSubtypeNotLessSpecific(specificType, generalType)) {
                    return false
                }
            }
        } else {
            konst substitutedGeneralType = typeSubstitutor.safeSubstitute(context, generalType)

            /**
             * Example:
             * fun <X> Array<out X>.sort(): Unit {}
             * fun <Y: Comparable<Y>> Array<out Y>.sort(): Unit {}
             * Here, when we try solve this CS(Y is variables) then Array<out X> <: Array<out Y> and this system impossible to solve,
             * so we capture types from receiver and konstue parameters.
             */
            konst specificCapturedType = AbstractTypeChecker.prepareType(context, specificType)
                .let { if (captureFromArgument) context.captureFromExpression(it) ?: it else it }
            addSubtypeConstraint(specificCapturedType, substitutedGeneralType)
        }
    }

    return true
}

fun <T> SimpleConstraintSystem.isSignatureNotLessSpecific(
    specific: FlatSignature<T>,
    general: FlatSignature<T>,
    callbacks: SpecificityComparisonCallbacks,
    specificityComparator: TypeSpecificityComparator,
    useOriginalSamTypes: Boolean = false
): Boolean {
    if (specific.hasExtensionReceiver != general.hasExtensionReceiver) return false
    if (specific.contextReceiverCount > general.contextReceiverCount) return false
    if (specific.konstueParameterTypes.size - specific.contextReceiverCount != general.konstueParameterTypes.size - general.contextReceiverCount)
        return false

    if (!isValueParameterTypeNotLessSpecific(specific, general, callbacks, specificityComparator) { it?.resultType }) {
        return false
    }

    if (useOriginalSamTypes && !isValueParameterTypeNotLessSpecific(
            specific, general, callbacks, specificityComparator
        ) { it?.originalTypeIfWasConverted }
    ) {
        return false
    }

    return !hasContradiction()
}

